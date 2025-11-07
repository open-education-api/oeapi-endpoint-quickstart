package oeapi.controller;

import oeapi.controller.requestparameters.oeapiCourseRequestParam;
import oeapi.controller.requestparameters.oeapiEducationSpecificationRequestParam;
import oeapi.controller.requestparameters.oeapiProgramRequestParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oeapi.model.EducationSpecification;
import oeapi.service.EducationSpecificationService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/education-specifications")
public class EducationSpecificationController extends oeapiController<EducationSpecification> {

    @Autowired
    private EducationSpecificationService service;

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@ModelAttribute oeapiEducationSpecificationRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter();
        return this.getAll(filter, requestParam.toPageable(), service);
    }

    @GetMapping(value = "/{id}/education-specifications", produces = "application/json")
    public ResponseEntity<?> getEducationSpecifications(@PathVariable String id, @ModelAttribute oeapiEducationSpecificationRequestParam requestParam) {

        return super.getResponse(requestParam.toPageable(), new ArrayList());
    }

    @GetMapping(value = "/{id}/courses", produces = "application/json")
    public ResponseEntity<?> getCourses(@PathVariable String id, @ModelAttribute oeapiCourseRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter(false);
        return super.getResponse(requestParam.toPageable(), new ArrayList());
    }

    @GetMapping(value = "/{id}/programs", produces = "application/json")
    public ResponseEntity<?> getPrograms(@PathVariable String id, @ModelAttribute oeapiProgramRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter(false);
        return super.getResponse(requestParam.toPageable(), new ArrayList());
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id) {
        return super.get(id, service);

    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody EducationSpecification educationSpecification) {

        return super.create(educationSpecification, service);

    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<EducationSpecification> items) {
        return super.createByJSON(items, service);

    }
}
