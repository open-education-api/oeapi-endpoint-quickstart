function languageSpecificSectionFragment({activeLanguage, buttonAttribute, label, fields}) {
    const section = document.createElement('section');
    section.className = 'nested-item-panel language-specific-section';

    section.append(languageSwitcherHtml({activeLanguage, buttonAttribute, label}));

    const fieldsElement = document.createElement('div');
    fieldsElement.className = 'language-specific-fields';
    fieldsElement.append(...fields);
    section.append(fieldsElement);
    return section;
}

function nestedFormActionsFragment(submitText) {
    const actions = document.createElement('div');
    actions.className = 'offering-form-actions';

    const cancelButton = document.createElement('button');
    cancelButton.className = 'detail-action-button';
    cancelButton.type = 'button';
    cancelButton.dataset.cancelNested = '';
    cancelButton.textContent = 'Cancel';

    const submitButton = document.createElement('button');
    submitButton.className = 'detail-action-button';
    submitButton.type = 'submit';
    submitButton.textContent = submitText;

    actions.append(cancelButton, submitButton);
    return actions;
}

function nestedSummaryPanelFragment(title, buttonText, nestedType, summaryId) {
    const section = document.createElement('section');
    section.className = 'nested-item-panel';

    const head = document.createElement('div');
    head.className = 'nested-item-head';

    const heading = document.createElement('h3');
    heading.textContent = title;

    const button = document.createElement('button');
    button.className = 'detail-action-button';
    button.type = 'button';
    button.dataset.openNestedOffering = nestedType;
    button.textContent = buttonText;

    const summary = document.createElement('div');
    summary.id = summaryId;

    head.append(heading, button);
    section.append(head, summary);
    return section;
}

function offeringFormActionsFragment(submitText) {
    const actions = document.createElement('div');
    actions.className = 'offering-form-actions';

    const status = document.createElement('span');
    status.className = 'offering-form-status';
    status.id = 'offering-form-status';

    const cancelButton = document.createElement('button');
    cancelButton.className = 'pager-button detail-action-button';
    cancelButton.type = 'button';
    cancelButton.dataset.cancelOffering = '';
    cancelButton.textContent = 'Cancel';

    const submitButton = document.createElement('button');
    submitButton.className = 'detail-action-button';
    submitButton.type = 'submit';
    submitButton.textContent = submitText;

    actions.append(status, cancelButton, submitButton);
    return actions;
}

function languageSwitcherHtml({activeLanguage, buttonAttribute, label}) {
    const head = document.createElement('div');
    head.className = 'language-section-head';

    const labelElement = document.createElement('div');
    labelElement.className = 'detail-label';
    labelElement.textContent = label;

    const navItems = document.createElement('div');
    navItems.className = 'language-nav-items';
    draftOffering.languages.forEach(code => {
        const button = document.createElement('button');
        button.className = `language-nav-button${code === activeLanguage ? ' active' : ''}`;
        button.type = 'button';
        button.setAttribute(buttonAttribute, code);
        button.title = languageLabel(code);
        button.textContent = code;
        navItems.append(button);
    });

    head.append(labelElement, navItems);
    return head;
}

function fieldLabelHtml(labelName, type, name, value = '', options = {}, className = '') {
    const label = document.createElement('label');
    if (className) {
        label.className = className;
    }
    label.append(labelTextElement(labelName, Boolean(options.required)));
    label.append(fieldHtml(type, name, value, options));
    return label;
}

function fieldHtml(type, name, value = '', options = {}) {
    const fieldValue = value ?? '';
    let field;
    if (type === 'textarea') {
        field = document.createElement('textarea');
        field.name = name;
        field.value = fieldValue;
        field.textContent = fieldValue;
    } else if (type === 'select') {
        field = document.createElement('select');
        field.name = name;
        enumOptionsHtml(options.options || [], fieldValue, options)
            .forEach(option => field.append(option));
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

function fieldOptionsHtml(field, options = {}) {
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
        const option = document.createElement('option');
        const displayLabel = optionsConfig.showValue ? `${label} (${value})` : label;
        option.value = value;
        option.textContent = displayLabel;
        option.selected = value === selectedValue;
        if (option.selected) {
            option.setAttribute('selected', '');
        }
        return option;
    });
}

function labelTextElement(labelName, required) {
    const labelText = document.createElement('span');
    labelText.className = 'label-text';
    labelText.append(document.createTextNode(labelName));
    if (required) {
        labelText.append(document.createTextNode(' '));
        const marker = document.createElement('span');
        marker.className = 'required-marker';
        marker.setAttribute('aria-label', 'required');
        marker.textContent = '*';
        labelText.append(marker);
    }
    return labelText;
}

function textElement(tagName, text, className = '') {
    const element = document.createElement(tagName);
    if (className) {
        element.className = className;
    }
    element.textContent = text ?? '';
    return element;
}

function detailValueElement(value) {
    const element = document.createElement('div');
    element.className = 'detail-value';
    if (value !== null && value !== undefined && value !== '') {
        element.textContent = String(value);
    } else {
        element.append(textElement('span', '-', 'muted'));
    }
    return element;
}

function detailFieldElement(label, value) {
    const field = document.createElement('div');
    field.className = 'detail-field';
    field.append(
        textElement('div', label, 'detail-label'),
        detailValueElement(value)
    );
    return field;
}

function detailSectionElement(id, heading) {
    const section = document.createElement('section');
    section.className = 'detail-section';
    if (id) {
        section.id = id;
    }
    if (heading) {
        section.append(textElement('h3', heading));
    }
    return section;
}

function detailNavSectionElement(label, content) {
    const section = document.createElement('div');
    section.className = 'detail-nav-section';
    const heading = textElement('div', label, 'detail-nav-label');
    heading.setAttribute('role', 'heading');
    heading.setAttribute('aria-level', '3');
    section.append(heading, content);
    return section;
}

function anchorElement(href, text, className = '') {
    const link = document.createElement('a');
    link.href = href;
    link.textContent = text;
    if (className) {
        link.className = className;
    }
    return link;
}

function jsonBoxElement(value) {
    const pre = document.createElement('pre');
    pre.className = 'json-box';
    pre.textContent = JSON.stringify(value, null, 2);
    return pre;
}

function nestedListHtml(items, label, emptyText, options = {}) {
    if (!items.length) {
        const empty = document.createElement('div');
        empty.className = 'nested-item-empty';
        empty.textContent = emptyText;
        return empty;
    }
    const deleteAttribute = options.deleteAttribute || '';
    const editAttribute = options.editAttribute || '';
    const list = document.createElement('ul');
    list.className = 'nested-item-list';
    items.forEach((item, index) => {
        const row = document.createElement('li');

        const labelElement = document.createElement('span');
        labelElement.textContent = label(item);

        const actions = document.createElement('span');
        actions.className = 'nested-item-actions';

        if (editAttribute) {
            const editButton = document.createElement('button');
            editButton.className = 'nested-item-edit';
            editButton.type = 'button';
            editButton.setAttribute(editAttribute, String(index));
            editButton.setAttribute('aria-label', `Edit ${options.itemName || 'item'}`);
            editButton.textContent = 'Edit';
            actions.append(editButton);
        }

        if (deleteAttribute) {
            const deleteButton = document.createElement('button');
            deleteButton.className = 'nested-item-delete';
            deleteButton.type = 'button';
            deleteButton.setAttribute(deleteAttribute, String(index));
            deleteButton.setAttribute('aria-label', `Delete ${options.itemName || 'item'}`);
            deleteButton.textContent = 'Delete';
            actions.append(deleteButton);
        }

        row.append(labelElement, actions);
        list.append(row);
    });
    return list;
}

function tableElement(tab, entities) {
    const table = document.createElement('table');
    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    tab.columns.forEach(([label]) => {
        const header = document.createElement('th');
        header.scope = 'col';
        header.textContent = label;
        headerRow.append(header);
    });
    thead.append(headerRow);

    const tbody = document.createElement('tbody');
    entities.forEach(entity => {
        const row = document.createElement('tr');
        tab.columns.forEach(([, getter], index) => {
            const cell = document.createElement('td');
            if (index === 0) {
                cell.className = 'name-cell';
            }
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
        const empty = document.createElement('span');
        empty.className = 'muted';
        empty.textContent = '-';
        return empty;
    }
    return document.createTextNode(String(value));
}
