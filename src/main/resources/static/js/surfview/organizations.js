window.SurfView = window.SurfView || {};

/** Loads all organizations into the lookup cache. */
async function loadOrganizations() {
    if (organizationsLoadPromise) {
        return organizationsLoadPromise;
    }

    organizationsLoadPromise = (async () => {
        organizationsById.clear();
        const items = await loadAllEndpointItems('/organizations');
        items.forEach(organization => {
            const id = organizationIdValue(organization);
            if (id) {
                organizationsById.set(String(id), organization);
            }
        });
    })().catch(error => {
        organizationsLoadPromise = null;
        throw error;
    });

    return organizationsLoadPromise;
}

SurfView.Organizations = (() => {

    /** Builds the organization read-only detail view. */
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
        const description = detailTextValue(organization.description);
        const consumers = Array.isArray(organization.consumers) ? organization.consumers : [];
        const otherCodes = Array.isArray(organization.otherCodes) ? organization.otherCodes : [];
        const addresses = Array.isArray(organization.addresses) ? organization.addresses : [];
        const layout = cloneTemplate('detail-layout-template', null, {navLabel: 'Organization detail sections'});
        const nav = layout.querySelector('.detail-nav');
        nav.append(
            detailNavSectionElement('Language', languageDetailNavElement()),
            detailNavSectionElement('Organization', organizationSectionLinks(description, consumers, otherCodes, addresses))
        );

        const content = layout.querySelector('.detail-content');
        const overview = detailSectionElement('organization-overview', 'Overview');
        const grid = detailGridElement();
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
            const codesGrid = detailGridElement();
            codesGrid.append(...otherCodes.map(code => detailFieldElement(code.codeType || 'Code', code.code || '')));
            section.append(codesGrid);
            content.append(section);
        }
        if (addresses.length) {
            const section = detailSectionElement('organization-addresses', 'Addresses');
            section.append(addressesHtml(addresses));
            content.append(section);
        }
        if (consumers.length) {
            const section = detailSectionElement('organization-consumers', 'Consumers');
            section.append(organizationConsumersHtml(consumers));
            content.append(section);
        }
        return layout;
    }

    function organizationSectionLinks(description, consumers, otherCodes, addresses) {
        const fragment = document.createDocumentFragment();
        [
            ['organization-overview', 'Overview', true],
            ['organization-description', 'Description', Boolean(description)],
            ['organization-codes', 'Other codes', otherCodes.length > 0],
            ['organization-addresses', 'Addresses', addresses.length > 0],
            ['organization-consumers', 'Consumers', consumers.length > 0]
        ].forEach(([id, label, visible]) => {
            if (visible) {
                fragment.append(createElement('a', {
                    text: label,
                    attributes: {href: `#${id}`}
                }));
            }
        });
        return fragment;
    }

    function organizationConsumersHtml(consumers) {
        const fragment = document.createDocumentFragment();
        consumers.forEach((consumer, index) => {
            const article = createElement('article.offering-card');
            if (typeof consumer === 'string') {
                article.append(detailValueElement(consumer));
            } else {
                const label = consumer?.consumerKey || consumer?.consumerId || `Consumer ${index + 1}`;
                article.append(textElement('h3', label), jsonBoxElement(consumer));
            }
            fragment.append(article);
        });
        return fragment;
    }

    function organizationFormFragment(organization, mode = 'edit') {
        organizationDraftLanguageController.initialize(organization);
        SurfView.State.draftEntity.addresses = Array.isArray(organization.addresses) ? structuredClone(organization.addresses) : [];
        const form = entityEditFormElement('organization', mode);
        form.append(
            ...primaryCodeFormFields({
                idLabel: 'Organization ID',
                idName: 'organizationId',
                idValue: organization.organizationId,
                primaryCode: organization.primaryCode
            }),
            fieldLabelHtml('Organization type', 'select', 'organizationType', organization.organizationType || 'root', {
                required: true,
                options: Constants.enumOptions.organizationType
            }),
            fieldLabelHtml('Short name', 'text', 'shortName', organization.shortName || '', {required: true}),
            organizationLocalizedPanel(organization),
            fieldLabelHtml('Link', 'url', 'link', organization.link || '', {placeholder: 'https://'}, 'full-width'),
            fieldLabelHtml('Logo', 'url', 'logo', organization.logo || '', {placeholder: 'https://'}, 'full-width'),
            organizationCodesPanel(organization.otherCodes),
            organizationAddressesPanel(),
            organizationConsumerPanel(organization),
            entityEditFormActions(mode === 'edit' ? 'Save' : 'Add')
        );
        return form;
    }

    function organizationConsumerPanel(organization) {
        const {consumer, index: consumerIndex} = surfConsumerSelection(organization, {fallbackFirst: true});
        const {alliance, index: allianceIndex} = surfAllianceSelection(consumer, {fallbackFirst: true});
        const allianceRow = createElement('div.organization-alliance-row', {
            attributes: {'data-original-alliance-index': allianceIndex}
        });
        allianceRow.append(fieldLabelHtml('Alliance name', 'text', 'organizationAllianceName', alliance.name || '', {required: true}, 'full-width'));

        return consumerPanelFragment(
            consumer,
            [allianceRow],
            node => node.classList.add('organization-consumer-row'),
            consumerIndex
        );
    }

    function organizationLocalizedPanel(organization) {
        const section = languageSpecificSectionFragment({
            activeLanguage: SurfView.State.draftEntity.language,
            buttonAttribute: 'data-organization-language-code',
            label: 'Content language',
            languages: SurfView.State.draftEntity.languages,
            languageActions: organizationLanguageActions,
            headerContent: draftLanguageControls('organization', 'newOrganizationLanguage'),
            fields: organizationLanguageFields(SurfView.State.draftEntity.language)
        });
        section.classList.add('entity-localized-panel');
        section.dataset.organizationLocalizedPanel = '';
        organizationDraftLanguageController.refresh(section);
        return section;
    }

    function organizationLanguageActions(language) {
        return draftLanguageActions('organization', language);
    }

    function organizationLanguageFields(language) {
        const values = SurfView.State.draftEntity.languageValues[language] || {name: '', description: ''};
        return [
            fieldLabelHtml('Name', 'text', 'organizationName', values.name || '', {required: true}, 'full-width'),
            fieldLabelHtml('Description', 'textarea', 'organizationDescription', values.description || '', {}, 'full-width')
        ];
    }

    const organizationDraftLanguageController = createDraftLanguageController({
        prefix: 'organization',
        state: () => SurfView.State.draftEntity,
        form: () => document.getElementById('entity-edit-form'),
        fields: () => ['name', 'description'],
        value: (organization, field, language) => localizedFieldValue(organization[field], language),
        formFieldName: field => field === 'name' ? 'organizationName' : 'organizationDescription',
        canSave: form => form.dataset.entityKind === 'organization',
        renderPanel: renderOrganizationLanguagePanel,
        focusField: 'name'
    });

    const organizationAddressListPanel = createDraftAddressListPanel({
        summaryId: 'organization-address-summary',
        items: () => SurfView.State.draftEntity.addresses,
        languages: () => SurfView.State.draftEntity.languages,
        language: () => SurfView.State.draftEntity.language,
        subtitle: () => `Organization ID: ${SurfView.State.currentModalEntity?.organizationId || ''}`,
        editAttribute: 'data-edit-organization-address-index',
        deleteAttribute: 'data-delete-organization-address-index'
    });

    function organizationCodesPanel(codes = []) {
        const section = cloneTemplate('nested-summary-panel-template', section => {
            section.classList.add('organization-nested-panel');
            section.querySelector('button').dataset['addOrganizationCode'] = '';
        }, {title: 'Other codes', buttonText: 'Add code'})
        const list = createElement('div#organization-code-list.organization-code-list');
        (Array.isArray(codes) ? codes : []).forEach((code, index) => list.append(organizationCodeRow(code, index)));
        section.append(list);
        return section;
    }

    /** Builds an editable organization code row. */
    function organizationCodeRow(code = {}, index = '') {
        return cloneTemplate('organization-code-row-template', row => {
            row.prepend(
                fieldLabelHtml('Code type', 'datalist', 'otherCodeType', code.codeType || 'otherType', {
                    required: true,
                    options: Constants.enumOptions.codeType
                }),
                fieldLabelHtml('Code', 'text', 'otherCode', code.code || '', {required: true})
            );
        }, {index});
    }

    function organizationAddressesPanel() {
        return organizationAddressListPanel.panel();
    }

    function organizationAddressTarget() {
        return organizationAddressListPanel.target();
    }

    function deleteDraftOrganizationAddress(indexValue) {
        organizationAddressListPanel.deleteItem(indexValue);
    }

    /** Opens the shared modal with a new organization form. */
    function openCreateOrganizationModal() {
        if (isReadOnlyMode()) {
            return;
        }
        const organization = {
            organizationId: generateId(),
            primaryCode: {codeType: 'identifier', code: ''},
            organizationType: 'root',
            name: [{language: 'en-GB', value: ''}],
            description: [],
            consumers: [
                {
                    consumerKey: Constants.offeringConsumer.consumerKey,
                    alliances: [{name: Constants.offeringConsumer.defaultAllianceName}]
                }
            ],
            addresses: [],
            otherCodes: []
        };
        SurfView.State.currentModalKind = 'organization';
        SurfView.State.currentModalEntity = organization;
        SurfView.State.currentModalOfferings = [];
        SurfView.State.currentModalLanguages = ['en-GB'];
        SurfView.State.currentModalLanguage = 'en-GB';
        resetDraftEntity();
        moduleModal.title.textContent = 'Add organization';
        setEntityDeletePending(false);
        setEntityModalMode('form');
        modalBody.replaceChildren(organizationFormFragment(organization, 'create'));
        openManagedModal(moduleModal, {
            initialFocus: () => modalBody.querySelector('input[name="primaryCode"]')
        });
    }

    /** Opens the shared modal with an organization edit form. */
    function openOrganizationEditForm(organization) {
        if (isReadOnlyMode()) {
            return;
        }
        resetDraftEntity();
        modalBody.replaceChildren(organizationFormFragment(organization, 'edit'));
        setEntityModalMode('form');
        modalBody.querySelector('input[name="organizationName"]')?.focus();
    }

    /** Adds a language row to the organization draft. */
    function addOrganizationLanguageRow() {
        organizationDraftLanguageController.add();
    }

    /** Deletes a language row from the organization draft. */
    function deleteOrganizationLanguageRow(language) {
        organizationDraftLanguageController.deleteLanguage(language);
    }

    /** Switches the active organization draft language. */
    function switchDraftOrganizationLanguage(language) {
        organizationDraftLanguageController.switchLanguage(language);
    }

    function renderOrganizationLanguagePanel() {
        const panel = document.querySelector('[data-organization-localized-panel]');
        const replacement = organizationLocalizedPanel(SurfView.State.currentModalEntity || {});
        panel?.replaceWith(replacement);
    }

    /** Reads an organization API payload from a form. */
    function organizationPayloadFromForm(form, originalOrganization = {}) {
        organizationDraftLanguageController.save(form);

        if (!organizationDraftLanguageController.validateRequiredField({targetForm: form})) {
            return null;
        }
        if (!validateOrganizationConsumerFields(form)) {
            return null;
        }
        const data = new FormData(form);
        const value = name => String(data.get(name) || '').trim();
        const payload = structuredClone(originalOrganization || {});
        payload.organizationId = value('organizationId');
        payload.primaryCode = primaryCodePayloadFromFormData(data);
        payload.organizationType = value('organizationType');
        payload.shortName = value('shortName');
        payload.link = value('link');
        payload.logo = value('logo');
        Object.assign(payload, organizationDraftLanguageController.payloadValues());
        payload.consumers = organizationConsumersFromForm(form, originalOrganization);
        const originalCodes = Array.isArray(originalOrganization?.otherCodes) ? originalOrganization.otherCodes : [];
        payload.otherCodes = [...form.querySelectorAll('.organization-code-row')].map(row => {
            const original = organizationOriginalItem(row, originalCodes);
            return {...original, codeType: row.querySelector('[name="otherCodeType"]').value.trim(), code: row.querySelector('[name="otherCode"]').value.trim()};
        });
        payload.addresses = structuredClone(SurfView.State.draftEntity.addresses);
        return removeEmptyPayloadValues(payload);
    }

    function validateOrganizationConsumerFields(form) {
        const allianceName = form.elements.organizationAllianceName;
        if (!allianceName?.value.trim()) {
            allianceName?.reportValidity();
            return false;
        }

        return true;
    }

    function organizationConsumersFromForm(form, originalOrganization = {}) {
        const consumerRow = form.querySelector('.organization-consumer-row');
        const originalConsumers = Array.isArray(originalOrganization?.consumers) ? originalOrganization.consumers : [];
        const originalConsumerIndex = originalIndex(consumerRow?.dataset.originalConsumerIndex);
        const originalConsumer = originalConsumerIndex !== null ? originalConsumers[originalConsumerIndex] : {};
        const consumer = structuredClone(originalConsumer || {});
        const allianceRow = consumerRow?.querySelector('.organization-alliance-row');
        const originalAlliances = Array.isArray(originalConsumer?.alliances) ? originalConsumer.alliances : [];
        const originalAllianceIndex = originalIndex(allianceRow?.dataset.originalAllianceIndex);
        const originalAlliance = originalAllianceIndex !== null ? originalAlliances[originalAllianceIndex] : {};
        const alliance = structuredClone(originalAlliance || {});

        consumer.consumerKey = Constants.offeringConsumer.consumerKey;
        alliance.name = consumerRow?.querySelector('[name="organizationAllianceName"]')?.value.trim() || '';
        consumer.alliances = [alliance];
        return [consumer];
    }

    function organizationOriginalItem(row, originals) {
        const index = originalIndex(row.dataset.originalIndex);
        return index === null ? {} : structuredClone(originals[index] || {});
    }

    /** Submits an organization create or edit form. */
    async function submitOrganizationForm(form) {
        if (isReadOnlyMode()) {
            closeEntityEditForm();
            return;
        }
        const mode = form.dataset.entityMode;
        const status = document.getElementById('entity-edit-form-status');
        const submitButton = form.querySelector('button[type="submit"]');

        await submitEntity({
            status,
            submitButton,
            payload: () => organizationPayloadFromForm(form, SurfView.State.currentModalEntity),
            endpoint: payload => mode === 'create' ? '/organizations' : `/organizations/${encodeURIComponent(payload.organizationId)}`,
            method: mode === 'create' ? 'POST' : 'PUT',
            savingText: mode === 'edit' ? 'Saving...' : 'Adding...',
            successText: mode === 'edit' ? 'Saved' : 'Added',
            preserveArrayFields: ['name', 'description', 'consumers', 'addresses', 'otherCodes'],
            errorMessage: mode === 'edit' ? 'Unable to save organization' : 'Unable to add organization',
            onSuccess: ({payload, updatedEntity: updatedOrganization}) => {
                const id = payload.organizationId;
                applySubmitEntitySuccess({
                    kind: 'organization',
                    mode,
                    updatedEntity: updatedOrganization,
                    id,
                    cacheMap: organizationsById,
                    invalidateCache: () => {
                        organizationsLoadPromise = null;
                    },
                    title: organization => textValueLocalizedAttribute(organization.name) || id,
                    detailsHtml: organizationDetailsHtml
                });
            }
        });
    }

    /** Deletes the organization currently open in the detail modal. */
    async function deleteCurrentOrganization() {
        await deleteCurrentEntity({
            kind: 'organization',
            id: organizationIdValue(SurfView.State.currentModalEntity),
            endpoint: id => `/organizations/${encodeURIComponent(id)}`,
            confirmMessage: id => `Delete organization ${id}?`,
            cacheMap: organizationsById,
            invalidateCache: () => {
                organizationsLoadPromise = null;
            },
            errorMessage: 'Unable to delete organization'
        });
    }

    /** Returns the display label for an organization. */
    function organizationLabel(organization, language = SurfView.State.draftOffering.language || SurfView.State.currentModalLanguage) {
        return localizedNameLabel(organization, organizationIdValue, language);
    }

    /** Resolves an organization reference to display text. */
    function organizationValue(value) {
        if (!value) {
            return '';
        }

        if (typeof value === 'string') {
            const organization = organizationsById.get(value);
            return organization ? organizationLabel(organization) : UNKNOWN_REFERENCE_LABEL;
        }

        const organizationId = organizationIdValue(value);
        const organization = organizationsById.get(String(organizationId));
        return organizationLabel(value) || (organization ? organizationLabel(organization) : UNKNOWN_REFERENCE_LABEL);
    }

    /** Builds organization select option entries. */
    function organizationOptionEntries(selectedOrganizationId = '') {
        return entityOptionEntries({
            items: organizationsById.values(),
            selectedId: selectedOrganizationId,
            emptyLabel: 'Select organization',
            label: organizationLabel,
            idValue: organizationIdValue
        });
    }

    /** Registers organization event handlers. */
    function initEventListeners() {
        delegateClick(modalBody, {
            '[data-add-organization-language]': () => addOrganizationLanguageRow(),
            '[data-delete-organization-language]': button => deleteOrganizationLanguageRow(button.dataset.deleteOrganizationLanguage),
            '[data-organization-language-code]': button => switchDraftOrganizationLanguage(button.dataset.organizationLanguageCode),
            '[data-add-organization-code]': () => {
                const list = document.getElementById('organization-code-list');
                list?.append(organizationCodeRow());
                list?.lastElementChild?.querySelector('input')?.focus();
            },
            '[data-delete-organization-code]': button => button.closest('.organization-code-row')?.remove(),
            '[data-open-nested-offering="address"]': () => openNestedAddressModal(organizationAddressTarget()),
            '[data-edit-organization-address-index]': button => openNestedAddressModal(organizationAddressTarget(), {index: button.dataset.editOrganizationAddressIndex}),
            '[data-delete-organization-address-index]': button => deleteDraftOrganizationAddress(button.dataset.deleteOrganizationAddressIndex)
        });
    }

    return {
        openCreateOrganizationModal,
        openOrganizationEditForm,
        organizationDetailsHtml,
        organizationCodeRow,
        addOrganizationLanguageRow,
        deleteOrganizationLanguageRow,
        switchDraftOrganizationLanguage,
        organizationPayloadFromForm,
        submitOrganizationForm,
        deleteCurrentOrganization,
        organizationLabel,
        organizationValue,
        organizationOptionEntries,
        initEventListeners
    };

})();
