/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.testingweb;

import java.io.IOException;

import java.util.UUID;
import oeapi.model.Course;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 *
 * @author itziar.urrutia
 */
public class TestUtilCUDRest {

    public static String whenPost_testOk(String restResource, String entity, String templateAbrev, WebTestClient webTestClient) throws IOException {
        String randomId = UUID.randomUUID().toString();
        String randomCode = TestUtil.genRandomCode();
        String payload = TestUtil.getPayload(entity + "_template", templateAbrev, randomId, randomCode);
        post_testOk(restResource, payload, webTestClient);
        return randomId;
    }

    public static void whenPost_test(String restResource, String entity, String templateAbrev, WebTestClient webTestClient) throws IOException {
        String randomId = UUID.randomUUID().toString();
        String randomCode = TestUtil.genRandomCode();
        String payload = TestUtil.getPayload(entity + "_template", templateAbrev, randomId, randomCode);

        post_test(restResource, entity, payload, randomId, randomCode, webTestClient);

    }

    public static String whenPost_testId(String restResource, String entity, String templateAbrev, WebTestClient webTestClient) throws IOException {
        String randomId = UUID.randomUUID().toString();
        String randomCode = TestUtil.genRandomCode();

        String payload = TestUtil.getPayload(entity + "_template", templateAbrev, randomId, randomCode);

        post_testId(restResource, entity, payload, randomId, webTestClient);

        return randomId;

    }

    public static String whenPost_testCode(String restResource, String entity, String templateAbrev, WebTestClient webTestClient) throws IOException {
        String randomId = UUID.randomUUID().toString();
        String randomCode = TestUtil.genRandomCode();

        String payload = TestUtil.getPayload(entity + "_template", templateAbrev, randomId, randomCode);

        post_testCode(restResource, payload, randomCode, webTestClient);

        return randomCode;

    }

    public static void whenPut_testUpdateCode(String restResource, String entity, String templateAbrev, String randomId, WebTestClient webTestClient) throws IOException {

        String newCode = TestUtil.genRandomCode();

        String payload = TestUtil.getPayload(entity + "update" + entity + "_template", templateAbrev, randomId, newCode);

        String uri = "/" + restResource + "/" + randomId;

        put_testCode(uri, payload, newCode, webTestClient);

    }

    public static void post_testCode(String restResource, String payload, String code, WebTestClient webTestClient) {
        webTestClient.post()
                .uri("/" + restResource)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.primaryCode.code").isEqualTo(code);
    }

    public static void put_testCode(String uri, String payload, String newCode, WebTestClient webTestClient) {
        webTestClient.put()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.primaryCode.code").isEqualTo(newCode);
    }

    public static void post_testOk(String restResource, String payload, WebTestClient webTestClient) {
        webTestClient.post()
                .uri("/" + restResource)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk();

    }

    public static void post_testId(String restResource, String entity, String payload, String id, WebTestClient webTestClient) {
        webTestClient.post()
                .uri("/" + restResource)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$." + entity + "Id").isEqualTo(id);
    }

    public static void post_test(String restResource, String entity, String payload, String id, String code, WebTestClient webTestClient) {
        webTestClient.post()
                .uri("/" + restResource)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$." + entity + "Id").isEqualTo(id)
                .jsonPath("$.primaryCode.code").isEqualTo(code);
    }

    public static void delete_test(String restResource, String id, WebTestClient webTestClient) {
        webTestClient.delete()
                .uri("/" + restResource + "/" + id)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/courses/" + id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody();

    }
}
