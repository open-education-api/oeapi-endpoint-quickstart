/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.testingweb;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author itziar.urrutia
 */
public class TestUtil {

    public static String genRandomCode() {
        Random r = new Random(System.currentTimeMillis());
        return "" + ((1 + r.nextInt(2)) * 10000 + r.nextInt(10000));
    }

    public static String genRandomValue(String enumName) {

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

    public static String getPayload(String templateJson, String templateAbrev, String randomId, String randomCode) throws IOException {

        String templatePayload = new String(Files.readAllBytes(Paths.get("src/test/resources/" + templateJson + ".json")));

        String payload = templatePayload.replace("--" + templateAbrev + "_ID_TOBEINFORMED--", randomId)
                .replace("--" + templateAbrev + "_CODE_TOBEINFORMED--", randomCode);
        return payload;
    }

}
