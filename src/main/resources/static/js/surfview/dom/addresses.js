function addressLabel(address = {}, index = 0) {
    return address.addressType
        || [address.street, address.streetNumber, address.city].filter(Boolean).join(' ')
        || `Address ${index + 1}`;
}

function addressSummaryLabel(address = {}) {
    return [address.postalCode, address.city, address.countryCode].filter(Boolean).join(', ');
}

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

function addressDetailsFields(address = {}) {
    return [
        ['Type', address.addressType],
        ['Street', [address.street, address.streetNumber].filter(Boolean).join(' ')],
        ['Postal code', address.postalCode],
        ['City', address.city],
        ['Country', address.countryCode],
        ['Additional', textValue(address.additional)]
    ];
}

function addressesHtml(addresses, options = {}) {
    const idPrefix = options.idPrefix || 'course-address';
    const fragment = document.createDocumentFragment();
    addresses.forEach((address, index) => {
        const article = document.createElement('article');
        article.className = 'offering-card';
        article.id = `${idPrefix}-${index + 1}`;
        const details = document.createElement('div');
        details.className = 'detail-grid';
        details.append(...addressDetailsFields(address).map(([label, value]) => detailFieldElement(label, value)));
        article.append(textElement('h3', address.addressType || `Address ${index + 1}`), details);
        fragment.append(article);
    });
    return fragment;
}

function addressFormHtml(address = {}) {
    const fragment = document.createDocumentFragment();
    fragment.append(
        ...addressFieldElements(address),
        languageSpecificSectionFragment({
            activeLanguage: draftOffering.nestedAddressLanguage,
            buttonAttribute: 'data-address-language-code',
            label: 'Address language',
            fields: [
                fieldLabelHtml('Additional information', 'textarea', 'additional', draftOffering.nestedAddressLanguageValues[draftOffering.nestedAddressLanguage]?.additional || '', {}, 'full-width')
            ]
        }),
        nestedFormActionsFragment('Add')
    );
    return fragment;
}

function addressPayloadFromFields(container, baseAddress = {}, options = {}) {
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
    address.geolocation = options.keepEmptyGeolocation ? geolocation : removeEmptyPayloadValues(geolocation);
    return address;
}

function addressItemFromForm(form, additional = []) {
    return removeEmptyPayloadValues({
        ...addressPayloadFromFields(form),
        additional
    });
}
