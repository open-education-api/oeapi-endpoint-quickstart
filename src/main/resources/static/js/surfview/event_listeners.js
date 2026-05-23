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
        state.page = 1;
        updateFilterUrl();
        renderCurrentEntities();
    });
});

languageFilterInputs.forEach(input => {
    input.addEventListener('change', () => {
        state.page = 1;
        updateFilterUrl();
        renderCurrentEntities();
    });
});

searchInput.addEventListener('input', () => {
    state.page = 1;
    renderCurrentEntities();
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

courseModal.close.addEventListener('click', closeEntityModal);
entityEditButton.addEventListener('click', openEntityEditForm);
entityDeleteButton.addEventListener('click', () => {
    if (currentModalKind === 'organization') {
        deleteCurrentOrganization();
    } else if (currentModalKind === 'person') {
        deleteCurrentPerson();
    }
});
entityJsonPreviewButton.addEventListener('click', openEntityJsonPreview);
addBackdropDismissListener(courseModal, closeEntityModal);

offeringModal.close.addEventListener('click', () => closeAddOfferingModal());
addBackdropDismissListener(offeringModal, closeAddOfferingModal);
offeringJsonPreviewButton.addEventListener('click', openOfferingJsonPreview);
jsonPreviewModal.close.addEventListener('click', closeOfferingJsonPreview);
addBackdropDismissListener(jsonPreviewModal, closeOfferingJsonPreview);

errorDetailsModal.close.addEventListener('click', closeErrorDetailsModal);
addBackdropDismissListener(errorDetailsModal, closeErrorDetailsModal);

offeringForm.addEventListener('submit', submitOfferingForm);
offeringForm.addEventListener('click', event => {
    const cancelButton = event.target.closest('[data-cancel-offering]');
    if (cancelButton) {
        event.preventDefault();
        closeAddOfferingModal();
        return;
    }

    const languageButton = event.target.closest('[data-offering-language-code]');
    if (languageButton) {
        event.preventDefault();
        switchDraftOfferingLanguage(languageButton.dataset.offeringLanguageCode);
        return;
    }

    const deletePriceButton = event.target.closest('[data-delete-price-index]');
    if (deletePriceButton) {
        event.preventDefault();
        deleteDraftNestedItem(draftOffering.priceInformation, deletePriceButton.dataset.deletePriceIndex);
        return;
    }

    const deleteAddressButton = event.target.closest('[data-delete-address-index]');
    if (deleteAddressButton) {
        event.preventDefault();
        deleteDraftNestedItem(draftOffering.addresses, deleteAddressButton.dataset.deleteAddressIndex);
        return;
    }

    const editAddressButton = event.target.closest('[data-edit-address-index]');
    if (editAddressButton) {
        event.preventDefault();
        openNestedOfferingModal('address', {index: editAddressButton.dataset.editAddressIndex});
        return;
    }

    const nestedButton = event.target.closest('[data-open-nested-offering]');
    if (!nestedButton) {
        return;
    }
    openNestedOfferingModal(nestedButton.dataset.openNestedOffering);
});

nestedOfferingModal.close.addEventListener('click', closeNestedOfferingModal);
addBackdropDismissListener(nestedOfferingModal, closeNestedOfferingModal);
nestedOfferingForm.addEventListener('submit', submitNestedOfferingForm);
nestedOfferingForm.addEventListener('click', event => {
    if (event.target.closest('[data-cancel-nested]')) {
        closeNestedOfferingModal();
        return;
    }

    const languageButton = event.target.closest('[data-price-language-code]');
    if (languageButton) {
        event.preventDefault();
        switchDraftNestedPriceLanguage(languageButton.dataset.priceLanguageCode);
        return;
    }

    const addressLanguageButton = event.target.closest('[data-address-language-code]');
    if (addressLanguageButton) {
        event.preventDefault();
        switchDraftNestedAddressLanguage(addressLanguageButton.dataset.addressLanguageCode);
    }
});

mainActionButton.addEventListener('click', () => {
    if (state.tab === 'organizations') {
        openCreateOrganizationModal();
    } else if (state.tab === 'persons') {
        openCreatePersonModal();
    } else if (state.tab === 'academicSessions') {
        openAcademicSessionModal();
    }
});
academicSessionModal.close.addEventListener('click', closeAcademicSessionModal);
addBackdropDismissListener(academicSessionModal, closeAcademicSessionModal);
academicSessionForm.addEventListener('submit', submitAcademicSessionForm);
academicSessionForm.addEventListener('click', event => {
    const cancelButton = event.target.closest('[data-cancel-academic-session]');
    if (cancelButton) {
        event.preventDefault();
        closeAcademicSessionModal();
        return;
    }

    const addNameButton = event.target.closest('[data-add-academic-session-name]');
    if (addNameButton) {
        event.preventDefault();
        addAcademicSessionNameRow();
        return;
    }

    const deleteNameButton = event.target.closest('[data-delete-academic-session-name]');
    if (deleteNameButton) {
        event.preventDefault();
        deleteNameButton.closest('.academic-session-name-row')?.remove();
    }
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
        if (errorDetailsModal.backdrop.classList.contains('open')) {
            closeErrorDetailsModal();
        } else if (jsonPreviewModal.backdrop.classList.contains('open')) {
            closeOfferingJsonPreview();
        } else if (nestedOfferingModal.backdrop.classList.contains('open')) {
            closeNestedOfferingModal();
        } else if (offeringModal.backdrop.classList.contains('open')) {
            closeAddOfferingModal();
        } else if (academicSessionModal.backdrop.classList.contains('open')) {
            closeAcademicSessionModal();
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
    const cancelEntityEditButton = event.target.closest('[data-cancel-entity-edit]');
    if (cancelEntityEditButton) {
        event.preventDefault();
        closeEntityEditForm();
        return;
    }

    const addEntityLanguageButton = event.target.closest('[data-add-entity-language]');
    if (addEntityLanguageButton) {
        event.preventDefault();
        addEntityLanguageRow();
        return;
    }

    const deleteEntityLanguageButton = event.target.closest('[data-delete-entity-language]');
    if (deleteEntityLanguageButton) {
        event.preventDefault();
        deleteEntityLanguageRow(deleteEntityLanguageButton);
        return;
    }

    const deleteEntityArrayValueButton = event.target.closest('[data-delete-entity-array-value]');
    if (deleteEntityArrayValueButton) {
        event.preventDefault();
        deleteEntityArrayValueButton.closest('.entity-array-chip')?.remove();
        return;
    }

    const addOrganizationLanguageButton = event.target.closest('[data-add-organization-language]');
    if (addOrganizationLanguageButton) {
        event.preventDefault();
        addOrganizationLanguageRow();
        return;
    }

    const deleteOrganizationLanguageButton = event.target.closest('[data-delete-organization-language]');
    if (deleteOrganizationLanguageButton) {
        event.preventDefault();
        deleteOrganizationLanguageRow(deleteOrganizationLanguageButton);
        return;
    }

    const addOrganizationCodeButton = event.target.closest('[data-add-organization-code]');
    if (addOrganizationCodeButton) {
        event.preventDefault();
        const list = document.getElementById('organization-code-list');
        list?.append(organizationCodeRow());
        list?.lastElementChild?.querySelector('input')?.focus();
        return;
    }

    const deleteOrganizationCodeButton = event.target.closest('[data-delete-organization-code]');
    if (deleteOrganizationCodeButton) {
        event.preventDefault();
        deleteOrganizationCodeButton.closest('.organization-code-row')?.remove();
        return;
    }

    const addOrganizationAddressButton = event.target.closest('[data-add-organization-address]');
    if (addOrganizationAddressButton) {
        event.preventDefault();
        const languages = organizationLocalizedNameLanguages();
        if (!languages.length) {
            return;
        }
        const list = document.getElementById('organization-address-list');
        list?.append(organizationAddressRow({}, '', languages));
        list?.lastElementChild?.querySelector('select, input')?.focus();
        return;
    }

    const deleteOrganizationAddressButton = event.target.closest('[data-delete-organization-address]');
    if (deleteOrganizationAddressButton) {
        event.preventDefault();
        deleteOrganizationAddressButton.closest('.organization-address-row')?.remove();
        return;
    }

    const deleteOfferingButton = event.target.closest('[data-delete-offering-id]');
    if (deleteOfferingButton) {
        event.preventDefault();
        deleteOffering(deleteOfferingButton);
        return;
    }

    const editOfferingButton = event.target.closest('[data-edit-offering-index]');
    if (editOfferingButton) {
        event.preventDefault();
        openEditOfferingModal(editOfferingButton.dataset.editOfferingIndex);
        return;
    }

    const addOfferingButton = event.target.closest('[data-add-offering-kind][data-add-offering-id]');
    if (addOfferingButton) {
        event.preventDefault();
        openAddOfferingModal(addOfferingButton.dataset.addOfferingKind, addOfferingButton.dataset.addOfferingId);
        return;
    }

    const languageLink = event.target.closest('[data-language-code]');
    if (!languageLink || !currentModalEntity) {
        return;
    }

    event.preventDefault();
    currentModalLanguage = languageLink.dataset.languageCode;
    courseModal.title.textContent = entityDisplayName(currentModalKind, currentModalEntity) || entityId(currentModalKind, currentModalEntity) || titleCase(currentModalKind);
    const details = currentModalKind === 'organization'
        ? organizationDetailsHtml(currentModalEntity)
        : currentModalKind === 'person'
            ? personDetailsHtml(currentModalEntity)
            : entityDetailsHtml(currentModalKind, currentModalEntity, {offerings: currentModalOfferings});
    modalBody.replaceChildren(details);
    updateEntityUrl(currentModalKind, entityId(currentModalKind, currentModalEntity), currentModalLanguage);
});

modalBody.addEventListener('keydown', event => {
    const input = event.target.closest('[data-entity-array-input]');
    if (input && event.key === 'Enter') {
        event.preventDefault();
        addEntityArrayValue(input);
    }
});

modalBody.addEventListener('input', event => {
    if (event.target.matches('.organization-language-row input[name="organizationName"]')) {
        syncOrganizationAddressLanguages();
    }
});

modalBody.addEventListener('change', event => {
    if (event.target.matches('.organization-language-row select[name="organizationLanguage"]')) {
        syncOrganizationAddressLanguages();
    }
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
        closeAddOfferingModal({updateHistory: false});
    }
    const nextTab = tabFromUrl();
    const shouldReload = nextTab !== state.tab;
    if (shouldReload) {
        selectTab(nextTab, {load: false});
    } else {
        renderCurrentEntities();
    }

    const entity = currentEntityFromUrl();
    const syncModal = () => {
        if (entity) {
            openEntityById(entity.kind, entity.id, {pushHistory: false});
        } else {
            suppressModalHistory = true;
            closeEntityModal();
            suppressModalHistory = false;
        }
    };

    if (shouldReload) {
        loadCurrentPage().then(syncModal);
    } else {
        syncModal();
    }
});

prevButton.addEventListener('click', () => {
    if (state.page > 1) {
        state.page -= 1;
        renderCurrentEntities();
    }
});

nextButton.addEventListener('click', () => {
    if (state.page < state.totalPages) {
        state.page += 1;
        renderCurrentEntities();
    }
});
