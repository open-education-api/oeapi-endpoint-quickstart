package oeapi.testingweb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.AfterAll;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProgramTest {

    Logger logger = LoggerFactory.getLogger(ProgramTest.class);
    String restResource = "programs";
    String templateAbrev = "PROG";
    String entity = "program";

    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private TestUtil TU;
    
    @Autowired    
    private TestUtilCUDRest TUCudRest;    
    
    @Autowired    
    private TestUtilGetRest TUGetRest;      

    @Test
    void CreateProgram() throws IOException {

        logStep("Create Program"); 
        
        TUCudRest.whenPost_test(restResource, entity, templateAbrev, webTestClient);

    }

    @Test
    void UpdateProgram() throws IOException {

        logStep("Create Program");

        String id = UUID.randomUUID().toString();
        TUCudRest.whenPost_test(restResource, entity, templateAbrev, id , webTestClient);

        logStep("Update Program");
        TUCudRest.whenPut_testUpdateCode(restResource, entity, templateAbrev, id, webTestClient);
    }

    @Test
    void DeleteProgram() throws IOException {

        logStep("Create Program");

        String id = UUID.randomUUID().toString();
        TUCudRest.whenPost_test(restResource, entity, templateAbrev, id , webTestClient);

        logStep("Delete Program");
        TUCudRest.delete_test(restResource, id, webTestClient);
    }

    @Test
    void FilterProgram() throws IOException {

        logStep("Filter Program"); 
        
        String randomCode = TU.genRandomCode();
        String levelRandom = TU.genRandomValue("levelType");
        String programTypeRandom = TU.genRandomValue("programType");
        String sectorRandom = TU.genRandomValue("sectorType");
        String qualificationAwardedRandom = TU.genRandomValue("qualificationAwardedType");
        String levelOfQualificationRandom = TU.genRandomValue("levelOfQualificationType");
        String teachingLanguage = "spa";

        logStep("Program [" + randomCode + "] load...");

        String programPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/" + entity + "_filter_template.json")),StandardCharsets.UTF_8);

        String payload = programPayload.replace("--" + templateAbrev + "_CODE_TOBEINFORMED--", randomCode)
                .replace("--" + templateAbrev + "_level--", levelRandom)
                .replace("--" + templateAbrev + "_programType--", programTypeRandom)
                .replace("--" + templateAbrev + "_sector--", sectorRandom)
                .replace("--" + templateAbrev + "_teachingLanguage--", teachingLanguage)
                .replace("--" + templateAbrev + "_levelOfQualification--", levelOfQualificationRandom)
                .replace("--" + templateAbrev + "_qualificationAwarded--", qualificationAwardedRandom);

        TUCudRest.post_testCode(restResource, payload, randomCode, webTestClient);

        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("programType", programTypeRandom);
        filterMap.put("teachingLanguage", teachingLanguage);
        filterMap.put("sector", sectorRandom);
        filterMap.put("qualificationAwarded", qualificationAwardedRandom);
        filterMap.put("levelOfQualification", levelOfQualificationRandom);

        TUGetRest.get_filter("programs", filterMap, webTestClient);

    }

    @Test
    void FilterProgramCode() throws IOException {

        logStep("Filter Program Code");

        String randomCode = TU.genRandomCode();
        String levelRandom = TU.genRandomValue("levelType");
        String programTypeRandom = TU.genRandomValue("programType");
        String sectorRandom = TU.genRandomValue("sectorType");
        String qualificationAwardedRandom = TU.genRandomValue("qualificationAwardedType");
        String levelOfQualificationRandom = TU.genRandomValue("levelOfQualificationType");
        String teachingLanguage = "spa";

        logStep("Program [" + randomCode + "] load...");

        String programPayload = new String(
                Files.readAllBytes(Paths.get("src/test/resources/" + entity + "_filter_template.json")),
                StandardCharsets.UTF_8);

        String payload = programPayload.replace("--" + templateAbrev + "_CODE_TOBEINFORMED--", randomCode)
                .replace("--" + templateAbrev + "_level--", levelRandom)
                .replace("--" + templateAbrev + "_programType--", programTypeRandom)
                .replace("--" + templateAbrev + "_sector--", sectorRandom)
                .replace("--" + templateAbrev + "_teachingLanguage--", teachingLanguage)
                .replace("--" + templateAbrev + "_levelOfQualification--", levelOfQualificationRandom)
                .replace("--" + templateAbrev + "_qualificationAwarded--", qualificationAwardedRandom);

        TUCudRest.post_testCode(restResource, payload, randomCode, webTestClient);
        TUGetRest.get_primaryCode(restResource, randomCode, webTestClient);
    }

    @Test
    void CreateParentChildAndRepostParent() throws Exception {
        logStep("CreateParentChildAndRepostParent...");

        // POST parent
        String parentId = "11111111-1111-4111-8111-111111111111";
        String parentJson = """
            {
                "programId": "%s",
                "primaryCode": { "codeType": "identifier", "code": "parent-program" },
                "programType": "minor",
                "name": [{"language": "en-GB", "value": "parent"}],
                "abbreviation": "parent",
                "description": [{"language": "en-GB", "value": "parent"}],
                "teachingLanguage": "eng",
                "level": "master"
            }
        """.formatted(parentId);
        TUCudRest.post_testId("programs", "program", parentJson, parentId, webTestClient);

        // POST child (with parentId)
        String childId = "22222222-2222-4222-8222-222222222222";
        String childJson = """
            {
                "programId": "%s",
                "primaryCode": { "codeType": "identifier", "code": "child-program" },
                "programType": "minor",
                "name": [{"language": "en-GB", "value": "child"}],
                "abbreviation": "child",
                "description": [{"language": "en-GB", "value": "child"}],
                "teachingLanguage": "eng",
                "level": "master",
                "parent": "%s"
            }
        """.formatted(childId, parentId);
        TUCudRest.post_testId("programs", "program", childJson, childId, webTestClient);

        // GET child (should have parentId)
        webTestClient.get()
            .uri("/programs/%s".formatted(childId))
            .header("Authorization", TU.authHeaderForTest())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.parent").exists();

        // GET parent (should have children)
        webTestClient.get()
            .uri("/programs/%s".formatted(parentId))
            .header("Authorization", TU.authHeaderForTest())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.children").exists()
            .jsonPath("$.children[0]").isEqualTo(childId);
    
        // GET parent JSON
        String parentBody = webTestClient.get()
            .uri("/programs/%s".formatted(parentId))
            .header("Authorization", TU.authHeaderForTest())
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();

        // PUT parent JSON
        webTestClient.put()
            .uri("/programs/%s".formatted(parentId))
            .header("Authorization", TU.authHeaderForTest())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(parentBody)
            .exchange()
            .expectStatus().isOk();

        // GET parent JSON and compare to PUT JSON
        String parentBodyAfterPut = webTestClient.get()
            .uri("/programs/%s".formatted(parentId))
            .header("Authorization", TU.authHeaderForTest())
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();
        assertEquals(parentBody, parentBodyAfterPut, "parent still the same");

        // GET child (see if it still exists)
        webTestClient.get()
            .uri("/programs/%s".formatted(childId))
            .header("Authorization", TU.authHeaderForTest())
            .exchange()
            .expectStatus().isOk();
    }

    private void logStep(String title) {
        logger.info("\n\n" +
            "############################################################\n" +
            "#                                                          #\n" +
            "#  STEP: " + String.format("%-50s", title) + " #\n" +
            "#                                                          #\n" +
            "############################################################\n");
    }      
    

    /**
     * Teardown. Tests in this class used to leave their entities in the database:
     * This runs even when a test fails, and it never asserts - teardown must not turn a
     * passing run red.
     */
    @AfterAll
    void cleanUpCreatedData() {
        TUCudRest.cleanupCreated(webTestClient);
    }

}
