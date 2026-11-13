package oeapi.testingweb;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * OEAPI v6 learning outcomes (resource, links from courses, v5 texts) and the
 * single "consumer" chosen with ?consumer=.
 */
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class LearningOutcomeAndConsumerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;

    private final ObjectMapper mapper = new ObjectMapper();

    private String courseId;
    private String outcomeId;

    private WebTestClient.ResponseSpec send(HttpMethod method, String uri, String body) {
        WebTestClient.RequestBodySpec spec = webTestClient.method(method).uri(uri)
                .header("Authorization", TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON);
        return body == null ? spec.exchange() : spec.bodyValue(body).exchange();
    }

    @BeforeEach
    void createCourse() throws Exception {
        // the template carries one v5 style inline learning outcome and a UNITA consumer
        courseId = UUID.randomUUID().toString();
        send(HttpMethod.POST, "/courses", TU.getPayload("course_template", "COURSE", courseId, TU.genRandomCode()))
                .expectStatus().isOk();

        outcomeId = UUID.randomUUID().toString();
        String outcome = """
                {
                  "learningOutcomeId": "%s",
                  "primaryCode": {"codeType": "identifier", "code": "%s"},
                  "name": [{"language": "en-GB", "value": "Apply statistical tests"}],
                  "complexityLevel": "bloom_3"
                }
                """.formatted(outcomeId, TU.genRandomCode());
        send(HttpMethod.POST, "/learning-outcomes", outcome).expectStatus().is2xxSuccessful();
    }

    @AfterEach
    void deleteCourse() {
        send(HttpMethod.DELETE, "/courses/" + courseId, null);
    }

    @Test
    void inlineTextsBecomeLearningOutcomes() {
        send(HttpMethod.GET, "/courses/" + courseId, null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.learningOutcomeIds").value(hasSize(1))
                .jsonPath("$.learningOutcomes").doesNotExist();

        send(HttpMethod.GET, "/courses/" + courseId + "?expand=learning_outcomes", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.learningOutcomes[0].learningOutcomeId").exists()
                .jsonPath("$.learningOutcomes[0].name[0].value").exists();

        // lists honour the expand too
        send(HttpMethod.GET, "/courses?primaryCode=x-none&expand=learning_outcomes", null).expectStatus().isOk();
    }

    @Test
    void courseLinksLearningOutcomeByIdAndHierarchyWorks() {
        String child = UUID.randomUUID().toString();
        send(HttpMethod.PUT, "/learning-outcomes/" + child, """
                {
                  "primaryCode": {"codeType": "identifier", "code": "%s"},
                  "name": [{"language": "en-GB", "value": "Choose a t-test"}],
                  "parentIds": ["%s"]
                }
                """.formatted(TU.genRandomCode(), outcomeId)).expectStatus().is2xxSuccessful();

        send(HttpMethod.GET, "/learning-outcomes/" + outcomeId + "?expand=children", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.childIds[0]").isEqualTo(child)
                .jsonPath("$.children[0].learningOutcomeId").isEqualTo(child)
                .jsonPath("$.complexityLevel").isEqualTo("bloom_3");

        send(HttpMethod.GET, "/learning-outcomes/" + child, null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.parentIds[0]").isEqualTo(outcomeId);

        // the course now points to the two outcomes by id
        String course = new String(send(HttpMethod.GET, "/courses/" + courseId + "?consumer=all", null)
                .expectStatus().isOk().expectBody().returnResult().getResponseBody());
        ObjectNode node = (ObjectNode) mapper.readTree(course);
        node.putArray("learningOutcomeIds").add(outcomeId).add(child);
        String updated = mapper.writeValueAsString(node);
        send(HttpMethod.PUT, "/courses/" + courseId, updated).expectStatus().isOk();

        send(HttpMethod.GET, "/courses/" + courseId, null)
                .expectBody()
                .jsonPath("$.learningOutcomeIds").value(hasSize(2));

        // an unknown id is rejected
        send(HttpMethod.PUT, "/courses/" + courseId, updated.replace(child, UUID.randomUUID().toString()))
                .expectStatus().is4xxClientError();
    }

    @Test
    void consumerIsChosenWithTheQueryParameter() {
        send(HttpMethod.GET, "/courses/" + courseId, null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.consumers").doesNotExist()
                .jsonPath("$.consumer").doesNotExist();

        send(HttpMethod.GET, "/courses/" + courseId + "?consumer=UNITA", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.consumer.consumerKey").isEqualTo("UNITA")
                .jsonPath("$.consumers").doesNotExist();

        send(HttpMethod.GET, "/courses/" + courseId + "?consumer=all", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.consumers[0].consumerKey").isEqualTo("UNITA");

        // lists follow the same rule
        send(HttpMethod.GET, "/courses?consumer=UNITA", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].consumers").doesNotExist();
    }

    @Test
    void singleConsumerIsMergedIntoTheStoredOnes() {
        String course = new String(send(HttpMethod.GET, "/courses/" + courseId + "?consumer=all", null)
                .expectStatus().isOk().expectBody().returnResult().getResponseBody());
        // replace the consumers list by a single v6 consumer for another key
        ObjectNode node = (ObjectNode) mapper.readTree(course);
        node.remove("consumers");
        node.putObject("consumer").put("consumerKey", "rio");
        String updated = mapper.writeValueAsString(node);
        send(HttpMethod.PUT, "/courses/" + courseId, updated).expectStatus().isOk();

        send(HttpMethod.GET, "/courses/" + courseId + "?consumer=all", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.consumers[?(@.consumerKey == 'UNITA')]").exists()
                .jsonPath("$.consumers[?(@.consumerKey == 'rio')]").exists();
    }
}
