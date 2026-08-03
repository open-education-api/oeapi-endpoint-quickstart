const modalStack = [];
const focusableSelector = [
    'a[href]',
    'button:not([disabled])',
    'input:not([disabled]):not([type="hidden"])',
    'select:not([disabled])',
    'textarea:not([disabled])',
    '[tabindex]:not([tabindex="-1"])'
].join(',');

function focusableElements(container) {
    return [...container.querySelectorAll(focusableSelector)].filter(element => {
        return !element.hidden
            && element.getAttribute('aria-hidden') !== 'true'
            && element.offsetParent !== null;
    });
}

function topOpenModal() {
    return modalStack[modalStack.length - 1] || null;
}

function updateModalIsolation() {
    const topModal = topOpenModal();
    const topBackdrop = topModal?.modal.backdrop || null;

    [...document.body.children].forEach(child => {
        const shouldIsolate = Boolean(topBackdrop) && child !== topBackdrop;
        child.inert = shouldIsolate;
        if (shouldIsolate) {
            child.setAttribute('aria-hidden', 'true');
        } else {
            child.removeAttribute('aria-hidden');
        }
    });

    document.body.style.overflow = modalStack.length ? 'hidden' : '';
}

function focusModal(modal, initialFocus) {
    const target = typeof initialFocus === 'function' ? initialFocus() : initialFocus;
    const fallback = modal.close || focusableElements(modal.backdrop)[0] || modal.dialog;
    const focusTarget = target || fallback;

    if (!focusTarget) {
        return;
    }

    if (!focusTarget.hasAttribute('tabindex') && focusTarget === modal.dialog) {
        focusTarget.setAttribute('tabindex', '-1');
    }
    focusTarget.focus();
}

/** Opens a modal with focus and inert background handling. */
function openManagedModal(modal, options = {}) {
    const existingIndex = modalStack.findIndex(entry => entry.modal.backdrop === modal.backdrop);
    const [existingEntry] = existingIndex === -1 ? [] : modalStack.splice(existingIndex, 1);

    const opener = existingEntry?.opener
        || (document.activeElement instanceof HTMLElement ? document.activeElement : null);
    modal.backdrop.classList.add('open');
    modalStack.push({modal, opener});
    updateModalIsolation();
    focusModal(modal, options.initialFocus);
}

/** Closes a managed modal and restores focus. */
function closeManagedModal(modal, options = {}) {
    const stackIndex = modalStack.findIndex(entry => entry.modal.backdrop === modal.backdrop);
    const [entry] = stackIndex === -1 ? [] : modalStack.splice(stackIndex, 1);

    modal.backdrop.classList.remove('open');
    updateModalIsolation();

    if (options.restoreFocus === false) {
        return;
    }

    const opener = entry?.opener;
    if (opener?.isConnected && !opener.inert) {
        opener.focus();
        return;
    }

    const topModal = topOpenModal();
    if (topModal) {
        focusModal(topModal.modal);
    }
}

document.addEventListener('keydown', event => {
    if (event.key !== 'Tab') {
        return;
    }

    const topModal = topOpenModal();
    if (!topModal) {
        return;
    }

    const focusable = focusableElements(topModal.modal.backdrop);
    if (!focusable.length) {
        event.preventDefault();
        focusModal(topModal.modal);
        return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    if (!topModal.modal.backdrop.contains(document.activeElement)) {
        event.preventDefault();
        first.focus();
    } else if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
    }
});
