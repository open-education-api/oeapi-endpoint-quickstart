/** Normalizes filter text for comparisons. */
function normalizeFilterValue(value) {
    return String(value || '').trim().toLowerCase();
}

function isLanguageValueList(value) {
    return value.some(entry => entry && typeof entry === 'object' && 'language' in entry && 'value' in entry);
}

/** Returns the tab key for an entity kind. */
function tabKeyByKind(kind) {
    return Object.entries(Constants.tabs).find(([, tab]) => tab.kind === kind)?.[0] || null;
}

/** Returns tab metadata for an entity kind. */
function tabByKind(kind) {
    const tabKey = tabKeyByKind(kind);
    return tabKey ? Constants.tabs[tabKey] : null;
}

function entityIdParams() {
    return Object.values(Constants.tabs).map(tab => tab.idField).filter(Boolean);
}

/** Returns the canonical id for an entity. */
function entityId(kind, entity) {
    if (!entity) {
        return '';
    }

    const idField = tabByKind(kind)?.idField;
    return idField ? entity[idField] || (idField === 'academicSessionId' ? entity.academic_session_id : '') : '';
}

/** Capitalizes a simple label. */
function titleCase(value) {
    return value.charAt(0).toUpperCase() + value.slice(1);
}

/** Returns an organization id from a reference. */
function organizationIdValue(value) {
    if (!value) {
        return '';
    }

    if (typeof value === 'string') {
        return value;
    }

    return value.organizationId || '';
}

/** Returns an academic session id from a reference. */
function academicSessionIdValue(value) {
    if (!value) {
        return '';
    }

    if (typeof value === 'string') {
        return value;
    }

    return value.academicSessionId || value.academic_session_id || '';
}

/** Formats a study load value. */
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

/** Generates a random entity id. */
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

/** Removes empty values from an API payload. */
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

/** Collects language codes from nested values. */
function collectLanguages(value, languages = new Set()) {
    if (Array.isArray(value)) {
        value.forEach(item => collectLanguages(item, languages));
        return [...languages].sort((a, b) => languageSortLabel(a).localeCompare(languageSortLabel(b)));
    }

    if (!value || typeof value !== 'object') {
        return [...languages].sort((a, b) => languageSortLabel(a).localeCompare(languageSortLabel(b)));
    }

    if (isKnownLanguageCode(value.language) && 'value' in value) {
        languages.add(value.language);
    }

    Object.values(value).forEach(child => collectLanguages(child, languages));
    return [...languages].sort((a, b) => languageSortLabel(a).localeCompare(languageSortLabel(b)));
}

function isKnownLanguageCode(code) {
    return typeof code === 'string' && code in Constants.languageNames;
}

function languageSortLabel(code) {
    return `${languageLabel(code)} ${code}`;
}

/** Returns a human-readable language label. */
function languageLabel(code) {
    return Constants.languageNames[code] || code;
}

/** Returns a localized field value. */
function localizedFieldValue(value, language) {
    if (typeof value === 'string') {
        return value;
    }

    return flattenItems(Array.isArray(value) ? value : [])
        .find(item => item?.language === language)?.value || '';
}

function flattenItems(items) {
    return items.flatMap(item => Array.isArray(item) ? flattenItems(item) : [item]);
}

/** Parses a saved original item index. */
function originalIndex(value) {
    if (value === '') {
        return null;
    }

    const index = Number(value);
    return Number.isInteger(index) && index >= 0 ? index : null;
}

/** Returns a localized entity name label. */
function localizedNameLabel(entity, idValue, language = SurfView.State.draftOffering.language || SurfView.State.currentModalLanguage) {
    const name = entity?.name;
    if (Array.isArray(name) && name.length) {
        const exact = language ? name.find(entry => entry?.language === language) : null;
        const english = name.find(entry => entry?.language === 'en-GB')
            || name.find(entry => String(entry?.language || '').startsWith('en-'));
        return exact?.value || english?.value || name[0]?.value || idValue(entity);
    }

    return typeof name === 'string' ? name : idValue(entity);
}

/** Builds sorted select option entries for entities. */
function entityOptionEntries({items, selectedId = '', emptyLabel, label, idValue}) {
    const sortedItems = [...items].sort((left, right) => label(left).localeCompare(label(right)));
    const selected = String(selectedId || '');
    const hasSelected = selected && sortedItems.some(item => String(idValue(item)) === selected);
    const options = [
        ['', emptyLabel],
        ...sortedItems.map(item => [String(idValue(item)), label(item)])
    ];

    if (selected && !hasSelected) {
        options.splice(1, 0, [selected, UNKNOWN_REFERENCE_LABEL]);
    }

    return options;
}

/** Returns plain text for a code value. */
function plainCodeValue(value) {
    if (!value) {
        return '';
    }

    if (typeof value === 'string') {
        return value;
    }

    return value.code || '';
}

/** Formats a list value for display. */
function listValue(value) {
    if (Array.isArray(value)) {
        return value.join(', ');
    }

    return value || '';
}

/** Returns a short error summary. */
function errorSummary(error, fallbackMessage) {
    const responseJson = error?.responseJson;
    if (responseJson?.status || responseJson?.error || responseJson?.message) {
        return [
            responseJson.status,
            responseJson.error,
            responseJson.message
        ].filter(Boolean).join(' - ');
    }
    return error?.message || fallbackMessage;
}

/** Returns detailed error text. */
function errorDetailsText(error) {
    if (error?.responseJson?.trace) {
        return error.responseJson.trace;
    }
    if (error?.responseJson) {
        return JSON.stringify(error.responseJson, null, 2);
    }
    return error?.responseBody || error?.stack || error?.message || '';
}

/** Builds an Error from a failed response. */
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

/** Parses an optional JSON object response. */
async function parseOptionalJsonObject(response) {
    const body = await response.text();
    if (!body) {
        return {};
    }

    try {
        const parsed = JSON.parse(body);
        return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
    } catch (error) {
        return {};
    }
}

function preserveArrayFields(entity, payload, fields) {
    fields.forEach(field => {
        if (!Array.isArray(entity[field]) || (!entity[field].length && Array.isArray(payload[field]) && payload[field].length)) {
            entity[field] = payload[field];
        }
    });
}

/** Normalizes primary code data for forms. */
function primaryCodeParts(primaryCode, fallbackCode = '', defaultCodeType = 'identifier') {
    if (!primaryCode) {
        return {
            codeType: defaultCodeType,
            code: fallbackCode
        };
    }

    if (typeof primaryCode === 'string') {
        return {
            codeType: defaultCodeType,
            code: primaryCode
        };
    }

    return {
        codeType: primaryCode.codeType || defaultCodeType,
        code: primaryCode.code || fallbackCode
    };
}

/** Builds the shared read-only id and primary-code form fields. */
function primaryCodeFormFields(config) {
    const {
        idLabel,
        idName,
        idValue,
        primaryCode,
        defaultCodeType = 'identifier'
    } = config;
    const code = primaryCodeParts(primaryCode, idValue, defaultCodeType);

    return [
        fieldLabelHtml(idLabel, 'text', idName, idValue || generateId(), {required: true, readonly: true}, 'full-width'),
        fieldLabelHtml('Primary code type', 'datalist', 'primaryCodeType', code.codeType, {
            required: true,
            options: Constants.enumOptions.codeType
        }),
        fieldLabelHtml('Primary code', 'text', 'primaryCode', code.code, {required: true})
    ];
}

/** Reads the shared primary-code payload pair from form data. */
function primaryCodePayloadFromFormData(formData) {
    const value = name => String(formData.get(name) || '').trim();
    return {
        codeType: value('primaryCodeType'),
        code: value('primaryCode')
    };
}

/** Opens the shared JSON preview modal for a generated payload. */
function openJsonPreview(title, subtitle, buildPayload, options = {}) {
    const {status, errorMessage = 'Unable to preview JSON'} = options;

    status?.classList.remove('error');
    status?.replaceChildren();

    try {
        const payload = buildPayload();
        if (!payload) {
            return;
        }

        jsonPreviewModal.title.textContent = title;
        jsonPreviewModal.subtitle.textContent = subtitle;
        jsonPreviewContent.textContent = JSON.stringify(payload, null, 2);
        openManagedModal(jsonPreviewModal);
    } catch (error) {
        if (status) {
            SurfView.Offerings.renderOfferingFormError(status, error, errorMessage);
        }
    }
}

/** Closes the shared JSON preview modal. */
function closeJsonPreview() {
    closeManagedModal(jsonPreviewModal);
    jsonPreviewContent.textContent = '';
}

/** Submits an entity payload with shared error handling. */
async function submitEntity(config) {
    const {
        status,
        submitButton,
        payload,
        endpoint,
        method,
        savingText = 'Saving...',
        successText = '',
        preserveArrayFields: arrayFields = [],
        onSuccess,
        onError,
        errorMessage
    } = config;

    status.classList.remove('error');
    status.replaceChildren();

    try {
        const requestPayload = typeof payload === 'function' ? payload() : payload;
        if (!requestPayload) {
            return null;
        }

        status.textContent = savingText;
        submitButton.disabled = true;
        const requestEndpoint = typeof endpoint === 'function' ? endpoint(requestPayload) : endpoint;
        const requestMethod = typeof method === 'function' ? method(requestPayload) : method;
        const response = await callEndpoint(requestEndpoint, {
            method: requestMethod,
            headers: {'Accept': 'application/json', 'Content-Type': 'application/json'},
            body: JSON.stringify(requestPayload)
        });

        if (!response.ok) {
            throw await requestErrorFromResponse(response);
        }

        const responseEntity = await parseOptionalJsonObject(response);
        const updatedEntity = {...requestPayload, ...responseEntity};
        preserveArrayFields(updatedEntity, requestPayload, arrayFields);

        if (successText) {
            status.replaceChildren();
            status.textContent = successText;
        }

        await onSuccess?.({payload: requestPayload, response, responseEntity, updatedEntity});
        return updatedEntity;
    } catch (error) {
        if (onError) {
            await onError(error);
        } else {
            SurfView.Offerings.renderOfferingFormError(status, error, errorMessage || 'Unable to save');
        }
        return null;
    } finally {
        submitButton.disabled = false;
    }
}

/** Applies the shared state updates after an entity create or edit succeeds. */
function applySubmitEntitySuccess(config) {
    const {
        kind,
        mode = 'edit',
        updatedEntity,
        id,
        matchId = id,
        cacheMap = entityMap(kind),
        invalidateCache,
        languages = entity => collectLanguages(entity),
        preserveModalLanguage = false,
        title,
        detailsHtml,
        updateUrl = true,
        replaceUrl = mode === 'create',
        renderList = true
    } = config;
    const normalizedId = id ? String(id) : '';
    const normalizedMatchId = matchId ? String(matchId) : normalizedId;

    SurfView.State.currentModalEntity = updatedEntity;
    if (cacheMap && normalizedId) {
        cacheMap.set(normalizedId, updatedEntity);
    }
    invalidateCache?.();

    SurfView.State.currentPageEntities = mode === 'create'
        ? [updatedEntity, ...SurfView.State.currentPageEntities]
        : SurfView.State.currentPageEntities.map(entity => (
            String(entityId(kind, entity)) === normalizedMatchId ? updatedEntity : entity
        ));

    SurfView.State.currentModalLanguages = languages(updatedEntity) || [];
    if (!preserveModalLanguage || !SurfView.State.currentModalLanguages.includes(SurfView.State.currentModalLanguage)) {
        SurfView.State.currentModalLanguage = chooseLanguage(SurfView.State.currentModalLanguages);
    }

    moduleModal.title.textContent = title?.(updatedEntity, normalizedId) || normalizedId || titleCase(kind);
    setEntityDeletePending(false);
    setEntityModalMode('details');
    modalBody.replaceChildren(detailsHtml(updatedEntity));
    if (updateUrl) {
        updateEntityUrl(kind, normalizedId, SurfView.State.currentModalLanguage, replaceUrl);
    }
    if (renderList) {
        renderEntityList();
    }
}

/** Deletes the current modal entity with shared pending, cache, and list updates. */
async function deleteCurrentEntity(config) {
    const {
        kind,
        id = entityId(kind, SurfView.State.currentModalEntity),
        endpoint,
        confirmMessage,
        cacheMap = entityMap(kind),
        invalidateCache,
        onSuccess,
        errorMessage = `Unable to delete ${kind}`
    } = config;
    const normalizedId = id ? String(id) : '';

    if (isReadOnlyMode()) {
        return;
    }
    if (!normalizedId) {
        return;
    }

    const message = typeof confirmMessage === 'function' ? confirmMessage(normalizedId) : confirmMessage;
    if (message && !window.confirm(message)) {
        return;
    }

    setEntityDeletePending(true);
    try {
        const response = await callEndpoint(endpoint(normalizedId), {
            method: 'DELETE',
            headers: {'Accept': 'application/json'}
        });
        if (!response.ok) {
            throw await requestErrorFromResponse(response);
        }
        if (onSuccess) {
            await onSuccess({id: normalizedId, response});
            return;
        }
        cacheMap?.delete(normalizedId);
        invalidateCache?.();
        SurfView.State.currentPageEntities = SurfView.State.currentPageEntities.filter(entity => (
            String(entityId(kind, entity)) !== normalizedId
        ));
        closeEntityModal();
        renderEntityList();
    } catch (error) {
        openErrorDetailsModal(error, errorMessage);
    } finally {
        setEntityDeletePending(false);
    }
}

/** Loads all paged items from an endpoint. */
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

/** Preloads lookup caches while keeping the caller usable if a lookup fails. */
async function preloadLookups(loaders) {
    await Promise.allSettled(loaders.map(loader => loader()));
}

// Finds value of localized string based on current lang && defaults
/** Returns display text from localized values. */
function textValueLocalizedAttribute(value) {
    if (Array.isArray(value)) {
        if (SurfView.State.currentModalLanguage && isLanguageValueList(value)) {
            const selected = value.find(entry => entry.language === SurfView.State.currentModalLanguage);
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

// does this have any value viz a viz textValueLocalizedAttribute?
/** Returns detail text for the active modal language. */
function detailTextValue(value) {
    if (Array.isArray(value) && isLanguageValueList(value)) {
        if (!SurfView.State.currentModalLanguage) {
            return textValueLocalizedAttribute(value);
        }

        const selected = value.find(entry => entry?.language === SurfView.State.currentModalLanguage);
        return selected?.value || '';
    }

    return value;
}

// Finds value of localized string based on given language
function textValueForLanguage(value, language) {
    if (Array.isArray(value)) {
        if (isLanguageValueList(value)) {
            const selected = value.find(entry => entry.language === language);
            return selected ? selected.value : '';
        } else {
            return textValueLocalizedAttribute(value);
        }
    } else {
        return value;
    }
}
