window.SurfView = window.SurfView || {};

SurfView.Organisations = (() => {

    /** Builds the organisation read-only detail view. */
    function organisationDetailsHtml(organisation) {
        const id = organisationIdValue(organisation);
        const fields = [
            ['Organisation ID', id],
            ['Primary code', plainCodeValue(organisation.primaryCode)],
            ['Type', organisation.organisationType],
            ['Short name', organisation.shortName],
            ['Link', organisation.link],
            ['Logo', organisation.logo]
        ];
        const description = detailTextValue(organisation.description);
        const consumers = Array.isArray(organisation.consumers) ? organisation.consumers : [];
        const otherCodes = Array.isArray(organisation.otherCodes) ? organisation.otherCodes : [];
        const addresses = Array.isArray(organisation.addresses) ? organisation.addresses : [];
        const layout = cloneTemplate('detail-layout-template', null, {navLabel: 'Organisation detail sections'});
        const nav = layout.querySelector('.detail-nav');
        nav.append(
            detailNavSectionElement('Language', languageDetailNavElement()),
            detailNavSectionElement('Organisation', organisationSectionLinks(description, consumers, otherCodes, addresses))
        );

        const content = layout.querySelector('.detail-content');
        const overview = detailSectionElement('organisation-overview', 'Overview');
        const grid = detailGridElement();
        grid.append(...fields.map(([label, value]) => detailFieldElement(label, value)));
        overview.append(grid);
        content.append(overview);

        if (description) {
            const section = detailSectionElement('organisation-description', 'Description');
            section.append(detailValueElement(description));
            content.append(section);
        }
        if (otherCodes.length) {
            const section = detailSectionElement('organisation-codes', 'Other codes');
            const codesGrid = detailGridElement();
            codesGrid.append(...otherCodes.map(code => detailFieldElement(code.codeType || 'Code', code.code || '')));
            section.append(codesGrid);
            content.append(section);
        }
        if (addresses.length) {
            const section = detailSectionElement('organisation-addresses', 'Addresses');
            section.append(addressesHtml(addresses));
            content.append(section);
        }
        if (consumers.length) {
            const section = detailSectionElement('organisation-consumers', 'Consumers');
            section.append(organisationConsumersHtml(consumers));
            content.append(section);
        }
        return layout;
    }

    function organisationSectionLinks(description, consumers, otherCodes, addresses) {
        const fragment = document.createDocumentFragment();
        [
            ['organisation-overview', 'Overview', true],
            ['organisation-description', 'Description', Boolean(description)],
            ['organisation-codes', 'Other codes', otherCodes.length > 0],
            ['organisation-addresses', 'Addresses', addresses.length > 0],
            ['organisation-consumers', 'Consumers', consumers.length > 0]
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

    function organisationConsumersHtml(consumers) {
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

    function organisationFormFragment(organisation, mode = 'edit') {
        organisationDraftLanguageController.initialize(organisation);
        SurfView.State.draftEntity.addresses = Array.isArray(organisation.addresses) ? structuredClone(organisation.addresses) : [];
        const form = entityEditFormElement('organisation', mode);
        form.append(
            ...primaryCodeFormFields({
                idLabel: 'Organisation ID',
                idName: 'organisationId',
                idValue: organisation.organisationId,
                primaryCode: organisation.primaryCode
            }),
            fieldLabelHtml('Organisation type', 'select', 'organisationType', organisation.organisationType || 'root', {
                required: true,
                options: Constants.enumOptions.organisationType
            }),
            fieldLabelHtml('Short name', 'text', 'shortName', organisation.shortName || '', {required: true}),
            organisationLocalizedPanel(organisation),
            fieldLabelHtml('Link', 'url', 'link', organisation.link || '', {placeholder: 'https://'}, 'full-width'),
            fieldLabelHtml('Logo', 'url', 'logo', organisation.logo || '', {placeholder: 'https://'}, 'full-width'),
            organisationCodesPanel(organisation.otherCodes),
            organisationAddressesPanel(),
            organisationConsumerPanel(organisation),
            entityEditFormActions(mode === 'edit' ? 'Save' : 'Add')
        );
        return form;
    }

    function organisationConsumerPanel(organisation) {
        const {consumer, index: consumerIndex} = surfConsumerSelection(organisation, {fallbackFirst: true});
        const {alliance, index: allianceIndex} = surfAllianceSelection(consumer, {fallbackFirst: true});
        const allianceRow = createElement('div.organisation-alliance-row', {
            attributes: {'data-original-alliance-index': allianceIndex}
        });
        allianceRow.append(fieldLabelHtml('Alliance name', 'text', 'organisationAllianceName', alliance.name || '', {required: true}, 'full-width'));

        return consumerPanelFragment(
            consumer,
            [allianceRow],
            node => node.classList.add('organisation-consumer-row'),
            consumerIndex
        );
    }

    function organisationLocalizedPanel(organisation) {
        const section = languageSpecificSectionFragment({
            activeLanguage: SurfView.State.draftEntity.language,
            buttonAttribute: 'data-organisation-language-code',
            label: 'Content language',
            languages: SurfView.State.draftEntity.languages,
            languageActions: organisationLanguageActions,
            headerContent: draftLanguageControls('organisation', 'newOrganisationLanguage'),
            fields: organisationLanguageFields(SurfView.State.draftEntity.language)
        });
        section.classList.add('entity-localized-panel');
        section.dataset.organisationLocalizedPanel = '';
        organisationDraftLanguageController.refresh(section);
        return section;
    }

    function organisationLanguageActions(language) {
        return draftLanguageActions('organisation', language);
    }

    function organisationLanguageFields(language) {
        const values = SurfView.State.draftEntity.languageValues[language] || {name: '', description: ''};
        return [
            fieldLabelHtml('Name', 'text', 'organisationName', values.name || '', {required: true}, 'full-width'),
            fieldLabelHtml('Description', 'textarea', 'organisationDescription', values.description || '', {}, 'full-width')
        ];
    }

    const organisationDraftLanguageController = createDraftLanguageController({
        prefix: 'organisation',
        state: () => SurfView.State.draftEntity,
        form: () => document.getElementById('entity-edit-form'),
        fields: () => ['name', 'description'],
        value: (organisation, field, language) => localizedFieldValue(organisation[field], language),
        formFieldName: field => field === 'name' ? 'organisationName' : 'organisationDescription',
        canSave: form => form.dataset.entityKind === 'organisation',
        renderPanel: renderOrganisationLanguagePanel,
        focusField: 'name'
    });

    const organisationAddressListPanel = createDraftAddressListPanel({
        summaryId: 'organisation-address-summary',
        items: () => SurfView.State.draftEntity.addresses,
        languages: () => SurfView.State.draftEntity.languages,
        language: () => SurfView.State.draftEntity.language,
        subtitle: () => `Organisation ID: ${SurfView.State.currentModalEntity?.organisationId || ''}`,
        editAttribute: 'data-edit-organisation-address-index',
        deleteAttribute: 'data-delete-organisation-address-index'
    });

    function organisationCodesPanel(codes = []) {
        const section = cloneTemplate('nested-summary-panel-template', section => {
            section.classList.add('organisation-nested-panel');
            section.querySelector('button').dataset['addOrganisationCode'] = '';
        }, {title: 'Other codes', buttonText: 'Add code'})
        const list = createElement('div#organisation-code-list.organisation-code-list');
        (Array.isArray(codes) ? codes : []).forEach((code, index) => list.append(organisationCodeRow(code, index)));
        section.append(list);
        return section;
    }

    /** Builds an editable organisation code row. */
    function organisationCodeRow(code = {}, index = '') {
        return cloneTemplate('organisation-code-row-template', row => {
            row.prepend(
                fieldLabelHtml('Code type', 'datalist', 'otherCodeType', code.codeType || 'otherType', {
                    required: true,
                    options: Constants.enumOptions.codeType
                }),
                fieldLabelHtml('Code', 'text', 'otherCode', code.code || '', {required: true})
            );
        }, {index});
    }

    function organisationAddressesPanel() {
        return organisationAddressListPanel.panel();
    }

    function organisationAddressTarget() {
        return organisationAddressListPanel.target();
    }

    function deleteDraftOrganisationAddress(indexValue) {
        organisationAddressListPanel.deleteItem(indexValue);
    }

    /** Opens the shared modal with a new organisation form. */
    function openCreateOrganisationModal() {
        if (isReadOnlyMode()) {
            return;
        }
        const organisation = {
            organisationId: generateId(),
            primaryCode: {codeType: 'identifier', code: ''},
            organisationType: 'root',
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
        SurfView.State.currentModalKind = 'organisation';
        SurfView.State.currentModalEntity = organisation;
        SurfView.State.currentModalOfferings = [];
        SurfView.State.currentModalLanguages = ['en-GB'];
        SurfView.State.currentModalLanguage = 'en-GB';
        resetDraftEntity();
        moduleModal.title.textContent = 'Add organisation';
        setEntityDeletePending(false);
        setEntityModalMode('form');
        modalBody.replaceChildren(organisationFormFragment(organisation, 'create'));
        openManagedModal(moduleModal, {
            initialFocus: () => modalBody.querySelector('input[name="primaryCode"]')
        });
    }

    /** Opens the shared modal with an organisation edit form. */
    function openOrganisationEditForm(organisation) {
        if (isReadOnlyMode()) {
            return;
        }
        resetDraftEntity();
        modalBody.replaceChildren(organisationFormFragment(organisation, 'edit'));
        setEntityModalMode('form');
        modalBody.querySelector('input[name="organisationName"]')?.focus();
    }

    /** Adds a language row to the organisation draft. */
    function addOrganisationLanguageRow() {
        organisationDraftLanguageController.add();
    }

    /** Deletes a language row from the organisation draft. */
    function deleteOrganisationLanguageRow(language) {
        organisationDraftLanguageController.deleteLanguage(language);
    }

    /** Switches the active organisation draft language. */
    function switchDraftOrganisationLanguage(language) {
        organisationDraftLanguageController.switchLanguage(language);
    }

    function renderOrganisationLanguagePanel() {
        const panel = document.querySelector('[data-organisation-localized-panel]');
        const replacement = organisationLocalizedPanel(SurfView.State.currentModalEntity || {});
        panel?.replaceWith(replacement);
    }

    /** Reads an organisation API payload from a form. */
    function organisationPayloadFromForm(form, originalOrganisation = {}) {
        organisationDraftLanguageController.save(form);

        if (!organisationDraftLanguageController.validateRequiredField({targetForm: form})) {
            return null;
        }
        if (!validateOrganisationConsumerFields(form)) {
            return null;
        }
        const data = new FormData(form);
        const value = name => String(data.get(name) || '').trim();
        const payload = structuredClone(originalOrganisation || {});
        payload.organisationId = value('organisationId');
        payload.primaryCode = primaryCodePayloadFromFormData(data);
        payload.organisationType = value('organisationType');
        payload.shortName = value('shortName');
        payload.link = value('link');
        payload.logo = value('logo');
        Object.assign(payload, organisationDraftLanguageController.payloadValues());
        payload.consumers = organisationConsumersFromForm(form, originalOrganisation);
        const originalCodes = Array.isArray(originalOrganisation?.otherCodes) ? originalOrganisation.otherCodes : [];
        payload.otherCodes = [...form.querySelectorAll('.organisation-code-row')].map(row => {
            const original = organisationOriginalItem(row, originalCodes);
            return {...original, codeType: row.querySelector('[name="otherCodeType"]').value.trim(), code: row.querySelector('[name="otherCode"]').value.trim()};
        });
        payload.addresses = structuredClone(SurfView.State.draftEntity.addresses);
        return removeEmptyPayloadValues(payload);
    }

    function validateOrganisationConsumerFields(form) {
        const allianceName = form.elements.organisationAllianceName;
        if (!allianceName?.value.trim()) {
            allianceName?.reportValidity();
            return false;
        }

        return true;
    }

    function organisationConsumersFromForm(form, originalOrganisation = {}) {
        const consumerRow = form.querySelector('.organisation-consumer-row');
        const originalConsumers = Array.isArray(originalOrganisation?.consumers) ? originalOrganisation.consumers : [];
        const originalConsumerIndex = originalIndex(consumerRow?.dataset.originalConsumerIndex);
        const originalConsumer = originalConsumerIndex !== null ? originalConsumers[originalConsumerIndex] : {};
        const consumer = structuredClone(originalConsumer || {});
        const allianceRow = consumerRow?.querySelector('.organisation-alliance-row');
        const originalAlliances = Array.isArray(originalConsumer?.alliances) ? originalConsumer.alliances : [];
        const originalAllianceIndex = originalIndex(allianceRow?.dataset.originalAllianceIndex);
        const originalAlliance = originalAllianceIndex !== null ? originalAlliances[originalAllianceIndex] : {};
        const alliance = structuredClone(originalAlliance || {});

        consumer.consumerKey = Constants.offeringConsumer.consumerKey;
        alliance.name = consumerRow?.querySelector('[name="organisationAllianceName"]')?.value.trim() || '';
        consumer.alliances = [alliance];
        return [consumer];
    }

    function organisationOriginalItem(row, originals) {
        const index = originalIndex(row.dataset.originalIndex);
        return index === null ? {} : structuredClone(originals[index] || {});
    }

    /** Submits an organisation create or edit form. */
    async function submitOrganisationForm(form) {
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
            payload: () => organisationPayloadFromForm(form, SurfView.State.currentModalEntity),
            endpoint: payload => mode === 'create' ? '/organisations' : `/organisations/${encodeURIComponent(payload.organisationId)}`,
            method: mode === 'create' ? 'POST' : 'PUT',
            savingText: mode === 'edit' ? 'Saving...' : 'Adding...',
            successText: mode === 'edit' ? 'Saved' : 'Added',
            preserveArrayFields: ['name', 'description', 'consumers', 'addresses', 'otherCodes'],
            errorMessage: mode === 'edit' ? 'Unable to save organisation' : 'Unable to add organisation',
            onSuccess: async ({payload, updatedEntity: updatedOrganisation}) => {
                const id = payload.organisationId;
                await applySubmitEntitySuccess({
                    kind: 'organisation',
                    mode,
                    updatedEntity: updatedOrganisation,
                    id,
                    title: organisation => textValueLocalizedAttribute(organisation.name) || id,
                    detailsHtml: organisationDetailsHtml
                });
            }
        });
    }

    /** Deletes the organisation currently open in the detail modal. */
    async function deleteCurrentOrganisation() {
        await deleteCurrentEntity({
            kind: 'organisation',
            id: organisationIdValue(SurfView.State.currentModalEntity),
            endpoint: id => `/organisations/${encodeURIComponent(id)}`,
            confirmMessage: id => `Delete organisation ${id}?`,
            errorMessage: 'Unable to delete organisation'
        });
    }

    /** Returns the display label for an organisation. */
    function organisationLabel(organisation, language = SurfView.State.draftOffering.language || SurfView.State.currentModalLanguage) {
        return localizedNameLabel(organisation, organisationIdValue, language);
    }

    /** Resolves an organisation reference to display text. */
    function organisationValue(value) {
        if (!value) {
            return '';
        }

        if (typeof value === 'string') {
            const organisation = organisationsById.get(value);
            return organisation ? organisationLabel(organisation) : UNKNOWN_REFERENCE_LABEL;
        }

        const organisationId = organisationIdValue(value);
        const organisation = organisationsById.get(String(organisationId));
        return organisationLabel(value) || (organisation ? organisationLabel(organisation) : UNKNOWN_REFERENCE_LABEL);
    }

    /** Builds organisation select option entries. */
    function organisationOptionEntries(selectedOrganisationId = '') {
        return entityOptionEntries({
            items: organisationsById.values(),
            selectedId: selectedOrganisationId,
            emptyLabel: 'Select organisation',
            label: organisationLabel,
            idValue: organisationIdValue
        });
    }

    /** Registers organisation event handlers. */
    function initEventListeners() {
        delegateClick(modalBody, {
            '[data-add-organisation-language]': () => addOrganisationLanguageRow(),
            '[data-delete-organisation-language]': button => deleteOrganisationLanguageRow(button.dataset.deleteOrganisationLanguage),
            '[data-organisation-language-code]': button => switchDraftOrganisationLanguage(button.dataset.organisationLanguageCode),
            '[data-add-organisation-code]': () => {
                const list = document.getElementById('organisation-code-list');
                list?.append(organisationCodeRow());
                list?.lastElementChild?.querySelector('input')?.focus();
            },
            '[data-delete-organisation-code]': button => button.closest('.organisation-code-row')?.remove(),
            '[data-open-nested-offering="address"]': () => openNestedAddressModal(organisationAddressTarget()),
            '[data-edit-organisation-address-index]': button => openNestedAddressModal(organisationAddressTarget(), {index: button.dataset.editOrganisationAddressIndex}),
            '[data-delete-organisation-address-index]': button => deleteDraftOrganisationAddress(button.dataset.deleteOrganisationAddressIndex)
        });
    }

    return {
        openCreateOrganisationModal,
        openOrganisationEditForm,
        organisationDetailsHtml,
        organisationCodeRow,
        addOrganisationLanguageRow,
        deleteOrganisationLanguageRow,
        switchDraftOrganisationLanguage,
        organisationPayloadFromForm,
        submitOrganisationForm,
        deleteCurrentOrganisation,
        organisationLabel,
        organisationValue,
        organisationOptionEntries,
        initEventListeners
    };

})();
