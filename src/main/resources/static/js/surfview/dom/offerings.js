function addOfferingFormHtml(offering) {
    const primaryCode = primaryCodeParts(offering.primaryCode, offeringId(offering));
    const modeOfDelivery = Array.isArray(offering.modeOfDelivery) ? offering.modeOfDelivery[0] : offering.modeOfDelivery;
    const fragment = document.createDocumentFragment();
    fragment.append(
        fieldLabelHtml('Offering ID', 'text', 'offeringId', offeringId(offering), {required: true, readonly: true}, 'full-width'),
        fieldLabelHtml('Primary code type', 'select', 'primaryCodeType', primaryCode.codeType, {
            required: true,
            options: Constants.enumOptions.offeringCodeType
        }),
        fieldLabelHtml('Primary code', 'text', 'primaryCode', primaryCode.code, {required: true}),
        fieldLabelHtml('Abbreviation', 'text', 'abbreviation', textValue(offering.abbreviation) || ''),
        languageSpecificSectionFragment({
            activeLanguage: draftOffering.language,
            buttonAttribute: 'data-offering-language-code',
            label: 'Offering language',
            fields: [
                fieldLabelHtml('Name', 'text', 'name', draftOffering.languageValues[draftOffering.language]?.name || '', {required: true}),
                fieldLabelHtml('Description', 'textarea', 'description', draftOffering.languageValues[draftOffering.language]?.description || '', {required: true}, 'full-width')
            ]
        }),
        fieldLabelHtml('Teaching language', 'select', 'teachingLanguage', offering.teachingLanguage || 'eng', {
            options: Constants.enumOptions.teachingLanguage,
            showValue: true
        }),
        fieldLabelHtml('Mode of delivery', 'select', 'modeOfDelivery', modeOfDelivery || 'online', {
            options: Constants.enumOptions.modeOfDelivery
        }),
        fieldLabelHtml('Start date', 'date', 'startDate', offering.startDate || '', {required: true}),
        fieldLabelHtml('End date', 'date', 'endDate', offering.endDate || '', {required: true}),
        fieldLabelHtml('Enrollment start', 'date', 'enrollStartDate', offering.enrollStartDate || ''),
        fieldLabelHtml('Enrollment end', 'date', 'enrollEndDate', offering.enrollEndDate || ''),
        fieldLabelHtml('Min students', 'number', 'minNumberStudents', offering.minNumberStudents ?? '', {min: 0}),
        fieldLabelHtml('Max students', 'number', 'maxNumberStudents', offering.maxNumberStudents ?? '', {min: 0}),
        fieldLabelHtml('Result expected', 'select', 'resultExpected', String(offering.resultExpected ?? true), {
            options: Constants.enumOptions.resultExpected
        }),
        fieldLabelHtml('Result value type', 'select', 'resultValueType', offering.resultValueType || '1-10', {
            options: Constants.enumOptions.resultValueType
        }),
        fieldLabelHtml('Link', 'url', 'link', offering.link || '', {placeholder: 'https://'}, 'full-width'),
        fieldLabelHtml('Organization', 'select', 'organization', organizationIdValue(offering.organization), {
            options: organizationOptionEntries(organizationIdValue(offering.organization))
        }),
        fieldLabelHtml('Academic session', 'select', 'academicSessionId', academicSessionIdForOffering(offering), {
            options: academicSessionOptionEntries(academicSessionIdForOffering(offering))
        }),
        nestedSummaryPanelFragment('Price information', 'Add price', 'price', 'offering-price-summary'),
        nestedSummaryPanelFragment('Addresses', 'Add address', 'address', 'offering-address-summary'),
        consumerFormPanel(offering),
        offeringFormActionsFragment(offeringForm.dataset.offeringMode === 'edit' ? 'Save' : 'Add')
    );
    return fragment;
}

function academicSessionIdForOffering(offering) {
    if (offering.academicSessionId) {
        return offering.academicSessionId;
    }

    if (offering.academic_session_id) {
        return offering.academic_session_id;
    }

    if (typeof offering.academicSession === 'string') {
        return offering.academicSession;
    }

    return academicSessionIdValue(offering.academicSession);
}

function consumerFormPanel(offering) {
    const consumer = consumerForOffering(offering);
    const alliance = allianceForConsumer(consumer);
    const allianceName = consumerAllianceName(alliance.name);
    const section = document.createElement('section');
    section.className = 'nested-item-panel consumer-panel';
    section.append(
        consumerPanelHead(),
        fieldLabelHtml('Consumer key', 'text', 'consumerKey', Constants.offeringConsumer.consumerKey, {readonly: true}),
        fieldLabelHtml('Alliance name', 'select', 'consumerAllianceName', allianceName, {
            options: consumerAllianceOptions()
        }),
        fieldLabelHtml('Enrollment start time', 'time', 'consumerEnrollStartTime', alliance.enrollStartTime || '', {step: 1}),
        fieldLabelHtml('Enrollment end time', 'time', 'consumerEnrollEndTime', alliance.enrollEndTime || '', {step: 1}),
        fieldLabelHtml('Enrollment URL for guests', 'url', 'consumerEnrollmentUrlForGuests', alliance.enrollmentUrlForGuests || '', {placeholder: 'https://'}, 'full-width'),
        fieldLabelHtml('Enrollment URL', 'url', 'consumerEnrollmentUrl', alliance.enrollmentUrl || '', {placeholder: 'https://'}, 'full-width'),
        fieldLabelHtml('Date comment', 'textarea', 'consumerDateComment', alliance.dateComment || '', {}, 'full-width'),
        fieldLabelHtml('Queued students', 'number', 'consumerQueuedNumberStudents', alliance.queuedNumberStudents ?? '', {min: 0}),
        fieldLabelHtml('Max queued students', 'number', 'consumerMaxQueuedNumberStudents', alliance.maxQueuedNumberStudents ?? '', {min: 0}),
        fieldLabelHtml('Has student queue', 'select', 'consumerHasStudentQueue', String(alliance.hasStudentQueue ?? true), {
            options: Constants.enumOptions.resultExpected
        })
    );
    return section;
}

function consumerPanelHead() {
    const head = document.createElement('div');
    head.className = 'nested-item-head';

    const heading = document.createElement('h3');
    heading.textContent = 'Consumer';

    head.append(heading);
    return head;
}

function consumerForOffering(offering) {
    if (offering.consumer && typeof offering.consumer === 'object') {
        return offering.consumer;
    }

    if (Array.isArray(offering.consumers)) {
        return offering.consumers.find(consumer => consumer?.consumerKey === Constants.offeringConsumer.consumerKey)
            || offering.consumers[0]
            || {};
    }

    return {};
}

function allianceForConsumer(consumer) {
    if (!Array.isArray(consumer?.alliances)) {
        return {};
    }

    return consumer.alliances.find(alliance => Constants.offeringConsumer.allianceNames.includes(alliance?.name))
        || consumer.alliances[0]
        || {};
}

function consumerAllianceName(value) {
    return Constants.offeringConsumer.allianceNames.includes(value)
        ? value
        : Constants.offeringConsumer.defaultAllianceName;
}

function consumerAllianceOptions() {
    return Constants.offeringConsumer.allianceNames.map(name => [name, name]);
}

function priceFormHtml(price = {}) {
    const fragment = document.createDocumentFragment();
    fragment.append(
        fieldLabelHtml('Cost type', 'select', 'costType', price.costType || 'total costs', {
            required: true,
            options: Constants.enumOptions.costType
        }),
        fieldLabelHtml('Amount', 'number', 'amount', price.amount ?? '', {required: true, min: 0, step: 0.01}),
        fieldLabelHtml('VAT amount', 'number', 'vatAmount', price.vatAmount ?? '', {min: 0, step: 0.01}),
        fieldLabelHtml('Amount without VAT', 'number', 'amountWithoutVat', price.amountWithoutVat ?? '', {min: 0, step: 0.01}),
        fieldLabelHtml('Currency', 'text', 'currency', price.currency || 'EUR', {required: true}),
        languageSpecificSectionFragment({
            activeLanguage: draftOffering.nestedPriceLanguage,
            buttonAttribute: 'data-price-language-code',
            label: 'Price language',
            fields: [
                fieldLabelHtml('Display amount', 'text', 'displayAmount', draftOffering.nestedPriceLanguageValues[draftOffering.nestedPriceLanguage]?.displayAmount || '', {placeholder: '€340.84'}, 'full-width'),
                fieldLabelHtml('Description', 'textarea', 'description', draftOffering.nestedPriceLanguageValues[draftOffering.nestedPriceLanguage]?.description || '', {placeholder: 'Describe what this price covers'}, 'full-width')
            ]
        }),
        nestedFormActionsFragment('Add')
    );
    return fragment;
}

function offeringLabel(offering, index) {
    return textValue(offering.name)
        || offering.offeringId
        || offering.courseOfferingId
        || `Offering ${index + 1}`;
}

function offeringId(offering) {
    return offering.offeringId || offering.courseOfferingId || offering.programOfferingId || '';
}

function offeringCountCacheKey(kind, id) {
    return `${kind}:${id}`;
}

function offeringCountCell(kind, entity) {
    const id = entityId(kind, entity);
    if (!id) {
        return '';
    }

    const cacheKey = offeringCountCacheKey(kind, id);
    if (offeringCountByEntity.has(cacheKey)) {
        return offeringCountByEntity.get(cacheKey);
    }

    const placeholder = textElement('span', '...', 'muted');
    placeholder.dataset.offeringCountKind = kind;
    placeholder.dataset.offeringCountId = id;
    return placeholder;
}

async function hydrateOfferingCounts(tab, entities, renderId) {
    if (!tab.kind || !tab.offeringsEndpoint) {
        return;
    }

    await Promise.all(entities.map(async entity => {
        const id = entityId(tab.kind, entity);
        if (!id) {
            return;
        }

        const cacheKey = offeringCountCacheKey(tab.kind, id);
        if (!offeringCountByEntity.has(cacheKey)) {
            const offerings = await loadEntityOfferings(tab.kind, id);
            offeringCountByEntity.set(cacheKey, offerings.length);
        }

        if (renderId !== tableRenderId) {
            return;
        }

        const cell = tableRegion.querySelector(
            `[data-offering-count-kind="${cssEscape(tab.kind)}"][data-offering-count-id="${cssEscape(id)}"]`
        );
        if (cell) {
            cell.classList.remove('muted');
            cell.textContent = offeringCountByEntity.get(cacheKey);
        }
    }));
}

function offeringSectionHtml(offering, index) {
    const label = offeringLabel(offering, index);
    const id = offeringId(offering);
    const fields = [
        ['Name', textValue(offering.name)],
        ['Code type', primaryCodeParts(offering.primaryCode).codeType],
        ['Code', primaryCodeParts(offering.primaryCode).code],
        ['Type', offering.offeringType],
        ['Mode of delivery', listValue(offering.modeOfDelivery)],
        ['Starts', offering.startDate || offering.firstStartDate],
        ['Ends', offering.endDate],
        ['Study load', studyLoadValue(offering.studyLoad)],
        ['Price', priceInformationValue(offering.priceInformation)],
        ['Location', locationValue(offering)]
    ];
    const article = document.createElement('article');
    article.className = 'offering-card';
    article.id = `offering-${index + 1}`;
    const head = document.createElement('div');
    head.className = 'detail-section-head';
    head.append(textElement('h3', label));
    if (id) {
        const actions = document.createElement('div');
        actions.className = 'offering-card-actions';
        const editButton = document.createElement('button');
        editButton.className = 'detail-action-button';
        editButton.type = 'button';
        editButton.dataset.editOfferingIndex = String(index);
        editButton.textContent = 'Edit';
        const deleteButton = document.createElement('button');
        deleteButton.className = 'detail-action-button';
        deleteButton.type = 'button';
        deleteButton.dataset.deleteOfferingId = id;
        deleteButton.textContent = 'Delete';
        actions.append(editButton, deleteButton);
        head.append(actions);
    }
    const details = document.createElement('div');
    details.className = 'detail-grid';
    details.append(...fields.map(([fieldLabel, value]) => detailFieldElement(fieldLabel, value)));
    article.append(head, details);
    return article;
}

function primaryCodeParts(primaryCode, fallbackCode = '') {
    if (!primaryCode) {
        return {
            codeType: 'identifier',
            code: fallbackCode
        };
    }

    if (typeof primaryCode === 'string') {
        return {
            codeType: 'identifier',
            code: primaryCode
        };
    }

    return {
        codeType: normalizeOfferingCodeType(primaryCode.codeType),
        code: primaryCode.code || fallbackCode
    };
}

function normalizeOfferingCodeType(codeType) {
    const validCodeTypes = new Set(Constants.enumOptions.offeringCodeType.map(([value]) => value));
    return validCodeTypes.has(codeType) ? codeType : 'identifier';
}

function locationValue(offering) {
    if (offering.location) {
        return typeof offering.location === 'string'
            ? offering.location
            : textValue(offering.location.name) || offering.location.roomId || offering.location.buildingId || '';
    }

    if (Array.isArray(offering.addresses) && offering.addresses.length) {
        return addressSummaryLabel(offering.addresses[0]);
    }

    return '';
}

function priceInformationValue(priceInformation) {
    if (!Array.isArray(priceInformation) || !priceInformation.length) {
        return '';
    }

    return priceInformation.map(price => {
        const displayAmount = textValue(price.displayAmount)
            || [price.amount, price.currency].filter(Boolean).join(' ');
        return [displayAmount, price.costType].filter(Boolean).join(' - ');
    }).filter(Boolean).join(', ');
}

function updateAddOfferingUrl(kind, id) {
    const url = new URL(window.location.href);
    url.searchParams.set('tab', kind === 'program' ? 'programs' : 'courses');
    url.searchParams.delete('section');
    url.searchParams.set(kind === 'program' ? 'programId' : 'courseId', id);
    url.searchParams.delete(kind === 'program' ? 'courseId' : 'programId');
    url.searchParams.set('action', 'addOffering');
    window.history.pushState({kind, id, action: 'addOffering'}, '', url);
}

async function openAddOfferingModal(kind, id) {
    updateAddOfferingUrl(kind, id);
    const entity = entityMap(kind).get(String(id)) || currentModalEntity || {};
    return openOfferingModal(kind, id, defaultOfferingForEntity(kind, id, entity), {mode: 'add'});
}

async function openEditOfferingModal(indexValue) {
    const index = Number(indexValue);
    if (!Number.isInteger(index) || index < 0 || index >= currentModalOfferings.length) {
        return;
    }

    const parentId = entityId(currentModalKind, currentModalEntity);
    await openOfferingModal(currentModalKind, parentId, currentModalOfferings[index], {mode: 'edit'});
}

async function openOfferingModal(kind, id, offering, options = {}) {
    await Promise.allSettled([
        loadOrganizations(),
        loadAcademicSessions()
    ]);
    const mode = options.mode || 'add';
    resetDraftOffering();
    draftOffering.addresses = Array.isArray(offering.addresses) ? structuredClone(offering.addresses) : [];
    draftOffering.priceInformation = Array.isArray(offering.priceInformation) ? structuredClone(offering.priceInformation) : [];
    initializeDraftOfferingLanguages(offering);

    offeringModal.title.textContent = `${mode === 'edit' ? 'Edit' : 'Add'} ${titleCase(kind)} offering`;
    offeringModal.subtitle.textContent = `${titleCase(kind)} ID: ${id}`;
    offeringForm.dataset.offeringKind = kind;
    offeringForm.dataset.parentId = id;
    offeringForm.dataset.offeringMode = mode;
    offeringForm.dataset.offeringId = offeringId(offering);
    offeringForm.replaceChildren(addOfferingFormHtml(offering));
    renderNestedOfferingSummaries();
    offeringModal.backdrop.classList.add('open');
    offeringForm.querySelector('input[name="offeringId"]').focus();
}

function defaultOfferingForEntity(kind, id, entity) {
    const baseName = textValue(entity.name) || id;
    const today = new Date().toISOString().slice(0, 10);
    const nextMonth = new Date();
    nextMonth.setMonth(nextMonth.getMonth() + 1);
    const newOfferingId = generateId();
    return {
        offeringId: newOfferingId,
        primaryCode: {
            codeType: 'identifier',
            code: newOfferingId
        },
        offeringType: kind,
        name: languageValue(`${baseName} offering`, currentModalLanguage || 'en-GB'),
        abbreviation: `${String(baseName).slice(0, 18)} offer`,
        description: languageValue(`Offering for ${baseName}`, currentModalLanguage || 'en-GB'),
        teachingLanguage: 'eng',
        modeOfDelivery: ['online'],
        startDate: today,
        endDate: nextMonth.toISOString().slice(0, 10),
        enrollStartDate: today,
        enrollEndDate: nextMonth.toISOString().slice(0, 10),
        minNumberStudents: 1,
        maxNumberStudents: 30,
        resultExpected: true,
        resultValueType: '1-10',
        organization: organizationIdValue(entity.organization)
    };
}

function closeAddOfferingModal(options = {}) {
    closeOfferingJsonPreview();
    closeErrorDetailsModal();
    offeringModal.backdrop.classList.remove('open');
    offeringForm.replaceChildren();
    resetDraftOffering();
    delete offeringForm.dataset.offeringMode;
    delete offeringForm.dataset.offeringId;
    if (options.updateHistory !== false) {
        clearAddOfferingUrl();
    }
}

function clearAddOfferingUrl() {
    const url = new URL(window.location.href);
    if (url.searchParams.get('action') !== 'addOffering') {
        return;
    }
    url.searchParams.delete('action');
    window.history.pushState({}, '', url);
}

function openOfferingJsonPreview() {
    const kind = offeringForm.dataset.offeringKind;
    const parentId = offeringForm.dataset.parentId;
    if (!kind || !parentId) {
        return;
    }

    const payload = offeringPayloadFromForm(kind, parentId);
    jsonPreviewModal.title.textContent = 'Offering JSON';
    jsonPreviewModal.subtitle.textContent = 'Payload that will be submitted when adding or saving this offering';
    jsonPreviewContent.textContent = JSON.stringify(payload, null, 2);
    jsonPreviewModal.backdrop.classList.add('open');
    jsonPreviewModal.close.focus();
}

function closeOfferingJsonPreview() {
    jsonPreviewModal.backdrop.classList.remove('open');
    jsonPreviewContent.textContent = '';
}

function initializeDraftOfferingLanguages(offering) {
    draftOffering.languages = currentModalLanguages.length ? [...currentModalLanguages] : ['en-GB'];
    draftOffering.language = currentModalLanguage && draftOffering.languages.includes(currentModalLanguage)
        ? currentModalLanguage
        : draftOffering.languages[0];
    draftOffering.languageValues = Object.fromEntries(draftOffering.languages.map(language => [language, {
        name: textValueForLanguage(offering.name, language),
        description: textValueForLanguage(offering.description, language)
    }]));
}

function saveDraftOfferingLanguageFields() {
    if (!draftOffering.language) {
        return;
    }

    draftOffering.languageValues[draftOffering.language] = {
        name: offeringForm.elements.name?.value || '',
        description: offeringForm.elements.description?.value || ''
    };
}

function switchDraftOfferingLanguage(language) {
    if (!language || language === draftOffering.language || !draftOffering.languages.includes(language)) {
        return;
    }

    saveDraftOfferingLanguageFields();
    draftOffering.language = language;
    const values = draftOffering.languageValues[language] || {name: '', description: ''};
    if (offeringForm.elements.name) {
        offeringForm.elements.name.value = values.name || '';
    }
    if (offeringForm.elements.description) {
        offeringForm.elements.description.value = values.description || '';
    }
    if (offeringForm.elements.organization) {
        const selectedOrganizationId = offeringForm.elements.organization.value;
        offeringForm.elements.organization.replaceChildren(
            ...enumOptionsHtml(organizationOptionEntries(selectedOrganizationId), selectedOrganizationId)
        );
        offeringForm.elements.organization.value = selectedOrganizationId;
    }
    if (offeringForm.elements.academicSessionId) {
        const selectedAcademicSessionId = offeringForm.elements.academicSessionId.value;
        offeringForm.elements.academicSessionId.replaceChildren(
            ...enumOptionsHtml(academicSessionOptionEntries(selectedAcademicSessionId), selectedAcademicSessionId)
        );
        offeringForm.elements.academicSessionId.value = selectedAcademicSessionId;
    }
    offeringForm.querySelectorAll('[data-offering-language-code]').forEach(button => {
        button.classList.toggle('active', button.dataset.offeringLanguageCode === language);
    });
    renderNestedOfferingSummaries();
}

function openNestedOfferingModal(type, options = {}) {
    nestedOfferingForm.dataset.nestedType = type;
    const editIndex = Number(options.index);
    const isEditing = Number.isInteger(editIndex);
    const price = type === 'price' && isEditing ? draftOffering.priceInformation[editIndex] : {};
    const address = type === 'address' && isEditing ? draftOffering.addresses[editIndex] : {};
    nestedOfferingForm.dataset.nestedMode = isEditing ? 'edit' : 'add';
    if (isEditing) {
        nestedOfferingForm.dataset.nestedIndex = String(editIndex);
    } else {
        delete nestedOfferingForm.dataset.nestedIndex;
    }
    nestedOfferingModal.title.textContent = type === 'price'
        ? `${isEditing ? 'Edit' : 'Add'} price information`
        : `${isEditing ? 'Edit' : 'Add'} address`;
    nestedOfferingModal.subtitle.textContent = offeringModal.subtitle.textContent;
    if (type === 'price') {
        initializeDraftNestedPriceLanguages(price);
    } else if (type === 'address') {
        initializeDraftNestedAddressLanguages(address);
    }
    nestedOfferingForm.replaceChildren(type === 'price' ? priceFormHtml(price) : addressFormHtml(address));
    if (isEditing) {
        const submitButton = nestedOfferingForm.querySelector('button[type="submit"]');
        if (submitButton) {
            submitButton.textContent = 'Save';
        }
    }
    nestedOfferingModal.backdrop.classList.add('open');
    nestedOfferingForm.querySelector('input, select, textarea')?.focus();
}

function closeNestedOfferingModal() {
    nestedOfferingModal.backdrop.classList.remove('open');
    nestedOfferingForm.replaceChildren();
    delete nestedOfferingForm.dataset.nestedType;
    delete nestedOfferingForm.dataset.nestedMode;
    delete nestedOfferingForm.dataset.nestedIndex;
    draftOffering.nestedPriceLanguage = 'en-GB';
    draftOffering.nestedPriceLanguageValues = {};
    draftOffering.nestedAddressLanguage = 'en-GB';
    draftOffering.nestedAddressLanguageValues = {};
}

function initializeDraftNestedPriceLanguages(price = {}) {
    const languages = draftOffering.languages.length ? draftOffering.languages : [draftOffering.language || 'en-GB'];
    draftOffering.nestedPriceLanguage = draftOffering.language && languages.includes(draftOffering.language)
        ? draftOffering.language
        : languages[0];
    draftOffering.nestedPriceLanguageValues = Object.fromEntries(languages.map(language => [language, {
        description: textValueForLanguage(price.description, language),
        displayAmount: textValueForLanguage(price.displayAmount, language)
    }]));
}

function saveDraftNestedPriceLanguageFields() {
    if (!draftOffering.nestedPriceLanguage || nestedOfferingForm.dataset.nestedType !== 'price') {
        return;
    }

    draftOffering.nestedPriceLanguageValues[draftOffering.nestedPriceLanguage] = {
        description: nestedOfferingForm.elements.description?.value || '',
        displayAmount: nestedOfferingForm.elements.displayAmount?.value || ''
    };
}

function switchDraftNestedPriceLanguage(language) {
    if (!language || language === draftOffering.nestedPriceLanguage || !(language in draftOffering.nestedPriceLanguageValues)) {
        return;
    }

    saveDraftNestedPriceLanguageFields();
    draftOffering.nestedPriceLanguage = language;
    const values = draftOffering.nestedPriceLanguageValues[language] || {description: '', displayAmount: ''};
    if (nestedOfferingForm.elements.description) {
        nestedOfferingForm.elements.description.value = values.description || '';
    }
    if (nestedOfferingForm.elements.displayAmount) {
        nestedOfferingForm.elements.displayAmount.value = values.displayAmount || '';
    }
    nestedOfferingForm.querySelectorAll('[data-price-language-code]').forEach(button => {
        button.classList.toggle('active', button.dataset.priceLanguageCode === language);
    });
}

function initializeDraftNestedAddressLanguages(address = {}) {
    const languages = draftOffering.languages.length ? draftOffering.languages : [draftOffering.language || 'en-GB'];
    draftOffering.nestedAddressLanguage = draftOffering.language && languages.includes(draftOffering.language)
        ? draftOffering.language
        : languages[0];
    draftOffering.nestedAddressLanguageValues = Object.fromEntries(languages.map(language => [language, {
        additional: textValueForLanguage(address.additional, language)
    }]));
}

function saveDraftNestedAddressLanguageFields() {
    if (!draftOffering.nestedAddressLanguage || nestedOfferingForm.dataset.nestedType !== 'address') {
        return;
    }

    draftOffering.nestedAddressLanguageValues[draftOffering.nestedAddressLanguage] = {
        additional: nestedOfferingForm.elements.additional?.value || ''
    };
}

function switchDraftNestedAddressLanguage(language) {
    if (!language || language === draftOffering.nestedAddressLanguage || !(language in draftOffering.nestedAddressLanguageValues)) {
        return;
    }

    saveDraftNestedAddressLanguageFields();
    draftOffering.nestedAddressLanguage = language;
    const values = draftOffering.nestedAddressLanguageValues[language] || {additional: ''};
    if (nestedOfferingForm.elements.additional) {
        nestedOfferingForm.elements.additional.value = values.additional || '';
    }
    nestedOfferingForm.querySelectorAll('[data-address-language-code]').forEach(button => {
        button.classList.toggle('active', button.dataset.addressLanguageCode === language);
    });
}

function submitNestedOfferingForm(event) {
    event.preventDefault();
    const cancelButton = event.submitter?.closest?.('[data-cancel-nested]');
    if (cancelButton) {
        closeNestedOfferingModal();
        return;
    }

    const type = nestedOfferingForm.dataset.nestedType;
    if (type === 'price') {
        saveDraftNestedPriceLanguageFields();
        const price = priceItemFromNestedForm();
        const index = Number(nestedOfferingForm.dataset.nestedIndex);
        if (nestedOfferingForm.dataset.nestedMode === 'edit' && Number.isInteger(index) && index >= 0 && index < draftOffering.priceInformation.length) {
            draftOffering.priceInformation[index] = price;
        } else {
            draftOffering.priceInformation.push(price);
        }
    } else if (type === 'address') {
        saveDraftNestedAddressLanguageFields();
        const address = addressItemFromForm(nestedOfferingForm, nestedAddressLanguageValues('additional'));
        const index = Number(nestedOfferingForm.dataset.nestedIndex);
        if (nestedOfferingForm.dataset.nestedMode === 'edit' && Number.isInteger(index) && index >= 0 && index < draftOffering.addresses.length) {
            draftOffering.addresses[index] = address;
        } else {
            draftOffering.addresses.push(address);
        }
    }
    renderNestedOfferingSummaries();
    closeNestedOfferingModal();
}

function priceItemFromNestedForm() {
    const data = new FormData(nestedOfferingForm);
    const value = name => String(data.get(name) || '').trim();
    return removeEmptyPayloadValues({
        costType: value('costType'),
        amount: value('amount'),
        vatAmount: value('vatAmount'),
        amountWithoutVat: value('amountWithoutVat'),
        currency: value('currency') || 'EUR',
        description: nestedPriceLanguageValues('description'),
        displayAmount: nestedPriceLanguageValues('displayAmount')
    });
}

function nestedPriceLanguageValues(field) {
    return Object.entries(draftOffering.nestedPriceLanguageValues)
        .map(([language, values]) => ({
            language,
            value: String(values?.[field] || '').trim()
        }))
        .filter(item => item.value);
}

function nestedAddressLanguageValues(field) {
    return Object.entries(draftOffering.nestedAddressLanguageValues)
        .map(([language, values]) => ({
            language,
            value: String(values?.[field] || '').trim()
        }))
        .filter(item => item.value);
}

function renderNestedOfferingSummaries() {
    const priceSummary = document.getElementById('offering-price-summary');
    const addressSummary = document.getElementById('offering-address-summary');
    if (priceSummary) {
        priceSummary.replaceChildren(nestedListHtml(
            draftOffering.priceInformation,
            item => [textValueForLanguage(item.displayAmount, draftOffering.language) || `${item.amount || '-'} ${item.currency || ''}`.trim(), item.costType].filter(Boolean).join(' - '),
            'No price information',
            {
                editAttribute: 'data-edit-price-index',
                deleteAttribute: 'data-delete-price-index',
                itemName: 'price information'
            }
        ));
    }
    if (addressSummary) {
        addressSummary.replaceChildren(nestedListHtml(
            draftOffering.addresses,
            item => addressSummaryLabel(item) || 'Address',
            'No addresses',
            {
                editAttribute: 'data-edit-address-index',
                deleteAttribute: 'data-delete-address-index',
                itemName: 'address'
            }
        ));
    }
}

function deleteDraftNestedItem(items, indexValue) {
    const index = Number(indexValue);
    if (!Number.isInteger(index) || index < 0 || index >= items.length) {
        return;
    }

    items.splice(index, 1);
    renderNestedOfferingSummaries();
}


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

function errorDetailsText(error) {
    if (error?.responseJson?.trace) {
        return error.responseJson.trace;
    }
    if (error?.responseJson) {
        return JSON.stringify(error.responseJson, null, 2);
    }
    return error?.responseBody || error?.stack || error?.message || '';
}

function renderOfferingFormError(status, error, fallbackMessage) {
    status.classList.add('error');
    status.replaceChildren();

    const message = errorSummary(error, fallbackMessage);
    const messageElement = document.createElement('span');
    messageElement.className = 'offering-form-status-text';
    messageElement.textContent = message;
    status.append(messageElement);

    if (errorDetailsText(error)) {
        const detailsButton = document.createElement('button');
        detailsButton.className = 'error-maximize-button';
        detailsButton.type = 'button';
        detailsButton.dataset.openErrorDetails = '';
        detailsButton.requestError = error;
        detailsButton.requestErrorSummary = message;
        detailsButton.textContent = 'Maximize';
        status.append(detailsButton);
    }
}

async function submitOfferingForm(event) {
    event.preventDefault();
    const status = document.getElementById('offering-form-status');
    const submitButton = offeringForm.querySelector('button[type="submit"]');
    const kind = offeringForm.dataset.offeringKind;
    const parentId = offeringForm.dataset.parentId;
    const mode = offeringForm.dataset.offeringMode || 'add';
    const payload = offeringPayloadFromForm(kind, parentId);
    const submittedOfferingId = offeringId(payload);
    const endpoint = mode === 'edit'
        ? offeringUpdateEndpoint(kind, submittedOfferingId)
        : kind === 'program' ? '/offerings/programOffering' : '/offerings';
    const method = mode === 'edit' ? 'PUT' : 'POST';

    status.classList.remove('error');
    status.replaceChildren();
    status.textContent = mode === 'edit' ? 'Saving...' : 'Adding...';
    submitButton.disabled = true;

    try {
        const response = await callEndpoint(endpoint, {
            method,
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw await requestErrorFromResponse(response);
        }

        status.replaceChildren();
        status.textContent = mode === 'edit' ? 'Saved' : 'Added';
        await refreshCurrentModalOfferings(kind, parentId);
        closeAddOfferingModal();
    } catch (error) {
        renderOfferingFormError(status, error, mode === 'edit' ? 'Unable to save offering' : 'Unable to add offering');
    } finally {
        submitButton.disabled = false;
    }
}

function offeringUpdateEndpoint(kind, offeringId) {
    const encodedId = encodeURIComponent(offeringId);
    return `/offerings/${encodedId}`;
}

function offeringPayloadFromForm(kind, parentId) {
    saveDraftOfferingLanguageFields();
    const formData = new FormData(offeringForm);
    const value = name => String(formData.get(name) || '').trim();
    const numberValue = name => value(name) ? Number(value(name)) : null;
    const payload = {
        offeringId: value('offeringId'),
        primaryCode: {
            codeType: value('primaryCodeType'),
            code: value('primaryCode')
        },
        offeringType: kind,
        name: languageValuesFromDraft('name'),
        abbreviation: value('abbreviation'),
        description: languageValuesFromDraft('description'),
        teachingLanguage: value('teachingLanguage'),
        modeOfDelivery: value('modeOfDelivery') ? [value('modeOfDelivery')] : [],
        minNumberStudents: numberValue('minNumberStudents'),
        maxNumberStudents: numberValue('maxNumberStudents'),
        resultExpected: value('resultExpected') === 'true',
        resultValueType: value('resultValueType'),
        link: value('link'),
        startDate: value('startDate'),
        endDate: value('endDate'),
        enrollStartDate: value('enrollStartDate'),
        enrollEndDate: value('enrollEndDate'),
        organization: value('organization'),
        academicSessionId: value('academicSessionId'),
        priceInformation: draftOffering.priceInformation,
        addresses: draftOffering.addresses,
        consumer: consumerPayloadFromForm(formData)
    };
    payload[kind] = parentId;
    return removeEmptyPayloadValues(payload);
}

function consumerPayloadFromForm(formData) {
    const value = name => String(formData.get(name) || '').trim();
    const numberValue = name => value(name) ? Number(value(name)) : null;
    return {
        consumerKey: Constants.offeringConsumer.consumerKey,
        alliances: [
            removeEmptyPayloadValues({
                name: consumerAllianceName(value('consumerAllianceName')),
                enrollStartTime: value('consumerEnrollStartTime'),
                enrollEndTime: value('consumerEnrollEndTime'),
                enrollmentUrlForGuests: value('consumerEnrollmentUrlForGuests'),
                enrollmentUrl: value('consumerEnrollmentUrl'),
                dateComment: value('consumerDateComment'),
                queuedNumberStudents: numberValue('consumerQueuedNumberStudents'),
                maxQueuedNumberStudents: numberValue('consumerMaxQueuedNumberStudents'),
                hasStudentQueue: value('consumerHasStudentQueue') === 'true'
            })
        ]
    };
}

function languageValuesFromDraft(field) {
    return draftOffering.languages
        .map(language => ({
            language,
            value: String(draftOffering.languageValues[language]?.[field] || '').trim()
        }))
        .filter(item => item.value);
}

async function refreshCurrentModalOfferings(kind, id) {
    const cacheKey = offeringCountCacheKey(kind, id);
    offeringCountByEntity.delete(cacheKey);
    currentModalOfferings = await loadEntityOfferings(kind, id);
    offeringCountByEntity.set(cacheKey, currentModalOfferings.length);
    currentModalLanguages = collectLanguages({course: currentModalEntity, offerings: currentModalOfferings});
    modalBody.replaceChildren(entityDetailsHtml(currentModalKind, currentModalEntity, {offerings: currentModalOfferings}));
    renderCurrentEntities();
}

async function deleteOffering(button) {
    const offeringId = button.dataset.deleteOfferingId;
    const parentId = entityId(currentModalKind, currentModalEntity);
    if (!offeringId || !parentId) {
        return;
    }

    const confirmed = window.confirm(`Delete offering ${offeringId}?`);
    if (!confirmed) {
        return;
    }

    button.disabled = true;
    const originalText = button.textContent;
    button.textContent = 'Deleting';

    try {
        const response = await callEndpoint(`/offerings/${encodeURIComponent(offeringId)}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(await response.text() || `Request failed with status ${response.status}`);
        }

        await refreshCurrentModalOfferings(currentModalKind, parentId);
    } catch (error) {
        button.disabled = false;
        button.textContent = originalText;
        window.alert(error.message || 'Unable to delete offering');
    }
}

async function loadEntityOfferings(kind, id) {
    if (!id) {
        return [];
    }

    try {
        const tab = Constants.tabs[kind === 'program' ? 'programs' : 'courses'];
        return await loadAllEndpointItems(tab.offeringsEndpoint(id));
    } catch (error) {
        return [];
    }
}
