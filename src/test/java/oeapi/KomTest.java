package oeapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import oeapi.repository.OrganizationRepository;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class KomTest {
    Logger logger = LoggerFactory.getLogger(KomTest.class);

    @Autowired
    MockMvc mockMvc;

    @Autowired
    OrganizationRepository organizationRepository;

    @Test
    void all() throws Exception {
        doPost("/organizations", "organization");
        doPost("/persons", "person");
        doPost("/programs", "parent");
        doPost("/programs", "program");
        doPost("/academic-sessions", "academic-session");
        doPost("/courses", "course");
        doPost("/offerings", "offering");
    }

    private void doPost(String path, String resource) throws Exception {
        logger.info("POST {}", path);

        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(slurp("kom/" + resource + ".json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    private String slurp(String path) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
