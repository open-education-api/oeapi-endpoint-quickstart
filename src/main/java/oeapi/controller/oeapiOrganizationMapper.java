package oeapi.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import oeapi.model.Organization;
import oeapi.payload.OrganizationDTO;
import oeapi.service.oeapiEnumConversionService;

/**
 *
 * @author itziar.urrutia
 */
@Component
public class oeapiOrganizationMapper extends oeapiDTOMapper<Organization, OrganizationDTO> {
    private final List<String> enumFields = Arrays.asList("organizationType");

    public oeapiOrganizationMapper(oeapiEnumConversionService ooapiEnumService, List<String> enumFields) {
        super(Organization.class, OrganizationDTO.class, ooapiEnumService, enumFields);
    }

    public void init() {
        super.setEnumFields(enumFields);
        throw new RuntimeException("INIT!");
    }

    @Override
    public Page<OrganizationDTO> toPageDTO(Page<Organization> pages) {
        return pages.map(this::toDTO);
    }
}
