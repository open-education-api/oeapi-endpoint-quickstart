package oeapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import oeapi.model.Course;
import oeapi.model.oeapiFieldsOfStudy;
import oeapi.oeapiUtils;
import static oeapi.oeapiUtils.debugJSON;
import oeapi.payload.CourseDTO;
import oeapi.service.oeapiEnumConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static oeapi.oeapiUtils.ooapiObjectMapper;
import oeapi.repository.oeapiFieldsOfStudyRepository;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiCourseMapper extends oeapiDTOMapper<Course, CourseDTO> {

    static Logger logger = LoggerFactory.getLogger(oeapiCourseMapper.class);

    private ObjectMapper objectMapper = ooapiObjectMapper();

    // First aproach with organization
    private final List<String> expandableFields = Arrays.asList("organization");
    private final List<String> enumFields = Arrays.asList("level");
    //private final List<String> expandableFields = Arrays.asList("organization", "coordinators", "programs", "educationSpecification");

    private oeapiFieldsOfStudyRepository fosRepo;

    public oeapiCourseMapper(oeapiEnumConversionService ooapiEnumService, List<String> enumFields) {
        super(Course.class, CourseDTO.class, ooapiEnumService, enumFields);

    }

    public oeapiCourseMapper(oeapiEnumConversionService ooapiEnumService, List<String> enumFields, oeapiFieldsOfStudyRepository fieldsOfStudyRepository) {
        super(Course.class, CourseDTO.class, ooapiEnumService, enumFields);
        this.fosRepo = fieldsOfStudyRepository;
    }

    public void init() {
        super.setEnumFields(enumFields);
    }

    /* This methods have to be with Expand output */
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

    public ObjectNode Clean(ObjectNode courseNode) {

        // This method cleans the tree removing the *Object nodes.
        // After expand, these nodes haven't to be in the output.
        if (courseNode == null) {
            logger.warn("\n\nCalling Clean with NULL courseNode!");
        } else {
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
        }
        return courseNode;
    }

    public Course CleanObject(Course dto) {

        // This method cleans the DTO setting to null the Object properties.
        // This properties hSasn't to be in the JSON output
        // This clean is necessary for: get courses
        // First aproach with organization
        dto.setOrganization(null);
        return dto;

    }

    public List<Course> Clean(List<Course> dtoList) {
        return dtoList.stream()
                .map(this::CleanObject)
                .collect(Collectors.toList());
    }

    public Page<Course> Clean(Page<Course> dtoPage) {
        // Extract the content (List<CourseDTO>) from the Page
        List<Course> cleanedList = dtoPage.getContent().stream()
                .map(this::CleanObject)
                .collect(Collectors.toList());

        // Return a new Page with the cleaned list and the same pagination information
        return new PageImpl<>(cleanedList, dtoPage.getPageable(), dtoPage.getTotalElements());
    }

    public String toDTO(Course course, String expand) {

        CourseDTO dto = super.toDTO(course);
        String output = "{}";

        ObjectNode courseNode = null;
        ObjectNode expandedNode = null;

        try {
            courseNode = objectMapper.valueToTree(dto);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("-!-!-!-Failed to serialize DTO to JSON: " + debugJSON(dto));
        }

        if (courseNode == null) {
            logger.warn("\n\nTrying to expand in toDTO with NULL courseNode!");
        } else {
            try {
                expandedNode = this.Expand(courseNode, expand);
                output = objectMapper.writeValueAsString(expandedNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return output;
    }

    @Override
    public Page<CourseDTO> toPageDTO(Page<Course> pages) {
        Page<CourseDTO> dtoPages = pages.map(this::toDTO);
        return dtoPages;
    }

    @Override
    public Course toEntity(CourseDTO dto) {
        Course c = super.toEntity(dto);
        List<oeapiFieldsOfStudy> fos = new ArrayList() {
        };
        if (dto.getFieldsOfStudyId() != null) {
            fos = fosRepo.findByTxtEn(dto.getFieldsOfStudyId());
        }

        if (!fos.isEmpty()) {
            c.setFieldsOfStudyId(fos.get(0).getFieldsOfStudyId());
        }
        return c;
    }

    @Override
    public CourseDTO toDTO(Course c) {
        CourseDTO dto = super.toDTO(c);
        
        /* Uncomment this if you want to show description of fieldsOfStudy instead of its code 
        Optional<ooapiFieldsOfStudy> fos = Optional.empty();
        if (c.getFieldsOfStudyId() != null) {
            fos = fosRepo.findById(c.getFieldsOfStudyId());
        }
        if (fos.isPresent()) {
            dto.setFieldsOfStudy(fos.get().getTxtEn());
        }
        */
        
        return dto;
    }
}
