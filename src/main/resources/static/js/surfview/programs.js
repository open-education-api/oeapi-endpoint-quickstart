window.SurfView = window.SurfView || {};

SurfView.Programs = (() => {
    const UUID_PATTERN = /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i;

    // Return a suitable string for display (don't show UUIDs)
    function visibleLabel(value) {
        const text = typeof value === 'string' ? value.trim() : '';
        return UUID_PATTERN.test(text) ? '' : text;
    }

    function programLabel(program) {
        const name = visibleLabel(textValueLocalizedAttribute(program?.name));
        const code = visibleLabel(plainCodeValue(program?.primaryCode));
        if (name) {
            return code && code !== name ? `${name} (${code})` : name;
        }
        return code || 'Unnamed program';
    }

    function programLabelById(id) {
        return programLabel(programById.get(id));
    }

    function programOptionEntries(excludedProgramIds) {
        const excluded = new Set(excludedProgramIds);
        const items = [...programById.values()]
            .filter(program => !excluded.has(entityId('program', program)));
        return entityOptionEntries({
            items,
            emptyLabel: items.length ? 'Select program' : 'No programs available',
            label: programLabel,
            idValue: program => entityId('program', program)
        });
    }

    return {programLabelById, programOptionEntries};
})();
