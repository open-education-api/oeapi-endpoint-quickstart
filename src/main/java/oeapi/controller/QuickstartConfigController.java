package oeapi.controller;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    @Value("${quickdashboard.config.ooapiDefaultCountry}")
    private String ooapiDefaultCountry;

    @Value("${quickdashboard.config.ooapiDefaultLogo}")
    private String ooapiDefaultLogo;

    @Value("${quickdashboard.config.ooapiDefaultShortUnivName}")
    private String ooapiDefaultShortUnivName;

    @Value("${quickdashboard.config.ooapiDefaultUnivName}")
    private String ooapiDefaultUnivName;

    @Value("${quickdashboard.config.ooapiDefaultOrganizationId:}")
    private String ooapiDefaultOrganizationId;

    @Value("${quickdashboard.config.ooapiDefaultEndpointURL}")
    private String ooapiDefaultEndpointURL;

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
                                                    required = false) String callback) throws JsonProcessingException {
        if (ooapiDefaultOrganizationId.isEmpty()) {
            Optional<Organization> org = organizationService.getDefault();
            ooapiDefaultOrganizationId = org.isPresent() ? org.get().getOrganizationId() : "";
        }

        Map<String,String> data =
            Map.of("ooapiDefaultCountry", ooapiDefaultCountry,
                   "ooapiDefaultLogo", ooapiDefaultLogo,
                   "ooapiDefaultShortUnivName", ooapiDefaultShortUnivName,
                   "ooapiDefaultUnivName", ooapiDefaultUnivName,
                   "ooapiDefaultOrganizationId", ooapiDefaultOrganizationId,
                   "ooapiDefaultEndpointURL", ooapiDefaultEndpointURL);
        String json = objectMapper.writeValueAsString(data);

        if (callback != null && !callback.isEmpty()) {
            return callback + "(" + json + ");";
        } else {
            return json;
        }
    }
}
