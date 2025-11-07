package oeapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import oeapi.model.Organization;
import oeapi.payload.OrganizationDTO;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import oeapi.service.oeapiEnumConversionService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 *
 * @author itziar.urrutia
 */
@Component
public class oeapiOrganizationMapper extends oeapiDTOMapper<Organization, OrganizationDTO> {

    private ObjectMapper objectMapper = ooapiObjectMapper();

    // First aproach with organization
    private final List<String> expandableFields = Arrays.asList("organization");
    private final List<String> enumFields = Arrays.asList("organizationType");
    //private final List<String> expandableFields = Arrays.asList("organization", "coordinators", "programs", "educationSpecification");

    public oeapiOrganizationMapper(oeapiEnumConversionService ooapiEnumService, List<String> enumFields) {
        super(Organization.class, OrganizationDTO.class, ooapiEnumService, enumFields);
    }

    public void init() {
        super.setEnumFields(enumFields);

    }

    /*
    public ObjectNode Expand(ObjectNode courseNode, String expand) {

        // The courseDTO has expandableFieldId property and expandableFieldObject property
        // CourseDTO has defined the p
        // ExpandableFieldObject property has the Object
        // ExpandableFieldId property has the Object Id (uuid)
        // This code is used when GET courses/<id>?expand=xxxx
        if (expand != null && !expand.isEmpty()) {
            String[] expandFields = expand.split(",");
            for (String fieldToExpand : expandFields) {
                if (courseNode.has(fieldToExpand + "Object")) {
                    courseNode.set(fieldToExpand, courseNode.remove(fieldToExpand + "Object"));
                }
            }
        }
        ObjectNode cleanNode = this.Clean(courseNode);

        return cleanNode;
    }

    /*
    public ObjectNode Clean(ObjectNode courseNode) {

        // This method cleans the tree removing the *Object nodes.
        // After expand, these nodes haven't to be in the output.
        for (String fieldExpandable : expandableFields) {
            if (courseNode.has(fieldExpandable + "Object")) {
                courseNode.remove(fieldExpandable + "Object");
                //courseNode.set(fieldExpandable + "Id", courseNode.remove(fieldExpandable));
            } else {
                if (courseNode.has(fieldExpandable + "Id")) {
                    courseNode.remove(fieldExpandable + "Id");
                }
            }
        }

        return courseNode;
    }

    public CourseDTO CleanObject(CourseDTO dto) {

        // This method cleans the DTO setting to null the Object properties.
        // This properties hasn't to be in the JSON output
        // This clean is necessary for: get courses
        // First aproach with organization
        dto.setOrganization(null);
        return dto;

    }

    public List<CourseDTO> Clean(List<CourseDTO> dtoList) {
        return dtoList.stream()
                .map(this::CleanObject)
                .collect(Collectors.toList());
    }

    public Page<CourseDTO> Clean(Page<CourseDTO> dtoPage) {
        // Extract the content (List<CourseDTO>) from the Page
        List<CourseDTO> cleanedList = dtoPage.getContent().stream()
                .map(this::CleanObject)
                .collect(Collectors.toList());

        // Return a new Page with the cleaned list and the same pagination information
        return new PageImpl<>(cleanedList, dtoPage.getPageable(), dtoPage.getTotalElements());
    }

     */
    @Override
    public OrganizationDTO toDTO(Organization item) {
        return super.toDTO(item);
    }

    public String toDTO(Organization item, String expand) {
        return "";
        /*
        OrganizationDTO dto = super.toDTO(item, enumFields);

        String output = null;
        ObjectNode itemNode = objectMapper.valueToTree(dto);
        ObjectNode expandedNode = this.Expand(itemNode, expand);
        try {

            return objectMapper.writeValueAsString(expandedNode);
        } catch (Exception e) {

        }
        return output;

         */
    }

    @Override
    public Organization toEntity(OrganizationDTO item) {

        return super.toEntity(item);
    }

    @Override
    public List<OrganizationDTO> toDTOList(List<Organization> items) {
        return super.toDTOList(items);
    }

    @Override
    public Page<OrganizationDTO> toPageDTO(Page<Organization> pages) {
        Page<OrganizationDTO> dtoPages = pages.map(this::toDTO);
        return dtoPages;
    }
}
