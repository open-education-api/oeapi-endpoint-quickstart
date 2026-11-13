package oeapi.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import jakarta.transaction.Transactional;

import oeapi.model.ModeOfDelivery;
import oeapi.model.Organisation;
import oeapi.model.Person;
import oeapi.model.Programme;
import oeapi.model.StudyLoad;
import oeapi.model.oeapiFieldsOfStudy;
import oeapi.model.oeapiIdentifierEntry;
import oeapi.payload.ProgrammeDTO;
import oeapi.oeapiException;
import oeapi.oeapiUtils;
import oeapi.controller.oeapiProgrammeMapper;

import static oeapi.oeapiUtils.defaultDescription;
import oeapi.repository.ModeOfDeliveryRepository;
import oeapi.repository.ProgrammeRepository;

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

public class ProgrammeService extends oeapiEndpointDTOService<Programme, ProgrammeRepository, ProgrammeDTO> implements oeapiRelatedObjServiceInterface<Programme>, oeapiRelationalModelInterface<Programme> {

    @org.springframework.beans.factory.annotation.Autowired
    private LearningOutcomeService learningOutcomeService;


    @Value("${ooapi.config.autoCreateProgIfNotExists:false}")
    private boolean autoCreateIfNotExists;

    Logger logger = LoggerFactory.getLogger(ProgrammeService.class);

    @Autowired
    private OrganisationService organisationService;

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

    @Value("${ooapi.config.autoCreateOrgIfNotExists:false}")
    private boolean defaultAutoCreateOrgIfNotExists;

    public ProgrammeService() {
        super();
        this.initializeMapper();
    }

    @Override
    public void initializeMapper() {
        super.initializeMapper(Programme.class, ProgrammeDTO.class, enumService, Arrays.asList("level"));
        super.setMapper(new oeapiProgrammeMapper(enumService, Arrays.asList("level"), fieldsOfStudyRepository));
    }

    @Override
    public Page<Programme> getByField(String capitalizedMethodName, String value, Pageable pageable) {
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
    public Optional<Programme> getById(String id) {
        return super.getById(id);
    }

    @Override
    public List<Programme> manageRelated(List<Programme> relateList) {
        
        logger.debug("manageRelated: Revising Programme supplied IDs. AutoCreateIfNotExists is: "+autoCreateIfNotExists);
        List<Programme> newRelateList = new ArrayList<>();
        String id = null;
        
        try
        {           
            for (Programme relate : relateList) {
                id = oeapiUtils.getId(relate).toString();
                logger.debug("manageRelated examining Programme Id: "+id);
                Optional<Programme> relExisting = manageRelated(id);

                if (relExisting.isPresent()) {
                    logger.debug("manageRelated found Id. Then add Id: "+id+" to owner object");                
                    newRelateList.add(relExisting.get());
                 } else
                    if (autoCreateIfNotExists) {
                        logger.debug("manageRelated has NOT FOUND Id. "+id+" +++ autocreating one based on ID");                
                        newRelateList.add(this.autoGenerateBasicItem(id));                    
                    } else {
                        logger.debug("manageRelated has NOT FOUND Id. "+id+" and autocreate is FALSE");                
                        throw new oeapiException(HttpStatus.BAD_REQUEST, "Check Programmes related list", "Element [" + id + "] not found");
                    }
            }        
         }
         catch (Exception ex)
             { throw new oeapiException(HttpStatus.BAD_REQUEST, "Cannot verify programme list. Element [" + id + "] not found.", ex.getLocalizedMessage()) ; }
     
        
        return newRelateList;        
        
    }

    /* Check single programme */
    @Override
    public Optional<Programme> manageRelated(Programme related) {
        
        logger.debug("--> ProgrammeService, manageRelated: Searching for the supplied Programme.. ["+related+"]");
        
        String id = related.getProgrammeId();
        
        Optional<Programme> objExisting = Optional.empty();
        try {
            objExisting = super.manageRelated(related);
            if (!objExisting.isPresent()) {
                logger.debug("--> ProgrammeService, manageRelated: No Programme exists like the supplied one. Getting default? autoCreateIfNotExists is: ..."+autoCreateIfNotExists);
                if (autoCreateIfNotExists) {
                  objExisting = Optional.ofNullable(autoGenerateBasicItem(related.getProgrammeId()));
                } 
                else
                 { throw new oeapiException(HttpStatus.BAD_REQUEST, "Check Programme or ProgrammeId ", "Element [" + id + "] not found"); }
            } else {
                logger.debug("--> ProgrammeService , manageRelated: The Programme supplied exists. ProgID = ["+id+"] returning it");
            }
        } catch (Exception ex) {
           throw new oeapiException(HttpStatus.BAD_REQUEST, "Cannot verify programme: ", ex.getLocalizedMessage()) ; 
        } 
        
        return objExisting;
        
    }  
    
    public Programme clearRelations(Programme programme) {

        List<ModeOfDelivery> modeOfDelivery = programme.getModesOfDelivery();
        if (!(modeOfDelivery == null) && !modeOfDelivery.isEmpty()) {
            programme.getModesOfDelivery().clear();
        }
 
        programme.getCoordinators().clear();
        programme.setStudyLoad(null);

        return programme;

    }
    
    public Programme checkRelations(Programme programme, boolean autoCreateOrgIfNotExists) {

        // autoCreateOrgIfNotExists is deprecated, creation or not of a default Org is managed at each object service
        try {
            // ORG is not mandatory for programme
            logger.debug("+ProgrammeService checkRelations: Organisation...");
            if (programme.getOrganisation() != null) {
                Optional<Organisation> orgExisting = organisationService.manageRelated(programme.getOrganisation());
                if (orgExisting.isPresent()) {
                    logger.debug("++ProgrammeService checkRelations: Organisation set to: id=" + orgExisting.get().getOrganisationId() + " ShortName: " + orgExisting.get().getShortName());
                    programme.setOrganisation(orgExisting.get());                    
                }
                else
                 {  
                   if (autoCreateOrgIfNotExists)
                     {    
                       programme.setOrganisation(organisationService.autoGenerateBasicItem(programme.getOrganisation().getOrganisationId()));                        
                     } 
                    else
                     { 
                      throw new oeapiException(HttpStatus.BAD_REQUEST, "Programme not created", "No such organisation: " + programme.getOrganisation().getOrganisationId());
                     }
                 }
            }    

            logger.debug("+ProgrammeService checkRelations: Coordinators..");
            List<Person> personList = (programme.getCoordinators() != null) ? programme.getCoordinators() : new ArrayList<>();
            List<Person> relatedPersons = personService.manageRelated(personList);
            programme.setCoordinators(relatedPersons);

        } catch (oeapiException ooapiEx) {
            logger.debug("ProgrammeService creating Programme: " + ooapiEx.getTitle() + " -> " + ooapiEx.getDetail());
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Programme not created", ooapiEx.getTitle() + " -> " + ooapiEx.getDetail());
        } catch (Exception exc) {
            logger.debug("Unexpected general exception creating Programme:" + exc.getLocalizedMessage());
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Programme not created", "Unexpected general exception creating Programme: " + exc.getLocalizedMessage());
        }

        // To-Do check similarly relations like parent, educationSpecification
        return programme;
    }
	

    /* Check relations with other entities and avoid duplicates on JPA automatic save or cascade */
    public Programme normalizeAttributes(Programme programme) {

        // OEAPI v6 learning outcomes: resolve learningOutcomeIds, turn v5 texts into resources
        learningOutcomeService.link(programme, programme.getProgrammeId() == null ? null : getById(programme.getProgrammeId()).orElse(null));

        // Check studyLoad 
        if (programme.getStudyLoad() != null) {
            Optional<StudyLoad> existingStudyLoad = studyLoadRepository.findByStudyLoadUnitAndValue(programme.getStudyLoad().getStudyLoadUnit(), programme.getStudyLoad().getValue());

            // If exists, attaches it
            if (existingStudyLoad.isPresent()) {
                programme.setStudyLoad(existingStudyLoad.get());
                logger.debug("Programme service, normalizing: studyLoads exists. Attaching it");
            }
        }
        
        // Manage array of ModeOfDelivery, check if exists
        if (programme.getModesOfDelivery() != null) {

            logger.debug("Programme service, normalizing: checking array ModeOfDelivery... ");
            List<ModeOfDelivery> deliveries = programme.getModesOfDelivery();
            List<ModeOfDelivery> newModeList = new ArrayList<>();

            deliveries.forEach(delivery -> {
                String name = delivery.getName();
                Optional<ModeOfDelivery> existingMode = modeOfDeliveryRepository.findModeOfDeliveryByName(name);
                ModeOfDelivery newMode;

                if (existingMode.isPresent()) {
                    newModeList.add(existingMode.get());
                    logger.debug("Programme service, normalizing: ModeOfDelivery already exists. Attaching it");
                } else {
                    newMode = new ModeOfDelivery();
                    newMode.setName(name);
                    newModeList.add(modeOfDeliveryRepository.save(newMode));
                }
            });
            programme.setModesOfDelivery(newModeList);
        }

        return programme;
    }

    @Override
    public Programme create(Programme programme) {

        logger.debug("*>-ProgrammeService Create(programme): Revising relations and normalizing attributes...");
        logger.debug("*>-ProgrammeServicee Create(programme): Organisation at START: " + ((programme.getOrganisation() != null) ? programme.getOrganisation().getOrganisationId() : "None"));
        Programme managedProgramme = checkRelations(programme, defaultAutoCreateOrgIfNotExists);
        Programme normalizedProgramme = normalizeAttributes(managedProgramme);
        logger.debug("*>-ProgrammeService Create(programme): Organisation at END:" + ((programme.getOrganisation() != null) ? programme.getOrganisation().getOrganisationId() : "None"));
        logger.debug("*>-ProgrammeService Create(programme): Calling super");
        return super.create(normalizedProgramme);
    }

    @Override
    public Programme update(Programme programme) {
        return super.update(normalizeAttributes(checkRelations(programme)));
    }

    @Override
    public boolean delete(String id) {

        Optional<Programme> programmeExisting = this.getById(id);
        if (!programmeExisting.isPresent()) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Programme not found", "Programme [" + id + "] not found");

        }
        Programme programme = programmeExisting.get();
 
        //ToDo programme child offerings and components deletion (not implmented yet)
        
        // Clear all rest relatons
        logger.debug("*>-ProgrammeService delete:  Clear programme relations for Programme with ID:"+programme.getProgrammeId()); 
        this.clearRelations(programme);
        super.delete(programme);
        return true;
    }    
    
    public Page<Programme> getByOrganisationId(String id, Pageable pageable) {

        return repository.findByOrganisation_OrganisationId(id, pageable);

    }

    public Page<Programme> getByOrganisationId(String id, Map.Entry<String, String> filter, Pageable pageable) {
        String sufixMethod = "Organisation_OrganisationId";
        return super.findByDynamicMethod(sufixMethod, id, filter, pageable);
    }

    @Override
    public Programme autoGenerateBasicItem(String id) {

        Programme programme = new Programme(id);
        programme.setPrimaryCode(new oeapiIdentifierEntry("identifier", id + "-AutoGenerated"));
        programme.setTeachingLanguages(java.util.List.of("en"));

        //ooapiBaseInfo baseInfo = new ooapiBaseInfo();
        programme.setName(defaultDescription("en-GB", "Autogenerated Programme for Id " + id));
        programme.setDescription(defaultDescription("en-GB", "This a default autogenerated Programme for Id " + id));
        programme.setAbbreviation("Autogen-Programme-" + id);
        //programme.setBaseInfo(baseInfo);

        return programme;

    }

    @Override
    public Programme autoGenerateBasicItem() {
        return this.autoGenerateBasicItem(UUID.randomUUID().toString() + "-Autogenerated-Programme");
    }

    @Override
    public Programme checkRelations(Programme obj) {
        return this.checkRelations(obj, false);
    }
}
