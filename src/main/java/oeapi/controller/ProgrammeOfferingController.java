package oeapi.controller;

import java.util.Optional;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import oeapi.model.ProgrammeOffering;
import oeapi.payload.ProgrammeOfferingDTO;

/**
 * OEAPI v6 /programme-offerings.
 */
@RestController
@RequestMapping("/programme-offerings")
public class ProgrammeOfferingController extends oeapiTypedOfferingController<ProgrammeOffering, ProgrammeOfferingDTO> {

    @Override
    protected String typeName() {
        return "ProgrammeOffering";
    }

    @Override
    protected Optional<ProgrammeOffering> find(String id) {
        return offeringService.getProgrammeOffering(id);
    }

    @Override
    protected oeapiDTOMapper<ProgrammeOffering, ProgrammeOfferingDTO> mapper() {
        return offeringService.programmeOfferingMapper();
    }
}
