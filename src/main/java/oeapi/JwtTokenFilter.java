package oeapi;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static oeapi.oeapiUtils.sendJsonError;

import oeapi.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenFilter.class);

    @Value("${ooapi.security.enabled:false}")
    private boolean securityEnabled;
    
    @Value("${app.jwt.static.token.allow:false}")
    private boolean staticTokenAllowed;

    @Value("${app.jwt.static.token.value:9018313!!!038_13291jsdaujtexsksdh}")
    private String staticTokenValue;

    @Value("${app.jwt.static.token.user:token_user}")
    private String staticTokenUser;
    
    @Value("${app.jwt.static.token.role:ROLE_USER}")
    private String staticTokenRole;    
    
    
    @Autowired
    private JwtTokenService jwtUtil;
    @Autowired
    private CustomUserDetailsService userDetailsService;

    private static final Set<String> SECURED_METHODS = new HashSet<>(Arrays.asList("POST", "PUT", "DELETE"));

    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        LOGGER.debug("Security enabled: {}", securityEnabled);

        // If security is disabled, skip everything
        if (!securityEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        // Allow public endpoints (login, GET requests)
        String uri = request.getRequestURI().toLowerCase();
        if (uri.contains("auth/login") || request.getMethod().equalsIgnoreCase("GET")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Only secure certain methods (e.g., POST/PUT/DELETE)
        if (!SECURED_METHODS.contains(request.getMethod().toUpperCase())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.warn("Missing or invalid Authorization header");
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7).trim();
        boolean authenticated = false;

        try {
            // Static token support
            if (staticTokenAllowed && token.equals(staticTokenValue)) {
                LOGGER.debug("Authenticated using static token");
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                staticTokenUser, null,
                                Collections.singletonList(new SimpleGrantedAuthority(staticTokenRole))
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
                authenticated = true;
            }

            // JWT token validation
            if (!authenticated && jwtUtil.validateAccessToken(token)) {
                String username = jwtUtil.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                authenticated = true;
            }

        } catch (Exception ex) {
            LOGGER.error("JWT processing error: {}", ex.getMessage(), ex);
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        if (!authenticated) {
            LOGGER.debug("Unauthorized User");
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: token invalid or expired");
            return;
        }

        // Authentication succeeded
        filterChain.doFilter(request, response);
    }    
    
    
    
    
    
    
    
    


    
    
    
}