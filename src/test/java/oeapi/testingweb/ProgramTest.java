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
class ProgramTest {

    Logger logger = LoggerFactory.getLogger(PersonTest.class);
    String restResource = "programs";
    String templateAbrev = "PROG";
    String entity = "program";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void CreateProgram() throws IOException {

        logStep("Create Program"); 
        
        TestUtilCUDRest.whenPost_test(restResource, entity, templateAbrev, webTestClient);

    }

    @Test
    void FilterProgram() throws IOException {

        logStep("Filter Program"); 
        
        String randomCode = TestUtil.genRandomCode();
        String levelRandom = TestUtil.genRandomValue("levelType");
        String programTypeRandom = TestUtil.genRandomValue("programType");
        String sectorRandom = TestUtil.genRandomValue("sectorType");
        String qualificationAwardedRandom = TestUtil.genRandomValue("qualificationAwardedType");
        String levelOfQualificationRandom = TestUtil.genRandomValue("levelOfQualificationType");
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

        TestUtilCUDRest.post_testCode(restResource, payload, randomCode, webTestClient);

        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("programType", programTypeRandom);
        filterMap.put("teachingLanguage", teachingLanguage);
        filterMap.put("sector", sectorRandom);
        filterMap.put("qualificationAwarded", qualificationAwardedRandom);
        filterMap.put("levelOfQualification", levelOfQualificationRandom);

        TestUtilGetRest.get_filter("programs", filterMap, webTestClient);

    }

    @Test
    void FilterProgramCode() throws IOException {

        logStep("Filter Program Code");        
        
        String randomCode = TestUtil.genRandomCode();
        String levelRandom = TestUtil.genRandomValue("levelType");
        String programTypeRandom = TestUtil.genRandomValue("programType");
        String sectorRandom = TestUtil.genRandomValue("sectorType");
        String qualificationAwardedRandom = TestUtil.genRandomValue("qualificationAwardedType");
        String levelOfQualificationRandom = TestUtil.genRandomValue("levelOfQualificationType");
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

        TestUtilCUDRest.post_testCode(restResource, payload, randomCode, webTestClient);
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
