package oeapi.testingweb;

import java.io.IOException;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    
    public  String whenPost_testOk(String restResource, String entity, String templateAbrev, WebTestClient webTestClient) throws IOException {
        String randomId = UUID.randomUUID().toString();
        String randomCode = TU.genRandomCode();
        String payload = TU.getPayload(entity + "_template", templateAbrev, randomId, randomCode);
        post_testOk(restResource, payload, webTestClient);
        return randomId;
    }

    public  void whenPost_test(String restResource, String entity, String templateAbrev, WebTestClient webTestClient) throws IOException {
        String randomId = UUID.randomUUID().toString();
        String randomCode = TU.genRandomCode();
        String payload = TU.getPayload(entity + "_template", templateAbrev, randomId, randomCode);

        post_test(restResource, entity, payload, randomId, randomCode, webTestClient);

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

        return randomCode;

    }

    public  void whenPut_testUpdateCode(String restResource, String entity, String templateAbrev, String randomId, WebTestClient webTestClient) throws IOException {

        String newCode = TU.genRandomCode();

        String payload = TU.getPayload(entity + "update" + entity + "_template", templateAbrev, randomId, newCode);

        String uri = "/" + restResource + "/" + randomId;

        put_testCode(uri, payload, newCode, webTestClient);

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
    }

    public void delete_test(String restResource, String id, WebTestClient webTestClient) {
        
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
