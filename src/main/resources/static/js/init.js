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
let ooapiDefaultOrganisationId;
let ooapiDefaultEndpointURL;
let ooapiDefaultCourseConsumers;
let ooapiDefaultOfferingConsumers;

function init(config) {
  console.log("Got configuration", config);

  ooapiDefaultCountry = config.ooapiDefaultCountry;
  ooapiDefaultLogo = config.ooapiDefaultLogo;
  ooapiDefaultShortUnivName = config.ooapiDefaultShortUnivName;
  ooapiDefaultUnivName = config.ooapiDefaultUnivName;
  ooapiDefaultOrganisationId = config.ooapiDefaultOrganisationId;
  ooapiDefaultEndpointURL = config.ooapiDefaultEndpointURL || window.location.origin;
  ooapiDefaultCourseConsumers = config.ooapiDefaultCourseConsumers;
  ooapiDefaultOfferingConsumers = config.ooapiDefaultOfferingConsumers;

  if (!ooapiDefaultOrganisationId) {
    alert('No organisations loaded!');
  }
}

// Peek function for debugging.
function pk(a, b) {
  if (arguments.length == 1) {
    console.debug(a);
    return a;
  } else {
    console.debug(a, b);
    return b;
  }
}
