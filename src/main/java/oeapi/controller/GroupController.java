package oeapi.controller;

import oeapi.controller.requestparameters.oeapiGroupRequestParam;
import oeapi.controller.requestparameters.oeapiPersonRequestParam;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import oeapi.model.Group;
import oeapi.model.Person;
import oeapi.oeapiException;
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
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/groups")
public class GroupController extends oeapiController<Group> {

    /*
    @Autowired
    private ooapiObjectsValidator validator;
     */
    @Autowired
    private GroupService service;

    @Autowired
    private PersonService personService;

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute oeapiGroupRequestParam requestParam) {
        Map.Entry<String, String> filter = requestParam.getFilter();
        return this.getAll(filter, requestParam.toPageable(), service);

    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id) {
        return super.get(id, service);

    }

    @GetMapping(value = "/{id}/persons", produces = "application/json")
    public ResponseEntity<?> getMembers(@PathVariable String id, @ModelAttribute oeapiPersonRequestParam requestParam) {
        Map.Entry<String, String> filter = requestParam.getFilter();
        Optional<Group> existing = service.getById(id);
        if (!existing.isPresent()) {

            // return super.NotFound(id);
            throw new oeapiException(HttpStatus.NOT_FOUND, "There are not members for Id: " + id);

        }
        List<Person> persons = existing.get().getMembers();
        return super.getResponse(requestParam.toPageable(), persons);

    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Group group) {
        return super.create(group, service);

    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<Group> items) {

        return super.createByJSON(items, service);

    }
}
