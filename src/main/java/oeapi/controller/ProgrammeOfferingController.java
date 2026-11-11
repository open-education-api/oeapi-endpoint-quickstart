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

    @org.springframework.beans.factory.annotation.Autowired
    private oeapi.service.OfferingAssociationService associationService;

    @org.springframework.web.bind.annotation.GetMapping(value = "/{id}/programme-offering-associations", produces = "application/json")
    public org.springframework.http.ResponseEntity<?> getAssociations(@org.springframework.web.bind.annotation.PathVariable String id,
            @org.springframework.web.bind.annotation.ModelAttribute oeapi.controller.requestparameters.oeapiAssociationRequestParam requestParam) {
        find(id).orElseThrow(() -> notFound(id));
        return oeapiOfferingLists.page(requestParam,
                oeapiTypedAssociationController.listForOffering(associationService, id, requestParam));
    }
}
