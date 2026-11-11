package oeapi.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import oeapi.model.CourseOffering;
import oeapi.payload.CourseOfferingAssociationDTO;

/**
 * OEAPI v6 /course-offering-associations.
 */
@RestController
@RequestMapping("/course-offering-associations")
public class CourseOfferingAssociationController extends oeapiTypedAssociationController<CourseOffering, CourseOfferingAssociationDTO> {

    @Override
    protected Class<CourseOffering> offeringType() {
        return CourseOffering.class;
    }

    @Override
    protected Class<CourseOfferingAssociationDTO> dtoType() {
        return CourseOfferingAssociationDTO.class;
    }

    @Override
    protected String offeringExpandName() {
        return "course_offering";
    }

    @Override
    protected Object offeringDTO(CourseOffering offering) {
        return offeringService.courseOfferingMapper().toDTO(offering);
    }

    /** OEAPI v6: the authenticated person associates themselves with an offering. */
    @PostMapping(value = "/external/me", produces = "application/json")
    public ResponseEntity<?> externalMe(@RequestBody CourseOfferingAssociationDTO dto, Principal principal) {
        return createForMe(dto, principal);
    }
}
