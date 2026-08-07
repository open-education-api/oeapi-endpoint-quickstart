window.SurfView = window.SurfView || {};

/** Loads all persons into the lookup cache. */
async function loadPersons() {
    if (personsLoadPromise) {
        return personsLoadPromise;
    }

    personsLoadPromise = (async () => {
        personById.clear();
        const items = await loadAllEndpointItems('/persons');
        items.forEach(person => {
            const id = entityId('person', person);
            if (id) {
                personById.set(String(id), person);
            }
        });
    })().catch(error => {
        personsLoadPromise = null;
        throw error;
    });

    return personsLoadPromise;
}

SurfView.Persons = (() => {

    /** Builds the person read-only detail view. */
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
        const content = createElement('div.detail-content.person-detail-content');
        const section = detailSectionElement('', 'Person');
        const grid = detailGridElement();
        grid.append(...fields.map(([label, value]) => detailFieldElement(label, value)));
        section.append(grid);
        content.append(section);
        return content;
    }

    function personFormFragment(person, mode = 'edit') {
        const form = entityEditFormElement('person', mode);
        form.append(
            ...primaryCodeFormFields({
                idLabel: 'Person ID',
                idName: 'personId',
                idValue: person.personId,
                primaryCode: person.primaryCode
            }),
            fieldLabelHtml('Given name', 'text', 'givenName', person.givenName || '', {required: true}),
            fieldLabelHtml('Surname', 'text', 'surname', person.surname || '', {required: true}),
            fieldLabelHtml('Display name', 'text', 'displayName', person.displayName || '', {required: true}, 'full-width'),
            fieldLabelHtml('Email', 'email', 'mail', person.mail || '', {required: true}, 'full-width'),
            fieldLabelHtml('Active enrollment', 'select', 'activeEnrollment', String(person.activeEnrollment ?? false), {
                options: Constants.booleanOptions
            }),
            personAffiliationsPanel(person.affiliations),
            entityEditFormActions(mode === 'edit' ? 'Save' : 'Add')
        );
        return form;
    }

    function personAffiliationsPanel(affiliations = []) {
        const selected = new Set(Array.isArray(affiliations) ? affiliations : []);
        const fieldset = createElement('fieldset.person-affiliations.full-width');
        const legend = createElement('legend', {text: 'Affiliations'});
        fieldset.append(legend);
        Constants.enumOptions.personAffiliations.forEach(([value, label]) => {
            const row = createElement('label.checkbox-row');
            const input = createElement('input', {
                attributes: {type: 'checkbox', name: 'affiliations', value}
            });
            input.checked = selected.has(value);
            row.append(input, document.createTextNode(` ${label}`));
            fieldset.append(row);
        });
        return fieldset;
    }

    /** Opens the shared modal with a new person form. */
    function openCreatePersonModal() {
        if (isReadOnlyMode()) {
            return;
        }
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
        SurfView.State.currentModalKind = 'person';
        SurfView.State.currentModalEntity = person;
        SurfView.State.currentModalOfferings = [];
        SurfView.State.currentModalLanguages = [];
        SurfView.State.currentModalLanguage = null;
        moduleModal.title.textContent = 'Add person';
        setEntityDeletePending(false);
        setEntityModalMode('form');
        modalBody.replaceChildren(personFormFragment(person, 'create'));
        openManagedModal(moduleModal, {
            initialFocus: () => modalBody.querySelector('input[name="primaryCode"]')
        });
    }

    /** Opens the shared modal with a person edit form. */
    function openPersonEditForm(person) {
        if (isReadOnlyMode()) {
            return;
        }
        modalBody.replaceChildren(personFormFragment(person, 'edit'));
        setEntityModalMode('form');
        modalBody.querySelector('input[name="givenName"]')?.focus();
    }

    /** Reads a person API payload from a form. */
    function personPayloadFromForm(form, originalPerson = {}) {
        const data = new FormData(form);
        const value = name => String(data.get(name) || '').trim();
        const payload = structuredClone(originalPerson || {});
        payload.personId = value('personId');
        payload.primaryCode = primaryCodePayloadFromFormData(data);
        payload.givenName = value('givenName');
        payload.surname = value('surname');
        payload.displayName = value('displayName');
        payload.mail = value('mail');
        payload.activeEnrollment = value('activeEnrollment') === 'true';
        payload.affiliations = data.getAll('affiliations').map(String);
        return removeEmptyPayloadValues(payload);
    }

    /** Returns the display label for a person. */
    function personLabel(person) {
        return entityDisplayName('person', person) || entityId('person', person) || '';
    }

    /** Resolves a person id to display text. */
    function personLabelById(id) {
        if (!id) {
            return '';
        }

        return personLabel(personById.get(String(id))) || UNKNOWN_REFERENCE_LABEL;
    }

    /** Builds person select option entries. */
    function personOptionEntries({selectedPersonId = '', excludedPersonIds = []} = {}) {
        const selected = String(selectedPersonId || '');
        const excluded = new Set(excludedPersonIds.map(String));
        const items = [...personById.values()].filter(person => {
            const id = entityId('person', person);
            return id && (!excluded.has(String(id)) || String(id) === selected);
        });

        return entityOptionEntries({
            items,
            selectedId: selected,
            emptyLabel: items.length ? 'Select person' : 'No persons available',
            label: personLabel,
            idValue: person => entityId('person', person)
        });
    }

    /** Submits a person create or edit form. */
    async function submitPersonForm(form) {
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
            payload: () => personPayloadFromForm(form, SurfView.State.currentModalEntity),
            endpoint: payload => mode === 'create' ? '/persons' : `/persons/${encodeURIComponent(payload.personId)}`,
            method: mode === 'create' ? 'POST' : 'PUT',
            savingText: mode === 'edit' ? 'Saving...' : 'Adding...',
            successText: mode === 'edit' ? 'Saved' : 'Added',
            preserveArrayFields: ['affiliations'],
            errorMessage: mode === 'edit' ? 'Unable to save person' : 'Unable to add person',
            onSuccess: ({payload, updatedEntity: updatedPerson}) => {
                const id = payload.personId;
                applySubmitEntitySuccess({
                    kind: 'person',
                    mode,
                    updatedEntity: updatedPerson,
                    id,
                    cacheMap: personById,
                    title: person => entityDisplayName('person', person) || id,
                    detailsHtml: personDetailsHtml
                });
            }
        });
    }

    /** Deletes the person currently open in the detail modal. */
    async function deleteCurrentPerson() {
        await deleteCurrentEntity({
            kind: 'person',
            id: SurfView.State.currentModalEntity?.personId,
            endpoint: id => `/persons/${encodeURIComponent(id)}`,
            confirmMessage: 'Are you sure you want to delete this person?',
            cacheMap: personById,
            errorMessage: 'Unable to delete person'
        });
    }

    return {
        openCreatePersonModal,
        openPersonEditForm,
        personDetailsHtml,
        personPayloadFromForm,
        submitPersonForm,
        deleteCurrentPerson,
        personLabel,
        personLabelById,
        personOptionEntries
    };

})();
