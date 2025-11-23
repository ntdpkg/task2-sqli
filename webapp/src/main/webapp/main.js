document.addEventListener('DOMContentLoaded', () => {
    const token = document.cookie.split('; ').find(row => row.startsWith('auth_token='))?.split('=')[1];

    if (token && (window.location.pathname.endsWith('/login') || window.location.pathname.endsWith('/register'))) {
        console.log("Token found, redirecting to tasks...");
        window.location.href = '/tasks';
        return;
    }

    if (window.location.pathname.endsWith('/tasks') && !token) {
        console.log("No token, redirecting to login...");
        window.location.href = '/login';
        return;
    }

    const registerForm = document.getElementById('registerForm');
    if (registerForm) registerForm.addEventListener('submit', handleRegister);

    const loginForm = document.getElementById('loginForm');
    if (loginForm) loginForm.addEventListener('submit', handleLogin);
});

function set_cookie(name, value) {
  document.cookie = name +'='+ value +'; Path=/;';
}
function delete_cookie(name) {
  document.cookie = name +'=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}

function logout(event) {
    if (event) event.preventDefault();
    delete_cookie('auth_token');
    window.location.href = '/login'; 
}

async function handleRegister(event) {
    event.preventDefault();
    const username = document.getElementById('username').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const messageDiv = document.getElementById('message');

    try {
        const response = await fetch('/register', {  
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ username, email, password })
        });

        const resultText = await response.text();
        messageDiv.style.color = response.ok ? 'green' : 'red';
        messageDiv.textContent = resultText;

        if (response.ok) {
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
        }
    } catch (error) {
        messageDiv.style.color = 'red';
        messageDiv.textContent = 'Network error';
        console.error(error);
    }
}

async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const messageDiv = document.getElementById('message');

    try {
        const response = await fetch('/login', {  
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ username, password })
        });

        if (!response.ok) throw new Error('Login failed');

        const result = await response.json();
        if (result.token) {
            set_cookie('auth_token', result.token);
            window.location.href = '/tasks'; 
        } else {
            // messageDiv.textContent = result.message || 'Invalid credentials';
            messageDiv.textContent = 'Invalid credentials';
        }
    } catch (error) {
        messageDiv.textContent = 'Login failed. Check console.';
        console.error("Login error:", error);
    }
}

async function handleImportTasks(event) {
    event.preventDefault();
    const fileInput = document.querySelector('input[name="xmlfile"]');
    const messageDiv = document.getElementById('importMessage');
    
    if (fileInput.files.length === 0) {
        messageDiv.style.color = 'red';
        messageDiv.textContent = 'Please select a file.';
        return;
    }
    const formData = new FormData();
    formData.append('xmlfile', fileInput.files[0]);
    
    try {
        const response = await fetch('/importTasks', {
            method: 'POST',
            body: formData
        });
        const resultText = await response.text();
        alert(resultText);
        // reload
        window.location.reload();
    } catch (error) {
        messageDiv.style.color = 'red';
        messageDiv.textContent = 'Import failed. Check console.';
        console.error("Import error:", error);
    }
}