package oeapi.testingweb;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Covers the "expand" query parameter on GET /courses/{courseId}.
 *
 * OOAPI v5 allows expand=programs, coordinators, organization, educationSpecification on
 * this endpoint (v6 renames it to "organisation" and adds "instructors"). This exercises
 * the organization case: unexpanded the response carries the organizationId as a string,
 * expanded it carries the organization itself.
 *
 * Reading the failures:
 *   - "$.organization" is an object without expand -> the id/object pair is out of sync;
 *   - "$.organization.primaryCode" missing WITH expand -> the expanded value is an id-only
 *     stub rather than the stored organization (this was the "!=" String comparison in
 *     oeapiEducationDTO.setOrganizationId, which discarded the mapped object);
 *   - "$.organization" still a plain string WITH expand -> the expansion did not fire at
 *     all; check that the field is public, since oeapiDTOMapper.toJSON() resolves
 *     expandables with Class.getField();
 *   - the expanded object carries "organizationTypeId" -> it was serialized as the entity
 *     rather than mapped to its DTO first (oeapiDTOMapperRegistry).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CourseExpandTest {

    Logger logger = LoggerFactory.getLogger(CourseExpandTest.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;

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
        orgCode = "CXP-" + run + "-ORG";
        courseId = UUID.randomUUID().toString();
        courseCode = "CXP-" + run + "-COURSE";
    }

    @Test
    @Order(1)
    void CreateCourseWithOrganization() throws Exception {

        logStep("Create Organization [" + orgId + "]");

        post("/organizations", TU.getPayload("organization_template", "ORG", orgId, orgCode))
                .jsonPath("$.organizationId").isEqualTo(orgId);

        logStep("Create Course [" + courseId + "] belonging to that Organization");

        // course_template.json carries no organization, so add the reference here rather
        // than editing a template several other tests depend on.
        ObjectNode course = (ObjectNode) objectMapper
                .readTree(TU.getPayload("course_template", "COURSE", courseId, courseCode));
        course.put("organization", orgId);

        post("/courses", objectMapper.writeValueAsString(course))
                .jsonPath("$.courseId").isEqualTo(courseId)
                .jsonPath("$.organization").isEqualTo(orgId);
    }

    /**
     * Without expand the organization is a plain organizationId, per the spec.
     */
    @Test
    @Order(2)
    void WithoutExpandOrganizationIsAnId() {

        logStep("Read Course [" + courseId + "] without expand");

        get("/courses/" + courseId)
                .jsonPath("$.courseId").isEqualTo(courseId)
                .jsonPath("$.organization").isEqualTo(orgId);
    }

    /**
     * With expand the organization is the stored object, not an id-only stub: it must carry
     * the fields the organization was created with.
     */
    @Test
    @Order(3)
    void ExpandOrganizationReturnsTheStoredOrganization() {

        logStep("Read Course [" + courseId + "] with ?expand=organization");

        get("/courses/" + courseId + "?expand=organization")
                .jsonPath("$.courseId").isEqualTo(courseId)
                .jsonPath("$.organization.organizationId").isEqualTo(orgId)
                .jsonPath("$.organization.primaryCode.code").isEqualTo(orgCode)
                .jsonPath("$.organization.shortName").isEqualTo("UPNA");
    }

    /**
     * The expanded organization must be OOAPI shaped, not entity shaped.
     *
     * oeapiEducationDTO.organization is typed as the Organization ENTITY, so serializing the
     * value as it stands answers with the entity's shape - the enum as the internal
     * "organizationTypeId" rather than the spec's "organizationType" value - because no
     * entity -> DTO conversion happened for it at all. oeapiDTOMapper.toJSON() therefore
     * routes an expanded value through the mapper registered for its type
     * (oeapiDTOMapperRegistry).
     *
     * Reading the failures:
     *   - "organizationTypeId" present -> the value was serialized as the entity, so no
     *     mapper was found for Organization; check that OrganizationService still goes
     *     through oeapiEndpointDTOService.setMapper(), which is what registers it;
     *   - "organizationType" present but a number -> a mapper was found but its enum fixup
     *     did not run.
     */
    @Test
    @Order(4)
    void ExpandedOrganizationIsSpecShaped() {

        logStep("Read Course [" + courseId + "] with ?expand=organization: OOAPI shape");

        get("/courses/" + courseId + "?expand=organization")
                .jsonPath("$.organization.organizationType").isEqualTo("department")
                .jsonPath("$.organization.organizationTypeId").doesNotExist();
    }

    /**
     * Mapping the expansion through the DTO must not lose anything: the organization arrives
     * with the same fields GET /organizations/{id} would return.
     */
    @Test
    @Order(5)
    void ExpandedOrganizationKeepsItsFields() {

        logStep("Read Course [" + courseId + "] with ?expand=organization: no field lost");

        get("/courses/" + courseId + "?expand=organization")
                .jsonPath("$.organization.name[0].value").isEqualTo("Organization [" + orgCode + "]")
                .jsonPath("$.organization.link").isEqualTo("https://www.unavarra.es")
                .jsonPath("$.organization.primaryCode.codeType").isEqualTo("identifier")
                .jsonPath("$.organization.otherCodes[0].code").isEqualTo("Otro");
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
}
