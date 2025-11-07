/*****************************************
 
 Code for Forms
 
 ******************************************/



const courseNames = {};

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
        ClassicEditor.create(el , {  // Config - Disable font change
						removePlugins: ['FontFamily', 'FontSize', 'FontColor', 'FontBackgroundColor'],
						htmlSupport: {
							disallow: [
								{ name: /.*/, attributes: ['style'], classes: true }
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
						console.log("Text changed: ",fieldId,currentLanguage,editor.getData().trim());
						
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
						
						console.log("CKeditor for "+fieldId+" Switched from "+oldLang+" to "+newLang);
						updateSavedEditorLangs(fieldId);
					});
					
				console.log("CKEditor initialized for: "+fieldId);		
					
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
		
		console.log("saveAllEditorsData: Checking "+fieldId);
		
        if (editorsData.hasOwnProperty(fieldId)) {
            var editorInstance = editorsData[fieldId]._editorInstance;
            if (!editorInstance)
                continue; // skip if no CKEditor instance

			// Get actual visible language from select 
			var langSelect = document.querySelector('select.lang-select[data-field="' + fieldId + '"]');
			var lang = langSelect ? langSelect.value : (currentLang[fieldId] || "en-GB");
			
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
            console.warn("⚠️ Missing text input for field:", fieldId);
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
    if (!map.hasOwnProperty(lang)) continue;
    var v = map[lang];
    if (typeof v === "string" && v.trim() !== "") {
      out.push({ language: lang, value: v.trim() });
    }
  }
  return out;
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
        languages.forEach((lang, index) => {
            const option = document.createElement("option");
            option.value = lang.iso639_2;
            option.textContent = lang.name;
            if (index === 0)
                option.selected = true; // Default to English
            select.appendChild(option);
        });
    });

}



function fillFieldsOfStudy(elementId) {
    const select = document.getElementById(elementId);
    select.innerHTML = "Loading... ";
    //console.log("fechFieldsOfStudy ..." + endpointURL);
    fetch(endpointURL + "/fieldsofstudy?level=1", {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        }
    })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                select.innerHTML = ""; // Clear loading or old options
                data.items.forEach(fieldOfStudy => {
                    const option = document.createElement("option");
                    option.value = fieldOfStudy.fieldsOfStudyId;
                    option.textContent = fieldOfStudy.txtEn;
                    select.appendChild(option);
                });
            })
            .catch(error => {
                console.error("Failed to load fields of study:", error);
                select.innerHTML = `<option value="">Error loading fields of study</option>`;
            });
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
      Object.keys(obj).forEach(function(key) {
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
      .filter(function(lang) { return window.addressAdditionalTexts[lang].trim() !== ""; })
      .map(function(lang) {
        return { language: lang, value: window.addressAdditionalTexts[lang].trim() };
      });
  } else {
    // fallback to single text field
    var addEl = document.getElementById("addressAdditional");
    if (addEl && addEl.value.trim()) {
      address.additional = [{ language: "en-GB", value: addEl.value.trim() }];
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

/*
document.querySelectorAll(".lang-tabs .lang-tab-btn").forEach(button => {
    button.addEventListener("click", () => {
        const lang = button.dataset.lang;
        const parent = button.closest("#tab-basic-info"); // adjust if language tabs are in other tabs too

        parent.querySelectorAll(".lang-tab-btn").forEach(btn => btn.classList.remove("active"));
        parent.querySelectorAll(".lang-content").forEach(el => el.classList.remove("active"));
        button.classList.add("active");
        parent.querySelector(`#desc-${lang}`).classList.add("active");
        document.querySelectorAll(".lang-content").forEach(div => {
            div.style.display = "none";
        });
        document.getElementById(`desc-${lang}`).style.display = "block";
    });
});
*/

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
			input.scrollIntoView({ behavior: 'smooth', block: 'center' });

			allValid = false;
			return; // Stop further validation
		  }
		}
	  }
			
		/* Gather all info from CKEditors */
		
		for (const fieldId in editorsData) {
			if (!editorsData.hasOwnProperty(fieldId)) continue;

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
                        givenName: coordinator.name,
                        surname: coordinator.surname,
                        mail: coordinator.email,
                        displayName: coordinator.name + " " + coordinator.surname,
                        activeEnrollment: false,
                        affiliations: ["employee"],
                        primaryCode: {
                            code: coordinator.email,
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


				/* Post main course data */

                let postCourseResponse = await postCourse(coordinatorsId, function (err, response) {
                    postCourseResponse = response;
                    console.log("On submit: Posting course response: ", response);
                });
				
				/* Post offerings */
				
				/* postCourseResponse.courseId is the ID of parent course for offering */

                switch (formCourseType) {

                    case "stdCourse":
                        let postCourseOfferResponse = await postOffering(postCourseResponse.courseId, function (err, response) {
                            console.log("On submit (stdCourse): postOffering response: ", offeringData);
                            // TBD rollback could be neccesary if fails
                        });
                        break;

                    case "BIPCourse":
                        // Physical Component 	  
                        let postPhysicalComponentCourseOfferResponse = await postPhysicalComponentOffering(postCourseResponse.courseId, function (err, response) {
                            console.log("On submit (Physical Component): postOffering response: ", PhysicalComponentOfferingData);
                            // TBD a rollback could be neccesary if fails
                        });
                        // Virtual Component 	  
                        let postVirtualComponentCourseOfferResponse = await postVirtualComponentOffering(postCourseResponse.courseId, function (err, response) {
                            console.log("On submit (Virtual Component): postOffering response: ", VirtualComponentOfferingData);
                            // TBD a rollback could be neccesary if fails
                        });

                        break;

                } // end switch

            } // end if-else validate dates
            
        } else // No valid session and JWT activated
        {
           alert("You cannot update data in your OEAPI Endpoint. Maybe you are not logged or your session has expired");
        }
 });


async function postCourse(listCoordinators, callback) {

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
        assessment: getMultilingualEditorJSON("assesment"),
        enrollment: getMultilingualEditorJSON("enrollment"),
        abbreviation: $('#abbreviation').val(),
		link: $('#link_to_more_info').val(),
        teachingLanguage: $('#teachingLanguage').val(),
        organization: ooapiDefaultOrganizationId,
        level: $('#level').val(),
        coordinators: listCoordinators,
        primaryCode: {
            codeType: "identifier",
            code: $('#code').val()
        }
    };
	
	// Remove empty values
	
	console.log("postCourse: Json previous cleaning of empty values.. " + safeToString(courseData));

	courseData = cleanNullsOrEmpties(courseData);
	
    console.log("postCourse: Ready to post course data to " + endpointURL + "/courses with " + safeToString(courseData), courseData);

    var univ = ooapiDefaultShortUnivName; // from init.js

    try {
        const response = await fetch(`${endpointURL}/courses`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('jwt')   // If security is disabled, will be ignored
            },
            body: JSON.stringify(courseData)
        });

        // Read the response body as text
        const text = await response.text();

        let parsedResponse = manageResponse(response, text, "postCourse");
        console.log("parsedResponse: ",parsedResponse);
        
        if (parsedResponse == null)
         {  alert("Submission of Course failed!"); }

        return parsedResponse;

    } catch (err)
    {
        console.error("Network or unexpected error:", err);
        alert("Network or unexpected error:\n" + err.message);
        return null;
    }
}

async function postOffering(courseId, callback) {

    const minNumberStudents = $('#minNumberStudents').val() ? $('#minNumberStudents').val() : null;
    const maxNumberStudents = $('#maxNumberStudents').val() ? $('#maxNumberStudents').val() : null;
    
	const courseAddress = buildAddressJSON();
	
	var costJson = null;
	var costInForm = $('#amount').val() ? $('#amount').val() : null;
	
    if (costInForm)
    {
        costJson = {
            amount: $('#amount').val(),
            currency: "Euro",
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
		cost : costJson,
		link: $('#link_to_enrollment').val(),
        offeringType: "course",
        startDate: $('#startDate').val(),
        endDate:   $('#endDate').val(),
        modeOfDelivery: $('#modeOfDelivery').val(),
		addresses: [courseAddress],
        course: courseId
    }
	
	// Remove empty values
	OfferingData = cleanNullsOrEmpties(OfferingData);
		
    console.log("Ready to post offering data: ",OfferingData);

    try {
        response = await fetch(endpointURL + "/offerings", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + localStorage.getItem('jwt')   // If security is disabled, will be ignored
				
            },
            body: JSON.stringify(OfferingData)
        });


        // Read the response body as text
        const text = await response.text();

        let parsedResponse = manageResponse(response, text, "postOfferingData");
        console.log("parsedResponse: ",parsedResponse);
        
        if (parsedResponse == null)
         {  alert("Submission of Offering Data failed!"); }

        return parsedResponse;

    } catch (err)
    {
        console.error("Network or unexpected error:", err);
        alert("Network or unexpected error:\n" + err.message);
        return null;
    }

}


async function postPhysicalComponentOffering(courseId, callback) {

    const startDate = $('#physicalStartDate').val() ? $('#physicalStartDate').val() : null;
    const endDate = $('#physicalEndDate').val() ? $('#physicalEndDate').val() : null;
	
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
        startDate: $('#physicalStartDate').val(),
        endDate: $('#physicalEndDate').val(),
        modeOfDelivery: ["on campus"],
		addresses: [courseAddress],
        course: courseId
    }
	
	// Remove empty values
	PhysicalComponentOfferingData = cleanNullsOrEmpties(PhysicalComponentOfferingData);	
	
    console.log(PhysicalComponentOfferingData);

    try {
        response = await fetch(endpointURL + "/offerings", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
				"Authorization": "Bearer " + localStorage.getItem('jwt')   // If security is disabled, will be ignored
            },
            body: JSON.stringify(PhysicalComponentOfferingData)
        });


        // Read the response body as text
        const text = await response.text();

        let parsedResponse = manageResponse(response, text, "postPhysicalComponentOffering");
        console.log("parsedResponse: ",parsedResponse);
        
        if (parsedResponse == null)
         {  alert("Submission of PhysicalComponentOffering failed!"); }

        return parsedResponse;

    } catch (err)
    {
        console.error("Network or unexpected error:", err);
        alert("Network or unexpected error:\n" + err.message);
        return null;
    }

}


async function postVirtualComponentOffering(courseId, callback) {

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
        resultExpected: true,
        offeringType: "course",
        startDate: $('#virtualStartDate').val(),
        endDate: $('#virtualEndDate').val(),
        modeOfDelivery: ["online"],
        course: courseId
    }

	// Remove empty values
	VirtualComponentOfferingData = cleanNullsOrEmpties(VirtualComponentOfferingData);	

    console.log(VirtualComponentOfferingData);

    try {
        response = await fetch(endpointURL + "/offerings",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
						"Authorization": "Bearer " + localStorage.getItem('jwt')   // If security is disabled, will be ignored
                    },
                    body: JSON.stringify(VirtualComponentOfferingData)
                });

        // Read the response body as text
        const text = await response.text();

        let parsedResponse = manageResponse(response, text, "postVirtualComponentOffering");
        console.log("parsedResponse: ",parsedResponse);
        
        if (parsedResponse == null)
         {  alert("Submission of VirtualComponentOffering failed!"); }

       return parsedResponse;

    } catch (err)
    {
        console.error("Network or unexpected error:", err);
        alert("Network or unexpected error:\n" + err.message);
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
        alert(message);

        return null;
    }

    // Try to parse JSON if possible
    let parsed;
    try {
        parsed = JSON.parse(textResult);
        alert(messageHelper + ": submitted to " + endpointURL + "!");
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
const coordinators = [];

function addCoordinator() {
    const name = document.getElementById('coordinator_name').value.trim();
    const surname = document.getElementById('coordinator_surname').value;
    const email = document.getElementById('coordinator_email').value;
    if (!name || !surname || !email) {
        alert("Please fill in all fields correctly.");
        return;
    }

    const coordinator = {
        name,
        surname,
        email
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
        nameSpan.textContent = coordinator.name;
        const surnameSpan = document.createElement('span');
        surnameSpan.textContent = coordinator.surname;
        const emailSpan = document.createElement('span');
        emailSpan.textContent = coordinator.email;
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
            alert("Offering start date is missing or incorrect");
            return false;
        } else {
            const startDate = new Date(document.getElementById("startDate").value);
        }

        if (!document.getElementById("endDate").value) {
            alert("Offering endDate date is missing or incorrect");
            return false;
        } else {
            const endDate = new Date(document.getElementById("endDate").value);
        }
		
        if (startDate && endDate && endDate < startDate) {
            alert("End date of offering must be after start date.");
            return false;
        }		
    }
    const startEnrollDate = new Date(document.getElementById("startEnrollDate").value);
    const endEnrollDate   = new Date(document.getElementById("endEnrollDate").value);
  
    // Check enroll dates
    if (startEnrollDate && endEnrollDate) {
        if (endEnrollDate < startEnrollDate) {
            alert("Enrollment end date must be after enrollment start date.");
            return false;
        }
        if (formCourseType != "BIPCourse")  // BIP has dates for PC and VC
        {
            if (startDate && endEnrollDate > startDate) {
                alert("Enrollment end date must not be after course start date.");
                return false;
            }
        }
    } else if (startEnrollDate || endEnrollDate) {
        alert("Both enrollment start and end dates must be provided.");
        return false;
    }

    return true;
}

function renderForm() {

    document.getElementById("destEndpoint").textContent = "OEAPI Endpoint -> " + endpointURL + ")";
    manageAdminItems();
}