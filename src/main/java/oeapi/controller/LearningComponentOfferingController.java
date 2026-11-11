package oeapi.controller;

import java.util.Optional;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import oeapi.model.ComponentOffering;
import oeapi.payload.LearningComponentOfferingDTO;

/**
 * OEAPI v6 /learning-component-offerings.
 */
@RestController
@RequestMapping("/learning-component-offerings")
public class LearningComponentOfferingController extends oeapiTypedOfferingController<ComponentOffering, LearningComponentOfferingDTO> {

    @Override
    protected String typeName() {
        return "LearningComponentOffering";
    }

    @Override
    protected Optional<ComponentOffering> find(String id) {
        return offeringService.getLearningComponentOffering(id);
    }

    @Override
    protected oeapiDTOMapper<ComponentOffering, LearningComponentOfferingDTO> mapper() {
        return offeringService.learningComponentOfferingMapper();
    }

    @org.springframework.beans.factory.annotation.Autowired
    private oeapi.service.OfferingAssociationService associationService;

    @org.springframework.web.bind.annotation.GetMapping(value = "/{id}/learning-component-offering-associations", produces = "application/json")
    public org.springframework.http.ResponseEntity<?> getAssociations(@org.springframework.web.bind.annotation.PathVariable String id,
            @org.springframework.web.bind.annotation.ModelAttribute oeapi.controller.requestparameters.oeapiAssociationRequestParam requestParam) {
        find(id).orElseThrow(() -> notFound(id));
        return oeapiOfferingLists.page(requestParam,
                oeapiTypedAssociationController.listForOffering(associationService, id, requestParam));
    }
}
