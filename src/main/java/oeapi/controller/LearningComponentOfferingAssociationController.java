package oeapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import oeapi.model.ComponentOffering;
import oeapi.payload.LearningComponentOfferingAssociationDTO;

/**
 * OEAPI v6 /learning-component-offering-associations.
 */
@RestController
@RequestMapping("/learning-component-offering-associations")
public class LearningComponentOfferingAssociationController extends oeapiTypedAssociationController<ComponentOffering, LearningComponentOfferingAssociationDTO> {

    @Override
    protected Class<ComponentOffering> offeringType() {
        return ComponentOffering.class;
    }

    @Override
    protected Class<LearningComponentOfferingAssociationDTO> dtoType() {
        return LearningComponentOfferingAssociationDTO.class;
    }

    @Override
    protected String offeringExpandName() {
        return "learning_component_offering";
    }

    @Override
    protected Object offeringDTO(ComponentOffering offering) {
        return offeringService.learningComponentOfferingMapper().toDTO(offering);
    }
}
