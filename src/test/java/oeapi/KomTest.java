package oeapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void all() throws Exception {
        doAll("/organizations", "organization");
        doAll("/persons", "person");
        doAll("/programs", "program", "parent");
        doAll("/programs", "program");
        doAll("/academic-sessions", "academicSession");
        doAll("/courses", "course");
        doAll("/offerings", "offering");
    }

    private void doAll(String path, String resource, String altResource) throws Exception {
        doPost(path, resource, altResource);
        doGet(path, resource, altResource);
    }

    private void doAll(String path, String resource) throws Exception {
        doAll(path, resource, resource);
    }

    private void doPost(String resourcePath, String resource, String altResource) throws Exception {
        String content = slurp("kom/" + altResource + ".json");

        logger.info("POST {}", resourcePath);
        mockMvc.perform(post(resourcePath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
    }

    private void doGet(String resourcePath, String resource, String altResource) throws Exception {
        String content = slurp("kom/" + altResource + ".json");
        String id = extractId(content, resource);
        String path = resourcePath + "/" + id;

        logger.info("GET {}", path);

        String json = mockMvc.perform(get(path))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andReturn().getResponse().getContentAsString();

        assertEquals(mapper.readTree(content), mapper.readTree(json));
    }

    private String extractId(String content, String resource) throws Exception {
        return mapper.readTree(content).at("/" + resource + "Id").asText();
    }

    private String slurp(String path) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
