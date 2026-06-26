function personDetailsHtml(person) {
    const fields = [
        ['Person ID', person.personId],
        ['Primary code', plainCodeValue(person.primaryCode)],
        ['Given name', person.givenName],
        ['Surname', person.surname],
        ['Display name', person.displayName],
        ['Email', person.mail],
        ['Active enrollment', person.activeEnrollment ? 'Yes' : 'No'],
        ['Affiliations', Array.isArray(person.affiliations) ? person.affiliations.join(', ') : '']
    ];
    const content = document.createElement('div');
    content.className = 'detail-content person-detail-content';
    const section = detailSectionElement('', 'Person');
    const grid = document.createElement('div');
    grid.className = 'detail-grid';
    grid.append(...fields.map(([label, value]) => detailFieldElement(label, value)));
    section.append(grid);
    content.append(section);
    return content;
}

function personFormFragment(person, mode = 'edit') {
    const form = document.createElement('form');
    const primaryCode = primaryCodeParts(person.primaryCode, person.personId);
    form.className = 'offering-form entity-edit-form';
    form.id = 'entity-edit-form';
    form.dataset.entityKind = 'person';
    form.dataset.entityMode = mode;
    form.append(
        fieldLabelHtml('Person ID', 'text', 'personId', person.personId || generateId(), {
            required: true,
            readonly: true
        }, 'full-width'),
        fieldLabelHtml('Primary code type', 'select', 'primaryCodeType', primaryCode.codeType, {
            required: true,
            options: Constants.enumOptions.offeringCodeType
        }),
        fieldLabelHtml('Primary code', 'text', 'primaryCode', primaryCode.code, {required: true}),
        fieldLabelHtml('Given name', 'text', 'givenName', person.givenName || '', {required: true}),
        fieldLabelHtml('Surname', 'text', 'surname', person.surname || '', {required: true}),
        fieldLabelHtml('Display name', 'text', 'displayName', person.displayName || '', {required: true}, 'full-width'),
        fieldLabelHtml('Email', 'email', 'mail', person.mail || '', {required: true}, 'full-width'),
        fieldLabelHtml('Active enrollment', 'select', 'activeEnrollment', String(person.activeEnrollment ?? false), {
            options: Constants.enumOptions.resultExpected
        }),
        personAffiliationsPanel(person.affiliations),
        entityEditFormActions()
    );
    return form;
}

function personAffiliationsPanel(affiliations = []) {
    const selected = new Set(Array.isArray(affiliations) ? affiliations : []);
    const fieldset = document.createElement('fieldset');
    fieldset.className = 'person-affiliations full-width';
    const legend = document.createElement('legend');
    legend.textContent = 'Affiliations';
    fieldset.append(legend);
    Constants.enumOptions.personAffiliations.forEach(([value, label]) => {
        const row = document.createElement('label');
        row.className = 'checkbox-row';
        const input = document.createElement('input');
        input.type = 'checkbox';
        input.name = 'affiliations';
        input.value = value;
        input.checked = selected.has(value);
        row.append(input, document.createTextNode(` ${label}`));
        fieldset.append(row);
    });
    return fieldset;
}

function openCreatePersonModal() {
    const person = {
        personId: generateId(),
        primaryCode: {codeType: 'identifier', code: ''},
        givenName: '',
        surname: '',
        displayName: '',
        activeEnrollment: false,
        affiliations: ['employee'],
        mail: ''
    };
    currentModalKind = 'person';
    currentModalEntity = person;
    currentModalOfferings = [];
    currentModalLanguages = [];
    currentModalLanguage = null;
    courseModal.title.textContent = 'Add person';
    entityEditButton.classList.add('hidden');
    entityDeleteButton.classList.add('hidden');
    entityJsonPreviewButton.classList.remove('hidden');
    modalBody.replaceChildren(personFormFragment(person, 'create'));
    courseModal.backdrop.classList.add('open');
    document.body.style.overflow = 'hidden';
    modalBody.querySelector('input[name="primaryCode"]')?.focus();
}

function openPersonEditForm(person) {
    modalBody.replaceChildren(personFormFragment(person, 'edit'));
    entityEditButton.classList.add('hidden');
    entityDeleteButton.classList.add('hidden');
    entityJsonPreviewButton.classList.remove('hidden');
    modalBody.querySelector('input[name="givenName"]')?.focus();
}

function personPayloadFromForm(form, originalPerson = {}) {
    const data = new FormData(form);
    const value = name => String(data.get(name) || '').trim();
    const payload = structuredClone(originalPerson || {});
    payload.personId = value('personId');
    payload.primaryCode = {
        codeType: value('primaryCodeType'),
        code: value('primaryCode')
    };
    payload.givenName = value('givenName');
    payload.surname = value('surname');
    payload.displayName = value('displayName');
    payload.mail = value('mail');
    payload.activeEnrollment = value('activeEnrollment') === 'true';
    payload.affiliations = data.getAll('affiliations').map(String);
    return removeEmptyPayloadValues(payload);
}

async function submitPersonForm(form) {
    const mode = form.dataset.entityMode;
    const status = document.getElementById('entity-edit-form-status');
    const submitButton = form.querySelector('button[type="submit"]');
    const payload = personPayloadFromForm(form, currentModalEntity);
    const id = payload.personId;
    status.classList.remove('error');
    status.textContent = 'Saving...';
    submitButton.disabled = true;
    try {
        const response = await callEndpoint(mode === 'create' ? '/persons' : `/persons/${encodeURIComponent(id)}`, {
            method: mode === 'create' ? 'POST' : 'PUT',
            headers: {'Accept': 'application/json', 'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            throw await requestErrorFromResponse(response);
        }
        const responseText = await response.text();
        let responsePerson = {};
        try {
            const parsed = responseText ? JSON.parse(responseText) : {};
            responsePerson = parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
        } catch (error) {
            responsePerson = {};
        }
        const updatedPerson = {...payload, ...responsePerson};
        if (!Array.isArray(updatedPerson.affiliations)) {
            updatedPerson.affiliations = payload.affiliations;
        }
        currentModalEntity = updatedPerson;
        personById.set(String(id), updatedPerson);
        currentPageEntities = mode === 'create'
            ? [updatedPerson, ...currentPageEntities]
            : currentPageEntities.map(item => item.personId === id ? updatedPerson : item);
        courseModal.title.textContent = entityDisplayName('person', updatedPerson) || id;
        entityEditButton.classList.remove('hidden');
        entityDeleteButton.classList.remove('hidden');
        entityJsonPreviewButton.classList.add('hidden');
        modalBody.replaceChildren(personDetailsHtml(updatedPerson));
        updateEntityUrl('person', id, null, mode === 'create');
        renderCurrentEntities();
    } catch (error) {
        renderOfferingFormError(status, error, 'Unable to save person');
    } finally {
        submitButton.disabled = false;
    }
}

async function deleteCurrentPerson() {
    const id = currentModalEntity?.personId;
    if (!id || !window.confirm(`Are you sure you want to delete this person?`)) {
        return;
    }
    entityDeleteButton.disabled = true;
    try {
        const response = await callEndpoint(`/persons/${encodeURIComponent(id)}`, {
            method: 'DELETE',
            headers: {'Accept': 'application/json'}
        });
        if (!response.ok) {
            throw await requestErrorFromResponse(response);
        }
        personById.delete(String(id));
        currentPageEntities = currentPageEntities.filter(item => item.personId !== id);
        closeEntityModal();
        renderCurrentEntities();
    } catch (error) {
        openErrorDetailsModal(error, 'Unable to delete person');
    } finally {
        entityDeleteButton.disabled = false;
    }
}
