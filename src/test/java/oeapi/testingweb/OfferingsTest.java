package oeapi.testingweb;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import oeapi.service.CourseService;
import oeapi.service.OfferingService;
import oeapi.service.OrganizationService;
import oeapi.service.PersonService;
import oeapi.service.ProgramService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OfferingsTest {
    @Autowired
    private OfferingService offeringService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private ProgramService programService;
    @Autowired
    private PersonService personService;
    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;

    Logger logger = LoggerFactory.getLogger(OfferingsTest.class);

    private String organizationId;
    private String programId;
    private String courseId;
    private String personId;

    @BeforeAll
    void beforeAll() throws Exception {
        organizationId = UUID.randomUUID().toString();
        String organizationPayload =
            new String(Files.readAllBytes(Paths.get("src/test/resources/organization_template.json")),
                       StandardCharsets.UTF_8);
        webTestClient.post()
            .uri("/organizations")
            .header("Authorization",TU.authHeaderForTest())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(organizationPayload
                           .replace("--ORG_ID_TOBEINFORMED--", organizationId)
                           .replace("--ORG_CODE_TOBEINFORMED--", TU.genRandomCode()))
            .exchange().expectStatus().isOk();

        personId = UUID.randomUUID().toString();
        String coordinatorPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/person_template.json")),
                                               StandardCharsets.UTF_8);
        webTestClient.post()
            .uri("/persons")
            .header("Authorization",TU.authHeaderForTest())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(coordinatorPayload
                       .replace("--PERSON_ID_TOBEINFORMED--", personId)
                       .replace("--PERSON_CODE_TOBEINFORMED--", TU.genRandomCode()))
            .exchange().expectStatus().isOk();

        programId = UUID.randomUUID().toString();
        String programPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/program_template.json")),
                                           StandardCharsets.UTF_8);
        webTestClient.post()
            .uri("/programs")
            .header("Authorization",TU.authHeaderForTest())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(programPayload
                       .replace("--PROG_ID_TOBEINFORMED--", programId)
                       .replace("--PROG_CODE_TOBEINFORMED--", TU.genRandomCode()))
            .exchange().expectStatus().isOk();

        courseId = UUID.randomUUID().toString();
        String coursePayload = new String(Files.readAllBytes(Paths.get("src/test/resources/fullcourse_template.json")),
                                          StandardCharsets.UTF_8);
        webTestClient.post()
            .uri("/courses")
            .header("Authorization",TU.authHeaderForTest())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(coursePayload.replace("--COURSE_ID_TOBEINFORMED--", courseId)
                       .replace("--COURSE_CODE_TOBEINFORMED--", TU.genRandomCode())
                       .replace("--PROG_ID_TOBEINFORMED--", programId)
                       .replace("--PERSON_ID_TOBEINFORMED--", personId)
                       .replace("--ORG_ID_TOBEINFORMED--", organizationId))
            .exchange().expectStatus().isOk();
    }

    @AfterAll
    void afterAll() {
        offeringService.deleteByCourseId(courseId);
        offeringService.deleteByProgramId(programId);
        courseService.delete(courseId);
        programService.delete(programId);
        personService.delete(personId);
        organizationService.delete(organizationId);
    }

    @Test
    void courseOfferingTest() throws Exception {
        String offeringId = UUID.randomUUID().toString();
        String courseOfferingPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/courseOffering_template.json")),
                StandardCharsets.UTF_8);

        webTestClient.post()
            .uri("/offerings")
            .header("Authorization",TU.authHeaderForTest())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(courseOfferingPayload.replace("--OFFERING_ID_TOBEINFORMED--", offeringId)
                       .replace("--COURSE_ID_TOBEINFORMED--", courseId)
                       .replace("--OFFERING_CODE_TOBEINFORMED--", TU.genRandomCode()))
            .exchange()
            .expectStatus().isOk();

        webTestClient.get()
            .uri("/offerings/{offeringId}", offeringId)
            .header("Authorization",TU.authHeaderForTest())
            .exchange().expectStatus().isOk().expectBody()
            .jsonPath("$.offeringId").isEqualTo(offeringId)
            .jsonPath("$.offeringType").isEqualTo("course")
            .jsonPath("$.course").isEqualTo(courseId)
            .jsonPath("$.program").doesNotExist();
    }

    @Test
    void programOfferingTest() throws Exception {
        String offeringId = UUID.randomUUID().toString();

        try {
            String programOfferingPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/programOffering_template.json")),
                                                       StandardCharsets.UTF_8);

            webTestClient.post()
                .uri("/offerings")
                .header("Authorization",TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(programOfferingPayload.replace("--OFFERING_ID_TOBEINFORMED--", offeringId)
                           .replace("--PROG_ID_TOBEINFORMED--", programId)
                           .replace("--OFFERING_CODE_TOBEINFORMED--", TU.genRandomCode()))
                .exchange().expectStatus().isOk();

            webTestClient.get()
                .uri("/offerings/{offeringId}", offeringId)
                .header("Authorization",TU.authHeaderForTest())
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.offeringId").isEqualTo(offeringId)
                .jsonPath("$.offeringType").isEqualTo("program")
                .jsonPath("$.program").isEqualTo(programId)
                .jsonPath("$.course").doesNotExist();
        } finally {
            offeringService.delete(offeringId);
        }
    }
}
