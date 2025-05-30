import {
    handleLogout, tryRefreshTokens, fetchWithAuth, startTokenAutoRefresh
} from '../utils/authUtils.js';
import {
    showConfirm, createModal
} from '../utils/modalUtils.js';
import {
    showDetails, hideDetails, formatDto
} from '../utils/internshipUtils.js';

/**
 * Загружает избранные стажировки пользователя и отображает их.
 * @param {number} page - номер страницы (по умолчанию 0)
 */
async function loadFavorites(page = 0) {
    const userId = localStorage.getItem('userId');
    const res = await fetchWithAuth(`/ims/users/${userId}/favorites?pageNumber=${page}`);
    if (!res || !res.ok) {
        const errText = res ? await res.text() : 'no response';
        console.error('Ошибка загрузки избранных:', res?.status, errText);
        return;
    }
    const data = await res.json();
    console.log("Полученные стажировки:", data.content);
    renderFavorites(data.content);
    renderPagination(data.totalPages, page);
}

/**
 * Отображает список стажировок в интерфейсе.
 * @param {Array} internships - массив стажировок
 */
function renderFavorites(internships) {
    const container = document.getElementById('favoritesGrid');

    if (internships.length === 0) {
        container.innerHTML = `
            <div class="no-favorites-message">
                Похоже, Вы ещё не добавляли стажировки в избранное.<br>
                Найдите то, что Вам по душе, и возвращайтесь обратно!
            </div>`;
        return;
    }

    container.innerHTML = internships.map(i => {
        const createdStr = new Date(i.createdAt).toLocaleDateString('ru-RU');
        const updatedStr = i.updatedAt ? new Date(i.updatedAt).toLocaleDateString('ru-RU') : '';
        const categoriesHtml = i.categoriesData?.map(c => `<span class="category-badge">${c.title}</span>`).join(' ') || '';

        return `
        <div class="internship-card">
            <img src="${i.logoUrl}" alt="${i.title}" class="internship-logo">
            <h3>${i.title}</h3>
            <div class="categories-container">${categoriesHtml}</div>
            <p class="description">📌${i.description?.substring(0, 250)}…</p>
            <p class="payment-type">
                ${i.paymentType === 'PAID' ? '💲Оплачиваемая стажировка' : '❎Неоплачиваемая стажировка'}
            </p>
            <p class="location">🌍 ${i.address}</p>
            <p class="created">🗓️Дата создания: ${createdStr}</p>
            ${updatedStr ? `<p class="updated">📅Дата обновления: ${updatedStr}</p>` : ''}
            <div class="card-actions">
                <button class="details-btn" onclick="showDetails(${i.internshipId})">Подробнее</button>
                <button class="remove-favorite" data-id="${i.internshipId}">Удалить</button>
            </div>
        </div>`;
    }).join('');

    document.querySelectorAll('.remove-favorite')
        .forEach(btn => btn.addEventListener('click', handleRemoveFavorite));
}

/**
 * Обрабатывает удаление стажировки из избранного.
 * @param {Event} e - событие клика по кнопке удаления
 */
async function handleRemoveFavorite(e) {
    const internshipId = e.target.dataset.id;
    const confirmed = await showConfirm('Удалить стажировку из избранного?', 'Подтверждение');
    if (!confirmed) return;
    const userId = localStorage.getItem('userId');
    const res = await fetchWithAuth(`/ims/users/${userId}/favorites/${internshipId}`, {method: 'DELETE'});
    if (res && res.ok) {
        const curr = document.querySelector('.pagination button.active')?.textContent - 1 || 0;
        loadFavorites(curr);
    } else {
        alert('Не удалось удалить стажировку');
    }
}

/**
 * Отрисовывает кнопки пагинации.
 * @param {number} totalPages - общее количество страниц
 * @param {number} currentPage - текущая активная страница
 */
function renderPagination(totalPages, currentPage) {
    const pag = document.getElementById('pagination');
    pag.innerHTML = '';
    const delta = 2, pages = [];
    for (let i = 0; i < totalPages; i++) {
        if (i === 0 || i === totalPages - 1 || (i >= currentPage - delta && i <= currentPage + delta)) {
            pages.push(i);
        }
    }
    pages.forEach(i => {
        const btn = document.createElement('button');
        btn.textContent = i + 1;
        btn.className = (i === currentPage ? 'active' : '');
        btn.addEventListener('click', () => loadFavorites(i));
        pag.appendChild(btn);
    });
}

document.addEventListener('DOMContentLoaded', async () => {

    if (!localStorage.getItem('token')) {
        window.location.href = '/auth/index.html';
        return;
    }
    await tryRefreshTokens();
    startTokenAutoRefresh();

    const isAdmin = localStorage.getItem('admin') === 'true';
    const categoriesLink = document.querySelector('.categories-link');
    const siteNav = document.querySelector('.site-nav');

    if (isAdmin) {
        categoriesLink.style.display = 'block';
    } else {
        siteNav.classList.add('single-link');
    }

    if (!localStorage.getItem('userId')) {
        const profileRes = await fetchWithAuth('/ims/users/me');
        if (profileRes && profileRes.ok) {
            const {id} = await profileRes.json();
            localStorage.setItem('userId', id);
        } else {
            alert('Не удалось получить профиль пользователя');
            return;
        }
    }

    document.querySelector('.logout-btn').addEventListener('click', handleLogout);
    loadFavorites(0);

    document.getElementById('homeLink').addEventListener('click', () => {
        window.location.href = '/main/index.html';
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('detailsModal');
    const closeBtn = modal.querySelector('.close');

    modal.addEventListener('click', e => {
        if (e.target === modal) {
            hideDetails();
        }
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('detailsModal');

    modal.querySelector('.close').addEventListener('click', hideDetails);

    modal.addEventListener('click', e => {
        if (e.target === modal) {
            hideDetails();
        }
    });
});

window.showDetails = showDetails;

