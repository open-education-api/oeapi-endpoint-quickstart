window.SurfView = window.SurfView || {};

/** Loads all academic sessions into the lookup cache. */
async function loadAcademicSessions() {
    if (academicSessionsLoadPromise) {
        return academicSessionsLoadPromise;
    }

    academicSessionsLoadPromise = (async () => {
        academicSessionsById.clear();
        const items = await loadAllEndpointItems('/academic-sessions');
        items.forEach(academicSession => {
            const id = academicSessionIdValue(academicSession);
            if (id) {
                academicSessionsById.set(String(id), academicSession);
            }
        });
    })().catch(error => {
        academicSessionsLoadPromise = null;
        throw error;
    });

    return academicSessionsLoadPromise;
}

SurfView.AcademicSessions = (() => {

    /** Builds the academic session read-only detail view. */
    function academicSessionDetailsHtml(academicSession) {
        const fields = [
            ['Academic session ID', academicSessionIdValue(academicSession)],
            ['Primary code', plainCodeValue(academicSession.primaryCode)],
            ['Type', academicSession.academicSessionType],
            ['Name', detailTextValue(academicSession.name)],
            ['Start date', academicSession.startDate],
            ['End date', academicSession.endDate]
        ];
        const layout = cloneTemplate('detail-layout-template', null, {navLabel: 'Academic session detail sections'});
        const nav = layout.querySelector('.detail-nav');
        nav.append(
            detailNavSectionElement('Language', languageDetailNavElement()),
            detailNavSectionElement('Academic session', anchorElement('#academic-session-overview', 'Overview'))
        );

        const content = layout.querySelector('.detail-content');
        content.classList.add('academic-session-detail-content');
        const section = detailSectionElement('academic-session-overview', 'Overview');
        const grid = detailGridElement();
        grid.append(...fields.map(([label, value]) => detailFieldElement(label, value)));
        section.append(grid);
        content.append(section);
        return layout;
    }

    /** Opens the shared modal with a new academic session form. */
    function openCreateAcademicSessionModal() {
        if (isReadOnlyMode()) {
            return;
        }
        const academicSession = {
            academicSessionId: generateId(),
            academicSessionType: 'semester',
            primaryCode: {codeType: 'identifier', code: ''},
            name: [{language: 'en-GB', value: ''}],
            startDate: '',
            endDate: ''
        };
        SurfView.State.currentModalKind = 'academicSession';
        SurfView.State.currentModalEntity = academicSession;
        SurfView.State.currentModalOfferings = [];
        SurfView.State.currentModalLanguages = ['en-GB'];
        SurfView.State.currentModalLanguage = 'en-GB';
        resetDraftEntity();
        moduleModal.title.textContent = 'Add academic session';
        setEntityDeletePending(false);
        setEntityModalMode('form');
        modalBody.replaceChildren(academicSessionFormFragment(academicSession, 'create'));
        openManagedModal(moduleModal, {
            initialFocus: () => modalBody.querySelector('input[name="primaryCode"]')
        });
    }

    /** Opens the shared modal with an academic session edit form. */
    function openAcademicSessionEditForm(academicSession) {
        if (isReadOnlyMode()) {
            return;
        }
        resetDraftEntity();
        modalBody.replaceChildren(academicSessionFormFragment(academicSession, 'edit'));
        setEntityModalMode('form');
        modalBody.querySelector('input[name="academicSessionName"]')?.focus();
    }

    /** Builds the shared modal academic session form. */
    function academicSessionFormFragment(academicSession = {}, mode = 'edit') {
        const form = entityEditFormElement('academicSession', mode);
        form.append(...academicSessionFormFields(academicSession, mode), entityEditFormActions(mode === 'edit' ? 'Save' : 'Add'));
        return form;
    }

    function academicSessionFormFields(academicSession = {}) {
        academicSessionDraftLanguageController.initialize(academicSession);
        const sessionId = academicSessionIdValue(academicSession) || generateId();
        const primaryCodeFields = primaryCodeFormFields({
            idLabel: 'Academic session ID',
            idName: 'academicSessionId',
            idValue: sessionId,
            primaryCode: academicSession.primaryCode,
            defaultCodeType: 'identifier'
        });
        return [
            primaryCodeFields[0],
            fieldLabelHtml('Academic session type', 'select', 'academicSessionType', academicSession.academicSessionType || 'semester', {
                required: true,
                options: Constants.enumOptions.academicSessionType
            }, 'full-width'),
            ...primaryCodeFields.slice(1),
            fieldLabelHtml('Start date', 'date', 'startDate', academicSession.startDate || '', {required: true}),
            fieldLabelHtml('End date', 'date', 'endDate', academicSession.endDate || '', {required: true}),
            academicSessionLocalizedPanel(academicSession)
        ];
    }

    function academicSessionLocalizedPanel(academicSession) {
        const section = languageSpecificSectionFragment({
            activeLanguage: SurfView.State.draftEntity.language,
            buttonAttribute: 'data-academic-session-language-code',
            label: 'Content language',
            languages: SurfView.State.draftEntity.languages,
            languageActions: academicSessionLanguageActions,
            headerContent: draftLanguageControls('academic-session', 'newAcademicSessionLanguage'),
            fields: academicSessionLanguageFields(SurfView.State.draftEntity.language)
        });
        section.classList.add('entity-localized-panel');
        section.dataset.academicSessionLocalizedPanel = '';
        academicSessionDraftLanguageController.refresh(section);
        return section;
    }

    function academicSessionLanguageActions(language) {
        return draftLanguageActions('academic-session', language);
    }

    function academicSessionLanguageFields(language) {
        const values = SurfView.State.draftEntity.languageValues[language] || academicSessionEmptyLanguageValues();
        return [
            fieldLabelHtml('Name', 'text', 'academicSessionName', values.name || '', {required: true}, 'full-width')
        ];
    }

    function academicSessionEmptyLanguageValues() {
        return {name: ''};
    }

    const academicSessionDraftLanguageController = createDraftLanguageController({
        prefix: 'academic-session',
        state: () => SurfView.State.draftEntity,
        form: () => document.getElementById('entity-edit-form'),
        fields: () => ['name'],
        value: (academicSession, field, language) => localizedFieldValue(academicSession[field], language),
        formFieldName: () => 'academicSessionName',
        renderPanel: renderAcademicSessionLanguagePanel,
        focusField: 'name'
    });

    function switchDraftAcademicSessionLanguage(language) {
        academicSessionDraftLanguageController.switchLanguage(language);
    }

    function renderAcademicSessionLanguagePanel() {
        const form = document.getElementById('entity-edit-form');
        const panel = form.querySelector('[data-academic-session-localized-panel]');
        const replacement = academicSessionLocalizedPanel(SurfView.State.currentModalEntity || {});
        panel?.replaceWith(replacement);
    }

    /** Submits an academic session create or edit form. */
    async function submitAcademicSessionForm(form) {
        if (isReadOnlyMode()) {
            closeEntityEditForm();
            return;
        }

        const status = document.getElementById('entity-edit-form-status');
        const submitButton = form.querySelector('button[type="submit"]');
        const mode = form.dataset.entityMode;

        await submitEntity({
            status,
            submitButton,
            payload: () => academicSessionPayloadFromForm(form),
            endpoint: payload => mode === 'edit'
                ? `/academic-sessions/${encodeURIComponent(payload.academicSessionId)}`
                : '/academic-sessions',
            method: mode === 'edit' ? 'PUT' : 'POST',
            savingText: mode === 'edit' ? 'Saving...' : 'Adding...',
            successText: mode === 'edit' ? 'Saved' : 'Added',
            preserveArrayFields: ['name'],
            errorMessage: mode === 'edit' ? 'Unable to save academic session' : 'Unable to add academic session',
            onSuccess: ({payload, updatedEntity: updatedAcademicSession}) => {
                const id = academicSessionIdValue(updatedAcademicSession) || payload.academicSessionId;
                applySubmitEntitySuccess({
                    kind: 'academicSession',
                    mode,
                    updatedEntity: updatedAcademicSession,
                    id,
                    matchId: payload.academicSessionId,
                    cacheMap: academicSessionsById,
                    invalidateCache: () => {
                        academicSessionsLoadPromise = null;
                    },
                    title: academicSession => academicSessionLabel(academicSession) || id,
                    detailsHtml: academicSessionDetailsHtml,
                    renderList: SurfView.State.tab === 'academicSessions'
                });
            }
        });
    }

    /** Reads an academic session API payload from a form. */
    function academicSessionPayloadFromForm(form = document.getElementById('entity-edit-form')) {
        academicSessionDraftLanguageController.save(form);
        if (!academicSessionDraftLanguageController.validateRequiredField({targetForm: form})) {
            return null;
        }
        const formData = new FormData(form);
        const value = name => String(formData.get(name) || '').trim();

        return removeEmptyPayloadValues({
            academicSessionId: value('academicSessionId'),
            academicSessionType: value('academicSessionType'),
            primaryCode: primaryCodePayloadFromFormData(formData),
            name: academicSessionDraftLanguageController.payloadFieldValues('name'),
            startDate: value('startDate'),
            endDate: value('endDate')
        });
    }

    /** Returns the display label for an academic session. */
    function academicSessionLabel(academicSession, language = SurfView.State.draftOffering.language || SurfView.State.currentModalLanguage) {
        return localizedNameLabel(academicSession, academicSessionIdValue, language);
    }

    /** Builds academic session select option entries. */
    function academicSessionOptionEntries(selectedAcademicSessionId = '') {
        return entityOptionEntries({
            items: academicSessionsById.values(),
            selectedId: selectedAcademicSessionId,
            emptyLabel: 'None',
            label: academicSessionLabel,
            idValue: academicSessionIdValue
        });
    }

    /** Deletes the academic session currently open in the detail modal. */
    async function deleteCurrentAcademicSession() {
        await deleteCurrentEntity({
            kind: 'academicSession',
            id: academicSessionIdValue(SurfView.State.currentModalEntity),
            endpoint: id => `/academic-sessions/${encodeURIComponent(id)}`,
            confirmMessage: id => `Delete academic session ${id}?`,
            cacheMap: academicSessionsById,
            invalidateCache: () => {
                academicSessionsLoadPromise = null;
            },
            errorMessage: 'Unable to delete academic session'
        });
    }

    /** Registers academic session event handlers. */
    function initEventListeners() {
        delegateClick(modalBody, {
            '[data-add-academic-session-language]': () => academicSessionDraftLanguageController.add(),
            '[data-delete-academic-session-language]': button => academicSessionDraftLanguageController.deleteLanguage(button.dataset.deleteAcademicSessionLanguage),
            '[data-academic-session-language-code]': button => switchDraftAcademicSessionLanguage(button.dataset.academicSessionLanguageCode)
        });
    }

    return {
        academicSessionDetailsHtml,
        academicSessionFormFragment,
        openCreateAcademicSessionModal,
        openAcademicSessionEditForm,
        academicSessionPayloadFromForm,
        submitAcademicSessionForm,
        deleteCurrentAcademicSession,
        academicSessionLabel,
        academicSessionOptionEntries,
        initEventListeners
    };

})();
