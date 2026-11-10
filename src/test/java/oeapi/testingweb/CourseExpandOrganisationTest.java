package oeapi.testingweb;

import java.util.UUID;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.hamcrest.MatcherAssert.assertThat;
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
 * Covers ?expand= on a to-ONE relation, using expand=organisation on GET /courses/{courseId}.
 *
 * KomTest already covers the same parameter on a PROGRAMME. A course is worth covering separately
 * rather than assumed equivalent: it is a different DTO and therefore a different ModelMapper
 * TypeMap, and both halves of the relation have a setter - setOrganisation.. 
 *
 * Reading the failures:
 *   - "$.organisation.primaryCode" or ".shortName" missing WITH expand -> the expanded value is
 *     an id-only stub, so setOrganisationId ran last and discarded the mapped organisation.
 *     The fix is to make its guard compare with equals, so it only builds a stub when it does
 *     not already hold the object for that id;
 *   - "$.organisation" still a plain string WITH expand -> the expansion did not fire at all;
 *     check that the field is public, since oeapiDTOMapper.toJSON() resolves expandables with
 *     Class.getField();
 *   - "organisationType" absent or a number -> the value was serialized as the entity rather
 *     than mapped through its DTO (oeapiDTOMapperService).
 */
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CourseExpandOrganisationTest {

    Logger logger = LoggerFactory.getLogger(CourseExpandOrganisationTest.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;

    @Autowired
    private TestUtilCUDRest TUCudRest;

    private String orgId;
    private String orgCode;
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
        orgCode = "CXO-" + run + "-ORG";
        courseId = UUID.randomUUID().toString();
        courseCode = "CXO-" + run + "-COURSE";
    }

    @Test
    @Order(1)
    void CreateCourseInAnOrganisation() throws Exception {

        logStep("Create Organisation [" + orgId + "]");

        post("/organisations", TU.getPayload("organisation_template", "ORG", orgId, orgCode))
                .jsonPath("$.organisationId").isEqualTo(orgId);

        logStep("Create Course [" + courseId + "] belonging to that Organisation");

        // course_template.json carries no organisation, so the reference is added here rather
        // than by editing a file several other tests depend on.
        ObjectNode course = (ObjectNode) objectMapper
                .readTree(TU.getPayload("course_template", "COURSE", courseId, courseCode));
        course.put("organisationId", orgId);

        post("/courses", objectMapper.writeValueAsString(course))
                .jsonPath("$.courseId").isEqualTo(courseId)
                .jsonPath("$.organisationId").isEqualTo(orgId);
    }

    /**
     * Without expand the organisation is a plain organisationId, per the spec.
     */
    @Test
    @Order(2)
    void WithoutExpandOrganisationIsAnId() {

        logStep("Read Course [" + courseId + "] without expand");

        get("/courses/" + courseId)
                .jsonPath("$.organisationId").isEqualTo(orgId)
                .jsonPath("$.organisationId")
                        .value((Object organisation) -> assertThat(organisation, Matchers.instanceOf(String.class)));
    }

    /**
     * With expand it is the stored organisation, not an id-only stub: it has to carry the
     * fields the organisation was created with. primaryCode and shortName are the tell - a
     * stub built from the id alone has neither.
     */
    @Test
    @Order(3)
    void ExpandOrganisationReturnsTheStoredOrganisation() {

        logStep("Read Course [" + courseId + "] with ?expand=organisation");

        get("/courses/" + courseId + "?expand=organisation")
                .jsonPath("$.organisation.organisationId").isEqualTo(orgId)
                .jsonPath("$.organisation.primaryCode.code").isEqualTo(orgCode)
                .jsonPath("$.organisation.shortName").isEqualTo("UPNA")
                .jsonPath("$.organisation.link").isEqualTo("https://www.unavarra.es");
    }

    /**
     * And it is OOAPI shaped, which is what going through the organisation's own DTO mapper
     * buys: the enum arrives as its value rather than the internal organisationTypeId.
     */
    @Test
    @Order(4)
    void ExpandedOrganisationIsSpecShaped() {

        logStep("Read Course [" + courseId + "] with ?expand=organisation: OOAPI shape");

        get("/courses/" + courseId + "?expand=organisation")
                .jsonPath("$.organisation.organisationType").isEqualTo("department")
                .jsonPath("$.organisation.organisationTypeId").doesNotExist();
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
     * Teardown. Tests in this class used to leave their entities in the database
     * 
     * This runs even when a test fails, and it never asserts - teardown must not turn a
     * passing run red.
     */
    @AfterAll
    void cleanUpCreatedData() {
        TUCudRest.deleteQuiet("courses", courseId, webTestClient);
        TUCudRest.deleteQuiet("organisations", orgId, webTestClient);
        TUCudRest.cleanupCreated(webTestClient);
    }

}
