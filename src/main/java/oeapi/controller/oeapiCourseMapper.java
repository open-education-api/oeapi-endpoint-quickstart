package oeapi.controller;

import static oeapi.oeapiUtils.debugJSON;
import static oeapi.oeapiUtils.ooapiObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import oeapi.model.Course;
import oeapi.model.oeapiFieldsOfStudy;
import oeapi.payload.CourseDTO;
import oeapi.repository.oeapiFieldsOfStudyRepository;
import oeapi.service.oeapiEnumConversionService;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiCourseMapper extends oeapiDTOMapper<Course, CourseDTO> {

    static Logger logger = LoggerFactory.getLogger(oeapiCourseMapper.class);

    private final List<String> enumFields = Arrays.asList("level");

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

    @Override
    public Course toEntity(CourseDTO dto) {
        Course c = super.toEntity(dto);
        List<oeapiFieldsOfStudy> fos = new ArrayList<oeapiFieldsOfStudy>() { };

        if (dto.getFieldsOfStudyId() != null) {
            fos = fosRepo.findByTxtEn(dto.getFieldsOfStudyId());
        }
        if (!fos.isEmpty()) {
            c.setFieldsOfStudyId(fos.get(0).getFieldsOfStudyId());
        }
        return c;
    }
}
