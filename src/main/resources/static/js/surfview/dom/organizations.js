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
    const form = document.createElement('form');
    const primaryCode = primaryCodeParts(organization.primaryCode, organization.organizationId);
    const localizedNameLanguages = organizationLocalizedNameLanguagesFromEntity(organization);
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
    const section = document.createElement('section');
    section.className = 'nested-item-panel entity-localized-panel';
    const head = document.createElement('div');
    head.className = 'nested-item-head';
    const heading = document.createElement('h3');
    heading.textContent = 'Localized content';
    const addButton = document.createElement('button');
    addButton.className = 'detail-action-button';
    addButton.type = 'button';
    addButton.dataset.addOrganizationLanguage = '';
    addButton.textContent = 'Add language';
    const list = document.createElement('div');
    list.className = 'entity-language-list';
    list.id = 'organization-language-list';
    organizationLanguageCodes(organization).forEach(language => list.append(organizationLanguageRow(organization, language)));
    head.append(heading, addButton);
    section.append(head, list);
    return section;
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

function organizationLocalizedNameLanguagesFromEntity(organization) {
    return [...new Set((Array.isArray(organization.name) ? organization.name : [])
        .filter(item => item?.language && String(item.value || '').trim())
        .map(item => item.language))];
}

function organizationLanguageRow(organization, language) {
    const row = document.createElement('section');
    row.className = 'entity-language-row organization-language-row';
    const head = document.createElement('div');
    head.className = 'entity-language-row-head';
    head.append(fieldLabelHtml('Language', 'select', 'organizationLanguage', language, {
        required: true,
        options: entityLanguageOptions(language)
    }));
    const deleteButton = document.createElement('button');
    deleteButton.className = 'nested-item-delete';
    deleteButton.type = 'button';
    deleteButton.dataset.deleteOrganizationLanguage = '';
    deleteButton.textContent = 'Delete';
    head.append(deleteButton);
    row.append(
        head,
        fieldLabelHtml('Name', 'text', 'organizationName', entityLocalizedFieldValue(organization.name, language), {required: true}, 'full-width'),
        fieldLabelHtml('Description', 'textarea', 'organizationDescription', entityLocalizedFieldValue(organization.description, language), {}, 'full-width')
    );
    return row;
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
    modalBody.replaceChildren(organizationFormFragment(organization, 'edit'));
    entityEditButton.classList.add('hidden');
    entityDeleteButton.classList.add('hidden');
    entityJsonPreviewButton.classList.remove('hidden');
    modalBody.querySelector('input[name="organizationName"]')?.focus();
}

function addOrganizationLanguageRow() {
    const list = document.getElementById('organization-language-list');
    if (!list) {
        return;
    }
    const selected = new Set([...list.querySelectorAll('select[name="organizationLanguage"]')].map(field => field.value));
    const language = Object.keys(Constants.languageNames).find(code => !selected.has(code));
    if (language) {
        list.append(organizationLanguageRow({}, language));
        syncOrganizationAddressLanguages();
    }
}

function deleteOrganizationLanguageRow(button) {
    const list = document.getElementById('organization-language-list');
    if (list?.querySelectorAll('.organization-language-row').length > 1) {
        button.closest('.organization-language-row')?.remove();
        syncOrganizationAddressLanguages();
    }
}

function organizationLocalizedNameLanguages() {
    const form = document.getElementById('entity-edit-form');
    if (!form || form.dataset.entityKind !== 'organization') {
        return [];
    }

    return [...new Set([...form.querySelectorAll('.organization-language-row')]
        .filter(row => row.querySelector('input[name="organizationName"]')?.value.trim())
        .map(row => row.querySelector('select[name="organizationLanguage"]')?.value)
        .filter(Boolean))];
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
    form.querySelectorAll('.organization-language-row').forEach(row => {
        const language = row.querySelector('select[name="organizationLanguage"]')?.value || '';
        const name = row.querySelector('input[name="organizationName"]')?.value.trim() || '';
        const description = row.querySelector('textarea[name="organizationDescription"]')?.value.trim() || '';
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
    const payload = organizationPayloadFromForm(form, currentModalEntity);
    const id = payload.organizationId;
    status.classList.remove('error');
    status.textContent = 'Saving...';
    submitButton.disabled = true;
    try {
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
