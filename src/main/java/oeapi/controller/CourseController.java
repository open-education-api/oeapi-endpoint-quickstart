package oeapi.controller;

import oeapi.controller.requestparameters.oeapiCourseRequestParam;
import oeapi.controller.requestparameters.oeapiComponentRequestParam;
import oeapi.controller.requestparameters.oeapiOfferingRequestParam;
import java.util.ArrayList;
import java.util.Arrays;
import oeapi.model.Course;
import oeapi.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.validation.Valid;

import oeapi.model.Component;
import oeapi.model.CourseOffering;
import oeapi.model.Offering;
import oeapi.oeapiException;
import oeapi.oeapiObjectsValidator;
import oeapi.payload.ComponentDTO;
import oeapi.payload.CourseDTO;
import oeapi.payload.CourseOfferingDTO;

import oeapi.service.ComponentService;
import oeapi.service.OfferingService;
import oeapi.service.oeapiEnumConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The type Courses controller.
 *
 * @author Carlos Alonso - losalo@unavarra.es
 */
@RestController
@RequestMapping("/courses")
public class CourseController extends oeapiDTOController<Course, CourseDTO> implements oeapiDTOControllerInterface<Course, CourseDTO> {

    Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private oeapiObjectsValidator validator;

    @Autowired
    private CourseService courseService;

    private oeapiDTOMapper<CourseOffering, CourseOfferingDTO> mapper;

    @Autowired
    private oeapiEnumConversionService enumService;

    @Autowired
    private OfferingService offeringService;

    @Autowired
    private ComponentService componentService;

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute oeapiCourseRequestParam requestParam) {
        Map.Entry<String, String> filter = requestParam.getFilter();
        return super.getAll(filter, requestParam.toPageable(), courseService);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id, @RequestParam(required = false) String expand) {
        /*
        Optional<Course> o = courseService.getById(id);
        if (!o.isPresent()) {
            return super.NotFound(id);
        } else {
            String dtoOutput = courseService.toDTO(o.get(), expand);
            return ResponseEntity.ok(dtoOutput);

        }
         */
        return super.get(id, courseService);

        //return super.get(id, expand, courseService);
    }

    //V6
    //@GetMapping(value = "/{id}/course-offerings")
    //V5
    @GetMapping(value = "/{id}/offerings")
    public ResponseEntity<?> getOfferings(@PathVariable String id, @ModelAttribute oeapiOfferingRequestParam requestParam) {

        Optional<Course> existing = courseService.getById(id);
        if (!existing.isPresent()) {
            // return super.NotFound(id);
            throw new oeapiException(HttpStatus.NOT_FOUND, "There are no course-offerings for Id: " + id);            
        }
        List<CourseOffering> courseOfferings = offeringService.getByCourseId(id);

        // If there is no CourseOffering, return a basic autogen offering
        if (courseOfferings.isEmpty()) {
            List<Offering> offerings = new ArrayList<>();
            offerings.add(offeringService.autoGenerateBasicItem(id));
            return super.getResponse(requestParam, offerings);
        }
        mapper = new oeapiDTOMapper(CourseOffering.class, CourseOfferingDTO.class, enumService, Arrays.asList());

        List<CourseOfferingDTO> dtos = mapper.toDTOList(courseOfferings);
        Pageable pageable = PageRequest.of(0, 100);

        oeapiResponse response = new oeapiResponse(dtos, pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    //V6
    //@GetMapping(value = "/{id}/learning-components")
    //V5
    @GetMapping(value = "/{id}/components")
    public ResponseEntity<?> getComponents(@PathVariable String id, @ModelAttribute oeapiComponentRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter();
        Optional<Course> existing = courseService.getById(id);
        if (!existing.isPresent()) {
            // return super.NotFound(id);
           throw new oeapiException(HttpStatus.NOT_FOUND, "There are no learning-components for Id: " + id);            
        }
        Pageable pageable = requestParam.toPageable();
        Page<ComponentDTO> components = componentService.toDTOPages(componentService.getByCourseId(id, filter, pageable));
        // Temporal
        //
        if (!components.hasContent()) {
            List<Component> defComponents = new ArrayList<>();
            defComponents.add(componentService.autoGenerateBasicItem(id));
            return super.getResponse(requestParam, defComponents);
        } else {
            return super.getResponse(components);
        }

    }

    @PostMapping
    @Override
    public ResponseEntity<?> createFromDTO(@Valid @RequestBody CourseDTO o) {

        return super.createDTO(o, courseService);

    }

    //public ResponseEntity<?> create(@RequestBody CourseDTO o) {
    //    return super.create(o, courseService);
    //}
    @PutMapping(value = "/{courseId}")
    @Override
    public ResponseEntity<?> updateFromDTO(@PathVariable String courseId, @Valid @RequestBody CourseDTO dto) {

        dto.setCourseId(courseId);
        Errors errors = new BeanPropertyBindingResult(dto, "course");
        validator.validate(dto, errors);
        if (errors.hasErrors()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Error creating Course at validate: " + errors.getAllErrors());
        }

        return super.createOrUpdate(courseId, dto, courseService);
    }

    /*
    public ResponseEntity<?> updateCourse(@PathVariable String courseId, @RequestBody CourseDTO course) {

        course.setCourseId(courseId);
        try {
            Optional<Course> existing = courseService.getById(courseId);
            if (existing.isPresent()) {
                return super.update(course, courseService);
            } else {
                return super.create(course, courseService);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
     */
    // Igual no es necesario
    @PostMapping(value = "/{courseId}/update", produces = "application/json")
    public ResponseEntity<?> postCourse(@PathVariable String courseId, @Valid
            @RequestBody Course course
    ) {

        Errors errors = new BeanPropertyBindingResult(course, "course");
        super.Validate(course, errors);

        if (errors.hasErrors()) {
            //return ResponseEntity.badRequest().body(errors.getAllErrors());
            throw new oeapiException(HttpStatus.NOT_FOUND, errors.getAllErrors().toString());

        }

        logger.debug("Update by post: Course is valid");

        /* Check course Id  */
        Optional<Course> existing = courseService.getById(courseId);

        if (existing.isPresent()) {

            logger.debug("Update by post: Ok, previous Course exists");

//     In case the update is done with a JSON with less attributes
//     or just the ones we want to update, something like could be done:
//
//           CourseDTO updatedCourse = existing.get();
//           logger.debug("Updating by post: Update entity fields");
//           copyNonNullProperties(course, updatedCourse);
//
//     Enforce to mantain same UID, just in case
//           updatedCourse.setCourseId(courseId);
            course.setCourseId(existing.get().getCourseId());
            logger.debug("Updating by post: Calling Service to update BD");
            return super.update(course, courseService);

        } else {
            return super.createErrorResponse(HttpStatus.BAD_REQUEST, "Course does not exist", "Course [" + courseId + "] exists");
        }
    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByCoursesJSON(@RequestBody List<CourseDTO> items
    ) {
        return super.createByJSON(items, courseService);
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable String courseId) {

        Optional<Course> existing = courseService.getById(courseId);

        if (existing.isPresent()) {
            courseService.delete(courseId);  // delete course in service will take care of deleting all related objects
            return ResponseEntity.ok().build();
        } else {
            // return super.NotFound(courseId);
           throw new oeapiException(HttpStatus.NOT_FOUND, "Error deleteCourse with Id: " + courseId);

        }

    }

}
