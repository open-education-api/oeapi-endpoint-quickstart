package oeapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import oeapi.repository.OrganisationRepository;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class KomTest {
    Logger logger = LoggerFactory.getLogger(KomTest.class);

    @Autowired
    MockMvc mockMvc;

    @Autowired
    OrganisationRepository organisationRepository;

    ObjectMapper mapper = new ObjectMapper();

    static final String OEAPI_JSON = "application/vnd.oeapi+json;version=6.0";

    @Test
    void all() throws Exception {
        doAll("/organisations", "organisation");
        doAll("/persons", "person");
        doAll("/programmes", "programme", "parent");
        doAll("/programmes", "programme");
        doAll("/academic-sessions", "academicSession");
        doAll("/courses", "course");
        doAll("/programme-offerings", "programmeOffering", "offering");

        logger.info("GET /programme/{id}?expand=organisation");
        mockMvc.perform(get("/programmes/" + extractId("programme") + "?expand=organisation"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(OEAPI_JSON))
            .andExpect(jsonPath("$.organisation.organisationType").value("root"));
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
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(OEAPI_JSON));
    }

    private void doGet(String resourcePath, String resource, String altResource) throws Exception {
        String content = slurp("kom/" + altResource + ".json");
        String id = extractId(content, resource);
        // consumer=all (implementation extension) returns the stored consumers list as posted
        String path = resourcePath + "/" + id + "?consumer=all";

        logger.info("GET {}", path);

        String json = mockMvc.perform(get(path))
            .andExpect(status().isOk())
            .andExpect(content().contentType(OEAPI_JSON))
            .andReturn().getResponse().getContentAsString();

        assertEquals(mapper.readTree(content), mapper.readTree(json));
    }

    private String extractId(String content, String resource) throws Exception {
        return mapper.readTree(content).at("/" + resource + "Id").asString();
    }

    private String extractId(String resource) throws Exception {
        String content = slurp("kom/" + resource + ".json");
        return extractId(content, resource);
    }

    private String slurp(String path) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
