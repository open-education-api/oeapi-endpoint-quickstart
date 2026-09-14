package oeapi.testingweb;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 *
 * @author itziar.urrutia
 */

@Component
public class TestUtilCUDRest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtilCUDRest.class); 

    @Autowired
    private TestUtil TU;

    /**
     * Everything created through this helper, most recent first.
     *
     * Tests used to leave their courses, programs, organizations and persons in the
     * database. That is invisible in CI, where every job gets a fresh database, but it
     * accumulates against a development database and eventually breaks the suite: a
     * retained row keeps its primaryCode, and create() refuses a duplicate one.
     *
     * A test class ends with cleanupCreated(webTestClient) in @AfterAll and everything it
     * created through this helper goes away - including when the test failed halfway.
     * This bean is a singleton in the test context, so anything a class forgets to clean
     * is cleaned by the next class that does.
     */
    private final Deque<String[]> created = new ArrayDeque<>();

    /** Records an entity for teardown. Public so tests that POST directly can register theirs. */
    public synchronized String track(String restResource, String id) {
        if (id != null) {
            created.push(new String[]{restResource, id});
        }
        return id;
    }

    private synchronized void untrack(String restResource, String id) {
        created.removeIf(e -> e[0].equals(restResource) && e[1].equals(id));
    }

    /**
     * Best-effort teardown, children before parents (most recent first). Never asserts:
     * a failed delete must not turn a passing run red, it is logged and the run continues.
     */
    public synchronized void cleanupCreated(WebTestClient webTestClient) {
        while (!created.isEmpty()) {
            String[] entity = created.pop();
            deleteQuiet(entity[0], entity[1], webTestClient);
        }
    }

    /** DELETE without any status assertion - for teardown, where the entity may already be gone. */
    public void deleteQuiet(String restResource, String id, WebTestClient webTestClient) {
        if (id == null) {
            return;
        }
        untrack(restResource, id);
        try {
            webTestClient.delete()
                    .uri("/" + restResource + "/" + id)
                    .header("Authorization", TU.authHeaderForTest())
                    .exchange();
        } catch (RuntimeException ex) {
            LOGGER.debug("cleanup: could not delete {}/{}: {}", restResource, id, ex.getMessage());
        }
    }

    public  String whenPost_testOk(String restResource, String entity, String templateAbrev, WebTestClient webTestClient) throws IOException {
        String randomId = UUID.randomUUID().toString();
        String randomCode = TU.genRandomCode();
        String payload = TU.getPayload(entity + "_template", templateAbrev, randomId, randomCode);
        post_testOk(restResource, payload, webTestClient);
        return track(restResource, randomId);
    }

    public  void whenPost_test(String restResource, String entity, String templateAbrev, String id, WebTestClient webTestClient) throws IOException {
        String randomCode = TU.genRandomCode();
        String payload = TU.getPayload(entity + "_template", templateAbrev, id, randomCode);

        post_test(restResource, entity, payload, id, randomCode, webTestClient);

    }

    public void whenPost_test(String restResource, String entity, String templateAbrev, WebTestClient webTestClient) throws IOException {
        String randomId = UUID.randomUUID().toString();
        whenPost_test(restResource, entity, templateAbrev, randomId, webTestClient);
    }

    public  String whenPost_testId(String restResource, String entity, String templateAbrev, WebTestClient webTestClient) throws IOException {
        String randomId = UUID.randomUUID().toString();
        String randomCode = TU.genRandomCode();

        String payload = TU.getPayload(entity + "_template", templateAbrev, randomId, randomCode);

        post_testId(restResource, entity, payload, randomId, webTestClient);

        return randomId;

    }

    public  String whenPost_testCode(String restResource, String entity, String templateAbrev, WebTestClient webTestClient) throws IOException {
        String randomId = UUID.randomUUID().toString();
        String randomCode = TU.genRandomCode();

        String payload = TU.getPayload(entity + "_template", templateAbrev, randomId, randomCode);

        post_testCode(restResource, payload, randomCode, webTestClient);

        track(restResource, randomId);

        return randomCode;

    }

    public String whenPut_testUpdateCode(String restResource, String entity, String templateAbrev, String randomId, WebTestClient webTestClient) throws IOException {

        String newCode = TU.genRandomCode();

        String payload = TU.getPayload("update" + entity + "_template", templateAbrev, randomId, newCode);

        String uri = "/" + restResource + "/" + randomId;

        put_testCode(uri, payload, newCode, webTestClient);

        return newCode;
    }

    public void post_testCode(String restResource, String payload, String code, WebTestClient webTestClient) {
        webTestClient.post()
                .uri("/" + restResource)
                .header("Authorization",TU.authHeaderForTest())                
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.primaryCode.code").isEqualTo(code);
    }

    public void put_testCode(String uri, String payload, String newCode, WebTestClient webTestClient) {
        webTestClient.put()
                .uri(uri)
                .header("Authorization",TU.authHeaderForTest())                
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.primaryCode.code").isEqualTo(newCode);
    }

    public void post_testOk(String restResource, String payload, WebTestClient webTestClient) {
        webTestClient.post()
                .uri("/" + restResource)
                .header("Authorization",TU.authHeaderForTest())                
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk();

    }

    public void post_testId(String restResource, String entity, String payload, String id, WebTestClient webTestClient) {
        webTestClient.post()
                .uri("/" + restResource)
                .header("Authorization",TU.authHeaderForTest())                
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$." + entity + "Id").isEqualTo(id);

        track(restResource, id);
    }

    public void post_test(String restResource, String entity, String payload, String id, String code, WebTestClient webTestClient) {
        webTestClient.post()
                .uri("/" + restResource)
                .header("Authorization",TU.authHeaderForTest())                
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$." + entity + "Id").isEqualTo(id)
                .jsonPath("$.primaryCode.code").isEqualTo(code);

        track(restResource, id);
    }

    public void delete_test(String restResource, String id, WebTestClient webTestClient) {

        untrack(restResource, id);

        
        LOGGER.debug("delete_test deleting... (DELETE) params: "+restResource+", "+id+" ,"+webTestClient);
        
        webTestClient.delete()
                .uri("/" + restResource + "/" + id)
                .header("Authorization",TU.authHeaderForTest())                
                .exchange()
                .expectStatus().isOk();

        LOGGER.debug("delete_test check if is actualy deleted (GET)... params: "+restResource+", "+id+" ,"+webTestClient);

        webTestClient.get()
                .uri("/" + restResource + "/" + id)
                .header("Authorization",TU.authHeaderForTest())                
                .exchange()
                .expectStatus().isNotFound()
                .expectBody();

    }
           
}
