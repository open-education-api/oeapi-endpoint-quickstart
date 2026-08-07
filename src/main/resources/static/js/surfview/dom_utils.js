function languageSpecificSectionFragment({
    activeLanguage,
    buttonAttribute,
    label,
    fields,
    languages = SurfView.State.draftOffering.languages,
    languageActions = null,
    headerContent = null
}) {
    return cloneTemplate('language-specific-section-template', section => {
        section.prepend(languageSwitcherHtml({activeLanguage, buttonAttribute, label, languages, languageActions, headerContent}));
        section.querySelector('.language-specific-fields').append(...fields);
    });
}

function elementsMatching(node, selector) {
    const matches = [...node.querySelectorAll(selector)];
    if (node.matches(selector)) {
        matches.unshift(node);
    }
    return matches;
}

/** Creates an element from a compact selector-like spec. */
function createElement(spec, options = {}) {
    const tagName = spec.match(/^[A-Za-z][A-Za-z0-9-]*/)?.[0] || 'div';
    const element = document.createElement(tagName);
    const id = spec.match(/#([A-Za-z][A-Za-z0-9_-]*)/)?.[1];
    const classes = [...spec.matchAll(/\.([A-Za-z][A-Za-z0-9_-]*)/g)].map(match => match[1]);

    if (id) {
        element.id = id;
    }
    if (classes.length) {
        element.classList.add(...classes);
    }
    if (Object.prototype.hasOwnProperty.call(options, 'text')) {
        element.textContent = options.text;
    }
    Object.entries(options.dataset || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
            element.dataset[key] = value;
        }
    });
    Object.entries(options.attributes || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
            element.setAttribute(key, value);
        }
    });

    return element;
}

/** Registers delegated click handlers under a root node. */
function delegateClick(root, handlers) {
    root.addEventListener('click', event => {
        if (!event.target?.closest) {
            return;
        }

        for (const [selector, handler] of Object.entries(handlers)) {
            const target = event.target.closest(selector);
            if (target && root.contains(target)) {
                event.preventDefault();
                handler(target, event);
                return;
            }
        }
    });
}

// Clones a <template>'s root element, optionally filling in data-tm-* placeholders and
// then handing the node to a configure callback for custom changes to the element.
//
// mapping placeholders:
//   data-tm-text="key"              sets textContent from mapping[key] (if present)
//   data-tm-attribute="key:attr"    sets the attr attribute from mapping[key] (if present)
//   data-tm-empty-attribute="key"   sets the mapping[key] attribute to "" (if present)
//   data-tm-remove-if-missing="key" removes the element when mapping[key] is missing
// Placeholders are honored both on the template's root element and on its descendants.
/** Clones a template and fills mapped placeholders. */
function cloneTemplate(templateId, configure, mapping) {
    const node = document.getElementById(templateId).content.firstElementChild.cloneNode(true);
    if (mapping) {
        elementsMatching(node, '[data-tm-remove-if-missing]').forEach(element => {
            const key = element.dataset.tmRemoveIfMissing;
            if (!Object.prototype.hasOwnProperty.call(mapping, key)) {
                element.remove();
            }
        });
        elementsMatching(node, '[data-tm-attribute]').forEach(element => {
            const match = element.dataset.tmAttribute.match(/^([A-Za-z_$][A-Za-z0-9_$]*):([A-Za-z][A-Za-z0-9_-]*)$/);

            if (match) {
                const [, key, attributeName] = match;

                if (Object.prototype.hasOwnProperty.call(mapping, key)) {
                    element.setAttribute(attributeName, mapping[key]);
                }
            }
        });
        elementsMatching(node, '[data-tm-empty-attribute]').forEach(element => {
            const key = element.dataset.tmEmptyAttribute;
            if (Object.prototype.hasOwnProperty.call(mapping, key) && mapping[key]) {
                element.setAttribute(mapping[key], '');
            }
        });
        elementsMatching(node, '[data-tm-text]').forEach(element => {
            const key = element.dataset.tmText;
            if (Object.prototype.hasOwnProperty.call(mapping, key)) {
                element.textContent = mapping[key];
            }
        });
    }
    if (configure) {
        configure(node);
    }
    return node;
}

function draftLanguageOptions(selectedLanguages, selectedLanguage = '') {
    const selected = new Set(selectedLanguages);
    const options = Object.entries(Constants.languageNames)
        .filter(([code]) => code === selectedLanguage || !selected.has(code))
        .sort(([, leftLabel], [, rightLabel]) => leftLabel.localeCompare(rightLabel))
        .map(([code, label]) => [code, `${label} (${code})`]);

    if (selectedLanguage && !isKnownLanguageCode(selectedLanguage)) {
        options.unshift([selectedLanguage, selectedLanguage]);
    }

    return options;
}

function draftLanguageActions(prefix, language) {
    return createElement('button.language-delete-button', {
        text: 'x',
        attributes: {
            type: 'button',
            title: `Delete ${languageLabel(language)}`,
            'aria-label': `Delete ${languageLabel(language)}`,
            [`data-delete-${prefix}-language`]: language
        }
    });
}

function draftLanguageControls(prefix, selectName) {
    return cloneTemplate('draft-language-controls-template', controls => {
        const select = controls.querySelector('select');
        select.name = selectName;
        select.setAttribute(`data-${prefix}-language-select`, '');
        controls.querySelector('button').setAttribute(`data-add-${prefix}-language`, '');
    });
}

function booleanFormValue(value) {
    return typeof value === 'boolean' ? String(value) : '';
}

function consumerAllianceOptions() {
    return Constants.offeringConsumer.allianceNames.map(name => [name, name]);
}

function consumerAllianceName(value) {
    return Constants.offeringConsumer.allianceNames.includes(value) ? value : Constants.offeringConsumer.defaultAllianceName;
}

function surfConsumerSelection(source = {}, options = {}) {
    if (options.includeSingleConsumer && source.consumer && typeof source.consumer === 'object') {
        return {consumer: source.consumer, index: ''};
    }

    const consumers = Array.isArray(source.consumers) ? source.consumers : [];
    let index = consumers.findIndex(consumer => consumer?.consumerKey === Constants.offeringConsumer.consumerKey);
    if (index < 0 && options.fallbackFirst && consumers.length) {
        index = 0;
    }

    return index >= 0
        ? {consumer: consumers[index], index}
        : {consumer: {consumerKey: Constants.offeringConsumer.consumerKey}, index: ''};
}

function surfAllianceSelection(consumer = {}, options = {}) {
    const alliances = Array.isArray(consumer.alliances) ? consumer.alliances : [];
    const names = options.names || [Constants.offeringConsumer.defaultAllianceName];
    let index = alliances.findIndex(alliance => names.includes(alliance?.name));
    if (index < 0 && options.fallbackFirst && alliances.length) {
        index = 0;
    }

    return index >= 0
        ? {alliance: alliances[index], index}
        : {alliance: {name: Constants.offeringConsumer.defaultAllianceName}, index: ''};
}

function consumerPanelFragment(consumer, fields, configure = null, consumerIndex = '', keyOptions = {}) {
    const section = cloneTemplate('consumer-panel-template', configure, {consumerIndex});
    section.append(
        fieldLabelHtml('Consumer key', 'text', 'consumerKey', consumer?.consumerKey || Constants.offeringConsumer.consumerKey, {
            readonly: true,
            ...keyOptions
        }),
        ...fields
    );
    return section;
}

/** Creates shared state handlers for localized draft forms. */
function createDraftLanguageController(config) {
    const languageKey = config.languageKey || 'language';
    const languagesKey = config.languagesKey || 'languages';
    const valuesKey = config.valuesKey || 'languageValues';
    const formFieldName = config.formFieldName || (field => field);
    const currentLanguage = config.currentLanguage || (() => SurfView.State.currentModalLanguage);

    const state = () => config.state();
    const getLanguages = draft => (config.getLanguages ? config.getLanguages(draft) : draft[languagesKey]) || [];
    const setLanguages = (draft, languages) => {
        if (config.setLanguages) {
            config.setLanguages(draft, languages);
        } else {
            draft[languagesKey] = languages;
        }
    };
    const fields = source => config.fields(source);
    const emptyValues = source => Object.fromEntries(fields(source).map(field => [field, '']));
    const form = () => typeof config.form === 'function' ? config.form() : config.form;
    const languageSource = source => config.languageSource ? config.languageSource(source) : source;

    function languagesFromSource(source, sourceFields = fields(source)) {
        if (config.languages) {
            return config.languages(source, sourceFields);
        }

        const codes = new Set();
        sourceFields.forEach(field => {
            flattenItems(Array.isArray(languageSource(source)?.[field]) ? languageSource(source)[field] : []).forEach(item => {
                if (isKnownLanguageCode(item?.language)) {
                    codes.add(item.language);
                }
            });
        });

        if (!codes.size) {
            codes.add(isKnownLanguageCode(currentLanguage()) ? currentLanguage() : 'en-GB');
        }

        return [...codes];
    }

    function initialize(source = {}) {
        const draft = state();
        const sourceFields = fields(source);
        const languages = languagesFromSource(source, sourceFields);
        const uniqueLanguages = languages.length ? [...new Set(languages)] : ['en-GB'];
        setLanguages(draft, uniqueLanguages);
        draft[languageKey] = currentLanguage() && uniqueLanguages.includes(currentLanguage())
            ? currentLanguage()
            : uniqueLanguages[0];
        draft[valuesKey] = Object.fromEntries(uniqueLanguages.map(language => [language, Object.fromEntries(
            sourceFields.map(field => [field, config.value(source, field, language)])
        )]));
    }

    function save(targetForm = form()) {
        const draft = state();
        if (!targetForm || !draft[languageKey] || config.canSave?.(targetForm) === false) {
            return;
        }

        draft[valuesKey][draft[languageKey]] = Object.fromEntries(
            fields().map(field => [field, targetForm.elements[formFieldName(field)]?.value || ''])
        );
    }

    function uniquePayloadLanguages() {
        const languages = getLanguages(state());
        const uniqueLanguages = [...new Set(languages.filter(isKnownLanguageCode))];
        if (!uniqueLanguages.length || uniqueLanguages.length !== languages.length) {
            throw new Error('Add at least one unique language.');
        }

        return uniqueLanguages;
    }

    function payloadFieldValues(field) {
        const draft = state();
        return uniquePayloadLanguages()
            .map(language => ({
                language,
                value: String(draft[valuesKey]?.[language]?.[field] || '').trim()
            }))
            .filter(item => item.value);
    }

    function payloadValues(source) {
        return Object.fromEntries(fields(source).map(field => [field, payloadFieldValues(field)]));
    }

    function validateRequiredField({field = 'name', targetForm = form()} = {}) {
        const draft = state();
        const invalidLanguage = getLanguages(draft).find(language =>
            !String(draft[valuesKey]?.[language]?.[field] || '').trim()
        ) || null;
        if (!invalidLanguage) {
            return true;
        }

        switchLanguage(invalidLanguage);
        targetForm?.elements[formFieldName(field)]?.reportValidity();
        return false;
    }

    function switchLanguage(language) {
        const targetForm = form();
        const draft = state();
        if (!targetForm || !language || language === draft[languageKey] || !getLanguages(draft).includes(language)) {
            return;
        }

        save(targetForm);
        draft[languageKey] = language;
        const values = draft[valuesKey][language] || emptyValues();
        fields().forEach(field => {
            const element = targetForm.elements[formFieldName(field)];
            if (element) {
                element.value = values[field] || '';
            }
        });
        targetForm.querySelectorAll(`[data-${config.prefix}-language-code]`).forEach(button => {
            button.classList.toggle('active', button.getAttribute(`data-${config.prefix}-language-code`) === language);
        });
        refresh(targetForm);
        config.afterSwitch?.(targetForm, language);
        const focusName = config.focusField && formFieldName(config.focusField);
        if (focusName) {
            targetForm.elements[focusName]?.focus();
        }
    }

    function add() {
        const targetForm = form();
        const draft = state();
        const select = targetForm?.querySelector(`[data-${config.prefix}-language-select]`);
        const language = select?.value || '';
        const languages = getLanguages(draft);
        if (!targetForm || !language || languages.includes(language) || !isKnownLanguageCode(language)) {
            return;
        }

        save(targetForm);
        setLanguages(draft, [...languages, language]);
        draft[valuesKey][language] = emptyValues();
        config.renderPanel?.();
        switchLanguage(language);
        config.afterAdd?.(targetForm, language);
    }

    function deleteLanguage(language) {
        const targetForm = form();
        const draft = state();
        const languages = getLanguages(draft);
        if (!targetForm || languages.length <= 1) {
            return;
        }

        save(targetForm);
        const index = languages.indexOf(language);
        if (index < 0) {
            return;
        }

        const nextLanguages = languages.filter(code => code !== language);
        setLanguages(draft, nextLanguages);
        delete draft[valuesKey][language];
        if (draft[languageKey] === language) {
            draft[languageKey] = nextLanguages[Math.min(index, nextLanguages.length - 1)];
        }
        config.renderPanel?.();
        config.afterDelete?.(targetForm, language);
    }

    function refresh(scope = form() || document) {
        const draft = state();
        const panel = scope.closest?.(`[data-${config.prefix}-localized-panel]`)
            || scope.querySelector?.(`[data-${config.prefix}-localized-panel]`)
            || scope;
        const select = panel.querySelector?.(`[data-${config.prefix}-language-select]`);
        const addButton = panel.querySelector?.(`[data-add-${config.prefix}-language]`);
        const deleteButtons = panel.querySelectorAll?.(`[data-delete-${config.prefix}-language]`) || [];
        if (!select || !addButton) {
            return;
        }

        const languages = getLanguages(draft);
        const options = draftLanguageOptions(languages);
        select.replaceChildren(...enumOptionsHtml(options.length ? options : [['', 'No languages available']], ''));
        const preferredOption = options.find(([code]) => code === 'en-GB')
            || options.find(([code]) => code === 'nl-NL')
            || options[0];
        select.value = preferredOption?.[0] || '';
        addButton.disabled = !options.length;
        deleteButtons.forEach(button => {
            button.disabled = languages.length <= 1;
        });
    }

    return {
        initialize,
        save,
        switchLanguage,
        add,
        deleteLanguage,
        refresh,
        emptyValues,
        languagesFromSource,
        payloadFieldValues,
        payloadValues,
        validateRequiredField
    };
}

/** Validates that each draft language has a required localized field value. */
function validateDraftLanguageRequiredField({draft, field = 'name', form, formFieldName = field, switchLanguage}) {
    const invalidLanguage = (draft.languages || []).find(language =>
        !String(draft.languageValues?.[language]?.[field] || '').trim()
    ) || null;
    if (!invalidLanguage) {
        return true;
    }

    switchLanguage(invalidLanguage);
    form.elements[formFieldName]?.reportValidity();
    return false;
}

// The nested form modal is shared between item types (offering prices,
// addresses). Each type registers its handlers once at load time:
//   submit         stores the edited item when the form is submitted
//   reset          clears the type's draft state when the modal closes
//   clickHandlers  delegateClick handlers scoped to the nested form
const nestedModalTypes = {};

/** Registers handlers for a nested modal item type. */
function registerNestedModalType(type, handlers) {
    nestedModalTypes[type] = handlers;
}

/** Closes the active nested modal. */
function closeNestedModal() {
    closeManagedModal(nestedOfferingModal);
    nestedOfferingForm.replaceChildren();
    delete nestedOfferingForm.dataset.nestedType;
    delete nestedOfferingForm.dataset.nestedMode;
    delete nestedOfferingForm.dataset.nestedIndex;
    Object.values(nestedModalTypes).forEach(handlers => handlers.reset?.());
}

/** Registers shared nested modal event handlers. */
function initNestedModalListeners() {
    nestedOfferingModal.close.addEventListener('click', closeNestedModal);
    addBackdropDismissListener(nestedOfferingModal, closeNestedModal);
    nestedOfferingForm.addEventListener('submit', event => {
        event.preventDefault();
        nestedModalTypes[nestedOfferingForm.dataset.nestedType]?.submit();
        closeNestedModal();
    });
    delegateClick(nestedOfferingForm, Object.assign(
        {'[data-cancel-nested]': () => closeNestedModal()},
        ...Object.values(nestedModalTypes).map(handlers => handlers.clickHandlers || {})
    ));
}

// Opens the shared nested form modal to add (index absent) or edit (index set)
// one item of a list; storeNestedItem is its counterpart on submit.
/** Opens a nested item modal for add or edit. */
function openNestedModal({type, itemName, items, index, initialize, formHtml, subtitle}) {
    nestedOfferingForm.dataset.nestedType = type;
    const editIndex = Number(index);
    const isEditing = Number.isInteger(editIndex);
    const item = isEditing ? items[editIndex] : {};
    nestedOfferingForm.dataset.nestedMode = isEditing ? 'edit' : 'add';
    if (isEditing) {
        nestedOfferingForm.dataset.nestedIndex = String(editIndex);
    } else {
        delete nestedOfferingForm.dataset.nestedIndex;
    }
    nestedOfferingModal.title.textContent = `${isEditing ? 'Edit' : 'Add'} ${itemName}`;
    nestedOfferingModal.subtitle.textContent = subtitle;
    initialize(item);
    nestedOfferingForm.replaceChildren(formHtml(item));
    if (isEditing) {
        const submitButton = nestedOfferingForm.querySelector('button[type="submit"]');
        if (submitButton) {
            submitButton.textContent = 'Save';
        }
    }
    openManagedModal(nestedOfferingModal, {
        initialFocus: () => nestedOfferingForm.querySelector('input, select, textarea')
    });
}

function storeNestedItem(items, buildItem) {
    const index = Number(nestedOfferingForm.dataset.nestedIndex);
    const isEditing = nestedOfferingForm.dataset.nestedMode === 'edit'
        && Number.isInteger(index) && index >= 0 && index < items.length;
    const item = buildItem(isEditing ? items[index] : {});
    if (isEditing) {
        items[index] = item;
    } else {
        items.push(item);
    }
}

function nestedFormActionsFragment(submitText) {
    return cloneTemplate('nested-form-actions-template', null, {submitText});
}

function nestedSummaryPanelFragment(title, buttonText, nestedType, summaryId) {
    return cloneTemplate('nested-summary-panel-template', null, {title, buttonText, nestedType, summaryId});
}

function languageSwitcherHtml({
    activeLanguage,
    buttonAttribute,
    label,
    languages = SurfView.State.draftOffering.languages,
    languageActions = null,
    headerContent = null
}) {
    const head = cloneTemplate('language-switcher-template', null, {label});
    const navItems = head.querySelector('.language-nav-items');
    languages.forEach(code => {
        const button = createElement(`button.language-nav-button${code === activeLanguage ? '.active' : ''}`, {
            text: code,
            attributes: {
                type: 'button',
                title: languageLabel(code),
                [buttonAttribute]: code
            }
        });

        if (typeof languageActions === 'function') {
            const item = createElement('span.language-nav-item');
            item.append(button, languageActions(code));
            navItems.append(item);
        } else {
            navItems.append(button);
        }
    });

    if (headerContent) {
        head.append(headerContent);
    }
    return head;
}

/** Builds a labeled form field. */
function fieldLabelHtml(labelName, type, name, value = '', options = {}, className = '') {
    const label = document.createElement('label');
    if (className) {
        label.className = className;
    }
    label.append(labelTextElement(labelName, Boolean(options.required)));
    label.append(fieldHtml(type, name, value, options));
    return label;
}

let datalistId = 0;

function fieldHtml(type, name, value = '', options = {}) {
    const fieldValue = value ?? '';
    let field;
    if (type === 'textarea') {
        field = document.createElement('textarea');
        field.name = name;
        field.value = fieldValue;
        field.textContent = fieldValue;
    } else if (type === 'datalist') {
        const wrapper = cloneTemplate('datalist-combobox-template');

        field = wrapper.querySelector('input');
        field.name = name;
        field.value = fieldValue;
        field.setAttribute('value', fieldValue);

        const list = wrapper.querySelector('.datalist-options');
        list.id = `${name}-options-${++datalistId}`;
        field.setAttribute('aria-controls', list.id);

        (options.options || []).forEach(([valueOption, label]) => {
            if (valueOption === '') {
                return;
            }
            const option = document.createElement('div');
            option.className = 'datalist-option';
            option.id = `${list.id}-${list.children.length}`;
            option.dataset.datalistValue = valueOption;
            option.setAttribute('role', 'option');
            option.textContent = options.showValue && label ? `${label} (${valueOption})` : label || valueOption;
            list.append(option);
        });

        fieldOptionsHtml(field, options);
        return wrapper;
    } else if (type === 'select') {
        field = document.createElement('select');
        field.name = name;
        enumOptionsHtml(options.options || [], fieldValue, options)
            .forEach(option => field.append(option));
        if (fieldValue && ![...field.options].some(option => option.value === fieldValue)) {
            field.append(new Option(fieldValue, fieldValue, true, true));
        }
        field.value = fieldValue;
    } else {
        field = document.createElement('input');
        field.type = type;
        field.name = name;
        field.value = fieldValue;
        field.setAttribute('value', fieldValue);
    }

    fieldOptionsHtml(field, options);
    return field;
}

function datalistCombobox(input) {
    return input.closest('.datalist-combobox');
}

function datalistList(input) {
    return datalistCombobox(input)?.querySelector('.datalist-options');
}

function visibleDatalistOptions(input) {
    return [...(datalistList(input)?.querySelectorAll('.datalist-option') || [])]
        .filter(option => !option.hidden);
}

function setActiveDatalistOption(input, option) {
    datalistList(input)?.querySelectorAll('.datalist-option.active').forEach(activeOption => {
        activeOption.classList.remove('active');
        activeOption.setAttribute('aria-selected', 'false');
    });

    if (!option) {
        input.removeAttribute('aria-activedescendant');
        return;
    }

    option.classList.add('active');
    option.setAttribute('aria-selected', 'true');
    input.setAttribute('aria-activedescendant', option.id);
    option.scrollIntoView({block: 'nearest'});
}

function filterDatalistOptions(input) {
    const query = input.value.trim().toLowerCase();
    const options = [...(datalistList(input)?.querySelectorAll('.datalist-option') || [])];
    options.forEach(option => {
        const value = String(option.dataset.datalistValue || '').toLowerCase();
        const label = option.textContent.toLowerCase();
        option.hidden = Boolean(query) && !value.includes(query) && !label.includes(query);
    });
    setActiveDatalistOption(input, visibleDatalistOptions(input)[0] || null);
}

function openDatalistOptions(input) {
    const list = datalistList(input);
    if (!list) {
        return;
    }

    filterDatalistOptions(input);
    list.hidden = false;
    datalistCombobox(input)?.classList.add('open');
    input.setAttribute('aria-expanded', 'true');
}

function closeDatalistOptions(input) {
    const list = datalistList(input);
    if (!list) {
        return;
    }

    list.hidden = true;
    datalistCombobox(input)?.classList.remove('open');
    input.setAttribute('aria-expanded', 'false');
    setActiveDatalistOption(input, null);
}

function selectDatalistOption(input, option) {
    if (!option) {
        return;
    }

    input.value = option.dataset.datalistValue || option.textContent;
    input.dispatchEvent(new Event('input', {bubbles: true}));
    input.dispatchEvent(new Event('change', {bubbles: true}));
    closeDatalistOptions(input);
    input.focus();
}

document.addEventListener('focusin', event => {
    if (event.target.matches('[data-datalist-input]')) {
        openDatalistOptions(event.target);
    }
});

document.addEventListener('input', event => {
    if (event.target.matches('[data-datalist-input]')) {
        openDatalistOptions(event.target);
    }
});

document.addEventListener('keydown', event => {
    const input = event.target.matches?.('[data-datalist-input]') ? event.target : null;
    if (!input) {
        return;
    }

    if (event.key === 'ArrowDown') {
        event.preventDefault();
        openDatalistOptions(input);
        const options = visibleDatalistOptions(input);
        const active = datalistList(input)?.querySelector('.datalist-option.active');
        const activeIndex = active ? options.indexOf(active) : -1;
        setActiveDatalistOption(input, options[Math.min(activeIndex + 1, options.length - 1)] || options[0] || null);
    } else if (event.key === 'ArrowUp') {
        event.preventDefault();
        openDatalistOptions(input);
        const options = visibleDatalistOptions(input);
        const active = datalistList(input)?.querySelector('.datalist-option.active');
        const activeIndex = active ? options.indexOf(active) : -1;
        setActiveDatalistOption(input, options[Math.max(activeIndex - 1, 0)] || options[options.length - 1] || null);
    } else if (event.key === 'Enter' && input.getAttribute('aria-expanded') === 'true') {
        const active = datalistList(input)?.querySelector('.datalist-option.active');
        if (!active) {
            return;
        }
        event.preventDefault();
        selectDatalistOption(input, active);
    } else if (event.key === 'Escape') {
        closeDatalistOptions(input);
    } else if (event.key === 'Tab') {
        closeDatalistOptions(input);
    }
});

document.addEventListener('mousedown', event => {
    const option = event.target.closest?.('.datalist-option');
    if (option) {
        event.preventDefault();
        const input = datalistCombobox(option)?.querySelector('[data-datalist-input]');
        selectDatalistOption(input, option);
        return;
    }

    document.querySelectorAll('[data-datalist-input]').forEach(input => {
        if (!datalistCombobox(input)?.contains(event.target)) {
            closeDatalistOptions(input);
        }
    });
});

function fieldOptionsHtml(field, options = {}) {
    if (field.matches?.('input, textarea')) {
        field.autocomplete = options.autocomplete || 'off';
    }
    if (options.required) {
        field.required = true;
    }
    if (options.readonly) {
        field.readOnly = true;
    }
    if (options.placeholder) {
        field.placeholder = options.placeholder;
    }
    ['step', 'min', 'max'].forEach(attribute => {
        if (options[attribute] !== undefined && options[attribute] !== null) {
            field.setAttribute(attribute, String(options[attribute]));
        }
    });
}

function enumOptionsHtml(options, selectedValue, optionsConfig = {}) {
    return options.map(([value, label]) => {
        const displayLabel = optionsConfig.showValue ? `${label} (${value})` : label;
        const option = createElement('option', {text: displayLabel});
        option.value = value;
        option.selected = value === selectedValue;
        if (option.selected) {
            option.setAttribute('selected', '');
        }
        return option;
    });
}

function labelTextElement(labelName, required) {
    const labelText = createElement('span.label-text');
    labelText.append(document.createTextNode(labelName));
    if (required) {
        labelText.append(document.createTextNode(' '));
        const marker = createElement('span.required-marker', {
            text: '*',
            attributes: {'aria-label': 'required'}
        });
        labelText.append(marker);
    }
    return labelText;
}

/** Creates a text-only element. */
function textElement(tagName, text, className = '') {
    return createElement(className ? `${tagName}.${className.split(/\s+/).filter(Boolean).join('.')}` : tagName, {
        text: text ?? ''
    });
}

function detailValueElement(value) {
    const element = createElement('div.detail-value');
    if (value !== null && value !== undefined && value !== '') {
        element.textContent = String(value);
    } else {
        element.append(textElement('span', '-', 'muted'));
    }
    return element;
}

/** Builds a labeled detail field. */
function detailFieldElement(label, value) {
    return cloneTemplate('detail-field-template', field => {
        field.querySelector('[data-detail-value-slot]').replaceWith(detailValueElement(value));
    }, {label});
}

/** Creates a detail field grid. */
function detailGridElement() {
    return createElement('div.detail-grid');
}

/** Creates the shared entity edit form shell. */
function entityEditFormElement(kind, mode = '') {
    const form = createElement('form#entity-edit-form.offering-form.entity-edit-form', {
        dataset: {entityKind: kind},
        attributes: {autocomplete: 'off'}
    });
    if (mode) {
        form.dataset.entityMode = mode;
    }
    return form;
}

/** Builds shared entity edit form actions. */
function entityEditFormActions(submitText = 'Save') {
    return cloneTemplate('offering-form-actions-template', null, {
        cancelAttribute: 'data-cancel-entity-edit',
        statusId: 'entity-edit-form-status',
        submitText
    });
}

/** Builds a detail section with an optional heading. */
function detailSectionElement(id, heading) {
    const section = createElement(id ? `section#${id}.detail-section` : 'section.detail-section');
    if (heading) {
        section.append(textElement('h3', heading));
    }
    return section;
}

/** Builds a detail navigation section. */
function detailNavSectionElement(label, content) {
    return cloneTemplate('detail-nav-section-template', section => {
        section.append(content);
    }, {label});
}

/** Builds detail language navigation links. */
function languageDetailNavElement(hrefForLanguage = () => '#') {
    if (!SurfView.State.currentModalLanguages.length) {
        return textElement('span', 'No language-specific content', 'detail-nav-empty');
    }
    const items = createElement('div.language-nav-items');
    SurfView.State.currentModalLanguages.forEach(code => {
        items.append(createElement(`a.language-nav-link${code === SurfView.State.currentModalLanguage ? '.active' : ''}`, {
            text: code,
            dataset: {languageCode: code},
            attributes: {
                href: hrefForLanguage(code),
                title: languageLabel(code)
            }
        }));
    });
    return items;
}

function anchorElement(href, text, className = '') {
    return createElement(className ? `a.${className.split(/\s+/).filter(Boolean).join('.')}` : 'a', {
        text,
        attributes: {href}
    });
}

function jsonBoxElement(value) {
    return createElement('pre.json-box', {text: JSON.stringify(value, null, 2)});
}

function nestedListHtml(items, label, emptyText, options = {}) {
    if (!items.length) {
        return createElement('div.nested-item-empty', {text: emptyText});
    }
    const deleteAttribute = options.deleteAttribute || '';
    const editAttribute = options.editAttribute || '';
    const itemName = options.itemName || 'item';
    const list = createElement('ul.nested-item-list');
    items.forEach((item, index) => {
        list.append(cloneTemplate('nested-item-row-template', row => {
            const editButton = row.querySelector('.nested-item-edit');
            if (editAttribute) {
                editButton.setAttribute(editAttribute, String(index));
                editButton.setAttribute('aria-label', `Edit ${itemName}`);
            } else {
                editButton.remove();
            }

            const deleteButton = row.querySelector('.nested-item-delete');
            if (deleteAttribute) {
                deleteButton.setAttribute(deleteAttribute, String(index));
                deleteButton.setAttribute('aria-label', `Delete ${itemName}`);
            } else {
                deleteButton.remove();
            }
        }, {label: label(item)}));
    });
    return list;
}

function codeValue(value) {
    if (!value) {
        return '';
    }
    return createElement('span.code', {text: typeof value === 'string' ? value : value?.code || ''});
}

function entityLinkValue(kind, entity) {
    const id = entityId(kind, entity);
    const name = entityDisplayName(kind, entity) || id || '';

    if (!id) {
        return name;
    }

    return createElement('button.course-name-button', {
        text: name,
        dataset: {entityKind: kind, entityId: id},
        attributes: {type: 'button'}
    });
}

/** Builds the entity table for a tab. */
function tableElement(tab, entities) {
    const table = createElement('table');
    const thead = createElement('thead');
    const headerRow = createElement('tr');
    tab.columns.forEach(([label]) => {
        const header = createElement('th', {
            text: label,
            attributes: {scope: 'col'}
        });
        headerRow.append(header);
    });
    thead.append(headerRow);

    const tbody = createElement('tbody');
    entities.forEach(entity => {
        const row = createElement('tr');
        tab.columns.forEach(([, getter], index) => {
            const cell = createElement(index === 0 ? 'td.name-cell' : 'td');
            cell.append(tableCellContent(getter(entity)));
            row.append(cell);
        });
        tbody.append(row);
    });

    table.append(thead, tbody);
    return table;
}

function tableCellContent(value) {
    if (value instanceof Node) {
        return value;
    }
    if (value === null || value === undefined || value === '') {
        return createElement('span.muted', {text: '-'});
    }
    return document.createTextNode(String(value));
}
