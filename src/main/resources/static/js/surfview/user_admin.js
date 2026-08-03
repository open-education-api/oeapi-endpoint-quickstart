window.SurfView = window.SurfView || {};

SurfView.UserAdmin = (() => {
    let users = [];
    let roles = [];

    const view = () => document.getElementById('user-admin-view');
    const status = () => document.getElementById('user-admin-status');
    const table = () => document.getElementById('user-admin-table');
    const form = () => document.getElementById('user-form');
    const formModal = () => modalElements(document.getElementById('user-form-modal-backdrop'));

    function isAvailable() {
        const mode = String(SurfView.Auth.mode || '').toLowerCase();
        return ['private', 'restricted'].includes(mode) && currentUserRoles().includes('ADMIN');
    }

    function updateVisibility() {
        document.getElementById('manage-users-link')?.classList.toggle('hidden', !isAvailable());
        if (!isAvailable() && !view()?.classList.contains('hidden')) {
            close();
        } else if (isAvailable() && tabFromUrl() === 'users' && view()?.classList.contains('hidden')) {
            open({updateHistory: false});
        }
    }

    async function api(path, options = {}) {
        const response = await callEndpoint(path, {
            ...options,
            headers: {'Accept': 'application/json', ...(options.body ? {'Content-Type': 'application/json'} : {})},
            promptLogin: false
        });
        if (!response.ok) {
            const error = new Error(response.status === 403
                ? 'Administrator access is required'
                : `Request failed with status ${response.status}`);
            error.status = response.status;
            throw error;
        }
        return response.status === 204 ? null : response.json();
    }

    async function load() {
        status().textContent = 'Loading...';
        [users, roles] = await Promise.all([api('/admin/users'), api('/admin/roles')]);
        render();
        status().textContent = '';
    }

    function render() {
        const container = table();
        container.replaceChildren();
        if (!users.length) {
            container.className = 'empty';
            container.textContent = 'No users found';
            return;
        }
        container.className = 'table-wrap';
        const grid = document.createElement('table');
        grid.className = 'user-admin-table';
        grid.innerHTML = '<thead><tr><th>Email</th><th>Roles</th><th>Actions</th></tr></thead>';
        const body = document.createElement('tbody');
        users.forEach(user => {
            const row = document.createElement('tr');
            const email = document.createElement('td');
            email.textContent = user.email;
            const roleNames = document.createElement('td');
            roleNames.textContent = (user.roles || []).map(role => role.name?.replace(/^ROLE_/, '')).filter(Boolean).join(', ') || 'None';
            const actions = document.createElement('td');
            actions.className = 'user-row-actions';
            actions.innerHTML = `<button type="button" data-edit-user="${user.id}">Edit</button><button type="button" data-delete-user="${user.id}">Delete</button>`;
            row.append(email, roleNames, actions);
            body.append(row);
        });
        grid.append(body);
        container.append(grid);
    }

    function showForm(user = null) {
        const userForm = form();
        userForm.reset();
        userForm.elements.id.value = user?.id || '';
        userForm.elements.email.value = user?.email || '';
        userForm.elements.password.required = !user;
        document.getElementById('user-form-title').textContent = user ? 'Edit user' : 'Add user';
        document.getElementById('user-password-hint').textContent = user ? '(leave blank to keep current)' : '';
        const selected = new Set((user?.roles || []).map(role => String(role.id)));
        const options = document.getElementById('user-role-options');
        const roleOrder = ['ROLE_ADMIN', 'ROLE_USER', 'ROLE_GUEST'];
        const orderedRoles = [...roles].sort((left, right) => {
            const leftIndex = roleOrder.indexOf(left.name);
            const rightIndex = roleOrder.indexOf(right.name);
            return (leftIndex === -1 ? roleOrder.length : leftIndex)
                - (rightIndex === -1 ? roleOrder.length : rightIndex);
        });
        options.replaceChildren(...orderedRoles.map(role => {
            const label = document.createElement('label');
            label.className = 'checkbox-row';
            const input = document.createElement('input');
            input.type = 'checkbox';
            input.name = 'roles';
            input.value = role.id;
            input.checked = selected.has(String(role.id));
            label.append(input, document.createTextNode(` ${role.name.replace(/^ROLE_/, '')}`));
            return label;
        }));
        openManagedModal(formModal(), {initialFocus: () => userForm.elements.email});
    }

    function hideForm() {
        closeManagedModal(formModal());
        form().reset();
    }

    function hydrateUserRoles(user) {
        const rolesById = new Map(roles.map(role => [String(role.id), role]));
        return {
            ...user,
            roles: (user.roles || []).map(role => rolesById.get(String(role.id)) || role)
        };
    }

    async function submit(event) {
        event.preventDefault();
        const userForm = event.currentTarget;
        const id = userForm.elements.id.value;
        const payload = {
            email: userForm.elements.email.value.trim(),
            roles: [...userForm.querySelectorAll('[name="roles"]:checked')].map(input => ({id: Number(input.value)}))
        };
        if (userForm.elements.password.value) payload.password = userForm.elements.password.value;
        status().textContent = id ? 'Saving...' : 'Creating...';
        try {
            const responseUser = await api(id ? `/admin/users/${encodeURIComponent(id)}` : '/admin/users', {
                method: id ? 'PUT' : 'POST', body: JSON.stringify(payload)
            });
            const saved = hydrateUserRoles(responseUser);
            users = id ? users.map(user => String(user.id) === String(id) ? saved : user) : [...users, saved];
            users.sort((a, b) => a.email.localeCompare(b.email));
            hideForm();
            render();
            status().textContent = id ? 'User saved' : 'User created';
        } catch (error) {
            status().textContent = error.status === 400 ? 'That email is already in use or the user is invalid' : error.message;
        }
    }

    async function remove(id) {
        const user = users.find(item => String(item.id) === String(id));
        if (!user || !window.confirm(`Delete ${user.email}?`)) return;
        status().textContent = 'Deleting...';
        try {
            await api(`/admin/users/${encodeURIComponent(id)}`, {method: 'DELETE'});
            users = users.filter(item => String(item.id) !== String(id));
            hideForm();
            render();
            status().textContent = 'User deleted';
            if (user.email === currentUserEmail()) await logout();
        } catch (error) {
            status().textContent = error.message;
        }
    }

    async function open(options = {}) {
        if (!isAvailable()) return;
        if (typeof closeAccountMenu === 'function') closeAccountMenu();
        if (options.updateHistory !== false) {
            const url = new URL(window.location.href);
            url.searchParams.set('tab', 'users');
            url.searchParams.delete('section');
            entityIdParams().forEach(param => url.searchParams.delete(param));
            url.searchParams.delete('lang');
            url.searchParams.delete('action');
            window.history.pushState({tab: 'users'}, '', url);
        }
        closeEntityModal();
        document.querySelector('.layout')?.classList.add('hidden');
        view()?.classList.remove('hidden');
        document.querySelectorAll('.tab-button').forEach(button => {
            button.classList.remove('active');
            button.setAttribute('aria-selected', 'false');
        });
        hideForm();
        try { await load(); } catch (error) { status().textContent = error.message; }
    }

    function close() {
        view()?.classList.add('hidden');
        document.querySelector('.layout')?.classList.remove('hidden');
        hideForm();
        const activeTab = document.querySelector(`.tab-button[data-tab="${SurfView.State.tab}"]`);
        activeTab?.classList.add('active');
        activeTab?.setAttribute('aria-selected', 'true');
    }

    function init() {
        document.getElementById('manage-users-link')?.addEventListener('click', open);
        document.getElementById('add-user-button')?.addEventListener('click', () => showForm());
        document.getElementById('cancel-user-button')?.addEventListener('click', hideForm);
        formModal().close?.addEventListener('click', hideForm);
        formModal().backdrop?.addEventListener('click', event => {
            if (event.target === formModal().backdrop) hideForm();
        });
        form()?.addEventListener('submit', submit);
        form()?.addEventListener('keydown', event => {
            const password = form().elements.password;
            const roleInputs = [...form().querySelectorAll('[name="roles"]')];
            if (event.key === 'Tab' && !event.shiftKey && event.target === password && roleInputs.length) {
                event.preventDefault();
                roleInputs[0].focus();
                return;
            }
            const roleIndex = roleInputs.indexOf(event.target);
            if (event.key === 'Tab' && !event.shiftKey && roleIndex >= 0 && roleIndex < roleInputs.length - 1) {
                event.preventDefault();
                roleInputs[roleIndex + 1].focus();
            } else if (event.key === 'Tab' && event.shiftKey && roleIndex > 0) {
                event.preventDefault();
                roleInputs[roleIndex - 1].focus();
            } else if (event.key === 'Tab' && event.shiftKey && roleIndex === 0) {
                event.preventDefault();
                password.focus();
            }
        });
        table()?.addEventListener('click', event => {
            const edit = event.target.closest('[data-edit-user]');
            const removeButton = event.target.closest('[data-delete-user]');
            if (edit) showForm(users.find(user => String(user.id) === edit.dataset.editUser));
            if (removeButton) remove(removeButton.dataset.deleteUser);
        });
        window.addEventListener('surfview:auth-changed', updateVisibility);
        updateVisibility();
    }

    return {init, open, close, hideForm, isAvailable, updateVisibility};
})();
