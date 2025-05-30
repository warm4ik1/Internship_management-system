/**
 * Выполняет выход пользователя из системы:
 * - отправляет запрос на logout с refreshToken;
 * - очищает localStorage;
 * - перенаправляет пользователя на страницу авторизации.
 */
export async function handleLogout() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
        await fetch('/auth/logout', {
            method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({refreshToken})
        });
    }
    localStorage.clear();
    window.location.href = '/auth/index.html';
}

/**
 * Пытается обновить токены доступа с помощью refreshToken.
 * Если токен обновлён успешно, сохраняет новые значения в localStorage.
 * При неудаче вызывает handleLogout().
 *
 * @returns {Promise<string|null>} Новый access token или null при неудаче.
 */
export async function tryRefreshTokens() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) return null;

    try {
        const res = await fetch('/auth/refresh', {
            method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({refreshToken})
        });
        if (!res.ok) throw new Error('Ошибка обновления токена');

        const {token, refreshToken: newRefresh} = await res.json();
        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', newRefresh);

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            localStorage.setItem('admin', payload.admin === true);
        } catch {
            // Игнорируем
        }

        return token;
    } catch (error) {
        console.error('Refresh token failed:', error);
        handleLogout();
        return null;
    }
}

/**
 * Универсальный fetch с поддержкой рефреша:
 * 1) Выполняем запрос с текущим токеном
 * 2) Если ответ 401/403 — пробуем обновить токен
 * 3) Если рефреш прошёл — повторяем запрос
 * 4) Если не удалось — логаутим
 */
export async function fetchWithAuth(url, options = {}) {
    const doFetch = (token) => {
        const headers = {
            ...(options.headers || {}), 'Authorization': `Bearer ${token}`
        };
        return fetch(url, {...options, headers});
    };

    let token = localStorage.getItem('token');
    let res = await doFetch(token);

    if (res.status === 401 || res.status === 403) {
        const newToken = await tryRefreshTokens();
        if (newToken) {
            res = await doFetch(newToken);
        } else {
            return null;
        }
    }

    return res;
}

export function startTokenAutoRefresh() {
    setInterval(() => {
        const token = localStorage.getItem('token');
        if (!token) return;
        try {
            const {exp} = JSON.parse(atob(token.split('.')[1]));
            const msLeft = exp * 1000 - Date.now();
            if (msLeft > 0 && msLeft < 10000) tryRefreshTokens();
        } catch {
        }
    }, 30000);
}

/**
 * Загружает данные текущего пользователя по токену.
 * @returns {Promise<number|null>} - ID пользователя или null.
 */
export async function getCurrentUserId() {
    const token = localStorage.getItem('token');
    if (!token) return null;

    try {
        const res = await fetch('/ims/users/me', {
            headers: {'Authorization': `Bearer ${token}`},
        });
        if (!res.ok) throw new Error('Не удалось получить данные пользователя');
        const {id, userId} = await res.json();
        return id ?? userId ?? null;
    } catch (e) {
        console.error(e);
        return null;
    }
}


