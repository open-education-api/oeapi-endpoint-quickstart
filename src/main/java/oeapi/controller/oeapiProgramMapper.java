package oeapi.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    /** parentIds -> (parentId -> direct children); supplied by ProgramService.findChildren. */
    private Function<Collection<String>, Map<String, List<Program>>> childrenFinder;

    public oeapiProgramMapper(oeapiEnumConversionService ooapiEnumService, List<String> enumFields) {
        super(Program.class, ProgramDTO.class, ooapiEnumService, enumFields);

    }

    public oeapiProgramMapper(oeapiEnumConversionService ooapiEnumService, List<String> enumFields, oeapiFieldsOfStudyRepository fieldsOfStudyRepository) {
        super(Program.class, ProgramDTO.class, ooapiEnumService, enumFields);
        this.fosRepo = fieldsOfStudyRepository;
    }

    public oeapiProgramMapper(oeapiEnumConversionService ooapiEnumService, List<String> enumFields,
                              oeapiFieldsOfStudyRepository fieldsOfStudyRepository,
                              Function<Collection<String>, Map<String, List<Program>>> childrenFinder) {
        this(ooapiEnumService, enumFields, fieldsOfStudyRepository);
        this.childrenFinder = childrenFinder;
    }

    @Override
    public ProgramDTO toDTO(Program e) {
        if (e == null) {
            return null;
        }
        return toDTO(e, findChildren(Collections.singletonList(e.getProgramId())));
    }

    /** One children query for the whole list instead of one per program. */
    @Override
    public List<ProgramDTO> toDTOList(List<Program> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        Map<String, List<Program>> children = findChildren(
                items.stream().map(Program::getProgramId).collect(Collectors.toList()));

        return items.stream().map(e -> toDTO(e, children)).collect(Collectors.toList());
    }

    private ProgramDTO toDTO(Program e, Map<String, List<Program>> childrenByParent) {
        ProgramDTO dto = super.toDTO(e);
        // Always a list, never null: without expand an empty one is dropped by
        // @JsonInclude(NON_EMPTY), with ?expand=children it answers [].
        dto.setChildren(childrenByParent.getOrDefault(e.getProgramId(), new ArrayList<>()));
        return dto;
    }

    private Map<String, List<Program>> findChildren(Collection<String> parentIds) {
        return (childrenFinder == null) ? Collections.emptyMap() : childrenFinder.apply(parentIds);
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
