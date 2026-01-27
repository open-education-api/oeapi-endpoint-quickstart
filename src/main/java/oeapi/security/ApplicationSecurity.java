package oeapi.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

import oeapi.JwtTokenFilter;
import oeapi.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class ApplicationSecurity {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationSecurity.class);

    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Value("${ooapi.security.public-access:true}")
    private boolean publicAccess = true;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(x -> x.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

            // // TODO drop this for @PreAuthorize("hasRole('ADMIN')")?
            // .authorizeHttpRequests(x -> x
            //                        .requestMatchers("/auth/signup").hasRole("ADMIN")
            //                        .requestMatchers("/auth/login").permitAll());

        // TODO add tests to verify calling authorizeHttpRequests actually works!
        logger.info("Allow public read only access: {}", publicAccess);
        if (publicAccess) {
            http.authorizeHttpRequests(x -> x.anyRequest().permitAll());
        } else {
            http.authorizeHttpRequests(x -> x
                                       .requestMatchers("/login.html").permitAll()
                                       .requestMatchers("/auth/login").permitAll()
                                       .requestMatchers("/js/*").permitAll()
                                       .requestMatchers("/_quickdashboard_config.json").permitAll()
                                       .anyRequest().authenticated());
        }


        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

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
