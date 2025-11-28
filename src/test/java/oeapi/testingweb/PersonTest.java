package oeapi.testingweb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
class PersonTest {

    Logger logger = LoggerFactory.getLogger(PersonTest.class);
    String restResource = "persons";
    String templateAbrev = "PERSON";
    String entity = "person";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void CreatePerson() throws IOException {

        logStep("Create Person");        
        
        TestUtilCUDRest.whenPost_test(restResource, entity, templateAbrev, webTestClient);

    }

    @Test
    void FilterPerson() throws IOException {
        
        logStep("Filter Person"); 

        String randomCode = TestUtil.genRandomCode();
        String affiliationRandom1 = TestUtil.genRandomValue("personAffiliations");
        String affiliationRandom2 = TestUtil.genRandomValue("personAffiliations");

        logStep("Person [" + randomCode + "] load...");

        String personPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/" + entity + "_filter_template.json")),StandardCharsets.UTF_8);

        String payload = personPayload.replace("--" + templateAbrev + "_CODE_TOBEINFORMED--", randomCode)
                .replace("--" + templateAbrev + "_affiliation1--", affiliationRandom1)
                .replace("--" + templateAbrev + "_affiliation2--", affiliationRandom2);

        TestUtilCUDRest.post_testCode(restResource, payload, randomCode, webTestClient);

        Map<String, String> filterMap = new HashMap<>();

        TestUtilGetRest.get_filter(restResource, filterMap, webTestClient);

    }

    @Test
    void FilterPersonCode() throws IOException {

        logStep("Filter Person Code"); 

        String randomCode = TestUtilCUDRest.whenPost_testCode(restResource, entity, templateAbrev, webTestClient);
        TestUtilGetRest.get_primaryCode(restResource, randomCode, webTestClient);

    }
    
    
    private void logStep(String title) {
        logger.info("\n\n" +
            "############################################################\n" +
            "#                                                          #\n" +
            "#  STEP: " + String.format("%-50s", title) + " #\n" +
            "#                                                          #\n" +
            "############################################################\n");
    }        
}
