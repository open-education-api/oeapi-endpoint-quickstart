const UNKNOWN_REFERENCE_LABEL = '[UNKNOWN]';

const entityKindRegistry = {
    academicSession: {
        detailsHtml: entity => SurfView.AcademicSessions.academicSessionDetailsHtml(entity),
        openEditForm: entity => SurfView.AcademicSessions.openAcademicSessionEditForm(entity),
        submitForm: form => SurfView.AcademicSessions.submitAcademicSessionForm(form),
        payloadFromForm: form => SurfView.AcademicSessions.academicSessionPayloadFromForm(form),
        deleteCurrent: () => SurfView.AcademicSessions.deleteCurrentAcademicSession(),
        openCreate: () => SurfView.AcademicSessions.openCreateAcademicSessionModal(),
        createLabel: 'academic session'
    },
    organization: {
        detailsHtml: entity => SurfView.Organizations.organizationDetailsHtml(entity),
        openEditForm: entity => SurfView.Organizations.openOrganizationEditForm(entity),
        submitForm: form => SurfView.Organizations.submitOrganizationForm(form),
        payloadFromForm: (form, entity) => SurfView.Organizations.organizationPayloadFromForm(form, entity),
        deleteCurrent: () => SurfView.Organizations.deleteCurrentOrganization(),
        openCreate: () => SurfView.Organizations.openCreateOrganizationModal(),
        createLabel: 'organization'
    },
    person: {
        detailsHtml: entity => SurfView.Persons.personDetailsHtml(entity),
        openEditForm: entity => SurfView.Persons.openPersonEditForm(entity),
        submitForm: form => SurfView.Persons.submitPersonForm(form),
        payloadFromForm: (form, entity) => SurfView.Persons.personPayloadFromForm(form, entity),
        deleteCurrent: () => SurfView.Persons.deleteCurrentPerson(),
        openCreate: () => SurfView.Persons.openCreatePersonModal(),
        createLabel: 'person'
    },
    course: moduleKindRegistryEntry('course'),
    program: moduleKindRegistryEntry('program')
};

function moduleKindRegistryEntry(kind) {
    return {
        detailsHtml: (entity, kind, options = {}) => SurfView.Module.moduleDetailsHtml(kind, entity, options),
        openEditForm: () => SurfView.Module.openModuleEditForm(),
        submitForm: form => SurfView.Module.submitModuleForm(form),
        payloadFromForm: (form, entity, kind) => SurfView.Module.modulePayloadFromForm(kind, form, entity),
        openCreate: () => SurfView.Module.openCreateModuleModal(kind),
        preloadDetails: () => preloadLookups([
            loadOrganizations,
            loadPersons
        ]),
        loadOfferings: true
    };
}

function entityKindConfig(kind) {
    return entityKindRegistry[kind] || null;
}

function currentEntityKindConfig() {
    return entityKindConfig(SurfView.State.currentModalKind);
}

function tabEntityKindConfig(tabKey = SurfView.State.tab) {
    return entityKindConfig(Constants.tabs[tabKey]?.kind);
}

/** Loads and renders the active tab collection. */
async function loadCurrentPage() {
    const tab = Constants.tabs[SurfView.State.tab];

    titleEl.textContent = tab.title;
    updateFilterSummary(0);
    statusEl.textContent = 'Loading...';
    tableRegion.replaceChildren();
    SurfView.State.totalPages = 1;
    updatePager();

    try {
        const entities = await loadAllEndpointItems(tab.endpoint);
        SurfView.State.currentPageEntities = entities;

        if (activeTabUsesEducationFilters()) {
            renderLanguageFilters(entities);
        }
        if (activeTabUsesFilters()) {
            syncFiltersFromUrl();
        }

        if (tab.kind) {
            const map = entityMap(tab.kind);
            map.clear();
            entities.forEach(entity => {
                const id = entityId(tab.kind, entity);
                if (id) {
                    map.set(String(id), entity);
                }
            });
        }

        renderEntityList();
    } catch (error) {
        SurfView.State.currentPageEntities = [];
        tableRegion.className = 'error';
        tableRegion.textContent = error.message;
        statusEl.textContent = 'Unable to load data';
        SurfView.State.totalPages = 1;
    }

    updatePager();
}

/** Selects a catalog tab and optionally loads it. */
function selectTab(tabKey, options = {}) {
    if (!Constants.tabs[tabKey]) {
        return;
    }

    SurfView.UserAdmin?.close();

    if (options.clearEntity && currentEntityFromUrl()) {
        SurfView.State.suppressModalHistory = true;
        closeEntityModal();
        SurfView.State.suppressModalHistory = false;
    }

    SurfView.State.tab = tabKey;
    SurfView.State.page = 1;
    searchInput.placeholder = `Search ${Constants.tabs[SurfView.State.tab].noun}...`;
    tabButtons.forEach(button => {
        const isActive = button.dataset.tab === tabKey;
        button.classList.toggle('active', isActive);
        button.setAttribute('aria-selected', String(isActive));
    });
    document.getElementById('catalog-panel')?.setAttribute('aria-labelledby', `tab-${tabKey}`);
    updateMainAction();
    layoutEl.classList.toggle('without-filters', !activeTabUsesFilters());
    educationFilterGroups.forEach(group => group.classList.toggle('hidden', !activeTabUsesEducationFilters()));
    personFilterGroups.forEach(group => group.classList.toggle('hidden', !activeTabUsesPersonFilters()));

    if (options.pushHistory) {
        const url = new URL(window.location.href);
        url.searchParams.set('tab', tabKey);
        url.searchParams.delete('section');
        if (options.clearEntity) {
            entityIdParams().forEach(param => url.searchParams.delete(param));
            url.searchParams.delete('lang');
        }
        window.history.pushState({tab: tabKey}, '', url);
    }

    if (options.load !== false) {
        loadCurrentPage();
    }
}

/** Returns whether write controls should be hidden. */
function isReadOnlyMode() {
    return typeof canWrite === 'function' ? !canWrite() : true;
}

function canCreateOnCurrentTab() {
    return Boolean(tabEntityKindConfig()?.openCreate);
}

/** Updates the main Add button for the active tab. */
function updateMainAction() {
    const tab = Constants.tabs[SurfView.State.tab];
    const noun = tab.noun.replace(/s$/, '');
    mainActionButton.classList.toggle('visible', canCreateOnCurrentTab() && !isReadOnlyMode());
    mainActionButton.textContent = `Add ${noun}`;
}

function openCreateEntityForCurrentTab() {
    if (isReadOnlyMode()) {
        return;
    }

    tabEntityKindConfig()?.openCreate?.();
}

function deleteCurrentModalEntity() {
    if (isReadOnlyMode()) {
        return;
    }

    currentEntityKindConfig()?.deleteCurrent?.();
}

/** Refreshes UI controls affected by write permission. */
function updateWriteControls() {
    document.body.classList.toggle('read-only-mode', isReadOnlyMode());
    updateMainAction();
    renderEntityModalActions();
    if (isReadOnlyMode()) {
        closeWriteSurfacesForReadOnly();
    } else if (SurfView.State.currentModalMode === 'details'
        && moduleModal.backdrop.classList.contains('open')
        && SurfView.State.currentModalEntity) {
        modalBody.replaceChildren(currentEntityDetailsHtml());
    }
}

/** Closes open write-only surfaces in read-only mode. */
function closeWriteSurfacesForReadOnly() {
    if (nestedOfferingModal.backdrop.classList.contains('open')) {
        closeNestedModal();
    }
    if (offeringModal.backdrop.classList.contains('open')) {
        SurfView.Offerings.closeAddOfferingModal();
    }
    if (SurfView.State.currentModalMode === 'form') {
        closeEntityEditForm();
    } else if (moduleModal.backdrop.classList.contains('open') && SurfView.State.currentModalEntity) {
        modalBody.replaceChildren(currentEntityDetailsHtml());
    }
}

/** Renders the active tab's filtered entity table. */
function renderEntityList() {
    const tab = Constants.tabs[SurfView.State.tab];
    const entities = applyInMemoryEntityFilters(SurfView.State.currentPageEntities);
    const totalEntities = entities.length;
    SurfView.State.totalPages = Math.max(Math.ceil(totalEntities / Constants.pageSize), 1);
    if (SurfView.State.page > SurfView.State.totalPages) {
        SurfView.State.page = SurfView.State.totalPages;
    }

    const pageStart = (SurfView.State.page - 1) * Constants.pageSize;
    const pageEntities = entities.slice(pageStart, pageStart + Constants.pageSize);
    // render table
    tableRegion.className = 'table-wrap';
    statusEl.textContent = `${pageEntities.length} of ${totalEntities} entit${totalEntities === 1 ? 'y' : 'ies'}`;
    updateFilterSummary(entities.length);
    updatePager();

    if (!pageEntities.length) {
        tableRegion.className = 'empty';
        tableRegion.textContent = 'No entities found';
        return;
    }

    tableRegion.replaceChildren(tableElement(tab, pageEntities));
}

function updatePager() {
    pagerEl.classList.toggle('hidden', SurfView.State.totalPages <= 1);
    pageIndicator.textContent = `Page ${SurfView.State.page} of ${SurfView.State.totalPages}`;
    prevButton.disabled = SurfView.State.page <= 1;
    nextButton.disabled = SurfView.State.page >= SurfView.State.totalPages;
}

/** Returns the display name for an entity. */
function entityDisplayName(kind, entity) {
    switch (kind) {
        case 'academicSession':
            return SurfView.AcademicSessions.academicSessionLabel(entity);
        case 'person':
            return entity?.displayName || [entity?.givenName, entity?.surname].filter(Boolean).join(' ');
        default:
            return textValueLocalizedAttribute(entity?.name);
    }
}

/** Returns the cache map for an entity kind. */
function entityMap(kind) {
    const tabKey = tabKeyByKind(kind);
    return tabKey ? entityMaps[tabKey] : courseById;
}

/** Renders shared entity modal action buttons. */
function renderEntityModalActions() {
    const mode = SurfView.State.currentModalMode;
    const kind = SurfView.State.currentModalKind;
    const showDetailActions = mode === 'details';
    const showFormActions = mode === 'form';
    const actions = [];

    entityTitleActions.replaceChildren();

    if (showFormActions && !isReadOnlyMode()) {
        entityTitleActions.replaceChildren(createElement('button#entity-json-preview-button.json-preview-button', {
            text: 'Show JSON',
            attributes: {type: 'button'}
        }));
    }

    if (showDetailActions && !isReadOnlyMode()) {
        actions.push(createElement('button#entity-edit-button.detail-action-button.modal-action-button', {
            text: 'Edit',
            attributes: {type: 'button'}
        }));

        if (entityKindConfig(kind)?.deleteCurrent) {
            const deleteButton = createElement('button#entity-delete-button.detail-action-button.modal-action-button.danger-button', {
                text: 'Delete',
                attributes: {type: 'button'}
            });
            deleteButton.disabled = SurfView.State.currentEntityDeletePending;
            actions.push(deleteButton);
        }
    }

    entityModalActions.replaceChildren(...actions);
}

/** Sets the shared entity modal mode. */
function setEntityModalMode(mode) {
    SurfView.State.currentModalMode = mode;
    renderEntityModalActions();
}

/** Updates the shared entity delete pending state. */
function setEntityDeletePending(isPending) {
    SurfView.State.currentEntityDeletePending = isPending;
    renderEntityModalActions();
}

/** Builds the current modal entity detail view. */
function currentEntityDetailsHtml() {
    const kind = SurfView.State.currentModalKind;
    return entityKindConfig(kind).detailsHtml(SurfView.State.currentModalEntity, kind, {
        offerings: SurfView.State.currentModalOfferings
    });
}

/** Opens the edit form for the current modal entity. */
async function openEntityEditForm() {
    if (!SurfView.State.currentModalEntity || isReadOnlyMode()) {
        return;
    }

    await currentEntityKindConfig().openEditForm(SurfView.State.currentModalEntity);
}

/** Closes the current modal entity edit form. */
function closeEntityEditForm() {
    if (!SurfView.State.currentModalEntity) {
        return;
    }

    if (document.getElementById('entity-edit-form')?.dataset.entityMode === 'create') {
        closeEntityModal();
        return;
    }

    if (currentEntityKindConfig()?.loadOfferings) {
        SurfView.Module.closeModuleEditForm();
        return;
    }

    modalBody.replaceChildren(currentEntityDetailsHtml());
    setEntityModalMode('details');
}

/** Submits the current modal entity edit form. */
async function submitEntityEditForm(event) {
    event.preventDefault();
    if (isReadOnlyMode()) {
        closeEntityEditForm();
        return;
    }
    const form = event.target;
    const kind = form.dataset.entityKind;

    await entityKindConfig(kind).submitForm(form);
}

/** Opens a JSON preview for the current entity form. */
function openEntityJsonPreview() {
    const form = document.getElementById('entity-edit-form');
    if (!form || !SurfView.State.currentModalEntity || isReadOnlyMode()) {
        return;
    }

    const kind = form.dataset.entityKind;
    const mode = form.dataset.entityMode || 'edit';
    const status = document.getElementById('entity-edit-form-status');
    openJsonPreview(
        `${titleCase(kind)} JSON`,
        `Payload that will be submitted when ${mode === 'create' ? 'adding' : 'saving'} this ${kind}`,
        () => entityKindConfig(kind).payloadFromForm(form, SurfView.State.currentModalEntity, kind),
        {
            status,
            errorMessage: `Unable to preview ${kind}`
        }
    );
}

/** Opens the shared detail modal for an entity. */
async function openEntityModal(kind, entity, options = {}) {
    const config = entityKindConfig(kind);
    SurfView.State.currentModalKind = kind;
    SurfView.State.currentModalEntity = entity;
    SurfView.State.currentModalOfferings = [];
    SurfView.State.currentModalLanguages = collectLanguages(entity);
    SurfView.State.currentModalLanguage = chooseLanguage(SurfView.State.currentModalLanguages);
    const id = entityId(kind, entity);
    const name = entityDisplayName(kind, entity) || id || titleCase(kind);
    moduleModal.title.textContent = name;
    setEntityDeletePending(false);
    setEntityModalMode('details');
    await config.preloadDetails?.();
    const details = config.detailsHtml(entity, kind, {loadingOfferings: config.loadOfferings});
    modalBody.replaceChildren(details);
    openManagedModal(moduleModal);

    if (options.pushHistory && id) {
        updateEntityUrl(kind, id, SurfView.State.currentModalLanguage);
    }

    if (!config.loadOfferings) {
        return;
    }

    const offerings = await SurfView.Offerings.loadEntityOfferings(kind, id);
    SurfView.State.currentModalOfferings = offerings;
    SurfView.State.currentModalLanguages = collectLanguages({module: entity, offerings});
    if (!SurfView.State.currentModalLanguage || !SurfView.State.currentModalLanguages.includes(SurfView.State.currentModalLanguage)) {
        SurfView.State.currentModalLanguage = chooseLanguage(SurfView.State.currentModalLanguages);
        updateEntityUrl(kind, id, SurfView.State.currentModalLanguage, true);
    }
    modalBody.replaceChildren(SurfView.Module.moduleDetailsHtml(SurfView.State.currentModalKind, entity, {offerings}));
}

/** Closes the shared entity detail modal. */
function closeEntityModal() {
    if (offeringModal.backdrop.classList.contains('open')) {
        SurfView.Offerings.closeAddOfferingModal({updateHistory: !SurfView.State.suppressModalHistory});
    }
    closeManagedModal(moduleModal);
    SurfView.State.currentModalEntity = null;
    SurfView.State.currentModalKind = 'course';
    SurfView.State.currentModalOfferings = [];
    SurfView.State.currentModalLanguages = [];
    SurfView.State.currentModalLanguage = null;
    SurfView.State.currentEntityDeletePending = false;
    setEntityModalMode('closed');
    if (!SurfView.State.suppressModalHistory && currentEntityFromUrl()) {
        clearEntityUrl();
    }
}

/** Fetches and opens an entity by kind and id. */
async function openEntityById(kind, id, options = {}) {
    const cached = entityMap(kind).get(String(id));
    if (cached) {
        openEntityModal(kind, cached, options);
        return;
    }

    try {
        const tab = tabByKind(kind);
        if (!tab) {
            return;
        }

        const response = await callEndpoint(tab.detailEndpoint(id), {
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            return;
        }

        const entity = await response.json();
        const actualId = entityId(kind, entity) || id;
        if (actualId) {
            entityMap(kind).set(String(actualId), entity);
        }
        openEntityModal(kind, entity, options);
    } catch (error) {
        // Keep the catalog usable if a deep-linked entity cannot be loaded.
    }
}

/** Opens the request error details modal. */
function openErrorDetailsModal(error, summary) {
    errorDetailsModal.subtitle.textContent = summary || 'Request failed';
    errorDetailsContent.textContent = errorDetailsText(error);
    openManagedModal(errorDetailsModal);
}

/** Closes the request error details modal. */
function closeErrorDetailsModal() {
    closeManagedModal(errorDetailsModal);
    errorDetailsModal.subtitle.textContent = '';
    errorDetailsContent.textContent = '';
}

/** Chooses the best active language from candidates. */
function chooseLanguage(languages) {
    const fromUrl = new URL(window.location.href).searchParams.get('lang');
    if (fromUrl && languages.includes(fromUrl)) {
        return fromUrl;
    }

    if (languages.includes('en-GB')) {
        return 'en-GB';
    }

    if (languages.includes('nl-NL')) {
        return 'nl-NL';
    }

    return languages[0] || null;
}
