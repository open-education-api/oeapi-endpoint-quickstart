package oeapi.controller;

import oeapi.controller.requestparameters.oeapiOfferingRequestParam;
import oeapi.controller.requestparameters.oeapiRequestParam;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.validation.Valid;

import oeapi.model.Component;
import oeapi.oeapiException;
import oeapi.oeapiObjectsValidator;
import oeapi.payload.ComponentDTO;
import oeapi.service.ComponentService;
import oeapi.service.OfferingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import oeapi.service.oeapiDTOServiceInterface;

/**
 *
 * @author itziar.urrutia
 */
@RestController
//V6
@RequestMapping("/learning-components")
public class ComponentController extends oeapiDTOController<Component, ComponentDTO> implements oeapiDTOControllerInterface<Component, ComponentDTO> {

    @Autowired
    private oeapiObjectsValidator validator;

    @Autowired
    private ComponentService service;

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@ModelAttribute oeapiRequestParam requestParam) {

        return super.getAll(requestParam.toPageable(Arrays.asList("offeringId", "name")), service);

    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id) {
        return super.get(id, service);
    }

    @PutMapping(value = "/{id}", produces = "application/json")
    @Override
    public ResponseEntity<?> updateFromDTO(@PathVariable String componentId, @Valid @RequestBody ComponentDTO dto) {
        dto.setComponentId(componentId);
        Errors errors = new BeanPropertyBindingResult(dto, "component");
        validator.validate(dto, errors);
        if (errors.hasErrors()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Error creating component at validate: " + errors.getAllErrors());
        }

        return super.createOrUpdate(componentId, dto, service);
    }

    @GetMapping(value = "/{id}/learning-component-offerings", produces = "application/json")
    public ResponseEntity<?> getOfferings(@PathVariable String id, oeapiOfferingRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter();
        Optional<Component> existing = service.getById(id);
        if (!existing.isPresent()) {
            // return super.NotFound(id);
            throw new oeapiException(HttpStatus.NOT_FOUND, "There are learning-component-offeringso for Id: " + id);

        }
        return super.getResponse(requestParam.toPageable(Arrays.asList("offeringId", "name")), new ArrayList());

    }

    @PostMapping
    @Override
    public ResponseEntity<?> createFromDTO(ComponentDTO dto) {
        return super.createDTO(dto, service);
    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<ComponentDTO> items) {

        /*
        /* Check if this entry point allows updates using REST or is it read-only
        if (!allowRestToModify) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "This OOAPI entry point does not allow updates using REST");
        }
         */
        return super.createByJSON(items, service);

    }
}
