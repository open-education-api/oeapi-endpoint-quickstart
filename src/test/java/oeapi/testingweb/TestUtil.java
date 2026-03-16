package oeapi.testingweb;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author itziar.urrutia
 */

@Component
public class TestUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtil.class); 
    
    @Value("${ooapi.static.security.enabled:true}")
    private boolean securityEnabled;
    
    @Value("${app.static.token.allow:true}")
    private boolean staticTokenAllowed;
    
    @Value("${app.static.token.value}")
    private String staticTokenValue;    
    
    public String authHeaderForTest() {
        
        LOGGER.debug("authHeaderForTest: Security (e,t,v)? "+securityEnabled+", "+staticTokenAllowed+", "+staticTokenValue);
        
        // For now we will rely on app token if secutity enabled
        
        String headerAuth = "None" ; // Default

        if(securityEnabled && staticTokenAllowed) {
        headerAuth = "Bearer "+staticTokenValue;
        }        
     
      return headerAuth;   
    }

    public String genRandomCode() {
        Random r = new Random(System.currentTimeMillis());
        return "" + ((1 + r.nextInt(2)) * 10000 + r.nextInt(10000));
    }

    
    public String genRandomValue(String enumName) {

        String filePath = "schemas/enum/" + enumName + ".yml";

        //InputStream inputStream = getClass().getClassLoader().getResourceAsStream();
        Yaml yaml = new Yaml();
        // Check if the file exists

        Resource resource = new ClassPathResource(filePath);
        // Get the InputStream
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, List<String>> loadEnums = yaml.load(inputStream);
            List<String> values = loadEnums.get("enum");
            if (values == null || values.isEmpty()) {
                throw new IllegalStateException("No values available for " + enumName);
            }
            Random random = new Random();
            return values.get(random.nextInt(values.size())); // Get a random value
        } catch (Exception e) {
        }

        return "";
    }

    public String getPayload(String templateJson, String templateAbrev, String randomId, String randomCode) throws IOException {

        String templatePayload = new String(Files.readAllBytes(Paths.get("src/test/resources/" + templateJson + ".json")));

        String payload = templatePayload.replace("--" + templateAbrev + "_ID_TOBEINFORMED--", randomId)
                .replace("--" + templateAbrev + "_CODE_TOBEINFORMED--", randomCode);
        return payload;
    }

    

}
