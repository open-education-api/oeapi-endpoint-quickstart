window.SurfView = window.SurfView || {};

SurfView.Offerings = (() => {

    function addOfferingFormHtml(offering) {
        const modeOfDelivery = Array.isArray(offering.modeOfDelivery) ? offering.modeOfDelivery[0] : offering.modeOfDelivery;
        const academicSessionId = academicSessionIdValue(offering) || academicSessionIdValue(offering.academicSession);
        const fragment = document.createDocumentFragment();
        fragment.append(
            ...primaryCodeFormFields({
                idLabel: 'Offering ID',
                idName: 'offeringId',
                idValue: offeringId(offering),
                primaryCode: offering.primaryCode
            }),
            fieldLabelHtml('Abbreviation', 'text', 'abbreviation', textValueLocalizedAttribute(offering.abbreviation) || ''),
            languageSpecificSectionFragment({
                activeLanguage: SurfView.State.draftOffering.language,
                buttonAttribute: 'data-offering-language-code',
                label: 'Offering language',
                fields: [
                    fieldLabelHtml('Name', 'text', 'name', SurfView.State.draftOffering.languageValues[SurfView.State.draftOffering.language]?.name || '', {required: true}),
                    fieldLabelHtml('Description', 'textarea', 'description', SurfView.State.draftOffering.languageValues[SurfView.State.draftOffering.language]?.description || '', {required: true}, 'full-width')
                ]
            }),
            fieldLabelHtml('Teaching language', 'select', 'teachingLanguage', offering.teachingLanguage || 'eng', {
                options: Constants.enumOptions.teachingLanguage,
                showValue: true
            }),
            fieldLabelHtml('Mode of delivery', 'datalist', 'modeOfDelivery', modeOfDelivery || 'online', {
                options: Constants.enumOptions.modeOfDelivery
            }),
            fieldLabelHtml('Start date', 'date', 'startDate', offering.startDate || '', {required: true}),
            fieldLabelHtml('End date', 'date', 'endDate', offering.endDate || '', {required: true}),
            fieldLabelHtml('Enrollment start', 'date', 'enrollStartDate', offering.enrollStartDate || ''),
            fieldLabelHtml('Enrollment end', 'date', 'enrollEndDate', offering.enrollEndDate || ''),
            fieldLabelHtml('Min students', 'number', 'minNumberStudents', offering.minNumberStudents ?? '', {min: 0}),
            fieldLabelHtml('Max students', 'number', 'maxNumberStudents', offering.maxNumberStudents ?? '', {min: 0}),
            fieldLabelHtml('Result expected', 'select', 'resultExpected', String(offering.resultExpected ?? true), {
                options: Constants.booleanOptions
            }),
            fieldLabelHtml('Result value type', 'select', 'resultValueType', offering.resultValueType || '1-10', {
                options: Constants.enumOptions.resultValueType
            }),
            fieldLabelHtml('Link', 'url', 'link', offering.link || '', {placeholder: 'https://'}, 'full-width'),
            fieldLabelHtml('Organization', 'select', 'organization', organizationIdValue(offering.organization), {
                options: SurfView.Organizations.organizationOptionEntries(organizationIdValue(offering.organization))
            }),
            fieldLabelHtml('Academic session', 'select', 'academicSessionId', academicSessionId, {
                options: SurfView.AcademicSessions.academicSessionOptionEntries(academicSessionId)
            }),
            nestedSummaryPanelFragment('Price information', 'Add price', 'price', 'offering-price-summary'),
            offeringAddressListPanel.panel(),
            consumerFormPanel(offering),
            cloneTemplate('offering-form-actions-template', null, {
                cancelAttribute: 'data-cancel-offering',
                statusId: 'offering-form-status',
                submitText: offeringForm.dataset.offeringMode === 'edit' ? 'Save' : 'Add'
            }),
        );
        return fragment;
    }

    function consumerFormPanel(offering) {
        const {consumer} = surfConsumerSelection(offering, {includeSingleConsumer: true, fallbackFirst: true});
        const {alliance} = surfAllianceSelection(consumer, {names: Constants.offeringConsumer.allianceNames, fallbackFirst: true});
        return consumerPanelFragment(
            {consumerKey: Constants.offeringConsumer.consumerKey},
            [
                fieldLabelHtml('Alliance name', 'select', 'consumerAllianceName', consumerAllianceName(alliance.name), {
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
                    options: Constants.booleanOptions
                })
            ],
            node => node.removeAttribute('data-original-consumer-index')
        );
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
                activeLanguage: SurfView.State.draftOffering.nestedPriceLanguage,
                buttonAttribute: 'data-price-language-code',
                label: 'Price language',
                fields: [
                    fieldLabelHtml('Display amount', 'text', 'displayAmount', SurfView.State.draftOffering.nestedPriceLanguageValues[SurfView.State.draftOffering.nestedPriceLanguage]?.displayAmount || '', {placeholder: '€340.84'}, 'full-width'),
                    fieldLabelHtml('Description', 'textarea', 'description', SurfView.State.draftOffering.nestedPriceLanguageValues[SurfView.State.draftOffering.nestedPriceLanguage]?.description || '', {placeholder: 'Describe what this price covers'}, 'full-width')
                ]
            }),
            nestedFormActionsFragment('Add')
        );
        return fragment;
    }

    /** Returns the display label for an offering. */
    function offeringLabel(offering, index) {
        return detailTextValue(offering.name)
            || offering.offeringId
            || offering.courseOfferingId
            || `Offering ${index + 1}`;
    }

    function offeringId(offering) {
        return offering.offeringId || offering.courseOfferingId || offering.programOfferingId || '';
    }

    /** Builds an offering read-only detail section. */
    function offeringSectionHtml(offering, index) {
        const label = offeringLabel(offering, index);
        const id = offeringId(offering);
        const fields = [
            ['Name', detailTextValue(offering.name)],
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
        const article = createElement(`article#offering-${index + 1}.offering-card`);
        const head = createElement('div.detail-section-head');
        head.append(textElement('h3', label));
        if (id && !isReadOnlyMode()) {
            const actions = createElement('div.offering-card-actions');
            const editButton = createElement('button.detail-action-button', {
                text: 'Edit',
                dataset: {editOfferingIndex: String(index)},
                attributes: {type: 'button'}
            });
            const deleteButton = createElement('button.detail-action-button', {
                text: 'Delete',
                dataset: {deleteOfferingId: id},
                attributes: {type: 'button'}
            });
            actions.append(editButton, deleteButton);
            head.append(actions);
        }
        const details = createElement('div.detail-grid');
        details.append(...fields.map(([fieldLabel, value]) => detailFieldElement(fieldLabel, value)));
        article.append(head, details);
        return article;
    }

    function locationValue(offering) {
        if (offering.location) {
            return typeof offering.location === 'string'
                ? offering.location
                : detailTextValue(offering.location.name) || offering.location.roomId || offering.location.buildingId || '';
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
            const displayAmount = detailTextValue(price.displayAmount)
                || [price.amount, price.currency].filter(Boolean).join(' ');
            return [displayAmount, price.costType].filter(Boolean).join(' - ');
        }).filter(Boolean).join(', ');
    }

    function updateAddOfferingUrl(kind, id) {
        const tabKey = tabKeyByKind(kind);
        const tab = tabByKind(kind);
        if (!tabKey || !tab) {
            return;
        }

        const url = new URL(window.location.href);
        url.searchParams.set('tab', tabKey);
        url.searchParams.delete('section');
        url.searchParams.set(tab.idField, id);
        entityIdParams().forEach(param => {
            if (param !== tab.idField) {
                url.searchParams.delete(param);
            }
        });
        url.searchParams.set('action', 'addOffering');
        window.history.pushState({kind, id, action: 'addOffering'}, '', url);
    }

    async function openAddOfferingModal(kind, id) {
        if (isReadOnlyMode()) {
            return;
        }
        updateAddOfferingUrl(kind, id);
        const entity = entityMap(kind).get(String(id)) || SurfView.State.currentModalEntity || {};
        return openOfferingModal(kind, id, defaultOfferingForEntity(kind, id, entity), {mode: 'add'});
    }

    async function openEditOfferingModal(indexValue) {
        if (isReadOnlyMode()) {
            return;
        }
        const index = Number(indexValue);
        if (!Number.isInteger(index) || index < 0 || index >= SurfView.State.currentModalOfferings.length) {
            return;
        }

        const parentId = entityId(SurfView.State.currentModalKind, SurfView.State.currentModalEntity);
        await openOfferingModal(SurfView.State.currentModalKind, parentId, SurfView.State.currentModalOfferings[index], {mode: 'edit'});
    }

    async function openOfferingModal(kind, id, offering, options = {}) {
        if (isReadOnlyMode()) {
            return;
        }
        await preloadLookups([
            loadOrganizations,
            loadAcademicSessions
        ]);
        const mode = options.mode || 'add';
        resetDraftOffering();
        SurfView.State.draftOffering.addresses = Array.isArray(offering.addresses) ? structuredClone(offering.addresses) : [];
        SurfView.State.draftOffering.priceInformation = Array.isArray(offering.priceInformation) ? structuredClone(offering.priceInformation) : [];
        initializeDraftOfferingLanguages(offering);

        offeringModal.title.textContent = `${mode === 'edit' ? 'Edit' : 'Add'} ${titleCase(kind)} offering`;
        offeringModal.subtitle.textContent = `${titleCase(kind)} ID: ${id}`;
        offeringForm.dataset.offeringKind = kind;
        offeringForm.dataset.parentId = id;
        offeringForm.dataset.offeringMode = mode;
        offeringForm.dataset.offeringId = offeringId(offering);
        offeringForm.replaceChildren(addOfferingFormHtml(offering));
        renderNestedOfferingSummaries();
        openManagedModal(offeringModal, {
            initialFocus: () => offeringForm.querySelector('input[name="primaryCode"]')
        });
    }

    function defaultOfferingForEntity(kind, id, entity) {
        const baseName = textValueLocalizedAttribute(entity.name) || id;
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
            name: languageValue(`${baseName} offering`, SurfView.State.currentModalLanguage || 'en-GB'),
            abbreviation: `${String(baseName).slice(0, 18)} offer`,
            description: languageValue(`Offering for ${baseName}`, SurfView.State.currentModalLanguage || 'en-GB'),
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

    /** Closes the offering add or edit modal. */
    function closeAddOfferingModal(options = {}) {
        closeOfferingJsonPreview();
        closeErrorDetailsModal();
        closeManagedModal(offeringModal);
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
        if (isReadOnlyMode()) {
            return;
        }
        const kind = offeringForm.dataset.offeringKind;
        const parentId = offeringForm.dataset.parentId;
        if (!kind || !parentId) {
            return;
        }

        openJsonPreview(
            'Offering JSON',
            'Payload that will be submitted when adding or saving this offering',
            () => offeringPayloadFromForm(kind, parentId),
            {
                status: document.getElementById('offering-form-status'),
                errorMessage: 'Unable to preview offering'
            }
        );
    }

    /** Closes the offering JSON preview modal. */
    function closeOfferingJsonPreview() {
        closeJsonPreview();
    }

    const offeringDraftLanguageController = createDraftLanguageController({
        prefix: 'offering',
        state: () => SurfView.State.draftOffering,
        form: () => offeringForm,
        fields: () => ['name', 'description'],
        languages: () => SurfView.State.currentModalLanguages.length ? [...SurfView.State.currentModalLanguages] : ['en-GB'],
        value: (offering, field, language) => textValueForLanguage(offering[field], language),
        afterSwitch: form => {
            if (form.elements.organization) {
                const selectedOrganizationId = form.elements.organization.value;
                form.elements.organization.replaceChildren(
                    ...enumOptionsHtml(SurfView.Organizations.organizationOptionEntries(selectedOrganizationId), selectedOrganizationId)
                );
                form.elements.organization.value = selectedOrganizationId;
            }
            if (form.elements.academicSessionId) {
                const selectedAcademicSessionId = form.elements.academicSessionId.value;
                form.elements.academicSessionId.replaceChildren(
                    ...enumOptionsHtml(SurfView.AcademicSessions.academicSessionOptionEntries(selectedAcademicSessionId), selectedAcademicSessionId)
                );
                form.elements.academicSessionId.value = selectedAcademicSessionId;
            }
            renderNestedOfferingSummaries();
        }
    });

    const nestedPriceDraftLanguageController = createDraftLanguageController({
        prefix: 'price',
        state: () => SurfView.State.draftOffering,
        form: () => nestedOfferingForm,
        languageKey: 'nestedPriceLanguage',
        valuesKey: 'nestedPriceLanguageValues',
        fields: () => ['description', 'displayAmount'],
        languages: () => SurfView.State.draftOffering.languages.length
            ? SurfView.State.draftOffering.languages
            : [SurfView.State.draftOffering.language || 'en-GB'],
        value: (price, field, language) => textValueForLanguage(priceLanguageFieldValue(price, field), language),
        getLanguages: draft => Object.keys(draft.nestedPriceLanguageValues),
        setLanguages: () => {},
        currentLanguage: () => SurfView.State.draftOffering.language,
        canSave: form => form.dataset.nestedType === 'price'
    });

    const offeringAddressListPanel = createDraftAddressListPanel({
        summaryId: 'offering-address-summary',
        items: () => SurfView.State.draftOffering.addresses,
        languages: () => SurfView.State.draftOffering.languages,
        language: () => SurfView.State.draftOffering.language,
        subtitle: () => offeringModal.subtitle.textContent,
        editAttribute: 'data-edit-address-index',
        deleteAttribute: 'data-delete-address-index'
    });

    function offeringAddressTarget() {
        return offeringAddressListPanel.target();
    }

    function initializeDraftOfferingLanguages(offering) {
        offeringDraftLanguageController.initialize(offering);
    }

    function saveDraftOfferingLanguageFields() {
        offeringDraftLanguageController.save();
    }

    function switchDraftOfferingLanguage(language) {
        offeringDraftLanguageController.switchLanguage(language);
    }

    function openNestedOfferingModal(type, options = {}) {
        if (type === 'address') {
            openNestedAddressModal(offeringAddressTarget(), options);
            return;
        }
        openNestedModal({
            type: 'price',
            itemName: 'price information',
            items: SurfView.State.draftOffering.priceInformation,
            index: options.index,
            initialize: initializeDraftNestedPriceLanguages,
            formHtml: priceFormHtml,
            subtitle: offeringModal.subtitle.textContent
        });
    }

    function initializeDraftNestedPriceLanguages(price = {}) {
        nestedPriceDraftLanguageController.initialize(price);
    }

    registerNestedModalType('price', {
        submit: () => {
            nestedPriceDraftLanguageController.save();
            storeNestedItem(SurfView.State.draftOffering.priceInformation, () => priceItemFromNestedForm());
            renderNestedOfferingSummaries();
        },
        reset: () => {
            SurfView.State.draftOffering.nestedPriceLanguage = 'en-GB';
            SurfView.State.draftOffering.nestedPriceLanguageValues = {};
        },
        clickHandlers: {
            '[data-price-language-code]': button => nestedPriceDraftLanguageController.switchLanguage(button.dataset.priceLanguageCode)
        }
    });

    function priceItemFromNestedForm() {
        const data = new FormData(nestedOfferingForm);
        const value = name => String(data.get(name) || '').trim();
        return removeEmptyPayloadValues({
            costType: value('costType'),
            amount: value('amount'),
            vatAmount: value('vatAmount'),
            amountWithoutVat: value('amountWithoutVat'),
            currency: value('currency') || 'EUR',
            ext: {
                description: nestedPriceLanguageValues('description')
            },
            displayAmount: nestedPriceLanguageValues('displayAmount')
        });
    }

    function priceLanguageFieldValue(price, field) {
        if (field === 'description') {
            return price.ext?.description || price.description;
        }

        return price[field];
    }

    function nestedPriceLanguageValues(field) {
        return Object.entries(SurfView.State.draftOffering.nestedPriceLanguageValues)
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
                SurfView.State.draftOffering.priceInformation,
                item => [textValueForLanguage(item.displayAmount, SurfView.State.draftOffering.language) || `${item.amount || '-'} ${item.currency || ''}`.trim(), item.costType].filter(Boolean).join(' - '),
                'No price information',
                {
                    editAttribute: 'data-edit-price-index',
                    deleteAttribute: 'data-delete-price-index',
                    itemName: 'price information'
                }
            ));
        }
        if (addressSummary) {
            offeringAddressListPanel.render();
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


    /** Renders an offering form error message. */
    function renderOfferingFormError(status, error, fallbackMessage) {
        status.classList.add('error');
        status.replaceChildren();

        const message = errorSummary(error, fallbackMessage);
        const messageElement = createElement('span.offering-form-status-text', {text: message});
        status.append(messageElement);

        if (errorDetailsText(error)) {
            const detailsButton = createElement('button.error-maximize-button', {
                text: 'Maximize',
                dataset: {openErrorDetails: ''},
                attributes: {type: 'button'}
            });
            detailsButton.requestError = error;
            detailsButton.requestErrorSummary = message;
            status.append(detailsButton);
        }
    }

    async function submitOfferingForm(event) {
        event.preventDefault();
        if (isReadOnlyMode()) {
            closeAddOfferingModal();
            return;
        }
        const status = document.getElementById('offering-form-status');
        const submitButton = offeringForm.querySelector('button[type="submit"]');
        const kind = offeringForm.dataset.offeringKind;
        const parentId = offeringForm.dataset.parentId;
        const mode = offeringForm.dataset.offeringMode || 'add';

        await submitEntity({
            status,
            submitButton,
            payload: () => offeringPayloadFromForm(kind, parentId),
            endpoint: payload => mode === 'edit'
                ? `/offerings/${encodeURIComponent(offeringId(payload))}`
                : kind === 'program' ? '/offerings/programOffering' : '/offerings',
            method: mode === 'edit' ? 'PUT' : 'POST',
            savingText: mode === 'edit' ? 'Saving...' : 'Adding...',
            successText: mode === 'edit' ? 'Saved' : 'Added',
            errorMessage: mode === 'edit' ? 'Unable to save offering' : 'Unable to add offering',
            onError: error => renderOfferingFormError(status, error, mode === 'edit' ? 'Unable to save offering' : 'Unable to add offering'),
            onSuccess: async () => {
                await refreshCurrentModalOfferings(kind, parentId);
                closeAddOfferingModal();
            }
        });
    }

    function offeringPayloadFromForm(kind, parentId) {
        saveDraftOfferingLanguageFields();
        if (!validateDraftLanguageRequiredField({
            draft: SurfView.State.draftOffering,
            form: offeringForm,
            switchLanguage: switchDraftOfferingLanguage
        })) {
            return null;
        }
        const formData = new FormData(offeringForm);
        const value = name => String(formData.get(name) || '').trim();
        const numberValue = name => value(name) ? Number(value(name)) : null;
        const payload = {
            offeringId: value('offeringId'),
            primaryCode: primaryCodePayloadFromFormData(formData),
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
            priceInformation: SurfView.State.draftOffering.priceInformation,
            addresses: SurfView.State.draftOffering.addresses,
            consumers: [consumerPayloadFromForm(formData)]
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
        return SurfView.State.draftOffering.languages
            .map(language => ({
                language,
                value: String(SurfView.State.draftOffering.languageValues[language]?.[field] || '').trim()
            }))
            .filter(item => item.value);
    }

    async function refreshCurrentModalOfferings(kind, id) {
        SurfView.State.currentModalOfferings = await loadEntityOfferings(kind, id);
        SurfView.State.currentModalLanguages = collectLanguages({course: SurfView.State.currentModalEntity, offerings: SurfView.State.currentModalOfferings});
        modalBody.replaceChildren(SurfView.Module.moduleDetailsHtml(SurfView.State.currentModalKind, SurfView.State.currentModalEntity, {offerings: SurfView.State.currentModalOfferings}));
        renderEntityList();
    }

    async function deleteOffering(button) {
        const offeringId = button.dataset.deleteOfferingId;
        const parentId = entityId(SurfView.State.currentModalKind, SurfView.State.currentModalEntity);
        if (!offeringId || !parentId) {
            return;
        }

        await deleteCurrentEntity({
            kind: 'offering',
            id: offeringId,
            endpoint: id => `/offerings/${encodeURIComponent(id)}`,
            confirmMessage: 'Are you sure you want to delete this offering?',
            cacheMap: null,
            onSuccess: async () => {
                await refreshCurrentModalOfferings(SurfView.State.currentModalKind, parentId);
            }
        });
    }

    /** Loads offerings for a course or program. */
    async function loadEntityOfferings(kind, id) {
        if (!id) {
            return [];
        }

        try {
            const tab = tabByKind(kind);
            if (!tab?.offeringsEndpoint) {
                return [];
            }
            return await loadAllEndpointItems(tab.offeringsEndpoint(id));
        } catch (error) {
            return [];
        }
    }

    /** Registers offering event handlers. */
    function initEventListeners() {
        offeringModal.close.addEventListener('click', () => closeAddOfferingModal());
        addBackdropDismissListener(offeringModal, closeAddOfferingModal);
        offeringJsonPreviewButton.addEventListener('click', openOfferingJsonPreview);
        jsonPreviewModal.close.addEventListener('click', closeOfferingJsonPreview);
        addBackdropDismissListener(jsonPreviewModal, closeOfferingJsonPreview);

        offeringForm.addEventListener('submit', submitOfferingForm);
        delegateClick(offeringForm, {
            '[data-cancel-offering]': () => closeAddOfferingModal(),
            '[data-offering-language-code]': button => switchDraftOfferingLanguage(button.dataset.offeringLanguageCode),
            '[data-delete-price-index]': button => deleteDraftNestedItem(SurfView.State.draftOffering.priceInformation, button.dataset.deletePriceIndex),
            '[data-edit-price-index]': button => openNestedOfferingModal('price', {index: button.dataset.editPriceIndex}),
            '[data-delete-address-index]': button => offeringAddressListPanel.deleteItem(button.dataset.deleteAddressIndex),
            '[data-edit-address-index]': button => openNestedOfferingModal('address', {index: button.dataset.editAddressIndex}),
            '[data-open-nested-offering]': button => openNestedOfferingModal(button.dataset.openNestedOffering)
        });

        delegateClick(modalBody, {
            '[data-delete-offering-id]': button => deleteOffering(button),
            '[data-edit-offering-index]': button => openEditOfferingModal(button.dataset.editOfferingIndex),
            '[data-add-offering-kind][data-add-offering-id]': button => openAddOfferingModal(button.dataset.addOfferingKind, button.dataset.addOfferingId)
        });
    }

    return {
        closeAddOfferingModal,
        closeOfferingJsonPreview,
        loadEntityOfferings,
        offeringLabel,
        offeringSectionHtml,
        renderOfferingFormError,
        initEventListeners
    };

})();
