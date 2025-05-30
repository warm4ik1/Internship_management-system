/**
 * Рисует элементы пагинации на основе общего количества страниц.
 *
 * @param {number} totalPages - Общее число страниц.
 * @param {number} currentPage - Текущая страница.
 * @param {function(number): void} onPageClick - Колбэк при клике на номер.
 */
export function renderPagination(totalPages, currentPage, onPageClick) {
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

    pag.innerHTML = range
        .map(p => p === '...' ? `<span class="dots">...</span>` : `<button class="${p === currentPage ? 'active' : ''}">${p}</button>`)
        .join('');

    // Повесим обработчики после вставки
    Array.from(pag.querySelectorAll('button')).forEach(btn => {
        const page = Number(btn.textContent);
        btn.addEventListener('click', () => onPageClick(page));
    });
}

/**
 * Удобный обёрточный колбэк для renderPagination
 */
export function goToPage(page, loadInternships, selectedCategories, mode) {
    // mode может быть 'search' или undefined
    if (mode === 'search') {
        loadInternships({mode: 'search', categories: selectedCategories, page});
    } else {
        loadInternships({page});
    }
}
