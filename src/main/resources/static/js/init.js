
// get url params...
const queryString = window.location.search;
urlParams = new URLSearchParams(queryString);
let theCourse = (urlParams.get('courseId')) ? urlParams.get('courseId') : "NONE";
let filterLevel = (urlParams.get('level')) ? urlParams.get('level') : "NONE";
let filterUniv = (urlParams.get('univ')) ? urlParams.get('univ') : "NONE";
let courseJSONData = null; // For later use in single course preview


const ooapiDefaultCountry = "EN";
const ooapiDefaultLogo = "./img/OpenEducationApi_Logo.png";
const ooapiDefaultShortUnivName = "localhost";
const ooapiDefaultUnivName = "LOCALHOST";
// See orgs.json, which are loaded at start. If this is not changed
// it will point to the default one loaded at start. 
// Change it to your actual one as soon as you customize your endpoint
const ooapiDefaultOrganizationId = "4f9c7a32-e89b-12d3-a456-7b8e5c9d3a21";

// If not proxied 
let ooapiDefaultEndpointURL = "http://localhost:57075";
// If proxied, your URL 
//const ooapiDefaultEndpointURL = "https://your-site/oeapi...";
const formOOAPIEndpoint = ooapiDefaultEndpointURL;
