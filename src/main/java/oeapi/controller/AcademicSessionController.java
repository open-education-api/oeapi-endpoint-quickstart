package oeapi.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import oeapi.oeapiException;
import oeapi.oeapiUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import oeapi.controller.requestparameters.oeapiAcademicSessionRequestParam;
import oeapi.controller.requestparameters.oeapiOfferingRequestParam;
import oeapi.model.AcademicSession;
import oeapi.service.AcademicSessionService;

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
    public ResponseEntity<?> get(@PathVariable String id, @RequestParam(required = false) String expand) {
        Optional<AcademicSession> existing = service.getById(id);
        if (!existing.isPresent()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no AcademicSession with Id: " + id);
        }
        AcademicSession session = existing.get();
        List<AcademicSession> children = service.getChildren(id);

        ObjectMapper mapper = oeapiUtils.ooapiObjectMapper();
        ObjectNode node = mapper.valueToTree(session);
        ArrayNode childIds = node.putArray("childIds");
        children.forEach(child -> childIds.add(child.getAcademicSessionId()));

        if (expand != null) {
            for (String value : expand.split(",")) {
                switch (value.trim()) {
                    case "parent" ->
                        expandReference(node, "parent", session.getParentId(), mapper);
                    case "year" ->
                        expandReference(node, "year", session.getYearId(), mapper);
                    case "children" ->
                        node.set("children", mapper.valueToTree(children));
                    default -> {
                    }
                }
            }
        }
        return ResponseEntity.ok(node);
    }

    private void expandReference(ObjectNode node, String name, String referencedId, ObjectMapper mapper) {
        if (referencedId != null) {
            service.getById(referencedId).ifPresent(referenced -> node.set(name, mapper.valueToTree(referenced)));
        }
    }

    @GetMapping(value = "/{id}/offerings", produces = "application/json")
    public ResponseEntity<?> getOfferings(@PathVariable String id, oeapiOfferingRequestParam requestParam) {
        Optional<AcademicSession> existing = service.getById(id);
        if (!existing.isPresent()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There are not offerings for Id: " + id);
        }
        return super.getResponse(requestParam.toPageable(), new ArrayList<AcademicSession>());
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<?> create(@RequestBody AcademicSession academicSession) {
        return super.create(academicSession, service);
    }

    @PutMapping(value = "/{academicSessionId}", produces = "application/json")
    public ResponseEntity<?> put(@PathVariable String academicSessionId, @RequestBody AcademicSession academicSession) {
        if (academicSessionId.equals(academicSession.getAcademicSessionId())) {
            return super.update(academicSession, service);
        }
        throw new oeapiException(HttpStatus.NOT_FOUND,
                "Error putting AcademicSession: academicSessionId in body does not match URL.");
    }

    @DeleteMapping("/{academicSessionId}")
    public ResponseEntity<String> delete(@PathVariable String academicSessionId) {
        Optional<AcademicSession> ac = service.getById(academicSessionId);
        if (ac.isPresent()) {
            service.delete(academicSessionId);
        } else {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Error deleting AcademicSession with Id: " + academicSessionId);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<AcademicSession> items) {
        return super.createByJSON(items, service);
    }
}
