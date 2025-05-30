/**
 * Создаёт и отображает модальное окно с заданным заголовком, содержимым и кнопками.
 *
 * @param {Object} options - Параметры модального окна.
 * @param {string} options.title - Заголовок окна.
 * @param {string} options.bodyHtml - HTML-содержимое тела модального окна.
 * @param {Array<Object>} options.buttons - Массив кнопок с действиями.
 * @param {string} options.buttons[].text - Текст кнопки.
 * @param {string} options.buttons[].role - Роль кнопки ('yes', 'no', 'ok') для стилизации.
 * @param {Function} options.buttons[].onClick - Коллбэк, вызываемый при нажатии на кнопку.
 */
export function createModal({title, bodyHtml, buttons}) {
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop';

    const win = document.createElement('div');
    win.className = 'modal-window';
    win.innerHTML = `
        <button class="modal-close-btn">&times;</button>
        <h2>${title}</h2>
        <div class="modal-body">${bodyHtml}</div>
        <div class="modal-actions"></div>
    `;

    const actions = win.querySelector('.modal-actions');
    buttons.forEach(btn => {
        const el = document.createElement('button');
        el.textContent = btn.text;
        if (btn.role === 'yes') el.classList.add('yes-btn');
        if (btn.role === 'no') el.classList.add('no-btn');
        if (btn.role === 'ok') el.classList.add('ok-btn');
        el.onclick = () => {
            btn.onClick();
            document.body.removeChild(backdrop);
        };
        actions.appendChild(el);
    });

    win.querySelector('.modal-close-btn').onclick = () => {
        document.body.removeChild(backdrop);
    };

    backdrop.appendChild(win);
    document.body.appendChild(backdrop);
}

/**
 * Показывает модальное окно с вопросом и кнопками "Да" и "Нет".
 *
 * @param {string} message - Текст вопроса.
 * @param {string} [title='Подтвердите'] - Заголовок окна (по умолчанию "Подтвердите").
 * @returns {Promise<boolean>} Промис, который возвращает true при выборе "Да" и false при выборе "Нет".
 */
export function showConfirm(message, title = 'Подтверждение') {
    return new Promise(resolve => {
        createModal({
            title, bodyHtml: `<p>${message}</p>`, buttons: [{
                text: 'Да', role: 'yes', onClick: () => resolve(true)
            }, {
                text: 'Нет', role: 'no', onClick: () => resolve(false)
            }]
        });
    });
}

/**
 * Показывает модальное окно с сообщением и одной кнопкой "OK".
 *
 * @param {string} message - Текст сообщения.
 * @param {string} [title='Внимание'] - Заголовок окна (по умолчанию "Внимание").
 * @returns {Promise<void>} Промис, который разрешается при нажатии "OK".
 */
export function showAlert(message, title = 'Внимание') {
    return new Promise(resolve => {
        createModal({
            title, bodyHtml: `<p>${message}</p>`, buttons: [{
                text: 'OK', role: 'ok', onClick: resolve
            }]
        });
    });
}
