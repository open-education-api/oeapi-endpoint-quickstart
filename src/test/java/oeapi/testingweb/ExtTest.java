package oeapi.testingweb;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Regression tests for the free-form OOAPI {@code ext} attribute.
 *
 * Background: {@code Ext} is not a JPA type, so an entity field of that type
 * needs an explicit {@code @Convert} to be stored as JSON. Without one Hibernate
 * falls back to SerializableType and writes a Java-serialized byte stream into
 * the text column, which MySQL/MariaDB reject at INSERT time:
 *
 *   Incorrect string value: '\xAC\xED\x00\x05sr...' for column 'ext' at row 1
 *
 * The bug stayed hidden because no template sent a top-level "ext": a null Ext
 * is written as SQL NULL, so only a non-null value reaches the broken path.
 *
 * Reading the failures:
 *   - a 500 on the POST/PUT means the ext column mapping is broken (no converter);
 *   - a 200 on the POST but a missing "$.ext.*" on the GET means the column is
 *     fine and the entity -> DTO mapping is dropping the Ext contents instead.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ExtTest {

    Logger logger = LoggerFactory.getLogger(ExtTest.class);

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;

    @Test
    void ProgramExtRoundTrip() throws Exception {

        String id = UUID.randomUUID().toString();
        String code = TU.genRandomCode();

        logStep("Create Program [" + id + "] carrying ext");
        post("/programs", TU.getPayload("program_template", "PROG", id, code))
                .jsonPath("$.programId").isEqualTo(id);

        logStep("Read Program [" + id + "] back and check ext survived the database");
        assertExt(get("/programs/" + id), "Pamplona", code);
    }

    @Test
    void ProgramExtUpdate() throws Exception {

        String id = UUID.randomUUID().toString();
        String code = TU.genRandomCode();

        logStep("Create Program [" + id + "] carrying ext");
        post("/programs", TU.getPayload("program_template", "PROG", id, code))
                .jsonPath("$.programId").isEqualTo(id);

        String newCode = TU.genRandomCode();

        logStep("Update Program [" + id + "] with a different ext");
        put("/programs/" + id, TU.getPayload("updateprogram_template", "PROG", id, newCode))
                .jsonPath("$.primaryCode.code").isEqualTo(newCode);

        logStep("Read Program [" + id + "] back: ext must be replaced, not appended to");
        assertExt(get("/programs/" + id), "Tudela", newCode);
    }

    @Test
    void CourseExtRoundTrip() throws Exception {

        String id = UUID.randomUUID().toString();
        String code = TU.genRandomCode();

        logStep("Create Course [" + id + "] carrying ext");
        post("/courses", TU.getPayload("course_template", "COURSE", id, code))
                .jsonPath("$.courseId").isEqualTo(id);

        logStep("Read Course [" + id + "] back and check ext survived the database");
        assertExt(get("/courses/" + id), "Pamplona", code);
    }

    @Test
    void OfferingExtRoundTrip() throws Exception {

        String courseId = UUID.randomUUID().toString();
        String courseCode = TU.genRandomCode();
        String offeringId = UUID.randomUUID().toString();
        String offeringCode = TU.genRandomCode();

        logStep("Create parent Course [" + courseId + "] for the offering");
        post("/courses", TU.getPayload("course_template", "COURSE", courseId, courseCode))
                .jsonPath("$.courseId").isEqualTo(courseId);

        String offeringPayload = new String(
                Files.readAllBytes(Paths.get("src/test/resources/courseOffering_template.json")),
                StandardCharsets.UTF_8)
                .replace("--OFFERING_ID_TOBEINFORMED--", offeringId)
                .replace("--OFFERING_CODE_TOBEINFORMED--", offeringCode)
                .replace("--COURSE_ID_TOBEINFORMED--", courseId);

        logStep("Create Offering [" + offeringId + "] carrying ext");
        post("/offerings", offeringPayload)
                .jsonPath("$.offeringId").isEqualTo(offeringId);

        logStep("Read Offering [" + offeringId + "] back and check ext survived the database");
        assertExt(get("/offerings/" + offeringId), "Pamplona", offeringCode);
    }

    /**
     * Checks every shape the ext object carries in the templates: string, integer,
     * floating point, boolean, array and nested object. Anything that silently
     * dropped or mangled the JSON would break at least one of these.
     */
    private void assertExt(WebTestClient.BodyContentSpec body, String campus, String code) {
        body.jsonPath("$.ext").exists()
                .jsonPath("$.ext.unitaCampus").isEqualTo(campus)
                .jsonPath("$.ext.unitaCredits").isEqualTo(4)
                .jsonPath("$.ext.unitaWeight").exists()
                .jsonPath("$.ext.unitaActive").isEqualTo(true)
                .jsonPath("$.ext.unitaTags[0]").isEqualTo("alpha")
                .jsonPath("$.ext.unitaTags[1]").isEqualTo("beta")
                .jsonPath("$.ext.unitaNested.code").isEqualTo(code)
                .jsonPath("$.ext.unitaNested.depth").isEqualTo(2);
    }

    private WebTestClient.BodyContentSpec post(String uri, String payload) {
        return webTestClient.post()
                .uri(uri)
                .header("Authorization", TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }

    private WebTestClient.BodyContentSpec put(String uri, String payload) {
        return webTestClient.put()
                .uri(uri)
                .header("Authorization", TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }

    private WebTestClient.BodyContentSpec get(String uri) {
        return webTestClient.get()
                .uri(uri)
                .header("Authorization", TU.authHeaderForTest())
                .exchange()
                .expectStatus().isOk()
                .expectBody();
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
