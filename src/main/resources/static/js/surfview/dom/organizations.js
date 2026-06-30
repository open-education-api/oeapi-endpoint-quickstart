function organizationDetailsHtml(organization) {
    const id = organizationIdValue(organization);
    const fields = [
        ['Organization ID', id],
        ['Primary code', plainCodeValue(organization.primaryCode)],
        ['Type', organization.organizationType],
        ['Short name', organization.shortName],
        ['Link', organization.link],
        ['Logo', organization.logo]
    ];
    const description = textValue(organization.description);
    const otherCodes = Array.isArray(organization.otherCodes) ? organization.otherCodes : [];
    const addresses = Array.isArray(organization.addresses) ? organization.addresses : [];
    const layout = document.createElement('div');
    layout.className = 'course-detail-layout';
    const nav = document.createElement('nav');
    nav.className = 'detail-nav';
    nav.setAttribute('aria-label', 'Organization detail sections');
    nav.append(
        detailNavSectionElement('Language', organizationLanguageNavElement()),
        detailNavSectionElement('Organization', organizationSectionLinks(description, otherCodes, addresses))
    );

    const content = document.createElement('div');
    content.className = 'detail-content';
    const overview = detailSectionElement('organization-overview', 'Overview');
    const grid = document.createElement('div');
    grid.className = 'detail-grid';
    grid.append(...fields.map(([label, value]) => detailFieldElement(label, value)));
    overview.append(grid);
    content.append(overview);

    if (description) {
        const section = detailSectionElement('organization-description', 'Description');
        section.append(detailValueElement(description));
        content.append(section);
    }
    if (otherCodes.length) {
        const section = detailSectionElement('organization-codes', 'Other codes');
        const codesGrid = document.createElement('div');
        codesGrid.className = 'detail-grid';
        codesGrid.append(...otherCodes.map(code => detailFieldElement(code.codeType || 'Code', code.code || '')));
        section.append(codesGrid);
        content.append(section);
    }
    if (addresses.length) {
        const section = detailSectionElement('organization-addresses', 'Addresses');
        section.append(addressesHtml(addresses));
        content.append(section);
    }
    layout.append(nav, content);
    return layout;
}

function organizationLanguageNavElement() {
    if (!currentModalLanguages.length) {
        return textElement('span', 'No language-specific content', 'detail-nav-empty');
    }
    const items = document.createElement('div');
    items.className = 'language-nav-items';
    currentModalLanguages.forEach(code => {
        const link = document.createElement('a');
        link.className = `language-nav-link${code === currentModalLanguage ? ' active' : ''}`;
        link.href = '#';
        link.dataset.languageCode = code;
        link.title = languageLabel(code);
        link.textContent = code;
        items.append(link);
    });
    return items;
}

function organizationSectionLinks(description, otherCodes, addresses) {
    const fragment = document.createDocumentFragment();
    [
        ['organization-overview', 'Overview', true],
        ['organization-description', 'Description', Boolean(description)],
        ['organization-codes', 'Other codes', otherCodes.length > 0],
        ['organization-addresses', 'Addresses', addresses.length > 0]
    ].forEach(([id, label, visible]) => {
        if (visible) {
            const link = document.createElement('a');
            link.href = `#${id}`;
            link.textContent = label;
            fragment.append(link);
        }
    });
    return fragment;
}

function organizationFormFragment(organization, mode = 'edit') {
    initializeDraftOrganizationLanguages(organization);
    const form = document.createElement('form');
    const primaryCode = primaryCodeParts(organization.primaryCode, organization.organizationId);
    const localizedNameLanguages = organizationLocalizedNameLanguagesFromDraft();
    form.className = 'offering-form entity-edit-form';
    form.id = 'entity-edit-form';
    form.dataset.entityKind = 'organization';
    form.dataset.entityMode = mode;
    form.append(
        fieldLabelHtml('Organization ID', 'text', 'organizationId', organization.organizationId || generateId(), {
            required: true,
            readonly: true
        }, 'full-width'),
        fieldLabelHtml('Primary code type', 'select', 'primaryCodeType', primaryCode.codeType, {
            required: true,
            options: Constants.enumOptions.offeringCodeType
        }),
        fieldLabelHtml('Primary code', 'text', 'primaryCode', primaryCode.code, {required: true}),
        fieldLabelHtml('Organization type', 'select', 'organizationType', organization.organizationType || 'root', {
            required: true,
            options: Constants.enumOptions.organizationType
        }),
        fieldLabelHtml('Short name', 'text', 'shortName', organization.shortName || ''),
        organizationLocalizedPanel(organization),
        fieldLabelHtml('Link', 'url', 'link', organization.link || '', {placeholder: 'https://'}, 'full-width'),
        fieldLabelHtml('Logo', 'url', 'logo', organization.logo || '', {placeholder: 'https://'}, 'full-width'),
        organizationCodesPanel(organization.otherCodes),
        organizationAddressesPanel(organization.addresses, localizedNameLanguages),
        entityEditFormActions()
    );
    return form;
}

function organizationLocalizedPanel(organization) {
    const section = languageSpecificSectionFragment({
        activeLanguage: draftEntity.language,
        buttonAttribute: 'data-organization-language-code',
        label: 'Content language',
        languages: draftEntity.languages,
        languageActions: organizationLanguageActions,
        headerContent: organizationLanguageControls(),
        fields: organizationLanguageFields(draftEntity.language)
    });
    section.classList.add('entity-localized-panel');
    section.dataset.organizationLocalizedPanel = '';
    refreshOrganizationLanguageControls(section);
    return section;
}

function organizationLanguageActions(language) {
    const deleteButton = document.createElement('button');
    deleteButton.className = 'language-delete-button';
    deleteButton.type = 'button';
    deleteButton.dataset.deleteOrganizationLanguage = language;
    deleteButton.title = `Delete ${languageLabel(language)}`;
    deleteButton.setAttribute('aria-label', `Delete ${languageLabel(language)}`);
    deleteButton.textContent = 'x';
    return deleteButton;
}

function organizationLanguageControls() {
    const controls = document.createElement('div');
    controls.className = 'entity-language-controls full-width';

    const label = document.createElement('label');
    label.append(labelTextElement('Add language', false));

    const select = document.createElement('select');
    select.name = 'newOrganizationLanguage';
    select.dataset.organizationLanguageSelect = '';
    label.append(select);

    const addButton = document.createElement('button');
    addButton.className = 'detail-action-button';
    addButton.type = 'button';
    addButton.dataset.addOrganizationLanguage = '';
    addButton.textContent = 'Add';

    controls.append(label, addButton);
    return controls;
}

function organizationLanguageFields(language) {
    const values = draftEntity.languageValues[language] || organizationEmptyLanguageValues();
    return [
        fieldLabelHtml('Name', 'text', 'organizationName', values.name || '', {required: true}, 'full-width'),
        fieldLabelHtml('Description', 'textarea', 'organizationDescription', values.description || '', {}, 'full-width')
    ];
}

function organizationEmptyLanguageValues() {
    return {name: '', description: ''};
}

function initializeDraftOrganizationLanguages(organization) {
    const languages = organizationLanguageCodes(organization);
    draftEntity.languages = languages.length ? [...new Set(languages)] : ['en-GB'];
    draftEntity.language = currentModalLanguage && draftEntity.languages.includes(currentModalLanguage)
        ? currentModalLanguage
        : draftEntity.languages[0];
    draftEntity.languageValues = Object.fromEntries(draftEntity.languages.map(language => [language, {
        name: entityLocalizedFieldValue(organization.name, language),
        description: entityLocalizedFieldValue(organization.description, language)
    }]));
}

function organizationLanguageCodes(organization) {
    const codes = new Set();
    ['name', 'description'].forEach(field => {
        (Array.isArray(organization[field]) ? organization[field] : []).forEach(item => {
            if (item?.language) {
                codes.add(item.language);
            }
        });
    });
    if (!codes.size) {
        codes.add(currentModalLanguage || 'en-GB');
    }
    return [...codes];
}

function organizationLocalizedNameLanguagesFromDraft() {
    return draftEntity.languages.filter(language => String(draftEntity.languageValues[language]?.name || '').trim());
}

function organizationCodesPanel(codes = []) {
    const section = organizationNestedPanel('Other codes', 'Add code', 'addOrganizationCode');
    const list = document.createElement('div');
    list.className = 'organization-code-list';
    list.id = 'organization-code-list';
    (Array.isArray(codes) ? codes : []).forEach((code, index) => list.append(organizationCodeRow(code, index)));
    section.append(list);
    return section;
}

function organizationCodeRow(code = {}, index = '') {
    const row = document.createElement('div');
    row.className = 'organization-code-row';
    row.dataset.originalIndex = index;
    row.append(
        fieldLabelHtml('Code type', 'text', 'otherCodeType', code.codeType || '', {required: true}),
        fieldLabelHtml('Code', 'text', 'otherCode', code.code || '', {required: true})
    );
    const deleteButton = document.createElement('button');
    deleteButton.className = 'nested-item-delete organization-row-delete';
    deleteButton.type = 'button';
    deleteButton.dataset.deleteOrganizationCode = '';
    deleteButton.textContent = 'Delete';
    row.append(deleteButton);
    return row;
}

function organizationAddressesPanel(addresses = [], languages = []) {
    const section = organizationNestedPanel('Addresses', 'Add address', 'addOrganizationAddress');
    section.querySelector('[data-add-organization-address]').disabled = !languages.length;
    const list = document.createElement('div');
    list.className = 'organization-address-list';
    list.id = 'organization-address-list';
    (Array.isArray(addresses) ? addresses : []).forEach((address, index) => {
        list.append(organizationAddressRow(address, index, languages));
    });
    section.append(list);
    return section;
}

function organizationAddressRow(address = {}, index = '', languages = organizationLocalizedNameLanguages()) {
    const row = document.createElement('section');
    row.className = 'organization-address-row';
    row.dataset.originalIndex = index;
    const head = document.createElement('div');
    head.className = 'nested-item-head organization-address-head';
    const heading = document.createElement('h4');
    heading.textContent = addressLabel(address, Number(index) || 0);
    const deleteButton = document.createElement('button');
    deleteButton.className = 'nested-item-delete';
    deleteButton.type = 'button';
    deleteButton.dataset.deleteOrganizationAddress = '';
    deleteButton.textContent = 'Delete';
    head.append(heading, deleteButton);
    row.append(
        head,
        ...addressFieldElements(address),
        organizationAddressAdditionalPanel(address.additional, languages)
    );
    return row;
}

function organizationAddressAdditionalPanel(additional = [], languages = []) {
    const panel = document.createElement('div');
    panel.className = 'full-width organization-address-additional';
    languages.forEach(language => {
        panel.append(fieldLabelHtml(
            `Additional (${language})`,
            'textarea',
            `additional:${language}`,
            entityLocalizedFieldValue(additional, language)
        ));
    });
    return panel;
}

function organizationNestedPanel(title, buttonText, dataAttribute) {
    const section = document.createElement('section');
    section.className = 'nested-item-panel organization-nested-panel';
    const head = document.createElement('div');
    head.className = 'nested-item-head';
    const heading = document.createElement('h3');
    heading.textContent = title;
    const button = document.createElement('button');
    button.className = 'detail-action-button';
    button.type = 'button';
    button.dataset[dataAttribute] = '';
    button.textContent = buttonText;
    head.append(heading, button);
    section.append(head);
    return section;
}

function openCreateOrganizationModal() {
    const organization = {
        organizationId: generateId(),
        primaryCode: {codeType: 'identifier', code: ''},
        organizationType: 'root',
        name: [{language: 'en-GB', value: ''}],
        description: [],
        addresses: [],
        otherCodes: []
    };
    currentModalKind = 'organization';
    currentModalEntity = organization;
    currentModalOfferings = [];
    currentModalLanguages = ['en-GB'];
    currentModalLanguage = 'en-GB';
    resetDraftEntity();
    courseModal.title.textContent = 'Add organization';
    entityEditButton.classList.add('hidden');
    entityDeleteButton.classList.add('hidden');
    entityJsonPreviewButton.classList.remove('hidden');
    modalBody.replaceChildren(organizationFormFragment(organization, 'create'));
    courseModal.backdrop.classList.add('open');
    document.body.style.overflow = 'hidden';
    modalBody.querySelector('input[name="primaryCode"]')?.focus();
}

function openOrganizationEditForm(organization) {
    resetDraftEntity();
    modalBody.replaceChildren(organizationFormFragment(organization, 'edit'));
    entityEditButton.classList.add('hidden');
    entityDeleteButton.classList.add('hidden');
    entityJsonPreviewButton.classList.remove('hidden');
    modalBody.querySelector('input[name="organizationName"]')?.focus();
}

function addOrganizationLanguageRow() {
    const form = document.getElementById('entity-edit-form');
    const select = form?.querySelector('[data-organization-language-select]');
    const language = select?.value || '';
    if (!form || !language || draftEntity.languages.includes(language) || !(language in Constants.languageNames)) {
        return;
    }

    saveDraftOrganizationLanguageFields(form);
    draftEntity.languages.push(language);
    draftEntity.languageValues[language] = organizationEmptyLanguageValues();
    renderOrganizationLanguagePanel();
    switchDraftOrganizationLanguage(language);
    syncOrganizationAddressLanguages();
}

function deleteOrganizationLanguageRow(language) {
    const form = document.getElementById('entity-edit-form');
    if (!form || draftEntity.languages.length <= 1) {
        return;
    }

    saveDraftOrganizationLanguageFields(form);
    const index = draftEntity.languages.indexOf(language);
    if (index < 0) {
        return;
    }

    draftEntity.languages.splice(index, 1);
    delete draftEntity.languageValues[language];
    if (draftEntity.language === language) {
        draftEntity.language = draftEntity.languages[Math.min(index, draftEntity.languages.length - 1)];
    }
    renderOrganizationLanguagePanel();
    syncOrganizationAddressLanguages();
}

function saveDraftOrganizationLanguageFields(form = document.getElementById('entity-edit-form')) {
    if (!form || form.dataset.entityKind !== 'organization' || !draftEntity.language) {
        return;
    }

    draftEntity.languageValues[draftEntity.language] = {
        name: form.elements.organizationName?.value || '',
        description: form.elements.organizationDescription?.value || ''
    };
}

function switchDraftOrganizationLanguage(language) {
    const form = document.getElementById('entity-edit-form');
    if (!form || !language || language === draftEntity.language || !draftEntity.languages.includes(language)) {
        return;
    }

    saveDraftOrganizationLanguageFields(form);
    draftEntity.language = language;
    const values = draftEntity.languageValues[language] || organizationEmptyLanguageValues();
    if (form.elements.organizationName) {
        form.elements.organizationName.value = values.name || '';
    }
    if (form.elements.organizationDescription) {
        form.elements.organizationDescription.value = values.description || '';
    }
    form.querySelectorAll('[data-organization-language-code]').forEach(button => {
        button.classList.toggle('active', button.dataset.organizationLanguageCode === language);
    });
    refreshOrganizationLanguageControls(form);
    syncOrganizationAddressLanguages();
    form.elements.organizationName?.focus();
}

function renderOrganizationLanguagePanel() {
    const panel = document.querySelector('[data-organization-localized-panel]');
    const replacement = organizationLocalizedPanel(currentModalEntity || {});
    panel?.replaceWith(replacement);
}

function refreshOrganizationLanguageControls(scope = document) {
    const panel = scope.closest?.('[data-organization-localized-panel]') || scope.querySelector?.('[data-organization-localized-panel]') || scope;
    const select = panel.querySelector?.('[data-organization-language-select]');
    const addButton = panel.querySelector?.('[data-add-organization-language]');
    const deleteButtons = panel.querySelectorAll?.('[data-delete-organization-language]') || [];
    if (!select || !addButton) {
        return;
    }

    const options = entityLanguageOptions();
    select.replaceChildren(...enumOptionsHtml(options.length ? options : [['', 'No languages available']], ''));
    select.value = options[0]?.[0] || '';
    addButton.disabled = !options.length;
    deleteButtons.forEach(button => {
        button.disabled = draftEntity.languages.length <= 1;
    });
}

function organizationLocalizedNameLanguages() {
    const form = document.getElementById('entity-edit-form');
    if (!form || form.dataset.entityKind !== 'organization') {
        return [];
    }

    saveDraftOrganizationLanguageFields(form);
    return organizationLocalizedNameLanguagesFromDraft();
}

function syncOrganizationAddressLanguages() {
    const languages = organizationLocalizedNameLanguages();
    const addButton = document.querySelector('[data-add-organization-address]');
    if (addButton) {
        addButton.disabled = !languages.length;
    }

    document.querySelectorAll('.organization-address-additional').forEach(panel => {
        const values = new Map([...panel.querySelectorAll('[name^="additional:"]')]
            .map(field => [field.name.slice('additional:'.length), field.value]));
        panel.replaceChildren(...languages.map(language => fieldLabelHtml(
            `Additional (${language})`,
            'textarea',
            `additional:${language}`,
            values.get(language) || ''
        )));
    });
}

function organizationPayloadFromForm(form, originalOrganization = {}) {
    saveDraftOrganizationLanguageFields(form);
    const data = new FormData(form);
    const value = name => String(data.get(name) || '').trim();
    const payload = structuredClone(originalOrganization || {});
    payload.organizationId = value('organizationId');
    payload.primaryCode = {codeType: value('primaryCodeType'), code: value('primaryCode')};
    payload.organizationType = value('organizationType');
    payload.shortName = value('shortName');
    payload.link = value('link');
    payload.logo = value('logo');
    payload.name = [];
    payload.description = [];
    const uniqueLanguages = [...new Set(draftEntity.languages.filter(language => language in Constants.languageNames))];
    if (!uniqueLanguages.length || uniqueLanguages.length !== draftEntity.languages.length) {
        throw new Error('Add at least one unique language.');
    }
    uniqueLanguages.forEach(language => {
        const name = String(draftEntity.languageValues[language]?.name || '').trim();
        const description = String(draftEntity.languageValues[language]?.description || '').trim();
        if (language && name) {
            payload.name.push({language, value: name});
        }
        if (language && description) {
            payload.description.push({language, value: description});
        }
    });
    const originalCodes = Array.isArray(originalOrganization?.otherCodes) ? originalOrganization.otherCodes : [];
    payload.otherCodes = [...form.querySelectorAll('.organization-code-row')].map(row => {
        const original = organizationOriginalItem(row, originalCodes);
        return {...original, codeType: row.querySelector('[name="otherCodeType"]').value.trim(), code: row.querySelector('[name="otherCode"]').value.trim()};
    });
    const originalAddresses = Array.isArray(originalOrganization?.addresses) ? originalOrganization.addresses : [];
    payload.addresses = [...form.querySelectorAll('.organization-address-row')]
        .map(row => organizationAddressFromRow(row, originalAddresses));
    return removeEmptyPayloadValues(payload);
}

function organizationOriginalItem(row, originals) {
    const index = entityOriginalIndex(row.dataset.originalIndex);
    return index === null ? {} : structuredClone(originals[index] || {});
}

function organizationAddressFromRow(row, originals) {
    const address = addressPayloadFromFields(row, organizationOriginalItem(row, originals), {
        keepEmptyGeolocation: true
    });
    address.additional = [...row.querySelectorAll('[name^="additional:"]')]
        .map(field => ({language: field.name.slice('additional:'.length), value: field.value.trim()}))
        .filter(item => item.value);
    return address;
}

async function submitOrganizationForm(form) {
    const mode = form.dataset.entityMode;
    const status = document.getElementById('entity-edit-form-status');
    const submitButton = form.querySelector('button[type="submit"]');
    status.classList.remove('error');
    status.replaceChildren();
    try {
        const payload = organizationPayloadFromForm(form, currentModalEntity);
        const id = payload.organizationId;
        status.textContent = 'Saving...';
        submitButton.disabled = true;
        const response = await callEndpoint(mode === 'create' ? '/organizations' : `/organizations/${encodeURIComponent(id)}`, {
            method: mode === 'create' ? 'POST' : 'PUT',
            headers: {'Accept': 'application/json', 'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            throw await requestErrorFromResponse(response);
        }
        const responseText = await response.text();
        let responseOrganization = {};
        try {
            const parsedResponse = responseText ? JSON.parse(responseText) : {};
            responseOrganization = parsedResponse && typeof parsedResponse === 'object' && !Array.isArray(parsedResponse)
                ? parsedResponse
                : {};
        } catch (error) {
            responseOrganization = {};
        }
        const updatedOrganization = {...payload, ...responseOrganization};
        ['name', 'description', 'addresses', 'otherCodes'].forEach(field => {
            if (!Array.isArray(updatedOrganization[field]) || (!updatedOrganization[field].length && payload[field]?.length)) {
                updatedOrganization[field] = payload[field];
            }
        });
        currentModalEntity = updatedOrganization;
        organizationById.set(String(id), updatedOrganization);
        organizationsById.set(String(id), updatedOrganization);
        organizationsLoadPromise = null;
        if (mode === 'create') {
            currentPageEntities = [updatedOrganization, ...currentPageEntities];
        } else {
            currentPageEntities = currentPageEntities.map(item => organizationIdValue(item) === id ? updatedOrganization : item);
        }
        currentModalLanguages = collectLanguages(updatedOrganization);
        currentModalLanguage = chooseLanguage(currentModalLanguages);
        courseModal.title.textContent = textValue(updatedOrganization.name) || id;
        entityEditButton.classList.remove('hidden');
        entityDeleteButton.classList.remove('hidden');
        entityJsonPreviewButton.classList.add('hidden');
        modalBody.replaceChildren(organizationDetailsHtml(updatedOrganization));
        updateEntityUrl('organization', id, currentModalLanguage, mode === 'create');
        renderCurrentEntities();
    } catch (error) {
        renderOfferingFormError(status, error, 'Unable to save organization');
    } finally {
        submitButton.disabled = false;
    }
}

async function deleteCurrentOrganization() {
    const id = organizationIdValue(currentModalEntity);
    if (!id || !window.confirm(`Delete organization ${id}?`)) {
        return;
    }
    entityDeleteButton.disabled = true;
    try {
        const response = await callEndpoint(`/organizations/${encodeURIComponent(id)}`, {
            method: 'DELETE',
            headers: {'Accept': 'application/json'}
        });
        if (!response.ok) {
            throw await requestErrorFromResponse(response);
        }
        organizationById.delete(String(id));
        organizationsById.delete(String(id));
        organizationsLoadPromise = null;
        currentPageEntities = currentPageEntities.filter(item => organizationIdValue(item) !== id);
        closeEntityModal();
        renderCurrentEntities();
    } catch (error) {
        openErrorDetailsModal(error, 'Unable to delete organization');
    } finally {
        entityDeleteButton.disabled = false;
    }
}
