import {handleLogout, fetchWithAuth} from '../utils/authUtils.js';
import {showConfirm, showAlert, createModal} from '../utils/modalUtils.js';

document.addEventListener('DOMContentLoaded', () => {
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) logoutBtn.addEventListener('click', handleLogout);

    const homeLink = document.getElementById('homeLink');
    if (homeLink) homeLink.addEventListener('click', () => window.location.href = '/main/index.html');

    initCategoryAdmin();
});

const categoryGrid = document.getElementById('categoryGrid');
const paginationContainer = document.getElementById('pagination');
const searchInput = document.getElementById('searchInput');
const searchBtn = document.getElementById('searchBtn');
const resetBtn = document.getElementById('resetBtn');
const addCategoryBtn = document.getElementById('addCategoryBtn');

let currentPage = 0;
let currentSearch = '';
let totalPages = 0;

/**
 * Инициализирует логику управления категориями и обработчики поиска/сброса/добавления.
 */
async function initCategoryAdmin() {
    await fetchCategories();

    searchBtn.addEventListener('click', () => fetchCategories(0, searchInput.value.trim()));
    searchInput.addEventListener('keydown', e => {
        if (e.key === 'Enter') fetchCategories(0, searchInput.value.trim());
    });

    resetBtn.addEventListener('click', () => {
        searchInput.value = '';
        fetchCategories(0, '');
    });

    addCategoryBtn.addEventListener('click', () => {
        createModal({
            title: 'Создать категорию',
            bodyHtml: '<input id="newCategoryTitle" type="text" placeholder="Название категории" minlength="2" maxlength="100" style="width: 100%; padding: 8px;"/>',
            buttons: [{
                text: 'OK', role: 'ok', onClick: async () => {
                    const input = document.getElementById('newCategoryTitle');
                    const title = input.value.trim();
                    if (title.length < 2 || title.length > 100) {
                        return showAlert('Название категории должно быть от 2 до 100 символов');
                    }
                    try {
                        const res = await fetchWithAuth('/ims/categories', {
                            method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({title})
                        });
                        if (!res || !res.ok) throw new Error('Ошибка создания');
                        await fetchCategories(currentPage, currentSearch);
                    } catch (err) {
                        showAlert(err.message || 'Не удалось создать категорию');
                    }
                }
            }]
        });
    });
}

/**
 * Получает и отображает список категорий с учётом пагинации и поиска.
 * @param {number} page - Номер страницы (по умолчанию 0)
 * @param {string} search - Строка поиска (по умолчанию '')
 */
async function fetchCategories(page = 0, search = '') {
    const endpoint = search ? `/ims/categories/search/like?title=${encodeURIComponent(search)}&pageNumber=${page}` : `/ims/categories?pageNumber=${page}`;

    const res = await fetchWithAuth(endpoint);
    if (!res || !res.ok) {
        categoryGrid.innerHTML = '<p>Ошибка загрузки категорий</p>';
        paginationContainer.innerHTML = '';
        return;
    }

    const data = await res.json();
    totalPages = data.totalPages;
    currentPage = page;
    currentSearch = search;

    renderCategories(data.content);
    renderPagination(totalPages, currentPage);
}

/**
 * Отображает список карточек категорий на странице.
 * @param {Array} categories - Список категорий
 */
function renderCategories(categories) {
    categoryGrid.innerHTML = '';
    categories.forEach(cat => {
        const id = cat.categoryId || cat.id;
        const card = document.createElement('div');
        card.className = 'category-item';

        const titleDiv = document.createElement('div');
        titleDiv.className = 'category-title';
        titleDiv.textContent = cat.title.length > 25 ? cat.title.slice(0, 25) + '...' : cat.title;

        const actions = document.createElement('div');
        actions.className = 'category-actions';

        const editBtn = document.createElement('button');
        editBtn.className = 'action-btn edit';
        editBtn.title = 'Редактировать';
        editBtn.addEventListener('click', () => {
            createModal({
                title: 'Редактировать категорию',
                bodyHtml: `<input id="editCategoryTitle" type="text" value="${cat.title}" style="width:100%; padding:8px;"/>`,
                buttons: [{
                    text: 'OK', role: 'ok', onClick: async () => {
                        const input = document.getElementById('editCategoryTitle');
                        const newTitle = input.value.trim();
                        if (!newTitle) {
                            return showAlert('Название не может быть пустым');
                        }
                        try {
                            const res = await fetchWithAuth('/ims/categories', {
                                method: 'PUT',
                                headers: {'Content-Type': 'application/json'},
                                body: JSON.stringify({categoryId: id, title: newTitle})
                            });
                            if (!res || !res.ok) {
                                throw new Error('Ошибка обновления');
                            }
                            await fetchCategories(currentPage, currentSearch);
                        } catch (err) {
                            showAlert(err.message || 'Не удалось обновить категорию');
                        }
                    }
                }]
            });
        });

        const delBtn = document.createElement('button');
        delBtn.className = 'action-btn delete';
        delBtn.title = 'Удалить';
        delBtn.addEventListener('click', async () => {
            const confirmed = await showConfirm(`Удалить категорию "${cat.title}"?`);
            if (!confirmed) {
                return;
            }
            try {
                const res = await fetchWithAuth(`/ims/categories/${id}`, {method: 'DELETE'});
                if (!res || !res.ok) {
                    throw new Error('Ошибка удаления');
                }
                await fetchCategories(currentPage, currentSearch);
            } catch (err) {
                showAlert(err.message || 'Не удалось удалить категорию');
            }
        });

        actions.appendChild(editBtn);
        actions.appendChild(delBtn);
        card.appendChild(titleDiv);
        card.appendChild(actions);
        categoryGrid.appendChild(card);
    });
}

/**
 * Отображает кнопки пагинации и настраивает переход между страницами.
 * @param {number} total - Общее количество страниц
 * @param {number} current - Текущая страница
 */
function renderPagination(total, current) {
    paginationContainer.innerHTML = '';
    const delta = 2;
    const pages = [];
    for (let i = 0; i < total; i++) {
        if (i === 0 || i === total - 1 || (i >= current - delta && i <= current + delta)) {
            pages.push(i);
        }
    }
    pages.forEach(i => {
        const btn = document.createElement('button');
        btn.textContent = i + 1;
        if (i === current) {
            btn.classList.add('active');
            btn.style.pointerEvents = 'none';
        }
        btn.addEventListener('click', () => fetchCategories(i, currentSearch));
        paginationContainer.appendChild(btn);
    });
}
