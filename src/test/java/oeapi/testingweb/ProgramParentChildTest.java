package oeapi.testingweb;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
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
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Program parent / children relation (issue #183).
 *
 * Children are not a JPA collection on Program any more: they are derived from each
 * child's "parent" by ProgramService.findChildren() and put on the DTO by
 * oeapiProgramMapper.toDTO(). So:
 *   - GET on a parent lists its children as ids;
 *   - PUT on a parent - with or without "children" - never deletes a child.
 */
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProgramParentChildTest {

    Logger logger = LoggerFactory.getLogger(ProgramParentChildTest.class);

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;

    @Autowired
    private TestUtilCUDRest TUCudRest;

    private String parentId;
    private String parentCode;
    private String childId;
    private String childCode;

    @BeforeAll
    void initOnce() {
        String run = UUID.randomUUID().toString().substring(0, 8);
        parentId = UUID.randomUUID().toString();
        parentCode = "PPC-" + run + "-PARENT";
        childId = UUID.randomUUID().toString();
        childCode = "PPC-" + run + "-CHILD";
    }

    @Test
    @Order(1)
    void CreateParentAndChild() throws Exception {
        post("/programs", programPayload(parentId, parentCode, null))
                .jsonPath("$.programId").isEqualTo(parentId);

        post("/programs", programPayload(childId, childCode, parentId))
                .jsonPath("$.programId").isEqualTo(childId)
                .jsonPath("$.parent").isEqualTo(parentId);
    }

    @Test
    @Order(2)
    void ParentListsChildAsId() {
        get("/programs/" + parentId)
                .jsonPath("$.children.length()").isEqualTo(1)
                .jsonPath("$.children[0]").isEqualTo(childId);

        get("/programs/" + childId)
                .jsonPath("$.parent").isEqualTo(parentId)
                .jsonPath("$.children").doesNotExist();
    }

    @Test
    @Order(3)
    void ExpandChildrenReturnsObjects() {
        get("/programs/" + parentId + "?expand=children")
                .jsonPath("$.children[0].programId").isEqualTo(childId)
                .jsonPath("$.children[0].parent").isEqualTo(parentId);
    }

    /** The exact scenario of issue #183: PUT the parent's own GET response back. */
    @Test
    @Order(4)
    void PutParentRoundTripKeepsChild() {
        String body = new String(get("/programs/" + parentId).returnResult().getResponseBody());

        put("/programs/" + parentId, body);

        get("/programs/" + childId)
                .jsonPath("$.parent").isEqualTo(parentId);
    }

    /** Also when the PUT body has no "children" at all. */
    @Test
    @Order(5)
    void PutParentWithoutChildrenKeepsChild() throws Exception {
        put("/programs/" + parentId, programPayload(parentId, parentCode, null));

        get("/programs/" + childId)
                .jsonPath("$.parent").isEqualTo(parentId);
        get("/programs/" + parentId)
                .jsonPath("$.children")
                        .value((List<String> children) -> assertThat(children, Matchers.hasItem(childId)));
    }

    /** Deleting the parent detaches the child instead of deleting it. */
    @Test
    @Order(6)
    void DeleteParentDetachesChild() {
        webTestClient.delete()
                .uri("/programs/" + parentId)
                .header("Authorization", TU.authHeaderForTest())
                .exchange()
                .expectStatus().is2xxSuccessful();

        get("/programs/" + childId)
                .jsonPath("$.programId").isEqualTo(childId)
                .jsonPath("$.parent").doesNotExist();
    }

    private String programPayload(String id, String code, String parent) throws Exception {
        String payload = TU.getPayload("program_template", "PROG", id, code);
        if (parent != null) {
            payload = payload.replaceFirst("\"programType\"", "\"parent\": \"" + parent + "\",\n    \"programType\"");
        }
        return payload;
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

    @AfterAll
    void cleanUpCreatedData() {
        TUCudRest.deleteQuiet("programs", childId, webTestClient);
        TUCudRest.deleteQuiet("programs", parentId, webTestClient);
        TUCudRest.cleanupCreated(webTestClient);
    }
}
