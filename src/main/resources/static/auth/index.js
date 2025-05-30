async function fetchAndStoreUserId() {
    const token = localStorage.getItem('token');
    const res = await fetch('/ims/users/me', {
        headers: {'Authorization': `Bearer ${token}`}
    });
    if (!res.ok) {
        console.error('Не удалось получить профиль:', res.status);
        throw new Error('Ошибка получения профиля');
    }
    const {id} = await res.json(); // UserResponseDto
    localStorage.setItem('userId', id); // сохраняем userId
}

document.querySelector('.signup form').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const username = form.txt.value.trim();
    const email = form.email.value.trim();
    const password = form.pswd.value;

    document.getElementById('usernameError').textContent = '';
    document.getElementById('emailError').textContent = '';
    document.getElementById('passwordError').textContent = '';

    let valid = true;
    if (username.length < 8 || username.length > 50) {
        document.getElementById('usernameError').textContent = 'Имя должно быть от 8 до 50 символов';
        valid = false;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) || email.length > 254) {
        document.getElementById('emailError').textContent = 'Некорректный email';
        valid = false;
    }
    if (password.length < 8 || password.length > 50) {
        document.getElementById('passwordError').textContent = 'Пароль должен быть от 8 до 50 символов';
        valid = false;
    }
    if (!valid) return;

    try {
        const regRes = await fetch('/auth/registration', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({username, email, password})
        });

        const regData = await (regRes.headers.get('Content-Type')?.includes('application/json') ? regRes.json() : regRes.text());

        if (!regRes.ok) {
            const msg = typeof regData === 'string' ? regData : regData.message;
            throw new Error(msg || 'Ошибка регистрации');
        }

        const loginRes = await fetch('/auth/sign-in', {
            method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({email, password})
        });
        const loginData = await loginRes.json();

        if (!loginRes.ok) {
            throw new Error(loginData.message || 'Ошибка авто‑логина');
        }

        localStorage.setItem('token', loginData.token);
        localStorage.setItem('refreshToken', loginData.refreshToken);

        try {
            const payload = JSON.parse(atob(loginData.token.split('.')[1]));
            localStorage.setItem('isAdmin', payload.admin === true);
        } catch {
            localStorage.setItem('isAdmin', false);
        }

        try {
            await fetchAndStoreUserId();
        } catch (e) {
            alert('Не удалось получить данные профиля. Попробуйте войти снова.');
            return;
        }

        window.location.href = '/main/index.html';

    } catch (err) {
        alert(err.message);
    }
});


document.getElementById('loginForm').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const email = form.email.value.trim();
    const password = form.pswd.value;

    document.getElementById('loginEmailError').textContent = '';
    document.getElementById('loginPasswordError').textContent = '';

    let valid = true;
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        document.getElementById('loginEmailError').textContent = 'Некорректный email';
        valid = false;
    }
    if (password.length < 8 || password.length > 50) {
        document.getElementById('loginPasswordError').textContent = 'Пароль от 8 до 50 символов';
        valid = false;
    }
    if (!valid) return;

    try {
        const response = await fetch('/auth/sign-in', {
            method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({email, password})
        });
        const data = await response.json();

        if (!response.ok) throw new Error(data.message || 'Ошибка входа');

        localStorage.setItem('token', data.token);
        localStorage.setItem('refreshToken', data.refreshToken);

        try {
            const payload = JSON.parse(atob(data.token.split('.')[1]));
            localStorage.setItem('admin', payload.admin === true);
        } catch (e) {
            console.error('Не удалось распарсить admin из токена', e);
            localStorage.setItem('admin', false);
        }
        // получаем и сохраняем userId
        try {
            await fetchAndStoreUserId();
        } catch (e) {
            alert('Не удалось получить данные профиля. Попробуйте войти снова.');
            return;
        }

        window.location.href = '/main/index.html';
    } catch (error) {
        alert(error.message);
    }
});