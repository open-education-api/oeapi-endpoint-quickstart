package oeapi.testingweb;

import java.util.UUID;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.AfterAll;

/**
 * Covers ?expand= on a to-MANY relation, using expand=programs on GET /courses/{courseId}.
 *
 * The to-one case (expand=organization) and the to-many case take different paths through
 * oeapiDTOMapperService: a to-one value is an entity, whose class is a registered key, while a
 * to-many value is a List - a Hibernate PersistentBag, in fact - whose class never is. Looking
 * the value up directly therefore finds nothing for a collection, and the expansion falls back
 * to serializing the entities. Every element has to be converted individually.
 *
 * Reading the failures:
 *   - "$.programs[0]" is a String WITH expand -> the expansion did not fire at all; check that
 *     the field is public, since oeapiDTOMapper.toJSON() resolves expandables with
 *     Class.getField();
 *   - "$.programs[0].organization" missing WITH expand -> the elements were serialized as
 *     entities rather than mapped to their DTO. Program.organization is a @JsonBackReference,
 *     so the entity does not serialize it at all, while ProgramDTO publishes it as the
 *     organizationId - which makes it a reliable tell for which of the two shapes came back.
 */
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CourseExpandProgramsTest {

    Logger logger = LoggerFactory.getLogger(CourseExpandProgramsTest.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;

    @Autowired
    private TestUtilCUDRest TUCudRest;

    private String orgId;
    private String orgCode;
    private String progId;
    private String progCode;
    private String courseId;
    private String courseCode;

    /**
     * Codes are not taken from TestUtil.genRandomCode(): it reseeds with
     * new Random(System.currentTimeMillis()) on every call, so several calls in the same
     * millisecond return the same code and the second create is rejected as a duplicate
     * primaryCode.
     */
    @BeforeAll
    void initOnce() {
        String run = UUID.randomUUID().toString().substring(0, 8);

        orgId = UUID.randomUUID().toString();
        orgCode = "CXPP-" + run + "-ORG";
        progId = UUID.randomUUID().toString();
        progCode = "CXPP-" + run + "-PROG";
        courseId = UUID.randomUUID().toString();
        courseCode = "CXPP-" + run + "-COURSE";
    }

    @Test
    @Order(1)
    void CreateCourseWithAProgram() throws Exception {

        logStep("Create Organization [" + orgId + "]");

        post("/organizations", TU.getPayload("organization_template", "ORG", orgId, orgCode))
                .jsonPath("$.organizationId").isEqualTo(orgId);

        logStep("Create Program [" + progId + "] in that Organization");

        // The templates carry neither an organization nor a program list, so the references are
        // added here rather than by editing files several other tests depend on.
        ObjectNode program = (ObjectNode) objectMapper
                .readTree(TU.getPayload("program_template", "PROG", progId, progCode));
        program.put("organization", orgId);

        post("/programs", objectMapper.writeValueAsString(program))
                .jsonPath("$.programId").isEqualTo(progId)
                .jsonPath("$.organization").isEqualTo(orgId);

        logStep("Create Course [" + courseId + "] belonging to that Program");

        ObjectNode course = (ObjectNode) objectMapper
                .readTree(TU.getPayload("course_template", "COURSE", courseId, courseCode));
        course.putArray("programs").add(progId);

        post("/courses", objectMapper.writeValueAsString(course))
                .jsonPath("$.courseId").isEqualTo(courseId);
    }

    /**
     * Without expand the programs are a list of programIds, per the spec.
     */
    @Test
    @Order(2)
    void WithoutExpandProgramsAreIds() {

        logStep("Read Course [" + courseId + "] without expand");

        get("/courses/" + courseId)
                .jsonPath("$.programs.length()").isEqualTo(1)
                .jsonPath("$.programs[0]").isEqualTo(progId)
                .jsonPath("$.programs[0]").value(Matchers.instanceOf(String.class));
    }

    /**
     * With expand every id becomes the stored program, not an id-only stub.
     */
    @Test
    @Order(3)
    void ExpandProgramsReturnsTheStoredPrograms() {

        logStep("Read Course [" + courseId + "] with ?expand=programs");

        get("/courses/" + courseId + "?expand=programs")
                .jsonPath("$.programs.length()").isEqualTo(1)
                .jsonPath("$.programs[0].programId").isEqualTo(progId)
                .jsonPath("$.programs[0].primaryCode.code").isEqualTo(progCode);
    }

    /**
     * And every element is OOAPI shaped, which is what needs converting one by one: the value
     * handed to the expansion is the List, and a List has no mapper of its own.
     */
    @Test
    @Order(4)
    void ExpandedProgramsAreSpecShaped() {

        logStep("Read Course [" + courseId + "] with ?expand=programs: OOAPI shape");

        get("/courses/" + courseId + "?expand=programs")
                .jsonPath("$.programs[0].organization").isEqualTo(orgId);
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

    /**
     * Teardown. Tests in this class used to leave their entities in the database:
     *
     * This runs even when a test fails, and it never asserts - teardown must not turn a
     * passing run red.
     */
    @AfterAll
    void cleanUpCreatedData() {
        TUCudRest.deleteQuiet("courses", courseId, webTestClient);
        TUCudRest.deleteQuiet("programs", progId, webTestClient);
        TUCudRest.deleteQuiet("organizations", orgId, webTestClient);
        TUCudRest.cleanupCreated(webTestClient);
    }

}
