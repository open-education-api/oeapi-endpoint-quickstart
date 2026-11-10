package oeapi.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import tools.jackson.core.JacksonException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import oeapi.oeapiException;
import oeapi.oeapiObjectsValidator;
import oeapi.controller.requestparameters.oeapiComponentRequestParam;
import oeapi.controller.requestparameters.oeapiCourseRequestParam;
import oeapi.controller.requestparameters.oeapiGroupRequestParam;
import oeapi.controller.requestparameters.oeapiOrganisationRequestParam;
import oeapi.controller.requestparameters.oeapiProgrammeRequestParam;
import oeapi.controller.requestparameters.oeapiRequestParam;
import oeapi.model.Component;
import oeapi.model.Group;
import oeapi.model.Organisation;
import oeapi.model.Programme;
import oeapi.payload.CourseDTO;
import oeapi.payload.OrganisationDTO;
import oeapi.service.ComponentService;
import oeapi.service.CourseService;
import oeapi.service.GroupService;
import oeapi.service.OrganisationService;
import oeapi.service.ProgrammeService;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/organisations")
public class OrganisationController extends oeapiDTOController<Organisation, OrganisationDTO> {

    @Autowired
    private oeapiObjectsValidator validator;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private ProgrammeService programmeService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ComponentService componentService;

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute oeapiOrganisationRequestParam requestParam) {
        Map.Entry<String, String> filter = requestParam.getFilter();
        return this.getAll(filter, requestParam.toPageable(), organisationService);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> getOrganisation(@PathVariable String id,
            @RequestParam(required = false) String expand) throws JacksonException {
        return super.get(id, expand, organisationService);
    }

    @GetMapping(value = "/{id}/groups", produces = "application/json")
    public ResponseEntity<?> getGroups(@PathVariable String id, oeapiGroupRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter();
        Optional<Organisation> existing = organisationService.getById(id);
        if (!existing.isPresent()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There are not groups for Id: " + id);
        }

        List<Group> groups = groupService.getByOrganisationId(id);
        return super.getResponse(requestParam.toPageable(), groups);
    }

    @GetMapping(value = "/{id}/course-offerings", produces = "application/json")
    public ResponseEntity<?> getCourseOfferings(@PathVariable String id, oeapiRequestParam requestParam) {

        Optional<Organisation> existing = organisationService.getById(id);
        if (!existing.isPresent()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organisation with Id: " + id);
        }
        throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Offering for Organisation with Id " + id);
    }

    //V6
    @GetMapping(value = "/{id}/programme-offerings", produces = "application/json")
    public ResponseEntity<?> getProgrammeOfferings(@PathVariable String id, oeapiRequestParam requestParam) {

        Optional<Organisation> existing = organisationService.getById(id);
        if (!existing.isPresent()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organisation with Id: " + id);
        }
        throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Offering for Organisation with Id " + id);
    }

    //V6
    @GetMapping(value = "/{id}/learning-component-offerings", produces = "application/json")
    public ResponseEntity<?> getComponentOfferings(@PathVariable String id, oeapiRequestParam requestParam) {

        Optional<Organisation> existing = organisationService.getById(id);
        if (!existing.isPresent()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organisation with Id: " + id);
        }
        throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Offering for Organisation with Id " + id);
    }

    @GetMapping(value = "/{id}/courses", produces = "application/json")
    public ResponseEntity<?> getCourses(@PathVariable String id, @ModelAttribute oeapiCourseRequestParam requestParam) {
 
        Map.Entry<String, String> filter = requestParam.getFilter();
        Optional<Organisation> existing = organisationService.getById(id);
        if (!existing.isPresent()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organisation with Id: " + id);
        }
        Pageable pageable = requestParam.toPageable();
        Page<CourseDTO> courses = courseService.getByOrganisationId(existing.get().getOrganisationId(), filter, pageable);
        return super.getResponse(courses);
    }

    @GetMapping(value = "/{id}/programmes", produces = "application/json")
    public ResponseEntity<?> getProgrammes(@PathVariable String id, @ModelAttribute oeapiProgrammeRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter(false);
        Pageable pageable = requestParam.toPageable();
        Optional<Organisation> existing = organisationService.getById(id);
        if (!existing.isPresent()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organisation with Id: " + id);
        }
        Page<Programme> programmes = programmeService.getByOrganisationId(existing.get().getOrganisationId(), filter, pageable);
        return super.getResponse(programmeService.toDTOPages(programmes));
    }

    @GetMapping(value = "/{id}/learning-components", produces = "application/json")
    public ResponseEntity<?> getComponents(@PathVariable String id, oeapiComponentRequestParam requestParam) {

        Optional<Organisation> existing = organisationService.getById(id);
        if (!existing.isPresent()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organisation with Id: " + id);
        }
        Pageable pageable = requestParam.toPageable();
        Map.Entry<String, String> filter = requestParam.getFilter();
        Page<Component> components = componentService.getByOrganisationId(id, filter, pageable);
        return super.getResponse(componentService.toDTOPages(components));
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<?> createOrganisation(@RequestBody OrganisationDTO o) {

        // Check organisation is not parent of itself.  This may lead to an infinite loop getting its children
        if ((o.getParent() != null)  && o.getParent().getOrganisationId().equalsIgnoreCase(o.getOrganisationId())) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Error putting Organisation: organisation cannot be parent of itself! Check organisation and parent ids");
        }

        Errors errors = new BeanPropertyBindingResult(o, "organisation");
        validator.validate(o, errors);
        if (errors.hasErrors()) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Error creating Organisation at validate: " + errors.getAllErrors());
        }        
        return super.createDTO(o, organisationService);
    }

    @PutMapping(value = "/{organisationId}", produces = "application/json")
    public ResponseEntity<?> updateOrganisation(@PathVariable String organisationId, @RequestBody OrganisationDTO o) {
 
        // Check Id first. Id might not be necessary in JSON but we ask for it to double check we update the right object. 
        if ((o.getOrganisationId() == null) || (!o.getOrganisationId().equalsIgnoreCase(organisationId))) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Error putting Organisation: organisationId on JSON does not match URL request or is missing : [" + organisationId + "," + o.getOrganisationId() + "]");
        }
        
        // Check organisation is not parent of itself.  This may lead to an infinite loop getting its children
        if ((o.getParent() != null) && o.getParent().getOrganisationId().equalsIgnoreCase(organisationId)) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Error putting Organisation: organisation cannot be parent of itself! Check organisation and parent ids");
        }
        
        Errors errors = new BeanPropertyBindingResult(o, "organisation");
        validator.validate(o, errors);
        if (errors.hasErrors()) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Error updating Organisation at validate: " + errors.getAllErrors());
        }
        return super.updateDTO(o, organisationService);
    }

    @Deprecated
    @PostMapping("/{organisationId}/update")
    public ResponseEntity<?> updateOrganisation(@PathVariable String organisationId, @RequestBody Organisation o) {

        // Check Id first. Id might not be necessary in JSON but we ask for it to double check we update the right object. 
        if ((o.getOrganisationId() == null) || (!o.getOrganisationId().equalsIgnoreCase(organisationId))) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Error putting Organisation: organisationId on JSON does not match URL request or is missing : [" + organisationId + "," + o.getOrganisationId() + "]");
        }

        Errors errors = new BeanPropertyBindingResult(o, "organisation");
        validator.validate(o, errors);
        if (errors.hasErrors()) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Error updating Organisation at validate: " + errors.getAllErrors());
        }
        organisationService.update(organisationId, o);

        return ResponseEntity.ok("Organisation updated successfully");
    }

    @DeleteMapping("/{organisationId}")
    public ResponseEntity<?> deleteOrganisation(@PathVariable String organisationId) {

        boolean isRemoved;
        isRemoved = organisationService.delete(organisationId);
        if (!isRemoved) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organisation with Id: " + organisationId);
        } else {
            return ResponseEntity.ok("Organisation " + organisationId + "delete successfully");
        }
    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<OrganisationDTO> items) {

        for (OrganisationDTO dto : items) {
            Errors errors = new BeanPropertyBindingResult(dto, "organisation");
            validator.validate(dto, errors);
            if (errors.hasErrors()) {
                throw new oeapiException(HttpStatus.BAD_REQUEST, "Error creating Organisation at validate: " + errors.getAllErrors());
            }

            super.createDTO(dto, organisationService);

        }
        return ResponseEntity.ok("Organisations created successfully");
    }

}
