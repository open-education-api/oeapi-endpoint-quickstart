package oeapi;

/**
 *
 * @author Carlos Alonso <losalo@unavarra.es>
 */
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class oeapiCorsConfig {

    Logger logger = LoggerFactory.getLogger(oeapiCorsConfig.class);

    @Value("${ooapi.cors.allowed.origins:'localhost'}")
    private String allowedOrigins;

    // CORS control. You can allow certain clients or all  (CORS is not allowed by default)
    // When proxied, You can also control this in the apache o nginx frontend without changing this code.
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {

                String[] corsEnabledFrom = allowedOrigins.split(",");

                logger.info("\n\n--------> CORS requests are enabled from: " + Arrays.toString(corsEnabledFrom) + "\n\n");
                //System.out.println("\n>---------> CORS requests are enabled from: "+Arrays.toString(corsEnabledFrom)+"\n");

                registry.addMapping("/**") // allow all endpoints
                        .allowedOrigins(corsEnabledFrom)
                        //.allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // allow all methods
                        .allowedHeaders("*"); // allow all headers

                /* In case to fine tune access to specific endpoints, more rules can be added
               The order doesn’t matter, because Spring matches the most specific path

                registry.addMapping("/api/public/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST")
                        .allowedHeaders("*");

                 */
                //registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
            }

        };
    }
}
