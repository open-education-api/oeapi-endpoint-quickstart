package oeapi.security;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import oeapi.model.Role;
import oeapi.model.User;
import oeapi.repository.RoleRepository;
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

        @Test
        void anonymousSecurityStatusOk() throws Exception {
            mockMvc.perform(get("/auth/secStatus")).andExpect(status().isOk());
        }

        @Test
        void statusOk() throws Exception {
            mockMvc.perform(get("/auth/status"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.mode").value("none"));
        }

        @Nested
        @TestPropertySource(properties = {"ooapi.security.mode=private"})
        class WithSecurityModeSet {
            @Test
            void statusOk() throws Exception {
                mockMvc.perform(get("/auth/status"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.mode").value("none"));
            }
        }
    }

    @Nested
    @TestPropertySource(properties = {"ooapi.security.enabled=true", "ooapi.security.mode=guest"})
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

        @Test
        void anonymousSecurityStatusOk() throws Exception {
            mockMvc.perform(get("/auth/secStatus")).andExpect(status().isOk());
        }

        @Test
        void statusOk() throws Exception {
            mockMvc.perform(get("/auth/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mode").value("guest"));
        }
    }

    @Nested
    @TestPropertySource(properties = {"ooapi.security.enabled=true", "ooapi.security.mode=private"})
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

        @Test
        void anonymousSecurityStatusOk() throws Exception {
            mockMvc.perform(get("/auth/secStatus")).andExpect(status().isOk());
        }

        @Test
        void statusOk() throws Exception {
            mockMvc.perform(get("/auth/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mode").value("private"));
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
            RoleRepository roleRepository;
            @Autowired
            PasswordEncoder passwordEncoder;

            @Test
            void anonymousLoginPostOk() throws Exception {
                User user = new User("dummy@example.com", passwordEncoder.encode("pass"));

                try {
                    userRepository.save(user);

                    mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"dummy@example.com\",\"password\":\"pass\"}"))
                            .andExpect(status().isOk());
                } finally {
                    userRepository.delete(user);
                }
            }

            @Nested
            class LoggedIn {
                Role role1 = new Role("foo");
                Role role2 = new Role("bar");
                User user = new User("dummy@example.com", passwordEncoder.encode("pass"));
                String token;

                @BeforeEach
                void beforeEach() throws Exception {
                    roleRepository.save(role1);
                    roleRepository.save(role2);
                    user.setRoles(List.of(role1, role2));
                    userRepository.save(user);

                    String json = mockMvc.perform(post("/auth/login")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content("{\"email\":\"dummy@example.com\",\"password\":\"pass\"}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.token").exists())
                        .andReturn().getResponse().getContentAsString();

                    token = (String) new ObjectMapper().readValue(json, Map.class).get("token");
                }

                @AfterEach
                void afterEach() {
                    userRepository.delete(user);
                    roleRepository.deleteAll(List.of(role1, role2));
                }

                @Test
                void statusOk() throws Exception {
                    mockMvc.perform(get("/auth/status").header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.mode").value("private"))
                        .andExpect(jsonPath("$.current.email").value(user.getEmail()))
                        .andExpect(jsonPath("$.current.roles", containsInAnyOrder(role1.getName(), role2.getName())));
                }
            }
        }
    }
}
