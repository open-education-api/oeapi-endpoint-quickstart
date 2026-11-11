package oeapi.controller;

import oeapi.controller.requestparameters.oeapiAssociationRequestParam;
import oeapi.controller.requestparameters.oeapiPersonRequestParam;
import oeapi.controller.requestparameters.oeapiRequestParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import oeapi.model.Person;
import oeapi.oeapiException;
import oeapi.service.PersonService;

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

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/persons")
public class PersonController extends oeapiController<Person> {

    @Autowired
    private PersonService personService;

    @Autowired
    private oeapi.service.OfferingAssociationService associationService;

    /** OEAPI v6: the person of the authenticated user (matched on e-mail address). */
    @GetMapping(value = "/me", produces = "application/json")
    public ResponseEntity<?> getMe(java.security.Principal principal) {
        if (principal == null) {
            throw new oeapiException(HttpStatus.UNAUTHORIZED, "Unauthorized", "/persons/me needs an authenticated user");
        }
        Person me = personService.getByEmail(principal.getName())
                .orElseThrow(() -> new oeapiException(HttpStatus.NOT_FOUND, "Person not found",
                        "There is no person with e-mail " + principal.getName()));
        return ResponseEntity.ok(me);
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@ModelAttribute oeapiPersonRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter();
        return this.getAll(filter, requestParam.toPageable(), personService);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id) {
        return super.get(id, personService);

    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<?> create(@RequestBody Person person) {
        return super.create(person, personService);
    }

    /** OEAPI v6 PUT: replaces the person, creating it when it does not exist yet. */
    @PutMapping(value = "/{personId}", produces = "application/json")
    public ResponseEntity<?> update(@PathVariable String personId, @RequestBody Person person) {
        if (person.getPersonId() == null || !person.getPersonId().equalsIgnoreCase(personId)) {
            person.setPersonId(personId);
        }
        if (!personService.getById(personId).isPresent()) {
            return super.create(person, personService);
        }
        return super.update(person, personService);
    }

    @DeleteMapping("/{personId}")
    public ResponseEntity<?> deletePerson(@PathVariable String personId) {

        Optional<Person> existing = personService.getById(personId);

        if (existing.isPresent()) {
            personService.delete(personId);  
            return ResponseEntity.ok().build();
        } else {
           throw new oeapiException(HttpStatus.NOT_FOUND, "Error deletePerson with Id: " + personId+ ". Check id");
        }

    }
           
    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<Person> items) {

        /*
        /* Check if this entry point allows updates using REST or is it read-only
        if (!allowRestToModify) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "This OOAPI entry point does not allow updates using REST");
        }

         */
        return super.createByJSON(items, personService);

    }


    @GetMapping(value = "/{id}/course-offering-associations", produces = "application/json")
    public ResponseEntity<?> getCourseOfferingAssociations(@PathVariable String id, @ModelAttribute oeapiAssociationRequestParam requestParam) {
        return associationsOf(id, oeapi.model.CourseOffering.class, requestParam);
    }

    @GetMapping(value = "/{id}/programme-offering-associations", produces = "application/json")
    public ResponseEntity<?> getProgrammeOfferingAssociations(@PathVariable String id, @ModelAttribute oeapiAssociationRequestParam requestParam) {
        return associationsOf(id, oeapi.model.ProgrammeOffering.class, requestParam);
    }

    @GetMapping(value = "/{id}/learning-component-offering-associations", produces = "application/json")
    public ResponseEntity<?> getLearningComponentOfferingAssociations(@PathVariable String id, @ModelAttribute oeapiAssociationRequestParam requestParam) {
        return associationsOf(id, oeapi.model.ComponentOffering.class, requestParam);
    }

    private ResponseEntity<?> associationsOf(String personId, Class<? extends oeapi.model.Offering> offeringType,
            oeapiAssociationRequestParam requestParam) {
        if (personService.getById(personId).isEmpty()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Person not found", "There is no Person with Id: " + personId);
        }
        return oeapiOfferingLists.page(requestParam, associationService.toDTOList(
                requestParam.filter(associationService.getByPersonId(personId, offeringType))));
    }
}
