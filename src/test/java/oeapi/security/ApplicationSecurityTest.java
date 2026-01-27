package oeapi.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import oeapi.model.User;
import oeapi.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ApplicationSecurityTest {

    @Nested
    @TestPropertySource(properties = {"ooapi.security.enabled=false"})
    class WithoutSecurity {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @WithMockUser(username = "admin", roles = "ADMIN")
        void adminApiOk() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "user", roles = "USER")
        void userApiOk() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "guest", roles = "GUEST")
        void guestApiOk() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isOk());
        }

        @Test
        void anonymousApiOk() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isOk());
        }
    }

    @Nested
    @TestPropertySource(properties = {"ooapi.security.enabled=true", "ooapi.security.public-access=true"})
    class WithSecurityAndPublicAccess {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @WithMockUser(username = "admin", roles = "ADMIN")
        void adminApiOk() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "user", roles = "USER")
        void userApiOk() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "guest", roles = "GUEST")
        void guestApiOk() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isOk());
        }

        @Test
        void anonymousApiOk() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isOk());
        }
    }

    @Nested
    @TestPropertySource(properties = {"ooapi.security.enabled=true", "ooapi.security.public-access=false"})
    class WithSecurityWithoutPublicAccess {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @WithMockUser(username = "admin", roles = "ADMIN")
        void adminApiOk() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "user", roles = "USER")
        void userApiOk() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "guest", roles = "GUEST")
        void guestApiOk() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isOk());
        }

        @Test
        void anonymousApiForbidden() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isForbidden());
        }

        @Nested
        class Login {
            @Test
            void anonymousLoginPageOk() throws Exception {
                mockMvc.perform(get("/login.html")).andExpect(status().isOk());
                mockMvc.perform(get("/js/init.js")).andExpect(status().isOk());
                mockMvc.perform(get("/_quickdashboard_config.json?callback=init")).andExpect(status().isOk());
                mockMvc.perform(get("/js/oeapi.js")).andExpect(status().isOk());
            }

            @Test
            void anonymousLoginPostUnauthorized() throws Exception {
                // Note: when not accessible returns Forbidden to this proves access
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                    .andExpect(status().isUnauthorized());
            }

            @Autowired
            UserRepository userRepository;
            @Autowired
            PasswordEncoder passwordEncoder;

            @Test
            void anonymousLoginPostOk() throws Exception {
                User user = new User("dummy", passwordEncoder.encode("pass"));

                try {
                    userRepository.save(user);

                    mockMvc.perform(post("/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"email\":\"dummy\",\"password\":\"pass\"}"))
                        .andExpect(status().isOk());
                } finally {
                    userRepository.delete(user);
                }
            }
        }
    }
}
