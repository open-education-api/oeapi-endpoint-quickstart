package oeapi.service;

import java.util.ArrayList;
import java.util.Arrays;

import oeapi.model.Course;
import oeapi.repository.CourseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import java.util.stream.Collectors;
import javax.transaction.Transactional;

import oeapi.controller.oeapiCourseMapper;

import oeapi.model.ModeOfDelivery;
import oeapi.model.Organization;
import oeapi.model.Person;
import oeapi.model.Program;
import oeapi.model.StudyLoad;
import oeapi.model.oeapiFieldsOfStudy;
import oeapi.model.oeapiIdentifierEntry;
import oeapi.oeapiException;
import oeapi.payload.CourseDTO;
import oeapi.repository.ModeOfDeliveryRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static oeapi.oeapiUtils.defaultDescription;
import oeapi.repository.oeapiFieldsOfStudyRepository;
import oeapi.repository.oeapiStudyLoadRepository;

@Service
@Transactional
public class CourseService extends oeapiEndpointDTOService<Course, CourseRepository, CourseDTO> implements oeapiRelatedObjServiceInterface<Course>, oeapiRelationalModelInterface<Course> {

    Logger logger = LoggerFactory.getLogger(CourseService.class);

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private PersonService personService;

    @Autowired
    private oeapiStudyLoadRepository studyLoadRepository;

    @Autowired
    private ModeOfDeliveryRepository modeOfDeliveryRepository;

    @Autowired
    private oeapiFieldsOfStudyRepository fieldsOfStudyRepository;

    @Autowired
    oeapiEnumConversionService enumService;

    //private oeapiCourseMapper mapper;
    public CourseService() {
        super();
        //super.initializeMapper(CourseDTO.class, CourseDTO.class, enumService, Arrays.asList("level"));
        this.initializeMapper();

    }

    @Override
    public void initializeMapper() {

        super.initializeMapper(Course.class, CourseDTO.class, enumService, Arrays.asList("level"));
        super.setMapper(new oeapiCourseMapper(enumService, Arrays.asList("level"), fieldsOfStudyRepository));
    }

    @Override
    public Page<Course> getByPrimaryCode(String code, Pageable pageable) {

        Page<Course> pages = super.getByPrimaryCode(code, pageable);
        return pages;

    }

    @Override
    public List<Course> getAll() {

        List<Course> courses = super.getAll();
        return courses;
    }

    @Override
    public Page<Course> getAll(Pageable pageable) {

        Page<Course> pages = super.getAll(pageable);
        return pages;

    }

    @Override
    public Course update(String id, Course course) {
        logger.debug("CourseService update(id,course): Calling super");
        Optional<Course> courseExisting = this.getById(id);
        if (!courseExisting.isPresent()) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Course not found", "Course [" + id + "] not found");

        }
        // c es el course persistido
        course.setCourseId(id);

        course = checkRelations(course);
        return super.update(normalizeAttributes(course));
    }

    @Override
    public boolean delete(String id) {

        Optional<Course> courseExisting = this.getById(id);
        if (!courseExisting.isPresent()) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Course not found", "Course [" + id + "] not found");

        }
        Course c = courseExisting.get();
        this.clearRelations(c);
        super.delete(c);
        return true;
    }

    @Override
    public Optional<Course> getById(String id) {
        return super.getById(id);
    }

    public Course clearRelations(Course course) {

        List<ModeOfDelivery> modeOfDelivery = course.getModeOfDelivery();
        if (!(modeOfDelivery == null) && !modeOfDelivery.isEmpty()) {
            course.getModeOfDelivery().clear();
        }
        List<Program> programs = course.getPrograms();
        if (!programs.isEmpty()) {
            course.getPrograms().clear();
        }

        course.getCoordinators().clear();
        course.setStudyLoad(null);

        return course;

    }

    public Course checkRelations(Course course) {

        try {
            logger.debug("+CourseService checkRelations: programList..");
            List<Program> programList = (course.getPrograms() != null) ? course.getPrograms() : new ArrayList<>();
            List<Program> relatedPrograms = programService.manageRelated(programList);
            course.setPrograms(relatedPrograms);

            logger.debug("+CourseService checkRelations: Organization...");
            Optional<Organization> orgExisting = (course.getOrganization() != null) ? organizationService.manageRelated(course.getOrganization()) : organizationService.getDefault();
            if (orgExisting.isPresent()) {
                logger.debug("++CourseService checkRelations: Organization set to: id=" + orgExisting.get().getOrganizationId() + " ShortName: " + orgExisting.get().getShortName());
                course.setOrganization(orgExisting.get());
            }

            logger.debug("+CourseService checkRelations: Coordinators personList...");
            List<Person> personList = (course.getCoordinators() != null) ? course.getCoordinators() : new ArrayList<>();
            List<Person> relatedPersons = personService.manageRelated(personList);
            course.setCoordinators(relatedPersons);

        } catch (oeapiException ooapiEx) {
            logger.debug("ooapiException creating course: " + ooapiEx.getTitle() + " -> " + ooapiEx.getDetail());
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Course not created", ooapiEx.getTitle() + " -> " + ooapiEx.getDetail());
        } catch (Exception exc) {
            logger.debug("Unexpected general exception creating course:" + exc.getLocalizedMessage());
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Course not created", "Unexpected general exception creating course: " + exc.getLocalizedMessage());
        }

        return course;

    }

    @Override
    public Course create(Course course) {
        
        logger.debug("*>-CourseService Crete(course): Revising relations and normalizing attributes...");
        logger.debug("*>-CourseService Crete(course): Organization at START: " + ((course.getOrganization() != null) ? course.getOrganization().getOrganizationId() : "None"));        
        Course managedCourse = checkRelations(course);
        Course normalizedCourse = normalizeAttributes(managedCourse);
        logger.debug("*>-CourseService Crete(course): Organization at END:" + ((course.getOrganization() != null) ? course.getOrganization().getOrganizationId() : "None"));
        logger.debug("*>-CourseService Crete(course): Calling super");
        return super.create(normalizedCourse);
    }

    @Override
    public Course update(Course course) {
        logger.debug("CourseService update(course): Calling super");
        Course managedCourse = checkRelations(course);
        Course normalizedCourse = normalizeAttributes(managedCourse);
        return super.update(normalizedCourse);

    }

    // Method related to RequestDTOtoEntity
    public List<Program> getPrograms(List<String> programs) {
        List<Program> newProgramList = new ArrayList<>();

        for (String programId : programs) {
            Optional<Program> progExisting = programService.getById(programId);
            if (!progExisting.isPresent()) {
                throw new oeapiException(HttpStatus.BAD_REQUEST, "Check list of programs. Program not found", "Program [" + programId + "] not found");
            }
            newProgramList.add(progExisting.get());
        }
        return newProgramList;

    }

    @Override
    public Page<Course> getByField(String capitalizedMethodName, String value, Pageable pageable) {
        logger.debug("Capigalized Methdod: " + capitalizedMethodName);
        if (capitalizedMethodName == "fieldsOfStudy") {
            Optional<oeapiFieldsOfStudy> fos = Optional.empty();
            fos = fieldsOfStudyRepository.findByTxtEnAndLevel(value, Long.parseLong("1"));
            if (fos.isPresent()) {
                return repository.findByFieldsOfStudyId(fos.get().getFieldsOfStudyId(), pageable);
            } else {
                return null;
            }
        } else {
            return super.getByField(capitalizedMethodName, value, pageable); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
        }
    }

    public List<Course> findByProgram(String code) {

        return ((List<Course>) repository.findAll()).stream()
                .filter(course -> course.getPrograms() != null && course.getPrograms().contains(code))
                .collect(Collectors.toList());
    }

    public List<Course> getCoursesByProgramId(String program) {

        return repository.findByPrograms_ProgramId(program);
    }

    public Page<CourseDTO> getByOrganizationId(String id, Pageable pageable) {

        return this.toDTOPages(repository.findByOrganization_OrganizationId(id, pageable));

    }

    public Page<CourseDTO> getByOrganizationId(String id, Map.Entry<String, String> filter, Pageable pageable) {

        String sufixMethod = "Organization_OrganizationId";
        return this.toDTOPages(super.findByDynamicMethod(sufixMethod, id, filter, pageable));

    }

    public List<Course> getCoursesByModeOfDeliveryName(String modelOfdeliveryName) {
        return repository.findByModeOfDelivery_Name(modelOfdeliveryName);
    }

    /* Check relations with other entities and avoid duplicates on JPA automatic save or cascade */
    public Course normalizeAttributes(Course course) {

        logger.debug("Course service normalize studyLoad and modeOfDeliveries...");

        // Check studyLoad actually is optional
        if (course.getStudyLoad() != null) {
            Optional<StudyLoad> existingStudyLoad = studyLoadRepository.findByStudyLoadUnitAndValue(course.getStudyLoad().getStudyLoadUnit(), course.getStudyLoad().getValue());

            // If exists, attaches it
            if (existingStudyLoad.isPresent()) {
                course.setStudyLoad(existingStudyLoad.get());
                logger.debug("Course service, normalizing: studyLoads exists. Attaching it");
            }
        }

        // Manage array of ModeOfDelivery, check if exists
        if (course.getModeOfDelivery() != null) {

            logger.debug("Course service, normalizing: checking array ModeOfDelivery... ");
            List<ModeOfDelivery> deliveries = course.getModeOfDelivery();
            List<ModeOfDelivery> newModeList = new ArrayList<>();

            deliveries.forEach(delivery -> {
                String name = delivery.getName();
                Optional<ModeOfDelivery> existingMode = modeOfDeliveryRepository.findModeOfDeliveryByName(name);
                ModeOfDelivery newMode;

                if (existingMode.isPresent()) {
                    newModeList.add(existingMode.get());
                    logger.debug("Course service, normalizing: ModeOfDelivery already exists. Attaching it");
                } else {
                    newMode = new ModeOfDelivery();
                    newMode.setName(name);
                    newModeList.add(modeOfDeliveryRepository.save(newMode));
                }
            });
            course.setModeOfDelivery(newModeList);
        }

        return course;
    }

    @Override
    public Course autoGenerateBasicItem(String CourseId) {

        Course newCourse = new Course(CourseId);
        newCourse.setPrimaryCode(new oeapiIdentifierEntry("identifier", CourseId + "-AutoGenerated"));
        newCourse.setTeachingLanguage("eng");

        newCourse.setName(defaultDescription("en-GB", "Autogenerated Course for Id " + CourseId));
        newCourse.setDescription(defaultDescription("en-GB", "This an autogenerated course for Id " + CourseId));
        newCourse.setAbbreviation("Autogen-Course-" + CourseId);

        // Persist or not? Let the caller decide
        // this.create(newCourse);
        return newCourse;

    }

    @Override
    public Course autoGenerateBasicItem() {

        return this.autoGenerateBasicItem(UUID.randomUUID().toString() + "-Autogenerated-Course");

    }

    @Override
    public String toDTOString(Course item, String expand) {

        // This method is used with
        // get courses/<uuid>
        //initializeMapper();
        //return mapper.toDTO(item, expand);
        return "";

    }

}
