/** Returns a compact display label for an address. */
function addressLabel(address = {}, index = 0) {
    return address.addressType
        || [address.street, address.streetNumber, address.city].filter(Boolean).join(' ')
        || `Address ${index + 1}`;
}

/** Returns the summary label for an address draft. */
function addressSummaryLabel(address = {}) {
    return [address.postalCode, address.city, address.countryCode].filter(Boolean).join(', ');
}

/** Builds address form fields. */
function addressFieldElements(address = {}) {
    const geolocation = address.geolocation || {};
    return [
        fieldLabelHtml('Address type', 'select', 'addressType', address.addressType || 'postal', {
            required: true,
            options: Constants.enumOptions.addressType
        }),
        fieldLabelHtml('Street', 'text', 'street', address.street || ''),
        fieldLabelHtml('Street number', 'text', 'streetNumber', address.streetNumber || ''),
        fieldLabelHtml('Postal code', 'text', 'postalCode', address.postalCode || ''),
        fieldLabelHtml('City', 'text', 'city', address.city || ''),
        fieldLabelHtml('Country code', 'text', 'countryCode', address.countryCode || 'NL', {required: true}),
        fieldLabelHtml('Latitude', 'number', 'latitude', geolocation.latitude ?? '', {step: 0.000001}),
        fieldLabelHtml('Longitude', 'number', 'longitude', geolocation.longitude ?? '', {step: 0.000001})
    ];
}

/** Returns detail fields for an address. */
function addressDetailsFields(address = {}) {
    return [
        ['Type', address.addressType],
        ['Street', [address.street, address.streetNumber].filter(Boolean).join(' ')],
        ['Postal code', address.postalCode],
        ['City', address.city],
        ['Country', address.countryCode],
        ['Additional', detailTextValue(address.additional)]
    ];
}

/** Builds read-only address detail sections. */
function addressesHtml(addresses, options = {}) {
    const idPrefix = options.idPrefix || 'course-address';
    const fragment = document.createDocumentFragment();
    addresses.forEach((address, index) => {
        const article = createElement(`article#${idPrefix}-${index + 1}.offering-card`);
        const details = detailGridElement();
        details.append(...addressDetailsFields(address).map(([label, value]) => detailFieldElement(label, value)));
        article.append(textElement('h3', address.addressType || `Address ${index + 1}`), details);
        fragment.append(article);
    });
    return fragment;
}

/** Builds the nested address form. */
function addressFormHtml(address = {}) {
    const fragment = document.createDocumentFragment();
    fragment.append(
        ...addressFieldElements(address),
        languageSpecificSectionFragment({
            activeLanguage: nestedAddressDraft.language,
            buttonAttribute: 'data-address-language-code',
            label: 'Address language',
            languages: Object.keys(nestedAddressDraft.languageValues),
            fields: [
                fieldLabelHtml('Additional information', 'textarea', 'additional', nestedAddressDraft.languageValues[nestedAddressDraft.language]?.additional || '', {}, 'full-width')
            ]
        }),
        nestedFormActionsFragment('Add')
    );
    return fragment;
}

/** Reads an address payload from form fields. */
function addressPayloadFromFields(container, baseAddress = {}) {
    const address = structuredClone(baseAddress || {});
    const value = name => container.querySelector(`[name="${name}"]`)?.value.trim() || '';
    const geolocation = {
        latitude: value('latitude') ? Number(value('latitude')) : null,
        longitude: value('longitude') ? Number(value('longitude')) : null
    };
    address.addressType = value('addressType');
    address.street = value('street');
    address.streetNumber = value('streetNumber');
    address.postalCode = value('postalCode');
    address.city = value('city');
    address.countryCode = value('countryCode');
    address.geolocation = removeEmptyPayloadValues(geolocation);
    return address;
}

/** Builds an address item from a nested form. */
function addressItemFromForm(form, additional = [], baseAddress = {}) {
    return removeEmptyPayloadValues({
        ...addressPayloadFromFields(form, baseAddress),
        additional
    });
}

/** Creates shared UI handlers for editable draft address lists. */
function createDraftAddressListPanel(config) {
    const summarySelector = `#${config.summaryId}`;
    const items = () => config.items() || [];
    const language = () => config.language() || 'en-GB';
    const languages = () => {
        const configuredLanguages = config.languages() || [];
        return configuredLanguages.length ? [...configuredLanguages] : [language()];
    };

    function panel() {
        const section = nestedSummaryPanelFragment(
            config.title || 'Addresses',
            config.buttonText || 'Add address',
            config.nestedType || 'address',
            config.summaryId
        );
        render(section);
        return section;
    }

    function render(scope = document) {
        scope.querySelector(summarySelector)?.replaceChildren(nestedListHtml(
            items(),
            item => addressSummaryLabel(item) || 'Address',
            config.emptyText || 'No addresses',
            {
                editAttribute: config.editAttribute,
                deleteAttribute: config.deleteAttribute,
                itemName: 'address'
            }
        ));
    }

    function target() {
        return {
            items: items(),
            languages: languages(),
            language: language(),
            subtitle: config.subtitle(),
            onChange: render
        };
    }

    function deleteItem(indexValue) {
        const index = Number(indexValue);
        if (!Number.isInteger(index) || index < 0 || index >= items().length) {
            return;
        }

        items().splice(index, 1);
        render();
    }

    return {panel, render, target, deleteItem};
}

// Nested address editor: edits a list of addresses in the shared nested modal.
// It works against a target so any form can plug in its own address list:
// {items, languages, language, subtitle, onChange}.
let nestedAddressTarget = null;

const nestedAddressDraft = {language: 'en-GB', languageValues: {}};

const nestedAddressDraftLanguageController = createDraftLanguageController({
    prefix: 'address',
    state: () => nestedAddressDraft,
    form: () => nestedOfferingForm,
    fields: () => ['additional'],
    languages: () => nestedAddressTarget.languages,
    value: (address, field, language) => textValueForLanguage(address[field], language),
    getLanguages: draft => Object.keys(draft.languageValues),
    setLanguages: () => {},
    currentLanguage: () => nestedAddressTarget.language,
    canSave: form => form.dataset.nestedType === 'address'
});

/** Opens the shared nested address modal. */
function openNestedAddressModal(target, options = {}) {
    nestedAddressTarget = target;
    openNestedModal({
        type: 'address',
        itemName: 'address',
        items: target.items,
        index: options.index,
        initialize: address => nestedAddressDraftLanguageController.initialize(address),
        formHtml: addressFormHtml,
        subtitle: target.subtitle
    });
}

function submitNestedAddressForm() {
    nestedAddressDraftLanguageController.save();
    storeNestedItem(nestedAddressTarget.items, original =>
        addressItemFromForm(nestedOfferingForm, nestedAddressLanguageValues('additional'), original));
    nestedAddressTarget.onChange();
}

function nestedAddressLanguageValues(field) {
    return Object.entries(nestedAddressDraft.languageValues)
        .map(([language, values]) => ({
            language,
            value: String(values?.[field] || '').trim()
        }))
        .filter(item => item.value);
}

function resetNestedAddressEditor() {
    nestedAddressTarget = null;
    nestedAddressDraft.language = 'en-GB';
    nestedAddressDraft.languageValues = {};
}

registerNestedModalType('address', {
    submit: submitNestedAddressForm,
    reset: resetNestedAddressEditor,
    clickHandlers: {
        '[data-address-language-code]': button => nestedAddressDraftLanguageController.switchLanguage(button.dataset.addressLanguageCode)
    }
});
