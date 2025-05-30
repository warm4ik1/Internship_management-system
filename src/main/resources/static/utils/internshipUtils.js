/**
 * Показывает модалку и заполняет её данными
 */
export async function showDetails(id) {
    console.log('showDetails called with id:', id);
    // 1 - открываем модалку
    const modal = document.getElementById('detailsModal');
    if (!modal) {
        console.error('Modal element not found!');
        return;
    }
    modal.classList.add('visible');
    console.log('Modal classes after show:', modal.classList);
    document.body.classList.add('modal-open'); // запрещаем скролл боди
    document.documentElement.classList.add('modal-open');

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

export function formatDto(d) {
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

export function hideDetails() {
    const modal = document.getElementById('detailsModal');
    modal.classList.remove('visible');
    document.body.classList.remove('modal-open');
    document.documentElement.classList.remove('modal-open');
}

window.hideDetails = hideDetails;
