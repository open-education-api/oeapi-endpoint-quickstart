function openAcademicSessionModal() {
    academicSessionForm.replaceChildren(academicSessionFormHtml());
    addAcademicSessionNameRow('en-GB');
    academicSessionModal.backdrop.classList.add('open');
    academicSessionForm.querySelector('input[name="academicSessionId"]').focus();
}

function closeAcademicSessionModal() {
    academicSessionModal.backdrop.classList.remove('open');
    academicSessionForm.replaceChildren();
}

function academicSessionFormHtml() {
    const sessionId = generateId();
    const fragment = document.createDocumentFragment();
    fragment.append(
        fieldLabelHtml('Academic session ID', 'text', 'academicSessionId', sessionId, {required: true, readonly: true}, 'full-width'),
        fieldLabelHtml('Academic session type', 'select', 'academicSessionType', 'semester', {
            required: true,
            options: Constants.enumOptions.academicSessionType
        }, 'full-width'),
        fieldLabelHtml('Primary code type', 'select', 'primaryCodeType', 'identifier', {
            required: true,
            options: Constants.enumOptions.academicSessionCodeType
        }),
        fieldLabelHtml('Primary code', 'text', 'primaryCode', '', {required: true}),
        fieldLabelHtml('Start date', 'date', 'startDate', '', {required: true}),
        fieldLabelHtml('End date', 'date', 'endDate', '', {required: true}),
        academicSessionNamesPanel(),
        academicSessionFormActions()
    );
    return fragment;
}

function academicSessionNamesPanel() {
    const section = document.createElement('section');
    section.className = 'nested-item-panel academic-session-names-panel';

    const head = document.createElement('div');
    head.className = 'nested-item-head';

    const heading = document.createElement('h3');
    heading.textContent = 'Localized names';

    const addButton = document.createElement('button');
    addButton.className = 'detail-action-button';
    addButton.type = 'button';
    addButton.dataset.addAcademicSessionName = '';
    addButton.textContent = 'Add name';

    const nameList = document.createElement('div');
    nameList.className = 'academic-session-name-list';
    nameList.id = 'academic-session-name-list';

    head.append(heading, addButton);
    section.append(head, nameList);
    return section;
}

function academicSessionFormActions() {
    const actions = document.createElement('div');
    actions.className = 'offering-form-actions';

    const status = document.createElement('span');
    status.className = 'offering-form-status';
    status.id = 'academic-session-form-status';

    const cancelButton = document.createElement('button');
    cancelButton.className = 'pager-button detail-action-button';
    cancelButton.type = 'button';
    cancelButton.dataset.cancelAcademicSession = '';
    cancelButton.textContent = 'Cancel';

    const submitButton = document.createElement('button');
    submitButton.className = 'detail-action-button';
    submitButton.type = 'submit';
    submitButton.textContent = 'Add';

    actions.append(status, cancelButton, submitButton);
    return actions;
}

function addAcademicSessionNameRow(language = '', value = '') {
    const nameList = academicSessionForm.querySelector('#academic-session-name-list');
    if (!nameList) {
        return;
    }

    const row = document.createElement('div');
    row.className = 'academic-session-name-row';
    const deleteButton = document.createElement('button');
    deleteButton.className = 'nested-item-delete academic-session-name-delete';
    deleteButton.type = 'button';
    deleteButton.dataset.deleteAcademicSessionName = '';
    deleteButton.textContent = 'Delete';
    row.append(
        fieldLabelHtml('Language', 'select', 'nameLanguage', language || 'en-GB', {
            required: true,
            options: academicSessionLanguageOptions()
        }),
        fieldLabelHtml('Name', 'text', 'nameValue', value, {required: true}),
        deleteButton
    );
    nameList.append(row);
}

function academicSessionLanguageOptions() {
    return Object.entries(Constants.languageNames)
        .sort(([, leftLabel], [, rightLabel]) => leftLabel.localeCompare(rightLabel))
        .map(([code, label]) => [code, `${label} (${code})`]);
}

async function submitAcademicSessionForm(event) {
    event.preventDefault();

    const cancelButton = event.submitter?.closest?.('[data-cancel-academic-session]');
    if (cancelButton) {
        closeAcademicSessionModal();
        return;
    }

    const status = document.getElementById('academic-session-form-status');
    const submitButton = academicSessionForm.querySelector('button[type="submit"]');
    const payload = academicSessionPayloadFromForm();
    if (!payload.name?.length) {
        status.classList.add('error');
        status.textContent = 'Add at least one localized name';
        return;
    }

    status.classList.remove('error');
    status.replaceChildren();
    status.textContent = 'Adding...';
    submitButton.disabled = true;

    try {
        const response = await fetch('/academic-sessions', {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                ...authorizationHeader()
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw await requestErrorFromResponse(response);
        }

        status.textContent = 'Added';
        academicSessionsLoadPromise = null;
        closeAcademicSessionModal();
        if (state.tab === 'academicSessions') {
            await loadCurrentPage();
        }
    } catch (error) {
        renderOfferingFormError(status, error, 'Unable to add academic session');
    } finally {
        submitButton.disabled = false;
    }
}

function academicSessionPayloadFromForm() {
    const formData = new FormData(academicSessionForm);
    const value = name => String(formData.get(name) || '').trim();
    const names = [...academicSessionForm.querySelectorAll('.academic-session-name-row')]
        .map(row => ({
            language: row.querySelector('[name="nameLanguage"]')?.value || '',
            value: row.querySelector('[name="nameValue"]')?.value.trim() || ''
        }))
        .filter(name => name.language && name.value);

    return removeEmptyPayloadValues({
        academicSessionId: value('academicSessionId'),
        academicSessionType: value('academicSessionType'),
        primaryCode: {
            codeType: value('primaryCodeType'),
            code: value('primaryCode')
        },
        name: names,
        startDate: value('startDate'),
        endDate: value('endDate')
    });
}
