
const languages = [
    {code: "en-GB", iso639_2: "eng", name: "English (UK)"},
    {code: "es-ES", iso639_2: "spa", name: "Spanish"},
    {code: "fr-FR", iso639_2: "fra", name: "French"},
    {code: "pt-PT", iso639_2: "por", name: "Portuguese"},
    {code: "it-IT", iso639_2: "ita", name: "Italian"},
    {code: "ro-RO", iso639_2: "ron", name: "Romanian"},
    {code: "de-DE", iso639_2: "ger", name: "German"},
    {code: "nl-NL", iso639_2: "nld", name: "Dutch"},
    {code: "el-GR", iso639_2: "ell", name: "Greek"},
    {code: "ca-ES", iso639_2: "cat", name: "Catalan"},
    {code: "eu_ES", iso639_2: "eus", name: "Basque"}
];


/* Org codes */ 
/* if not changed the default org will be Organization for Testing defined in orgs.json */

const univPartners = [
    {code: "4f9c7a32-e89b-12d3-a456-7b8e5c9d3a21", name: "Organization for Testing"}
    /*,
    {code: "11111111-e89b-12d3-a456-123514174eee", name: "Universidad Pública de Navarra (UPNA)"},
    {code: "22222222-e89b-12d3-a456-123514174eee", name: "Université Savoie Mont Blanc (USMB)"},
    {code: "33333333-e89b-12d3-a456-123514174eee", name: "Hayte Ecole Spécialisée de Suisse occidentale (HES_SO)"},
    {code: "44444444-e89b-12d3-a456-123514174eee", name: "Université de Paue et des Pays de L'Adour (UPPA)"},
    {code: "55555555-e89b-12d3-a456-123514174eee", name: "Poli Técnico Guarda (IPG)"},
    {code: "77777777-e89b-12d3-a456-123514174eee", name: "Universidad de Zaragoza (UNIZAR)"}
    */
];

// Function to find university name by code
function getUniversityNameByCode(code) {
  const found = univPartners.find(u => u.code === code);
  return found ? found.name : 'TBD';
}

// Function to find language name using ISO639_2
function getLanguageNameByCodeISO639_2(code) {
  const found = languages.find(u => u.iso639_2 === code);
  return found ? found.name : 'TBD';
}


/* Only ooapiDefaultEndpointURL is accessed, but here you
   could add code to merge data from other endpoints */

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

/* Rendering of courses
   Some of the html rendering of the courses is based 
   on the code https://codepen.io/feri-irawan/pen/RwLQKpY 
   (By Feri Irawan - 29/12/2021)
*/   


/* Full list of courses */


const getAllCoursesData = async (univOOAPI_URL) => {
    console.log("getAllCoursesData-> Destiny URL: " + univOOAPI_URL + "/courses");
    const response = await fetch(univOOAPI_URL + "/courses")
            .then((res) => res.json())
            .then(({ items }) => items);
    //console.log("getAllCoursesData-> Response: " + response);
    return response;
};
async function AllMergedCoursesRawData() {

    const data = await getAndMergeAllCoursesData();
    return data;
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


async function renderAllCourses() {
    const data = await getAllCoursesData(univOOAPI_URL);
    console.log(data);
    console.log("Filter by level: " + filterLevel);
    let results = [];
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
            results = data.filter(({ name }) => name[0].value.toLowerCase().includes(q.toLowerCase()));
        }

        renderAllCoursesResults(results, q);
    };
}

function renderAllCoursesResults(results, q = "") {

    console.log("Using OEAPI endpoint: " + ooapiDefaultEndpointURL);
    console.log("Results: " , results);

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
                        .replace(/{summary}/gi, "<p><strong>Mode of delivery:</strong> " + course.modeOfDelivery + "</p><p style='float:right;font-size: x-small;'><strong>Course Code:</strong> &nbsp; " + course.primaryCode.code + "<strong><br>Id: </strong>"+ course.courseId +"</p>")
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


async function loadFullCourseData(univShortName, courseID) {


    try {
        let {courseJSON, resultCoordinatorsList, resultProgramsList} = await loadAsyncCourseData(ooapiDefaultEndpointURL, courseID);

        courseJSONData = courseJSON;  // To have visibility of during HTML some async rendering

        console.log("Async call results:", courseJSON, resultCoordinatorsList, resultProgramsList);

        const oneCourseElement =
                oneCourseCard.replace(/{imgSrc}/gi, htmltizeLogo(courseJSON))
                .replace(/{cardHeader}/gi, "<strong>Level: </strong>" + courseJSON.level +
                        "&nbsp; " + htmltizeStudyLoad(courseJSON) +
                        "&nbsp; <strong>Mode of Delivery: </strong>" + courseJSON.modeOfDelivery)
                .replace(/{title}/gi,"<span style='font-size:1.25em; font-weight:bold'>"+htmltizeMultiLingualText(courseJSON,"name")+"</span>")
                .replace(/{assessment}/gi, htmltizeMultiLingualText(courseJSON,"assessment"))
                .replace(/{enrollment}/gi, htmltizeMultiLingualText(courseJSON,"enrollment"))
                .replace(/{targetUniversities}/gi, htmltizeTargetUniversities(courseJSON))
                .replace(/{summary}/gi, "<p>" + htmltizeMultiLingualText(courseJSON,"description") + "</p><p style='float:right;font-size: small;'><strong>Course Code:</strong> &nbsp; " + courseJSON.primaryCode.code + "<strong><br>Id: </strong>"+ courseJSON.courseId +"</p>")
                .replace(/{footcolor}/gi, cardFooterColor(courseJSON.level))
                .replace(/{level}/gi, "<strong>Level: </strong>" + (courseJSON.level.toLowerCase() == 'bachelor' ? 'degree' : courseJSON.level))
                .replace(/{admissionRequirements}/gi, htmltizeMultiLingualText(courseJSON,"admissionRequirements"))
                .replace(/{learningOutcomes}/gi, htmltizeMultiLingualText(courseJSON,"learningOutcomes"))
                .replace(/{coordinators}/gi, (courseJSON.coordinators ? htmltizeCoordinators(resultCoordinatorsList) : "Not defined"));

        resultsContainer.innerHTML = oneCourseElement;
        // Enable or disable admin options, like add, delete, etc. if logged or not
        manageAdminItems();

    } catch (error) {
        resultsContainer.innerHTML = "<h3>Oops, something went wrong reading course data</h3>";
        console.error("Something went wrong reading course data:", error);
    }
}

function deleteCourse()
{

    console.log("deleteCourse: endpointURL..." + ooapiDefaultEndpointURL);
    console.log("deleteCourse: the Course..." + theCourse);
    console.log("deleteCourse token",localStorage.getItem('jwt'));
   
    if (confirm('Are you sure you want to delete this course?')) {
        fetch(ooapiDefaultEndpointURL + "/courses/" + theCourse, {method: "DELETE",
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('jwt'),
                'Content-length': 0
            }
        })
                .then(async response => {
                    if (!response.ok) {
                        throw new Error('deleteCourse Failed to delete resource');
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
    ;
}
async function loadAsyncCourseData(ooapiEndPoint, courseID) {

    try {
        console.log("loadCourseData: Get course main data...");
        const courseData = await fetch(ooapiEndPoint + "/courses/" + courseID);
        const courseJSON = await courseData.json();
        // Get other values/keys
        const coordinators = courseJSON.coordinators || [];
        const programs = courseJSON.programs || [];
        // Prepare requests
        const getCoordinatorsArray = coordinators.map(c =>
            fetch(ooapiEndPoint + "/persons/" + encodeURIComponent(c))
        );
        const getProgramsArray = programs.map(p =>
            fetch(ooapiEndPoint + "/programs/" + encodeURIComponent(p))
        );
        // Await all fetch calls
        const [coordinatorResponses, programResponses] = await Promise.all([
            Promise.all(getCoordinatorsArray),
            Promise.all(getProgramsArray)
        ]);
        const resultCoordinatorsList = await Promise.all(
                coordinatorResponses.map(res => res.json())
                );
        const resultProgramsList = await Promise.all(
                programResponses.map(res => res.json())
                );
        console.log('Course:', courseJSON);
        console.log('Coordinators:', resultCoordinatorsList);
        console.log('Programs:', resultProgramsList);
        return {courseJSON, resultCoordinatorsList, resultProgramsList};
        
    } catch (error) {
        console.error('Error during API call sequence:', error);
        throw error;
    }
}

async function loadAsyncFirstOfferingData(ooapiEndPoint, courseID) {

    try {
        console.log("loadOfferingData: Get offering data...");
        const response = await fetch(ooapiEndPoint + "/courses/" + courseID + "/offerings");
		
        if (!response.ok) {
            throw new Error('loadOfferingData HTTP error! Status: ${response.status}');
            return ;
        } 	
		
        const allOfferingData = await response.json();

        console.log('All OfferingData:', allOfferingData);
        firstOfferingData = allOfferingData.items[0];
        console.log('firstOfferingData:', firstOfferingData);
        return {firstOfferingData};
        
    } catch (error) {
        console.error('Error during API call sequence on loadOfferingData :', error);
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
	  {	console.log("canDoUpdates? "+"Yes, JWT is disabled. Be sure this is what you want. See doc.");
        updatesArePossible = true ;	
	  }	    
	 else
	  { if (!isJwtValid())
	     { console.log("canDoUpdates? "+"No, JWT is enabled and no valid token found");
          updatesArePossible = false ;			  
	     }	
        else
		 { console.log("canDoUpdates? "+"Yes, JWT is enabled and valid token found");
          updatesArePossible = true ;			  
	     }	
	  } 
	  
   return  updatesArePossible;
 
 }		 

function isJwtValid() {
	
    const token = localStorage.getItem("jwt");
	
	console.log("Token is: "+token);
	
    if (!token) {
        return false; 
    }

    try {
        const payloadBase64 = token.split('.')[1];
        const payloadJson = atob(payloadBase64);
        const payload = JSON.parse(payloadJson);

        // exp is in seconds since epoch
        const now = Math.floor(Date.now() / 1000);
		console.log("jwt expires in: ", new Date(payload.exp*1000));
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
              return status+": no valid response";
        }
        
        const statusText = await response.text();
        console.log("Security Status:", statusText);

        const enabled = statusText.toLowerCase().includes("enabled");

        if (enabled) 
		  {	status = "Enabled, you must login before updates";    }
 		  else { status = " Disabled. (Updates are allowed without login. Check that's what you want. See doc.) ";     }

       } catch (error) {
           console.error("Error checking JWT security:", err);
           status = "Unknown, error checking JWT security "; 
        }     
	
	return status; 
}

async function manageAdminItems()
{
	let JTWSecurityEnabled = await isJwtSecurityEnabled();
	
	if (!JTWSecurityEnabled ) {
		 console.log("manageAdminItems: No JWT activated, updates are allowed");
	     document.getElementById("avatar").style = "display: none;";
		 // addCourse is not ever present 
		 if (document.getElementById("addCourse")) { document.getElementById("addCourse").style = "cursor: pointer;"; }
		 // Delete is only in single course view, 
		 if (document.getElementById("deleteIcon")) { document.getElementById("deleteIcon").style = "display:block;"; }				 				 
		 document.getElementById("loginLink").style = "display : none";
		 document.getElementById("administration").style = "display : none";
	  }
      else
  		if (isJwtValid() ) {
		 console.log("manageAdminItems: JWT activated and valid JWT token, can admin");	
	     document.getElementById("avatar").style = "cursor: pointer;";
		 // addCourse is not ever present 
		 if (document.getElementById("addCourse")) { document.getElementById("addCourse").style = "cursor: pointer;"; }		 
		 // Delete is only in single course view, 
		 if (document.getElementById("deleteIcon")) { document.getElementById("deleteIcon").style = "display:block;"; }				 				 
		 document.getElementById("loginLink").style = "display : none";
		 if (isAdmin()) {
			document.getElementById("administration").style = "cursor: pointer;";
		   }
	     } 
         else		 
		  {
		   console.log("manageAdminItems: No valid JWT token, must login");				  
           document.getElementById("avatar").style = "display: none;";
 		   // addCourse is not ever present 
    	   if (document.getElementById("addCourse")) { document.getElementById("addCourse").style = "display : none"; }		   
		   // Delete is only in single course view, 
 		   if (document.getElementById("deleteIcon")) { document.getElementById("deleteIcon").style = "display:block;"; }				 				 
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

  <!-- Delete Icon (right) -->
  <div class="delete-icon" id = "deleteIcon" onclick="deleteCourse()" style="cursor: pointer;"  >
    Delete course <i class="fas fa-trash 4x"></i>
  </div>


  <div style="background-color:#DADDF8; padding: .5em 0 0 .5em"> {cardHeader} </div>
    <div class="card-body">
      <div class="card-text" style="color: blue; font-siz">{title}</div>
      <div class="card-text">{summary}</div>
	
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
        Enrollment Info
      </button>
    </h2>
    <div id="collapseFour" class="accordion-collapse collapse" aria-labelledby="headingFour"
      data-bs-parent="#courseExtraInfo">
      <div class="accordion-body">
        {enrollment}
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

  <div class="accordion-item">
    <h2 class="accordion-header" id="headingSeven">
      <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
        data-bs-target="#collapseSeven" aria-expanded="false" aria-controls="collapseSeven">
        Extra Info
      </button>
    </h2>
    <div id="collapseSeven" class="accordion-collapse collapse" aria-labelledby="headingSeven"
      data-bs-parent="#courseExtraInfo">
      <div class="accordion-body">
        <ul><li>For more information, visit the university’s page at … </li></ul>
      </div>
    </div>
  </div>

 </div>

  <div class="card-footer" style="background-color:{footcolor};color:white; font-weight:600">{level}</div>
  </div>
</div>
`;

