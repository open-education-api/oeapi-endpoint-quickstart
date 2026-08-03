window.SurfView = window.SurfView || {};

SurfView.Module = (() => {

    function languageHrefQuery(kind, entityId, language) {
        const tab = tabByKind(kind);
        const params = new URLSearchParams(window.location.search);
        entityIdParams().forEach(param => {
            if (param !== tab?.idField) {
                params.delete(param);
            }
        });
        if (tab) {
            params.set(tab.idField, entityId || '');
        }
        params.set('lang', language);
        return params.toString();
    }

    /** Builds a course or program read-only detail view. */
    function moduleDetailsHtml(kind, module, options = {}) {
        const offerings = options.offerings || [];
        const loadingOfferings = options.loadingOfferings || false;
        const fields = [
            ['Name', detailTextValue(module.name)],
            ['Code', plainCodeValue(module.primaryCode)],
            ['Level', module.level],
            ['Study load', studyLoadValue(module.studyLoad)],
            ['Abbreviation', module.abbreviation],
            [kind === 'program' ? 'Program type' : 'Teaching language', kind === 'program' ? module.programType : module.teachingLanguage],
            ['Delivery', listValue(module.modeOfDelivery)],
            ['First start date', module.firstStartDate],
            ['Organization', SurfView.Organizations.organizationValue(module.organization)],
            ['Coordinators', coordinatorLabels(module)],
            ['Link', module.link]
        ];
        const description = detailTextValue(module.description);
        const admissionRequirements = filterLanguageItems(module.admissionRequirements);
        const learningOutcomes = filterLanguageItems(module.learningOutcomes);
        const enrollment = filterLanguageItems(module.enrollment);
        const assessment = filterLanguageItems(module.assessment);
        const qualificationRequirements = filterLanguageItems(module.qualificationRequirements);
        const hasAddresses = Array.isArray(module.addresses) && module.addresses.length > 0;
        const hasAdmissionRequirements = admissionRequirements.length > 0;
        const hasLearningOutcomes = learningOutcomes.length > 0;
        const hasEnrollment = enrollment.length > 0;
        const hasAssessment = assessment.length > 0;
        const hasQualificationRequirements = qualificationRequirements.length > 0;
        const hasRequirements = hasAdmissionRequirements || hasQualificationRequirements;
        const hasConsumers = Array.isArray(module.consumers) && module.consumers.length > 0;
        const moduleDetailId = entityId(kind, module);
        const layout = cloneTemplate('detail-layout-template', null, {navLabel: `${titleCase(kind)} detail sections`});
        const nav = layout.querySelector('.detail-nav');
        nav.append(
            detailNavSectionElement('Language', languageDetailNavElement(code => `?${languageHrefQuery(kind, entityId(kind, module), code)}`)),
            detailNavSectionElement(titleCase(kind), moduleSectionLinks({
                description,
                hasRequirements,
                hasLearningOutcomes,
                hasEnrollment,
                hasAssessment,
                hasConsumers
            })),
            detailNavSectionElement('Addresses', moduleAddressNavElement(module.addresses)),
            detailNavSectionElement('Offerings', moduleOfferingNavElement(offerings, loadingOfferings))
        );

        const content = layout.querySelector('.detail-content');
        const overview = detailSectionElement('course-overview', 'Overview');
        const details = detailGridElement();
        details.append(...fields.map(([label, value]) => detailFieldElement(label, value)));
        overview.append(details);
        content.append(overview);

        if (description) {
            const section = detailSectionElement('course-description', 'Description');
            section.append(detailValueElement(description));
            content.append(section);
        }
        if (hasRequirements) {
            content.append(requirementsHtml(admissionRequirements, qualificationRequirements));
        }
        [
            [hasLearningOutcomes, 'course-learning-outcomes', 'Learning outcomes', learningOutcomes],
            [hasEnrollment, 'course-enrollment', 'Enrollment', enrollment],
            [hasAssessment, 'course-assessment', 'Assessment', assessment]
        ].forEach(([visible, id, heading, items]) => {
            if (visible) {
                const section = detailSectionElement(id, heading);
                section.append(languageTextSectionHtml(items));
                content.append(section);
            }
        });
        if (hasConsumers) {
            const section = detailSectionElement('course-consumers', 'Consumers');
            section.append(consumersHtml(module.consumers));
            content.append(section);
        }
        if (hasAddresses) {
            const section = detailSectionElement('course-addresses', 'Addresses');
            section.append(addressesHtml(module.addresses));
            content.append(section);
        }
        content.append(moduleOfferingsSection(kind, moduleDetailId, offerings, loadingOfferings));
        return layout;
    }

    function moduleSectionLinks(state) {
        const fragment = document.createDocumentFragment();
        [
            ['course-overview', 'Overview', true],
            ['course-description', 'Description', Boolean(state.description)],
            ['course-requirements', 'Requirements', state.hasRequirements],
            ['course-learning-outcomes', 'Learning outcomes', state.hasLearningOutcomes],
            ['course-enrollment', 'Enrollment', state.hasEnrollment],
            ['course-assessment', 'Assessment', state.hasAssessment],
            ['course-consumers', 'Consumers', state.hasConsumers]
        ].forEach(([id, label, visible]) => {
            if (visible) {
                fragment.append(anchorElement(`#${id}`, label));
            }
        });
        return fragment;
    }

    function moduleAddressNavElement(addresses) {
        if (!Array.isArray(addresses) || !addresses.length) {
            return textElement('span', 'No addresses', 'detail-nav-empty');
        }
        const fragment = document.createDocumentFragment();
        addresses.forEach((address, index) => fragment.append(anchorElement(`#course-address-${index + 1}`, addressLabel(address, index))));
        return fragment;
    }

    function moduleOfferingNavElement(offerings, loading) {
        if (loading) {
            return textElement('span', 'Loading offerings...', 'detail-nav-empty');
        }
        if (!offerings.length) {
            return textElement('span', 'No offerings', 'detail-nav-empty');
        }
        const fragment = document.createDocumentFragment();
        offerings.forEach((offering, index) => fragment.append(anchorElement(`#offering-${index + 1}`, SurfView.Offerings.offeringLabel(offering, index))));
        return fragment;
    }

    function moduleOfferingsSection(kind, id, offerings, loading) {
        const section = detailSectionElement('course-offerings');
        const head = cloneTemplate('module-offerings-section-head-template', node => {
            const button = node.querySelector('[data-add-offering-kind]');
            if (!id || isReadOnlyMode()) {
                button.remove();
                return;
            }

            button.dataset.addOfferingKind = kind;
            button.dataset.addOfferingId = id;
            button.dataset.offeringsEndpoint = tabByKind(kind)?.offeringsEndpoint?.(id) || '';
        });
        section.append(head);
        if (loading) {
            section.append(textElement('p', 'Loading offerings...', 'muted'));
        } else if (offerings.length) {
            offerings.forEach((offering, index) => section.append(SurfView.Offerings.offeringSectionHtml(offering, index)));
        } else {
            section.append(textElement('p', 'No offerings', 'muted'));
        }
        return section;
    }

    /** Filters nested language values to the active modal language. */
    function localizeLanguageValues(value) {
        if (Array.isArray(value)) {
            const items = SurfView.State.currentModalLanguage && isLanguageValueList(value)
                ? value.filter(item => item && item.language === SurfView.State.currentModalLanguage)
                : value;
            return items.map(item => localizeLanguageValues(item));
        }

        if (!value || typeof value !== 'object') {
            return value;
        }

        return Object.fromEntries(
            Object.entries(value).map(([key, child]) => [key, localizeLanguageValues(child)])
        );
    }

    function languageTextSectionHtml(items) {
        const flattened = flattenLanguageItems(items);
        const fragment = document.createDocumentFragment();
        flattened.forEach(item => {
            if (item && typeof item === 'object' && 'value' in item) {
                fragment.append(detailValueElement(item.value || ''));
            } else {
                fragment.append(jsonBoxElement(localizeLanguageValues(item)));
            }
        });
        return fragment;
    }

    function requirementsHtml(admissionRequirements, qualificationRequirements) {
        const section = detailSectionElement('course-requirements', 'Requirements');
        if (admissionRequirements.length) {
            section.append(textElement('h4', 'Admission requirements'), languageTextSectionHtml(admissionRequirements));
        }
        if (qualificationRequirements.length) {
            section.append(textElement('h4', 'Qualification requirements'), languageTextSectionHtml(qualificationRequirements));
        }
        return section;
    }

    function flattenLanguageItems(items) {
        return items.flatMap(item => {
            if (Array.isArray(item)) {
                return flattenLanguageItems(item);
            }

            return [item];
        });
    }

    function consumersHtml(consumers) {
        const fragment = document.createDocumentFragment();
        consumers.forEach((consumer, index) => {
            const article = createElement('article.offering-card');
            if (typeof consumer === 'string') {
                article.append(detailValueElement(consumer));
            } else {
                const label = detailTextValue(consumer.name) || consumer.consumerKey || consumer.consumerId || `Consumer ${index + 1}`;
                article.append(textElement('h3', label), jsonBoxElement(localizeLanguageValues(consumer)));
            }
            fragment.append(article);
        });
        return fragment;
    }

    function moduleEditFormHtml(kind, module, mode = 'edit') {
        moduleDraftLanguageController.initialize({kind, module});
        const id = entityId(kind, module);
        const studyLoad = module.studyLoad || {};
        const studyLoadUnit = studyLoad.studyLoadUnit || studyLoad.unit || 'ects';
        const modeOfDelivery = Array.isArray(module.modeOfDelivery) ? module.modeOfDelivery[0] : module.modeOfDelivery;
        const form = entityEditFormElement(kind, mode);

        const fields = [
            ...primaryCodeFormFields({
                idLabel: `${titleCase(kind)} ID`,
                idName: `${kind}Id`,
                idValue: id,
                primaryCode: module.primaryCode
            }),
            moduleLanguagePanel(kind, module),
            fieldLabelHtml('Abbreviation', 'text', 'abbreviation', module.abbreviation || '', {
                required: kind === 'program'
            })
        ];

        if (kind === 'program') {
            fields.push(fieldLabelHtml('Program type', 'select', 'programType', module.programType || 'program', {
                required: true,
                options: Constants.enumOptions.programType
            }));
        }

        const coordinatorsPanel = moduleCoordinatorsPanel(module);
        coordinatorsPanel.dataset.moduleFormSection = 'coordinators';

        fields.push(
            fieldLabelHtml('Teaching language', 'select', 'teachingLanguage', module.teachingLanguage || 'eng', {
                options: Constants.enumOptions.teachingLanguage,
                showValue: true
            }),
            fieldLabelHtml('Level', 'datalist', 'level', module.level || '', {
                required: kind === 'program',
                options: Constants.enumOptions.level
            }),
            fieldLabelHtml('Study load value', 'number', 'studyLoadValue', studyLoad.value ?? studyLoad.amount ?? '', {min: 0, step: 0.01}),
            fieldLabelHtml('Study load unit', 'select', 'studyLoadUnit', studyLoadUnit, {
                options: Constants.enumOptions.studyLoadType
            }),
            fieldLabelHtml('Mode of delivery', 'datalist', 'modeOfDelivery', modeOfDelivery || '', {
                options: Constants.enumOptions.modeOfDelivery
            }),
            fieldLabelHtml('First start date', 'date', 'firstStartDate', module.firstStartDate || ''),
            fieldLabelHtml('Sector', 'select', 'sector', module.sector || '', {
                options: [['', 'None'], ...Constants.enumOptions.sector]
            }),
            fieldLabelHtml('Organization', 'select', 'organization', organizationIdValue(module.organization), {
                options: SurfView.Organizations.organizationOptionEntries(organizationIdValue(module.organization))
            }),
            fieldLabelHtml('Link', 'url', 'link', module.link || '', {placeholder: 'https://'}, 'full-width'),
            coordinatorsPanel,
            moduleConsumersPanel(module),
            entityEditFormActions(mode === 'edit' ? 'Save' : 'Add')
        );
        form.append(...fields);
        return form;
    }

    function coordinatorIds(module) {
        return (Array.isArray(module?.coordinators) ? module.coordinators : [])
            .map(coordinator => typeof coordinator === 'string' ? coordinator : entityId('person', coordinator))
            .filter(Boolean)
            .map(String);
    }

    function coordinatorLabels(module) {
        const labels = coordinatorIds(module).map(id => SurfView.Persons.personLabelById(id));
        return labels.length ? labels.join(', ') : '';
    }

    function moduleCoordinatorsPanel(module) {
        return cloneTemplate('module-coordinators-panel-template', section => {
            const list = section.querySelector('#module-coordinator-list');
            coordinatorIds(module).forEach(id => list.append(moduleCoordinatorChip(id)));
            refreshCoordinatorSelect(section);
        });
    }

    function moduleCoordinatorChip(id) {
        const label = SurfView.Persons.personLabelById(id);
        const buttonLabel = `Remove ${label}`;
        return cloneTemplate('module-coordinator-chip-template', null, {label, id, buttonLabel});
    }

    function refreshCoordinatorSelect(scope = document) {
        const panel = scope.closest?.('.module-coordinators-panel') || scope.querySelector?.('.module-coordinators-panel') || scope;
        const select = panel.querySelector?.('[data-module-coordinator-select]');
        const addButton = panel.querySelector?.('[data-add-module-coordinator]');
        if (!select || !addButton) {
            return;
        }

        const options = SurfView.Persons.personOptionEntries({
            excludedPersonIds: moduleCoordinatorValues(panel)
        });

        select.replaceChildren(...enumOptionsHtml(options, ''));
        select.value = '';
        addButton.disabled = options.length <= 1;
    }

    function addModuleCoordinator(button) {
        const panel = button.closest('.module-coordinators-panel');
        const select = panel?.querySelector('[data-module-coordinator-select]');
        const list = panel?.querySelector('#module-coordinator-list');
        const id = select?.value || '';
        if (!panel || !list || !id) {
            return;
        }

        const duplicate = moduleCoordinatorValues(panel).includes(String(id));
        if (!duplicate) {
            list.append(moduleCoordinatorChip(String(id)));
        }
        refreshCoordinatorSelect(panel);
    }

    function deleteModuleCoordinator(button) {
        const panel = button.closest('.module-coordinators-panel');
        button.closest('.module-coordinator-chip')?.remove();
        if (panel) {
            refreshCoordinatorSelect(panel);
        }
    }

    function moduleConsumersPanel(module) {
        const {consumer, index: consumerIndex} = surfConsumerSelection(module);
        const {alliance, index: allianceIndex} = surfAllianceSelection(consumer, {fallbackFirst: true});

        return consumerPanelFragment(
            consumer,
            [moduleAllianceRow(alliance, allianceIndex)],
            node => node.classList.add('module-consumers-panel', 'module-consumer-row'),
            consumerIndex,
            {required: true}
        );
    }

    function moduleAllianceRow(alliance = {}, originalIndex = '') {
        const row = createElement('div.module-alliance-row', {
            dataset: {originalAllianceIndex: originalIndex}
        });

        row.append(
            fieldLabelHtml('Alliance name', 'text', 'allianceName', alliance.name || '', {required: true}),
            moduleArrayField('Themes', 'themes', alliance.themes),
            fieldLabelHtml('Selection', 'select', 'selection', booleanFormValue(alliance.selection), {
                options: Constants.booleanOptions
            }),
            fieldLabelHtml('Type', 'text', 'type', alliance.type || ''),
            moduleArrayField('Instructor names', 'instructorNames', alliance.instructorNames),
            fieldLabelHtml('Contact hours', 'number', 'contactHours', alliance.contactHours ?? '', {min: 0, step: 0.01}),
            fieldLabelHtml('Activities', 'textarea', 'activities', alliance.activities || '', {}, 'full-width'),
            fieldLabelHtml('Microcredential', 'text', 'microcredential', alliance.microcredential || ''),
            fieldLabelHtml('Target group', 'text', 'targetGroup', alliance.targetGroup || ''),
            fieldLabelHtml('Visible for own students', 'select', 'visibleForOwnStudents', booleanFormValue(alliance.visibleForOwnStudents), {
                options: Constants.booleanOptions
            }),
            fieldLabelHtml('Enrollment for own students', 'text', 'enrollmentForOwnStudents', alliance.enrollmentForOwnStudents || ''),
            fieldLabelHtml('Visible for guests', 'select', 'visibleForGuests', booleanFormValue(alliance.visibleForGuests), {
                options: Constants.booleanOptions
            }),
            fieldLabelHtml('Enrollment for guests', 'text', 'enrollmentForGuests', alliance.enrollmentForGuests || ''),
            moduleArrayField('Joint partner codes', 'jointPartnerCodes', alliance.jointPartnerCodes),
            fieldLabelHtml('Mode of delivery', 'datalist', 'allianceModeOfDelivery', alliance.modeOfDelivery || '', {
                options: Constants.enumOptions.modeOfDelivery
            }),
            fieldLabelHtml('Level', 'datalist', 'allianceLevel', alliance.level || '', {
                options: Constants.enumOptions.level
            })
        );
        return row;
    }

    function moduleArrayField(labelName, fieldName, values = []) {
        return cloneTemplate('module-array-field-template', label => {
            const editor = label.querySelector('.module-array-editor');

            const chips = editor.querySelector('.module-array-chips');
            (Array.isArray(values) ? values : []).forEach(value => {
                if (value !== null && value !== undefined && String(value).trim()) {
                    chips.append(moduleArrayChip(String(value).trim()));
                }
            });

            editor.querySelector('input').setAttribute('aria-label', `Add ${labelName.toLowerCase()}`);
        }, {labelName, fieldName});
    }

    function moduleArrayChip(value) {
        const buttonLabel = `Remove ${value}`;
        return cloneTemplate('module-array-chip-template', null, {value, buttonLabel});
    }

    function moduleLanguagePanel(kind, module) {
        const section = languageSpecificSectionFragment({
            activeLanguage: SurfView.State.draftEntity.language,
            buttonAttribute: 'data-module-language-code',
            label: 'Content language',
            languages: SurfView.State.draftEntity.languages,
            languageActions: language => draftLanguageActions('module', language),
            headerContent: cloneTemplate('module-language-controls-template'),
            fields: moduleLanguageFields(kind, SurfView.State.draftEntity.language)
        });
        section.classList.add('module-localized-panel');
        section.dataset.moduleLocalizedPanel = '';
        moduleDraftLanguageController.refresh(section);
        return section;
    }

    function moduleLanguageFields(kind, language) {
        const values = SurfView.State.draftEntity.languageValues[language]
            || Object.fromEntries(moduleLocalizedFields(kind).map(field => [field, '']));
        const fields = [
            fieldLabelHtml('Name', 'text', 'name', values.name || '', {required: true}, 'full-width'),
            fieldLabelHtml('Description', 'textarea', 'description', values.description || '', {
                required: true
            }, 'full-width'),
            fieldLabelHtml('Admission requirements', 'textarea', 'admissionRequirements', values.admissionRequirements || '', {}, 'full-width'),
            fieldLabelHtml('Qualification requirements', 'textarea', 'qualificationRequirements', values.qualificationRequirements || '', {}, 'full-width')
        ];
        if (kind === 'course') {
            fields.push(
                fieldLabelHtml('Enrollment', 'textarea', 'enrollment', values.enrollment || '', {}, 'full-width'),
                fieldLabelHtml('Assessment', 'textarea', 'assessment', values.assessment || '', {}, 'full-width'),
                fieldLabelHtml('Learning outcomes', 'textarea', 'learningOutcomes', values.learningOutcomes || '', {}, 'full-width')
            );
        }
        return fields;
    }

    function moduleLocalizedFields(kind) {
        const fields = ['name', 'description', 'admissionRequirements', 'qualificationRequirements'];
        return kind === 'course'
            ? [...fields, 'enrollment', 'assessment', 'learningOutcomes']
            : fields;
    }

    const moduleDraftLanguageController = createDraftLanguageController({
        prefix: 'module',
        state: () => SurfView.State.draftEntity,
        form: () => document.getElementById('entity-edit-form'),
        fields: source => moduleLocalizedFields(source?.kind || document.getElementById('entity-edit-form')?.dataset.entityKind || SurfView.State.currentModalKind),
        languageSource: source => source?.module,
        value: (source, field, language) => localizedFieldValue(source.module[field], language),
        renderPanel: () => {
            const form = document.getElementById('entity-edit-form');
            renderModuleLanguagePanel(form?.dataset.entityKind || SurfView.State.currentModalKind);
        },
        focusField: 'name'
    });

    function renderModuleLanguagePanel(kind) {
        const panel = document.querySelector('[data-module-localized-panel]');
        const replacement = moduleLanguagePanel(kind, SurfView.State.currentModalEntity || {});
        panel?.replaceWith(replacement);
    }

    function addModuleArrayValue(input) {
        const value = input.value.trim();
        const editor = input.closest('[data-module-array-field]');
        const chips = editor?.querySelector('.module-array-chips');
        if (!value || !chips) {
            return;
        }

        const duplicate = [...chips.querySelectorAll('.module-array-chip')]
            .some(chip => chip.dataset.value === value);
        if (!duplicate) {
            chips.append(moduleArrayChip(value));
        }
        input.value = '';
    }

    /** Opens the shared modal with a new course or program form. */
    async function openCreateModuleModal(kind) {
        if (isReadOnlyMode()) {
            return;
        }

        await preloadLookups([
            loadOrganizations,
            loadPersons
        ]);

        const module = defaultModule(kind);
        SurfView.State.currentModalKind = kind;
        SurfView.State.currentModalEntity = module;
        SurfView.State.currentModalOfferings = [];
        SurfView.State.currentModalLanguages = ['en-GB'];
        SurfView.State.currentModalLanguage = 'en-GB';
        resetDraftEntity();
        moduleModal.title.textContent = `Add ${kind}`;
        setEntityDeletePending(false);
        setEntityModalMode('form');
        modalBody.replaceChildren(moduleEditFormHtml(kind, module, 'create'));
        openManagedModal(moduleModal, {
            initialFocus: () => modalBody.querySelector('input[name="primaryCode"]')
        });
    }

    function defaultModule(kind) {
        const module = {
            [`${kind}Id`]: generateId(),
            primaryCode: {codeType: 'identifier', code: ''},
            name: [{language: 'en-GB', value: ''}],
            description: [],
            admissionRequirements: [],
            qualificationRequirements: [],
            teachingLanguage: 'eng',
            modeOfDelivery: [],
            studyLoad: {value: null, studyLoadUnit: 'ects'},
            consumers: [{
                consumerKey: Constants.offeringConsumer.consumerKey,
                alliances: [{
                    name: Constants.offeringConsumer.defaultAllianceName
                }]
            }]
        };

        if (kind === 'course') {
            module.enrollment = [];
            module.assessment = [];
            module.learningOutcomes = [];
        } else if (kind === 'program') {
            module.programType = 'program';
        }

        return module;
    }

    /** Opens the shared modal with a course or program edit form. */
    async function openModuleEditForm() {
        if (!SurfView.State.currentModalEntity || isReadOnlyMode()) {
            return;
        }

        await preloadLookups([
            loadOrganizations,
            loadPersons
        ]);

        resetDraftEntity();
        modalBody.replaceChildren(moduleEditFormHtml(SurfView.State.currentModalKind, SurfView.State.currentModalEntity, 'edit'));
        setEntityModalMode('form');
        modalBody.querySelector('.module-localized-panel input[name="name"]')?.focus();
    }

    /** Closes the course or program edit form. */
    function closeModuleEditForm() {
        if (!SurfView.State.currentModalEntity) {
            return;
        }

        modalBody.replaceChildren(moduleDetailsHtml(SurfView.State.currentModalKind, SurfView.State.currentModalEntity, {offerings: SurfView.State.currentModalOfferings}));
        setEntityModalMode('details');
    }

    /** Submits a course or program create or edit form. */
    async function submitModuleForm(form) {
        if (isReadOnlyMode()) {
            closeModuleEditForm();
            return;
        }
        const kind = form.dataset.entityKind;
        const mode = form.dataset.entityMode || 'edit';
        const status = document.getElementById('entity-edit-form-status');
        const submitButton = form.querySelector('button[type="submit"]');

        await submitEntity({
            status,
            submitButton,
            payload: () => modulePayloadFromForm(kind, form, SurfView.State.currentModalEntity),
            endpoint: payload => mode === 'create'
                ? tabByKind(kind).endpoint
                : tabByKind(kind).detailEndpoint(entityId(kind, payload)),
            method: mode === 'create' ? 'POST' : 'PUT',
            savingText: mode === 'edit' ? 'Saving...' : 'Adding...',
            successText: mode === 'edit' ? 'Saved' : 'Added',
            preserveArrayFields: moduleLocalizedFields(kind),
            errorMessage: mode === 'edit' ? `Unable to save ${kind}` : `Unable to add ${kind}`,
            onSuccess: ({payload, updatedEntity: updatedModule}) => {
                const id = entityId(kind, updatedModule) || entityId(kind, payload);
                applySubmitEntitySuccess({
                    kind,
                    mode,
                    updatedEntity: updatedModule,
                    id,
                    matchId: entityId(kind, payload),
                    languages: module => collectLanguages({module, offerings: SurfView.State.currentModalOfferings}),
                    preserveModalLanguage: true,
                    title: module => textValueLocalizedAttribute(module.name) || entityId(kind, module) || titleCase(kind),
                    detailsHtml: module => moduleDetailsHtml(kind, module, {offerings: SurfView.State.currentModalOfferings}),
                    updateUrl: mode === 'create',
                    replaceUrl: mode === 'create',
                    renderList: SurfView.State.tab === tabKeyByKind(kind)
                });
            }
        });
    }

    /** Reads a course or program API payload from a form. */
    function modulePayloadFromForm(kind, form, originalModule) {
        form.querySelectorAll('[data-module-array-input]').forEach(input => addModuleArrayValue(input));
        moduleDraftLanguageController.save(form);
        if (!moduleDraftLanguageController.validateRequiredField({targetForm: form})) {
            return null;
        }
        if (!moduleDraftLanguageController.validateRequiredField({
            field: 'description',
            targetForm: form
        })) {
            return null;
        }
        const formData = new FormData(form);
        const value = name => String(formData.get(name) || '').trim();
        const payload = structuredClone(originalModule || {});
        const studyLoadValue = value('studyLoadValue');
        const modeOfDelivery = value('modeOfDelivery');
        const localizedContent = moduleDraftLanguageController.payloadValues({kind});

        payload[`${kind}Id`] = value(`${kind}Id`);
        payload.primaryCode = primaryCodePayloadFromFormData(formData);
        moduleLocalizedFields(kind).forEach(field => {
            payload[field] = field === 'learningOutcomes'
                ? localizedContent[field].map(item => [item])
                : localizedContent[field];
        });
        payload.abbreviation = value('abbreviation');
        if (kind === 'program') {
            payload.programType = value('programType');
        }
        payload.teachingLanguage = value('teachingLanguage');
        payload.level = value('level');
        payload.studyLoad = studyLoadValue
            ? {
                value: Number(studyLoadValue),
                studyLoadUnit: value('studyLoadUnit')
            }
            : null;
        payload.modeOfDelivery = modeOfDelivery ? [modeOfDelivery] : [];
        payload.firstStartDate = value('firstStartDate');
        payload.sector = value('sector');
        payload.organization = value('organization');
        payload.link = value('link');
        payload.coordinators = moduleCoordinatorValues(form);
        payload.consumers = moduleConsumersFromForm(form, originalModule);

        const cleanedPayload = removeEmptyPayloadValues(payload);
        cleanedPayload.coordinators = payload.coordinators;
        return cleanedPayload;
    }

    function moduleCoordinatorValues(row) {
        return [...row.querySelectorAll('.module-coordinator-chip')]
            .map(chip => chip.dataset.coordinatorId)
            .filter(Boolean);
    }

    function moduleConsumersFromForm(form, originalModule) {
        const originalConsumers = Array.isArray(originalModule?.consumers) ? originalModule.consumers : [];

        return [...form.querySelectorAll('.module-consumer-row')].map(consumerRow => {
            const originalConsumerIndex = originalIndex(consumerRow.dataset.originalConsumerIndex);
            const originalConsumer = originalConsumerIndex !== null
                ? originalConsumers[originalConsumerIndex]
                : {};
            const consumer = structuredClone(originalConsumer || {});
            const allianceRows = [...consumerRow.querySelectorAll('.module-alliance-row')];
            const originalAlliances = Array.isArray(originalConsumer?.alliances) ? originalConsumer.alliances : [];

            consumer.consumerKey = consumerRow.querySelector('input[name="consumerKey"]')?.value.trim() || '';
            consumer.alliances = allianceRows.map(allianceRow => moduleAllianceFromRow(allianceRow, originalAlliances));
            return consumer;
        });
    }

    function moduleAllianceFromRow(row, originalAlliances) {
        const originalAllianceIndex = originalIndex(row.dataset.originalAllianceIndex);
        const originalAlliance = originalAllianceIndex !== null
            ? originalAlliances[originalAllianceIndex]
            : {};
        const alliance = structuredClone(originalAlliance || {});
        const value = name => row.querySelector(`[name="${name}"]`)?.value.trim() || '';
        const booleanValue = name => {
            const fieldValue = value(name);
            return fieldValue === '' ? null : fieldValue === 'true';
        };
        const numberValue = name => value(name) ? Number(value(name)) : null;

        alliance.name = value('allianceName');
        alliance.themes = moduleArrayValues(row, 'themes');
        alliance.selection = booleanValue('selection');
        alliance.type = value('type');
        alliance.instructorNames = moduleArrayValues(row, 'instructorNames');
        alliance.contactHours = numberValue('contactHours');
        alliance.activities = value('activities');
        alliance.microcredential = value('microcredential');
        alliance.targetGroup = value('targetGroup');
        alliance.visibleForOwnStudents = booleanValue('visibleForOwnStudents');
        alliance.enrollmentForOwnStudents = value('enrollmentForOwnStudents');
        alliance.visibleForGuests = booleanValue('visibleForGuests');
        alliance.enrollmentForGuests = value('enrollmentForGuests');
        alliance.jointPartnerCodes = moduleArrayValues(row, 'jointPartnerCodes');
        alliance.modeOfDelivery = value('allianceModeOfDelivery');
        alliance.level = value('allianceLevel');
        return alliance;
    }

    function moduleArrayValues(row, fieldName) {
        return [...row.querySelectorAll(`[data-module-array-field="${fieldName}"] .module-array-chip`)]
            .map(chip => chip.dataset.value)
            .filter(Boolean);
    }

    /** Registers course and program event handlers. */
    function initEventListeners() {
        delegateClick(modalBody, {
            '[data-add-module-language]': () => moduleDraftLanguageController.add(),
            '[data-delete-module-language]': button => moduleDraftLanguageController.deleteLanguage(button.dataset.deleteModuleLanguage),
            '[data-module-language-code]': button => moduleDraftLanguageController.switchLanguage(button.dataset.moduleLanguageCode),
            '[data-delete-module-array-value]': button => button.closest('.module-array-chip')?.remove(),
            '[data-add-module-coordinator]': button => addModuleCoordinator(button),
            '[data-delete-module-coordinator]': button => deleteModuleCoordinator(button)
        });

        modalBody.addEventListener('keydown', event => {
            const input = event.target.closest('[data-module-array-input]');
            if (input && event.key === 'Enter') {
                event.preventDefault();
                addModuleArrayValue(input);
            }
        });
    }

    return {
        moduleDetailsHtml,
        openCreateModuleModal,
        openModuleEditForm,
        closeModuleEditForm,
        modulePayloadFromForm,
        submitModuleForm,
        initEventListeners
    };

})();
