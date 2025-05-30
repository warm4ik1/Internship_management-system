/**
 * Собирает фильтры из формы
 * @param {Array} selectedCategories - Выбранные категории
 * @returns {Object} Объект с параметрами фильтрации
 */
export function collectFiltersFromForm(selectedCategories) {
    const fd = new FormData(document.getElementById('filterForm'));
    const startVal = fd.get('start');
    const endVal = fd.get('end');

    return {
        searchStr: fd.get('searchStr')?.trim() || null,
        mode: fd.get('mode') || null,
        paymentType: fd.get('paymentType') || null,
        address: fd.get('address')?.trim() || null,
        start: startVal ? `${startVal} 00:00:00` : null,
        end: endVal ? `${endVal} 23:59:59` : null,
        categoryNames: selectedCategories.map(c => c.title)
    };
}

/**
 * Обновляет отображение выбранных категорий
 * @param {Array} selectedCategories - Выбранные категории
 * @param {Function} onRemove - Callback для удаления категории
 */
export function updateSelectedCategories(selectedCategories, onRemove) {
    const container = document.getElementById('selectedCategories');
    container.innerHTML = selectedCategories.map(c => `
    <div class="category-tag">
      ${c.title}
      <span class="remove-category" data-id="${c.id}">&times;</span>
    </div>
  `).join('');

    container.querySelectorAll('.remove-category').forEach(span => {
        span.onclick = () => onRemove(span.dataset.id);
    });
}

/**
 * Инициализирует поиск категорий
 * @param {Function} onCategorySelect - Callback при выборе категории
 */
export function initCategorySearch(onCategorySelect, fetchWithAuth) { // Добавлен второй параметр
    const inp = document.getElementById('categoryFilterInput');
    const listBox = document.getElementById('categoryListBox');

    inp.addEventListener('input', debounce(async () => {
        const q = inp.value.trim();
        if (q.length < 2) {
            listBox.innerHTML = '';
            return;
        }

        const res = await fetchWithAuth(`/ims/categories/autocomplete?searchStr=${encodeURIComponent(q)}`, {
            method: 'GET'
        });

        if (!res.ok) return;

        const cats = await res.json();
        listBox.innerHTML = cats.map(cat => `
      <option value="${cat.categoryId}" data-title="${cat.title}">${cat.title}</option>
    `).join('');
    }, 300));

    listBox.addEventListener('dblclick', () => {
        const newCategories = Array.from(listBox.selectedOptions).map(opt => ({
            id: opt.value, title: opt.dataset.title || opt.textContent
        }));

        onCategorySelect(newCategories);
        inp.value = '';
        listBox.innerHTML = '';
    });
}

/**
 * Валидирует даты фильтра
 * @returns {boolean} Валидны ли даты
 */
export function validateDates() {
    const dateErrorEl = document.getElementById('dateError');
    const startInput = document.getElementById('startDate');
    const endInput = document.getElementById('endDate');

    dateErrorEl.textContent = '';
    const s = startInput.value, e = endInput.value;

    if (s && e && new Date(s) > new Date(e)) {
        dateErrorEl.textContent = 'Дата начала не может быть позже даты окончания';
        return false;
    }
    return true;
}

/**
 * Утилита debounce
 * @param {Function} fn - Целевая функция
 * @param {number} delay - Задержка в мс
 */
export function debounce(fn, delay) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => fn(...args), delay);
    };
}