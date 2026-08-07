/** Applies active client-side filters to entities. */
function applyInMemoryEntityFilters(entities) {
    const tab = Constants.tabs[SurfView.State.tab];
    const searchQuery = normalizeFilterValue(searchInput.value);
    const selectedLevels = selectedLevelFilters();
    const selectedLanguages = selectedLanguageFilters();
    const selectedAffiliations = selectedAffiliationFilters();
    const selectedEnrollmentStates = selectedActiveEnrollmentFilters();
    const useEducationFilters = activeTabUsesEducationFilters();
    const usePersonFilters = activeTabUsesPersonFilters();

    return entities.filter(entity => {
        const searchMatches = !searchQuery || entityMatchesSearch(tab, entity, searchQuery);
        const levelMatches = !useEducationFilters || !selectedLevels.length || entityMatchesSelectedLevel(entity, selectedLevels);
        const languageMatches = !useEducationFilters || !selectedLanguages.length || entityMatchesSelectedLanguage(entity, selectedLanguages);
        const affiliationMatches = !usePersonFilters || !selectedAffiliations.length || entityMatchesSelectedAffiliation(entity, selectedAffiliations);
        const enrollmentMatches = !usePersonFilters || !selectedEnrollmentStates.length || selectedEnrollmentStates.includes(String(entity.activeEnrollment === true));
        return searchMatches && levelMatches && languageMatches && affiliationMatches && enrollmentMatches;
    });
}

function entityMatchesSearch(tab, entity, searchQuery) {
    return entitySearchValues(tab, entity)
        .some(value => normalizeFilterValue(value).includes(searchQuery));
}

function entitySearchValues(tab, entity) {
    // Only person is not localized
    if (tab.kind === 'person') {
        const fullName = [entity.givenName, entity.surname].filter(Boolean).join(' ')
        return [entity.displayName, fullName ].filter(Boolean);
    } else if (Array.isArray(entity.name)) {
        return entity.name
            .filter(item => item && typeof item === 'object' && 'value' in item)
            .map(item => item.value)
            .filter(Boolean);
    } else {
        return entity.name ? [entity.name] : [];
    }
}

function selectedLevelFilters() {
    return selectedCheckboxFilters(levelFilterInputs, 'levelFilter');
}

function selectedLanguageFilters() {
    return selectedCheckboxFilters(SurfView.State.languageFilterInputs, 'languageFilter');
}

function selectedCheckboxFilters(inputs, dataKey) {
    return inputs
        .filter(input => input.checked)
        .map(input => normalizeFilterValue(input.dataset[dataKey]));
}

function selectedAffiliationFilters() {
    return selectedCheckboxFilters(affiliationFilterInputs, 'affiliationFilter');
}

function selectedActiveEnrollmentFilters() {
    return selectedCheckboxFilters(activeEnrollmentFilterInputs, 'activeEnrollmentFilter');
}

function entityMatchesSelectedAffiliation(entity, selectedAffiliations) {
    const affiliations = new Set((Array.isArray(entity.affiliations) ? entity.affiliations : [])
        .map(normalizeFilterValue));
    return selectedAffiliations.some(affiliation => affiliations.has(affiliation));
}

function entityMatchesSelectedLevel(entity, selectedLevels) {
    const entityLevel = normalizeFilterValue(entity.level || entity.educationSpecificationType);
    return selectedLevels.includes(entityLevel);
}

function entityMatchesSelectedLanguage(entity, selectedLanguages) {
    const entityLanguages = entityLanguageFilters(entity);
    return selectedLanguages.some(language => entityLanguages.has(language));
}

function entityLanguageFilters(entity) {
    const languages = new Set();
    const languageCodes = collectLanguages(entity);
    languageCodes.forEach(language => languages.add(normalizeFilterValue(language)));

    if (languageCodes.length > 1) {
        languages.add('multiple');
    }

    return languages;
}

/** Returns whether the active tab shows filters. */
function activeTabUsesEducationFilters() {
    return ['course', 'program'].includes(Constants.tabs[SurfView.State.tab]?.kind);
}

function activeTabUsesPersonFilters() {
    return Constants.tabs[SurfView.State.tab]?.kind === 'person';
}

function activeTabUsesFilters() {
    return activeTabUsesEducationFilters() || activeTabUsesPersonFilters();
}

/** Writes active filters to the URL. */
function updateFilterUrl(replace = false) {
    const url = new URL(window.location.href);
    const selectedLevels = selectedLevelFilters();
    const selectedLanguages = selectedLanguageFilters();
    const selectedAffiliations = selectedAffiliationFilters();
    const selectedEnrollmentStates = selectedActiveEnrollmentFilters();

    if (selectedLevels.length) {
        url.searchParams.set('educationLevel', selectedLevels.join(','));
    } else {
        url.searchParams.delete('educationLevel');
    }
    if (selectedLanguages.length) {
        url.searchParams.set('languageFilter', selectedLanguages.join(','));
    } else {
        url.searchParams.delete('languageFilter');
    }
    url.searchParams.delete('levels');
    if (selectedAffiliations.length) {
        url.searchParams.set('affiliation', selectedAffiliations.join(','));
    } else {
        url.searchParams.delete('affiliation');
    }
    if (selectedEnrollmentStates.length) {
        url.searchParams.set('activeEnrollment', selectedEnrollmentStates.join(','));
    } else {
        url.searchParams.delete('activeEnrollment');
    }

    if (replace) {
        window.history.replaceState({}, '', url);
    } else {
        window.history.pushState({}, '', url);
    }
}

/** Updates the visible filter summary text. */
function updateFilterSummary(visibleCount) {
    const tab = Constants.tabs[SurfView.State.tab];
    const searchQuery = searchInput.value.trim();
    if (!activeTabUsesFilters()) {
        filterSummary.textContent = `Showing ${visibleCount} ${tab.noun}${searchQuery ? ` matching "${searchQuery}"` : ''}`;
        return;
    }

    const selectedLevels = selectedLevelFilters();
    const selectedLanguages = selectedLanguageFilters();
    const selectedAffiliations = selectedAffiliationFilters();
    const selectedEnrollmentStates = selectedActiveEnrollmentFilters();
    const filterParts = [];
    if (activeTabUsesEducationFilters() && selectedLevels.length) {
        filterParts.push(selectedLevels.join(', '));
    }
    if (activeTabUsesEducationFilters() && selectedLanguages.length) {
        filterParts.push(selectedLanguages.join(', '));
    }
    if (activeTabUsesPersonFilters() && selectedAffiliations.length) {
        filterParts.push(selectedAffiliations.join(', '));
    }
    if (activeTabUsesPersonFilters() && selectedEnrollmentStates.length) {
        filterParts.push(`active enrollment: ${selectedEnrollmentStates.map(value => value === 'true' ? 'yes' : 'no').join(', ')}`);
    }
    if (searchQuery) {
        filterParts.push(`"${searchQuery}"`);
    }
    const filterText = filterParts.length ? ` matching ${filterParts.join('; ')}` : '';
    filterSummary.textContent = `Showing ${visibleCount} ${tab.noun}${filterText}`;
}

function filterLanguageLabel(language) {
    const label = languageLabel(language);
    return label === language ? language : `${label} (${language})`;
}

/** Renders language filter options for entities. */
function renderLanguageFilters(entities) {
    if (!languageFilterOptions) {
        return;
    }

    const selectedLanguages = new Set(selectedLanguageFilters());
    const languageCodes = new Set();
    entities.forEach(entity => {
        collectLanguages(entity).forEach(language => languageCodes.add(language));
    });

    const options = [...languageCodes]
        .sort((a, b) => languageSortLabel(a).localeCompare(languageSortLabel(b)))
        .map(language => [normalizeFilterValue(language), filterLanguageLabel(language)]);

    options.push(['multiple', 'Multiple languages']);
    languageFilterOptions.replaceChildren(...options.map(([value, label]) => languageFilterRow(value, label, selectedLanguages.has(value))));
    /** Refreshes cached language filter inputs. */
    SurfView.State.languageFilterInputs = [...document.querySelectorAll('[data-language-filter]')];
}

function languageFilterRow(value, label, checked = false) {
    const row = createElement('label.checkbox-row');
    const input = createElement('input', {
        dataset: {languageFilter: value},
        attributes: {type: 'checkbox'}
    });
    input.checked = checked;

    row.append(input, document.createTextNode(` ${label}`));
    return row;
}
