
/** Reads the active tab from the current URL. */
function tabFromUrl() {
    const url = new URL(window.location.href);
    const tab = url.searchParams.get('tab') || url.searchParams.get('section');
    if (tab === 'users') {
        return 'users';
    }
    if (Constants.tabs[tab]) {
        return tab;
    }

    const entity = currentEntityFromUrl();
    const entityTab = entity && tabKeyByKind(entity.kind);
    if (entityTab) {
        return entityTab;
    }
    return 'courses';
}

/** Applies URL filter parameters to filter controls. */
function syncFiltersFromUrl() {
    const url = new URL(window.location.href);
    const levelParam = url.searchParams.get('educationLevel') || url.searchParams.get('levels') || '';
    const languageParam = url.searchParams.get('languageFilter') || '';
    const affiliationParam = url.searchParams.get('affiliation') || '';
    const activeEnrollmentParam = url.searchParams.get('activeEnrollment') || '';
    const allowedLevels = new Set(levelFilterInputs.map(input => normalizeFilterValue(input.dataset.levelFilter)));
    const allowedLanguages = new Set(SurfView.State.languageFilterInputs.map(input => normalizeFilterValue(input.dataset.languageFilter)));
    const allowedAffiliations = new Set(affiliationFilterInputs.map(input => normalizeFilterValue(input.dataset.affiliationFilter)));
    const allowedEnrollmentStates = new Set(activeEnrollmentFilterInputs.map(input => normalizeFilterValue(input.dataset.activeEnrollmentFilter)));
    const selectedLevels = levelParam
        .split(',')
        .map(normalizeFilterValue)
        .filter(level => allowedLevels.has(level));
    const selectedLanguages = languageParam
        .split(',')
        .map(normalizeFilterValue)
        .filter(language => allowedLanguages.has(language));
    const selectedAffiliations = affiliationParam.split(',').map(normalizeFilterValue)
        .filter(affiliation => allowedAffiliations.has(affiliation));
    const selectedEnrollmentStates = activeEnrollmentParam.split(',').map(normalizeFilterValue)
        .filter(state => allowedEnrollmentStates.has(state));

    levelFilterInputs.forEach(input => {
        input.checked = selectedLevels.includes(normalizeFilterValue(input.dataset.levelFilter));
    });
    SurfView.State.languageFilterInputs.forEach(input => {
        input.checked = selectedLanguages.includes(normalizeFilterValue(input.dataset.languageFilter));
    });
    affiliationFilterInputs.forEach(input => {
        input.checked = selectedAffiliations.includes(normalizeFilterValue(input.dataset.affiliationFilter));
    });
    activeEnrollmentFilterInputs.forEach(input => {
        input.checked = selectedEnrollmentStates.includes(normalizeFilterValue(input.dataset.activeEnrollmentFilter));
    });
}

/** Reads the deep-linked entity from the current URL. */
function currentEntityFromUrl() {
    const url = new URL(window.location.href);
    const tabKey = url.searchParams.get('tab') || url.searchParams.get('section');
    const tab = Constants.tabs[tabKey];
    const tabId = tab && url.searchParams.get(tab.idField);
    if (tabId) {
        return {kind: tab.kind, id: tabId};
    }

    for (const tab of Object.values(Constants.tabs)) {
        const id = url.searchParams.get(tab.idField);
        if (id) {
            return {kind: tab.kind, id};
        }
    }
    return null;
}

/** Removes entity parameters from the current URL. */
function clearEntityUrl() {
    const url = new URL(window.location.href);
    entityIdParams().forEach(param => url.searchParams.delete(param));
    url.searchParams.delete('lang');
    url.searchParams.delete('action');
    window.history.pushState({}, '', url);
}

/** Writes entity modal state to the current URL. */
function updateEntityUrl(kind, id, language, replace = false) {
    const tabKey = tabKeyByKind(kind);
    const tab = tabByKind(kind);
    if (!tabKey || !tab) {
        return;
    }

    const url = new URL(window.location.href);
    url.searchParams.set('tab', tabKey);
    url.searchParams.delete('section');
    url.searchParams.delete('action');
    entityIdParams().forEach(param => {
        if (param !== tab.idField) {
            url.searchParams.delete(param);
        }
    });
    url.searchParams.set(tab.idField, id);
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
