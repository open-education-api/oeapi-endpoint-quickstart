package oeapi.controller;

import java.util.Arrays;
import oeapi.oeapiUtils;
import oeapi.controller.requestparameters.oeapiRequestParam;
import oeapi.model.Membership;
import oeapi.service.AcademicSessionService;
import oeapi.service.OrganisationService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

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

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private AcademicSessionService academicSessionService;

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute oeapiGroupRequestParam requestParam) {
        Map.Entry<String, String> filter = requestParam.getFilter();
        return this.getAll(filter, requestParam.toPageable(), service);

    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id, @RequestParam(required = false) String expand) {
        Group group = service.getById(id).orElseThrow(() -> notFound(id));
        ObjectMapper mapper = oeapiUtils.ooapiObjectMapper();
        ObjectNode node = mapper.valueToTree(group);
        if (expand != null) {
            List<String> values = Arrays.asList(expand.split(","));
            if (values.contains("organisation") && group.getOrganisation() != null) {
                organisationService.getById(group.getOrganisationId()).ifPresent(organisation ->
                        node.set("organisation", mapper.valueToTree(organisationService.getMapper().toDTO(organisation))));
            }
            if (values.contains("academic_session") && group.getAcademicSession() != null) {
                academicSessionService.getById(group.getAcademicSessionId()).ifPresent(session ->
                        node.set("academicSession", mapper.valueToTree(session)));
            }
        }
        return ResponseEntity.ok(node);
    }

    /** OEAPI v6 PUT: replaces the group, creating it when it does not exist yet. */
    @PutMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> put(@PathVariable String id, @RequestBody Group group) {
        if (group.getGroupId() == null || !group.getGroupId().equalsIgnoreCase(id)) {
            group.setGroupId(id);
        }
        if (service.getById(id).isEmpty()) {
            return super.create(group, service);
        }
        return super.update(group, service);
    }

    @GetMapping(value = "/{id}/memberships", produces = "application/json")
    public ResponseEntity<?> getMemberships(@PathVariable String id, @ModelAttribute oeapiRequestParam requestParam) {
        service.getById(id).orElseThrow(() -> notFound(id));
        return super.getResponse(requestParam.toPageable(), service.getMemberships(id));
    }

    /** OEAPI v6: creates or replaces the membership of a person in a group. */
    @PutMapping(value = "/{id}/memberships/{personId}", produces = "application/json")
    public ResponseEntity<?> putMembership(@PathVariable String id, @PathVariable String personId,
            @RequestBody Membership membership) {
        Group group = service.getById(id).orElseThrow(() -> notFound(id));
        Person person = personService.getById(personId).orElseThrow(() ->
                new oeapiException(HttpStatus.NOT_FOUND, "Person not found", "There is no Person with Id: " + personId));

        Optional<Membership> existing = service.getMembership(id, personId);
        existing.ifPresent(stored -> membership.setMembershipId(stored.getMembershipId()));
        membership.setGroup(group);
        membership.setPerson(person);
        if (membership.getState() == null) {
            membership.setState("active");
        }
        if (membership.getRole() == null) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Role missing", "A membership needs a role");
        }
        Membership saved = service.saveMembership(membership);
        return ResponseEntity.status(existing.isPresent() ? HttpStatus.OK : HttpStatus.CREATED).body(saved);
    }

    private oeapiException notFound(String id) {
        return new oeapiException(HttpStatus.NOT_FOUND, "Group not found", "There is no Group with Id: " + id);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<?> create(@RequestBody Group group) {
        return super.create(group, service);

    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<Group> items) {

        return super.createByJSON(items, service);

    }
}
