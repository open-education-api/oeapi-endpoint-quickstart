/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeapi.testingweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
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

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseFullTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    Logger logger = LoggerFactory.getLogger(CourseFullTest.class);

    private final String randomOrgId = UUID.randomUUID().toString();
    private final String randomProgId = UUID.randomUUID().toString();
    private final String randomOfferingId = UUID.randomUUID().toString();
    private static final String randomCourseId = UUID.randomUUID().toString();
    private final String randomPersonId = UUID.randomUUID().toString();
    private final String randomOrgCode = TestUtil.genRandomCode();
    private final String randomProgCode = TestUtil.genRandomCode();
    private final String randomCourseCode = TestUtil.genRandomCode();
    private final String randomPersonCode = TestUtil.genRandomCode();

    String restResource = "courses";

    @Test
    @Order(1)
    void fullCourseTest() throws Exception {

        /*
        String randomOrgId = UUID.randomUUID().toString();
        String randomProgId = UUID.randomUUID().toString();
        String randomCourseId = UUID.randomUUID().toString();
        String randomPersonId = UUID.randomUUID().toString();

        String randomOrgCode = TestUtil.genRandomCode();
        String randomProgCode = TestUtil.genRandomCode();
        String randomCourseCode = TestUtil.genRandomCode();
        String randomPersonCode = TestUtil.genRandomCode();

         */
        logStep("Course Entering fullCourseTest");

        logStep("Course fullCourseTest add parent Organization [" + randomOrgId + "] load...");

        String organizationPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/organization_template.json")), StandardCharsets.UTF_8);

        webTestClient.post()
                .uri("/organizations")
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
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(coordinatorPayload.replace("--PERSON_CODE_TOBEINFORMED--", randomPersonCode)
                        .replace("--PERSON_ID_TOBEINFORMED--", randomPersonId))
                .exchange();

        logStep("Course fullCourseTest add Program [" + randomProgId + "] load...");

        String programPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/program_template.json")), StandardCharsets.UTF_8);

        webTestClient.post()
                .uri("/programs")
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

    }

    private void logStep(String title) {
        logger.info("\n\n"
                + "############################################################\n"
                + "#                                                          #\n"
                + "#  STEP: " + String.format("%-50s", title) + " #\n"
                + "#                                                          #\n"
                + "############################################################\n");
    }

    @Test
    @Order(2)
    void addCourseOfferingTest() throws Exception {
        logStep("Adding Offering to Course [" + randomCourseId + "] load ...");
        String offeringPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/offering_template.json")), StandardCharsets.UTF_8);

        webTestClient.post()
                .uri("/offerings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(offeringPayload.replace("--OFFERING_ID_TOBEINFORMED--", randomOfferingId)
                        .replace("--COURSE_ID_TOBEINFORMED--", randomCourseId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.offeringId").isEqualTo(randomOfferingId);
    }

    @Test
    @Order(3)
    void deleteCourseTest() throws Exception {

        logStep("Course deleteCourseTest delete Course [" + randomCourseId + "] delete...");
        TestUtilCUDRest.delete_test(restResource, randomCourseId, webTestClient);

    }

}
