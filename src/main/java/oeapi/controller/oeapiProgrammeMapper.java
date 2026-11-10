package oeapi.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oeapi.model.Programme;
import oeapi.model.oeapiFieldsOfStudy;
import oeapi.payload.ProgrammeDTO;
import oeapi.repository.oeapiFieldsOfStudyRepository;
import oeapi.service.oeapiEnumConversionService;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiProgrammeMapper extends oeapiDTOMapper<Programme, ProgrammeDTO> {

    static Logger logger = LoggerFactory.getLogger(oeapiProgrammeMapper.class);

    private final List<String> enumFields = Arrays.asList("level");

    private oeapiFieldsOfStudyRepository fosRepo;

    public oeapiProgrammeMapper(oeapiEnumConversionService ooapiEnumService, List<String> enumFields) {
        super(Programme.class, ProgrammeDTO.class, ooapiEnumService, enumFields);

    }

    public oeapiProgrammeMapper(oeapiEnumConversionService ooapiEnumService, List<String> enumFields, oeapiFieldsOfStudyRepository fieldsOfStudyRepository) {
        super(Programme.class, ProgrammeDTO.class, ooapiEnumService, enumFields);
        this.fosRepo = fieldsOfStudyRepository;
    }

    public void init() {
        super.setEnumFields(enumFields);
    }

    @Override
    public Programme toEntity(ProgrammeDTO dto) {
        Programme c = super.toEntity(dto);
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
