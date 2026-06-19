package oeapi.testingweb;

import oeapi.model.AcademicSession;
import oeapi.service.AcademicSessionService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AcademicSessionTest {
    @Test
    void DeserializeAcademicSessionInsertPayloadWithName() {
        String payload = """
                {
                  "endDate": "2030-01-04",
                  "academicSessionType": "quarter",
                  "startDate": "2029-09-01",
                  "name": [
                    {
                      "language": "en-GB",
                      "value": "EN TRANSLATION: semester 1"
                    },
                    {
                      "language": "nl-NL",
                      "value": "NL VERTALING: semester 1"
                    }
                  ]
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        AcademicSession academicSession = assertDoesNotThrow(
                () -> objectMapper.readValue(payload, AcademicSession.class));

        assertEquals("quarter", academicSession.getAcademicSessionType());
        assertEquals("en-GB", academicSession.getName().get(0).getLanguage());
        assertEquals("nl-NL", academicSession.getName().get(1).getLanguage());
    }

    @Autowired
    private AcademicSessionService academicSessionService;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;

    private String academicSessionId, academicSessionCode, academicSessionPayload;

    @BeforeEach
    void beforeEach() throws Exception {
        academicSessionId = UUID.randomUUID().toString();
        academicSessionCode = TU.genRandomCode();
        academicSessionPayload = new String(Files.readAllBytes(Paths.get("src/test/resources/academic_session_template.json")))
            .replace("--AC_ID_TOBEINFORMED--", academicSessionId)
            .replace("--AC_CODE_TOBEINFORMED--", academicSessionCode);
    }

    @AfterEach
    void afterEach() throws Exception {
        academicSessionService.delete(academicSessionId);
    }

    @Test
    void createAcademicSessionTest() throws Exception {
        webTestClient.post()
            .uri("/academic-sessions")
            .header("Authorization", TU.authHeaderForTest())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(academicSessionPayload)
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.academicSessionId").isEqualTo(academicSessionId);

        webTestClient.get()
            .uri("/academic-sessions/{academicSessionId}", academicSessionId)
            .header("Authorization", TU.authHeaderForTest())
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.academicSessionId").isEqualTo(academicSessionId)
            .jsonPath("$.primaryCode.code").isEqualTo(academicSessionCode);
    }

    @Test
    void updateAcademicSessionTest() throws Exception {
        createAcademicSessionTest();

        String newAcademicSessionCode = TU.genRandomCode();
        assert(! academicSessionCode.equals(newAcademicSessionCode));

        webTestClient.put()
            .uri("/academic-sessions/{academicSessionId}", academicSessionId)
            .header("Authorization", TU.authHeaderForTest())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(academicSessionPayload
                       .replace(academicSessionCode, newAcademicSessionCode))
            .exchange().expectStatus().isOk();

        Optional<AcademicSession> ac = academicSessionService.getById(academicSessionId);
        assert(ac.isPresent() && ac.get().getPrimaryCode().getCode().equals(newAcademicSessionCode));

        webTestClient.get()
            .uri("/academic-sessions/{academicSessionId}", academicSessionId)
            .header("Authorization", TU.authHeaderForTest())
            .exchange().expectStatus().isOk()
            .expectBody()
            .jsonPath("$.primaryCode.code").isEqualTo(newAcademicSessionCode);

    }

    @Test
    void deleteAcademicSessionTest() throws Exception {
        createAcademicSessionTest();

        webTestClient.delete()
            .uri("/academic-sessions/{academicSessionId}", academicSessionId)
            .header("Authorization", TU.authHeaderForTest())
            .exchange().expectStatus().isOk();

        assert(academicSessionService.getById(academicSessionId).isEmpty());
    }
}
