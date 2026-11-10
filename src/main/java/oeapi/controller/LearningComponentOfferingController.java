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
}
