window.SurfView = window.SurfView || {};

SurfView.Auth = {
    mode: 'private',
    current: null,
    ready: false
};

// Because the backend requires the bearer token in the header, we cannot store
// the jwt in a httpOnly cookie, even though that's safer with regard to XSS.
/** Returns the bearer auth header for API calls. */
function authorizationHeader() {
    const token = window.localStorage.getItem('jwt');
    return token ? {'Authorization': `Bearer ${token}`} : {};
}

function normalizeRole(role) {
    return String(role || '').replace(/^ROLE_/, '').toUpperCase();
}

/** Returns the current user's normalized roles. */
function currentUserRoles() {
    const roles = SurfView.Auth.current?.roles;
    return Array.isArray(roles) ? roles.map(normalizeRole).filter(Boolean) : [];
}

/** Returns whether auth status has a current user. */
function isLoggedIn() {
    return Boolean(SurfView.Auth.current);
}

/** Returns the current user's email. */
function currentUserEmail() {
    return SurfView.Auth.current?.email || '';
}

/** Returns whether writes are allowed by auth status. */
function canWrite() {
    const mode = String(SurfView.Auth.mode || 'private').toLowerCase();
    if (mode === 'none') {
        return true;
    }
    if (mode === 'guest') {
        return false;
    }
    const roles = currentUserRoles();
    return roles.includes('ADMIN') || roles.includes('USER');
}

/** Returns whether private mode must show login. */
function privateLoginRequired() {
    return String(SurfView.Auth.mode || '').toLowerCase() === 'private' && !isLoggedIn();
}

/** Applies /auth/status data to frontend auth state. */
function setAuthState(status = {}) {
    const current = status.current || null;
    const roles = Array.isArray(current?.roles) ? current.roles.map(normalizeRole) : [];
    SurfView.Auth.mode = status.mode || SurfView.Auth.mode || 'private';
    SurfView.Auth.current = current ? {...current, roles} : null;
    SurfView.Auth.ready = true;
    updateLoginLink();
    if (typeof updateWriteControls === 'function') {
        updateWriteControls();
    }
    updatePrivateLoginScreen();
    window.dispatchEvent(new CustomEvent('surfview:auth-changed', {
        detail: {
            mode: SurfView.Auth.mode,
            current: SurfView.Auth.current,
            canWrite: canWrite()
        }
    }));
}

/** Loads /auth/status and updates auth state. */
async function loadAuthStatus(options = {}) {
    const clearStaleToken = options.clearStaleToken !== false;
    try {
        const response = await fetch(new URL('/auth/status', window.location.origin).toString(), {
            headers: {
                'Accept': 'application/json',
                ...authorizationHeader()
            }
        });

        if (response.status === 401) {
            window.localStorage.removeItem('jwt');
            setAuthState({mode: SurfView.Auth.mode, current: null});
            return SurfView.Auth;
        }

        if (!response.ok) {
            throw new Error(`Auth status failed with status ${response.status}`);
        }

        const status = await response.json();
        if (clearStaleToken && !status.current) {
            window.localStorage.removeItem('jwt');
        }
        setAuthState(status);
    } catch (error) {
        setAuthState({mode: SurfView.Auth.mode || 'private', current: null});
    }
    return SurfView.Auth;
}

/** Logs out locally and updates auth state. */
async function logout() {
    window.localStorage.removeItem('jwt');
    setAuthState({mode: SurfView.Auth.mode, current: null});
}

function updatePrivateLoginScreen() {
    const loginClose = document.getElementById('login-modal-close');
    loginClose?.classList.toggle('hidden', privateLoginRequired());
    document.body.classList.toggle('private-login-required', privateLoginRequired());

    if (privateLoginRequired()) {
        showLoginModal({required: true});
    }
}

function updateLoginLink() {
    const link = document.getElementById('login-link');
    const accountMenu = document.getElementById('account-menu');
    const statusLabel = document.getElementById('auth-status-label');
    const email = currentUserEmail();
    const mode = String(SurfView.Auth.mode || '').toLowerCase();
    const securityDisabled = mode === 'none';
    const hideAuthButtons = securityDisabled || mode === 'guest';

    if (statusLabel) {
        statusLabel.textContent = securityDisabled ? 'Security disabled' : '';
        statusLabel.classList.toggle('hidden', !securityDisabled);
    }

    if (link) {
        link.textContent = 'Log in';
        link.title = 'Log in';
        link.classList.toggle('hidden', hideAuthButtons || isLoggedIn());
    }

    if (accountMenu) {
        accountMenu.classList.toggle('hidden', hideAuthButtons || !isLoggedIn());
        accountMenu.title = email ? `Logged in as ${email}` : '';
        if (hideAuthButtons || !isLoggedIn()) {
            closeAccountMenu();
        }
    }
}

function closeAccountMenu() {
    document.getElementById('account-menu-items')?.classList.add('hidden');
    document.getElementById('account-menu-toggle')?.setAttribute('aria-expanded', 'false');
}

function toggleAccountMenu() {
    const items = document.getElementById('account-menu-items');
    const toggle = document.getElementById('account-menu-toggle');
    const opening = items?.classList.contains('hidden');
    items?.classList.toggle('hidden', !opening);
    toggle?.setAttribute('aria-expanded', String(opening));
}

/** Opens the login modal. */
function showLoginModal(options = {}) {
    const backdrop = document.getElementById('login-modal-backdrop');
    if (!backdrop) {
        return;
    }
    backdrop.dataset.requiredLogin = String(Boolean(options.required || privateLoginRequired()));

    openManagedModal(modalElements(backdrop), {
        initialFocus: () => document.getElementById('login-username')
    });
}

/** Closes the login modal when login is optional. */
function hideLoginModal() {
    if (privateLoginRequired()) {
        return;
    }
    const backdrop = document.getElementById('login-modal-backdrop');
    if (!backdrop) {
        return;
    }

    delete backdrop.dataset.requiredLogin;
    closeManagedModal(modalElements(backdrop));
    const form = document.getElementById('login-form');
    if (form) {
        form.reset();
    }
    const status = document.getElementById('login-result-message');
    if (status) {
        status.textContent = '';
    }
}

function isWriteRequest(method = 'GET') {
    return !['GET', 'HEAD', 'OPTIONS'].includes(String(method || 'GET').toUpperCase());
}

/** Calls an API endpoint with auth handling. */
async function callEndpoint(url, options = {}) {
    const resolvedUrl = url.startsWith('http://') || url.startsWith('https://')
        ? url
        : new URL(url, window.location.origin).toString();
    const response = await fetch(resolvedUrl, {
        ...options,
        headers: {
            ...options.headers,
            ...authorizationHeader()
        }
    });

    if (response.status === 401) {
        window.localStorage.removeItem('jwt');
        await loadAuthStatus();
        if (isWriteRequest(options.method) && options.promptLogin !== false) {
            showLoginModal();
        }
    } else if (response.status === 403) {
        await loadAuthStatus({clearStaleToken: false});
    }

    return response;
}

async function submitLoginForm(event) {
    event.preventDefault();
    const form = event.target;
    const status = document.getElementById('login-result-message');
    const submitButton = document.getElementById('login-submit');

    if (status) {
        status.textContent = '';
    }
    if (submitButton) {
        submitButton.disabled = true;
    }

    try {
        const response = await fetch(new URL('auth/login', window.location.origin).toString(), {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({email: form.username.value, password: form.password.value})
        });

        if (!response.ok) {
            throw new Error(response.status === 401 ? 'Invalid user/password' : `Login failed with status ${response.status}`);
        }

        const data = await response.json();
        if (!data.token) {
            throw new Error('Login failed: no token received');
        }

        window.localStorage.setItem('jwt', data.token);
        await loadAuthStatus();
        hideLoginModal();
        loadOrganizations().catch(() => {
            // Organization dropdowns fall back to the current entity organization id.
        });
        loadCurrentPageAndEntity();
    } catch (error) {
        window.localStorage.removeItem('jwt');
        await loadAuthStatus();
        if (status) {
            status.textContent = error.message || 'Login failed';
        }
    } finally {
        if (submitButton) {
            submitButton.disabled = false;
        }
    }
}

/** Registers login modal event handlers. */
function initLoginModal() {
    const loginLink = document.getElementById('login-link');
    const logoutLink = document.getElementById('logout-link');
    const loginForm = document.getElementById('login-form');
    const loginClose = document.getElementById('login-modal-close');
    const loginBackdrop = document.getElementById('login-modal-backdrop');
    const accountMenuToggle = document.getElementById('account-menu-toggle');

    if (loginLink) {
        loginLink.addEventListener('click', () => {
            if (!isLoggedIn()) {
                showLoginModal();
            }
        });
    }

    if (logoutLink) {
        logoutLink.addEventListener('click', () => {
            closeAccountMenu();
            if (window.confirm('Do you want to log out?')) {
                logout();
            }
        });
    }

    accountMenuToggle?.addEventListener('click', event => {
        event.stopPropagation();
        toggleAccountMenu();
    });
    document.addEventListener('click', event => {
        if (!event.target.closest('#account-menu')) closeAccountMenu();
    });
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape') closeAccountMenu();
    });

    if (loginForm) {
        loginForm.addEventListener('submit', submitLoginForm);
    }

    if (loginClose) {
        loginClose.addEventListener('click', hideLoginModal);
    }

    if (loginBackdrop) {
        let pointerDownOnBackdrop = false;
        loginBackdrop.addEventListener('pointerdown', event => {
            pointerDownOnBackdrop = event.target === loginBackdrop;
        });
        loginBackdrop.addEventListener('click', event => {
            if (pointerDownOnBackdrop && event.target === loginBackdrop && !privateLoginRequired()) {
                hideLoginModal();
            }
            pointerDownOnBackdrop = false;
        });
    }

    updateLoginLink();
}
