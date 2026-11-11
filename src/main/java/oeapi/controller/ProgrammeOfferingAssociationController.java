package oeapi.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import oeapi.model.ProgrammeOffering;
import oeapi.payload.ProgrammeOfferingAssociationDTO;

/**
 * OEAPI v6 /programme-offering-associations.
 */
@RestController
@RequestMapping("/programme-offering-associations")
public class ProgrammeOfferingAssociationController extends oeapiTypedAssociationController<ProgrammeOffering, ProgrammeOfferingAssociationDTO> {

    @Override
    protected Class<ProgrammeOffering> offeringType() {
        return ProgrammeOffering.class;
    }

    @Override
    protected Class<ProgrammeOfferingAssociationDTO> dtoType() {
        return ProgrammeOfferingAssociationDTO.class;
    }

    @Override
    protected String offeringExpandName() {
        return "programme_offering";
    }

    @Override
    protected Object offeringDTO(ProgrammeOffering offering) {
        return offeringService.programmeOfferingMapper().toDTO(offering);
    }

    /** OEAPI v6: the authenticated person associates themselves with an offering. */
    @PostMapping(value = "/external/me", produces = "application/json")
    public ResponseEntity<?> externalMe(@RequestBody ProgrammeOfferingAssociationDTO dto, Principal principal) {
        return createForMe(dto, principal);
    }
}
