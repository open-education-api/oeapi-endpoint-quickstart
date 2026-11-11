package oeapi.controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import oeapi.oeapiException;
import oeapi.oeapiUtils;
import oeapi.model.Offering;
import oeapi.model.OfferingAssociation;
import oeapi.model.Person;
import oeapi.payload.OfferingAssociationDTO;
import oeapi.service.OfferingAssociationService;
import oeapi.service.OfferingService;
import oeapi.service.PersonService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Common behaviour of the OEAPI v6 association resources
 * (/course-offering-associations, /programme-offering-associations,
 * /learning-component-offering-associations): GET with expand, PUT (create or
 * replace) and PATCH (JSON merge) as in the specification; DELETE as an
 * implementation extension.
 *
 * @param <O> offering subtype the association points to
 * @param <D> v6 DTO
 */
public abstract class oeapiTypedAssociationController<O extends Offering, D extends OfferingAssociationDTO> {

    @Autowired
    protected OfferingAssociationService associationService;

    @Autowired
    protected OfferingService offeringService;

    @Autowired
    protected PersonService personService;

    protected final ObjectMapper mapper = oeapiUtils.ooapiObjectMapper();

    protected abstract Class<O> offeringType();

    protected abstract Class<D> dtoType();

    /** v6 expand value of the offering, e.g. course_offering. */
    protected abstract String offeringExpandName();

    /** The offering converted to its v6 DTO, for ?expand. */
    protected abstract Object offeringDTO(O offering);

    protected String typeName() {
        return OfferingAssociationService.typeLabel(offeringType()) + "Association";
    }

    protected oeapiException notFound(String id) {
        return new oeapiException(HttpStatus.NOT_FOUND, typeName() + " not found", "There is no " + typeName() + " with Id: " + id);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id, @RequestParam(required = false) String expand) {
        OfferingAssociation association = associationService.getById(id, offeringType()).orElseThrow(() -> notFound(id));
        return ResponseEntity.ok(toJson(association, expand));
    }

    /** OEAPI v6 PUT: replaces the association, creating it when it does not exist yet. */
    @PutMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> put(@PathVariable String id, @RequestBody D dto) {
        if (dto.getAssociationId() == null) {
            dto.setAssociationId(id);
        } else if (!dto.getAssociationId().equalsIgnoreCase(id)) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Error putting " + typeName(),
                    "The associationId in the body [" + dto.getAssociationId() + "] does not match the URL [" + id + "]");
        }
        boolean exists = associationService.getById(id).isPresent();
        if (exists && associationService.getById(id, offeringType()).isEmpty()) {
            throw new oeapiException(HttpStatus.CONFLICT, "Association of another type",
                    "Association " + id + " is not a " + typeName());
        }
        OfferingAssociation saved = associationService.save(associationService.toEntity(dto, offeringType()));
        return ResponseEntity.status(exists ? HttpStatus.OK : HttpStatus.CREATED).body(toJson(saved, null));
    }

    /** OEAPI v6 PATCH: merges the properties sent into the stored association. */
    @PatchMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> patch(@PathVariable String id, @RequestBody String patch) {
        OfferingAssociation existing = associationService.getById(id, offeringType()).orElseThrow(() -> notFound(id));
        D current = dtoType().cast(associationService.toDTO(existing));
        D patched;
        try {
            patched = mapper.readerForUpdating(current).readValue(patch);
        } catch (RuntimeException ex) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Invalid PATCH body", ex.getMessage());
        }
        patched.setAssociationId(id);
        OfferingAssociation saved = associationService.save(associationService.toEntity(patched, offeringType()));
        return ResponseEntity.ok(toJson(saved, null));
    }

    /** Implementation specific: delete an association. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        associationService.getById(id, offeringType()).orElseThrow(() -> notFound(id));
        associationService.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * OEAPI v6 POST .../external/me: the authenticated person associates
     * themselves with an offering. Without authentication (security disabled)
     * the personId of the body is used.
     */
    protected ResponseEntity<?> createForMe(D dto, Principal principal) {
        if (principal != null) {
            Person me = personService.getByEmail(principal.getName())
                    .orElseThrow(() -> new oeapiException(HttpStatus.NOT_FOUND, "Person not found",
                            "There is no person with e-mail " + principal.getName()));
            dto.setPersonId(me.getPersonId());
        } else if (dto.getPersonId() == null) {
            throw new oeapiException(HttpStatus.UNAUTHORIZED, "Unauthorized",
                    "external/me needs an authenticated user or a personId");
        }
        dto.setAssociationId(UUID.randomUUID().toString());
        if (dto.getState() == null) {
            dto.setState("pending");
        }
        OfferingAssociation saved = associationService.save(associationService.toEntity(dto, offeringType()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toJson(saved, null));
    }

    protected ObjectNode toJson(OfferingAssociation association, String expand) {
        ObjectNode node = mapper.valueToTree(associationService.toDTO(association));
        if (expand != null) {
            List<String> values = Arrays.asList(expand.split(","));
            if (values.contains("person") && association.getPerson() != null) {
                node.set("person", mapper.valueToTree(association.getPerson()));
            }
            if (values.contains(offeringExpandName()) && association.getOffering() != null) {
                node.set(snakeToCamel(offeringExpandName()),
                         mapper.valueToTree(offeringDTO(offeringType().cast(association.getOffering()))));
            }
        }
        return node;
    }

    private static String snakeToCamel(String value) {
        return oeapiDTOMapper.snakeToCamel(value);
    }

    /** The associations of one offering, filtered by role, state and resultState. */
    public static List<OfferingAssociationDTO> listForOffering(OfferingAssociationService service, String offeringId,
            oeapi.controller.requestparameters.oeapiAssociationRequestParam requestParam) {
        return service.toDTOList(requestParam.filter(service.getByOfferingId(offeringId)));
    }
}
