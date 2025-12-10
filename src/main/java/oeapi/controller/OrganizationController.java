package oeapi.controller;

import oeapi.controller.requestparameters.oeapiCourseRequestParam;
import oeapi.controller.requestparameters.oeapiComponentRequestParam;
import oeapi.controller.requestparameters.oeapiEducationSpecificationRequestParam;
import oeapi.controller.requestparameters.oeapiGroupRequestParam;
import oeapi.controller.requestparameters.oeapiProgramRequestParam;
import oeapi.controller.requestparameters.oeapiOrganizationRequestParam;
import oeapi.controller.requestparameters.oeapiRequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import oeapi.model.Component;
import oeapi.model.EducationSpecification;
import oeapi.model.Group;
import oeapi.model.Organization;
import oeapi.payload.OrganizationDTO;
import oeapi.model.Program;
import oeapi.oeapiException;
import oeapi.oeapiObjectsValidator;
import oeapi.payload.CourseDTO;
import oeapi.service.ComponentService;
import oeapi.service.CourseService;
import oeapi.service.EducationSpecificationService;
import oeapi.service.GroupService;
import oeapi.service.OrganizationService;
import oeapi.service.ProgramService;
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
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/organizations")
public class OrganizationController extends oeapiDTOController<Organization, OrganizationDTO> {

    @Autowired
    private oeapiObjectsValidator validator;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private EducationSpecificationService educationSpecificationService;

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute oeapiOrganizationRequestParam requestParam) {
        Map.Entry<String, String> filter = requestParam.getFilter();
        return this.getAll(filter, requestParam.toPageable(), organizationService);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> getOrganization(@PathVariable String id) {
        return super.get(id, organizationService);
    }

    @GetMapping(value = "/{id}/groups", produces = "application/json")
    public ResponseEntity<?> getGroups(@PathVariable String id, oeapiGroupRequestParam requestParam) {
        Map.Entry<String, String> filter = requestParam.getFilter();
        Optional<Organization> existing = organizationService.getById(id);
        if (!existing.isPresent()) {
            //return super.NotFound(id);
            throw new oeapiException(HttpStatus.NOT_FOUND, "There are not groups for Id: " + id);
        }

        List<Group> groups = groupService.getByOrganizationId(id);
        return super.getResponse(requestParam.toPageable(), groups);

    }

    @GetMapping(value = "/{id}/course-offerings", produces = "application/json")
    public ResponseEntity<?> getCourseOfferings(@PathVariable String id, oeapiRequestParam requestParam) {
        //public ResponseEntity<?> getCourses(@PathVariable String id, @RequestParam oeapiRequestParam requestParam) {

        Optional<Organization> existing = organizationService.getById(id);
        if (!existing.isPresent()) {

            //return ResponseEntity.notFound().build();
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organization with Id: " + id);
        }

        throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Offering for Organization with Id " + id);
        //return ResponseEntity.notFound().build();
    }

    //V6
    @GetMapping(value = "/{id}/program-offerings", produces = "application/json")
    public ResponseEntity<?> getProgramOfferings(@PathVariable String id, oeapiRequestParam requestParam) {
        //public ResponseEntity<?> getCourses(@PathVariable String id, @RequestParam oeapiRequestParam requestParam) {

        Optional<Organization> existing = organizationService.getById(id);
        if (!existing.isPresent()) {

            //return ResponseEntity.notFound().build();
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organization with Id: " + id);
        }

        throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Offering for Organization with Id " + id);
        //return ResponseEntity.notFound().build();
    }

    //V6
    @GetMapping(value = "/{id}/learning-component-offerings", produces = "application/json")
    public ResponseEntity<?> getComponentOfferings(@PathVariable String id, oeapiRequestParam requestParam) {
        //public ResponseEntity<?> getCourses(@PathVariable String id, @RequestParam oeapiRequestParam requestParam) {

        Optional<Organization> existing = organizationService.getById(id);
        if (!existing.isPresent()) {

            //return ResponseEntity.notFound().build();
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organization with Id: " + id);
        }

        throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Offering for Organization with Id " + id);
        //return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/{id}/courses", produces = "application/json")
    public ResponseEntity<?> getCourses(@PathVariable String id, @ModelAttribute oeapiCourseRequestParam requestParam) {
        Map.Entry<String, String> filter = requestParam.getFilter();
        Optional<Organization> existing = organizationService.getById(id);
        if (!existing.isPresent()) {
            // return ResponseEntity.notFound().build();
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organization with Id: " + id);

        }
        Pageable pageable = requestParam.toPageable();
        Page<CourseDTO> courses = courseService.getByOrganizationId(existing.get().getOrganizationId(), filter, pageable);
        return super.getResponse(courses);

    }

    @GetMapping(value = "/{id}/programs", produces = "application/json")
    public ResponseEntity<?> getPrograms(@PathVariable String id, @ModelAttribute oeapiProgramRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter(false);
        Pageable pageable = requestParam.toPageable();
        Optional<Organization> existing = organizationService.getById(id);
        if (!existing.isPresent()) {

            // return ResponseEntity.notFound().build();
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organization with Id: " + id);
        }
        Page<Program> programs = programService.getByOrganizationId(existing.get().getOrganizationId(), filter, pageable);
        return super.getResponse(programs);

    }

    @GetMapping(value = "/{id}/education-specifications", produces = "application/json")
    public ResponseEntity<?> getEducationSpecifications(@PathVariable String id, oeapiEducationSpecificationRequestParam requestParam) {

        Optional<Organization> existing = organizationService.getById(id);
        if (!existing.isPresent()) {
            // return ResponseEntity.notFound().build();
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organization with Id: " + id);
        }

        List<EducationSpecification> educationSpecifications = educationSpecificationService.getByOrganizationId(id);
        return super.getResponse(requestParam.toPageable(), educationSpecifications);

    }

    @GetMapping(value = "/{id}/components", produces = "application/json")
    public ResponseEntity<?> getComponents(@PathVariable String id, oeapiComponentRequestParam requestParam) {

        Optional<Organization> existing = organizationService.getById(id);
        if (!existing.isPresent()) {
            // return ResponseEntity.notFound().build();
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Organization with Id: " + id);
        }
        Pageable pageable = requestParam.toPageable();
        Map.Entry<String, String> filter = requestParam.getFilter();
        Page<Component> components = componentService.getByOrganizationId(id, filter, pageable);
        return super.getResponse(components);

    }

    @PostMapping
    public ResponseEntity<?> createOrganization(@RequestBody OrganizationDTO o) {

        Errors errors = new BeanPropertyBindingResult(o, "organization");
        validator.validate(o, errors);
        if (errors.hasErrors()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Error creating Organization at validate: " + errors.getAllErrors());
        }

        return super.createDTO(o, organizationService);

    }

    @PutMapping
    public ResponseEntity<?> updateOrganization(@RequestBody OrganizationDTO o) {
        Errors errors = new BeanPropertyBindingResult(o, "organization");
        validator.validate(o, errors);
        if (errors.hasErrors()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Error updating Organization at validate: " + errors.getAllErrors());
        }
        return super.updateDTO(o, organizationService);
        //Organization updated = organizationService.update(o);
        //return ResponseEntity.ok("Organization updated successfully");
    }

    @PostMapping("/{organizationId}/update")
    public ResponseEntity<?> updateOrganization(@PathVariable String organizationId, @RequestBody Organization o) {
        Errors errors = new BeanPropertyBindingResult(o, "organization");
        validator.validate(o, errors);
        if (errors.hasErrors()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Error updating Organization at validate: " + errors.getAllErrors());
        }
        organizationService.update(organizationId, o);

        return ResponseEntity.ok("Organization updated successfully");
    }

    @DeleteMapping("/{organizationId}")
    public ResponseEntity<?> deleteOrganization(@PathVariable String organizationId) {

        boolean isRemoved;
        isRemoved = organizationService.delete(organizationId);
        if (!isRemoved) {
            return ResponseEntity.badRequest().body("Organization " + organizationId + " not found");
        } else {
            return ResponseEntity.ok("Organization " + organizationId + "delete successfully");
        }
    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<OrganizationDTO> items) {

        for (OrganizationDTO dto : items) {
            Errors errors = new BeanPropertyBindingResult(dto, "organization");
            validator.validate(dto, errors);
            if (errors.hasErrors()) {
                throw new oeapiException(HttpStatus.NOT_FOUND, "Error creating Organization at validate: " + errors.getAllErrors());
            }

            super.createDTO(dto, organizationService);

        }
        return ResponseEntity.ok("Organizations created successfully");

    }

}
