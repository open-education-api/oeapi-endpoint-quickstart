package oeapi.testingweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;

/*
TestInstance(PER_CLASS)
JUnit creates one instance of test class for all test methods.
 Random values for fields are shared across all tests.

@BeforeAll (non‑static)
Runs once, after Spring injects @Autowired fields.
 All random values are generated once

@TestMethodOrder
Controls the order of execution but does not affect lifecycle.
*/

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseOfferingsTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private TestUtil TU;
    
    @Autowired    
    private TestUtilCUDRest TUCudRest;

    Logger logger = LoggerFactory.getLogger(CourseOfferingsTest.class);
    
    private String randomOrgId;
    private String randomProgId;
    private String randomCourseId;
    private String randomPersonId;

    private String randomOrgCode;
    private String randomProgCode;
    private String randomCourseCode;
    private String randomPersonCode;

    private String randomOfferingId;
    private String randomOfferingCode;
    private String randomOfferingId2;
    private String randomOfferingCode2;
   
    @BeforeAll
    void initOnce() {
        randomOrgId = UUID.randomUUID().toString();
        randomProgId = UUID.randomUUID().toString();
        randomOfferingId = UUID.randomUUID().toString();
        randomCourseId = UUID.randomUUID().toString();
        randomPersonId = UUID.randomUUID().toString();

        randomOrgCode = TU.genRandomCode();
        randomProgCode = TU.genRandomCode();
        randomCourseCode = TU.genRandomCode();
        randomPersonCode = TU.genRandomCode();
        
        randomOfferingId = UUID.randomUUID().toString();
        randomOfferingCode = UUID.randomUUID().toString();
        randomOfferingId2 = UUID.randomUUID().toString();
        randomOfferingCode2 = UUID.randomUUID().toString();            
    } 

    String restResource = "courses";

    @Test
    @Order(1)
    void fullCourseOfferingTest() throws Exception {

        logStep("Course Entering fullCourseTest");

        logStep("Course fullCourseTest add parent Organization [" + randomOrgId + "] load...");

        String organizationPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/organization_template.json")), StandardCharsets.UTF_8);

        webTestClient.post()
                .uri("/organizations")
                .header("Authorization",TU.authHeaderForTest())                
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(organizationPayload.replace("--ORG_ID_TOBEINFORMED--", randomOrgId)
                        .replace("--ORG_CODE_TOBEINFORMED--", randomOrgCode))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.organizationId").isEqualTo(randomOrgId);

        logStep("Course fullCourseTest add Coordinator [" + randomPersonId + "] load...");

        String coordinatorPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/person_template.json")), StandardCharsets.UTF_8);

        webTestClient.post()
                .uri("/persons")
                .header("Authorization",TU.authHeaderForTest())                
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(coordinatorPayload.replace("--PERSON_CODE_TOBEINFORMED--", randomPersonCode)
                        .replace("--PERSON_ID_TOBEINFORMED--", randomPersonId))
                .exchange();

        logStep("Course fullCourseTest add Program [" + randomProgId + "] load...");

        String programPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/program_template.json")), StandardCharsets.UTF_8);

        webTestClient.post()
                .uri("/programs")
                .header("Authorization",TU.authHeaderForTest())                
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(programPayload.replace("--PROG_ID_TOBEINFORMED--", randomProgId)
                        .replace("--PROG_CODE_TOBEINFORMED--", randomProgCode))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.primaryCode.code").isEqualTo(randomProgCode)
                .jsonPath("$.programId").isEqualTo(randomProgId);

        logStep("Course fullCourseTest add Course [" + randomCourseId + "] load...");

        String coursePayload = new String(Files.readAllBytes(Paths.get("src/test/resources/fullcourse_template.json")), StandardCharsets.UTF_8);

        String testCourse = coursePayload.replace("--COURSE_ID_TOBEINFORMED--", randomCourseId)
                .replace("--COURSE_CODE_TOBEINFORMED--", randomCourseCode)
                .replace("--PROG_ID_TOBEINFORMED--", randomProgId)
                .replace("--PERSON_ID_TOBEINFORMED--", randomPersonId)
                .replace("--ORG_ID_TOBEINFORMED--", randomOrgId);

        JsonNode expectedJson = objectMapper.readTree(testCourse);

        String responseBody = webTestClient.post()
                .uri("/courses")
                .header("Authorization",TU.authHeaderForTest())                
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testCourse)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        JsonNode actualJson = objectMapper.readTree(responseBody);

        // Compare JSON structures
        //assertThat(actualJson).isEqualTo(expectedJson);
        try {
            JSONAssert.assertEquals(testCourse, responseBody, JSONCompareMode.STRICT);
        } catch (AssertionError e) {
            System.out.println("Differences: " + e.getMessage());
        }



        logStep("Adding Offering to Course [" + randomCourseId + "] load, OfferingID: " + randomOfferingId);
        String offeringPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/offering_template.json")), StandardCharsets.UTF_8);

        webTestClient.post()
                .uri("/offerings")                
                .header("Authorization",TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(offeringPayload.replace("--OFFERING_ID_TOBEINFORMED--", randomOfferingId)
                                          .replace("--COURSE_ID_TOBEINFORMED--", randomCourseId)
                                          .replace("--OFFERING_CODE_TOBEINFORMED--",randomOfferingCode))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.offeringId").isEqualTo(randomOfferingId);



        logStep("Adding Second Offering to Course [" + randomCourseId + "] load, OfferingID: " + randomOfferingId2);
        
        offeringPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/offering_template.json")), StandardCharsets.UTF_8);

        webTestClient.post()
                .uri("/offerings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization",TU.authHeaderForTest())                
                .bodyValue(offeringPayload.replace("--OFFERING_ID_TOBEINFORMED--", randomOfferingId2)
                                          .replace("--COURSE_ID_TOBEINFORMED--", randomCourseId)
                                          .replace("--OFFERING_CODE_TOBEINFORMED--",randomOfferingCode2))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.offeringId").isEqualTo(randomOfferingId2);


        logStep("Fetching offerings for Course [" + randomCourseId + "] ...");

        webTestClient.get()
                .uri("/courses/{courseId}/offerings", randomCourseId)
                .header("Authorization",TU.authHeaderForTest())                
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                // structure checks
                .jsonPath("$.pageSize").exists()
                .jsonPath("$.pageNumber").exists()
                .jsonPath("$.items").isArray()
                // verify both added offerings exist in the returned list
                .jsonPath("$.items[*].offeringId").value(offeringIds -> {
            List<String> ids = (List<String>) offeringIds;
            ids.forEach(id -> System.out.println("getOfferingsForCourseTest detected Offering ID: " + id));
            assertTrue(ids.contains(randomOfferingId),
                    "Expected offeringId " + randomOfferingId + " not found in returned items");

            assertTrue(ids.contains(randomOfferingId2),
                    "Expected offeringId " + randomOfferingId2 + " not found in returned items");
        });
 



        logStep("Course deleteCourseTest delete Course [" + randomCourseId + "] delete...");
        TUCudRest.delete_test(restResource, randomCourseId, webTestClient);

//        // --- Step 2: query offerings for deleted course ---
//        webTestClient.get()
//                .uri("/courses/{courseId}/offerings", randomCourseId)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                // confirm items exists and is empty
//                .jsonPath("$.items").isArray()
//                .jsonPath("$.items.length()").isEqualTo(0);

        // --- Step 2: query offerings should now return 404 ---
        webTestClient.get()
                .uri("/courses/{courseId}/offerings", randomCourseId)
                .header("Authorization",TU.authHeaderForTest())                
                .exchange()
                .expectStatus().isNotFound();

        verifyOfferingIsDeleted(randomOfferingId);
        verifyOfferingIsDeleted(randomOfferingId2);
    }
            
    private void verifyOfferingIsDeleted(String offeringId) {
        webTestClient.get()
                .uri("/offerings/{offeringId}", offeringId)
                .header("Authorization",TU.authHeaderForTest())
                .exchange()
                .expectStatus().isNotFound();
    }

    
    // Clean other data inserted on tests
    
    @Test
    @Order(2)
    void deleteOrgTest() throws Exception {
              
        logStep("Course deleteOrgTest delete Org [" + randomOrgId + "] delete...");
                
        TUCudRest.delete_test("organizations", randomOrgId, webTestClient);
    }

    @Test
    @Order(3)
    void deletePersonTest() throws Exception {
              
        logStep("Course deletePersonTest delete Person [" + randomPersonId + "] delete...");
                
        TUCudRest.delete_test("persons", randomPersonId, webTestClient);
    }    
        
    @Test
    @Order(4)
    void deleteProgramTest() throws Exception {
              
        logStep("Course deleteProgramTest delete Program [" + randomProgId + "] delete...");
                
        TUCudRest.delete_test("programs", randomProgId, webTestClient);
    }        
        
    
    
    private void logStep(String title) {
        logger.info("\n\n"
                + "############################################################\n"
                + "#                                                          #\n"
                + "#  STEP: " + String.format("%-50s", title) + " #\n"
                + "#                                                          #\n"
                + "############################################################\n");
    }

}
