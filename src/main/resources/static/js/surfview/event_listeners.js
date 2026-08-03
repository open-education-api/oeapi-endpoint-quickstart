tabButtons.forEach(button => {
    button.addEventListener('click', () => {
        selectTab(button.dataset.tab, {
            pushHistory: true,
            clearEntity: true
        });
    });
});

levelFilterInputs.forEach(input => {
    input.addEventListener('change', () => {
        SurfView.State.page = 1;
        updateFilterUrl();
        renderEntityList();
    });
});

[...affiliationFilterInputs, ...activeEnrollmentFilterInputs].forEach(input => {
    input.addEventListener('change', () => {
        SurfView.State.page = 1;
        updateFilterUrl();
        renderEntityList();
    });
});

languageFilterOptions?.addEventListener('change', event => {
    if (event.target.matches('[data-language-filter]')) {
        SurfView.State.page = 1;
        updateFilterUrl();
        renderEntityList();
    }
});

searchInput.addEventListener('input', () => {
    SurfView.State.page = 1;
    renderEntityList();
});

function addBackdropDismissListener(modal, closeModal) {
    let pointerDownOnBackdrop = false;

    modal.backdrop.addEventListener('pointerdown', event => {
        pointerDownOnBackdrop = event.target === modal.backdrop;
    });

    modal.backdrop.addEventListener('click', event => {
        if (pointerDownOnBackdrop && event.target === modal.backdrop) {
            closeModal();
        }
        pointerDownOnBackdrop = false;
    });
}

moduleModal.close.addEventListener('click', closeEntityModal);
entityModalActions.addEventListener('click', event => {
    if (isReadOnlyMode()) {
        return;
    }

    if (event.target.closest('#entity-edit-button')) {
        openEntityEditForm();
        return;
    }

    if (!event.target.closest('#entity-delete-button')) {
        return;
    }

    deleteCurrentModalEntity();
});
entityTitleActions.addEventListener('click', event => {
    if (!isReadOnlyMode() && event.target.closest('#entity-json-preview-button')) {
        openEntityJsonPreview();
    }
});
addBackdropDismissListener(moduleModal, closeEntityModal);

initNestedModalListeners();
SurfView.Offerings.initEventListeners();
SurfView.Module.initEventListeners();
SurfView.Organizations.initEventListeners();
SurfView.AcademicSessions.initEventListeners();

errorDetailsModal.close.addEventListener('click', closeErrorDetailsModal);
addBackdropDismissListener(errorDetailsModal, closeErrorDetailsModal);

mainActionButton.addEventListener('click', () => {
    openCreateEntityForCurrentTab();
});
document.addEventListener('click', event => {
    const errorDetailsButton = event.target.closest('[data-open-error-details]');
    if (!errorDetailsButton) {
        return;
    }

    event.preventDefault();
    openErrorDetailsModal(errorDetailsButton.requestError, errorDetailsButton.requestErrorSummary);
});

document.addEventListener('keydown', event => {
    if (event.key === 'Escape') {
        const userFormBackdrop = document.getElementById('user-form-modal-backdrop');
        const loginBackdrop = document.getElementById('login-modal-backdrop');
        if (userFormBackdrop?.classList.contains('open')) {
            SurfView.UserAdmin.hideForm();
        } else if (loginBackdrop?.classList.contains('open')) {
            if (!privateLoginRequired()) {
                hideLoginModal();
            }
        } else if (errorDetailsModal.backdrop.classList.contains('open')) {
            closeErrorDetailsModal();
        } else if (jsonPreviewModal.backdrop.classList.contains('open')) {
            closeJsonPreview();
        } else if (nestedOfferingModal.backdrop.classList.contains('open')) {
            closeNestedModal();
        } else if (offeringModal.backdrop.classList.contains('open')) {
            SurfView.Offerings.closeAddOfferingModal();
        } else {
            closeEntityModal();
        }
    }
});

tableRegion.addEventListener('click', event => {
    const button = event.target.closest('[data-entity-kind][data-entity-id]');
    if (!button) {
        return;
    }

    const entity = entityMap(button.dataset.entityKind).get(button.dataset.entityId);
    if (entity) {
        openEntityModal(button.dataset.entityKind, entity, {pushHistory: true});
    }
});

modalBody.addEventListener('click', event => {
    const languageLink = event.target.closest('[data-language-code]');
    if (!languageLink || !SurfView.State.currentModalEntity) {
        return;
    }

    event.preventDefault();
    SurfView.State.currentModalLanguage = languageLink.dataset.languageCode;
    moduleModal.title.textContent = entityDisplayName(SurfView.State.currentModalKind, SurfView.State.currentModalEntity) || entityId(SurfView.State.currentModalKind, SurfView.State.currentModalEntity) || titleCase(SurfView.State.currentModalKind);
    modalBody.replaceChildren(currentEntityDetailsHtml());
    updateEntityUrl(SurfView.State.currentModalKind, entityId(SurfView.State.currentModalKind, SurfView.State.currentModalEntity), SurfView.State.currentModalLanguage);
});

delegateClick(modalBody, {
    '[data-cancel-entity-edit]': () => closeEntityEditForm()
});

modalBody.addEventListener('submit', event => {
    if (event.target?.id === 'entity-edit-form') {
        submitEntityEditForm(event);
    }
});

window.addEventListener('popstate', () => {
    syncFiltersFromUrl();
    if (new URL(window.location.href).searchParams.get('action') !== 'addOffering'
        && offeringModal.backdrop.classList.contains('open')) {
        SurfView.Offerings.closeAddOfferingModal({updateHistory: false});
    }
    const nextTab = tabFromUrl();
    if (nextTab === 'users') {
        if (SurfView.UserAdmin.isAvailable()) {
            SurfView.UserAdmin.open({updateHistory: false});
        }
        return;
    }
    SurfView.UserAdmin.close();
    const shouldReload = nextTab !== SurfView.State.tab;
    if (shouldReload) {
        selectTab(nextTab, {load: false});
    } else {
        renderEntityList();
    }

    const entity = currentEntityFromUrl();
    const syncModal = () => {
        if (entity) {
            openEntityById(entity.kind, entity.id, {pushHistory: false});
        } else {
            SurfView.State.suppressModalHistory = true;
            closeEntityModal();
            SurfView.State.suppressModalHistory = false;
        }
    };

    if (shouldReload) {
        loadCurrentPage().then(syncModal);
    } else {
        syncModal();
    }
});

prevButton.addEventListener('click', () => {
    if (SurfView.State.page > 1) {
        SurfView.State.page -= 1;
        renderEntityList();
    }
});

nextButton.addEventListener('click', () => {
    if (SurfView.State.page < SurfView.State.totalPages) {
        SurfView.State.page += 1;
        renderEntityList();
    }
});
