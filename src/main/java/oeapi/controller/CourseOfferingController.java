package oeapi.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import oeapi.controller.requestparameters.oeapiOfferingRequestParam;
import oeapi.model.CourseOffering;
import oeapi.payload.CourseOfferingDTO;

/**
 * OEAPI v6 /course-offerings.
 */
@RestController
@RequestMapping("/course-offerings")
public class CourseOfferingController extends oeapiTypedOfferingController<CourseOffering, CourseOfferingDTO> {

    @Override
    protected String typeName() {
        return "CourseOffering";
    }

    @Override
    protected Optional<CourseOffering> find(String id) {
        return offeringService.getCourseOffering(id);
    }

    @Override
    protected oeapiDTOMapper<CourseOffering, CourseOfferingDTO> mapper() {
        return offeringService.courseOfferingMapper();
    }

    @GetMapping(value = "/{id}/learning-component-offerings", produces = "application/json")
    public ResponseEntity<?> getLearningComponentOfferings(@PathVariable String id, @ModelAttribute oeapiOfferingRequestParam requestParam) {
        find(id).orElseThrow(() -> notFound(id));
        return oeapiOfferingLists.page(requestParam,
                offeringService.learningComponentOfferingMapper().toDTOList(offeringService.getByCourseOfferingId(id)));
    }

    @org.springframework.beans.factory.annotation.Autowired
    private oeapi.service.OfferingAssociationService associationService;

    @org.springframework.web.bind.annotation.GetMapping(value = "/{id}/course-offering-associations", produces = "application/json")
    public org.springframework.http.ResponseEntity<?> getAssociations(@org.springframework.web.bind.annotation.PathVariable String id,
            @org.springframework.web.bind.annotation.ModelAttribute oeapi.controller.requestparameters.oeapiAssociationRequestParam requestParam) {
        find(id).orElseThrow(() -> notFound(id));
        return oeapiOfferingLists.page(requestParam,
                oeapiTypedAssociationController.listForOffering(associationService, id, requestParam));
    }
}
