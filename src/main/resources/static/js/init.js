
// get url params...
const queryString = window.location.search;
urlParams = new URLSearchParams(queryString);
let theCourse = (urlParams.get('courseId')) ? urlParams.get('courseId') : "NONE";
let filterLevel = (urlParams.get('level')) ? urlParams.get('level') : "NONE";
let filterUniv = (urlParams.get('univ')) ? urlParams.get('univ') : "NONE";
let courseJSONData = null; // For later use in single course preview

let ooapiDefaultCountry;
let ooapiDefaultLogo;
let ooapiDefaultShortUnivName;
let ooapiDefaultUnivName;
let ooapiDefaultOrganizationId;
let ooapiDefaultEndpointURL;

function init(config) {
  console.log("Got configuration", config);

  ooapiDefaultCountry = config.ooapiDefaultCountry;
  ooapiDefaultLogo = config.ooapiDefaultLogo;
  ooapiDefaultShortUnivName = config.ooapiDefaultShortUnivName;
  ooapiDefaultUnivName = config.ooapiDefaultUnivName;
  ooapiDefaultOrganizationId = config.ooapiDefaultOrganizationId;
  ooapiDefaultEndpointURL = config.ooapiDefaultEndpointURL || window.location.origin;
}
