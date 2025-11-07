package oeapi.controller;

import oeapi.controller.requestparameters.oeapiAssociationRequestParam;
import oeapi.controller.requestparameters.oeapiGroupRequestParam;
import oeapi.controller.requestparameters.oeapiPersonRequestParam;
import oeapi.controller.requestparameters.oeapiRequestParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import oeapi.model.Association;
import oeapi.model.Group;
import oeapi.model.Person;
import oeapi.oeapiException;
import oeapi.service.AssociationService;
import oeapi.service.GroupService;
import oeapi.service.PersonService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    private PersonService service;

    @Autowired
    private GroupService groupService;

    @Autowired
    private AssociationService associationService;

    @GetMapping(value = "/me", produces = "application/json")
    public ResponseEntity<?> getMe(@RequestParam(required = false) String primaryCode, oeapiRequestParam requestParam) {

        // return super.NotFound("Me");
        throw new oeapiException(HttpStatus.NOT_FOUND, " /me endpoint not yet implemented");
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@ModelAttribute oeapiPersonRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter();
        return this.getAll(filter, requestParam.toPageable(), service);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id) {
        return super.get(id, service);

    }

    @GetMapping(value = "/{id}/groups", produces = "application/json")
    public ResponseEntity<?> getGroups(@PathVariable String id, @ModelAttribute oeapiGroupRequestParam requestParam) {

        Optional<Person> existing = service.getById(id);
        if (!existing.isPresent()) {
            //return super.NotFound(id);
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Person with Id: " + id);
        }
        List<Group> groups = groupService.getPersonsByPersonId(id);
        return super.getResponse(requestParam.toPageable(), groups);

    }

    @GetMapping(value = "/{id}/associations", produces = "application/json")
    public ResponseEntity<?> getAssociations(@PathVariable String id, @ModelAttribute oeapiAssociationRequestParam requestParam) {

        Optional<Person> existing = service.getById(id);
        if (existing.isPresent()) {

            List<Association> associations = new ArrayList<>();
            Association a = associationService.autoGenerateBasicItem(id);
            a.setPerson(existing.get());
            associations.add(a);

            return super.getResponse(requestParam.toPageable(), associations);
        }
        // return super.NotFound(id);
        throw new oeapiException(HttpStatus.NOT_FOUND, "There are not Associations for Id: " + id);

    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Person person) {
        return super.create(person, service);
    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<Person> items) {

        /*
        /* Check if this entry point allows updates using REST or is it read-only
        if (!allowRestToModify) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "This OOAPI entry point does not allow updates using REST");
        }

         */
        return super.createByJSON(items, service);

    }

}
