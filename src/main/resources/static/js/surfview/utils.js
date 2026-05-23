function normalizeFilterValue(value) {
    return String(value || '').trim().toLowerCase();
}

function normalizeTeachingLanguages(value) {
    if (Array.isArray(value)) {
        return value.flatMap(normalizeTeachingLanguages);
    }

    if (!value) {
        return [];
    }

    return [normalizeFilterValue(value)];
}

function isLanguageValueList(value) {
    return value.some(entry => entry && typeof entry === 'object' && 'language' in entry && 'value' in entry);
}

function cssEscape(value) {
    if (window.CSS?.escape) {
        return CSS.escape(String(value));
    }

    return String(value).replace(/["\\]/g, '\\$&');
}

function entityId(kind, entity) {
    if (kind === 'program') {
        return entity?.programId;
    }
    if (kind === 'organization') {
        return entity?.organizationId;
    }
    if (kind === 'person') {
        return entity?.personId;
    }
    return entity?.courseId;
}

function titleCase(value) {
    return value.charAt(0).toUpperCase() + value.slice(1);
}

function organizationIdValue(value) {
    if (!value) {
        return '';
    }

    if (typeof value === 'string') {
        return value;
    }

    return value.organizationId || '';
}

function academicSessionIdValue(value) {
    if (!value) {
        return '';
    }

    if (typeof value === 'string') {
        return value;
    }

    return value.academicSessionId || value.academic_session_id || '';
}

function studyLoadValue(value) {
    if (!value) {
        return '';
    }

    if (typeof value === 'string' || typeof value === 'number') {
        return value;
    }

    const amount = value.value || value.amount || '';
    const unit = value.studyLoadUnit || value.unit || '';
    return [amount, unit].filter(Boolean).join(' ');
}

function authorizationHeader() {
    const token = window.localStorage.getItem('jwt');
    return token ? {'Authorization': `Bearer ${token}`} : {};
}

function generateId() {
    if (window.crypto?.randomUUID) {
        return crypto.randomUUID();
    }

    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, char => {
        const random = Math.random() * 16 | 0;
        const value = char === 'x' ? random : (random & 0x3 | 0x8);
        return value.toString(16);
    });
}

function languageValue(value, language = 'en-GB') {
    return value ? [{language, value}] : [];
}

function removeEmptyPayloadValues(value) {
    if (Array.isArray(value)) {
        return value
            .map(removeEmptyPayloadValues)
            .filter(item => {
                if (Array.isArray(item)) {
                    return item.length > 0;
                }
                if (item && typeof item === 'object') {
                    return Object.keys(item).length > 0;
                }
                return item !== '' && item !== null && item !== undefined;
            });
    }

    if (!value || typeof value !== 'object') {
        return value;
    }

    return Object.fromEntries(Object.entries(value)
        .map(([key, child]) => [key, removeEmptyPayloadValues(child)])
        .filter(([, child]) => {
            if (Array.isArray(child)) {
                return child.length > 0;
            }
            if (child && typeof child === 'object') {
                return Object.keys(child).length > 0;
            }
            return child !== '' && child !== null && child !== undefined;
        }));
}

function collectLanguages(value, languages = new Set()) {
    if (Array.isArray(value)) {
        value.forEach(item => collectLanguages(item, languages));
        return [...languages].sort((a, b) => languageSortLabel(a).localeCompare(languageSortLabel(b)));
    }

    if (!value || typeof value !== 'object') {
        return [...languages].sort((a, b) => languageSortLabel(a).localeCompare(languageSortLabel(b)));
    }

    if (typeof value.language === 'string' && 'value' in value) {
        languages.add(value.language);
    }

    Object.values(value).forEach(child => collectLanguages(child, languages));
    return [...languages].sort((a, b) => languageSortLabel(a).localeCompare(languageSortLabel(b)));
}

function languageSortLabel(code) {
    return `${languageLabel(code)} ${code}`;
}

function languageLabel(code) {
    return Constants.languageNames[code] || code;
}

function plainCodeValue(value) {
    if (!value) {
        return '';
    }

    if (typeof value === 'string') {
        return value;
    }

    return value.code || '';
}

function listValue(value) {
    if (Array.isArray(value)) {
        return value.join(', ');
    }

    return value || '';
}
