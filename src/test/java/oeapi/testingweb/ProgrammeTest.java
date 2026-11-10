package oeapi.testingweb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.AfterAll;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProgrammeTest {

    Logger logger = LoggerFactory.getLogger(ProgrammeTest.class);
    String restResource = "programmes";
    String templateAbrev = "PROG";
    String entity = "programme";

    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private TestUtil TU;
    
    @Autowired    
    private TestUtilCUDRest TUCudRest;    
    
    @Autowired    
    private TestUtilGetRest TUGetRest;      

    @Test
    void CreateProgramme() throws IOException {

        logStep("Create Programme"); 
        
        TUCudRest.whenPost_test(restResource, entity, templateAbrev, webTestClient);

    }

    @Test
    void UpdateProgramme() throws IOException {

        logStep("Create Programme");

        String id = UUID.randomUUID().toString();
        TUCudRest.whenPost_test(restResource, entity, templateAbrev, id , webTestClient);

        logStep("Update Programme");
        TUCudRest.whenPut_testUpdateCode(restResource, entity, templateAbrev, id, webTestClient);
    }

    @Test
    void DeleteProgramme() throws IOException {

        logStep("Create Programme");

        String id = UUID.randomUUID().toString();
        TUCudRest.whenPost_test(restResource, entity, templateAbrev, id , webTestClient);

        logStep("Delete Programme");
        TUCudRest.delete_test(restResource, id, webTestClient);
    }

    @Test
    void FilterProgramme() throws IOException {

        logStep("Filter Programme"); 
        
        String randomCode = TU.genRandomCode();
        String levelRandom = TU.genRandomValue("levelType");
        String programmeTypeRandom = TU.genRandomValue("programmeType");
        String qualificationAwardedRandom = TU.genRandomValue("qualificationAwardedType");
        String levelOfQualificationRandom = TU.genRandomValue("levelOfQualificationType");
        String teachingLanguage = "es";

        logStep("Programme [" + randomCode + "] load...");

        String programmePayload = new String(Files.readAllBytes(Paths.get("src/test/resources/" + entity + "_filter_template.json")),StandardCharsets.UTF_8);

        String payload = programmePayload.replace("--" + templateAbrev + "_CODE_TOBEINFORMED--", randomCode)
                .replace("--" + templateAbrev + "_level--", levelRandom)
                .replace("--" + templateAbrev + "_programmeType--", programmeTypeRandom)
                .replace("--" + templateAbrev + "_teachingLanguage--", teachingLanguage)
                .replace("--" + templateAbrev + "_levelOfQualification--", levelOfQualificationRandom)
                .replace("--" + templateAbrev + "_qualificationAwarded--", qualificationAwardedRandom);

        TUCudRest.post_testCode(restResource, payload, randomCode, webTestClient);

        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("programmeType", programmeTypeRandom);
        filterMap.put("teachingLanguage", teachingLanguage);
        filterMap.put("qualificationAwarded", qualificationAwardedRandom);
        filterMap.put("levelOfQualification", levelOfQualificationRandom);

        TUGetRest.get_filter("programmes", filterMap, webTestClient);

    }

    @Test
    void FilterProgrammeCode() throws IOException {

        logStep("Filter Programme Code");        
        
        String randomCode = TU.genRandomCode();
        String levelRandom = TU.genRandomValue("levelType");
        String programmeTypeRandom = TU.genRandomValue("programmeType");
        String qualificationAwardedRandom = TU.genRandomValue("qualificationAwardedType");
        String levelOfQualificationRandom = TU.genRandomValue("levelOfQualificationType");
        String teachingLanguage = "es";

        logStep("Programme [" + randomCode + "] load...");

        String programmePayload = new String(Files.readAllBytes(Paths.get("src/test/resources/" + entity + "_filter_template.json")),StandardCharsets.UTF_8);

        String payload = programmePayload.replace("--" + templateAbrev + "_CODE_TOBEINFORMED--", randomCode)
                .replace("--" + templateAbrev + "_level--", levelRandom)
                .replace("--" + templateAbrev + "_programmeType--", programmeTypeRandom)
                .replace("--" + templateAbrev + "_teachingLanguage--", teachingLanguage)
                .replace("--" + templateAbrev + "_levelOfQualification--", levelOfQualificationRandom)
                .replace("--" + templateAbrev + "_qualificationAwarded--", qualificationAwardedRandom);

        TUCudRest.post_testCode(restResource, payload, randomCode, webTestClient);
        TUGetRest.get_primaryCode(restResource, randomCode, webTestClient);
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
