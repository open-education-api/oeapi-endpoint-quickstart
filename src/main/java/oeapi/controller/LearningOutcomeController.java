package oeapi.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import oeapi.controller.requestparameters.oeapiRequestParam;
import oeapi.model.LearningOutcome;
import oeapi.service.LearningOutcomeService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * OEAPI v6 /learning-outcomes. GET is the specification; POST, PUT and
 * DELETE are implementation extensions to manage the outcomes.
 */
@RestController
@RequestMapping("/learning-outcomes")
public class LearningOutcomeController extends oeapiController<LearningOutcome> {

    @Autowired
    private LearningOutcomeService service;

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@ModelAttribute oeapiRequestParam requestParam) {
        Page<LearningOutcome> page = requestParam.getFilter() == null
                ? service.getAll(requestParam.toPageable())
                : service.getByPrimaryCode(requestParam.getPrimaryCode(), requestParam.toPageable());
        List<LearningOutcome> items = page.getContent().stream().map(service::withChildIds).collect(Collectors.toList());
        return super.getResponse(new PageImpl<>(items, page.getPageable(), page.getTotalElements()));
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id, @RequestParam(required = false) String expand) {
        LearningOutcome outcome = service.getById(id).map(service::withChildIds).orElseThrow(() -> notFound(id));
        ObjectMapper mapper = oeapiUtils.ooapiObjectMapper();
        ObjectNode node = mapper.valueToTree(outcome);
        if (expand != null) {
            List<String> values = Arrays.asList(expand.split(","));
            if (values.contains("parents")) {
                node.set("parents", mapper.valueToTree(outcome.getParents()));
            }
            if (values.contains("children")) {
                node.set("children", mapper.valueToTree(service.getChildren(id)));
            }
        }
        return ResponseEntity.ok(node);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<?> create(@RequestBody LearningOutcome outcome) {
        return super.create(outcome, service);
    }

    @PutMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> put(@PathVariable String id, @RequestBody LearningOutcome outcome) {
        outcome.setLearningOutcomeId(id);
        if (service.getById(id).isEmpty()) {
            return super.create(outcome, service);
        }
        return super.update(outcome, service);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        service.getById(id).orElseThrow(() -> notFound(id));
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    private oeapiException notFound(String id) {
        return new oeapiException(HttpStatus.NOT_FOUND, "Learning outcome not found", "There is no learning outcome with Id: " + id);
    }
}
