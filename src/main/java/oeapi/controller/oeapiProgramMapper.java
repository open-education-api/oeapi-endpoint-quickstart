package oeapi.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oeapi.model.Program;
import oeapi.model.oeapiFieldsOfStudy;
import oeapi.payload.ProgramDTO;
import oeapi.repository.oeapiFieldsOfStudyRepository;
import oeapi.service.oeapiEnumConversionService;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiProgramMapper extends oeapiDTOMapper<Program, ProgramDTO> {

    static Logger logger = LoggerFactory.getLogger(oeapiProgramMapper.class);

    private final List<String> enumFields = Arrays.asList("level");

    private oeapiFieldsOfStudyRepository fosRepo;

    public oeapiProgramMapper(oeapiEnumConversionService ooapiEnumService, List<String> enumFields) {
        super(Program.class, ProgramDTO.class, ooapiEnumService, enumFields);

    }

    public oeapiProgramMapper(oeapiEnumConversionService ooapiEnumService, List<String> enumFields, oeapiFieldsOfStudyRepository fieldsOfStudyRepository) {
        super(Program.class, ProgramDTO.class, ooapiEnumService, enumFields);
        this.fosRepo = fieldsOfStudyRepository;
    }

    public void init() {
        super.setEnumFields(enumFields);
    }

    @Override
    public Program toEntity(ProgramDTO dto) {
        Program c = super.toEntity(dto);
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
