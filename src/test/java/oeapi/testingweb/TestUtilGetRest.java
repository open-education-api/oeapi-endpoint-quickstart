/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.testingweb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 *
 * @author itziar.urrutia
 */
public class TestUtilGetRest {

    private static Integer pageSize = 3;

    public static void get_primaryCode(String restResource, String code, WebTestClient webTestClient) {

        webTestClient.get()
                .uri("/" + restResource + "?pageNumer=0&primaryCode=" + code)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].primaryCode.code").isEqualTo(code);
    }

    public static void get_filter(String restResource, Map<String, String> filterMap, WebTestClient webTestClient) throws UnsupportedEncodingException {

        for (Map.Entry<String, String> entry : filterMap.entrySet()) {
            String filter = entry.getKey();  // This would be the level filter
            String filterValue = entry.getValue();
            String encodedValue = URLEncoder.encode(filterValue, "UTF-8");

            webTestClient.get()
                    .uri("/" + restResource + "?pageSize=" + pageSize + "&" + filter + "=" + encodedValue)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.totalPages").value(totalPages -> {
                // Assert that totalItems is greater than or equal to 0
                assertTrue((Integer) totalPages >= 0, "The totalItems should be greater than or equal to 0.");
            })
                    .jsonPath("$.items[*]." + filter).value(levels -> {
                List<String> levelList = (List<String>) levels;
                levelList.forEach(level -> {
                    assert level.equals(filterValue);
                });
            });
        }
    }

    public static void get_filterList(String restResource, Map<String, List<String>> filterMap, WebTestClient webTestClient) {
        for (Map.Entry<String, List<String>> entry : filterMap.entrySet()) {
            String filter = entry.getKey();  // This would be the level filter
            List<String> filterValue = entry.getValue();

            webTestClient.get()
                    .uri("/" + restResource + "?pageSize=" + pageSize + "&" + filter + "=" + filterValue)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.totalPages").value(totalPages -> {
                // Assert that totalItems is greater than or equal to 0
                assertTrue((Integer) totalPages >= 0, "The totalItems should be greater than or equal to 0.");
            })
                    .jsonPath("$.items[*]." + filter).value(levels -> {
                List<String> levelList = (List<String>) levels;
                levelList.forEach(level -> {
                    assert level.equals(filterValue);
                });
            });
        }
    }

    public static void getPages_testTotalPages(String uri, WebTestClient webTestClient) {
        webTestClient.get()
                .uri(uri + "?pageSize=" + pageSize)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalPages").value(totalPages -> {
            // Assert that totalItems is greater than or equal to 0
            assertTrue((Integer) totalPages >= 0, "The totalItems should be greater than or equal to 0.");
        });
    }

}
