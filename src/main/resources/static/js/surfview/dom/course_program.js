function languageHrefQuery(courseId, language) {
    const params = new URLSearchParams(window.location.search);
    params.delete(currentModalKind === 'program' ? 'courseId' : 'programId');
    params.set(currentModalKind === 'program' ? 'programId' : 'courseId', courseId || '');
    params.set('lang', language);
    return params.toString();
}

function entityDetailsHtml(kind, entity, options = {}) {
    const offerings = options.offerings || [];
    const loadingOfferings = options.loadingOfferings || false;
    const fields = [
        ['Name', textValue(entity.name)],
        ['Code', plainCodeValue(entity.primaryCode)],
        ['Level', entity.level],
        ['Study load', studyLoadValue(entity.studyLoad)],
        ['Abbreviation', entity.abbreviation],
        [kind === 'program' ? 'Program type' : 'Teaching language', kind === 'program' ? entity.programType : entity.teachingLanguage],
        ['Delivery', listValue(entity.modeOfDelivery)],
        ['First start date', entity.firstStartDate],
        ['Organization', organizationValue(entity.organization)],
        ['Coordinators', coordinatorLabels(entity)],
        ['Link', entity.link]
    ];
    const description = textValue(entity.description);
    const admissionRequirements = filterLanguageItems(entity.admissionRequirements);
    const learningOutcomes = filterLanguageItems(entity.learningOutcomes);
    const enrollment = filterLanguageItems(entity.enrollment);
    const assessment = filterLanguageItems(entity.assessment);
    const qualificationRequirements = filterLanguageItems(entity.qualificationRequirements);
    const hasAddresses = Array.isArray(entity.addresses) && entity.addresses.length > 0;
    const hasAdmissionRequirements = admissionRequirements.length > 0;
    const hasLearningOutcomes = learningOutcomes.length > 0;
    const hasEnrollment = enrollment.length > 0;
    const hasAssessment = assessment.length > 0;
    const hasQualificationRequirements = qualificationRequirements.length > 0;
    const hasRequirements = hasAdmissionRequirements || hasQualificationRequirements;
    const hasConsumers = Array.isArray(entity.consumers) && entity.consumers.length > 0;
    const entityDetailId = entityId(kind, entity);
    const layout = document.createElement('div');
    layout.className = 'course-detail-layout';
    const nav = document.createElement('nav');
    nav.className = 'detail-nav';
    nav.setAttribute('aria-label', `${titleCase(kind)} detail sections`);
    nav.append(
        detailNavSectionElement('Language', entityLanguageNavElement(kind, entity)),
        detailNavSectionElement(titleCase(kind), entitySectionLinks({
            description,
            hasRequirements,
            hasLearningOutcomes,
            hasEnrollment,
            hasAssessment,
            hasConsumers
        })),
        detailNavSectionElement('Addresses', entityAddressNavElement(entity.addresses)),
        detailNavSectionElement('Offerings', entityOfferingNavElement(offerings, loadingOfferings))
    );

    const content = document.createElement('div');
    content.className = 'detail-content';
    const overview = detailSectionElement('course-overview', 'Overview');
    const details = document.createElement('div');
    details.className = 'detail-grid';
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
        section.append(consumersHtml(entity.consumers));
        content.append(section);
    }
    if (hasAddresses) {
        const section = detailSectionElement('course-addresses', 'Addresses');
        section.append(addressesHtml(entity.addresses));
        content.append(section);
    }
    content.append(entityOfferingsSection(kind, entityDetailId, offerings, loadingOfferings));
    layout.append(nav, content);
    return layout;
}

function entityLanguageNavElement(kind, entity) {
    if (!currentModalLanguages.length) {
        return textElement('span', 'No language-specific content', 'detail-nav-empty');
    }
    const items = document.createElement('div');
    items.className = 'language-nav-items';
    currentModalLanguages.forEach(code => {
        const link = anchorElement(`?${languageHrefQuery(entityId(kind, entity), code)}`, code, `language-nav-link${code === currentModalLanguage ? ' active' : ''}`);
        link.dataset.languageCode = code;
        link.title = languageLabel(code);
        items.append(link);
    });
    return items;
}

function entitySectionLinks(state) {
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

function entityAddressNavElement(addresses) {
    if (!Array.isArray(addresses) || !addresses.length) {
        return textElement('span', 'No addresses', 'detail-nav-empty');
    }
    const fragment = document.createDocumentFragment();
    addresses.forEach((address, index) => fragment.append(anchorElement(`#course-address-${index + 1}`, addressLabel(address, index))));
    return fragment;
}

function entityOfferingNavElement(offerings, loading) {
    if (loading) {
        return textElement('span', 'Loading offerings...', 'detail-nav-empty');
    }
    if (!offerings.length) {
        return textElement('span', 'No offerings', 'detail-nav-empty');
    }
    const fragment = document.createDocumentFragment();
    offerings.forEach((offering, index) => fragment.append(anchorElement(`#offering-${index + 1}`, offeringLabel(offering, index))));
    return fragment;
}

function entityOfferingsSection(kind, id, offerings, loading) {
    const section = detailSectionElement('course-offerings');
    const head = document.createElement('div');
    head.className = 'detail-section-head';
    head.append(textElement('h3', 'Offerings'));
    if (id) {
        const button = document.createElement('button');
        button.className = 'detail-action-button';
        button.type = 'button';
        button.dataset.addOfferingKind = kind;
        button.dataset.addOfferingId = id;
        button.dataset.offeringsEndpoint = Constants.tabs[kind === 'program' ? 'programs' : 'courses'].offeringsEndpoint(id);
        button.textContent = 'Add offering';
        head.append(button);
    }
    section.append(head);
    if (loading) {
        section.append(textElement('p', 'Loading offerings...', 'muted'));
    } else if (offerings.length) {
        offerings.forEach((offering, index) => section.append(offeringSectionHtml(offering, index)));
    } else {
        section.append(textElement('p', 'No offerings', 'muted'));
    }
    return section;
}

function languageTextListHtml(items) {
    const fragment = document.createDocumentFragment();
    items.forEach((item, index) => {
        const article = document.createElement('article');
        article.className = 'offering-card';
        article.id = `course-admission-requirement-${index + 1}`;
        if (item && typeof item === 'object' && 'value' in item) {
            if (item.language) {
                article.append(textElement('div', languageLabel(item.language), 'detail-label'));
            }
            article.append(detailValueElement(item.value || ''));
        } else {
            article.append(textElement('h3', `Requirement ${index + 1}`), jsonBoxElement(localizeLanguageValues(item)));
        }
        fragment.append(article);
    });
    return fragment;
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

function requirementLabel(requirement, index) {
    if (requirement && typeof requirement === 'object') {
        if (requirement.language) {
            return languageLabel(requirement.language);
        }

        if (requirement.name) {
            return textValue(requirement.name);
        }
    }

    return `Requirement ${index + 1}`;
}

function consumersHtml(consumers) {
    const fragment = document.createDocumentFragment();
    consumers.forEach((consumer, index) => {
        const article = document.createElement('article');
        article.className = 'offering-card';
        if (typeof consumer === 'string') {
            article.append(detailValueElement(consumer));
        } else {
            const label = textValue(consumer.name) || consumer.consumerKey || consumer.consumerId || `Consumer ${index + 1}`;
            article.append(textElement('h3', label), jsonBoxElement(localizeLanguageValues(consumer)));
        }
        fragment.append(article);
    });
    return fragment;
}

function programDetailsHtml(program, options = {}) {
    return entityDetailsHtml('program', program, options);
}

function entityEditFormHtml(kind, entity) {
    const id = entityId(kind, entity);
    const primaryCode = primaryCodeParts(entity.primaryCode, id);
    const studyLoad = entity.studyLoad || {};
    const studyLoadUnit = studyLoad.studyLoadUnit || studyLoad.unit || 'ects';
    const modeOfDelivery = Array.isArray(entity.modeOfDelivery) ? entity.modeOfDelivery[0] : entity.modeOfDelivery;
    const fragment = document.createDocumentFragment();
    const form = document.createElement('form');
    form.className = 'offering-form entity-edit-form';
    form.id = 'entity-edit-form';
    form.dataset.entityKind = kind;

    const fields = [
        fieldLabelHtml(`${titleCase(kind)} ID`, 'text', `${kind}Id`, id || '', {required: true, readonly: true}, 'full-width'),
        fieldLabelHtml('Primary code type', 'select', 'primaryCodeType', primaryCode.codeType, {
            required: true,
            options: Constants.enumOptions.offeringCodeType
        }),
        fieldLabelHtml('Primary code', 'text', 'primaryCode', primaryCode.code, {required: true}),
        entityLanguagePanel(kind, entity),
        fieldLabelHtml('Abbreviation', 'text', 'abbreviation', entity.abbreviation || '')
    ];

    if (kind === 'program') {
        fields.push(fieldLabelHtml('Program type', 'select', 'programType', entity.programType || 'program', {
            required: true,
            options: Constants.enumOptions.programType
        }));
    }

    const coordinatorsPanel = entityCoordinatorsPanel(entity);
    coordinatorsPanel.dataset.entityFormSection = 'coordinators';

    fields.push(
        fieldLabelHtml('Teaching language', 'select', 'teachingLanguage', entity.teachingLanguage || 'eng', {
            options: Constants.enumOptions.teachingLanguage,
            showValue: true
        }),
        fieldLabelHtml('Level', 'select', 'level', entity.level || '', {
            options: [['', 'None'], ...Constants.enumOptions.level]
        }),
        fieldLabelHtml('Study load value', 'number', 'studyLoadValue', studyLoad.value ?? studyLoad.amount ?? '', {min: 0, step: 0.01}),
        fieldLabelHtml('Study load unit', 'select', 'studyLoadUnit', studyLoadUnit, {
            options: Constants.enumOptions.studyLoadType
        }),
        fieldLabelHtml('Mode of delivery', 'select', 'modeOfDelivery', modeOfDelivery || '', {
            options: [['', 'None'], ...Constants.enumOptions.modeOfDelivery]
        }),
        fieldLabelHtml('First start date', 'date', 'firstStartDate', entity.firstStartDate || ''),
        fieldLabelHtml('Sector', 'select', 'sector', entity.sector || '', {
            options: [['', 'None'], ...Constants.enumOptions.sector]
        }),
        fieldLabelHtml('Organization', 'select', 'organization', organizationIdValue(entity.organization), {
            options: organizationOptionEntries(organizationIdValue(entity.organization))
        }),
        fieldLabelHtml('Link', 'url', 'link', entity.link || '', {placeholder: 'https://'}, 'full-width'),
        coordinatorsPanel,
        entityConsumersPanel(entity),
        entityEditFormActions()
    );
    form.append(...fields);
    fragment.append(form);
    return fragment;
}

function coordinatorIds(entity) {
    return (Array.isArray(entity?.coordinators) ? entity.coordinators : [])
        .map(coordinator => typeof coordinator === 'string' ? coordinator : entityId('person', coordinator))
        .filter(Boolean)
        .map(String);
}

function coordinatorLabels(entity) {
    const labels = coordinatorIds(entity).map(id => personLabelById(id));
    return labels.length ? labels.join(', ') : '';
}

function entityCoordinatorsPanel(entity) {
    const section = document.createElement('section');
    section.className = 'nested-item-panel consumer-panel entity-coordinators-panel';

    const head = document.createElement('div');
    head.className = 'nested-item-head';
    head.append(textElement('h3', 'Coordinators'));

    const list = document.createElement('div');
    list.id = 'entity-coordinator-list';
    list.className = 'entity-array-chips entity-coordinator-list full-width';
    coordinatorIds(entity).forEach(id => list.append(entityCoordinatorChip(id)));

    const controls = document.createElement('div');
    controls.className = 'entity-coordinator-controls full-width';

    const label = document.createElement('label');
    label.append(labelTextElement('Add coordinator', false));

    const select = document.createElement('select');
    select.id = 'entity-coordinator-select';
    select.dataset.entityCoordinatorSelect = '';
    label.append(select);

    const addButton = document.createElement('button');
    addButton.className = 'detail-action-button';
    addButton.type = 'button';
    addButton.dataset.addEntityCoordinator = '';
    addButton.textContent = 'Add';

    controls.append(label, addButton);
    section.append(head, list, controls);
    refreshCoordinatorSelect(section);
    return section;
}

function entityCoordinatorChip(id) {
    const chip = document.createElement('span');
    chip.className = 'entity-array-chip entity-coordinator-chip';
    chip.dataset.coordinatorId = id;

    const text = document.createElement('span');
    text.textContent = personLabelById(id);

    const deleteButton = document.createElement('button');
    deleteButton.type = 'button';
    deleteButton.dataset.deleteEntityCoordinator = '';
    deleteButton.setAttribute('aria-label', `Remove ${text.textContent}`);
    deleteButton.textContent = 'x';

    chip.append(text, deleteButton);
    return chip;
}

function refreshCoordinatorSelect(scope = document) {
    const panel = scope.closest?.('.entity-coordinators-panel') || scope.querySelector?.('.entity-coordinators-panel') || scope;
    const select = panel.querySelector?.('[data-entity-coordinator-select]');
    const addButton = panel.querySelector?.('[data-add-entity-coordinator]');
    if (!select || !addButton) {
        return;
    }

    const selectedIds = new Set(entityCoordinatorValues(panel));
    const options = [...personById.values()]
        .filter(person => {
            const id = entityId('person', person);
            return id && !selectedIds.has(String(id));
        })
        .sort((left, right) => personLabel(left).localeCompare(personLabel(right)));

    select.replaceChildren();
    const placeholder = document.createElement('option');
    placeholder.value = '';
    placeholder.textContent = options.length ? 'Select person' : 'No persons available';
    select.append(placeholder);
    options.forEach(person => {
        const id = String(entityId('person', person));
        const option = document.createElement('option');
        option.value = id;
        option.textContent = personLabel(person);
        select.append(option);
    });
    select.value = '';
    addButton.disabled = !options.length;
}

function addEntityCoordinator(button) {
    const panel = button.closest('.entity-coordinators-panel');
    const select = panel?.querySelector('[data-entity-coordinator-select]');
    const list = panel?.querySelector('#entity-coordinator-list');
    const id = select?.value || '';
    if (!panel || !list || !id) {
        return;
    }

    const duplicate = entityCoordinatorValues(panel).includes(String(id));
    if (!duplicate) {
        list.append(entityCoordinatorChip(String(id)));
    }
    refreshCoordinatorSelect(panel);
}

function deleteEntityCoordinator(button) {
    const panel = button.closest('.entity-coordinators-panel');
    button.closest('.entity-coordinator-chip')?.remove();
    if (panel) {
        refreshCoordinatorSelect(panel);
    }
}

function entityConsumersPanel(entity) {
    const section = document.createElement('section');
    section.className = 'nested-item-panel consumer-panel entity-consumers-panel entity-consumer-row';
    const consumers = Array.isArray(entity.consumers) ? entity.consumers : [];
    const consumerIndex = consumers.findIndex(consumer => consumer?.consumerKey === Constants.offeringConsumer.consumerKey);
    const selectedIndex = consumerIndex >= 0 ? consumerIndex : '';
    const consumer = selectedIndex === '' ? {consumerKey: Constants.offeringConsumer.consumerKey} : consumers[selectedIndex];
    section.dataset.originalConsumerIndex = selectedIndex;

    const head = document.createElement('div');
    head.className = 'nested-item-head';

    const heading = document.createElement('h3');
    heading.textContent = 'Consumer';

    head.append(heading);
    const alliances = Array.isArray(consumer.alliances) ? consumer.alliances : [];
    const komIndex = alliances.findIndex(alliance => alliance?.name === Constants.offeringConsumer.defaultAllianceName);
    const selectedAllianceIndex = komIndex >= 0 ? komIndex : (alliances.length ? 0 : '');
    const alliance = selectedAllianceIndex === ''
        ? {name: Constants.offeringConsumer.defaultAllianceName}
        : alliances[selectedAllianceIndex];

    section.append(
        head,
        fieldLabelHtml('Consumer key', 'text', 'consumerKey', consumer.consumerKey || Constants.offeringConsumer.consumerKey, {
            required: true,
            readonly: true
        }),
        entityAllianceRow(alliance, selectedAllianceIndex)
    );
    return section;
}

function entityAllianceRow(alliance = {}, originalIndex = '') {
    const row = document.createElement('div');
    row.className = 'entity-alliance-row';
    row.dataset.originalAllianceIndex = originalIndex;

    row.append(
        fieldLabelHtml('Alliance name', 'text', 'allianceName', alliance.name || '', {required: true}),
        entityArrayField('Themes', 'themes', alliance.themes),
        fieldLabelHtml('Selection', 'select', 'selection', booleanFormValue(alliance.selection), {
            options: booleanFormOptions()
        }),
        fieldLabelHtml('Type', 'text', 'type', alliance.type || ''),
        entityArrayField('Instructor names', 'instructorNames', alliance.instructorNames),
        fieldLabelHtml('Contact hours', 'number', 'contactHours', alliance.contactHours ?? '', {min: 0, step: 0.01}),
        fieldLabelHtml('Activities', 'textarea', 'activities', alliance.activities || '', {}, 'full-width'),
        fieldLabelHtml('Microcredential', 'text', 'microcredential', alliance.microcredential || ''),
        fieldLabelHtml('Target group', 'text', 'targetGroup', alliance.targetGroup || ''),
        fieldLabelHtml('Visible for own students', 'select', 'visibleForOwnStudents', booleanFormValue(alliance.visibleForOwnStudents), {
            options: booleanFormOptions()
        }),
        fieldLabelHtml('Enrollment for own students', 'text', 'enrollmentForOwnStudents', alliance.enrollmentForOwnStudents || ''),
        fieldLabelHtml('Visible for guests', 'select', 'visibleForGuests', booleanFormValue(alliance.visibleForGuests), {
            options: booleanFormOptions()
        }),
        fieldLabelHtml('Enrollment for guests', 'text', 'enrollmentForGuests', alliance.enrollmentForGuests || ''),
        entityArrayField('Joint partner codes', 'jointPartnerCodes', alliance.jointPartnerCodes),
        fieldLabelHtml('Mode of delivery', 'text', 'allianceModeOfDelivery', alliance.modeOfDelivery || ''),
        fieldLabelHtml('Level', 'text', 'allianceLevel', alliance.level || '')
    );
    return row;
}

function booleanFormOptions() {
    return [['', 'None'], ['true', 'Yes'], ['false', 'No']];
}

function booleanFormValue(value) {
    return typeof value === 'boolean' ? String(value) : '';
}

function entityArrayField(labelName, fieldName, values = []) {
    const label = document.createElement('label');
    label.className = 'full-width entity-array-field';
    label.append(labelTextElement(labelName, false));

    const editor = document.createElement('div');
    editor.className = 'entity-array-editor';
    editor.dataset.entityArrayField = fieldName;

    const chips = document.createElement('div');
    chips.className = 'entity-array-chips';
    (Array.isArray(values) ? values : []).forEach(value => {
        if (value !== null && value !== undefined && String(value).trim()) {
            chips.append(entityArrayChip(String(value).trim()));
        }
    });

    const input = document.createElement('input');
    input.type = 'text';
    input.dataset.entityArrayInput = '';
    input.placeholder = 'Type a value and press Enter';
    input.setAttribute('aria-label', `Add ${labelName.toLowerCase()}`);

    editor.append(chips, input);
    label.append(editor);
    return label;
}

function entityArrayChip(value) {
    const chip = document.createElement('span');
    chip.className = 'entity-array-chip';
    chip.dataset.value = value;

    const text = document.createElement('span');
    text.textContent = value;

    const deleteButton = document.createElement('button');
    deleteButton.type = 'button';
    deleteButton.dataset.deleteEntityArrayValue = '';
    deleteButton.setAttribute('aria-label', `Remove ${value}`);
    deleteButton.textContent = 'x';

    chip.append(text, deleteButton);
    return chip;
}

function entityLanguagePanel(kind, entity) {
    const section = document.createElement('section');
    section.className = 'nested-item-panel entity-localized-panel';

    const head = document.createElement('div');
    head.className = 'nested-item-head';

    const heading = document.createElement('h3');
    heading.textContent = 'Localized content';

    const addButton = document.createElement('button');
    addButton.className = 'detail-action-button';
    addButton.type = 'button';
    addButton.dataset.addEntityLanguage = '';
    addButton.textContent = 'Add language';

    const list = document.createElement('div');
    list.className = 'entity-language-list';
    list.id = 'entity-language-list';
    entityLanguageCodes(kind, entity).forEach(language => {
        list.append(entityLanguageRow(kind, entity, language));
    });

    head.append(heading, addButton);
    section.append(head, list);
    return section;
}

function entityLanguageRow(kind, entity, language) {
    const row = document.createElement('section');
    row.className = 'entity-language-row';

    const rowHead = document.createElement('div');
    rowHead.className = 'entity-language-row-head';
    rowHead.append(fieldLabelHtml('Language', 'select', 'language', language, {
        required: true,
        options: entityLanguageOptions(language)
    }));

    const deleteButton = document.createElement('button');
    deleteButton.className = 'nested-item-delete';
    deleteButton.type = 'button';
    deleteButton.dataset.deleteEntityLanguage = '';
    deleteButton.textContent = 'Delete';
    rowHead.append(deleteButton);

    const fields = [
        rowHead,
        fieldLabelHtml('Name', 'text', 'name', entityLocalizedFieldValue(entity.name, language), {required: true}, 'full-width'),
        fieldLabelHtml('Description', 'textarea', 'description', entityLocalizedFieldValue(entity.description, language), {}, 'full-width'),
        fieldLabelHtml('Admission requirements', 'textarea', 'admissionRequirements', entityLocalizedFieldValue(entity.admissionRequirements, language), {}, 'full-width'),
        fieldLabelHtml('Qualification requirements', 'textarea', 'qualificationRequirements', entityLocalizedFieldValue(entity.qualificationRequirements, language), {}, 'full-width')
    ];

    if (kind === 'course') {
        fields.push(
            fieldLabelHtml('Enrollment', 'textarea', 'enrollment', entityLocalizedFieldValue(entity.enrollment, language), {}, 'full-width'),
            fieldLabelHtml('Assessment', 'textarea', 'assessment', entityLocalizedFieldValue(entity.assessment, language), {}, 'full-width'),
            fieldLabelHtml('Learning outcomes', 'textarea', 'learningOutcomes', entityLocalizedFieldValue(entity.learningOutcomes, language), {}, 'full-width')
        );
    }

    row.append(
        ...fields
    );
    return row;
}

function entityLocalizedFieldValue(value, language) {
    if (typeof value === 'string') {
        return value;
    }

    return flattenLanguageItems(Array.isArray(value) ? value : [])
        .find(item => item?.language === language)?.value || '';
}

function entityLocalizedFields(kind) {
    const fields = ['name', 'description', 'admissionRequirements', 'qualificationRequirements'];
    return kind === 'course'
        ? [...fields, 'enrollment', 'assessment', 'learningOutcomes']
        : fields;
}

function entityLanguageCodes(kind, entity) {
    const allowed = new Set(Object.keys(Constants.languageNames));
    const codes = new Set();
    entityLocalizedFields(kind).forEach(field => {
        flattenLanguageItems(Array.isArray(entity[field]) ? entity[field] : []).forEach(item => {
            if (allowed.has(item?.language)) {
                codes.add(item.language);
            }
        });
    });

    if (!codes.size) {
        codes.add(allowed.has(currentModalLanguage) ? currentModalLanguage : 'en-GB');
    }

    return [...codes];
}

function entityLanguageOptions(selectedLanguage = '') {
    const options = Object.entries(Constants.languageNames)
        .sort(([, leftLabel], [, rightLabel]) => leftLabel.localeCompare(rightLabel))
        .map(([code, label]) => [code, `${label} (${code})`]);

    if (selectedLanguage && !(selectedLanguage in Constants.languageNames)) {
        options.unshift([selectedLanguage, selectedLanguage]);
    }

    return options;
}

function addEntityLanguageRow() {
    const list = document.getElementById('entity-language-list');
    if (!list || !currentModalEntity) {
        return;
    }

    const selectedLanguages = new Set([...list.querySelectorAll('select[name="language"]')].map(select => select.value));
    const nextLanguage = Object.keys(Constants.languageNames).find(language => !selectedLanguages.has(language));
    if (!nextLanguage) {
        return;
    }

    list.append(entityLanguageRow(currentModalKind, currentModalEntity, nextLanguage));
    list.lastElementChild?.querySelector('select[name="language"]')?.focus();
}

function deleteEntityLanguageRow(button) {
    const list = document.getElementById('entity-language-list');
    const row = button.closest('.entity-language-row');
    if (!list || !row) {
        return;
    }

    const rows = list.querySelectorAll('.entity-language-row');
    if (rows.length <= 1) {
        return;
    }

    row.remove();
}

function addEntityArrayValue(input) {
    const value = input.value.trim();
    const editor = input.closest('[data-entity-array-field]');
    const chips = editor?.querySelector('.entity-array-chips');
    if (!value || !chips) {
        return;
    }

    const duplicate = [...chips.querySelectorAll('.entity-array-chip')]
        .some(chip => chip.dataset.value === value);
    if (!duplicate) {
        chips.append(entityArrayChip(value));
    }
    input.value = '';
}

function entityEditFormActions() {
    const actions = document.createElement('div');
    actions.className = 'offering-form-actions';

    const status = document.createElement('span');
    status.className = 'offering-form-status';
    status.id = 'entity-edit-form-status';

    const cancelButton = document.createElement('button');
    cancelButton.className = 'pager-button detail-action-button';
    cancelButton.type = 'button';
    cancelButton.dataset.cancelEntityEdit = '';
    cancelButton.textContent = 'Cancel';

    const submitButton = document.createElement('button');
    submitButton.className = 'detail-action-button';
    submitButton.type = 'submit';
    submitButton.textContent = 'Save';

    actions.append(status, cancelButton, submitButton);
    return actions;
}

async function openEntityEditForm() {
    if (!currentModalEntity) {
        return;
    }

    if (currentModalKind === 'organization') {
        openOrganizationEditForm(currentModalEntity);
        return;
    }
    if (currentModalKind === 'person') {
        openPersonEditForm(currentModalEntity);
        return;
    }

    try {
        await Promise.all([
            loadOrganizations().catch(() => null),
            loadPersons().catch(() => null)
        ]);
    } catch (error) {
        // Keep editing available if related entity lists cannot be loaded.
    }

    modalBody.replaceChildren(entityEditFormHtml(currentModalKind, currentModalEntity));
    entityEditButton.classList.add('hidden');
    entityJsonPreviewButton.classList.remove('hidden');
    modalBody.querySelector('.entity-language-row input[name="name"]')?.focus();
}

function closeEntityEditForm() {
    if (!currentModalEntity) {
        return;
    }

    if (currentModalKind === 'organization' || currentModalKind === 'person') {
        if (document.getElementById('entity-edit-form')?.dataset.entityMode === 'create') {
            closeEntityModal();
            return;
        }
        modalBody.replaceChildren(currentModalKind === 'organization'
            ? organizationDetailsHtml(currentModalEntity)
            : personDetailsHtml(currentModalEntity));
    } else {
        modalBody.replaceChildren(entityDetailsHtml(currentModalKind, currentModalEntity, {offerings: currentModalOfferings}));
    }
    entityEditButton.classList.remove('hidden');
    entityDeleteButton.classList.toggle('hidden', !['organization', 'person'].includes(currentModalKind));
    entityJsonPreviewButton.classList.add('hidden');
}

async function submitEntityEditForm(event) {
    event.preventDefault();
    const form = event.target;
    const kind = form.dataset.entityKind;
    if (kind === 'organization') {
        await submitOrganizationForm(form);
        return;
    }
    if (kind === 'person') {
        await submitPersonForm(form);
        return;
    }
    const status = document.getElementById('entity-edit-form-status');
    const submitButton = form.querySelector('button[type="submit"]');
    const payload = entityPayloadFromForm(kind, form, currentModalEntity);
    const id = entityId(kind, payload);

    status.classList.remove('error');
    status.replaceChildren();
    status.textContent = 'Saving...';
    submitButton.disabled = true;

    try {
        const response = await callEndpoint(`/${kind === 'program' ? 'programs' : 'courses'}/${encodeURIComponent(id)}`, {
            method: 'PUT',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw await requestErrorFromResponse(response);
        }

        const responseEntity = await response.json();
        const updatedEntity = {
            ...payload,
            ...responseEntity
        };
        entityLocalizedFields(kind).forEach(field => {
            if (!Array.isArray(updatedEntity[field]) || !updatedEntity[field].length) {
                updatedEntity[field] = payload[field];
            }
        });
        currentModalEntity = updatedEntity;
        entityMap(kind).set(String(entityId(kind, updatedEntity)), updatedEntity);
        currentPageEntities = currentPageEntities.map(entity => (
            entityId(kind, entity) === entityId(kind, updatedEntity) ? updatedEntity : entity
        ));
        currentModalLanguages = collectLanguages({entity: currentModalEntity, offerings: currentModalOfferings});
        if (!currentModalLanguage || !currentModalLanguages.includes(currentModalLanguage)) {
            currentModalLanguage = chooseLanguage(currentModalLanguages);
        }
        courseModal.title.textContent = textValue(currentModalEntity.name) || entityId(kind, currentModalEntity) || titleCase(kind);
        entityEditButton.classList.remove('hidden');
        entityJsonPreviewButton.classList.add('hidden');
        modalBody.replaceChildren(entityDetailsHtml(kind, currentModalEntity, {offerings: currentModalOfferings}));
        renderCurrentEntities();
    } catch (error) {
        renderOfferingFormError(status, error, `Unable to save ${kind}`);
    } finally {
        submitButton.disabled = false;
    }
}

function openEntityJsonPreview() {
    const form = document.getElementById('entity-edit-form');
    if (!form || !currentModalEntity) {
        return;
    }

    const kind = form.dataset.entityKind;
    const payload = kind === 'organization'
        ? organizationPayloadFromForm(form, currentModalEntity)
        : kind === 'person'
            ? personPayloadFromForm(form, currentModalEntity)
            : entityPayloadFromForm(kind, form, currentModalEntity);
    jsonPreviewModal.title.textContent = `${titleCase(kind)} JSON`;
    jsonPreviewModal.subtitle.textContent = `Payload that will be submitted when saving this ${kind}`;
    jsonPreviewContent.textContent = JSON.stringify(payload, null, 2);
    jsonPreviewModal.backdrop.classList.add('open');
    jsonPreviewModal.close.focus();
}

function entityPayloadFromForm(kind, form, originalEntity) {
    form.querySelectorAll('[data-entity-array-input]').forEach(input => addEntityArrayValue(input));
    const formData = new FormData(form);
    const value = name => String(formData.get(name) || '').trim();
    const payload = structuredClone(originalEntity || {});
    const studyLoadValue = value('studyLoadValue');
    const modeOfDelivery = value('modeOfDelivery');
    const localizedContent = entityLocalizedContentFromForm(kind, form);

    payload[`${kind}Id`] = value(`${kind}Id`);
    payload.primaryCode = {
        codeType: value('primaryCodeType'),
        code: value('primaryCode')
    };
    entityLocalizedFields(kind).forEach(field => {
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
    payload.coordinators = entityCoordinatorValues(form);
    payload.consumers = entityConsumersFromForm(form, originalEntity);

    const cleanedPayload = removeEmptyPayloadValues(payload);
    cleanedPayload.coordinators = payload.coordinators;
    return cleanedPayload;
}

function entityCoordinatorValues(row) {
    return [...row.querySelectorAll('.entity-coordinator-chip')]
        .map(chip => chip.dataset.coordinatorId)
        .filter(Boolean);
}

function entityConsumersFromForm(form, originalEntity) {
    const originalConsumers = Array.isArray(originalEntity?.consumers) ? originalEntity.consumers : [];

    return [...form.querySelectorAll('.entity-consumer-row')].map(consumerRow => {
        const originalConsumerIndex = entityOriginalIndex(consumerRow.dataset.originalConsumerIndex);
        const originalConsumer = originalConsumerIndex !== null
            ? originalConsumers[originalConsumerIndex]
            : {};
        const consumer = structuredClone(originalConsumer || {});
        const allianceRows = [...consumerRow.querySelectorAll('.entity-alliance-row')];
        const originalAlliances = Array.isArray(originalConsumer?.alliances) ? originalConsumer.alliances : [];

        consumer.consumerKey = consumerRow.querySelector('input[name="consumerKey"]')?.value.trim() || '';
        consumer.alliances = allianceRows.map(allianceRow => entityAllianceFromRow(allianceRow, originalAlliances));
        return consumer;
    });
}

function entityAllianceFromRow(row, originalAlliances) {
    const originalAllianceIndex = entityOriginalIndex(row.dataset.originalAllianceIndex);
    const originalAlliance = originalAllianceIndex !== null
        ? originalAlliances[originalAllianceIndex]
        : {};
    const alliance = structuredClone(originalAlliance || {});
    const value = name => row.querySelector(`[name="${name}"]`)?.value.trim() || '';
    const booleanValue = name => {
        const fieldValue = value(name);
        return fieldValue === '' ? null : fieldValue === 'true';
    };
    const numberValue = name => {
        const fieldValue = value(name);
        return fieldValue === '' ? null : Number(fieldValue);
    };

    alliance.name = value('allianceName');
    alliance.themes = entityArrayValues(row, 'themes');
    alliance.selection = booleanValue('selection');
    alliance.type = value('type');
    alliance.instructorNames = entityArrayValues(row, 'instructorNames');
    alliance.contactHours = numberValue('contactHours');
    alliance.activities = value('activities');
    alliance.microcredential = value('microcredential');
    alliance.targetGroup = value('targetGroup');
    alliance.visibleForOwnStudents = booleanValue('visibleForOwnStudents');
    alliance.enrollmentForOwnStudents = value('enrollmentForOwnStudents');
    alliance.visibleForGuests = booleanValue('visibleForGuests');
    alliance.enrollmentForGuests = value('enrollmentForGuests');
    alliance.jointPartnerCodes = entityArrayValues(row, 'jointPartnerCodes');
    alliance.modeOfDelivery = value('allianceModeOfDelivery');
    alliance.level = value('allianceLevel');
    return alliance;
}

function entityOriginalIndex(value) {
    if (value === '') {
        return null;
    }

    const index = Number(value);
    return Number.isInteger(index) && index >= 0 ? index : null;
}

function entityArrayValues(row, fieldName) {
    return [...row.querySelectorAll(`[data-entity-array-field="${fieldName}"] .entity-array-chip`)]
        .map(chip => chip.dataset.value)
        .filter(Boolean);
}

function entityLocalizedContentFromForm(kind, form) {
    const localizedContent = Object.fromEntries(entityLocalizedFields(kind).map(field => [field, []]));

    form.querySelectorAll('.entity-language-row').forEach(row => {
        const language = row.querySelector('select[name="language"]')?.value || '';
        if (!language || !(language in Constants.languageNames)) {
            return;
        }

        Object.keys(localizedContent).forEach(field => {
            const value = row.querySelector(`[name="${field}"]`)?.value.trim() || '';
            if (value) {
                const existingIndex = localizedContent[field].findIndex(item => item.language === language);
                const item = {language, value};
                if (existingIndex >= 0) {
                    localizedContent[field][existingIndex] = item;
                } else {
                    localizedContent[field].push(item);
                }
            }
        });
    });

    return localizedContent;
}
