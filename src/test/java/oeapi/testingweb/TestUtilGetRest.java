package oeapi.testingweb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 *
 * @author itziar.urrutia
 */

@Component
public class TestUtilGetRest {

    private static Integer pageSize = 3;
    
    @Autowired
    private TestUtil TU;
    
    public void get_primaryCode(String restResource, String code, WebTestClient webTestClient) {

        webTestClient.get()
                .uri("/" + restResource + "?pageNumer=0&primaryCode=" + code)
                .header("Authorization",TU.authHeaderForTest())                
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].primaryCode.code").isEqualTo(code);
    }

    public void get_filter(String restResource, Map<String, String> filterMap, WebTestClient webTestClient) throws UnsupportedEncodingException {

        for (Map.Entry<String, String> entry : filterMap.entrySet()) {
            String filter = entry.getKey();  // This would be the level filter
            String filterValue = entry.getValue();
            String encodedValue = URLEncoder.encode(filterValue, "UTF-8");

            webTestClient.get()
                    .uri("/" + restResource + "?pageSize=" + pageSize + "&" + filter + "=" + encodedValue)
                    .header("Authorization",TU.authHeaderForTest())                
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

    public void get_filterList(String restResource, Map<String, List<String>> filterMap, WebTestClient webTestClient) {
        for (Map.Entry<String, List<String>> entry : filterMap.entrySet()) {
            String filter = entry.getKey();  // This would be the level filter
            List<String> filterValue = entry.getValue();

            webTestClient.get()
                    .uri("/" + restResource + "?pageSize=" + pageSize + "&" + filter + "=" + filterValue)
                    .header("Authorization",TU.authHeaderForTest())                
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

    public void getPages_testTotalPages(String uri, WebTestClient webTestClient) {
        webTestClient.get()
                .uri(uri + "?pageSize=" + pageSize)
                .header("Authorization",TU.authHeaderForTest())                
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalPages").value(totalPages -> {
            // Assert that totalItems is greater than or equal to 0
            assertTrue((Integer) totalPages >= 0, "The totalItems should be greater than or equal to 0.");
        });
    }  

}
