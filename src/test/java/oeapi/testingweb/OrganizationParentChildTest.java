package oeapi.testingweb;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the Organization parent / children relation.
 *
 * Wire format under test (OrganizationDTO):
 *   - "parent"   is the parent's organizationId, a plain string;
 *   - "children" is an array of the direct children's organizationIds;
 *   - ?expand=parent / ?expand=children replace either with the full object(s).
 *
 * Two regressions are guarded here:
 *
 *   1. HTTP 415 on POST /organizations. When @JsonBackReference was removed from
 *      OrganizationDTO.parent, the @JsonManagedReference left on children became
 *      unpaired; Jackson then could not build a deserializer, canDeserialize()
 *      returned false, MappingJackson2HttpMessageConverter.canRead() returned
 *      false, and Spring answered "Unsupported Media Type" for perfectly valid
 *      JSON. CreateRootOrganization is the canary: it only has to reach 200.
 *
 *   2. "children" serialized as full nested objects instead of ids, which is both
 *      off-spec and a recursion risk once a subtree gets deep.
 *
 * Reading the failures:
 *   - 415 on any POST here  -> the Jackson managed/back reference pairing is broken again;
 *   - children present but as objects -> childrenIds is not being kept in sync by setChildren;
 *   - children absent on the root -> ModelMapper is not populating the DTO's children list;
 *   - expand returning ids -> the expandable field is not public, so Class.getField() missed it.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrganizationParentChildTest {

    Logger logger = LoggerFactory.getLogger(OrganizationParentChildTest.class);

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;

    private String rootId;
    private String rootCode;
    private String childAId;
    private String childACode;
    private String childBId;
    private String childBCode;
    private String selfCode;

    /**
     * Codes are NOT taken from TestUtil.genRandomCode() here. That helper reseeds
     * with new Random(System.currentTimeMillis()) on every call, so several calls
     * inside one method land in the same millisecond and return the SAME code.
     * oeapiEndpointService.create() rejects a duplicate primaryCode with
     * "This element [code] already exists, cannot create it", so the children
     * would fail to be created for a reason that has nothing to do with parents.
     */
    @BeforeAll
    void initOnce() {
        String run = UUID.randomUUID().toString().substring(0, 8);

        rootId = UUID.randomUUID().toString();
        rootCode = "PCT-" + run + "-ROOT";
        childAId = UUID.randomUUID().toString();
        childACode = "PCT-" + run + "-A";
        childBId = UUID.randomUUID().toString();
        childBCode = "PCT-" + run + "-B";
        selfCode = "PCT-" + run + "-SELF";
    }

    /**
     * Regression for the 415. A parentless organization is the simplest possible
     * body; if this returns anything but 200 the deserializer is broken and every
     * other assertion below is meaningless.
     */
    @Test
    @Order(1)
    void CreateRootOrganization() throws Exception {

        logStep("Create root Organization [" + rootId + "]");

        post("/organizations", TU.getPayload("organization_template", "ORG", rootId, rootCode))
                .jsonPath("$.organizationId").isEqualTo(rootId)
                .jsonPath("$.primaryCode.code").isEqualTo(rootCode);
    }

    /**
     * "parent" is written as a bare organizationId, and echoed back the same way.
     */
    @Test
    @Order(2)
    void CreateChildrenWithParentAsId() throws Exception {

        logStep("Create child Organization [" + childAId + "] under [" + rootId + "]");

        post("/organizations", childPayload(childAId, childACode, rootId))
                .jsonPath("$.organizationId").isEqualTo(childAId)
                .jsonPath("$.parent").isEqualTo(rootId);

        logStep("Create child Organization [" + childBId + "] under [" + rootId + "]");

        post("/organizations", childPayload(childBId, childBCode, rootId))
                .jsonPath("$.organizationId").isEqualTo(childBId)
                .jsonPath("$.parent").isEqualTo(rootId);
    }

    /**
     * The root must list both children, as ids, and only its direct children.
     */
    @Test
    @Order(3)
    void RootListsDirectChildrenAsIds() {

        logStep("Read root [" + rootId + "]: children must be an array of ids");

        get("/organizations/" + rootId)
                .jsonPath("$.organizationId").isEqualTo(rootId)
                .jsonPath("$.children").exists()
                .jsonPath("$.children.length()").isEqualTo(2)
                .jsonPath("$.children").value(Matchers.<String>hasItems(childAId, childBId));
    }

    /**
     * Creating children must not overwrite the parent row. The DTO hands
     * ModelMapper a stub OrganizationDTO for the parent, and Organization.children
     * cascades ALL, so a regression here would blank the parent's own fields.
     */
    @Test
    @Order(4)
    void ParentSurvivesChildCreation() {

        logStep("Read root [" + rootId + "]: its own fields must be intact");

        get("/organizations/" + rootId)
                .jsonPath("$.primaryCode.code").isEqualTo(rootCode)
                .jsonPath("$.shortName").isEqualTo("UPNA")
                .jsonPath("$.link").isEqualTo("https://www.unavarra.es");
    }

    /**
     * A child points back at its parent by id, and being a leaf has no children
     * key at all (@JsonInclude(NON_EMPTY) drops the empty array).
     */
    @Test
    @Order(5)
    void ChildReportsParentAsIdAndHasNoChildren() {

        logStep("Read child [" + childAId + "]: parent as id, no children key");

        get("/organizations/" + childAId)
                .jsonPath("$.organizationId").isEqualTo(childAId)
                .jsonPath("$.parent").isEqualTo(rootId)
                .jsonPath("$.children").doesNotExist();
    }

    /**
     * ?expand=children promotes the ids to full objects. Note the objects
     * themselves still carry parent/children as ids, so expansion is one level
     * deep and cannot recurse.
     */
    @Test
    @Order(6)
    void ExpandChildrenReturnsObjects() {

        logStep("Read root [" + rootId + "] with ?expand=children");

        get("/organizations/" + rootId + "?expand=children")
                .jsonPath("$.children.length()").isEqualTo(2)
                .jsonPath("$.children[*].organizationId")
                        .value(Matchers.<String>hasItems(childAId, childBId))
                .jsonPath("$.children[0].primaryCode.code").exists()
                .jsonPath("$.children[0].parent").isEqualTo(rootId);
    }

    /**
     * ?expand=parent does the same for the other direction.
     */
    @Test
    @Order(7)
    void ExpandParentReturnsObject() {

        logStep("Read child [" + childAId + "] with ?expand=parent");

        get("/organizations/" + childAId + "?expand=parent")
                .jsonPath("$.parent.organizationId").isEqualTo(rootId)
                .jsonPath("$.parent.primaryCode.code").isEqualTo(rootCode);
    }

    /**
     * Without expand the ids must come back, unchanged, as strings - guarding
     * against a future change that silently starts nesting objects again.
     */
    @Test
    @Order(8)
    void WithoutExpandParentStaysAnId() {

        logStep("Read child [" + childAId + "] without expand");

        get("/organizations/" + childAId)
                .jsonPath("$.parent").value(Matchers.instanceOf(String.class));
    }


    /**
     * An organization must not be accepted as its own parent - that would loop
     * forever when walking children.
     *
     * This asserts the behaviour that matters (rejected as a client error, with the
     * reason in the body, and not created) rather than an exact status code. Today
     * the reply is 404 even though the body says BAD_REQUEST, because oeapiController
     * is annotated @ControllerAdvice and its @ExceptionHandler(oeapiException.class)
     * returns ResponseEntity.status(HttpStatus.NOT_FOUND) unconditionally, discarding
     * ex.getStatus(). Once that handler honours the exception's status, tighten the
     * first assertion to .isBadRequest().
     */
    @Test
    @Order(9)
    void SelfParentIsRejected() throws Exception {

        String selfId = UUID.randomUUID().toString();

        logStep("Create Organization [" + selfId + "] parented to itself - must be rejected");

        webTestClient.post()
                .uri("/organizations")
                .header("Authorization", TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(childPayload(selfId, selfCode, selfId))
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.title").value(Matchers.containsString("cannot be parent of itself"));

        logStep("And it must not have been created");

        webTestClient.get()
                .uri("/organizations/" + selfId)
                .header("Authorization", TU.authHeaderForTest())
                .exchange()
                .expectStatus().isNotFound();
    }

    /**
     * A parent id that is not in the database must be refused outright, not written
     * as a dangling foreign key. OrganizationService.toEntity() looks the parent up
     * and rejects it, so this returns a real 400 (the exception is raised inside
     * oeapiDTOController.createDTO's try block, which honours ex.getStatus()).
     */
    @Test
    @Order(10)
    void UnknownParentIsRejected() throws Exception {

        String orphanId = UUID.randomUUID().toString();
        String ghostParentId = UUID.randomUUID().toString();

        logStep("Create Organization [" + orphanId + "] under non-existent parent [" + ghostParentId + "]");

        webTestClient.post()
                .uri("/organizations")
                .header("Authorization", TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(childPayload(orphanId, "PCT-" + ghostParentId.substring(0, 8) + "-ORPHAN", ghostParentId))
                .exchange()
                .expectStatus().isBadRequest();

        logStep("And it must not have been created");

        webTestClient.get()
                .uri("/organizations/" + orphanId)
                .header("Authorization", TU.authHeaderForTest())
                .exchange()
                .expectStatus().isNotFound();
    }

    private String childPayload(String id, String code, String parentId) throws Exception {
        return new String(
                Files.readAllBytes(Paths.get("src/test/resources/organization_child_template.json")),
                StandardCharsets.UTF_8)
                .replace("--ORG_ID_TOBEINFORMED--", id)
                .replace("--ORG_CODE_TOBEINFORMED--", code)
                .replace("--PARENT_ID_TOBEINFORMED--", parentId);
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
