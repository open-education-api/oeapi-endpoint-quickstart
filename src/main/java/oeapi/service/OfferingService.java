package oeapi.service;

import java.time.LocalDate;
import java.util.ArrayList;
import oeapi.model.Offering;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import oeapi.model.Course;
import oeapi.model.CourseOffering;
import oeapi.model.ModeOfDelivery;
import oeapi.model.Organization;
import oeapi.model.Program;
import oeapi.model.ProgramOffering;
import oeapi.oeapiException;
import oeapi.oeapiUtils;
import oeapi.repository.ModeOfDeliveryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import oeapi.repository.OfferingRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

/**
 *
 * @author itziar.urrutia
 */
@Service
@Transactional
public class OfferingService extends oeapiEndpointService<Offering, OfferingRepository> implements oeapiServiceInterface<Offering> {

    static Logger logger = LoggerFactory.getLogger(OfferingService.class);

    @Autowired
    private ModeOfDeliveryRepository modeOfDeliveryRepository;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProgramService programService;    

    @Autowired
    private CourseService courseService;    
    
    @Value("${ooapi.config.autoCreateOrgIfNotExists:false}")
    private boolean defaultAutoCreateOrgIfNotExists;

    public OfferingService() {
        super();
    }

    @Override
    public Page<Offering> getByPrimaryCode(String code, Pageable pageable) {
        return super.getByPrimaryCode(code, pageable);
    }

    @Override
    public Page<Offering> getAll(Pageable pageable) {
        return super.getAll(pageable);
    }

    @Override
    public Page<Offering> getByField(String field, String value, Pageable pageable) {
        String capitalizedMethodName = "findBy" + field.substring(0, 1).toUpperCase() + field.substring(1);
        return super.findByDynamicMethod(capitalizedMethodName, value, pageable);
    }

    // mejorar pasar a getByField
    public List<CourseOffering> getByCourseId(String id) {
        return repository.findByCourse_CourseId(id);
    }

    @Override
    public Optional<Offering> getById(String id) {
        return super.getById(id);
    }

    @Override
    public List<Offering> getAll() {
        return super.getAll();
    }

    @Override
    public Offering create(Offering object) {

    logger.debug("*>-Offering Create: Revising relations and normalizing attributes...");
    logger.debug("*>--Offering Create: Organization at START: {}", 
        (object.getOrganization() != null) ? object.getOrganization().getOrganizationId() : "None");

    // Handle common logic for all types of offerings
    Offering managedOffering = checkRelations(object, defaultAutoCreateOrgIfNotExists);
    Offering revisedOffering = normalizeAttributes(managedOffering);

    // --- Handle specific subtypes ---
    if (revisedOffering instanceof CourseOffering) {
        CourseOffering courseOffering = (CourseOffering) revisedOffering;
        logger.debug("*>-- Detected CourseOffering, applying course-specific logic");

        courseOffering.setOfferingType("course");
        courseOffering.setCourse(((CourseOffering) object).getCourse()); 

        boolean hasValidCourse = false;   
        
        // CourseOffering should have a parent course to belong to
        if (courseOffering.getCourse() != null)
          {  
           logger.debug("+(Course) Offering service checkRelations: Course...");   
           Optional<Course> courseExisting ;
            try {
                courseExisting = courseService.manageRelated(courseOffering.getCourse());
                }
               catch (Exception ex) {
                   throw new oeapiException(HttpStatus.BAD_REQUEST, "(Offering create )Error revising course in CourseOffering", "Error: "+ex.getLocalizedMessage());
                }
           if (courseExisting.isPresent()) {
                logger.debug("++(Course) Offering service checkRelations: courseExisting set to: id=" + courseExisting.get().getCourseId() + "Abbreviation: " + courseExisting.get().getAbbreviation());
                courseOffering.setCourse(courseExisting.get());
                hasValidCourse = true; 
              }
          }
        
        if (!hasValidCourse) {  
             throw new oeapiException(HttpStatus.BAD_REQUEST, "(Course) Offering not created", "The parent course does not exists or invalid! Course :"+courseOffering.getCourse());
        }
        
        revisedOffering = courseOffering;
    } 
    else if (revisedOffering instanceof ProgramOffering) {
        ProgramOffering programOffering = (ProgramOffering) revisedOffering;
        logger.debug("*>-- Detected ProgramOffering, applying program-specific logic");

        programOffering.setOfferingType("program");
        programOffering.setProgram(((ProgramOffering) object).getProgram());
                
        boolean hasValidProgram = false;   
        
        if (programOffering.getProgram() != null)
          {  
           logger.debug("+(Program) Offering service checkRelations: program...");   
           Optional<Program> progExisting = programService.manageRelated(programOffering.getProgram()) ;
           if (progExisting.isPresent()) {
                logger.debug("++(Course) Offering service checkRelations: programOffering set to: id=" + progExisting.get().getProgramId() + "Name: " + progExisting.get().getName());
                programOffering.setProgram(progExisting.get());
                hasValidProgram = true;                                 
            }
          } 
           
        if (!hasValidProgram) {  
             throw new oeapiException(HttpStatus.BAD_REQUEST, "(Program) Offering not created", "The parent program does not exists or invalid! Program :"+programOffering.getProgram());
        } 
        
        revisedOffering = programOffering;
    }

    logger.debug("*>-Offering Create: Organization at END: {}", 
        (revisedOffering.getOrganization() != null) ? revisedOffering.getOrganization().getOrganizationId() : "None");
    logger.debug("*>--Offering Create: Checked and normalized offering {}", revisedOffering);

    return super.create(revisedOffering);

    }


    @Override
    public Offering update(Offering object) {
        return super.update(object);
    }

    @Override
    public Offering update(String id, Offering object) {
        return super.update(id, object);
    }

    @Override
    public boolean delete(String id) {
        super.delete(id);
        return true;
    }

    public void delete(Course course) {
        List<CourseOffering> offerings = repository.findByCourse_CourseId(course.getCourseId());
        for (CourseOffering offering : offerings) {
            offering.setCourse(null); // disassociate
            repository.delete(offering); // delete
        }
    }

    public Offering checkRelations(Offering offering, boolean mandatoryOrganization) {

        logger.debug("+(Course) Offering service checkRelations: Organization...");
        Optional<Organization> orgExisting = (offering.getOrganization() != null) ? organizationService.manageRelated(offering.getOrganization()) : organizationService.getDefault();
        if (orgExisting.isPresent()) {
            logger.debug("++(Course) Offering service checkRelations: Organization set to: id=" + orgExisting.get().getOrganizationId() + " ShortName: " + orgExisting.get().getShortName());
            offering.setOrganization(orgExisting.get());
        }

        return offering;
    }
    
    /* Check relations with other entities and avoid duplicates on JPA automatic save or cascade */
    public Offering normalizeAttributes(Offering offering) {

        logger.debug("Offering service normalize attribs...");

        // Manage array of ModeOfDelivery, check if exists
        if (offering.getModeOfDelivery() != null) {

            logger.debug("Offering service, normalizing: checking array ModeOfDelivery... ");
            List<ModeOfDelivery> deliveries = offering.getModeOfDelivery();
            List<ModeOfDelivery> newModeList = new ArrayList<>();

            deliveries.forEach(delivery -> {
                String name = delivery.getName();
                Optional<ModeOfDelivery> existingMode = modeOfDeliveryRepository.findModeOfDeliveryByName(name);
                ModeOfDelivery newMode;

                if (existingMode.isPresent()) {
                    newModeList.add(existingMode.get());
                    logger.debug("Offering service, normalizing: ModeOfDelivery already exists. Attaching it");
                } else {
                    newMode = new ModeOfDelivery();
                    newMode.setName(name);
                    newModeList.add(modeOfDeliveryRepository.save(newMode));
                }
            });
            offering.setModeOfDelivery(newModeList);
        }

        return offering;
    }

    @Override
    public Offering autoGenerateBasicItem(String id) {
        Offering offering = new Offering();
        offering.setOfferingId(id);
        offering.setName(oeapiUtils.defaultName());
        offering.setDescription(oeapiUtils.defaultDescription());
        offering.setAbbreviation("Autogenerated Offering for Id: " + id);
        offering.setPrimaryCode(oeapiUtils.defaultPrimaryCode("identifier", id + "-AutoGenerated"));
        offering.setOfferingType("course");  // ToDo Fine tune this, it could be also program or component depending on which uses it
        offering.setTeachingLanguage("eng");
        offering.setResultExpected(false);
        ArrayList<ModeOfDelivery> modesOfDelivery = new ArrayList<ModeOfDelivery>();
        ModeOfDelivery modeOfDelivery = new ModeOfDelivery();
        modeOfDelivery.setName("situated");
        modesOfDelivery.add(modeOfDelivery);

        offering.setModeOfDelivery(modesOfDelivery);
        offering.setStartDate(LocalDate.MIN);
        offering.setEndDate(LocalDate.MIN);

        return offering;
    }

    @Override
    public Offering autoGenerateBasicItem() {
        return this.autoGenerateBasicItem(UUID.randomUUID().toString() + "-Autogenerated-Offering");
    }

}
