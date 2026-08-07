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
const moduleModal = modalElements(document.getElementById('module-modal-backdrop'));
const entityTitleActions = document.getElementById('entity-title-actions');
const entityModalActions = document.getElementById('entity-modal-actions');
const modalBody = document.getElementById('module-modal-body');
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
const entityMaps = Object.fromEntries(Object.keys(Constants.tabs).map(tabKey => [tabKey, new Map()]));
const courseById = entityMaps.courses;
const organizationsById = entityMaps.organizations;
const personById = entityMaps.persons;
const academicSessionsById = entityMaps.academicSessions;
const levelFilterInputs = [...document.querySelectorAll('[data-level-filter]')];
const affiliationFilterInputs = [...document.querySelectorAll('[data-affiliation-filter]')];
const activeEnrollmentFilterInputs = [...document.querySelectorAll('[data-active-enrollment-filter]')];
const educationFilterGroups = [...document.querySelectorAll('[data-education-filter-group]')];
const personFilterGroups = [...document.querySelectorAll('[data-person-filter-group]')];
const languageFilterOptions = document.getElementById('language-filter-options');
const tabButtons = [...document.querySelectorAll('.tab-button')];
let organizationsLoadPromise = null;
let academicSessionsLoadPromise = null;
let personsLoadPromise = null;

window.SurfView = window.SurfView || {};

SurfView.State = {
    tab: 'courses',
    page: 1,
    totalPages: 1,
    languageFilterInputs: [...document.querySelectorAll('[data-language-filter]')],
    suppressModalHistory: false,
    currentModalEntity: null,
    currentModalKind: 'course',
    currentModalOfferings: [],
    currentModalLanguages: [],
    currentModalLanguage: null,
    currentModalMode: 'closed',
    currentEntityDeletePending: false,
    currentPageEntities: [],
    draftOffering: initialDraftOffering(),
    draftEntity: initialDraftEntity()
};

function initialDraftOffering() {
    return {
        addresses: [],
        priceInformation: [],
        language: 'en-GB',
        languages: [],
        languageValues: {},
        nestedPriceLanguage: 'en-GB',
        nestedPriceLanguageValues: {}
    };
}

/** Resets the offering draft state. */
function resetDraftOffering() {
    SurfView.State.draftOffering = initialDraftOffering();
}

function initialDraftEntity() {
    return {
        addresses: [],
        language: 'en-GB',
        languages: [],
        languageValues: {}
    };
}

/** Resets the entity draft state. */
function resetDraftEntity() {
    SurfView.State.draftEntity = initialDraftEntity();
}

/** Resolves standard modal element references. */
function modalElements(backdrop) {
    return {
        backdrop,
        dialog: backdrop.querySelector('[role="dialog"]'),
        title: backdrop.querySelector('.modal-title'),
        subtitle: backdrop.querySelector('.modal-subtitle'),
        close: backdrop.querySelector('.modal-close')
    };
}

/** Loads the current page and opens a deep link. */
async function loadCurrentPageAndEntity() {
    await loadCurrentPage();
    const entity = currentEntityFromUrl();
    if (entity) {
        openEntityById(entity.kind, entity.id, {pushHistory: false});
    }
}

/** Starts SURFView after auth state is known. */
async function startSurfView() {
    initLoginModal();
    SurfView.UserAdmin.init();

    syncFiltersFromUrl();
    const initialTab = tabFromUrl();
    selectTab(initialTab === 'users' ? 'courses' : initialTab, {load: false});
    await loadAuthStatus();
    updateWriteControls();
    if (privateLoginRequired()) {
        return;
    }
    loadOrganizations().catch(() => {
        // Organization dropdowns fall back to the current entity organization id.
    });

    if (initialTab === 'users' && SurfView.UserAdmin.isAvailable()) {
        if (document.getElementById('user-admin-view')?.classList.contains('hidden')) {
            await SurfView.UserAdmin.open({updateHistory: false});
        }
        return;
    }

    await loadCurrentPageAndEntity();
}

startSurfView();
