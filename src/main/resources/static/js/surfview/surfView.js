const state = {
    tab: 'courses',
    page: 1,
    totalPages: 1
};

const tableRegion = document.getElementById('table-region');
const layoutEl = document.querySelector('.layout');
const statusEl = document.getElementById('status');
const titleEl = document.getElementById('view-title');
const filterSummary = document.getElementById('filter-summary');
const searchInput = document.getElementById('filter-search');
const pagerEl = document.querySelector('.pager');
const pageIndicator = document.getElementById('page-indicator');
const prevButton = document.getElementById('prev-page');
const nextButton = document.getElementById('next-page');
const courseModal = modalElements(document.getElementById('course-modal-backdrop'));
const entityEditButton = document.getElementById('entity-edit-button');
const entityDeleteButton = document.getElementById('entity-delete-button');
const entityJsonPreviewButton = document.getElementById('entity-json-preview-button');
const modalBody = document.getElementById('course-modal-body');
const offeringModal = modalElements(document.getElementById('offering-modal-backdrop'));
const offeringJsonPreviewButton = document.getElementById('offering-json-preview-button');
const offeringForm = document.getElementById('offering-form');
const jsonPreviewModal = modalElements(document.getElementById('json-preview-modal-backdrop'));
const jsonPreviewContent = document.getElementById('json-preview-content');
const errorDetailsModal = modalElements(document.getElementById('error-details-modal-backdrop'));
const errorDetailsContent = document.getElementById('error-details-content');
const nestedOfferingModal = modalElements(document.getElementById('nested-offering-modal-backdrop'));
const nestedOfferingForm = document.getElementById('nested-offering-form');
const mainActionButton = document.getElementById('main-action-button');
const academicSessionModal = modalElements(document.getElementById('academic-session-modal-backdrop'));
const academicSessionForm = document.getElementById('academic-session-form');
const courseById = new Map();
const programById = new Map();
const organizationById = new Map();
const personById = new Map();
const offeringCountByEntity = new Map();
const organizationsById = new Map();
const academicSessionsById = new Map();
const levelFilterInputs = [...document.querySelectorAll('[data-level-filter]')];
const languageFilterInputs = [...document.querySelectorAll('[data-language-filter]')];
const tabButtons = [...document.querySelectorAll('.tab-button')];
let suppressModalHistory = false;
let currentModalEntity = null;
let currentModalKind = 'course';
let currentModalOfferings = [];
let currentModalLanguages = [];
let currentModalLanguage = null;
let currentPageEntities = [];
let tableRenderId = 0;
let draftOffering = initialDraftOffering();
let organizationsLoadPromise = null;
let academicSessionsLoadPromise = null;
let personsLoadPromise = null;

function initialDraftOffering() {
    return {
        addresses: [],
        priceInformation: [],
        language: 'en-GB',
        languages: [],
        languageValues: {},
        nestedPriceLanguage: 'en-GB',
        nestedPriceLanguageValues: {},
        nestedAddressLanguage: 'en-GB',
        nestedAddressLanguageValues: {}
    };
}

function resetDraftOffering() {
    draftOffering = initialDraftOffering();
}

function modalElements(backdrop) {
    return {
        backdrop,
        title: backdrop.querySelector('.modal-title'),
        subtitle: backdrop.querySelector('.modal-subtitle'),
        close: backdrop.querySelector('.modal-close')
    };
}

async function loadCurrentPage() {
    const tab = Constants.tabs[state.tab];

    titleEl.textContent = tab.title;
    updateFilterSummary(0);
    statusEl.textContent = 'Loading...';
    tableRegion.replaceChildren();
    state.totalPages = 1;
    updatePager();

    try {
        const entities = await loadAllEndpointItems(tab.endpoint);
        currentPageEntities = entities;

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

        renderCurrentEntities();
    } catch (error) {
        currentPageEntities = [];
        tableRegion.className = 'error';
        tableRegion.textContent = error.message;
        statusEl.textContent = 'Unable to load data';
        state.totalPages = 1;
    }

    updatePager();
}

async function loadAllEndpointItems(endpoint) {
    const entities = [];
    let pageNumber = 1;
    let totalPages = 1;

    do {
        const url = new URL(endpoint, window.location.origin);
        url.searchParams.set('pageSize', Constants.loadPageSize);
        url.searchParams.set('pageNumber', pageNumber);
        const response = await callEndpoint(url.toString(), {
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Request failed with status ${response.status}`);
        }

        const payload = await response.json();
        const items = Array.isArray(payload.items) ? payload.items : [];
        entities.push(...items);
        totalPages = Math.max(Number(payload.totalPages) || 1, 1);
        pageNumber += 1;
    } while (pageNumber <= totalPages);

    return entities;
}

function selectTab(tabKey, options = {}) {
    if (!Constants.tabs[tabKey]) {
        return;
    }

    if (options.clearEntity && currentEntityFromUrl()) {
        suppressModalHistory = true;
        closeEntityModal();
        suppressModalHistory = false;
    }

    state.tab = tabKey;
    state.page = 1;
    searchInput.placeholder = `Search ${Constants.tabs[state.tab].noun}...`;
    tabButtons.forEach(button => {
        button.classList.toggle('active', button.dataset.tab === tabKey);
    });
    updateMainAction();

    if (options.pushHistory) {
        updateTabUrl(tabKey, {clearEntity: options.clearEntity});
    }

    if (options.load !== false) {
        loadCurrentPage();
    }
}

function updateTabUrl(tabKey, options = {}) {
    const url = new URL(window.location.href);
    url.searchParams.set('tab', tabKey);
    url.searchParams.delete('section');
    if (options.clearEntity) {
        url.searchParams.delete('courseId');
        url.searchParams.delete('programId');
        url.searchParams.delete('organizationId');
        url.searchParams.delete('personId');
        url.searchParams.delete('lang');
    }
    window.history.pushState({tab: tabKey}, '', url);
}

function renderCurrentEntities() {
    const tab = Constants.tabs[state.tab];
    const entities = applyInMemoryEntityFilters(currentPageEntities);
    const totalEntities = entities.length;
    state.totalPages = Math.max(Math.ceil(totalEntities / Constants.pageSize), 1);
    if (state.page > state.totalPages) {
        state.page = state.totalPages;
    }

    const pageStart = (state.page - 1) * Constants.pageSize;
    const pageEntities = entities.slice(pageStart, pageStart + Constants.pageSize);
    renderTable(tab, pageEntities);
    statusEl.textContent = `${pageEntities.length} of ${totalEntities} entit${totalEntities === 1 ? 'y' : 'ies'}`;
    updateFilterSummary(entities.length);
    updatePager();
}

function updateMainAction() {
    const isAcademicSessions = state.tab === 'academicSessions';
    const isOrganizations = state.tab === 'organizations';
    const isPersons = state.tab === 'persons';
    mainActionButton.classList.toggle('visible', isAcademicSessions || isOrganizations || isPersons);
    mainActionButton.textContent = isPersons ? 'Add person' : isOrganizations ? 'Add organization' : 'Add academic session';
    layoutEl.classList.toggle('without-filters', isAcademicSessions || isOrganizations || isPersons);
}

function renderTable(tab, entities) {
    const renderId = ++tableRenderId;
    tableRegion.className = 'table-wrap';

    if (!entities.length) {
        tableRegion.className = 'empty';
        tableRegion.textContent = 'No entities found';
        return;
    }

    tableRegion.replaceChildren(tableElement(tab, entities));
    hydrateOfferingCounts(tab, entities, renderId);
}

function updatePager() {
    pagerEl.classList.toggle('hidden', state.totalPages <= 1);
    pageIndicator.textContent = `Page ${state.page} of ${state.totalPages}`;
    prevButton.disabled = state.page <= 1;
    nextButton.disabled = state.page >= state.totalPages;
}

function textValue(value) {
    if (Array.isArray(value)) {
        if (currentModalLanguage && isLanguageValueList(value)) {
            const selected = value.find(entry => entry.language === currentModalLanguage);
            if (selected?.value) {
                return selected.value;
            }
        }

        const preferred = value.find(entry => entry.language === 'en-GB')
            || value.find(entry => entry.language === 'nl-NL')
            || value[0];
        return preferred && preferred.value;
    }

    return value;
}

function textValueForLanguage(value, language) {
    if (Array.isArray(value) && isLanguageValueList(value)) {
        const selected = value.find(entry => entry.language === language);
        return selected ? selected.value : '';
    }

    return textValue(value);
}

function codeValue(value) {
    if (!value) {
        return '';
    }
    const code = document.createElement('span');
    code.className = 'code';
    code.textContent = typeof value === 'string' ? value : value?.code || '';
    return code;
}

function entityLinkValue(kind, entity) {
    const id = entityId(kind, entity);
    const name = entityDisplayName(kind, entity) || id || '';

    if (!id) {
        return name;
    }

    const button = document.createElement('button');
    button.className = 'course-name-button';
    button.type = 'button';
    button.dataset.entityKind = kind;
    button.dataset.entityId = id;
    button.textContent = name;
    return button;
}

function entityDisplayName(kind, entity) {
    return kind === 'person'
        ? entity?.displayName || [entity?.givenName, entity?.surname].filter(Boolean).join(' ')
        : textValue(entity?.name);
}

function entityMap(kind) {
    if (kind === 'program') {
        return programById;
    }
    if (kind === 'organization') {
        return organizationById;
    }
    if (kind === 'person') {
        return personById;
    }
    return courseById;
}

function organizationValue(value) {
    if (!value) {
        return '';
    }

    if (typeof value === 'string') {
        return value;
    }

    return textValue(value.name) || value.organizationId || '';
}

async function loadOrganizations() {
    if (organizationsLoadPromise) {
        return organizationsLoadPromise;
    }

    organizationsLoadPromise = (async () => {
        organizationsById.clear();
        const items = await loadAllEndpointItems('/organizations');
        items.forEach(organization => {
            const id = organizationIdValue(organization);
            if (id) {
                organizationsById.set(String(id), organization);
            }
        });
    })().catch(error => {
        organizationsLoadPromise = null;
        throw error;
    });

    return organizationsLoadPromise;
}

async function loadAcademicSessions() {
    if (academicSessionsLoadPromise) {
        return academicSessionsLoadPromise;
    }

    academicSessionsLoadPromise = (async () => {
        academicSessionsById.clear();
        const items = await loadAllEndpointItems('/academic-sessions');
        items.forEach(academicSession => {
            const id = academicSessionIdValue(academicSession);
            if (id) {
                academicSessionsById.set(String(id), academicSession);
            }
        });
    })().catch(error => {
        academicSessionsLoadPromise = null;
        throw error;
    });

    return academicSessionsLoadPromise;
}

async function loadPersons() {
    if (personsLoadPromise) {
        return personsLoadPromise;
    }

    personsLoadPromise = (async () => {
        personById.clear();
        const items = await loadAllEndpointItems('/persons');
        items.forEach(person => {
            const id = entityId('person', person);
            if (id) {
                personById.set(String(id), person);
            }
        });
    })().catch(error => {
        personsLoadPromise = null;
        throw error;
    });

    return personsLoadPromise;
}

function personLabel(person) {
    return entityDisplayName('person', person) || entityId('person', person) || '';
}

function personLabelById(id) {
    return personLabel(personById.get(String(id))) || String(id || '');
}

function academicSessionOptionEntries(selectedAcademicSessionId = '') {
    const academicSessions = [...academicSessionsById.values()]
        .sort((left, right) => academicSessionLabel(left).localeCompare(academicSessionLabel(right)));
    const selected = String(selectedAcademicSessionId || '');
    const hasSelected = selected && academicSessions.some(academicSession => String(academicSessionIdValue(academicSession)) === selected);
    const options = [
        ['', 'None'],
        ...academicSessions.map(academicSession => {
            const id = String(academicSessionIdValue(academicSession));
            return [id, academicSessionLabel(academicSession)];
        })
    ];

    if (selected && !hasSelected) {
        options.splice(1, 0, [selected, selected]);
    }

    return options;
}

function academicSessionLabel(academicSession, language = draftOffering.language || currentModalLanguage) {
    const name = academicSession?.name;
    if (Array.isArray(name) && name.length) {
        const exact = language ? name.find(entry => entry?.language === language) : null;
        const english = name.find(entry => entry?.language === 'en-GB')
            || name.find(entry => String(entry?.language || '').startsWith('en-'));
        return (exact?.value || english?.value || name[0]?.value || academicSessionIdValue(academicSession));
    }

    return typeof name === 'string' ? name : academicSessionIdValue(academicSession);
}

function organizationOptionEntries(selectedOrganizationId = '') {
    const organizations = [...organizationsById.values()]
        .sort((left, right) => organizationLabel(left).localeCompare(organizationLabel(right)));
    const selected = String(selectedOrganizationId || '');
    const hasSelected = selected && organizations.some(organization => String(organizationIdValue(organization)) === selected);
    const options = [
        ['', 'Select organization'],
        ...organizations.map(organization => {
            const id = String(organizationIdValue(organization));
            return [id, organizationLabel(organization)];
        })
    ];

    if (selected && !hasSelected) {
        options.splice(1, 0, [selected, selected]);
    }

    return options;
}

function organizationLabel(organization, language = draftOffering.language || currentModalLanguage) {
    const name = organization?.name;
    if (Array.isArray(name) && name.length) {
        const exact = language ? name.find(entry => entry?.language === language) : null;
        const english = name.find(entry => entry?.language === 'en-GB')
            || name.find(entry => String(entry?.language || '').startsWith('en-'));
        return (exact?.value || english?.value || name[0]?.value || organizationIdValue(organization));
    }

    return typeof name === 'string' ? name : organizationIdValue(organization);
}

async function openEntityModal(kind, entity, options = {}) {
    currentModalKind = kind;
    currentModalEntity = entity;
    currentModalOfferings = [];
    currentModalLanguages = collectLanguages(entity);
    currentModalLanguage = chooseLanguage(currentModalLanguages);
    const id = entityId(kind, entity);
    const name = entityDisplayName(kind, entity) || id || titleCase(kind);
    courseModal.title.textContent = name;
    entityEditButton.textContent = `Edit ${kind}`;
    entityEditButton.classList.remove('hidden');
    entityDeleteButton.classList.toggle('hidden', !['organization', 'person'].includes(kind));
    entityJsonPreviewButton.classList.add('hidden');
    const details = kind === 'organization'
        ? organizationDetailsHtml(entity)
        : kind === 'person'
            ? personDetailsHtml(entity)
            : entityDetailsHtml(kind, entity, {loadingOfferings: true});
    modalBody.replaceChildren(details);
    courseModal.backdrop.classList.add('open');
    document.body.style.overflow = 'hidden';
    courseModal.close.focus();

    if (options.pushHistory && id) {
        updateEntityUrl(kind, id, currentModalLanguage);
    }

    if (kind === 'organization' || kind === 'person') {
        return;
    }

    const offerings = await loadEntityOfferings(kind, id);
    currentModalOfferings = offerings;
    currentModalLanguages = collectLanguages({entity, offerings});
    if (!currentModalLanguage || !currentModalLanguages.includes(currentModalLanguage)) {
        currentModalLanguage = chooseLanguage(currentModalLanguages);
        updateEntityUrl(kind, id, currentModalLanguage, true);
    }
    modalBody.replaceChildren(entityDetailsHtml(currentModalKind, entity, {offerings}));
}

function closeEntityModal() {
    if (offeringModal.backdrop.classList.contains('open')) {
        closeAddOfferingModal({updateHistory: !suppressModalHistory});
    }
    courseModal.backdrop.classList.remove('open');
    entityEditButton.classList.add('hidden');
    entityDeleteButton.classList.add('hidden');
    entityJsonPreviewButton.classList.add('hidden');
    document.body.style.overflow = '';
    currentModalEntity = null;
    currentModalKind = 'course';
    currentModalOfferings = [];
    currentModalLanguages = [];
    currentModalLanguage = null;
    if (!suppressModalHistory && currentEntityFromUrl()) {
        clearEntityUrl();
    }
}

async function openEntityById(kind, id, options = {}) {
    const cached = entityMap(kind).get(String(id));
    if (cached) {
        openEntityModal(kind, cached, options);
        return;
    }

    try {
        const tabKey = kind === 'program'
            ? 'programs'
            : kind === 'organization'
                ? 'organizations'
                : kind === 'person' ? 'persons' : 'courses';
        const tab = Constants.tabs[tabKey];
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

function updateEntityUrl(kind, id, language, replace = false) {
    const url = new URL(window.location.href);
    const tab = kind === 'program' ? 'programs' : kind === 'organization' ? 'organizations' : kind === 'person' ? 'persons' : 'courses';
    const idParam = kind === 'program' ? 'programId' : kind === 'organization' ? 'organizationId' : kind === 'person' ? 'personId' : 'courseId';
    url.searchParams.set('tab', tab);
    url.searchParams.delete('section');
    url.searchParams.delete('action');
    ['courseId', 'programId', 'organizationId', 'personId'].forEach(param => {
        if (param !== idParam) {
            url.searchParams.delete(param);
        }
    });
    url.searchParams.set(idParam, id);
    if (language) {
        url.searchParams.set('lang', language);
    } else {
        url.searchParams.delete('lang');
    }
    const state = {kind, id, lang: language};
    if (replace) {
        window.history.replaceState(state, '', url);
    } else {
        window.history.pushState(state, '', url);
    }
}

async function requestErrorFromResponse(response) {
    const body = await response.text();
    let parsedBody = null;
    try {
        parsedBody = body ? JSON.parse(body) : null;
    } catch (error) {
        parsedBody = null;
    }

    const message = parsedBody?.message
        || parsedBody?.error
        || body
        || `Request failed with status ${response.status}`;
    const error = new Error(message);
    error.status = response.status;
    error.statusText = response.statusText;
    error.responseBody = body;
    error.responseJson = parsedBody;
    return error;
}

function openErrorDetailsModal(error, summary) {
    errorDetailsModal.subtitle.textContent = summary || 'Request failed';
    errorDetailsContent.textContent = errorDetailsText(error);
    errorDetailsModal.backdrop.classList.add('open');
    errorDetailsModal.close.focus();
}

function closeErrorDetailsModal() {
    errorDetailsModal.backdrop.classList.remove('open');
    errorDetailsModal.subtitle.textContent = '';
    errorDetailsContent.textContent = '';
}

function chooseLanguage(languages) {
    const fromUrl = currentLanguageFromUrl();
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

function localizeLanguageValues(value) {
    if (Array.isArray(value)) {
        const items = currentModalLanguage && isLanguageValueList(value)
            ? value.filter(item => item && item.language === currentModalLanguage)
            : value;
        return items.map(item => localizeLanguageValues(item));
    }

    if (!value || typeof value !== 'object') {
        return value;
    }

    return Object.fromEntries(
        Object.entries(value).map(([key, child]) => [key, localizeLanguageValues(child)])
    );
}


function initLoginModal() {
    const loginLink = document.getElementById('login-link');
    const logoutLink = document.getElementById('logout-link');
    const loginForm = document.getElementById('login-form');
    const loginClose = document.getElementById('login-modal-close');
    const loginBackdrop = document.getElementById('login-modal-backdrop');

    if (loginLink) {
        loginLink.addEventListener('click', () => {
            if (!isAuthenticated()) {
                showLoginModal();
            }
        });
    }

    if (logoutLink) {
        logoutLink.addEventListener('click', () => {
            if (window.confirm('Do you want to log out?')) {
                logout();
            }
        });
    }

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
            if (pointerDownOnBackdrop && event.target === loginBackdrop) {
                hideLoginModal();
            }
            pointerDownOnBackdrop = false;
        });
    }

    window.addEventListener('surfview:login', () => {
        loadOrganizations().catch(() => {
            // Organization dropdowns fall back to the current entity organization id.
        });
        loadCurrentPage();
    });

    updateLoginLink();
}

initLoginModal();

syncFiltersFromUrl();
selectTab(tabFromUrl(), {load: false});
loadOrganizations().catch(() => {
    // Organization dropdowns fall back to the current entity organization id.
});

loadCurrentPage().then(() => {
    const entity = currentEntityFromUrl();
    if (entity) {
        openEntityById(entity.kind, entity.id, {pushHistory: false});
    }
});
