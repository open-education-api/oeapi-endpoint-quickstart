package oeapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;

import oeapi.model.ModeOfDelivery;
import oeapi.model.Organization;
import oeapi.model.Person;
import oeapi.model.Program;
import oeapi.model.StudyLoad;
import oeapi.model.oeapiFieldsOfStudy;
import oeapi.model.oeapiIdentifierEntry;
import oeapi.oeapiException;
import oeapi.oeapiUtils;
import static oeapi.oeapiUtils.defaultDescription;
import oeapi.repository.ModeOfDeliveryRepository;
import oeapi.repository.ProgramRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import oeapi.repository.oeapiFieldsOfStudyRepository;
import oeapi.repository.oeapiStudyLoadRepository;

/**
 *
 * @author itziar.urrutia
 */
@Service
@Transactional

public class ProgramService extends oeapiEndpointService<Program, ProgramRepository> implements oeapiRelatedListServiceInterface<Program> {

    @Value("${ooapi.config.autoCreateProgIfNotExists:false}")
    private boolean autoCreateIfNotExists;

    Logger logger = LoggerFactory.getLogger(ProgramService.class);

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private PersonService personService;

    @Autowired
    private oeapiStudyLoadRepository studyLoadRepository;

    @Autowired
    private ModeOfDeliveryRepository modeOfDeliveryRepository;

    @Autowired
    private oeapiFieldsOfStudyRepository fieldsOfStudyRepository;

    @Value("${ooapi.config.autoCreateOrgIfNotExists:false}")
    private boolean defaultAutoCreateOrgIfNotExists;

    @Override
    public Page<Program> getByField(String capitalizedMethodName, String value, Pageable pageable) {
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

    @Override
    public List<Program> manageRelated(List<Program> relateList) {
        
        logger.debug("manageRelated: Revising Program supplied IDs. AutoCreateIfNotExists is: "+autoCreateIfNotExists);
        List<Program> newRelateList = new ArrayList<>();
        String id = null;
        
        try
        {           
            for (Program relate : relateList) {
                id = oeapiUtils.getId(relate).toString();
                logger.debug("manageRelated examining Program Id: "+id);
                Optional<Program> relExisting = manageRelated(id);

                if (relExisting.isPresent()) {
                    logger.debug("manageRelated found Id. Then add Id: "+id+" to owner object");                
                    newRelateList.add(relExisting.get());
                 } else
                    if (autoCreateIfNotExists) {
                        logger.debug("manageRelated has NOT FOUND Id. "+id+" +++ autocreating one based on ID");                
                        newRelateList.add(this.autoGenerateBasicItem(id));                    
                    } else {
                        logger.debug("manageRelated has NOT FOUND Id. "+id+" and autocreate is FALSE");                
                        throw new oeapiException(HttpStatus.BAD_REQUEST, "Check Programs related list", "Element [" + id + "] not found");
                    }
            }        
         }
         catch (Exception ex)
             { throw new oeapiException(HttpStatus.BAD_REQUEST, "Cannot verify program list. Element [" + id + "] not found.", ex.getLocalizedMessage()) ; }
     
        
        return newRelateList;        
        
    }

    /* Check single program */
    @Override
    public Optional<Program> manageRelated(Program related) {
        
        logger.debug("--> ProgramService, manageRelated: Searching for the supplied Program.. ["+related+"]");
        
        String id = related.getProgramId();
        
        Optional<Program> objExisting = Optional.empty();
        try {
            objExisting = super.manageRelated(related);
            if (!objExisting.isPresent()) {
                logger.debug("--> ProgramService, manageRelated: No Program exists like the supplied one. Getting default? autoCreateIfNotExists is: ..."+autoCreateIfNotExists);
                if (autoCreateIfNotExists) {
                  objExisting = Optional.ofNullable(autoGenerateBasicItem(related.getProgramId()));
                } 
                else
                 { throw new oeapiException(HttpStatus.BAD_REQUEST, "Check Ptogram or ProgramId ", "Element [" + id + "] not found"); } 
            } else {
                logger.debug("--> ProgramService , manageRelated: The Program supplied exists. ProgID = ["+id+"] returning it");
            }
        } catch (Exception ex) {
           throw new oeapiException(HttpStatus.BAD_REQUEST, "Cannot verify program: ", ex.getLocalizedMessage()) ; 
        } 
        
        return objExisting;
        
    }  
    
    
    public Program checkRelations(Program program, boolean autoCreateOrgIfNotExists) {

        // autoCreateOrgIfNotExists is deprecated, creation or not of a default Org is managed at each object service
        try {
            // ORG is not mandatory for program
            logger.debug("+ProgramService checkRelations: Organization...");
            if (program.getOrganization() != null) {
                Optional<Organization> orgExisting = organizationService.manageRelated(program.getOrganization());
                if (orgExisting.isPresent()) {
                    logger.debug("++ProgramService checkRelations: Organization set to: id=" + orgExisting.get().getOrganizationId() + " ShortName: " + orgExisting.get().getShortName());
                    program.setOrganization(orgExisting.get());                    
                }
                else
                 {  
                   if (autoCreateOrgIfNotExists)
                     {    
                       program.setOrganization(organizationService.autoGenerateBasicItem(program.getOrganization().getOrganizationId()));                        
                     } 
                    else
                     { 
                      throw new oeapiException(HttpStatus.BAD_REQUEST, "Program not created", "No such organization: " + program.getOrganization().getOrganizationId());
                     }
                 }
            }    

            logger.debug("+ProgramService checkRelations: Coordinators..");
            List<Person> personList = (program.getCoordinators() != null) ? program.getCoordinators() : new ArrayList<>();
            List<Person> relatedPersons = personService.manageRelated(personList);
            program.setCoordinators(relatedPersons);

        } catch (oeapiException ooapiEx) {
            logger.debug("ProgramService creating Program: " + ooapiEx.getTitle() + " -> " + ooapiEx.getDetail());
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Program not created", ooapiEx.getTitle() + " -> " + ooapiEx.getDetail());
        } catch (Exception exc) {
            logger.debug("Unexpected general exception creating Program:" + exc.getLocalizedMessage());
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Program not created", "Unexpected general exception creating Program: " + exc.getLocalizedMessage());
        }

        // To-Do check similarly relations like parent, educationSpecification
        return program;
    }
	

    /* Check relations with other entities and avoid duplicates on JPA automatic save or cascade */
    public Program normalizeAttributes(Program program) {

        // Check studyLoad 
        if (program.getStudyLoad() != null) {
            Optional<StudyLoad> existingStudyLoad = studyLoadRepository.findByStudyLoadUnitAndValue(program.getStudyLoad().getStudyLoadUnit(), program.getStudyLoad().getValue());

            // If exists, attaches it
            if (existingStudyLoad.isPresent()) {
                program.setStudyLoad(existingStudyLoad.get());
                logger.debug("Program service, normalizing: studyLoads exists. Attaching it");
            }
        }
        
        // Manage array of ModeOfDelivery, check if exists
        if (program.getModeOfDelivery() != null) {

            logger.debug("Program service, normalizing: checking array ModeOfDelivery... ");
            List<ModeOfDelivery> deliveries = program.getModeOfDelivery();
            List<ModeOfDelivery> newModeList = new ArrayList<>();

            deliveries.forEach(delivery -> {
                String name = delivery.getName();
                Optional<ModeOfDelivery> existingMode = modeOfDeliveryRepository.findModeOfDeliveryByName(name);
                ModeOfDelivery newMode;

                if (existingMode.isPresent()) {
                    newModeList.add(existingMode.get());
                    logger.debug("Program service, normalizing: ModeOfDelivery already exists. Attaching it");
                } else {
                    newMode = new ModeOfDelivery();
                    newMode.setName(name);
                    newModeList.add(modeOfDeliveryRepository.save(newMode));
                }
            });
            program.setModeOfDelivery(newModeList);
        }

        return program;
    }

    @Override
    public Program create(Program program) {

        logger.debug("*>-ProgramService Create(program): Revising relations and normalizing attributes...");
        logger.debug("*>-ProgramServicee Create(program): Organization at START: " + ((program.getOrganization() != null) ? program.getOrganization().getOrganizationId() : "None"));
        Program managedProgram = checkRelations(program, defaultAutoCreateOrgIfNotExists);
        Program normalizedProgram = normalizeAttributes(managedProgram);
        logger.debug("*>-ProgramService Create(program): Organization at END:" + ((program.getOrganization() != null) ? program.getOrganization().getOrganizationId() : "None"));
        logger.debug("*>-ProgramService Create(program): Calling super");
        return super.create(normalizedProgram);
    }

    public Page<Program> getByOrganizationId(String id, Pageable pageable) {

        return repository.findByOrganization_OrganizationId(id, pageable);

    }

    public Page<Program> getByOrganizationId(String id, Map.Entry<String, String> filter, Pageable pageable) {
        String sufixMethod = "Organization_OrganizationId";
        return super.findByDynamicMethod(sufixMethod, id, filter, pageable);
    }

    @Override
    public Program autoGenerateBasicItem(String id) {

        Program program = new Program(id);
        program.setPrimaryCode(new oeapiIdentifierEntry("identifier", id + "-AutoGenerated"));
        program.setTeachingLanguage("eng");

        //ooapiBaseInfo baseInfo = new ooapiBaseInfo();
        program.setName(defaultDescription("en-GB", "Autogenerated Program for Id " + id));
        program.setDescription(defaultDescription("en-GB", "This a default autogenerated Program for Id " + id));
        program.setAbbreviation("Autogen-Program-" + id);
        //program.setBaseInfo(baseInfo);

        return program;

    }

    @Override
    public Program autoGenerateBasicItem() {

        return this.autoGenerateBasicItem(UUID.randomUUID().toString() + "-Autogenerated-Program");

    }

}
