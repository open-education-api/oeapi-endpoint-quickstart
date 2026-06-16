package oeapi.testingweb;

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
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Integration test for the programOffering endpoints, modeled on CourseOfferingsTest:
 *   - POST /offerings/programOffering   (create)
 *   - GET  /programs/{id}/offerings      (list by program)
 *   - GET  /offerings/{id}               (single fetch / delete verification)
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProgramOfferingsTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;

    @Autowired
    private TestUtilCUDRest TUCudRest;

    Logger logger = LoggerFactory.getLogger(ProgramOfferingsTest.class);

    private String randomProgId;
    private String randomProgCode;
    private String randomOfferingId;
    private String randomOfferingCode;
    private String randomOfferingId2;
    private String randomOfferingCode2;
    private String randomOfferingCode3;

    @BeforeAll
    void initOnce() {
        randomProgId = UUID.randomUUID().toString();
        randomProgCode = TU.genRandomCode();
        randomOfferingId = UUID.randomUUID().toString();
        randomOfferingCode = UUID.randomUUID().toString();
        randomOfferingId2 = UUID.randomUUID().toString();
        randomOfferingCode2 = UUID.randomUUID().toString();
        randomOfferingCode3 = UUID.randomUUID().toString();
    }

    @Test
    @Order(1)
    void fullProgramOfferingTest() throws Exception {

        logStep("ProgramOffering test: add parent Program [" + randomProgId + "] ...");

        String programPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/program_template.json")), StandardCharsets.UTF_8);

        webTestClient.post()
                .uri("/programs")
                .header("Authorization", TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(programPayload.replace("--PROG_ID_TOBEINFORMED--", randomProgId)
                        .replace("--PROG_CODE_TOBEINFORMED--", randomProgCode))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.programId").isEqualTo(randomProgId);

        logStep("Adding ProgramOffering [" + randomOfferingId + "] to Program [" + randomProgId + "] ...");

        String offeringPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/programOffering_template.json")), StandardCharsets.UTF_8);

        webTestClient.post()
                .uri("/offerings")
                .header("Authorization", TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(offeringPayload.replace("--OFFERING_ID_TOBEINFORMED--", randomOfferingId)
                        .replace("--PROG_ID_TOBEINFORMED--", randomProgId)
                        .replace("--OFFERING_CODE_TOBEINFORMED--", randomOfferingCode))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.offeringId").isEqualTo(randomOfferingId);

        logStep("Adding second ProgramOffering [" + randomOfferingId2 + "] ...");

        webTestClient.post()
                .uri("/offerings")
                .header("Authorization", TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(offeringPayload.replace("--OFFERING_ID_TOBEINFORMED--", randomOfferingId2)
                        .replace("--PROG_ID_TOBEINFORMED--", randomProgId)
                        .replace("--OFFERING_CODE_TOBEINFORMED--", randomOfferingCode2))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.offeringId").isEqualTo(randomOfferingId2);

        logStep("Updating second ProgramOffering [" + randomOfferingId2 + "] ...");

        webTestClient.put()
                .uri("/offerings")
                .header("Authorization", TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(offeringPayload.replace("--OFFERING_ID_TOBEINFORMED--", randomOfferingId2)
                        .replace("--PROG_ID_TOBEINFORMED--", randomProgId)
                        .replace("--OFFERING_CODE_TOBEINFORMED--", randomOfferingCode3))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.offeringId").isEqualTo(randomOfferingId2)
                .jsonPath("$.primaryCode.code").isEqualTo(randomOfferingCode3);

        logStep("Fetching offerings for Program [" + randomProgId + "] ...");

        webTestClient.get()
                .uri("/programs/{programId}/offerings", randomProgId)
                .header("Authorization", TU.authHeaderForTest())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                // structure checks
                .jsonPath("$.pageSize").exists()
                .jsonPath("$.pageNumber").exists()
                .jsonPath("$.items").isArray()
                .jsonPath("$.items.length()").isEqualTo(2)
                // verify both added offerings are present
                .jsonPath("$.items[*].offeringId").value(offeringIds -> {
                    @SuppressWarnings("unchecked")
                    List<String> ids = (List<String>) offeringIds;
                    ids.forEach(id -> System.out.println("ProgramOfferings detected Offering ID: " + id));
                    assertTrue(ids.contains(randomOfferingId),
                            "Expected offeringId " + randomOfferingId + " not found in returned items");
                    assertTrue(ids.contains(randomOfferingId2),
                            "Expected offeringId " + randomOfferingId2 + " not found in returned items");
                })
                // verify the program reference round-trips (ProgramOfferingDTO serializes program as programId)
                .jsonPath("$.items[*].program").value(programs -> {
                    @SuppressWarnings("unchecked")
                    List<String> progs = (List<String>) programs;
                    assertTrue(!progs.isEmpty() && progs.stream().allMatch(randomProgId::equals),
                            "Expected every offering item to reference program " + randomProgId + ", got " + progs);
                });

        logStep("Cleanup: deleting ProgramOfferings ...");

        webTestClient.delete()
                .uri("/offerings/{offeringId}", randomOfferingId)
                .header("Authorization", TU.authHeaderForTest())
                .exchange()
                .expectStatus().isOk();

        webTestClient.delete()
                .uri("/offerings/{offeringId}", randomOfferingId2)
                .header("Authorization", TU.authHeaderForTest())
                .exchange()
                .expectStatus().isOk();

        verifyOfferingIsDeleted(randomOfferingId);
        verifyOfferingIsDeleted(randomOfferingId2);
    }

    private void verifyOfferingIsDeleted(String offeringId) {
        webTestClient.get()
                .uri("/offerings/{offeringId}", offeringId)
                .header("Authorization", TU.authHeaderForTest())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(2)
    void deleteProgramTest() throws Exception {
        logStep("ProgramOffering test: delete Program [" + randomProgId + "] ...");
        TUCudRest.delete_test("programs", randomProgId, webTestClient);
    }

    private void logStep(String title) {
        logger.info("\n\n"
                + "############################################################\n"
                + "#  STEP: " + String.format("%-50s", title) + " #\n"
                + "############################################################\n");
    }

}
