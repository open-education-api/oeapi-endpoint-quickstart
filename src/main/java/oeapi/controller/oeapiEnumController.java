package oeapi.controller;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/enumerator")
public class oeapiEnumController {

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@RequestParam(name = "enum") String enumName) {

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
            return ResponseEntity.ok(values);
        } catch (Exception e) {
            return ResponseEntity.ok(null);
        }

    }

}
