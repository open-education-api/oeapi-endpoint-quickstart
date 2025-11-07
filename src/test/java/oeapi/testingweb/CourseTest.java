package oeapi.testingweb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CourseTest {

    Logger logger = LoggerFactory.getLogger(CourseTest.class);

    String restResource = "courses";
    String templateAbrev = "COURSE";
    String entity = "course";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testCreateCourse() throws IOException {

        String randomId = UUID.randomUUID().toString();
        String randomCode = TestUtil.genRandomCode();

        logStep("CourseTest Create Course: with ID: "+randomId+" Code: "+randomCode);
     
        logger.info("CourseTest Create Course [" + randomId + "] load...");

        String coursePayload = new String(Files.readAllBytes(Paths.get("src/test/resources/course_template.json")),StandardCharsets.UTF_8);

        String payload = coursePayload.replace("--" + templateAbrev + "_ID_TOBEINFORMED--", randomId)
                                      .replace("--" + templateAbrev + "_CODE_TOBEINFORMED--", randomCode);
        
        TestUtilCUDRest.post_test(restResource, entity, payload, randomId, randomCode, webTestClient);

    }

    @Test
    void testUpdateCourseCode() throws IOException {

        String randomId = TestUtil.genRandomCode();
        String randomCode = TestUtil.genRandomCode();

        logStep("CourseTest testUpdateCourseCode: with ID: "+randomId+" Code: "+randomCode);
        logger.info("CourseTest testUpdateCourseCode: with ID: "+randomId+" Code: "+randomCode);

        String coursePayload = new String(Files.readAllBytes(Paths.get("src/test/resources/updatecourse_template.json")),StandardCharsets.UTF_8);
        String payload = coursePayload.replace("--" + templateAbrev + "_ID_TOBEINFORMED--", randomId)
                                      .replace("--" + templateAbrev + "_CODE_TOBEINFORMED--", randomCode);
        
        TestUtilCUDRest.post_testOk(restResource, payload, webTestClient);
        String newCode = TestUtil.genRandomCode();
        
        coursePayload = new String(Files.readAllBytes(Paths.get("src/test/resources/updatecourse_template.json")),StandardCharsets.UTF_8);
        payload = coursePayload.replace("--" + templateAbrev + "_CODE_TOBEINFORMED--", newCode);

        webTestClient.put()
                .uri("/courses/" + randomId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.primaryCode.code").isEqualTo(newCode)
                .jsonPath("$.courseId").isEqualTo(randomId);

    }

    @Test
    void testFilterCourse() throws IOException {

        String randomCode = TestUtil.genRandomCode();
        String levelRandom = TestUtil.genRandomValue("levelType");
        String teachingLanguage = "spa";

        logStep("CourseTest testFilterCourse: with Code: "+randomCode+" level: "+levelRandom+ " teachingLanguage: "+teachingLanguage);

        logger.info("CourseTest testFilterCourse: with Code: "+randomCode+" level: "+levelRandom+ " teachingLanguage: "+teachingLanguage);

        String coursePayload = new String(Files.readAllBytes(Paths.get("src/test/resources/course_filter_template.json")),StandardCharsets.UTF_8);

        String payload = coursePayload.replace("--" + templateAbrev + "_CODE_TOBEINFORMED--", randomCode)
                                      .replace("--" + templateAbrev + "_level--", levelRandom)
                                      .replace("--" + templateAbrev + "_teachingLanguage--", teachingLanguage);

        TestUtilCUDRest.post_testCode(restResource, payload, randomCode, webTestClient);
        Map<String, String> filterMap = new HashMap<>();
        
        filterMap.put("level", levelRandom);
        filterMap.put("teachingLanguage", teachingLanguage);
        
        TestUtilGetRest.get_filter(restResource, filterMap, webTestClient);

    }

    @Test
    void testFilterCourseByCode() throws IOException {

        String randomCode = TestUtil.genRandomCode();
        String levelRandom = TestUtil.genRandomValue("levelType");
        String teachingLanguage = "spa";

        logger.info("Course [" + randomCode + "] load...");

        String coursePayload = new String(Files.readAllBytes(Paths.get("src/test/resources/course_filter_template.json")),StandardCharsets.UTF_8);

        String payload = coursePayload.replace("--" + templateAbrev + "_CODE_TOBEINFORMED--", randomCode)
                                      .replace("--" + templateAbrev + "_level--", levelRandom)
                                      .replace("--" + templateAbrev + "_teachingLanguage--", teachingLanguage);

        TestUtilCUDRest.post_testCode(restResource, payload, randomCode, webTestClient);
        TestUtilGetRest.get_primaryCode(restResource, randomCode, webTestClient);

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
