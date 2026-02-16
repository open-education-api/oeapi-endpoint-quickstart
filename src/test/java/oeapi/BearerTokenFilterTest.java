package oeapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import oeapi.model.CustomUserDetails;
import oeapi.model.Role;
import oeapi.model.User;
import oeapi.repository.RoleRepository;
import oeapi.repository.UserRepository;

@SpringBootTest
public class BearerTokenFilterTest {
    @Autowired
    BearerTokenFilter filter;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    JwtTokenService tokenService;

    @Value("${app.jwt.static.token.value}")
    private String staticTokenValue;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    public void setup() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @Test
    public void noBearerToken() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        assertNotAuthenticated();
        assertContinueFilterChain();
    }

    @Test
    public void staticBearerToken() throws Exception {
        ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + staticTokenValue);
        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticated();
        assertContinueFilterChain();
    }

    @Test
    public void badJWT() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";
        ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + token);
        filter.doFilterInternal(request, response, filterChain);

        assertNotAuthenticated();
        assertUnauthorizedResponse();
    }

    @Test
    public void goodJWT() throws Exception {
        User user = new User("dummy", "pass");
        Role role = new Role("dummy");
        user.setRoles(List.of(role));

        try {
            roleRepository.save(role);
            userRepository.save(user);
            UserDetails userDetails = new CustomUserDetails(user);

            String token = tokenService.generateAccessToken(userDetails);
                ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + token);
            filter.doFilterInternal(request, response, filterChain);

            assertAuthenticated();
            assertContinueFilterChain();
        } finally {
            userRepository.delete(user);
            roleRepository.delete(role);
        }
    }

    void assertAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        assertTrue(auth.isAuthenticated());
    }

    void assertNotAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        assertNull(context.getAuthentication());
    }

    void assertContinueFilterChain() throws Exception {
        verify(filterChain).doFilter(request, response);
    }

    void assertUnauthorizedResponse() throws Exception {
        assertEquals(response.getStatus(), HttpServletResponse.SC_UNAUTHORIZED);

        // filter chain aborted
        verify(filterChain, never()).doFilter(any(), any());
    }
}
