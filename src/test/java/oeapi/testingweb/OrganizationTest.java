package oeapi.testingweb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.AfterAll;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrganizationTest {

    Logger logger = LoggerFactory.getLogger(OrganizationTest.class);

    String restResource = "organizations";
    String entity = "organization";
    String templateAbrev = "ORG";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;
    
    @Autowired    
    private TestUtilCUDRest TUCudRest;    
    
    @Autowired    
    private TestUtilGetRest TUGetRest;      
     
    @Test
    void CreateOrganization() throws IOException {

        logStep("Create Organization");

        TUCudRest.whenPost_test(restResource, entity, templateAbrev, webTestClient);
    }

    @Test
    void FilterOrganizationPrimaryCode() throws IOException {

        logStep("Filter Organization by PrimaryCode");

        String randomCode = TUCudRest.whenPost_testCode(restResource, entity, templateAbrev, webTestClient);
        TUGetRest.get_primaryCode(restResource, randomCode, webTestClient);

    }

    @Test
    void FilterOrganization() throws IOException {

        logStep("Filter Organization");

        String organizationTypeRandom = TU.genRandomValue("organizationType");
        logger.info("Organization Type [" + organizationTypeRandom + "] filter...");
        String randomCode = TUCudRest.whenPost_testCode(restResource, entity, templateAbrev, webTestClient);

        logger.info("Organization [" + randomCode + "] filter...");
        // Testing other filters
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("organizationType", organizationTypeRandom);
        TUGetRest.get_filter(restResource, filterMap, webTestClient);

    }

    @Test
    void CreateParentChildAndRepostParent() throws Exception {
        logStep("CreateParentChildAndRepostParent...");

        // POST parent
        String parentId = "11111111-1111-4111-8111-111111111111";
        String parentJson = """
            {
                "organizationId": "%s",
                "organizationType": "institute",
                "primaryCode": { "codeType": "identifier", "code": "parent-organization" },
                "name": [{"language": "en-GB", "value": "parent"}],
                "shortName": "parent"
            }
        """.formatted(parentId);
        TUCudRest.post_testId("organizations", "organization", parentJson, parentId, webTestClient);

        // POST child (with parentId)
        String childId = "22222222-2222-4222-8222-222222222222";
        String childJson = """
            {
                "organizationId": "%s",
                "organizationType": "institute",
                "primaryCode": { "codeType": "identifier", "code": "child-organization" },
                "name": [{"language": "en-GB", "value": "child"}],
                "shortName": "child",
                "parent": "%s"
            }
        """.formatted(childId, parentId);
        TUCudRest.post_testId("organizations", "organization", childJson, childId, webTestClient);

        // GET child (should have parentId)
        webTestClient.get()
            .uri("/organizations/%s".formatted(childId))
            .header("Authorization", TU.authHeaderForTest())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.parent").exists();

        // GET parent (should have children)
        webTestClient.get()
            .uri("/organizations/%s".formatted(parentId))
            .header("Authorization", TU.authHeaderForTest())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.children").exists()
            .jsonPath("$.children[0]").isEqualTo(childId);

        // GET parent JSON
        String parentBody = webTestClient.get()
            .uri("/organizations/%s".formatted(parentId))
            .header("Authorization", TU.authHeaderForTest())
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();

        // PUT parent JSON
        webTestClient.put()
            .uri("/organizations/%s".formatted(parentId))
            .header("Authorization", TU.authHeaderForTest())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(parentBody)
            .exchange()
            .expectStatus().isOk();

        // GET parent JSON and compare to PUT JSON
        String parentBodyAfterPut = webTestClient.get()
            .uri("/organizations/%s".formatted(parentId))
            .header("Authorization", TU.authHeaderForTest())
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();
        assertEquals(parentBody, parentBodyAfterPut, "parent still the same");

        // GET child (see if it still exists)
        webTestClient.get()
            .uri("/organizations/%s".formatted(childId))
            .header("Authorization", TU.authHeaderForTest())
            .exchange()
            .expectStatus().isOk();
    }

    private void logStep(String title) {
        logger.info("\n\n"
                + "############################################################\n"
                + "#                                                          #\n"
                + "#  STEP: " + String.format("%-50s", title) + " #\n"
                + "#                                                          #\n"
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
        TUCudRest.cleanupCreated(webTestClient);
    }

}
