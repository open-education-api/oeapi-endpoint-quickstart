package oeapi.controller.admin;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import oeapi.model.Role;
import oeapi.model.User;
import oeapi.repository.RoleRepository;
import oeapi.repository.UserRepository;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    ObjectMapper mapper = new ObjectMapper();

    private User user;
    private Role role;
    private Role otherRole;

    @BeforeEach
    void beforeEach() {
        roleRepository.deleteAll();
        role = roleRepository.save(new Role("test"));
        otherRole = roleRepository.save(new Role("yelp"));

        userRepository.deleteAll();
        user = new User("fred@example.com", "wilma");
        user.setRoles(Arrays.asList(role));
        user = userRepository.save(user);
    }

    @Test
    void testGetAll() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].email").value(user.getEmail()))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void testGetAllWithoutUsers() throws Exception {
        userRepository.deleteAll();

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetById() throws Exception {
        mockMvc.perform(get("/admin/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.roles", hasSize(user.getRoles().size())))
                .andExpect(jsonPath("$.roles[0].name").value(user.getRoles().get(0).getName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testPost() throws Exception {
        User newUser = new User("wilma@example.com", "fred");
        newUser.setRoles(Arrays.asList(role));

        String json = mockMvc
                .perform(post("/admin/users").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(newUser.getEmail()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = mapper.readTree(json);
        int id = node.at("/id").asInt();
        Optional<User> maybeUser = userRepository.findById(id);
        assertTrue(maybeUser.isPresent(), "user saved");

        User createdUser = maybeUser.get();
        assertTrue(passwordEncoder.matches(newUser.getPassword(), createdUser.getPassword()), "password properly encoded");

        List<Role> roles = createdUser.getRoles();
        assertTrue(roles.size() == 1, "roles added");
    }

    @Test
    void testPostExisting() throws Exception {
        mockMvc.perform(post("/admin/users").contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(user)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testPut() throws Exception {
        User updateUser = new User("barney@example.com", null);
        updateUser.setRoles(Arrays.asList(role, otherRole));

        String json = mockMvc
                .perform(put("/admin/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(updateUser.getEmail()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = mapper.readTree(json);
        int id = node.at("/id").asInt();
        assertTrue(id == user.getId(), "updated correct user");

        User updatedUser = userRepository.findById(id).get();

        List<Role> roles = updatedUser.getRoles();
        assertTrue(roles.size() == 2, "roles added");
    }

    @Test
    void testPutWithRoleIdsOnly() throws Exception {
        User updateUser = new User("barney@example.com", null);
        updateUser.setRoles(Arrays.asList(new Role(otherRole.getId())));

        String json = mockMvc
                .perform(put("/admin/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(updateUser.getEmail()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = mapper.readTree(json);
        int id = node.at("/id").asInt();
        assertTrue(id == user.getId(), "updated correct user");

        User updatedUser = userRepository.findById(id).get();

        List<Role> roles = updatedUser.getRoles();
        assertTrue(roles.size() == 1, "roles changed");
        assertEquals(otherRole, roles.get(0), "correct role");
    }

    @Test
    void testDelete() throws Exception {
        Optional<User> maybeUser = userRepository.findById(user.getId());
        assertTrue(maybeUser.isPresent(), "user exists");

        mockMvc.perform(delete("/admin/users/{id}", user.getId()))
                .andExpect(status().isOk());

        maybeUser = userRepository.findById(user.getId());
        assertTrue(maybeUser.isEmpty(), "user deleted");
    }
}
