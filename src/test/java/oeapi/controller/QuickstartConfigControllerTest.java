package oeapi.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class QuickstartConfigControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    void getQuickstartConfigWithCallback() throws Exception {
        mockMvc.perform(get("/_quickdashboard_config.json").param("callback", "myCallback"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/javascript;charset=UTF-8"))
            .andExpect(content().string(startsWith("myCallback(")))
            .andExpect(content().string(containsString("\"ooapiDefaultCountry\":")));
    }

    @Test
    void getQuickstartConfigWithoutCallback() throws Exception {
        mockMvc.perform(get("/_quickdashboard_config.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ooapiDefaultCountry").value("EN"))
            .andExpect(jsonPath("$.ooapiDefaultEndpointURL").value(""));
    }
}
