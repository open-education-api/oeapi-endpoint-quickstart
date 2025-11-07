package oeapi.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.UUID;

import javax.transaction.Transactional;
import oeapi.controller.oeapiCourseMapper;
import oeapi.controller.oeapiDTOMapper;
import oeapi.model.Component;
import oeapi.model.ComponentOffering;
import oeapi.model.Course;
import oeapi.model.ModeOfDelivery;
import oeapi.model.Organization;
import oeapi.model.Person;
import oeapi.model.Program;
import oeapi.oeapiException;
import oeapi.oeapiUtils;
import oeapi.payload.ComponentDTO;
import oeapi.payload.CourseDTO;
import oeapi.payload.OrganizationDTO;

import oeapi.repository.ComponentRepository;
import oeapi.repository.ModeOfDeliveryRepository;
import static oeapi.service.OfferingService.logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 *
 * @author itziar.urrutia
 */
@Service
@Transactional

public class ComponentService extends oeapiEndpointDTOService<Component, ComponentRepository, ComponentDTO> implements oeapiRelationalModelInterface<Component> {

    @Autowired
    private oeapiEnumConversionService enumService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private OrganizationService organizationService;    
    
    @Autowired
    private ModeOfDeliveryRepository modeOfDeliveryRepository;
    
    
    public ComponentService() {
        super();
        this.initializeMapper();

    }

    @Override
    public Component checkRelations(Component component) {
 
       try {
            logger.debug("+ComponentService checkRelations: Organization...");
            Optional<Organization> orgExisting = (component.getOrganization() != null) ? organizationService.manageRelated(component.getOrganization()) : organizationService.getDefault();
            if (orgExisting.isPresent()) {
                logger.debug("++ComponentService checkRelations: Organization set to: id=" + orgExisting.get().getOrganizationId() + " ShortName: " + orgExisting.get().getShortName());
                component.setOrganization(orgExisting.get());
            }

            logger.debug("+ComponentService checkRelations: Course...");
            Optional<Course> relatedExisting = (component.getCourse() != null) ? courseService.manageRelated(component.getCourse()) : Optional.empty();
            if (relatedExisting.isPresent()) {
                logger.debug("+++ComponentService checkRelations: Course set to: id=" + relatedExisting.get().getCourseId());
                component.setCourse(relatedExisting.get());
            }            

        } catch (oeapiException ooapiEx) {
            logger.debug("ComponentService creating Component: " + ooapiEx.getTitle() + " -> " + ooapiEx.getDetail());
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Component not created", ooapiEx.getTitle() + " -> " + ooapiEx.getDetail());
        } catch (Exception exc) {
            logger.debug("Unexpected general exception creating Component:" + exc.getLocalizedMessage());
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Component not created", "Unexpected general exception creating Component: " + exc.getLocalizedMessage());
        }

        // To-Do check similarly relations like parent, educationSpecification
        return component;
    }
        

    @Override
    public Component normalizeAttributes(Component component) {
        
        // Manage array of ModeOfDelivery, check if exists
        if (component.getModeOfDelivery() != null) {

            logger.debug("Program service, normalizing: checking array ModeOfDelivery... ");
            List<ModeOfDelivery> deliveries = component.getModeOfDelivery();
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
            component.setModeOfDelivery(newModeList);
        }
              
        return component;
    }

    @Override
    public Optional<Component> manageRelated(Component related) {
        
        logger.debug("--> ComponentService, manageRelated: Searching for the supplied Program.. ["+related+"]");
        
        String id = related.getComponentId();
        
        Optional<Component> objExisting = Optional.empty();
        try {
            objExisting = super.manageRelated(related);
            if (!objExisting.isPresent()) {
                logger.debug("--> ComponentService, manageRelated: No ComponentID exists like the supplied one. ");
                throw new oeapiException(HttpStatus.BAD_REQUEST, "Check Component or ComponentID ", "Element [" + id + "] not found"); 
              } 
             else {
                logger.debug("--> ComponentService , manageRelated: The Component supplied exists. ComponentID = ["+id+"] returning it");
            }
        } catch (Exception ex) {
           throw new oeapiException(HttpStatus.BAD_REQUEST, "Cannot verify Component: ", ex.getLocalizedMessage()) ; 
        } 
        
        return objExisting;
        
    }    
            
    @Override
    public Component create(Component component) {
        // course es el JSON
        logger.debug("*>-CourseService Create(component): Revising relations and normalizing attributes...");
        Component managed = checkRelations(component);
        Component normalized = normalizeAttributes(managed);
        logger.debug("*>-CourseService Crete(component): Organization at END:" + component.getCourse().getCourseId());
        logger.debug("*>-CourseComponent Crete(component): Calling super");
        return super.create(normalized);
    }

    @Override
    public void initializeMapper() {

        //super.initializeMapper(Component.class, ComponentDTO.class, enumService, Arrays.asList("componentType"));
        super.setMapper((oeapiDTOMapper<Component, ComponentDTO>) new oeapiDTOMapper(Component.class, ComponentDTO.class, enumService, Arrays.asList("componentType")));
    }

    public List<ComponentOffering> getOfferings(Component component) {
// To-Do Revise this
//        List<ComponentOffering> componentOffering = component.getOfferings();
//
//        if (componentOffering.isEmpty()) {
//            componentOffering = new ArrayList<>();
//            componentOffering.add(offeringService.defaultComponentOffering(component));
//        }
//        return componentOffering;
        return new ArrayList();

    }

    public Page<Component> getByCourseId(String id, Map.Entry<String, String> filter, Pageable pageable) {

        String capitalizedMethodName;

        String findBy = "findBy";

        String sufixMethod = "Course_CourseId";
        if (filter == null) {
            capitalizedMethodName = findBy + sufixMethod;
            return super.findByDynamicMethod(capitalizedMethodName, id, pageable);
        }
        String field = filter.getKey();
        String value = filter.getValue();
        capitalizedMethodName = findBy + field.substring(0, 1).toUpperCase() + field.substring(1) + "And" + sufixMethod;
        return super.findByDynamicMethod(capitalizedMethodName, value, id, pageable);
    }

    public Page<Component> getByOrganizationId(String id, Map.Entry<String, String> filter, Pageable pageable) {

        String capitalizedMethodName;

        String findBy = "findBy";
        String sufixMethod = "Organization_OrganizationId";
        if (filter == null) {
            capitalizedMethodName = findBy + sufixMethod;
            return super.findByDynamicMethod(capitalizedMethodName, id, pageable);
        }
        String field = filter.getKey();
        String value = filter.getValue();
        capitalizedMethodName = findBy + field.substring(0, 1).toUpperCase() + field.substring(1) + "And" + sufixMethod;
        return super.findByDynamicMethod(capitalizedMethodName, value, id, pageable);

    }

    @Override
    public Component autoGenerateBasicItem(String id) {

        Component component = new Component();

        /* Same id as course, so we can notice is autogen */
        component.setComponentId(id);

        component.setPrimaryCode(oeapiUtils.defaultPrimaryCode("identifier", id + "-AutoGenerated"));
        component.setTeachingLanguage("eng");
        //component.setComponentTypeId("independent study");
        component.setName(oeapiUtils.defaultName());
        component.setAbbreviation("AUTO-" + id);

        /*
        Address address = new Address();
        address.setAddressType("postal");
        List<Address> listAddresses = new ArrayList<Address>();
        listAddresses.add(address);


        //ToDo - Add detail in address
        component.setAddresses(listAddresses);

         */
        return component;
    }

    @Override
    public Component autoGenerateBasicItem() {
        return this.autoGenerateBasicItem(UUID.randomUUID().toString() + "-Autogenerated-Component");
    }
}
