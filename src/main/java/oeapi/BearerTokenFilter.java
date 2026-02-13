package oeapi;

import java.io.IOException;
import java.util.Collections;

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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import oeapi.service.CustomUserDetailsService;

@Component
public class BearerTokenFilter extends OncePerRequestFilter {
    private static final Logger logger =
        LoggerFactory.getLogger(BearerTokenFilter.class);

    @Autowired
    private JwtTokenService jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Value("${app.jwt.static.token.value}")
    private String staticTokenValue;

    @Value("${app.jwt.static.token.user:token_user}")
    private String staticTokenUser;

    @Value("${app.jwt.static.token.role:ROLE_USER}")
    private String staticTokenRole;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();

            // handle static token
            if (token.equals(staticTokenValue)) {
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(staticTokenUser, null, Collections.singletonList(new SimpleGrantedAuthority(staticTokenRole)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            // handle JWT token
            else if (jwtUtil.validateAccessToken(token)) {
                try {
                    String subject = jwtUtil.extractSubject(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception ex) {
                    logger.debug("Bad JWT: {}", ex.getMessage(), ex);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bad JWT");
                    return;
                }
            }

            // bad token causes immediate response
            else {
                logger.debug("Bad token");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bad token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
