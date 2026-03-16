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

    @Value("${ooapi.security.mode:restricted}")
    private String endpointSecMode;

    @Value("${app.static.token.allow:false}")
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

        http.authorizeHttpRequests(auth -> {

            // 1. Always‑public endpoints (apply in all modes)
            auth.requestMatchers(HttpMethod.GET, "/auth/secStatus").permitAll();
            auth.requestMatchers(HttpMethod.GET, "/auth/secMode").permitAll();

            auth.requestMatchers(HttpMethod.GET, "/*.html").permitAll();
            auth.requestMatchers(HttpMethod.GET, "/css/*").permitAll();
            auth.requestMatchers(HttpMethod.GET, "/img/*").permitAll();
            auth.requestMatchers(HttpMethod.GET, "/js/*").permitAll();
            auth.requestMatchers(HttpMethod.GET, "/_quickdashboard_config.json").permitAll();

            // 2. Mode‑specific logic
            switch (endpointSecMode.toLowerCase()) {

                case "guest":
                   // In guest mode the endpoint is only readable, no updates are possible       
                    auth.requestMatchers(HttpMethod.GET, "/**").permitAll();

                    auth.requestMatchers(HttpMethod.POST, "/**").denyAll();
                    auth.requestMatchers(HttpMethod.PUT, "/**").denyAll();
                    auth.requestMatchers(HttpMethod.DELETE, "/**").denyAll();
                    break;


                case "restricted":
                     // In restricted mode the endpoint is readable but need authorization for updates
                    auth.requestMatchers(HttpMethod.GET, "/**").permitAll();

                    auth.requestMatchers("/auth/login").permitAll();
                    auth.requestMatchers("/auth/signup").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.POST, "/**").hasAnyRole("ADMIN", "USER");
                    auth.requestMatchers(HttpMethod.PUT, "/**").hasAnyRole("ADMIN", "USER");
                    auth.requestMatchers(HttpMethod.DELETE, "/**").hasAnyRole("ADMIN", "USER");

                    auth.anyRequest().authenticated();
                    break;


                case "private":
                    // Everything requires authentication except login/signup
                    auth.requestMatchers("/auth/login").permitAll();
                    auth.requestMatchers("/auth/signup").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.POST, "/**").hasAnyRole("ADMIN", "USER");
                    auth.requestMatchers(HttpMethod.PUT, "/**").hasAnyRole("ADMIN", "USER");
                    auth.requestMatchers(HttpMethod.DELETE, "/**").hasAnyRole("ADMIN", "USER");            

                    auth.anyRequest().authenticated();
                    break;


                case "none":
                    // No security at all
                    auth.anyRequest().permitAll();
                    break;

                default:
                    // if mode is not one of the allowed modes, default to private
                    auth.requestMatchers("/auth/login").permitAll();
                    auth.requestMatchers("/auth/signup").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.POST, "/**").hasAnyRole("ADMIN", "USER");
                    auth.requestMatchers(HttpMethod.PUT, "/**").hasAnyRole("ADMIN", "USER");
                    auth.requestMatchers(HttpMethod.DELETE, "/**").hasAnyRole("ADMIN", "USER");            

                    auth.anyRequest().authenticated();
                    break;                    

            }
        });
        
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
