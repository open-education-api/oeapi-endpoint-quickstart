// Functions for styling. Mostly from JSON to HTML

// Useful to detect if structure is an array of arrays like learningOutcomes
function isArrayOfArrays(data) {
  return Array.isArray(data) && data.length > 0 && Array.isArray(data[0]);
}

/* Updates text of item on selected language */
function changeTextLanguage(ItemJson, idSelectLang, idTextField, Item) {

    // item is not neccesary, just in case of debugging
	
	let resultText = " ( Not specified. )";
	
	let selectedLanguage = document.getElementById(idSelectLang).value;
	if (!selectedLanguage || selectedLanguage == "undefined")
	 { selectedLanguage = "en-GB"; }
	
	if (isArrayOfArrays(ItemJson))
	 { const filteredByLang = ItemJson.flat().filter(item => item.language === selectedLanguage); 
       resultText = "<ul>";
	   filteredByLang.forEach(item => {
		 resultText = resultText + "<li>" + item.value + "</li>";
	    });
       resultText = resultText + "</ul>";
     }
     else	 
	 {	
		selectedText = ItemJson.find(text => text.language === selectedLanguage);
		if (selectedText)	
		 {  resultText = selectedText.value; }  
     }
   
   document.getElementById(idTextField).innerHTML = resultText;   
}

/* Fills language selector with available languages */
function populateLanguageSelector(ItemJson, idSelectLang) {

    let selector = document.getElementById(idSelectLang);
	
	if (isArrayOfArrays(ItemJson))
	 { ItemJson = ItemJson.flat(); }	// Convert array of arrays to array of items

    ItemJson.forEach((desc, index) => {
        if (selector.offsetParent === null) {
            selector.style.display = 'block';
        }   // While no data, should start as display:none
        const option = document.createElement("option");
        option.value = desc.language;
        option.textContent = desc.language;
        selector.appendChild(option);
    });
}


function fillValues(courseJson, ItemSelId, ItemTextId, Item)
{
    if (courseJson && courseJson.length > 0) {
        populateLanguageSelector(courseJson, ItemSelId);
        changeTextLanguage(courseJson, ItemSelId, ItemTextId, Item);
    } else {
        document.getElementById(ItemTextId).innerText = " ( Not specified. )";
    }
}

function htmltizeMultiLingualText(courseJson, item) {

    console.log("MTXT with item: "+ item);
	console.log(courseJson)
	console.log("Value: ",courseJson[item]);		

    let htmlRes = " ( Not specified. )";

    let ItemTextId = 'text' + Math.floor(Math.random() * 10000);
    let ItemSelId = 'selector' + Math.floor(Math.random() * 10000);
    let ItemText = '<div id=' + ItemTextId + '></div>';

    ItemSelLang = '<select id="' + ItemSelId + '" style="background-color: #e1f1ff; display:none; margin-left: 90%; " onchange="changeTextLanguage(courseJSONData[\'' + item + '\'],this.id,document.getElementById(\'' + ItemTextId + '\').id)"></select><br>';

    htmlRes = '<div>' + ItemSelLang + ItemText + '</div>';

    //setTimeout(fillValues(courseJson,ItemSelId,ItemTextId),500);  Beware, this does not work

    setTimeout(function () {
        fillValues(courseJson[item], ItemSelId, ItemTextId, item)
    }, 1000);   // Let DOM be constructed first

    return htmlRes;
}

function htmltizeCoordinatorsList(coordinatorsJson) {}

function htmltizeStudyLoad(courseJson) {
    htmlRes = "<p><strong>Study Load :</strong> ND";

    if (courseJson.studyLoad) {
        htmlRes = "<p><strong>Study Load: </strong>" + courseJson.studyLoad.value + " (" + courseJson.studyLoad.studyLoadUnit + ") ";
    }

    return htmlRes;
}

function htmltizeCoordinators(CoordinatorsList) {

    console.log("htmltizeCoordinators with :", CoordinatorsList);

    let htlmRes = " ( Not specified. )";

    if (CoordinatorsList) {
        htmlRes = '<div style="display:flex;"><ul> '
        CoordinatorsList.forEach(c => {
            ;
            //htmlRes = htmlRes + '<li>'  + c.displayName + '</li>';
            //<i class="fas fa-envelope">
            htmlRes += '<li>' + c.givenName + ' ' + c.surname + ' <a href="mailto:' + c.mail + '"><i class="fas fa-envelope"></i></a></li>';
        });
        htmlRes = htmlRes + '</ul>';
    }
    return htmlRes;

}


function htmltizeTargetUniversities(courseJson) {
    htmlRes = " ( Not specified. )";
    if (courseJson.consumers && courseJson.consumers[0]) {
        elem = courseJson.consumers[0].alliances[0].jointPartnerCodes
        if (elem) {
            htmlRes = '<div style="display:flex;"> '
            elem.forEach(univ => {
                img = htmltizeLogo(univ);
                htmlRes = htmlRes + '<p><img src=' + img + ' style="width:100px; height:20px;" alt="' + univ + '"></p>';
            });
            htmlRes = htmlRes + '</div>';
        }
    }

    return htmlRes;
}
/* In git this is default */
function htmltizeLogo(courseJson) {
    return ooapiDefaultLogo;
}


function cardFooterColor(levelType) {
    switch (levelType.toLowerCase()) {
        case "degree":
            return "#07b807"; // green
            break;
        case "bachelor":
            return "#07b807";
            break;
        case "master":
            return "#be5a1c";
            break;
        case "microcredential":
            return "blue";
            break;
        case "bip":
            return "#6b1b85";
            break;
        default:
            return "#ff8fb1";
    }
}
