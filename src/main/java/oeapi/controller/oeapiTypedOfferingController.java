package oeapi.controller;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import oeapi.oeapiException;
import oeapi.model.Offering;
import oeapi.payload.OfferingDTO;
import oeapi.service.OfferingService;
import tools.jackson.core.JacksonException;

/**
 * Common behaviour of the OEAPI v6 typed offering resources
 * (/course-offerings, /programme-offerings, /learning-component-offerings).
 *
 * GET and PUT are the operations of the specification; POST (create) and
 * DELETE are kept as implementation specific extensions used by the
 * quickstart dashboard.
 *
 * @param <E> offering entity subtype
 * @param <D> v6 DTO
 */
public abstract class oeapiTypedOfferingController<E extends Offering, D extends OfferingDTO> {

    static Logger logger = LoggerFactory.getLogger(oeapiTypedOfferingController.class);

    @Autowired
    protected OfferingService offeringService;

    /** Human readable name used in error messages, e.g. "CourseOffering". */
    protected abstract String typeName();

    protected abstract Optional<E> find(String id);

    protected abstract oeapiDTOMapper<E, D> mapper();

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id, @RequestParam(required = false) String expand) throws JacksonException {
        E offering = find(id).orElseThrow(() -> notFound(id));
        return ResponseEntity.ok(mapper().toJSON(offering, expand));
    }

    /** OEAPI v6 PUT: replaces the offering, creating it when it does not exist yet. */
    @PutMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> put(@PathVariable String id, @RequestBody D dto) throws JacksonException {
        if (dto.getOfferingId() == null) {
            dto.setOfferingId(id);
        } else if (!dto.getOfferingId().equalsIgnoreCase(id)) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Error putting " + typeName(),
                    "The id in the body [" + dto.getOfferingId() + "] does not match the URL [" + id + "]");
        }

        E entity = mapper().toEntity(dto);
        boolean exists = find(id).isPresent();
        Offering saved = exists ? offeringService.update(id, entity) : offeringService.create(entity);

        return ResponseEntity.status(exists ? HttpStatus.OK : HttpStatus.CREATED)
                .body(mapper().toJSON(cast(saved), null));
    }

    /** Implementation specific: create an offering, generating its id when none is given. */
    @PostMapping(produces = "application/json")
    public ResponseEntity<?> create(@RequestBody D dto) throws JacksonException {
        if (dto.getOfferingId() == null) {
            dto.setOfferingId(UUID.randomUUID().toString());
        }
        if (find(dto.getOfferingId()).isPresent()) {
            throw new oeapiException(HttpStatus.CONFLICT, typeName() + " already exists",
                    typeName() + " [" + dto.getOfferingId() + "] already exists, use PUT to replace it");
        }
        Offering saved = offeringService.create(mapper().toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper().toJSON(cast(saved), null));
    }

    /** Implementation specific: delete an offering. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        find(id).orElseThrow(() -> notFound(id));
        offeringService.delete(id);
        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    private E cast(Offering offering) {
        return (E) offering;
    }

    protected oeapiException notFound(String id) {
        return new oeapiException(HttpStatus.NOT_FOUND, typeName() + " not found", "There is no " + typeName() + " with Id: " + id);
    }
}
