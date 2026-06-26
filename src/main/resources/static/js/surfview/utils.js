function normalizeFilterValue(value) {
    return String(value || '').trim().toLowerCase();
}

function normalizeTeachingLanguages(value) {
    if (Array.isArray(value)) {
        return value.flatMap(normalizeTeachingLanguages);
    }

    if (!value) {
        return [];
    }

    return [normalizeFilterValue(value)];
}

function isLanguageValueList(value) {
    return value.some(entry => entry && typeof entry === 'object' && 'language' in entry && 'value' in entry);
}

function cssEscape(value) {
    if (window.CSS?.escape) {
        return CSS.escape(String(value));
    }

    return String(value).replace(/["\\]/g, '\\$&');
}

function entityId(kind, entity) {
    if (kind === 'program') {
        return entity?.programId;
    }
    if (kind === 'organization') {
        return entity?.organizationId;
    }
    if (kind === 'person') {
        return entity?.personId;
    }
    return entity?.courseId;
}

function titleCase(value) {
    return value.charAt(0).toUpperCase() + value.slice(1);
}

function organizationIdValue(value) {
    if (!value) {
        return '';
    }

    if (typeof value === 'string') {
        return value;
    }

    return value.organizationId || '';
}

function academicSessionIdValue(value) {
    if (!value) {
        return '';
    }

    if (typeof value === 'string') {
        return value;
    }

    return value.academicSessionId || value.academic_session_id || '';
}

function studyLoadValue(value) {
    if (!value) {
        return '';
    }

    if (typeof value === 'string' || typeof value === 'number') {
        return value;
    }

    const amount = value.value || value.amount || '';
    const unit = value.studyLoadUnit || value.unit || '';
    return [amount, unit].filter(Boolean).join(' ');
}

function authorizationHeader() {
    const token = window.localStorage.getItem('jwt');
    return token ? {'Authorization': `Bearer ${token}`} : {};
}

function generateId() {
    if (window.crypto?.randomUUID) {
        return crypto.randomUUID();
    }

    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, char => {
        const random = Math.random() * 16 | 0;
        const value = char === 'x' ? random : (random & 0x3 | 0x8);
        return value.toString(16);
    });
}

function languageValue(value, language = 'en-GB') {
    return value ? [{language, value}] : [];
}

function removeEmptyPayloadValues(value) {
    if (Array.isArray(value)) {
        return value
            .map(removeEmptyPayloadValues)
            .filter(item => {
                if (Array.isArray(item)) {
                    return item.length > 0;
                }
                if (item && typeof item === 'object') {
                    return Object.keys(item).length > 0;
                }
                return item !== '' && item !== null && item !== undefined;
            });
    }

    if (!value || typeof value !== 'object') {
        return value;
    }

    return Object.fromEntries(Object.entries(value)
        .map(([key, child]) => [key, removeEmptyPayloadValues(child)])
        .filter(([, child]) => {
            if (Array.isArray(child)) {
                return child.length > 0;
            }
            if (child && typeof child === 'object') {
                return Object.keys(child).length > 0;
            }
            return child !== '' && child !== null && child !== undefined;
        }));
}

function collectLanguages(value, languages = new Set()) {
    if (Array.isArray(value)) {
        value.forEach(item => collectLanguages(item, languages));
        return [...languages].sort((a, b) => languageSortLabel(a).localeCompare(languageSortLabel(b)));
    }

    if (!value || typeof value !== 'object') {
        return [...languages].sort((a, b) => languageSortLabel(a).localeCompare(languageSortLabel(b)));
    }

    if (typeof value.language === 'string' && 'value' in value) {
        languages.add(value.language);
    }

    Object.values(value).forEach(child => collectLanguages(child, languages));
    return [...languages].sort((a, b) => languageSortLabel(a).localeCompare(languageSortLabel(b)));
}

function languageSortLabel(code) {
    return `${languageLabel(code)} ${code}`;
}

function languageLabel(code) {
    return Constants.languageNames[code] || code;
}

function plainCodeValue(value) {
    if (!value) {
        return '';
    }

    if (typeof value === 'string') {
        return value;
    }

    return value.code || '';
}

function listValue(value) {
    if (Array.isArray(value)) {
        return value.join(', ');
    }

    return value || '';
}

function isAuthenticated() {
    return Boolean(window.localStorage.getItem('jwt'));
}

function authUserNameFromToken(token) {
    if (!token) {
        return '';
    }
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.sub || payload.email || payload.preferred_username || payload.name || '';
    } catch {
        return '';
    }
}

function getAuthUserName() {
    return authUserNameFromToken(window.localStorage.getItem('jwt'));
}

function logout() {
    window.localStorage.removeItem('jwt');
    updateLoginLink();
}

function updateLoginLink() {
    const link = document.getElementById('login-link');
    const logoutButton = document.getElementById('logout-link');
    const name = getAuthUserName();
    const authenticated = isAuthenticated();

    if (link) {
        link.textContent = name || 'Log in';
    }

    if (logoutButton) {
        logoutButton.classList.toggle('hidden', !authenticated);
    }
}

function showLoginModal() {
    const backdrop = document.getElementById('login-modal-backdrop');
    if (!backdrop) {
        return;
    }

    backdrop.classList.add('open');
    document.body.style.overflow = 'hidden';
    document.getElementById('login-username')?.focus();
}

function hideLoginModal() {
    const backdrop = document.getElementById('login-modal-backdrop');
    if (!backdrop) {
        return;
    }

    backdrop.classList.remove('open');
    document.body.style.overflow = '';
    const form = document.getElementById('login-form');
    if (form) {
        form.reset();
    }
    const status = document.getElementById('login-result-message');
    if (status) {
        status.textContent = '';
    }
}

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

    if (response.status === 401 || response.status === 403) {
        window.localStorage.removeItem('jwt');
        updateLoginLink();
        showLoginModal();
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
        updateLoginLink();
        hideLoginModal();
        window.dispatchEvent(new CustomEvent('surfview:login'));
    } catch (error) {
        window.localStorage.removeItem('jwt');
        updateLoginLink();
        if (status) {
            status.textContent = error.message || 'Login failed';
        }
    } finally {
        if (submitButton) {
            submitButton.disabled = false;
        }
    }
}
