import {
    handleLogout, tryRefreshTokens, fetchWithAuth, startTokenAutoRefresh, getCurrentUserId
} from '../utils/authUtils.js';

import {
    showConfirm, createModal, showAlert
} from '../utils/modalUtils.js';

import {
    collectFiltersFromForm, updateSelectedCategories, initCategorySearch, validateDates, debounce
} from '../utils/filterUtils.js';

let currentPage = 1;
let selectedCategories = [];

/**
 * Загружает список стажировок с учётом фильтров и пагинации.
 * Использует `fetchWithAuth` для авторизованного доступа.
 *
 * @example
 * await loadInternships({ mode: 'advanced', category: 'IT' });
 */
async function loadInternships(searchParams = {}) {
    const isSearch = Boolean(searchParams.mode);
    const pageIndex = currentPage - 1;
    const url = isSearch ? `/ims/internships/search?pageNumber=${pageIndex}` : `/ims/internships?pageNumber=${pageIndex}`;

    const fetchOptions = {
        method: isSearch ? 'POST' : 'GET', ...(isSearch && {
            headers: {'Content-Type': 'application/json'}, body: JSON.stringify(searchParams)
        })
    };

    const res = await fetchWithAuth(url, fetchOptions);
    if (!res) return;
    if (!res.ok) {
        showAlert('Ошибка при загрузке стажировок');
        return;
    }
    const data = await res.json();
    displayInternships(data.content);
    renderPagination(data.totalPages);
}

/**
 * Отображает карточки стажировок в интерфейсе.
 * Показывает сообщение, если результатов нет.
 */
function displayInternships(internships) {
    const container = document.getElementById('internshipsGrid');
    const pag = document.getElementById('pagination');
    const isAdmin = localStorage.getItem('admin') === 'true';

    if (!internships.length) {
        container.innerHTML = `
            <div class="no-results-message">
                По заданным параметрам стажировок не найдено :( <br>
                Попробуйте поискать что-то другое!
            </div>
        `;
        pag.style.display = 'none';
        return;
    }

    pag.style.display = '';

    container.innerHTML = internships.map(i => {
        const createdStr = new Date(i.createdAt).toLocaleDateString('ru-RU');
        const updatedStr = i.updatedAt ? new Date(i.updatedAt).toLocaleDateString('ru-RU') : null;

        const categoriesHtml = i.categoriesData?.map(c => `<span class="category-badge">${c.title}</span>`).join(' ') || '';

        return `
        <div class="internship-card">
            <img src="${i.logoUrl}" alt="${i.title}" class="internship-logo">
            <h3>${i.title}</h3>
            <div class="categories-container">${categoriesHtml}</div>
            <p class="description">📌${i.description.substring(0, 250)}…</p>
            <p class="payment-type">
                ${i.paymentType === 'PAID' ? '💲Оплачиваемая стажировка' : '❎Неоплачиваемая стажировка'}
            </p>
            <p class="location">🌍 ${i.address}</p>
            <p class="created">🗓️Дата создания: ${createdStr}</p>
            ${updatedStr ? `<p class="updated">📅Дата обновления: ${updatedStr}</p>` : ''}
            <div class="card-actions">
                <button class="details-btn" onclick="showDetails(${i.internshipId})">
                  Подробнее
                </button>
                <button class="favorite-btn" onclick="addToFavorites(${i.internshipId})">
                  В избранное
                </button>
                ${isAdmin ? `
                    <button class="edit-btn" onclick="editInternship(${i.internshipId})">✏️</button>
                    <button class="delete-btn" onclick="deleteInternship(${i.internshipId})">🗑️</button>
                ` : ''}
            </div>
        </div>`;
    }).join('');
}

/**
 * Открывает модальное окно редактирования стажировки.
 * Загружает данные, инициализирует поля формы, категории и контакты.
 * После подтверждения отправляет обновлённые данные на сервер.
 *
 * @param {number} id - ID редактируемой стажировки.
 */
async function editInternship(id) {
    const token = localStorage.getItem('token');
    if (!token) return showAlert('Пожалуйста, войдите в систему.');

    // 1 - получаем текущие данные стажировки
    let dto;
    try {
        const res = await fetch(`/ims/internships/${id}`, {
            headers: {'Authorization': `Bearer ${token}`}
        });
        if (!res.ok) throw new Error('Не удалось загрузить данные');
        dto = await res.json();
    } catch (e) {
        return showAlert(e.message, 'Ошибка');
    }

    // 2 - рисуем модалку
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop';
    const win = document.createElement('div');
    win.className = 'modal-window';
    win.innerHTML = `
      <button class="modal-close-btn">&times;</button>
      <h2>Редактировать стажировку</h2>
      <div class="modal-body">
        <form id="editForm">
          <label>Заголовок</label>
          <input type="text" name="title" value="${dto.title}" required minlength="5" maxlength="100">
  
          <label>Описание</label>
          <textarea name="description" rows="4" required minlength="250" maxlength="2500">${dto.description}</textarea>
  
          <label>Тип оплаты</label>
          <select name="paymentType" required>
            <option value="PAID"   ${dto.paymentType === 'PAID' ? 'selected' : ''}>Оплачиваемая</option>
            <option value="UNPAID" ${dto.paymentType === 'UNPAID' ? 'selected' : ''}>Неоплачиваемая</option>
          </select>
  
          <label>Город</label>
          <input type="text" name="address" value="${dto.address}" required minlength="2" maxlength="100">
  
          <label>Категории</label>
          <div id="editCategorySearch" class="category-search">
            <input type="text" id="editCategoryInput" placeholder="Поиск категории…">
            <select id="editCategoryList" multiple size="5"></select>
            <div id="editSelectedCategories" class="selected-categories"></div>
          </div>
  
          <label>Контактные данные <button type="button" id="addContactBtn">＋</button></label>
          <div id="contactContainer" style="margin-bottom:20px;"></div>
  
          <label>Логотип (необязательно)</label>
          <input type="file" name="logoImg" accept="image/*">
          <input type="hidden" name="logoUrl" value="${dto.logoUrl}">
        </form>
      </div>
      <div class="modal-actions">
        <button type="button" class="cancel">Отмена</button>
        <button type="submit" form="editForm" class="ok-btn">Сохранить</button>
      </div>
    `;
    backdrop.appendChild(win);
    document.body.appendChild(backdrop);
    document.body.style.overflow = 'hidden';

    // авторазмер textarea
    const descTextarea = win.querySelector('textarea[name="description"]');
    if (descTextarea) {
        descTextarea.style.overflow = 'hidden';
        descTextarea.style.resize = 'none';
        descTextarea.addEventListener('input', function () {
            this.style.height = 'auto';
            this.style.height = this.scrollHeight + 'px';
        });
        descTextarea.dispatchEvent(new Event('input'));
    }

    // закрытие модалки
    win.querySelector('.modal-close-btn').onclick = win.querySelector('.cancel').onclick = () => {
        document.body.removeChild(backdrop);
        document.body.style.overflow = '';
    };

    // === категории ===
    let editSelected = [];
    const inp = win.querySelector('#editCategoryInput');
    const listBox = win.querySelector('#editCategoryList');
    const tags = win.querySelector('#editSelectedCategories');

    function renderEditCategoryTags() {
        tags.innerHTML = editSelected.map(c => `<div class="category-tag">
               ${c.title}
               <span data-id="${c.id}" class="remove-category">&times;</span>
             </div>`).join('');
        tags.querySelectorAll('.remove-category').forEach(btn => {
            const cid = btn.dataset.id;
            if (editSelected.length <= 1) {
                btn.style.opacity = '0.3';
                btn.style.cursor = 'default';
                btn.onclick = null;
            } else {
                btn.style.opacity = '1';
                btn.style.cursor = 'pointer';
                btn.onclick = () => {
                    editSelected = editSelected.filter(c => c.id !== cid);
                    renderEditCategoryTags();
                };
            }
        });
    }

    inp.addEventListener('input', debounce(async () => {
        const q = inp.value.trim();
        if (q.length < 2) {
            listBox.innerHTML = '';
            return;
        }
        const res = await fetchWithAuth(`/ims/categories/autocomplete?searchStr=${encodeURIComponent(q)}`, {method: 'GET'});
        if (!res.ok) return;
        const cats = await res.json();
        listBox.innerHTML = cats.map(cat => `<option value="${cat.categoryId}" data-title="${cat.title}">${cat.title}</option>`).join('');
    }), 300);

    listBox.addEventListener('dblclick', () => {
        Array.from(listBox.selectedOptions).forEach(opt => {
            const id = opt.value, title = opt.dataset.title;
            if (!editSelected.some(c => c.id === id)) editSelected.push({id, title});
        });
        inp.value = '';
        listBox.innerHTML = '';
        renderEditCategoryTags();
    });

    dto.categoriesData.forEach(c => editSelected.push({id: String(c.categoryId), title: c.title}));
    renderEditCategoryTags();

    // === контактные данные ===
    const contactContainer = win.querySelector('#contactContainer');
    const CONTACT_TYPES = ['TELEGRAM', 'EMAIL', 'WEBSITE'];

    function addContactRow(type = 'TELEGRAM', value = '') {
        const row = document.createElement('div');
        row.className = 'contact-row';
        row.innerHTML = `
          <select class="contact-type">
            ${CONTACT_TYPES.map(t => `<option value="${t}">${t}</option>`).join('')}
          </select>
          <input type="text" class="contact-val" value="${value}" placeholder="Введите значение">
          <button type="button" class="remove-contact">&times;</button>
        `;
        row.querySelector('.contact-type').value = type;
        row.querySelector('.remove-contact').onclick = () => {
            if (contactContainer.children.length > 1) row.remove();
        };
        contactContainer.appendChild(row);
    }

    dto.contactData.forEach(c => addContactRow(c.type, c.value));
    win.querySelector('#addContactBtn').onclick = () => addContactRow();

    // === отправка формы ===
    win.querySelector('#editForm').onsubmit = async function (e) {
        e.preventDefault();
        const formData = new FormData(this);
        const contactData = Array.from(contactContainer.children).map(row => ({
            type: row.querySelector('.contact-type').value, value: row.querySelector('.contact-val').value.trim()
        }));

        const updateDto = {
            internshipId: id,
            title: formData.get('title'),
            description: formData.get('description'),
            paymentType: formData.get('paymentType'),
            address: formData.get('address'),
            categoriesIds: editSelected.map(c => c.id),
            contactData,
            logoUrl: formData.get('logoUrl')
        };

        const fd = new FormData();
        fd.append('dto', new Blob([JSON.stringify(updateDto)], {type: 'application/json'}));
        const file = formData.get('logoImg');
        if (file && file.size) fd.append('logoImg', file);

        try {
            const res = await fetchWithAuth('/ims/internships', {
                method: 'PUT', body: fd
            });
            if (!res.ok) throw new Error('Ошибка при сохранении');
            await showAlert('Стажировка обновлена', 'Успех');
            document.body.removeChild(backdrop);
            document.body.style.overflow = '';
            loadInternships();
        } catch (err) {
            showAlert(err.message, 'Ошибка');
        }
    };
}

/**
 * Удаляет стажировку по ID после подтверждения.
 * При успехе обновляет список стажировок.
 *
 * @param {number} id - ID стажировки для удаления.
 */
async function deleteInternship(id) {
    const token = localStorage.getItem('token');
    if (!token) {
        showAlert('Пожалуйста, войдите в систему.');
        return;
    }

    const ok = await showConfirm('Вы уверены, что хотите удалить стажировку?', 'Подтверждение');
    if (!ok) return;

    try {
        const res = await fetch(`/ims/internships/${id}`, {
            method: 'DELETE', headers: {
                'Authorization': `Bearer ${token}`,
            }
        });

        if (!res.ok) {
            throw new Error('Не удалось удалить стажировку');
        }

        loadInternships();
    } catch (error) {
        console.error('Ошибка удаления стажировки:', error);
        showAlert('Ошибка при удалении стажировки');
    }
}


/**
 * Рисует элементы пагинации на основе общего количества страниц.
 *
 * @param {number} totalPages - Общее число страниц.
 */
function renderPagination(totalPages) {
    const pag = document.getElementById('pagination');
    const delta = 2;
    const range = [];
    const start = Math.max(2, currentPage - delta);
    const end = Math.min(totalPages - 1, currentPage + delta);

    range.push(1);
    if (start > 2) range.push('...');
    for (let i = start; i <= end; i++) range.push(i);
    if (end < totalPages - 1) range.push('...');
    if (totalPages > 1) range.push(totalPages);

    pag.innerHTML = range.map(p => p === '...' ? `<span class="dots">...</span>` : `<button class="${p === currentPage ? 'active' : ''}" onclick="goToPage(${p})">${p}</button>`).join('');
}

/**
 * Переходит на указанную страницу и загружает соответствующие стажировки.
 *
 * @param {number} page - Номер страницы.
 */
function goToPage(page) {
    currentPage = page;
    const form = document.getElementById('filterForm');
    if (form.dataset.mode === 'search') {
        loadInternships(collectFiltersFromForm(selectedCategories));
    } else {
        loadInternships();
    }
}

/**
 * Применяет текущие фильтры из формы и запускает поиск стажировок.
 * Сбрасывает текущую страницу на первую.
 */
function applyCurrentFilters() {
    currentPage = 1;
    document.getElementById('filterForm').dataset.mode = 'search';
    loadInternships(collectFiltersFromForm(selectedCategories));
}

/**
 * Сбрасывает фильтры формы, выбранные категории и ошибки.
 * Загружает стажировки с дефолтными параметрами.
 */
function resetFilters() {
    const form = document.getElementById('filterForm');
    form.reset();
    form.dataset.mode = '';
    selectedCategories = [];
    updateSelectedCategories(selectedCategories, handleRemoveCategory);
    currentPage = 1;

    document.getElementById('searchError').textContent = '';
    document.getElementById('dateError').textContent = '';

    loadInternships();
}

function handleRemoveCategory(categoryId) {
    selectedCategories = selectedCategories.filter(c => c.id !== categoryId);
    updateSelectedCategories(selectedCategories, handleRemoveCategory);
}

function handleCategorySelect(newCategories) {
    newCategories.forEach(newCat => {
        if (!selectedCategories.some(c => c.id === newCat.id)) {
            selectedCategories.push(newCat);
        }
    });
    updateSelectedCategories(selectedCategories, handleRemoveCategory);
}

document.addEventListener('DOMContentLoaded', async () => {
    if (!localStorage.getItem('token')) {
        window.location.href = '/auth/index.html';
        return;
    }

    // обновляем токены и сохраняем флаг роли
    await tryRefreshTokens();
    startTokenAutoRefresh();
    const isAdmin = localStorage.getItem('admin') === 'true';

    // настраиваем шапку в зависимости от роли
    const nav = document.querySelector('.site-nav');
    const profileLink = nav.querySelector('a[href="/favorite/index.html"]');
    const categoriesLink = nav.querySelector('a[href="/category/index.html"]');

    profileLink.textContent = 'Избранное';
    profileLink.href = '/favorite/index.html';

    if (!isAdmin) {
        // только «Избранное», прижатое вправо
        nav.innerHTML = '';
        nav.appendChild(profileLink);
        nav.style.justifyContent = 'flex-end';
    } else {
        // админ видит оба пункта в обычном порядке
        nav.innerHTML = '';
        nav.appendChild(profileLink);
        nav.appendChild(categoriesLink);
        nav.style.justifyContent = '';
    }

    // кнопка «Добавить» и контейнер поиска
    const addBtn = document.getElementById('addInternBtn');
    const searchContainer = document.querySelector('.search-container');
    if (!isAdmin) {
        addBtn.style.display = 'none';
        searchContainer.classList.add('user-only');
    }

    // валидация дат
    const dateErrorEl = document.getElementById('dateError');
    const startInput = document.getElementById('startDate');
    const endInput = document.getElementById('endDate');

    function validateDates() {
        dateErrorEl.textContent = '';
        const s = startInput.value, e = endInput.value;
        if (s && e && new Date(s) > new Date(e)) {
            dateErrorEl.textContent = 'Дата начала не может быть позже даты окончания';
        }
    }

    startInput.addEventListener('change', validateDates);
    endInput.addEventListener('change', validateDates);

    // загрузка данных и автокомплит категорий
    loadInternships();
    initCategorySearch(handleCategorySelect, fetchWithAuth);

    // валидация дат
    startInput.addEventListener('change', validateDates);
    endInput.addEventListener('change', validateDates);

    // обработчик формы поиска
    const searchErrorEl = document.getElementById('searchError');
    document.getElementById('filterForm').addEventListener('submit', function (e) {
        e.preventDefault();
        searchErrorEl.textContent = '';
        validateDates();
        if (dateErrorEl.textContent) {
            (startInput.value && endInput.value) ? endInput.focus() : startInput.focus();
            return;
        }
        const query = document.getElementById('searchInput').value.trim();
        if (!query) {
            searchErrorEl.textContent = 'Поле поиска не может быть пустым';
            document.getElementById('searchInput').focus();
            return;
        }
        if (query.length < 2) {
            searchErrorEl.textContent = 'Введите минимум 2 символа для поиска';
            document.getElementById('searchInput').focus();
            return;
        }
        applyCurrentFilters();
    });

    // сброс фильтров
    document.getElementById('resetFilters')
        .addEventListener('click', resetFilters);

    // показ/скрытие дропдауна фильтров
    document.querySelector('.dropdown-btn')
        .addEventListener('click', () => {
            document.querySelector('.dropdown-content').classList.toggle('show');
        });

    // кнопка логаута
    document.querySelector('.logout-btn')
        .addEventListener('click', handleLogout);
});

/**
 * Добавляет стажировку в избранное для текущего пользователя.
 * Отправляет POST запрос на бекенд и показывает уведомление.
 * @param {number} internshipId - ID стажировки для добавления в избранное.
 */
async function addToFavorites(internshipId) {
    const token = localStorage.getItem('token');
    if (!token) {
        return showAlert('Пожалуйста, войдите в систему, чтобы добавлять в избранное.', 'Ошибка');
    }

    const userId = await getCurrentUserId();
    if (!userId) {
        return showAlert('Не удалось определить пользователя. Пожалуйста, войдите заново.', 'Ошибка');
    }

    try {
        const res = await fetch(`/ims/users/${userId}/favorites/${internshipId}`, {
            method: 'POST', headers: {
                'Authorization': `Bearer ${token}`,
            }
        });

        if (!res.ok) {
            throw new Error(`Стажировка уже была добавлена в избранное, либо возникла непредвиденная ошибка: HTTP_STATUS_${res.status}`);
        }

        await showAlert('Стажировка добавлена в избранное', 'Успех');
    } catch (error) {
        showAlert(error.message, 'Ошибка');
    }
}

/**
 * Показывает модалку и заполняет её данными
 */
async function showDetails(id) {
    // 1 - открываем модалку
    const modal = document.getElementById('detailsModal');
    modal.classList.add('show');
    document.body.classList.add('modal-open');  // запрещаем скролл боди

    // 2 - загружаем DTO с бэкенда
    try {
        const token = localStorage.getItem('token');
        const res = await fetch(`/ims/internships/${id}`, {
            headers: {'Authorization': `Bearer ${token}`}
        });
        if (!res.ok) throw new Error('Не удалось получить данные');
        const dto = await res.json();

        // 3 - рендерим красивый HTML
        document.getElementById('modalBody').innerHTML = formatDto(dto);
    } catch (e) {
        document.getElementById('modalBody').innerHTML = `<p style="color:red">Ошибка: ${e.message}</p>`;
    }
}

function hideDetails() {
    const modal = document.getElementById('detailsModal');
    modal.classList.remove('show');
    document.body.classList.remove('modal-open');  // возвращаем скролл
}

function formatDto(d) {
    const paidText = d.paymentType === 'PAID' ? 'Оплачиваемая' : 'Неоплачиваемая';
    return `
    <img src="${d.logoUrl}" alt="${d.title}" />
    <h2>${d.title}</h2>
    <p><strong>Описание:</strong><br>${d.description.replace(/\n/g, '<br>')}</p>
    <p><strong>Тип оплаты:</strong> ${paidText}</p>
    <p><strong>Контакты:</strong><br>
      ${d.contactData.map(c => `${c.type}: ${c.value}`).join('<br>')}
    </p>
    <p><strong>Город:</strong> ${d.address}</p>
    <p><strong>Категории:</strong> ${d.categoriesData.map(c => c.title).join(', ')}</p>
    <p><strong>Создано:</strong> ${new Date(d.createdAt).toLocaleString()}</p>
    ${d.updatedAt ? `<p><strong>Обновлено:</strong> ${new Date(d.updatedAt).toLocaleString()}</p>` : ''}
   `;
}

document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('detailsModal');
    const closeBtn = modal.querySelector('.close');

    // закрываем по клику на фоновую область .modal
    modal.addEventListener('click', e => {
        if (e.target === modal) {
            hideDetails();
        }
    });
});

/**
 * Функция создания стажировки.
 * Отображает модальное окно с формой для добавления новой стажировки,
 * управляет выбором категорий, контактными данными и отправкой формы.
 * Требует авторизации пользователя (наличие токена).
 */
async function addInternship() {
    const token = localStorage.getItem('token');
    if (!token) return showAlert('Пожалуйста, войдите в систему.');

    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop';
    const win = document.createElement('div');
    win.className = 'modal-window';
    win.innerHTML = `
      <button class="modal-close-btn">&times;</button>
      <h2>Добавить стажировку</h2>
      <div class="modal-body">
        <form id="createForm">
          <label>Заголовок</label>
          <input type="text" name="title" required minlength="5" maxlength="100"
                 placeholder="Введите название стажировки">

          <label>Описание</label>
          <textarea name="description" rows="4" required minlength="250" maxlength="2500"
                    placeholder="Опишите стажировку (минимум 250 символов)"></textarea>

          <label>Тип оплаты</label>
          <select name="paymentType" required>
            <option value="" disabled selected>Выберите тип оплаты</option>
            <option value="PAID">Оплачиваемая</option>
            <option value="UNPAID">Неоплачиваемая</option>
          </select>

          <label>Город</label>
          <input type="text" name="address" required minlength="2" maxlength="100"
                 placeholder="Введите город">

          <label>Категории</label>
          <div id="createCategorySearch" class="category-search">
            <input type="text" id="createCategoryInput" placeholder="Поиск категории…">
            <select id="createCategoryList" multiple size="5"></select>
            <div id="createSelectedCategories" class="selected-categories"></div>
          </div>

          <label>Контактные данные <button type="button" id="addContactBtnCreate">＋</button></label>
          <div id="createContactContainer" style="margin-bottom:20px;"></div>

          <label>Логотип (необязательно)</label>
          <input type="file" name="logoImg" accept="image/*">
        </form>
      </div>
      <div class="modal-actions">
        <button type="button" class="cancel">Отмена</button>
        <button type="submit" form="createForm" class="ok-btn">Создать</button>
      </div>
    `;
    backdrop.appendChild(win);
    document.body.appendChild(backdrop);
    document.body.style.overflow = 'hidden';

    // закрытие модалки
    win.querySelector('.modal-close-btn').onclick = win.querySelector('.cancel').onclick = () => {
        document.body.removeChild(backdrop);
        document.body.style.overflow = '';
    };

    // === категории ===
    let selectedForCreate = [];
    const inp = win.querySelector('#createCategoryInput');
    const listBox = win.querySelector('#createCategoryList');
    const tags = win.querySelector('#createSelectedCategories');

    function renderCategoryTags() {
        tags.innerHTML = selectedForCreate.map(c => `<div class="category-tag">
               ${c.title}
               <span data-id="${c.id}" class="remove-category">&times;</span>
             </div>`).join('');
        tags.querySelectorAll('.remove-category').forEach(btn => {
            btn.onclick = () => {
                selectedForCreate = selectedForCreate.filter(c => c.id !== btn.dataset.id);
                renderCategoryTags();
            };
        });
    }

    inp.addEventListener('input', debounce(async () => {
        const q = inp.value.trim();
        if (q.length < 2) {
            listBox.innerHTML = '';
            return;
        }
        const res = await fetchWithAuth(`/ims/categories/autocomplete?searchStr=${encodeURIComponent(q)}`, {method: 'GET'});
        if (!res.ok) return;
        const cats = await res.json();
        listBox.innerHTML = cats.map(cat => `<option value="${cat.categoryId}" data-title="${cat.title}">${cat.title}</option>`).join('');
    }), 300);

    listBox.addEventListener('dblclick', () => {
        Array.from(listBox.selectedOptions).forEach(opt => {
            const id = opt.value, title = opt.dataset.title;
            if (!selectedForCreate.some(c => c.id === id)) selectedForCreate.push({id, title});
        });
        inp.value = '';
        listBox.innerHTML = '';
        renderCategoryTags();
    });

    // === контактные данные ===
    const createContactContainer = win.querySelector('#createContactContainer');
    const CONTACT_TYPES = ['TELEGRAM', 'EMAIL', 'WEBSITE'];

    function addContactRow(container) {
        const row = document.createElement('div');
        row.className = 'contact-row';
        row.innerHTML = `
          <select class="contact-type">
            ${CONTACT_TYPES.map(t => `<option value="${t}">${t}</option>`).join('')}
          </select>
          <input type="text" class="contact-val" placeholder="Введите значение">
          <button type="button" class="remove-contact">&times;</button>
        `;
        row.querySelector('.remove-contact').onclick = () => {
            if (container.children.length > 1) row.remove();
        };
        container.appendChild(row);
    }

    win.querySelector('#addContactBtnCreate').onclick = () => addContactRow(createContactContainer);

    // === отправка формы ===
    win.querySelector('#createForm').onsubmit = async e => {
        e.preventDefault();
        const form = e.target;
        const fd = new FormData();

        const dto = {
            title: form.title.value.trim(),
            description: form.description.value.trim(),
            paymentType: form.paymentType.value,
            address: form.address.value.trim(),
            categoriesIds: selectedForCreate.map(c => c.id),
            contactData: Array.from(createContactContainer.children).map(row => ({
                type: row.querySelector('.contact-type').value, value: row.querySelector('.contact-val').value.trim()
            }))
        };

        fd.append('dto', new Blob([JSON.stringify(dto)], {type: 'application/json'}));
        const file = form.logoImg.files[0];
        if (file) fd.append('logoImg', file);

        try {
            const res = await fetchWithAuth('/ims/internships/create', {
                method: 'POST', body: fd
            });
            if (!res.ok) throw new Error('Ошибка при создании');
            await showAlert('Стажировка создана', 'Успех');
            document.body.removeChild(backdrop);
            document.body.style.overflow = '';
            loadInternships();
        } catch (err) {
            showAlert(err.message, 'Ошибка');
        }
    };
}

document.getElementById('addInternBtn')
    .addEventListener('click', addInternship);

document.getElementById('homeLink').addEventListener('click', function () {
    window.location.href = 'http://localhost:8080/main/index.html';
});

window.showDetails = showDetails;
window.addToFavorites = addToFavorites;
window.editInternship = editInternship;
window.deleteInternship = deleteInternship;
window.goToPage = goToPage;
window.hideDetails = hideDetails;