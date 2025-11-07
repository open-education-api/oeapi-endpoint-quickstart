package oeapi.testingweb;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class OrganizationTest {

    Logger logger = LoggerFactory.getLogger(OrganizationTest.class);

    String restResource = "organizations";
    String entity = "organization";
    String templateAbrev = "ORG";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void CreateOrganization() throws IOException {

        logStep("Create Organization");

        TestUtilCUDRest.whenPost_test(restResource, entity, templateAbrev, webTestClient);
    }

    @Test
    void FilterOrganizationPrimaryCode() throws IOException {

        logStep("Filter Organization by PrimaryCode");

        String randomCode = TestUtilCUDRest.whenPost_testCode(restResource, entity, templateAbrev, webTestClient);
        TestUtilGetRest.get_primaryCode(restResource, randomCode, webTestClient);

    }

    @Test
    void FilterOrganization() throws IOException {

        logStep("Filter Organization");

        String organizationTypeRandom = TestUtil.genRandomValue("organizationType");
        logger.info("Organization Type [" + organizationTypeRandom + "] filter...");
        String randomCode = TestUtilCUDRest.whenPost_testCode(restResource, entity, templateAbrev, webTestClient);

        logger.info("Organization [" + randomCode + "] filter...");
        // Testing other filters
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("organizationType", organizationTypeRandom);
        TestUtilGetRest.get_filter(restResource, filterMap, webTestClient);

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
