
function tabFromUrl() {
    const url = new URL(window.location.href);
    const tab = url.searchParams.get('tab') || url.searchParams.get('section');
    if (Constants.tabs[tab]) {
        return tab;
    }

    const entity = currentEntityFromUrl();
    if (entity?.kind === 'program') {
        return 'programs';
    }
    if (entity?.kind === 'course') {
        return 'courses';
    }
    if (entity?.kind === 'organization') {
        return 'organizations';
    }
    if (entity?.kind === 'person') {
        return 'persons';
    }
    return 'courses';
}

function syncFiltersFromUrl() {
    const url = new URL(window.location.href);
    const levelParam = url.searchParams.get('educationLevel') || url.searchParams.get('levels') || '';
    const languageParam = url.searchParams.get('languageFilter') || '';
    const allowedLevels = new Set(levelFilterInputs.map(input => normalizeFilterValue(input.dataset.levelFilter)));
    const allowedLanguages = new Set(languageFilterInputs.map(input => normalizeFilterValue(input.dataset.languageFilter)));
    const selectedLevels = levelParam
        .split(',')
        .map(normalizeFilterValue)
        .filter(level => allowedLevels.has(level));
    const selectedLanguages = languageParam
        .split(',')
        .map(normalizeFilterValue)
        .filter(language => allowedLanguages.has(language));

    levelFilterInputs.forEach(input => {
        input.checked = selectedLevels.includes(normalizeFilterValue(input.dataset.levelFilter));
    });
    languageFilterInputs.forEach(input => {
        input.checked = selectedLanguages.includes(normalizeFilterValue(input.dataset.languageFilter));
    });
}

function currentCourseIdFromUrl() {
    return new URL(window.location.href).searchParams.get('courseId');
}

function currentEntityFromUrl() {
    const url = new URL(window.location.href);
    const courseId = url.searchParams.get('courseId');
    const programId = url.searchParams.get('programId');
    const organizationId = url.searchParams.get('organizationId');
    const personId = url.searchParams.get('personId');
    if (courseId) {
        return {kind: 'course', id: courseId};
    }
    if (programId) {
        return {kind: 'program', id: programId};
    }
    if (organizationId) {
        return {kind: 'organization', id: organizationId};
    }
    if (personId) {
        return {kind: 'person', id: personId};
    }
    return null;
}

function currentLanguageFromUrl() {
    return new URL(window.location.href).searchParams.get('lang');
}

function clearEntityUrl() {
    const url = new URL(window.location.href);
    url.searchParams.delete('courseId');
    url.searchParams.delete('programId');
    url.searchParams.delete('organizationId');
    url.searchParams.delete('personId');
    url.searchParams.delete('lang');
    url.searchParams.delete('action');
    window.history.pushState({}, '', url);
}
