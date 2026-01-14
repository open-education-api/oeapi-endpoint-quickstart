/*****************************************
 
 Code for Forms
 
 ******************************************/



const courseNames = {};
const defaultLanguage = "en-GB";


/* Rich Text management (CKEditor) **********************/

var editors = {};
var editorsData = {};
var currentLang = {}; // Current language per field



/* For rich text values */
function initMultilingualEditors() {

    // Fill language selectors
    fillItemSelectorsByClassName("lang-select", languages);

    // Automatically initialize all CKEditor textareas that have a corresponding lang-select	
    document.querySelectorAll('textarea[id^="editor-"]').forEach(function (el) {

        var fieldId = el.id.replace('editor-', ''); // e.g., "physicalDescription"
        editorsData[fieldId] = {};
        currentLang[fieldId] = 'en-GB'; // default language

        // Initialize CKEditor
        ClassicEditor.create(el, {// Config - Disable font change
            removePlugins: ['FontFamily', 'FontSize', 'FontColor', 'FontBackgroundColor'],
            htmlSupport: {
                disallow: [
                    {name: /.*/, attributes: ['style'], classes: true}
                ]
            },
            fontFamily: {
                options: ['default'],
                supportAllValues: false
            },
            fontSize: {
                options: ['default'],
                supportAllValues: false
            }
        })
                .then(function (editor) {

                    // Save instance for possible future use
                    editorsData[fieldId]._editorInstance = editor;

                    // Automatically save current language text whenever it changes
                    editor.model.document.on('change:data', () => {
                        const currentLanguage = currentLang[fieldId];
                        // If empty is ignored
                        if (editor.getData().trim()) {
                            editorsData[fieldId][currentLanguage] = editor.getData().trim();
                        } else {
                            delete editorsData[fieldId][currentLanguage];
                        }
                        console.log("Text changed: ", fieldId, currentLanguage, editor.getData().trim());

                        updateSavedEditorLangs(fieldId);

                    });

                    // Get corresponding language select
                    var langSelect = document.querySelector('select.lang-select[data-field="' + fieldId + '"]');
                    if (!langSelect)
                        return;

                    langSelect.addEventListener('change', function (event) {
                        const newLang = event.target.value;
                        const oldLang = currentLang[fieldId];
                        const content = editor.getData().trim();

                        // Save current text under the old language key
                        if (content) {
                            editorsData[fieldId][oldLang] = content;
                        } else {
                            delete editorsData[fieldId][oldLang];
                        }

                        // Switch the tracking variable BEFORE setting data
                        currentLang[fieldId] = newLang;

                        // Load text for the new language (if any)
                        const newContent = editorsData[fieldId][newLang] || '';
                        editor.setData(newContent);

                        console.log("CKeditor for " + fieldId + " Switched from " + oldLang + " to " + newLang);
                        updateSavedEditorLangs(fieldId);
                    });

                    console.log("CKEditor initialized for: " + fieldId);

                })
                .catch(function (err) {
                    console.error('CKEditor init failed for ' + fieldId + ':', err);
                });
    });

}

function updateSavedEditorLangs(fieldId, labelEl) {
    if (!labelEl) {
        labelEl = document.getElementById("savedLangs-" + fieldId);
    }
    if (!labelEl) {
        console.warn("Could not find label for field: " + fieldId);
        return;
    }

    let langs = Object.keys(editorsData[fieldId] || {}).filter(function (key) {
        // Keep only valid language codes (like en_GB, es_ES, fr_FR)
        // and remove any internal keys (like _editorInstance)
        return /^[a-z]{2}-[A-Z]{2}$/.test(key);
    });

    // 🧹 Clean editorsData[fieldId] by removing invalid keys permanently
    Object.keys(editorsData[fieldId] || {}).forEach(function (key) {
        if (!/^[a-z]{2}-[A-Z]{2}$/.test(key)) {
            delete editorsData[fieldId][key];
        }
    });


    langs = Object.keys(editorsData[fieldId] || {});
    labelEl.innerHTML =
            langs.length === 0
            ? "No translations yet."
            : "<b>Languages with content:</b> " + langs.join(", ");
}

// Gather all editor contents on form submit
function collectAllEditorData() {
    Object.keys(editorInstances).forEach(fieldId => {
        const editor = editorInstances[fieldId];
        const lang = currentLang[fieldId];
        editorsData[fieldId][lang] = editor.getData();
    });
    console.log("Collected multilingual editor data:", editorsData);
    return editorsData;
}


/**
 * Save the currently visible content for all CKEditor fields before submit.
 * It ensures the latest edited content is stored in editorsData[fieldId][lang].
 */

function saveAllEditorsData() {

    for (var fieldId in editorsData) {

        console.log("saveAllEditorsData: Checking " + fieldId);

        if (editorsData.hasOwnProperty(fieldId)) {
            var editorInstance = editorsData[fieldId]._editorInstance;
            if (!editorInstance)
                continue; // skip if no CKEditor instance

            // Get actual visible language from select 
            var langSelect = document.querySelector('select.lang-select[data-field="' + fieldId + '"]');
            var lang = langSelect ? langSelect.value : (currentLang[fieldId] || defaultLanguage);

            // Get editor data
            var content = "";
            try {
                content = editorInstance.getData().trim();
            } catch (e) {
                console.warn("Could not read data for editor:", fieldId, e);
            }

            // Save or delete depending on content
            if (content) {
                editorsData[fieldId][lang] = content;
            } else {
                delete editorsData[fieldId][lang];
            }
        }
    }
}

/**
 * Get multilingual JSON for a single CKEditor field.
 */

function getMultilingualEditorJSON(fieldId) {
    var result = [];
    if (!editorsData[fieldId])
        return result;

    for (var lang in editorsData[fieldId]) {
        if (
                editorsData[fieldId].hasOwnProperty(lang) &&
                lang !== "_editorInstance"
                ) {
            var value = editorsData[fieldId][lang];
            if (typeof value === "string" && value.trim() !== "") {
                result.push({
                    language: lang,
                    value: value.trim(),
                });
            }
        }
    }

    return result;
}

/* Cleanup previous values on new or before modifing */

function resetEditors() {
    Object.keys(editorsData).forEach(fieldId => {
        const editor = editorsData[fieldId]._editorInstance;
        if (editor) {
            editor.setData("");
        }
    });

    editorsData = {};
    currentLang = {};
}

/* When editing */

function setMultilingualEditorContent(fieldId, values) {

    if (!values || !Array.isArray(values))
        return;
    if (!editorsData[fieldId])
        return;

    // Clear existing content (important when switching between courses)
    Object.keys(editorsData[fieldId]).forEach(k => {
        if (k !== "_editorInstance")
            delete editorsData[fieldId][k];
    });

    // Store all languages
    values.forEach(v => {
        if (v.language && v.value) {
            editorsData[fieldId][v.language] = v.value;
        }
    });

    // Pick which language to show
    const preferredLang =
            values.find(v => v.language === defaultLanguage)?.language ||
            values[0]?.language;

    if (!preferredLang)
        return;

    currentLang[fieldId] = preferredLang;

    // Update language selector
    const langSelect = document.querySelector(
            `select.lang-select[data-field="${fieldId}"]`
            );
    if (langSelect) {
        langSelect.value = preferredLang;
    }

    // Update editor UI
    const editor = editorsData[fieldId]._editorInstance;
    if (editor) {
        editor.setData(editorsData[fieldId][preferredLang] || "");
    }

    updateSavedEditorLangs(fieldId);
}


/* For simple text fields */

// Global storage for multilingual plain-text fields
var textFieldData = {};
var currentTextLang = {};

// Initialize multilingual text inputs (not rich text)
function initMultilingualTextFields() {
    var selects = document.querySelectorAll(".lang-select-text");
    selects.forEach(function (select) {
        var fieldId = select.dataset.field;
        var input = document.getElementById(fieldId);
        var savedLabel = document.getElementById("savedLangs-" + fieldId);

        if (!input) {
            console.warn("Missing text input for field! : ", fieldId);
            return;
        }

        textFieldData[fieldId] = textFieldData[fieldId] || {};
        currentTextLang[fieldId] = select.value || "en_GB";

        // When changing language
        select.addEventListener("change", function () {
            var oldLang = currentTextLang[fieldId];
            var newLang = select.value;
            var content = input.value.trim();

            // Save or remove old value
            if (content) {
                textFieldData[fieldId][oldLang] = content;
            } else {
                delete textFieldData[fieldId][oldLang];
            }

            // Load new language text
            input.value = textFieldData[fieldId][newLang] || "";
            currentTextLang[fieldId] = newLang;

            updateSavedTextLangs(fieldId, savedLabel);
        });

        // Auto-save as user types
        input.addEventListener("input", function () {
            var lang = currentTextLang[fieldId];
            var content = input.value.trim();

            if (content) {
                textFieldData[fieldId][lang] = content;
            } else {
                delete textFieldData[fieldId][lang];
            }

            updateSavedTextLangs(fieldId, savedLabel);
        });
    });
}

// Display which languages have content
function updateSavedTextLangs(fieldId, labelEl) {

    if (!labelEl)
        return;
    var langs = Object.keys(textFieldData[fieldId] || {}).filter(function (k) {
        return /^[a-z]{2}-[A-Z]{2}$/.test(k);
    });

    labelEl.innerHTML =
            langs.length === 0
            ? "No translations yet."
            : "<b>Languages with content:</b> " + langs.join(", ");
}


// To extract JSON of all text fields
function getMultilingualTextJSON() {
    return JSON.parse(JSON.stringify(textFieldData));
}

// helper to get JSON for a single text field (array of {language,value})
function getMultilingualTextJSONFor(fieldId) {
    var out = [];
    var map = textFieldData[fieldId] || {};
    for (var lang in map) {
        if (!map.hasOwnProperty(lang))
            continue;
        var v = map[lang];
        if (typeof v === "string" && v.trim() !== "") {
            out.push({language: lang, value: v.trim()});
        }
    }
    return out;
}

/* Cleanup previous values on new or before modifing */

function resetMultilingualTextFields() {
    Object.keys(textFieldData).forEach(fieldId => {
        const input = document.getElementById(fieldId);
        const select = document.querySelector(
                '.lang-select-text[data-field="' + fieldId + '"]'
                );
        const label = document.getElementById("savedLangs-" + fieldId);

        // Clear visible input
        if (input) {
            input.value = "";
        }

        // Reset language selector to default
        if (select) {
            select.value = defaultLanguage;
        }

        // Clear saved translations
        textFieldData[fieldId] = {};
        currentTextLang[fieldId] = defaultLanguage;

        // Update UI label
        if (label) {
            label.innerHTML = "No translations yet.";
        }
    });

    // Safety reset
    textFieldData = {};
    currentTextLang = {};
}

// For editing...

function setMultilingualTextField(fieldId, values) {
    if (!Array.isArray(values))
        return;

    const input = document.getElementById(fieldId);
    const select = document.querySelector(
            '.lang-select-text[data-field="' + fieldId + '"]'
            );
    const label = document.getElementById("savedLangs-" + fieldId);

    if (!input || !select)
        return;

    textFieldData[fieldId] = textFieldData[fieldId] || {};

    // Load all translations
    values.forEach(v => {
        if (v.language && v.value) {
            textFieldData[fieldId][v.language] = v.value;
        }
    });

    // Ensure current language is valid
    const langs = Object.keys(textFieldData[fieldId]);
    const activeLang = langs.includes(select.value)
            ? select.value
            : langs[0];

    select.value = activeLang;
    currentTextLang[fieldId] = activeLang;
    input.value = textFieldData[fieldId][activeLang] || "";

    updateSavedTextLangs(fieldId, label);
}


function resetAllMultilingualFields() {
    resetEditors();                // CKEditor fields
    resetMultilingualTextFields(); // Text inputs
}


/* Load Selectors ****************/

function fillItemSelectorsByClassName(className, itemList) {

    // Find all select elements with class "className"
    document.querySelectorAll("select." + className).forEach(select => {
        // Clear any existing options
        select.innerHTML = "";

        // Populate with languages
        itemList.forEach((item, index) => {
            const option = document.createElement("option");
            option.value = item.code;
            option.textContent = item.name;
            if (index === 0)
                option.selected = true;
            select.appendChild(option);
        });
    });
}


function fillSelectOptions(selectId, values) {
    const $select = $('#' + selectId);
    $select.empty();
    for (let i = 0; i < values.length; i++) {
        $select.append($('<option>', {
            value: values[i],
            text: values[i]
        }));
    }
}


function fillLanguageISO639Selectors() {

    // Find all select elements with class "lang-iso639-select"
    document.querySelectorAll("select.lang-iso639-select").forEach(select => {
        // Clear any existing options
        select.innerHTML = "";

        // Populate with languages
        languagesISO639.forEach((lang, index) => {
            const option = document.createElement("option");
            option.value = lang.iso639_2;
            option.textContent = lang.name;
            if (index === 0)
                option.selected = true; // Default to English
            select.appendChild(option);
        });
    });

}

// Transform OEAPI items into an async array of `{label, value,
// selected}`.
async function asyncItemsToOptions(asyncItems, idAttribute, defaultValue) {
    return (await asyncItems).map(item => ({
            label: extractName(item),
            value: item[idAttribute],
            selected: item[idAttribute] == defaultValue
        }));
}

// Populate <select> elements from `selector` with options.  The
// options are an async array of `{label, value, selected}`.
async function populateSelect(selector, asyncOptions) {
    try {
        const options = await asyncOptions;
        document.querySelectorAll(selector).forEach(selectEl => {
            selectEl.innerHTML = '';
            options.forEach(({label, value, selected}) => {
                const optionEl = document.createElement('option');
                optionEl.label = label;
                optionEl.value = value;
                optionEl.selected = selected;
                selectEl.appendChild(optionEl);
            })
        });
    } catch (err) {
        console.error('Failed to fetch options', err);

        document.querySelectorAll(selector).forEach(selectEl => {
            selectEl.innerHTML = '';
            const optionEl = document.createElement('option');
            optionEl.label = 'ERROR: failed to fetch options';
            selectEl.appendChild(optionEl);
        })
    }
}

// Populate <select> elements from `selector` with OEAPI organizations.
async function populateSelectOrganization(selector) {
    populateSelect(
            selector,
            asyncItemsToOptions(
                    sortAsyncItemsByName(fetchItems('organizations')),
                    'organizationId',
                    ooapiDefaultOrganizationId
                    )
            );
}

async function populateSelectFieldsOfStudy(selector) {
    const asyncFieldsOfStudyOptions = async (asyncFields) => (
                (await asyncFields).map(x => ({label: x.txtEn, value: x.fieldsOfStudyId}))
                );

    populateSelect(
            selector,
            asyncFieldsOfStudyOptions(fetchItems('fieldsofstudy?level=1'))
            );
}

/* JSON auxiliary functions */

function safeToString(value) {
    if (value === null || value === undefined)
        return String(value);

    try {
        // Try JSON.stringify first for readability
        return JSON.stringify(value, null, 2);
    } catch (e) {
        // Fallback: force to string
        try {
            return String(value);
        } catch (e2) {
            return "[Unprintable Value]";
        }
    }
}


// Remove empty or null properties recursively
function cleanNullsOrEmpties(obj) {
    if (Array.isArray(obj)) {
        obj = obj.map(cleanNullsOrEmpties).filter(Boolean);
        return obj.length ? obj : null;
    } else if (typeof obj === "object" && obj !== null) {
        var newObj = {};
        Object.keys(obj).forEach(function (key) {
            var val = cleanNullsOrEmpties(obj[key]);
            if (val !== null && val !== "" && !(typeof val === "object" && Object.keys(val).length === 0)) {
                newObj[key] = val;
            }
        });
        return Object.keys(newObj).length ? newObj : null;
    }
    return obj;
}


function buildAddressJSON() {
    function getValue(id) {
        var el = document.getElementById(id);
        return el ? el.value.trim() : "";
    }

    function getFloat(id) {
        var el = document.getElementById(id);
        var val = el && el.value ? parseFloat(el.value) : null;
        return isNaN(val) ? null : val;
    }

    var address = {
        addressType: "postal",
        street: getValue("street"),
        streetNumber: getValue("streetNumber"),
        postalCode: getValue("postalCode"),
        city: getValue("city"),
        countryCode: getValue("countryCode"),
        additional: [],
        geolocation: {
            latitude: getFloat("latitude"),
            longitude: getFloat("longitude")
        }
    };

    // Handle multilingual "additional" info
    if (typeof window.addressAdditionalTexts === "object" && Object.keys(window.addressAdditionalTexts).length > 0) {
        address.additional = Object.keys(window.addressAdditionalTexts)
                .filter(function (lang) {
                    return window.addressAdditionalTexts[lang].trim() !== "";
                })
                .map(function (lang) {
                    return {language: lang, value: window.addressAdditionalTexts[lang].trim()};
                });
    } else {
        // fallback to single text field
        var addEl = document.getElementById("addressAdditional");
        if (addEl && addEl.value.trim()) {
            address.additional = [{language: defaultLanguage, value: addEl.value.trim()}];
        }
    }

    // Remove empty values
    cleaned = cleanNullsOrEmpties(address);

    // Return "" if all fields are empty
    return cleaned && Object.keys(cleaned).length > 0 ? cleaned : "";
}

let editorInstance = null;
let endpointURL = ooapiDefaultEndpointURL; // from init.js
let language = ooapiDefaultCountry; // from init.js

/* Location of data for preload selectors */

const preLoadItems = [{
        name: "modeOfDeliveryType",
        elementId: "modeOfDelivery",
        url: endpointURL + "/enumerator?enum=modeOfDeliveryType"
    },
    {
        name: "levelType",
        elementId: "level",
        url: endpointURL + "/enumerator?enum=levelType"
    },
    {
        name: "studyLoadType",
        elementId: "studyLoadType",
        url: endpointURL + "/enumerator?enum=studyLoadType"
    }
];


document.querySelectorAll(".general-tabs .tab-btn").forEach(button => {
    button.addEventListener("click", () => {
        const target = button.dataset.tab;
        document.querySelectorAll(".general-tabs .tab-btn").forEach(btn => btn.classList.remove("active"));
        document.querySelectorAll(".tab-content").forEach(tab => tab.classList.remove("active"));
        button.classList.add("active");
        document.getElementById(`tab-${target}`).classList.add("active");
    });
});


// Language tabs


$(document).ready(function () {

    // Load enums and fill selects
    $.each(preLoadItems, function (index, preLoadItems) {
        $.ajax({
            url: preLoadItems.url,
            method: 'GET'
        })
                .done(function (data) {
                    fillSelectOptions(preLoadItems.elementId, data);
                })
                .fail(function () {
                    console.error("Failed to load enum:", preLoadItems.name);
                });
    });
});

async function loadOfferingsForCourse(courseId) {
    const res = await fetch(`${ooapiDefaultEndpointURL}/courses/${courseId}/offerings`);
    if (!res.ok) {
        console.warn("No offerings found for course", courseId);
        return;
    }

    const page = await res.json();
    offeringsById = {};

    page.items.forEach(o => {
        offeringsById[o.offeringId] = o;
    });

    populateOfferingSelector(page.items);
}



function loadOfferingIntoForm(offeringId) {
    const offering = offeringsById[offeringId];
    if (!offering)
        return;

    currentOfferingId = offeringId;

    // Reset editors first
    resetEditors();

    // ---- Dates ----
    document.getElementById("startDate").value = offering.startDate || "";
    document.getElementById("endDate").value = offering.endDate || "";

    document.getElementById("startEnrollDate").value = offering.enrollStartDate || "";
    document.getElementById("endEnrollDate").value = offering.enrollEndDate || "";

    // ---- Numbers ----
    document.getElementById("minNumberStudents").value = offering.minNumberStudents ?? "";
    document.getElementById("maxNumberStudents").value = offering.maxNumberStudents ?? "";

    // ---- Offering multilingual fields ----
    setMultilingualEditorContent("offeringDescription", offering.description);

    // ---- Address ----
    if (offering.addresses?.length) {
        const addr = offering.addresses[0];
        document.getElementById("street").value = addr.street || "";
        document.getElementById("streetNumber").value = addr.streetNumber || "";
        document.getElementById("postalCode").value = addr.postalCode || "";
        document.getElementById("city").value = addr.city || "";
        document.getElementById("countryCode").value = addr.countryCode || "";
        document.getElementById("latitude").value = addr.geolocation?.latitude || "";
        document.getElementById("longitude").value = addr.geolocation?.longitude || "";

        setMultilingualEditorContent("addressAdditional", addr.additional);
    }

    console.log("Loaded offering", offeringId);
}


/* Submit Form */

$('#catalogForm').on('submit', async function (e) {

    event.preventDefault();

    /* Ensure save values of CKEditor fields */
    console.log("CKeditorsData right now:", editorsData);
    saveAllEditorsData();

    /* Check requiered fields are ok */
    const tabs = document.querySelectorAll('.tab-content');
    let allValid = true;

    for (const tab of tabs) {

        const inputs = tab.querySelectorAll('input, textarea, select');
        for (const input of inputs) {
            if (!input.checkValidity()) {

                // Activate the tab that contains the invalid input
                if (!tab.classList.contains('active')) {
                    // Remove active from current tab and button
                    document.querySelector('.tab-content.active')?.classList.remove('active');
                    document.querySelector('.tab-btn.active')?.classList.remove('active');

                    // Activate this tab
                    tab.classList.add('active');

                    // Activate matching tab button
                    const tabId = tab.id.replace('tab-', ''); // e.g. "basic-info"
                    document.querySelector(`.tab-btn[data-tab="${tabId}"]`)?.classList.add('active');
                }

                // Show browser's native validation message
                input.reportValidity();

                // Scroll to the invalid input for visibility
                input.scrollIntoView({behavior: 'smooth', block: 'center'});

                allValid = false;
                return; // Stop further validation
            }
        }
    }

    /* Gather all info from CKEditors */

    for (const fieldId in editorsData) {
        if (!editorsData.hasOwnProperty(fieldId))
            continue;

        const editorInstance = editorsData[fieldId]._editorInstance;
        if (editorInstance) {
            editorInstance.updateSourceElement(); // Push visual text into <textarea>
        }
    }

    // Can really do an update (am i logged or no JWT?)
    let updatesArePermited = await canDoUpdates();

    if (updatesArePermited) {
        console.log("On submit: User is validated to perform updates...");
        if (validateDates()) {
            console.log("On submit: Required dates are valid...");

            /* Manage coordinators... */

            const coordinatorsData = await Promise.all(coordinators.map(async coordinator => {
                return {
                    givenName: coordinator.givenName,
                    surname: coordinator.surname,
                    mail: coordinator.mail,
                    displayName: coordinator.givenName + " " + coordinator.surname,
                    activeEnrollment: false,
                    affiliations: ["employee"],
                    primaryCode: {
                        code: coordinator.mail,
                        codeType: "identifier"
                    }
                };
            }));

            console.log("On submit: Coordinator list processed: ", coordinatorsData);
            console.log("On submit: num of coordinators:", coordinatorsData.length);

            let coordinatorsId = [];
            if (coordinatorsData.length > 0) {

                console.log("On submit: Processing coordinators (obtaining ids) ...");
                // 2. Process each coordinator to get their personId
                let personRes1, personRes2, personJson1, personJson2, queryUrl

                coordinatorsId = await Promise.all(coordinatorsData.map(async c => {
                    console.log("mail: " + c.mail);
                    // First: check if person already exists
                    queryUrl = `${endpointURL}/persons?primaryCode=${encodeURIComponent(c.mail)}`; // Use query param
                    personRes1 = await fetch(queryUrl, {
                        method: "GET",
                        headers: {
                            "Content-Type": "application/json"
                        }
                    });
                    if (personRes1.ok) {
                        // Doesn't exist: create
                        personJson1 = await personRes1.json();
                        if (personJson1.items.length == 0) {
                            console.log("On submit: New coordinator: " + c.mail);
                            personRes2 = await fetch(`${endpointURL}/persons`, {
                                method: "POST",
                                headers: {
                                    "Content-Type": "application/json",
                                    "Authorization": "Bearer " + localStorage.getItem('jwt')
                                },
                                body: JSON.stringify(c)
                            });
                            personJson2 = await personRes2.json();
                            return personJson2.personId + "";
                        } else {
                            // Exists: use existing
                            console.log("On submit: Coordinator already in OOAPI: " + c.mail);
                            return personJson1.items[0].personId + "";
                        }
                    }
                }));
            }


            // 3. coordinatorsId now contains all personIds
            console.log("On submit: Final list of coordinators ids:", coordinatorsId);
            console.log("On submit: Posting course and Offering... ", coordinatorsId);


            let postCourseResponse;

            try {
                postCourseResponse = await postCourse(coordinatorsId);
                console.log("Posting course response: ", postCourseResponse);

            } catch (err) {
                // Stop the function immediately
                console.error("Posting course exiting due to errors: ", postCourseResponse);
                showAlert("error", "Error", "Submission of Course failed!");
                return;
            }


            /* Post offerings */

            /* postCourseResponse.courseId is the ID of parent course for offering */


            let sendMode = isEditMode ? "Updated!" : "Created!";

            switch (formCourseType) {

                case "stdCourse":
                    let postCourseOfferResponse;
                    try {
                        postCourseOfferResponse = await postOffering(postCourseResponse.courseId);
                        console.log("On submit (stdCourse): postOffering response: ", postCourseOfferResponse);
                        if (postCourseOfferResponse !== null)
                        {
                            showAlert("success", "Course sent!", "Course " + sendMode);
                        }

                    } catch (err) {
                        // Stop the function immediately
                        console.log("postOffering course exiting due to errors: ", err);

                        // if course is new, it is not completed if it has no offering, then delete it
                        if (!isEditMode)
                        {
                            console.error("postOffering deleting uncomplete course: ", postCourseResponse.courseId);
                            deleteCourse(postCourseResponse.courseId, false);
                        }
                        showAlert("error", "Error", "Submission of Course failed, problem submitting offering!");
                        return;
                    }

                    break;

                case "BIPCourse":

                    // Physical Component
                    let postPhysicalComponentCourseOfferResponse;
                    try {
                        postPhysicalComponentCourseOfferResponse = await postPhysicalComponentOffering(postCourseResponse.courseId);
                        console.log("On submit (Physical Component): postOffering response: ", postPhysicalComponentCourseOfferResponse);
                        if (postPhysicalComponentCourseOfferResponse !== null)
                        {
                            console.log("On submit (Physical Component): Success !");
                        }
                    } catch (err) {
                        // Stop the function immediately
                        console.error("On submit (Physical Component): postOffering: ", err);
                        // if course is new, it is not completed if it has no offering, then delete it
                        if (!isEditMode)
                        {
                            console.log("On submit (Physical Component) deleting uncomplete course: ", postCourseResponse.courseId);
                            deleteCourse(postCourseResponse.courseId, false);
                        }
                        showAlert("error", "Error", "Submission of Course failed, problem submitting offering (Physical Component) ");
                        return;
                    }


                    // Virtual Component
                    let postVirtualComponentCourseOfferResponse;
                    try {
                        postVirtualComponentCourseOfferResponse = await postVirtualComponentOffering(postCourseResponse.courseId);
                        console.log("On submit (Virtual Component): postOffering response: ", postVirtualComponentCourseOfferResponse);
                        if (postVirtualComponentCourseOfferResponse !== null)
                        {
                            console.log("On submit (Virtual Component): Success !");
                            showAlert("success", "BIP Course sent!", "Course " + sendMode);
                        }
                    } catch (err) {
                        // Stop the function immediately
                        console.log("On submit (Virtual Component): postOffering: ", err);
                        // if course is new, it is not completed if it has no offering, then delete it
                        if (!isEditMode)
                        {
                            console.log("On submit (Virtual Component) deleting uncomplete course: ", postCourseResponse.courseId);
                            deleteCourse(postCourseResponse.courseId, false);
                        }
                        showAlert("error", "Error", "Submission of Course failed, problem submitting offering (Virtual Component) ");
                        return;
                    }

                    break;

            } // end switch

        } // end if-else validate dates

    } else // No valid session and JWT activated
    {
        showAlert("error", "Error", "You cannot update data in your OEAPI Endpoint. Maybe you are not logged or your session has expired");
    }
});


async function postCourse(listCoordinators) {

    const studyLoad = $('#studyLoad').val() ? {
        studyLoadUnit: $('#studyLoadType').val(),
        value: $('#studyLoad').val()
    } : null;


    // Build the courseData JSON
    var courseData = {
        name: getMultilingualTextJSONFor("courseName"),
        description: getMultilingualEditorJSON("description"),
        studyLoad: studyLoad,
        fieldsOfStudy: $('#fieldsOfStudy').val(),
        modeOfDelivery: $('#modeOfDelivery').val(),
        learningOutcomes: [getMultilingualEditorJSON("learningOutcomes")],
        admissionRequirements: getMultilingualEditorJSON("admissionRequirements"),
        assessment: getMultilingualEditorJSON("assessment"),
        enrollment: getMultilingualEditorJSON("enrollment"),
        abbreviation: $('#abbreviation').val(),
        link: $('#link_to_more_info').val(),
        teachingLanguage: $('#teachingLanguage').val(),
        organization: ooapiDefaultOrganizationId,
        level: $('#level').val(),
        coordinators: listCoordinators,
        validFrom: $('#validStartDate').val(),
        validTo: $('#validEndDate').val(),
        primaryCode: {
            codeType: "identifier",
            code: $('#code').val()
        }
    };

    // Remove empty values

    console.log("postCourse: Json previous cleaning of empty values.. " + safeToString(courseData));

    if (isEditMode)
    {
        courseData.courseId = courseIdEd;
    } // Ensure no new Id 

    courseData = cleanNullsOrEmpties(courseData);

    console.log("postCourse: Ready to post course data to " + endpointURL + "/courses with " + safeToString(courseData), courseData);

    var univ = ooapiDefaultShortUnivName; // from init.js

    try {

        let whichEndpoint = isEditMode
                ? ooapiDefaultEndpointURL + "/courses/" + courseIdEd
                : ooapiDefaultEndpointURL + "/courses";

        let methodToFollow = isEditMode ? "PUT" : "POST";

        console.log("postCourse: isEditMode? : " , isEditMode);
        console.log("postCourse: methodToFollow? : " , methodToFollow);
        console.log("postCourse: whichEndpoint? : " , whichEndpoint);

        const response = await fetch(whichEndpoint, {
            method: methodToFollow,
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + localStorage.getItem("jwt")
            },
            body: JSON.stringify(courseData)
        });

        // Read the response body as text
        const text = await response.text();

        let parsedResponse = manageResponse(response, text, "postCourse");
        console.log("parsedResponse: ", parsedResponse);

        return parsedResponse;

    } catch (err)
    {
        console.error("postCourse: Network or unexpected error:", err);
        showAlert("error", "Error", "Network or unexpected error:\n" + err.message);
        return null;
    }
}

async function postOffering(courseId) {

    const minNumberStudents = $('#minNumberStudents').val() ? $('#minNumberStudents').val() : null;
    const maxNumberStudents = $('#maxNumberStudents').val() ? $('#maxNumberStudents').val() : null;

    const courseAddress = buildAddressJSON();

    var costJson = null;
    var costInForm = $('#amount').val() ? $('#amount').val() : null;

    if (costInForm)
    {
        costJson = {
            amount: $('#amount').val(),
            currency: "EUR",
            costType: "total costs"
        };
    }


    OfferingData = {
        primaryCode: {
            code: $('#code').val() + " (Offering)",
            codeType: "identifier"
        },
        abbreviation: $('#abbreviation').val() + "(Offer)",
        teachingLanguage: $('#teachingLanguage').val(),
        name: getMultilingualTextJSONFor("courseName"),
        description: getMultilingualEditorJSON("offeringDescription"),
        minNumberStudents: minNumberStudents,
        maxNumberStudents: maxNumberStudents,
        resultExpected: false,
        priceInformation: [costJson],
        link: $('#link_to_enroll').val(),
        offeringType: "course",
        startDate: $('#startDate').val(),
        endDate: $('#endDate').val(),
        enrollStartDate: $('#startEnrollDate').val(),
        enrollEndDate: $('#endEnrollDate').val(),
        modeOfDelivery: $('#modeOfDelivery').val(),
        addresses: [courseAddress],
        course: courseId
    }

    // Remove empty values
    OfferingData = cleanNullsOrEmpties(OfferingData);

    if (isEditMode)
    {
        OfferingData.offeringId = offeringIdEd;
    } // Ensure no new Id 

    console.log("Ready to post offering data: ", OfferingData);

    let whichEndpoint = isEditMode
            ? ooapiDefaultEndpointURL + "/offerings/" + offeringIdEd
            : ooapiDefaultEndpointURL + "/offerings";

    let methodToFollow = isEditMode ? "PUT" : "POST";

    console.log("postOffering: isEditMode? : " , isEditMode);
    console.log("postOffering: methodToFollow? : " , methodToFollow);
    console.log("postOffering: whichEndpoint? : " , whichEndpoint);

    try {
        response = await fetch(whichEndpoint, {
            method: methodToFollow,
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + localStorage.getItem("jwt")
            },
            body: JSON.stringify(OfferingData)
        });

        // Read the response body as text
        const text = await response.text();

        let parsedResponse = manageResponse(response, text, "postOfferingData");

        console.log("parsedResponse: ", parsedResponse);

        return parsedResponse;

    } catch (err)
    {
        console.error("Network or unexpected error:", err);
        showAlert("error", "Error", "Network or unexpected error:\n" + err.message);
        return null;
    }

}


async function postPhysicalComponentOffering(courseId) {

    const startDate = $('#physicalStartDate').val() ? $('#physicalStartDate').val() : null;
    const endDate = $('#physicalEndDate').val() ? $('#physicalEndDate').val() : null;

    var costJson = null;
    var costInForm = $('#amount').val() ? $('#amount').val() : null;

    if (costInForm)
    {
        costJson = {
            amount: $('#amount').val(),
            currency: "EUR",
            costType: "total costs"
        };
    }


    const courseAddress = buildAddressJSON();

    PhysicalComponentOfferingData = {
        primaryCode: {
            code: $('#code').val() + " (PhysicalComponent)",
            codeType: "identifier"
        },
        abbreviation: $('#abbreviation').val() + " (PhC)",
        teachingLanguage: $('#teachingLanguage').val(),
        name: getMultilingualTextJSONFor("courseName"),
        description: getMultilingualEditorJSON("physicalDescription"),
        resultExpected: true,
        offeringType: "course",
        priceInformation: [costJson],
        startDate: $('#physicalStartDate').val(),
        endDate: $('#physicalEndDate').val(),
        link: $('#link_to_enroll').val(),
        minNumberStudents: $('#minNumberStudents').val(),
        maxNumberStudents: $('#maxNumberStudents').val(),
        enrollStartDate: $('#startEnrollDate').val(),
        enrollEndDate: $('#endEnrollDate').val(),
        modeOfDelivery: ["on campus"],
        addresses: [courseAddress],
        organization: $('#physicalUniversity').val(),
        course: courseId
    }

    // Remove empty values
    PhysicalComponentOfferingData = cleanNullsOrEmpties(PhysicalComponentOfferingData);

    if (isEditMode)
    {
        PhysicalComponentOfferingData.offeringId = physicalOfferingIdEd;
    } // Ensure no new Id 

    console.log("Ready to post Physical offering data: ", PhysicalComponentOfferingData);


    let whichEndpoint = isEditMode
            ? ooapiDefaultEndpointURL + "/offerings/" + physicalOfferingIdEd
            : ooapiDefaultEndpointURL + "/offerings";

    let methodToFollow = isEditMode ? "PUT" : "POST";

    console.log("postPhysicalOffering: isEditMode? : " , isEditMode);
    console.log("postPhysicalOffering: methodToFollow? : " , methodToFollow);
    console.log("postPhysicalOffering: whichEndpoint? : " , whichEndpoint);

    try {
        response = await fetch(whichEndpoint, {
            method: methodToFollow,
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + localStorage.getItem("jwt")
            },
            body: JSON.stringify(PhysicalComponentOfferingData)
        });

        // Read the response body as text
        const text = await response.text();

        let parsedResponse = manageResponse(response, text, "postPhysicalComponentOffering");

        console.log("parsedResponse: ", parsedResponse);

        return parsedResponse;

    } catch (err)
    {
        console.error("Network or unexpected error:", err);
        showAlert("error", "Error", "Network or unexpected error:\n" + err.message);
        return null;
    }

}


async function postVirtualComponentOffering(courseId) {

    const startDate = $('#virtualStartDate').val() ? $('#virtualStartDate').val() : null;
    const endDate = $('#virtualEndDate').val() ? $('#virtualEndDate').val() : null;

    VirtualComponentOfferingData = {
        primaryCode: {
            code: $('#code').val() + " (virtualComponent)",
            codeType: "identifier"
        },
        abbreviation: $('#abbreviation').val() + " (VirtC)",
        teachingLanguage: $('#teachingLanguage').val(),
        name: getMultilingualTextJSONFor("courseName"),
        description: getMultilingualEditorJSON("virtualDescription"),
        resultExpected: false,
        offeringType: "course",
        startDate: $('#virtualStartDate').val(),
        endDate: $('#virtualEndDate').val(),
        modeOfDelivery: ["online"],
        course: courseId
    }

    // Remove empty values
    VirtualComponentOfferingData = cleanNullsOrEmpties(VirtualComponentOfferingData);

    if (isEditMode)
    {
        VirtualComponentOfferingData.offeringId = virtualOfferingIdEd;
    } // Ensure no new Id 

    console.log("Ready to post Virtual offering data: ", VirtualComponentOfferingData);

    let whichEndpoint = isEditMode
            ? ooapiDefaultEndpointURL + "/offerings/" + virtualOfferingIdEd
            : ooapiDefaultEndpointURL + "/offerings";

    let methodToFollow = isEditMode ? "PUT" : "POST";

    console.log("postVirtualComponentOffering: isEditMode? : " , isEditMode);
    console.log("postVirtualComponentOffering: methodToFollow? : " , methodToFollow);
    console.log("postVirtualComponentOffering: whichEndpoint? : " , whichEndpoint);

    try {
        response = await fetch(whichEndpoint, {
            method: methodToFollow,
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + localStorage.getItem("jwt")
            },
            body: JSON.stringify(VirtualComponentOfferingData)
        });

        // Read the response body as text
        const text = await response.text();

        let parsedResponse = manageResponse(response, text, "postVirtualComponentOffering");

        console.log("parsedResponse: ", parsedResponse);

        return parsedResponse;

    } catch (err)
    {
        console.error("Network or unexpected error:", err);
        showAlert("error", "Error", "Network or unexpected error:\n" + err.message);
        return null;
    }


}

function manageResponse(responseResult, textResult, messageHelper) {

    // If response is NOT OK (4xx or 5xx)
    if (!responseResult.ok) {
        console.error("Submission failed with status:", responseResult.status, responseResult.statusText);
        console.error("Server response:", textResult);

        // Try to show something meaningful to the user
        let message = `Submission failed (${responseResult.status} ${responseResult.statusText})`;
        if (textResult)
            message += `\n\nServer response:\n${textResult}`;

        showAlert("error", "Error at " + messageHelper, message);

        return null;
    }

    // Try to parse JSON if possible
    let parsed;
    try {
        parsed = JSON.parse(textResult);
        console.log("Success" + messageHelper + ": submitted to " + endpointURL + "!");
        console.log(messageHelper + ": submitted successfully:", parsed);
    } catch (e)
    {
        console.warn("Response is not valid JSON, returning raw text.");
        parsed = textResult;
    }

    return parsed;

}



function post(resource, data) {

    response = fetch(endpointURL + "/" + resource, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + localStorage.getItem('jwt')   // If security is disabled, will be ignored
        },
        body: JSON.stringify(data)
    });
    return response;
}


async function loadCourseForEdit(courseId) {

    resetAllMultilingualFields();

    let {courseJSON, offeringsJSON, resultCoordinatorsList, resultProgramsList} = await loadAsyncCourseData(ooapiDefaultEndpointURL, courseId);

    switch (formCourseType) {

        case "stdCourse":
            populateCourseForm(courseJSON, offeringsJSON, resultCoordinatorsList, resultProgramsList);
            break;
        case "BIPCourse":
            populateBIPCourseForm(courseJSON, offeringsJSON, resultCoordinatorsList, resultProgramsList);
            break;
    }
}


async function populateCourseForm(course, offerings, courseCoordinators, programs) {

    console.log("populateCourseForm: loading course...");

    setMultilingualTextField("courseName", course.name);

    document.getElementById("code").value = course.primaryCode.code ?? "";
    document.getElementById("abbreviation").value = course.abbreviation ?? "";
    document.getElementById("validStartDate").value = course.validFrom ?? "";
    document.getElementById("validEndDate").value = course.validTo ?? "";
    document.getElementById("teachingLanguage").value = course.teachingLanguage ?? "";
    document.getElementById("level").value = course.level ?? "";
    document.getElementById("studyLoadType").value = course.studyLoad.studyLoadUnit ?? "";
    document.getElementById("studyLoad").value = course.studyLoad.value ?? "";
    document.getElementById("fieldsOfStudy").value = course.fieldsOfStudy ?? "";

    document.getElementById("link_to_more_info").value = course.link ?? "";

    populateSelectMultiple("modeOfDelivery", course.modeOfDelivery);

    // Multilingual editors
    setMultilingualEditorContent("description", course.description);
    setMultilingualEditorContent("learningOutcomes", course.learningOutcomes);
    setMultilingualEditorContent("assessment", course.assessment);
    setMultilingualEditorContent("admissionRequirements", course.admissionRequirements);
    setMultilingualEditorContent("enrollment", course.enrollment);

    // Offering / dates
    console.log("populateCourseForm: Checking offerings...");

    if (offerings.items?.length > 0) {
        const offering = offerings.items[0];
        console.log("populateCourseForm: Load offering.items[0]...");

        offeringIdEd = offering.offeringId;

        document.getElementById("startDate").value = offering.startDate ?? "";
        document.getElementById("endDate").value = offering.endDate ?? "";
        document.getElementById("maxNumberStudents").value = offering.maxNumberStudents ?? "";
        document.getElementById("minNumberStudents").value = offering.minNumberStudents ?? "";
        document.getElementById("startEnrollDate").value = offering.enrollStartDate ?? "";
        document.getElementById("endEnrollDate").value = offering.enrollEndDate ?? "";
        document.getElementById("amount").value = offering.priceInformation?.[0]?.amount ?? "";
        document.getElementById("link_to_enroll").value = offering.link ?? "";

        setMultilingualEditorContent("offeringDescription", offering.description);

        // Address
        const addr = offering.addresses?.[0];
        if (addr) {
            document.getElementById("street").value = addr.street ?? "";
            document.getElementById("streetNumber").value = addr.streetNumber ?? "";
            document.getElementById("postalCode").value = addr.postalCode ?? "";
            document.getElementById("city").value = addr.city ?? "";
            document.getElementById("countryCode").value = addr.countryCode ?? "";
            document.getElementById("latitude").value = addr.geolocation?.latitude ?? "";
            document.getElementById("longitude").value = addr.geolocation?.longitude ?? "";

            setMultilingualEditorContent("addressAdditional", addr.additional);
        }
    }

    // Fill form coordinators with received ones
    coordinators = Array.from(courseCoordinators);
    updateCoordinatorList();
    console.log(coordinators);

    // Programs are still to be handled in the form

}

// For BIPs

// Needed when editing 

let physicalOfferingIdEd;
let virtualOfferingIdEd;

async function populateBIPCourseForm(course, offerings, courseCoordinators, programs) {

    console.log("populateBIPCourseForm: loading course...");

    setMultilingualTextField("courseName", course.name);

    document.getElementById("code").value = course.primaryCode.code ?? "";
    document.getElementById("abbreviation").value = course.abbreviation ?? "";
    document.getElementById("validStartDate").value = course.validFrom ?? "";
    document.getElementById("validEndDate").value = course.validTo ?? "";
    document.getElementById("teachingLanguage").value = course.teachingLanguage ?? "";
    document.getElementById("level").value = course.level ?? "";
    document.getElementById("studyLoadType").value = course.studyLoad.studyLoadUnit ?? "";
    document.getElementById("studyLoad").value = course.studyLoad.value ?? "";
    document.getElementById("fieldsOfStudy").value = course.fieldsOfStudy ?? "";

    document.getElementById("link_to_more_info").value = course.link ?? "";

    populateSelectMultiple("modeOfDelivery", course.modeOfDelivery);

    // Multilingual editors
    setMultilingualEditorContent("description", course.description);
    setMultilingualEditorContent("learningOutcomes", course.learningOutcomes);
    setMultilingualEditorContent("assessment", course.assessment);
    setMultilingualEditorContent("admissionRequirements", course.admissionRequirements);
    setMultilingualEditorContent("enrollment", course.enrollment);

    // Offering / dates
    console.log("populateBIPCourseForm: Checking offerings...");

    if (offerings.items?.length > 0) {

        // Locate which is physical o virtual
        let idNormOrPhy = 0; // Default: The first offering is normal or physical component
        let idVirt = 1;      // Default: The second offering is virtual component

        // Actualy?
        let firstItemCode = offerings.items[0].primaryCode.code;
        if (firstItemCode.includes("virtualComponent"))
        {
            idVirt = 0;      // The first offering is virtual component
            idNormOrPhy = 1; // The first offering is virtual component
        }

        const physicalOffering = offerings.items[idNormOrPhy];

        physicalOfferingIdEd = physicalOffering.offeringId;

        document.getElementById("physicalStartDate").value = physicalOffering.startDate ?? "";
        document.getElementById("physicalEndDate").value = physicalOffering.endDate ?? "";
        document.getElementById("maxNumberStudents").value = physicalOffering.maxNumberStudents ?? "";
        document.getElementById("minNumberStudents").value = physicalOffering.minNumberStudents ?? "";
        document.getElementById("startEnrollDate").value = physicalOffering.enrollStartDate ?? "";
        document.getElementById("endEnrollDate").value = physicalOffering.enrollEndDate ?? "";
        document.getElementById("amount").value = physicalOffering.priceInformation?.[0]?.amount ?? "";
        // Not present in BIPS
        // document.getElementById("link_to_enroll").value = physicalOffering.link ?? "";

        setMultilingualEditorContent("physicalDescription", physicalOffering.description);

        // Address
        const addr = physicalOffering.addresses?.[0];
        if (addr) {
            document.getElementById("street").value = addr.street ?? "";
            document.getElementById("streetNumber").value = addr.streetNumber ?? "";
            document.getElementById("postalCode").value = addr.postalCode ?? "";
            document.getElementById("city").value = addr.city ?? "";
            document.getElementById("countryCode").value = addr.countryCode ?? "";
            document.getElementById("latitude").value = addr.geolocation?.latitude ?? "";
            document.getElementById("longitude").value = addr.geolocation?.longitude ?? "";

            setMultilingualEditorContent("addressAdditional", addr.additional);
        }

        const virtualOffering = offerings.items[idVirt];
        virtualOfferingIdEd = virtualOffering.offeringId;

        document.getElementById("virtualStartDate").value = physicalOffering.startDate ?? "";
        document.getElementById("virtualEndDate").value = physicalOffering.endDate ?? "";

        setMultilingualEditorContent("virtualDescription", physicalOffering.description);

    }

    // Coordinators disabled in BIPS
    // Fill form coordinators with received ones
    //    coordinators = Array.from(courseCoordinators);
    //    updateCoordinatorList();
    //    console.log(coordinators);

    // Programs are still to be handled in the form

}


function populateSelectMultiple(item, values) {
    const select = document.getElementById(item);

    Array.from(select.options).forEach(option => {
        option.selected = values.includes(option.value);
    });
}


function showTab(index) {
    const contents = document.querySelectorAll("general-tabs .tab-content");
    const buttons = document.querySelectorAll("general-tabs .tab-btn");
    contents.forEach((content, i) => {
        content.classList.toggle("active", i === index);
    });
    buttons.forEach((btn, i) => {
        btn.classList.toggle("active", i === index);
    });
}

function showTabLanguage(index) {
    const contents = document.querySelectorAll("lang-tabs .tab-content");
    const buttons = document.querySelectorAll("lang-tabs .tab-btn");
    contents.forEach((content, i) => {
        content.classList.toggle("active", i === index);
    });
    buttons.forEach((btn, i) => {
        btn.classList.toggle("active", i === index);
    });
}

// Handle coordinators

let coordinators = [];

function addCoordinator() {

    const givenName = document.getElementById('coordinator_name').value.trim();
    const surname = document.getElementById('coordinator_surname').value;
    const mail = document.getElementById('coordinator_email').value;

    if (!givenName || !surname || !mail) {
        showAlert("error", "Error", "Please fill in all fields correctly.");
        return;
    }

    let coordinator = {
        givenName,
        surname,
        mail
    };

    coordinators.push(coordinator);
    updateCoordinatorList();
    // Clear inputs
    document.getElementById('coordinator_name').value = '';
    document.getElementById('coordinator_surname').value = '';
    document.getElementById('coordinator_email').value = '';
}

function updateCoordinatorList() {
    const list = document.getElementById('coordinator-list');
    list.innerHTML = ''; // Clear existing list

    const header = document.createElement('li');
    const nameHeaderSpan = document.createElement('span');
    nameHeaderSpan.textContent = "Name";
    const surnameHeaderSpan = document.createElement('span');
    surnameHeaderSpan.textContent = "Surname";
    const emailHeaderSpan = document.createElement('span');
    emailHeaderSpan.textContent = "Email";
    const removeButtonSpan = document.createElement('span');
    removeButtonSpan.textContent = "   Action     ";

    header.appendChild(nameHeaderSpan);
    header.appendChild(surnameHeaderSpan);
    header.appendChild(emailHeaderSpan);
    header.appendChild(removeButtonSpan);
    list.appendChild(header);
    coordinators.forEach((coordinator, index) => {
        const li = document.createElement('li');
        const nameSpan = document.createElement('span');
        nameSpan.textContent = coordinator.givenName;
        const surnameSpan = document.createElement('span');
        surnameSpan.textContent = coordinator.surname;
        const emailSpan = document.createElement('span');
        emailSpan.textContent = coordinator.mail;
        li.appendChild(nameSpan);
        li.appendChild(surnameSpan);
        li.appendChild(emailSpan);
        const removeBtn = document.createElement('button');
        removeBtn.textContent = 'Remove';
        removeBtn.id = "remove-list-button";
        removeBtn.onclick = () => {
            coordinators.splice(index, 1);
            updateCoordinatorList();
        };
        li.appendChild(removeBtn);
        list.appendChild(li);
    });
}


function validateDates() {

    if (formCourseType != "BIPCourse")  // BIP has dates for PC and VC
    {
        if (!document.getElementById("startDate").value) {
            showAlert("error", "Error", "Offering start date is missing or incorrect");
            return false;
        } else {
            const startDate = new Date(document.getElementById("startDate").value);
        }

        if (!document.getElementById("endDate").value) {
            showAlert("error", "Error", "Offering endDate date is missing or incorrect");
            return false;
        } else {
            const endDate = new Date(document.getElementById("endDate").value);
        }

        if (startDate && endDate && endDate < startDate) {
            showAlert("error", "Error", "End date of offering must be after start date.");
            return false;
        }
    }
    const startEnrollDate = new Date(document.getElementById("startEnrollDate").value);
    const endEnrollDate = new Date(document.getElementById("endEnrollDate").value);

    // Check enroll dates
    if (startEnrollDate && endEnrollDate) {
        if (endEnrollDate < startEnrollDate) {
            showAlert("error", "Error", "Enrollment end date must be after enrollment start date.");
            return false;
        }
        if (formCourseType != "BIPCourse")  // BIP has dates for PC and VC
        {
            if (startDate && endEnrollDate > startDate) {
                showAlert("error", "Error", "Enrollment end date must not be after course start date.");
                return false;
            }
        }
    } else if (startEnrollDate || endEnrollDate) {
        showAlert("error", "Error", "Both enrollment start and end dates must be provided.");
        return false;
    }

    return true;
}

function renderForm() {

    document.getElementById("destEndpoint").textContent = "OEAPI Endpoint -> " + endpointURL + ")";
    manageAdminItems();
}
