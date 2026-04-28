package oeapi.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import oeapi.model.Organization;
import oeapi.service.OrganizationService;

@RestController
public class QuickstartConfigController {
    @Value("${quickdashboard.config.ooapiDefaultCountry:EN}")
    private String ooapiDefaultCountry;

    @Value("${quickdashboard.config.ooapiDefaultLogo:./img/OpenEducationApi_Logo.png}")
    private String ooapiDefaultLogo;

    @Value("${quickdashboard.config.ooapiDefaultShortUnivName:dummy}")
    private String ooapiDefaultShortUnivName;

    @Value("${quickdashboard.config.ooapiDefaultUnivName:DUMMY}")
    private String ooapiDefaultUnivName;

    @Value("${quickdashboard.config.ooapiDefaultOrganizationId:78ca90e6-7257-4721-aae3-94addbed42fc}")
    private String ooapiDefaultOrganizationId;

    @Value("${quickdashboard.config.ooapiDefaultEndpointURL:http://localhost:57075}")
    private String ooapiDefaultEndpointURL;

    @Value("${quickdashboard.config.ooapiDefaultCourseConsumersJsonFile:}")
    private String ooapiDefaultCourseConsumersJsonFile;

    @Value("${quickdashboard.config.ooapiDefaultOfferingConsumersJsonFile:}")
    private String ooapiDefaultOfferingConsumersJsonFile;

    @Autowired
    private OrganizationService organizationService;

    private final ObjectMapper objectMapper;

    public QuickstartConfigController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/_quickdashboard_config.json",
                    method = RequestMethod.GET,
                    produces = "application/javascript")
    @ResponseBody
    public String getQuickstartConfig(@RequestParam(value = "callback",
                                                    required = false) String callback)
            throws IOException {
        if (ooapiDefaultOrganizationId.isEmpty()) {
            Optional<Organization> org = organizationService.getDefault();
            ooapiDefaultOrganizationId = org.isPresent() ? org.get().getOrganizationId() : "";
        }

        Map<String, Object> data = new HashMap<>();
        data.putAll(Map.of("ooapiDefaultCountry", ooapiDefaultCountry,
                "ooapiDefaultLogo", ooapiDefaultLogo,
                "ooapiDefaultShortUnivName", ooapiDefaultShortUnivName,
                "ooapiDefaultUnivName", ooapiDefaultUnivName,
                "ooapiDefaultOrganizationId", ooapiDefaultOrganizationId,
                "ooapiDefaultEndpointURL", ooapiDefaultEndpointURL));

        if (!ooapiDefaultCourseConsumersJsonFile.isEmpty()) {
            data.put("ooapiDefaultCourseConsumers", readJSONFile(ooapiDefaultCourseConsumersJsonFile));
        }

        if (!ooapiDefaultOfferingConsumersJsonFile.isEmpty()) {
            data.put("ooapiDefaultOfferingConsumers", readJSONFile(ooapiDefaultOfferingConsumersJsonFile));
        }

        String json = objectMapper.writeValueAsString(data);

        if (callback != null && !callback.isEmpty()) {
            return callback + "(" + json + ");";
        } else {
            return json;
        }
    }

    private Object readJSONFile(String path) throws IOException {
        String json = Files.readString(Path.of(path));
        return  objectMapper.readValue(json, Object.class);
    }
}
