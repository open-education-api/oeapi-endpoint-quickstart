/** Filters localized items to the active modal language. */
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

/** Filters a localized value to the active modal language. */
function filterLanguageValue(value) {
    if (Array.isArray(value)) {
        if (SurfView.State.currentModalLanguage && isLanguageValueList(value)) {
            return value.filter(item => item && item.language === SurfView.State.currentModalLanguage);
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
        return !SurfView.State.currentModalLanguage || value.language === SurfView.State.currentModalLanguage ? value : null;
    }

    return value;
}
