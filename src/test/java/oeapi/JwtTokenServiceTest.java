package oeapi;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {
    UserDetails user = User.withUsername("dummy")
        .password("pass")
        .roles("admin")
        .build();

    @Test void generateAccessToken() {
        JwtTokenService jwt = new JwtTokenService();
        // all JWTs start with "ey"
        assertTrue(jwt.generateAccessToken(user).startsWith("ey"));
    }

    @Test void validateAccessToken() {
        JwtTokenService jwt = new JwtTokenService();
        String token = jwt.generateAccessToken(user);
        assertTrue(jwt.validateAccessToken(token));

        // different secret key
        JwtTokenService jwt2 = new JwtTokenService();
        assertFalse(jwt2.validateAccessToken(token));
    }

    @Test void extractUsername() {
        JwtTokenService jwt = new JwtTokenService();
        String token = jwt.generateAccessToken(user);
        assertEquals(user.getUsername(), jwt.extractUsername(token));
    }
}
