function applyInMemoryEntityFilters(entities) {
    const tab = Constants.tabs[state.tab];
    const searchQuery = normalizeFilterValue(searchInput.value);
    const selectedLevels = selectedLevelFilters();
    const selectedLanguages = selectedLanguageFilters();
    const useEducationFilters = activeTabUsesFilters();

    return entities.filter(entity => {
        const searchMatches = !searchQuery || entityMatchesSearch(tab, entity, searchQuery);
        const levelMatches = !useEducationFilters || !selectedLevels.length || entityMatchesSelectedLevel(entity, selectedLevels);
        const languageMatches = !useEducationFilters || !selectedLanguages.length || entityMatchesSelectedLanguage(entity, selectedLanguages);
        return searchMatches && levelMatches && languageMatches;
    });
}

function entityMatchesSearch(tab, entity, searchQuery) {
    return entitySearchValues(tab, entity)
        .some(value => normalizeFilterValue(value).includes(searchQuery));
}

function entitySearchValues(tab, entity) {
    if (tab.kind === 'person') {
        return [
            entity.displayName,
            [entity.givenName, entity.surname].filter(Boolean).join(' ')
        ].filter(Boolean);
    }

    return localizedTextValues(entity.name);
}

function localizedTextValues(value) {
    if (Array.isArray(value)) {
        return value
            .filter(item => item && typeof item === 'object' && 'value' in item)
            .map(item => item.value)
            .filter(Boolean);
    }

    return value ? [value] : [];
}

function selectedLevelFilters() {
    return levelFilterInputs
        .filter(input => input.checked)
        .map(input => normalizeFilterValue(input.dataset.levelFilter));
}

function selectedLanguageFilters() {
    return languageFilterInputs
        .filter(input => input.checked)
        .map(input => normalizeFilterValue(input.dataset.languageFilter));
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
    const normalizedLanguageCodes = languageCodes.map(normalizeFilterValue);
    const teachingLanguageCodes = normalizeTeachingLanguages(entity.teachingLanguage);

    Constants.supportedLanguages.forEach((language, key) => {
        const matchesLocalizedContent = language.languagePrefixes.some(prefix =>
            normalizedLanguageCodes.some(code => code.startsWith(prefix))
        );
        const matchesTeachingLanguage = language.teachingLanguageCodes.some(code =>
            teachingLanguageCodes.includes(code)
        );
        if (matchesLocalizedContent || matchesTeachingLanguage) {
            languages.add(key);
        }
    });

    if (languageCodes.length > 1 || languages.size > 1) {
        languages.add('multiple');
    }

    return languages;
}

function activeTabUsesFilters() {
    return ['course', 'program'].includes(Constants.tabs[state.tab]?.kind);
}

function updateFilterUrl(replace = false) {
    const url = new URL(window.location.href);
    const selectedLevels = selectedLevelFilters();
    const selectedLanguages = selectedLanguageFilters();

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

    if (replace) {
        window.history.replaceState({}, '', url);
    } else {
        window.history.pushState({}, '', url);
    }
}

function updateFilterSummary(visibleCount) {
    const tab = Constants.tabs[state.tab];
    const searchQuery = searchInput.value.trim();
    if (!activeTabUsesFilters()) {
        filterSummary.textContent = `Showing ${visibleCount} ${tab.noun}${searchQuery ? ` matching "${searchQuery}"` : ''}`;
        return;
    }

    const selectedLevels = selectedLevelFilters();
    const selectedLanguages = selectedLanguageFilters();
    const filterParts = [];
    if (selectedLevels.length) {
        filterParts.push(selectedLevels.join(', '));
    }
    if (selectedLanguages.length) {
        filterParts.push(selectedLanguages.join(', '));
    }
    if (searchQuery) {
        filterParts.push(`"${searchQuery}"`);
    }
    const filterText = filterParts.length ? ` matching ${filterParts.join('; ')}` : '';
    filterSummary.textContent = `Showing ${visibleCount} ${tab.noun}${filterText}`;
}


function filterLanguageItems(value) {
    if (!Array.isArray(value)) {
        return [];
    }

    return value
        .map(item => filterLanguageValue(item))
        .filter(item => {
            if (Array.isArray(item)) {
                return item.length > 0;
            }

            return item !== null && item !== undefined && item !== '';
        });
}

function filterLanguageValue(value) {
    if (Array.isArray(value)) {
        if (currentModalLanguage && isLanguageValueList(value)) {
            return value.filter(item => item && item.language === currentModalLanguage);
        }

        return value
            .map(item => filterLanguageValue(item))
            .filter(item => {
                if (Array.isArray(item)) {
                    return item.length > 0;
                }

                return item !== null && item !== undefined && item !== '';
            });
    }

    if (value && typeof value === 'object' && 'language' in value && 'value' in value) {
        return !currentModalLanguage || value.language === currentModalLanguage ? value : null;
    }

    return value;
}
