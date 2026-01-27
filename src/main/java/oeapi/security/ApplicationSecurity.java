package oeapi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import oeapi.BearerTokenFilter;
import oeapi.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class ApplicationSecurity {
    @Autowired
    private BearerTokenFilter tokenFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Value("${ooapi.security.enabled:false}")
    private boolean securityEnabled;

    @Value("${ooapi.security.public-access:true}")
    private boolean publicAccess = true;

    @Value("${app.jwt.static.token.allow:false}")
    private boolean staticTokenAllowed;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults());

        if (!securityEnabled) {
            return http.build();
        }

        http.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        http.sessionManagement(x -> x.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(x -> x
                                   // anybody can login
                                   .requestMatchers("/auth/login").permitAll()

                                   // admin can add users
                                   .requestMatchers("/auth/signup").hasRole("ADMIN")

                                   // edits for users and admins
                                   .requestMatchers(HttpMethod.POST).hasAnyRole("ADMIN", "USER")
                                   .requestMatchers(HttpMethod.PUT).hasAnyRole("ADMIN", "USER")
                                   .requestMatchers(HttpMethod.DELETE).hasAnyRole("ADMIN", "USER"));

        if (publicAccess) {
            http.authorizeHttpRequests(x -> x
                                       .requestMatchers(HttpMethod.GET).permitAll());
        } else {
            http.authorizeHttpRequests(x -> x
                                       // UI pages
                                       .requestMatchers(HttpMethod.GET, "/*.html").permitAll()
                                       .requestMatchers(HttpMethod.GET, "/css/*").permitAll()
                                       .requestMatchers(HttpMethod.GET, "/img/*").permitAll()
                                       .requestMatchers(HttpMethod.GET, "/js/*").permitAll()
                                       .requestMatchers(HttpMethod.GET, "/_quickdashboard_config.json").permitAll()

                                       // need to login for the rest
                                       .anyRequest().authenticated());
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }
}
