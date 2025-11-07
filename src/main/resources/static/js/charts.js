
/***************************************** 
 
 Code for Charts
 
 ******************************************/


const getData = async (univOOAPI_URL) => {

    console.log("Destiny URL: " + univOOAPI_URL);
    const response = await fetch(univOOAPI_URL)
        .then((res) => res.json());

    //console.log("Response: "+response);
    return response;
};

upnaCoursesSummary = {
    "university": "UPNA",
    "degrees": 0,
    "masters": 0,
    "microcredentials": 0,
    "Others or ND": 0
};

unizarCoursesSummary = {
    "university": "Unizar",
    "degrees": 0,
    "masters": 0,
    "microcredentials": 0,
    "Others or ND": 0
};

univbtCoursesSummary = {
    "university": "Univbt",
    "degrees": 0,
    "masters": 0,
    "microcredentials": 0,
    "Others or ND": 0
};

LocalhostCoursesSummary = {
    "university": "Localhost",
    "degrees": 0,
    "masters": 0,
    "microcredentials": 0,
    "Others or ND": 0
};

function calculateTypeCourses(courses, univJsonSummary) {

    console.log("Courses to be examined: ",courses);
    
    for (var i in courses.items) {

        course = courses.items[i];
        console.log("Reading course ("+course.courseId+")");

        switch (course.level.toLowerCase()) {
            case "degree":
                univJsonSummary["degrees"]++;
                break;
            case "bachelor":
                univJsonSummary["degrees"]++;
                break;
            case "master":
                univJsonSummary["masters"]++;
                break;
            case "microcredential":
                univJsonSummary["microcredentials"]++;
                break;
            default:
                univJsonSummary["Others or ND"]++;
                console.log("This course (" + course.courseId + ") has no level specified");
        }
    };

    console.log('calculateTypeCourses of ' + univJsonSummary["university"] + " :" +
        ' Degrees: ' + univJsonSummary["degrees"] +
        ' Masters: ' + univJsonSummary["masters"] +
        ' Microcredentials: ' + univJsonSummary["microcredentials"] +
        ' Others or ND: ' + univJsonSummary["Others or ND"]);


};



async function showCharts() {
    const dataLocalhost = await getData(ooapiDefaultEndpointURL+"/courses");    
    calculateTypeCourses(dataLocalhost  , LocalhostCoursesSummary);    
    renderChart();
}


function renderChart() {

    var configLocalhost = {
        type: 'doughnut',
        data: {
            datasets: [{
                data: [
                    LocalhostCoursesSummary["degrees"],
                    LocalhostCoursesSummary["masters"],
                    LocalhostCoursesSummary["microcredentials"],
                    LocalhostCoursesSummary["Others or ND"]
                ],
                backgroundColor: [
                    'rgb(255, 99, 132)',
                    'rgb(54, 162, 235)',
                    'rgb(255, 205, 86)',
                    'rgb(25, 25, 86)'
                ],
                label: 'Localhost EndPoint'
            }],
            labels: [
                "Degree",
                "Master",
                "Microcredential",
                "Others or ND"
            ],

        },
        options: {
            responsive: true,
            title: {
                display: true,
                text: ' Localhost Course Types Summary'
            },
            onClick: (event, elements, chart) => {
                /* 
                            if (elements[0]) {            
                            const i = elements[0]._index;
                            console.log(elements);
                            console.log(configUnizar);
                            alert(configUnizar.data.labels[i] + ': ' + configUnizar.data.datasets[0].data[i]);
                            } */
            }
        }
    };

    univLocalSummChart  = new Chart(document.getElementById("univLocalSummaryChart").getContext("2d"), configLocalhost);

}
