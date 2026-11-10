package oeapi.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import oeapi.model.Organisation;
import oeapi.payload.OrganisationDTO;
import oeapi.service.oeapiEnumConversionService;

/**
 *
 * @author itziar.urrutia
 */
@Component
public class oeapiOrganisationMapper extends oeapiDTOMapper<Organisation, OrganisationDTO> {
    private final List<String> enumFields = Arrays.asList("organisationType");

    public oeapiOrganisationMapper(oeapiEnumConversionService ooapiEnumService, List<String> enumFields) {
        super(Organisation.class, OrganisationDTO.class, ooapiEnumService, enumFields);
    }

    public void init() {
        super.setEnumFields(enumFields);
        throw new RuntimeException("INIT!");
    }

    @Override
    public Page<OrganisationDTO> toPageDTO(Page<Organisation> pages) {
        return pages.map(this::toDTO);
    }
}
