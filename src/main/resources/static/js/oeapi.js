const languages = [
    {code: "en-GB", name: "English (UK)"},
    {code: "es-ES", name: "Spanish"},
    {code: "fr-FR", name: "French"},
    {code: "pt-PT", name: "Portuguese"},
    {code: "it-IT", name: "Italian"},
    {code: "ro-RO", name: "Romanian"},
    {code: "de-DE", name: "German"},
    {code: "nl-NL", name: "Dutch"},
    {code: "el-GR", name: "Greek"},
    {code: "ca-ES", name: "Catalan"},
    {code: "eu-ES", name: "Basque"}
];


const languagesISO639 = [
    {iso639_2: "eng", name: "English"},
    {iso639_2: "spa", name: "Spanish"},
    {iso639_2: "fra", name: "French"},
    {iso639_2: "por", name: "Portuguese"},
    {iso639_2: "ita", name: "Italian"},
    {iso639_2: "ron", name: "Romanian"},
    {iso639_2: "ger", name: "German"},
    {iso639_2: "nld", name: "Dutch"},
    {iso639_2: "ell", name: "Greek"},
    {iso639_2: "cat", name: "Catalan"},
    {iso639_2: "eus", name: "Basque"},
    {iso639_2: "mul", name: "Multiple Languages"}  // Special case 
];

// Extract name from OOAPI entity, use `language` list order as
// preference.
function extractName({name}) {
    return name && name.find(n => languages.map(({code}) => n.language == code).find(x => x)).value;
}

// Sort items by name.
async function sortAsyncItemsByName(asyncItems) {
    return (await asyncItems).sort((a, b) => {
        const aName = extractName(a);
        const bName = extractName(b);
        return a > b ? -1 : (b > a ? 1 : 0)
    });
}

// Fetch JSON from path.
async function fetchData(path) {
    const response = await fetch(`${endpointURL}/${path}`, {
        headers: {'Content-Type': 'application/json'}
    });
    if (!response.ok) {
        throw new Error(`Failed to fetch ${path}, got status ${response.status}`);
    }
    return response.json();
}

// Fetch items from path.
async function fetchItems(path) {
    return (await fetchData(path)).items;
}

const getAndMergeAllCoursesData = async () => {

    const response = await fetch(ooapiDefaultEndpointURL + "/courses");
    const res = await response.json();
    console.log("getAndMergeAllCoursesData res:", res)
    return res.items;

}

const input = document.getElementById("search");
const resultsContainer = document.getElementById("results");

function getCourseUniv(courseJson) {
    return ooapiDefaultShortUnivName;
}

async function renderAllMergedCoursesData() {
    const data = await getAndMergeAllCoursesData();
    console.log("renderAllMergedCoursesData data:");
    console.log(data);
    console.log("renderAllMergedCoursesData -> Filter by level: " + filterLevel);
    if (filterLevel != "NONE")
    {
        renderAllCoursesResults(data.filter(({ level }) => level.toLowerCase().includes(filterLevel.toLowerCase())));
    } else
    {
        renderAllCoursesResults(data);
    }

    // Watchout search box...
    input.onkeyup = async (e) => {

        const q = e.target.value;
        if (filterLevel != "NONE")
        {
            results = data.filter(({ level }) => level.toLowerCase().includes(filterLevel.toLowerCase()))
                    .filter(({ name }) => name[0].value.toLowerCase().includes(q.toLowerCase()));
        } else
        {
            results = data.filter(({ name }) => nombreFiltro(name, q));
            // {name[0].value.toLowerCase().includes(q.toLowerCase()));}
        }

        renderAllCoursesResults(results, q);
    };

}

function renderAllCoursesResults(results, q = "") {

    console.log("Using OEAPI endpoint: " + ooapiDefaultEndpointURL);
    console.log("Results: ", results);

    if (results.length === 0) {
        resultsContainer.innerHTML = `
    <div class="col-12 text-center">
      No items found with level '${q}'.
    </div>
    `;
        manageAdminItems(); // if there is no course here we manage admin opts
        return;
    }

    console.log(results);

    const allCoursesElements = results
            .map((course) => {
                return allCoursesCards
                        .replace(/{imgSrc}/gi, htmltizeLogo(course))
                        .replace(/{link}/gi, 'course.html?courseId=' + course.courseId + '&univ=' + getCourseUniv(course))
                        .replace(/{title}/gi, course.name[0].value)
                        .replace(/{summary}/gi, "<p><strong>Mode of delivery:</strong> " + course.modeOfDelivery + "</p><p style='float:right;font-size: x-small;'><strong>Course Code:</strong> &nbsp; " + course.primaryCode.code + "<strong><br>Id: </strong>" + course.courseId + "</p>")
                        .replace(/{footcolor}/gi, cardFooterColor(course.level))
                        .replace(/{level}/gi, "Course Level: " + (course.level == 'bachelor' ? 'degree' : course.level));
            })
            .join("");

    resultsContainer.innerHTML = allCoursesElements;
    manageAdminItems();

}


function nombreFiltro(nombre, q) {
    res = null;
    if (nombre[0].value) {
        res = nombre[0].value.toLowerCase().includes(q.toLowerCase());
    }
    return res;
}


/* For Single Course */

let CourseType;  // Help to know which form use in case of edit

async function loadFullCourseData(univShortName, courseID) {


    try {
        let {courseJSON, offeringsJSON, resultCoordinatorsList, resultProgramsList} = await loadAsyncCourseData(ooapiDefaultEndpointURL, courseID);

        courseJSONData = courseJSON;  // To have visibility of during HTML some async rendering

        let accBipVirtDisplay = "none";
        let firstOfferingTitle = "Additional Course Info";

        let mainOffering, virtualOffering;

        let idVirt = 1;  // In case of BIPs it has two offerings; physical and virtual. In case of BIP this will hold position of the virtual one
        let idNormOrPhy = 0;  // Normal courses normally would have only one offering. In case of BIP this will hold position of the physical one

        // Is it a BIP (two offerings; physical and virtual) or ordinary course/microdential)
        if (offeringsJSON.items[1])  // has two offerings
        {
            console.log("Displaying a BIP...");
            CourseType = "BIPCourse";

            [mainOffering, virtualOffering] = offeringsJSON.items;  // Initial guess  

            accBipVirtDisplay = "block";   //BIP
            firstOfferingTitle = "Physical Component";

            // Which is for virtualComponent?
            let firstItemCode = offeringsJSON.items[0].primaryCode.code;
            if (firstItemCode.includes("virtualComponent"))
            {
                [virtualOffering, mainOffering] = offeringsJSON.items;
            }
        } else
        {
            CourseType = "OrdinaryCourse";

            mainOffering = offeringsJSON.items[0];

            accBipVirtDisplay = "none";
            firstOfferingTitle = "Additional Course Info";
            console.log("Displaying an ordinary or microdential course...");

        }

        console.log("Async call results:", courseJSON, offeringsJSON, resultCoordinatorsList, resultProgramsList);

        const oneCourseElement =
                oneCourseCard.replace(/{imgSrc}/gi, htmltizeLogo(courseJSON))
                .replace(/{cardHeader}/gi, "<strong>Level: </strong>" + courseJSON.level +
                        "&nbsp; " + htmltizeStudyLoad(courseJSON) +
                        "&nbsp; <strong>Mode of Delivery: </strong>" + courseJSON.modeOfDelivery)
                .replace(/{title}/gi, "<span style='font-size:1.25em; font-weight:bold'>" + htmltizeMultiLingualText(courseJSON, "name") + "</span>")
                .replace(/{assessment}/gi, htmltizeMultiLingualText(courseJSON, "assessment"))
                .replace(/{enrollment}/gi, htmltizeMultiLingualText(courseJSON, "enrollment"))
                .replace(/{targetUniversities}/gi, htmltizeTargetUniversities(courseJSON))
                .replace(/{summary}/gi, "<p>" + htmltizeMultiLingualText(courseJSON, "description") + "</p><p style='float:right;font-size: small;'><strong>Course Code:</strong> &nbsp; " + courseJSON.primaryCode.code + "<strong><br>Id: </strong>" + courseJSON.courseId + "</p>")
                .replace(/{footcolor}/gi, cardFooterColor(courseJSON.level))
                .replace(/{level}/gi, "Level: &nbsp; <strong style='color:white;'>" + (courseJSON.level.toLowerCase() == 'bachelor' ? 'degree' : courseJSON.level) + "</strong>")
                .replace(/{admissionRequirements}/gi, htmltizeMultiLingualText(courseJSON, "admissionRequirements"))
                .replace(/{learningOutcomes}/gi, htmltizeMultiLingualText(courseJSON, "learningOutcomes"))
                .replace(/{coordinators}/gi, (courseJSON.coordinators ? htmltizeCoordinators(resultCoordinatorsList) : "Not defined"))
                .replace(/{validStartDate}/gi, (courseJSON.validFrom ? courseJSON.validFrom : ""))
                .replace(/{validEndDate}/gi, (courseJSON.validTo ? courseJSON.validTo : ""))



                .replace(/{infoLink}/gi, (courseJSON.link ? courseJSON.link : "#"))

                // Activate or not accordions by course type
                .replace(/{firstOfferingTitle}/gi, firstOfferingTitle)
                .replace(/{accBipVirtDisplay}/gi, accBipVirtDisplay)


                // from offering
                .replace(/{costJson}/gi, (mainOffering && mainOffering.priceInformation ? mainOffering.priceInformation[0].amount + " Euros" : ""))
                .replace(/{startDate}/gi, (mainOffering && mainOffering.startDate ? mainOffering.startDate : ""))
                .replace(/{endDate}/gi, (mainOffering && mainOffering.startDate ? mainOffering.endDate : ""))
                .replace(/{enrollStartDate}/gi, (mainOffering && mainOffering.enrollStartDate ? mainOffering.enrollStartDate : ""))
                .replace(/{enrollEndDate}/gi, (mainOffering && mainOffering.enrollEndDate ? mainOffering.enrollEndDate : ""))
                .replace(/{minNumberStudents}/gi, (mainOffering && mainOffering.minNumberStudents ? mainOffering.minNumberStudents : ""))
                .replace(/{maxNumberStudents}/gi, (mainOffering && mainOffering.maxNumberStudents ? mainOffering.maxNumberStudents : ""))


                .replace(/{enrollLink}/gi, (mainOffering && mainOffering.link ? mainOffering.link : "#"))

                .replace(/{offeringDescription}/gi, (mainOffering && mainOffering.description[0] ? mainOffering.description[0].value : "Not defined.."))
                .replace(/{addresses}/gi, (mainOffering && mainOffering.addresses ? htmltizeAddresses(mainOffering.addresses) : "No additional address info."))

                // Beware, virtual component not always present
                .replace(/{virtualDescription}/gi, (virtualOffering && virtualOffering.description[0] ? virtualOffering.description[0].value : "Not defined.."))
                .replace(/{virtStartDate}/gi, (virtualOffering && virtualOffering.startDate ? virtualOffering.startDate : ""))
                .replace(/{virtEndDate}/gi, (virtualOffering && virtualOffering.endDate ? virtualOffering.endDate : ""));


        resultsContainer.innerHTML = oneCourseElement;
        // Enable or disable admin options, like add, delete, etc. if logged or not
        manageAdminItems();

    } catch (error) {
        resultsContainer.innerHTML = "<h3>Oops, something went wrong reading course data</h3>";
        console.error("Something went wrong reading course data:", error);
    }
}


function deleteCourse(theCourse, needToConfirm)
{

    console.log("deleteCourse: endpointURL..." + ooapiDefaultEndpointURL);
    console.log("deleteCourse: the Course..." + theCourse);
    console.log("deleteCourse token", localStorage.getItem('jwt'));

    if (!needToConfirm || (needToConfirm && confirm('Are you sure you want to delete this course?'))) {

        console.log("Delete course with Id: " + theCourse + " confirmed");
        console.log("Delete call to: " + ooapiDefaultEndpointURL + "/courses/" + theCourse);

        fetch(ooapiDefaultEndpointURL + "/courses/" + theCourse, {method: "DELETE",
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('jwt'),
                'Content-length': 0
            }
        })
                .then(async response => {
                    if (!response.ok) {
                        throw new Error('deleteCourse Failed to delete resource: ' + response.text());
                    }
                    return response.text(); // use `.json()` if server returns JSON
                })
                .then(data => {
                    console.log('Delete successful:', data);
                    window.location.href = "./catalog.html";
                })
                .catch(error => {
                    console.error('deleteCourse Error:', error);
                });
    }
}

function editCourse(theCourse) {

    let editPage = "./courseForm.html?courseId=" + theCourse + "&univ=dummy";  // Default for most course types

    if (CourseType === "BIPCourse")  // then assume BIP Course, it has 2 Offerings
    {
        editPage = "./bipForm.html?courseId=" + theCourse + "&univ=dummy";
    }

    window.location.href = editPage;
}


async function loadAsyncCourseData(ooapiEndPoint, courseID) {

    try {
        console.log("loadCourseData: Get course main data...");
        const courseData = await fetch(ooapiEndPoint + "/courses/" + courseID);
        const courseJSON = await courseData.json();

        console.log("loadCourseData: Get course offerings...");
        const offeringsData = await fetch(ooapiEndPoint + "/courses/" + courseID + "/offerings");
        const offeringsJSON = await offeringsData.json();

        // Get other values/keys
        const coordinators = courseJSON.coordinators || [];
        const programs = courseJSON.programs || [];
        // Prepare requests


        const getCoordinatorsArray = coordinators.map(personId =>
            fetch(ooapiEndPoint + "/persons/" + encodeURIComponent(personId))
                    .then(res => res.json())
                    .then(json => ({
                            ...json,
                            personId // put LAST so it cannot be overwritten
                        }))
        );


        const getProgramsArray = programs.map(p =>
            fetch(ooapiEndPoint + "/programs/" + encodeURIComponent(p))
        );
        // Await all fetch calls
        const [coordinatorResponses, programResponses] = await Promise.all([
            Promise.all(getCoordinatorsArray),
            Promise.all(getProgramsArray)
        ]);

        const resultCoordinatorsList = coordinatorResponses;

        const resultProgramsList = await Promise.all(
                programResponses.map(res => res.json())
                );

        console.log('Course:', courseJSON);
        console.log('OfferingsJSON:', offeringsJSON);
        console.log('Coordinators:', resultCoordinatorsList);
        console.log('Programs:', resultProgramsList);

        return {courseJSON, offeringsJSON, resultCoordinatorsList, resultProgramsList};

    } catch (error) {
        console.error('Error during API call sequence:', error);
        throw error;
    }

}

/* 	Security JWT  */


const token = localStorage.getItem("jwt");

async function canDoUpdates()
{
    let updatesArePossible = false

    let JTWSecurityEnabled = await isJwtSecurityEnabled();

    if (!JTWSecurityEnabled)
    {
        console.log("canDoUpdates? " + "Yes, JWT is disabled. Be sure this is what you want. See doc.");
        updatesArePossible = true;
    } else
    {
        if (!isJwtValid())
        {
            console.log("canDoUpdates? " + "No, JWT is enabled and no valid token found");
            updatesArePossible = false;
        } else
        {
            console.log("canDoUpdates? " + "Yes, JWT is enabled and valid token found");
            updatesArePossible = true;
        }
    }

    return  updatesArePossible;

}

function isJwtValid() {

    const token = localStorage.getItem("jwt");

    console.log("Token is: " + token);

    if (!token) {
        return false;
    }

    try {
        const payloadBase64 = token.split('.')[1];
        const payloadJson = atob(payloadBase64);
        const payload = JSON.parse(payloadJson);

        // exp is in seconds since epoch
        const now = Math.floor(Date.now() / 1000);
        console.log("jwt expires in: ", new Date(payload.exp * 1000));
        return payload.exp && payload.exp > now;
    } catch (e) {
        console.error("Invalid JWT format", e);
        return false;
    }
}

async function isJwtSecurityEnabled() {
    let isEnabled = false;

    let textResponse = await getJwtSecurityStatus();
    isEnabled = textResponse.toLowerCase().includes("enabled");

// getJwtSecurityStatus().then(textResponse => {
//	 isEnabled = textResponse.toLowerCase().includes("enabled")
//     console.log("isJwtSecurityEnabled :", isEnabled);
//	 });

    return isEnabled;
}


async function getJwtSecurityStatus() {

    let status = "Unknown";

    try {
        const response = await fetch('./auth/secStatus');
        if (!response.ok) {
            console.error("Error checking JWT security: no valid response");
            return status + ": no valid response";
        }

        const statusText = await response.text();
        console.log("Security Status:", statusText);

        const enabled = statusText.toLowerCase().includes("enabled");

        if (enabled)
        {
            status = "Enabled, you must login before updates";
        } else {
            status = " Disabled. (Updates are allowed without login. Check that's what you want. See doc.) ";
        }

    } catch (error) {
        console.error("Error checking JWT security:", error);
        showAlert("error", "Network Error", "Unable to check JWT security status. Is your endpoint active?");
        status = "Unknown, error checking JWT security ";
    }

    return status;
}

async function manageAdminItems()
{
    let JTWSecurityEnabled = await isJwtSecurityEnabled();

    if (!JTWSecurityEnabled) {
        console.log("manageAdminItems: No JWT activated, updates are allowed");
        document.getElementById("avatar").style = "display: none;";
        // addCourse is not ever present 
        if (document.getElementById("addCourse")) {
            document.getElementById("addCourse").style = "cursor: pointer;";
        }
        // Delete is only in single course view, 
        if (document.getElementById("deleteIcon")) {
            document.getElementById("deleteIcon").style = "display:block;";
        }
        document.getElementById("loginLink").style = "display : none";
        document.getElementById("administration").style = "display : none";
    } else
    if (isJwtValid()) {
        console.log("manageAdminItems: JWT activated and valid JWT token, can admin");
        document.getElementById("avatar").style = "cursor: pointer;";
        // addCourse is not ever present 
        if (document.getElementById("addCourse")) {
            document.getElementById("addCourse").style = "cursor: pointer;";
        }
        // Delete is only in single course view, 
        if (document.getElementById("deleteIcon")) {
            document.getElementById("deleteIcon").style = "display:block;";
        }
        document.getElementById("loginLink").style = "display : none";
        if (isAdmin()) {
            document.getElementById("administration").style = "cursor: pointer;";
        }
    } else
    {
        console.log("manageAdminItems: No valid JWT token, must login");
        document.getElementById("avatar").style = "display: none;";
        // addCourse is not ever present 
        if (document.getElementById("addCourse")) {
            document.getElementById("addCourse").style = "display : none";
        }
        // Delete is only in single course view, 
        if (document.getElementById("deleteIcon")) {
            document.getElementById("deleteIcon").style = "display:block;";
        }
        document.getElementById("loginLink").style = "cursor: pointer;";
        document.getElementById("administration").style = "display : none";
    }

}


function isAdmin() {
    const token = localStorage.getItem('jwt');
    if (!token)
        return false;

    // If you decode the token, check for 'roles' or 'authorities'
    const payload = JSON.parse(atob(token.split('.')[1]));
    console.log(payload);
    return payload.roles?.includes('ROLE_ADMIN') || payload.authorities?.includes('ROLE_ADMIN');
}


// Popup messages using Bootstrap

const alertQueue = [];
let alertShowing = false;

function showAlert(type, title, message) {
    alertQueue.push({type, title, message});
    processAlertQueue();
}

function processAlertQueue() {
    if (alertShowing)
        return;
    if (alertQueue.length === 0)
        return;

    const {type, title, message} = alertQueue.shift();
    alertShowing = true;

    showAlertModal(type, title, message);
}


function showAlertModal(type, title, message) {
    const modalEl = document.getElementById("alertModal");

    // Dispose any previous instance
    const existing = bootstrap.Modal.getInstance(modalEl);
    if (existing)
        existing.dispose();

    // Update content
    document.getElementById("alertModalTitle").innerText = title;
    document.getElementById("alertModalBody").innerText = message;

    const header = document.getElementById("alertModalHeader");
    header.classList.remove("bg-danger", "bg-success", "text-white");

    if (type === "error")
        header.classList.add("bg-danger", "text-white");
    if (type === "success")
        header.classList.add("bg-success", "text-white");

    // Create fresh modal
    const modal = new bootstrap.Modal(modalEl);

    // When the modal closes, show the next alert
    modalEl.addEventListener("hidden.bs.modal", () => {
        alertShowing = false;
        processAlertQueue();
    }, {once: true});

    modal.show();
}




/* HTML Templates for rendering */

const allCoursesCards = `
<div class="col-6 col-md-4">
	  <div class="card h-100">
	   <img src="{imgSrc}" class="card-img-top" style="height: 50%; width: 20%; padding: 0.2em;" alt="{title}"> 
	    <div class="card-body">
	      <h5 class="card-title">
	        <a href="{link}" class="text-decoration-none">{title}</a>
	      </h5>
	      <p class="card-text">{summary}</p>
	    </div>
	    <div class="card-footer" style="background-color:{footcolor};color:white; font-weight:600">{level}</div>
	  </div>
	</div>
	`;

let oneCourseCard = `
<div class="row">
  <div class="col card">
 
  <span style="height: 8%; width: 8%; margin: 0 0 .5em 0; "><img src="{imgSrc}"  style="height: 90%; padding: 0.2em;" alt="University Logo"></span>

  <div class="course-actions">
    <div class="edit-icon" id="editIcon" onclick="editCourse(theCourse)" style="cursor: pointer;">
        Edit this course <i class="fas fa-pencil fa-1x"></i>
    </div>

    <div class="delete-icon" id="deleteIcon" onclick="deleteCourse(theCourse,true)" style="cursor: pointer;">
        Delete this course <i class="fas fa-trash fa-1x"></i>
    </div>
  </div>


  <div style="background-color:#DADDF8; padding: .5em 0 0 .5em"> {cardHeader} </div>
    <div class="card-body">
      <div class="card-text" style="color: blue; font-siz">{title}</div>
      <div class="card-text">{summary}</div>

<br>

<strong>Total Cost: </strong> {costJson}
<hr>
<br>
<p> <strong style="color:black;">(This data would appear differently when shown in Course Catalog. This only an internal preview) </strong><p>
<br>
<strong> Course dates (Physical Component in BIPs) &nbsp; [Start &minus; End] :</strong> &nbsp; {startDate} &minus; {endDate}
<hr>
<strong>Enrollment dates &nbsp; [Start &minus; Deadline]:</strong> &nbsp; {enrollStartDate} &minus; {enrollEndDate}
<hr>

<strong>Visible in catalog &nbsp; [Start &minus; End]:</strong> &nbsp; {validStartDate} &minus; {validEndDate}
<hr>

<strong>Min / Max Students</strong> {minNumberStudents} &minus; {maxNumberStudents}
<hr>
	
<div class="accordion" style="padding-top: 3em;" id="courseExtraInfo">

  <div class="accordion-item">
    <h2 class="accordion-header" id="headingOne">
      <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
        data-bs-target="#collapseOne" aria-expanded="false" aria-controls="collapseOne">
        Assessment
      </button>
    </h2>
    <div id="collapseOne" class="accordion-collapse collapse" aria-labelledby="headingOne"
      data-bs-parent="#courseExtraInfo">
      <div class="accordion-body">
        {assessment}
      </div>
    </div>
  </div>

  <div class="accordion-item">
    <h2 class="accordion-header" id="headingTwo">
      <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
        data-bs-target="#collapseTwo" aria-expanded="false" aria-controls="collapseTwo">
        Learning Outcomes
      </button>
    </h2>
    <div id="collapseTwo" class="accordion-collapse collapse" aria-labelledby="headingTwo"
      data-bs-parent="#courseExtraInfo">
      <div class="accordion-body">
        {learningOutcomes}
      </div>
    </div>
  </div>

  <div class="accordion-item">
    <h2 class="accordion-header" id="headingThree">
      <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
        data-bs-target="#collapseThree" aria-expanded="false" aria-controls="collapseThree">
        Admission Requirements
      </button>
    </h2>
    <div id="collapseThree" class="accordion-collapse collapse" aria-labelledby="headingThree"
      data-bs-parent="#courseExtraInfo">
      <div class="accordion-body">
        {admissionRequirements}
      </div>
    </div>
  </div>

  <div class="accordion-item">
    <h2 class="accordion-header" id="headingFour">
      <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
        data-bs-target="#collapseFour" aria-expanded="false" aria-controls="collapseFour">
        Application and Enrollment Procedure, Datailed Costs, Organizing Board
      </button>
    </h2>
    <div id="collapseFour" class="accordion-collapse collapse" aria-labelledby="headingFour"
      data-bs-parent="#courseExtraInfo">
      <div class="accordion-body">
        {enrollment}
        <a href="{enrollLink}">Enroll or more info clicking here</a>
      </div>
    </div>
  </div>

  <div class="accordion-item">
    <h2 class="accordion-header" id="headingFive">
      <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
        data-bs-target="#collapseFive" aria-expanded="false" aria-controls="collapseFive">
        Coordinators
      </button>
    </h2>
    <div id="collapseFive" class="accordion-collapse collapse" aria-labelledby="headingFive"
      data-bs-parent="#courseExtraInfo">
      <div class="accordion-body">
        {coordinators}
      </div>
    </div>
  </div>

<!--
  <div class="accordion-item">
    <h2 class="accordion-header" id="headingSix">
      <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
        data-bs-target="#collapseSix" aria-expanded="false" aria-controls="collapseSix">
        Target Universities
      </button>
    </h2>
    <div id="collapseSix" class="accordion-collapse collapse" aria-labelledby="headingSix"
      data-bs-parent="#courseExtraInfo">
      <div class="accordion-body">
        {targetUniversities}
      </div>
    </div>
  </div>

-->




<!-- Phisical component -->

<div class="accordion-item">
  <h2 class="accordion-header" id="headingAdditional">
    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
      data-bs-target="#collapseAdditional" aria-expanded="false" aria-controls="collapseAdditional">
      {firstOfferingTitle}
    </button>
  </h2>
  <div id="collapseAdditional" class="accordion-collapse collapse" aria-labelledby="headingAdditional"
    data-bs-parent="#courseExtraInfo">
    <div class="accordion-body">
      <strong>Dates (Start &minus; End) :</strong> {startDate} &minus; {endDate}
      <hr>
      <strong>Resources, detailed activities, calendar, etc.</strong>
      <br>
      <br>
      <div>{offeringDescription}</div>
      <hr>
    </div>
  </div>
</div>

<div class="accordion-item">
  <h2 class="accordion-header" id="headingAddress">
    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
      data-bs-target="#collapseAddress" aria-expanded="false" aria-controls="collapseAddress">
      Addresses
    </button>
  </h2>
  <div id="collapseAddress" class="accordion-collapse collapse" aria-labelledby="headingAddress"
    data-bs-parent="#courseExtraInfo">
    <div class="accordion-body">
      {addresses}
    </div>
  </div>
</div>

<!--  End Phisical component -->





<!--  Virtual component -->

  <div class="accordion-item" style="display : {accBipVirtDisplay}" >
    <h2 class="accordion-header" id="headingVirtual">
      <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
        data-bs-target="#collapseheadingVirtual" aria-expanded="false" aria-controls="headingVirtual">
        Virtual Component
      </button>
    </h2>
    <div id="collapseheadingVirtual" class="accordion-collapse collapse" aria-labelledby="headingVirtual"
      data-bs-parent="#courseExtraInfo">
      <div class="accordion-body">
         <strong>Dates for Virtual Component (Start &minus; End) :</strong> {virtStartDate} &minus; {virtEndDate}
         <hr>
         <p><strong>Virtual Component Description (activities, tools, etc.) :</strong></p>
         <br>
        {virtualdescription}
      </div>
    </div>
  </div>

<!--  End Virtual component -->

  <div class="accordion-item" >
    <h2 class="accordion-header" id="headingExtra">
      <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
        data-bs-target="#collapseheadingExtra" aria-expanded="false" aria-controls="headingExtra">
        Extra Info
      </button>
    </h2>
    <div id="collapseheadingExtra" class="accordion-collapse collapse" aria-labelledby="headingExtra"
      data-bs-parent="#courseExtraInfo">
      <div class="accordion-body">
        For more information about this course visit this <a href="{infoLink}"><strong>page</strong></a>
      </div>
    </div>
  </div>

 </div>

  <div class="card-footer" style="background-color:{footcolor};color:white; font-weight:600">{level}</div>
  </div>
</div>
`;

