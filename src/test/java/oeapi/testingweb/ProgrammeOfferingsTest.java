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
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.AfterAll;

/*
 * Integration test for the programmeOffering endpoints, modeled on CourseOfferingsTest:
 *   - POST /offerings/programmeOffering   (create)
 *   - GET  /programmes/{id}/offerings      (list by programme)
 *   - GET  /offerings/{id}               (single fetch / delete verification)
 */
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProgrammeOfferingsTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;

    @Autowired
    private TestUtilCUDRest TUCudRest;

    Logger logger = LoggerFactory.getLogger(ProgrammeOfferingsTest.class);

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
    void fullProgrammeOfferingTest() throws Exception {

        logStep("ProgrammeOffering test: add parent Programme [" + randomProgId + "] ...");

        String programmePayload = new String(Files.readAllBytes(Paths.get("src/test/resources/programme_template.json")), StandardCharsets.UTF_8);

        webTestClient.post()
                .uri("/programmes")
                .header("Authorization", TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(programmePayload.replace("--PROG_ID_TOBEINFORMED--", randomProgId)
                        .replace("--PROG_CODE_TOBEINFORMED--", randomProgCode))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.programmeId").isEqualTo(randomProgId);

        logStep("Adding ProgrammeOffering [" + randomOfferingId + "] to Programme [" + randomProgId + "] ...");

        String offeringPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/programmeOffering_template.json")), StandardCharsets.UTF_8);

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

        logStep("Adding second ProgrammeOffering [" + randomOfferingId2 + "] ...");

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

        logStep("Updating second ProgrammeOffering [" + randomOfferingId2 + "] ...");

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

        logStep("Fetching offerings for Programme [" + randomProgId + "] ...");

        webTestClient.get()
                .uri("/programmes/{programmeId}/offerings", randomProgId)
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
                    ids.forEach(id -> System.out.println("ProgrammeOfferings detected Offering ID: " + id));
                    assertTrue(ids.contains(randomOfferingId),
                            "Expected offeringId " + randomOfferingId + " not found in returned items");
                    assertTrue(ids.contains(randomOfferingId2),
                            "Expected offeringId " + randomOfferingId2 + " not found in returned items");
                })
                // verify the programme reference round-trips (ProgrammeOfferingDTO serializes programme as programmeId)
                .jsonPath("$.items[*].programme").value(programmes -> {
                    @SuppressWarnings("unchecked")
                    List<String> progs = (List<String>) programmes;
                    assertTrue(!progs.isEmpty() && progs.stream().allMatch(randomProgId::equals),
                            "Expected every offering item to reference programme " + randomProgId + ", got " + progs);
                });

        logStep("Cleanup: deleting ProgrammeOfferings ...");

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
    void deleteProgrammeTest() throws Exception {
        logStep("ProgrammeOffering test: delete Programme [" + randomProgId + "] ...");
        TUCudRest.delete_test("programmes", randomProgId, webTestClient);
    }

    private void logStep(String title) {
        logger.info("\n\n"
                + "############################################################\n"
                + "#  STEP: " + String.format("%-50s", title) + " #\n"
                + "############################################################\n");
    }


    /**
     * Teardown. Tests in this class used to leave their entities in the database:
     *
     * This runs even when a test fails, and it never asserts - teardown must not turn a
     * passing run red.
     */
    @AfterAll
    void cleanUpCreatedData() {
        TUCudRest.deleteQuiet("offerings", randomOfferingId, webTestClient);
        TUCudRest.deleteQuiet("offerings", randomOfferingId2, webTestClient);
        TUCudRest.deleteQuiet("programmes", randomProgId, webTestClient);
        TUCudRest.cleanupCreated(webTestClient);
    }

}
