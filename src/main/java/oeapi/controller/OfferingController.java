package oeapi.controller;

import java.util.ArrayList;
import oeapi.controller.requestparameters.oeapiRequestParam;
import java.util.Map;
import java.util.Optional;
import oeapi.controller.requestparameters.oeapiGroupRequestParam;
import oeapi.controller.requestparameters.oeapiOfferingRequestParam;
import oeapi.model.Course;
import oeapi.model.CourseOffering;
import oeapi.model.Offering;
import oeapi.oeapiException;
import oeapi.service.CourseService;
import oeapi.service.OfferingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/offerings")
public class OfferingController extends oeapiController<Offering> {

    static Logger logger = LoggerFactory.getLogger(OfferingController.class);

    @Autowired
    private OfferingService offeringService;

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute oeapiOfferingRequestParam requestParam) {

        // return super.getResponse(requestParam.toPageable(Arrays.asList("offeringId", "name")), new ArrayList());
        Map.Entry<String, String> filter = requestParam.getFilter();

        if (filter == null) {
            return super.getAll(requestParam.toPageable(), offeringService);
            //return super.getAllByPrimaryCode(requestParam.getPrimaryCode(), requestParam, service)
        }
        if (filter.getKey() == "primaryCode") {
            return super.getAllByPrimaryCode(filter.getValue(), requestParam.toPageable(), offeringService);
        } else {
            return super.getAllByFieldValue(filter.getKey(), filter.getValue(), requestParam.toPageable(), offeringService);
        }
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id) {

        Optional<Offering> o = offeringService.getById(id);
        if (!o.isPresent()) {
            //return this.NotFound(id, offeringService);
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Offering with Id: " + id);
        }
        return super.get(id, offeringService);

    }

    @GetMapping(value = "/{id}/associations", produces = "application/json")
    public ResponseEntity<?> getAssociations(@PathVariable String id) {
        Optional<Offering> o = offeringService.getById(id);
        //if (!o.isPresent()) {
        //return super.NotFound(id);
        //throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Offering with Id: " + id);
        //}
        return super.getResponse(new ArrayList());
        //return super.getResponse(requestParam.toPageable(), new ArrayList());
    }

    @GetMapping(value = "/{id}/groups", produces = "application/json")
    public ResponseEntity<?> getGroups(@PathVariable String id, @ModelAttribute oeapiGroupRequestParam requestParam) {

        Optional<Offering> o = offeringService.getById(id);
        //if (!o.isPresent()) {
        //    return super.NotFound(id);
        //    //throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Offering with Id: " + id);
        //}
        Map.Entry<String, String> filter = requestParam.getFilter();

        return super.getResponse(requestParam.toPageable(), new ArrayList());

    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Offering offering) {
        return super.create(offering, offeringService);
    }

    @PostMapping(value = "/courseOffering", produces = "application/json")
    public ResponseEntity<?> createCourseOffering(@RequestBody CourseOffering courseOffering) {

        logger.debug("Creating CourseOffering...");

        manageOfferingCourse(courseService, courseOffering, true);

        return super.create(courseOffering, offeringService);
    }

    
    @DeleteMapping("/{offeringId}")
    public ResponseEntity<?> delete(@PathVariable String offeringId) {

        Optional<Offering> existing = offeringService.getById(offeringId);

        if (existing.isPresent()) {
            offeringService.delete(offeringId);  
            return ResponseEntity.ok().build();
        } else {
            // return super.NotFound(courseId);
           throw new oeapiException(HttpStatus.NOT_FOUND, "Error deleting Offering with Id: " + offeringId);
        }
    }
           
    
    @DeleteMapping("/deleteByCourseId/{courseId}")
    public ResponseEntity<?> deleteByCourseId(@PathVariable String courseId) {

        try {
            Optional<Course> existing = courseService.getById(courseId);
            if (existing.isPresent()) {
                offeringService.deleteByCourse((Course) existing.get());
                return ResponseEntity.ok().build();
            } else {
                throw new oeapiException(HttpStatus.NOT_FOUND, "Error: Trying to delete offering of not existing course with Id: " + courseId);
            }
          } catch (Exception ex)
             {  
                throw new oeapiException(HttpStatus.NOT_FOUND, "Error trying to delete offering of course with Id: " + courseId + "Reason: "+ex.getLocalizedMessage());
             }
    }
    

    public void manageOfferingCourse(CourseService courseService, CourseOffering courseOffering, boolean autoCreateCourseIfNotExists) {

        logger.debug("Creating CourseOffering, Checking if course exists..");

        String courseId = "";
        Course theCourse = null;

        if (courseOffering.getCourse() != null) {
            courseId = courseOffering.getCourse().getCourseId();
            logger.debug("--manageOfferingCourse Checking course ----> " + courseOffering.getCourse().getCourseId());
            Optional<Course> courseExisting = courseService.getById(courseId);
            if (!courseExisting.isPresent()) {
                if (autoCreateCourseIfNotExists) {
                    theCourse = courseService.autoGenerateBasicItem(courseId);
                } else {
                    throw new oeapiException(HttpStatus.BAD_REQUEST, "Course not found", "Course [" + courseId + "] not found");
                }
            } else {
                theCourse = courseExisting.get();
            }
        } // no course given
        else {
            logger.debug("--Creating CourseOffering, No course given or found for courseOffering.getCourse().getCourseId() = " + courseId);
            if (autoCreateCourseIfNotExists && courseId != null) {
                theCourse = courseService.autoGenerateBasicItem(courseId);
            }
        }

        if (theCourse != null) {
            logger.debug("--Exiting CourseOffering, default autgenerated course attached to CourseOffering: " + courseId);
            courseOffering.setCourse(theCourse);
        } else {
            logger.debug("--Exiting CourseOffering, No info about course detected. None informed and autogenerated is off");
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Course not found", "No info about course detected. None informed and autogenerated is off");
        }

    }

}
