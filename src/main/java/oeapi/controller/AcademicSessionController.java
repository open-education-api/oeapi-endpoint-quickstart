package oeapi.controller;

import oeapi.controller.requestparameters.oeapiAcademicSessionRequestParam;
import oeapi.controller.requestparameters.oeapiOfferingRequestParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import oeapi.model.AcademicSession;
import oeapi.oeapiException;
import oeapi.service.AcademicSessionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

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
@RequestMapping("/academic-sessions")
public class AcademicSessionController extends oeapiController<AcademicSession> {

    @Autowired
    private AcademicSessionService service;

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@ModelAttribute oeapiAcademicSessionRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter();
        return this.getAll(filter, requestParam.toPageable(), service);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id) {
        return super.get(id, service);

    }

    @GetMapping(value = "/{id}/offerings", produces = "application/json")
    public ResponseEntity<?> getOfferings(@PathVariable String id, oeapiOfferingRequestParam requestParam) {

        Optional<AcademicSession> existing = service.getById(id);
        if (!existing.isPresent()) {
            //return super.NotFound(id);
            throw new oeapiException(HttpStatus.NOT_FOUND, "There are not offerings for Id: " + id);
        }
        return super.getResponse(requestParam.toPageable(), new ArrayList());

    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AcademicSession academicSession) {

        return super.create(academicSession, service);

    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<AcademicSession> items) {

        return super.createByJSON(items, service);

    }
}
