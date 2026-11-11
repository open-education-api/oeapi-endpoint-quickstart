package oeapi.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import oeapi.oeapiException;
import oeapi.model.ComponentOffering;
import oeapi.model.CourseOffering;
import oeapi.model.Offering;
import oeapi.model.OfferingAssociation;
import oeapi.model.Person;
import oeapi.model.ProgrammeOffering;
import oeapi.payload.CourseOfferingAssociationDTO;
import oeapi.payload.LearningComponentOfferingAssociationDTO;
import oeapi.payload.OfferingAssociationDTO;
import oeapi.payload.ProgrammeOfferingAssociationDTO;
import oeapi.repository.OfferingAssociationRepository;

/**
 * OEAPI v6 offering associations (course, programme and learning component
 * offering associations). Entity and DTOs are mapped by hand: the DTO only
 * differs from the entity in how the offering and the person are referenced.
 */
@Service
@Transactional
public class OfferingAssociationService {

    @Autowired
    private OfferingAssociationRepository repository;

    @Autowired
    private OfferingService offeringService;

    @Autowired
    private PersonService personService;

    public Optional<OfferingAssociation> getById(String id) {
        return repository.findById(id);
    }

    /** Association of the given offering type, empty when the id belongs to another type. */
    public <O extends Offering> Optional<OfferingAssociation> getById(String id, Class<O> offeringType) {
        return repository.findById(id).filter(a -> offeringType.isInstance(a.getOffering()));
    }

    public List<OfferingAssociation> getByOfferingId(String offeringId) {
        return repository.findByOffering_OfferingId(offeringId);
    }

    public <O extends Offering> List<OfferingAssociation> getByPersonId(String personId, Class<O> offeringType) {
        return repository.findByPerson_PersonId(personId).stream()
                .filter(a -> offeringType.isInstance(a.getOffering()))
                .collect(Collectors.toList());
    }

    public OfferingAssociation save(OfferingAssociation association) {
        return repository.saveAndFlush(association);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    // ---- mapping -------------------------------------------------------------

    public OfferingAssociationDTO toDTO(OfferingAssociation a) {
        Offering offering = a.getOffering();
        OfferingAssociationDTO dto;
        if (offering instanceof CourseOffering) {
            CourseOfferingAssociationDTO course = new CourseOfferingAssociationDTO();
            course.setStudyLoad(a.getStudyLoad());
            dto = course;
        } else if (offering instanceof ProgrammeOffering) {
            ProgrammeOfferingAssociationDTO programme = new ProgrammeOfferingAssociationDTO();
            programme.setStudyLoad(a.getStudyLoad());
            dto = programme;
        } else {
            dto = new LearningComponentOfferingAssociationDTO();
        }
        dto.setAssociationId(a.getAssociationId());
        dto.setOfferingId(offering == null ? null : offering.getOfferingId());
        dto.setPersonId(a.getPerson() == null ? null : a.getPerson().getPersonId());
        dto.setRole(a.getRole());
        dto.setState(a.getState());
        dto.setRemoteState(a.getRemoteState());
        dto.setStartDateTime(a.getStartDateTime());
        dto.setExpectedEndDateTime(a.getExpectedEndDateTime());
        dto.setActualEndDateTime(a.getActualEndDateTime());
        dto.setResult(a.getResult());
        dto.setConsumers(a.getConsumers());
        dto.setExt(a.getExt());
        return dto;
    }

    public List<OfferingAssociationDTO> toDTOList(List<OfferingAssociation> associations) {
        return associations.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Builds the entity for a DTO, checking that the offering exists and is of the
     * expected type and that the person exists.
     */
    public <O extends Offering> OfferingAssociation toEntity(OfferingAssociationDTO dto, Class<O> offeringType) {
        if (dto.getOfferingId() == null) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Offering missing",
                    "The association does not name its " + typeLabel(offeringType));
        }
        Offering offering = offeringService.getById(dto.getOfferingId())
                .filter(offeringType::isInstance)
                .orElseThrow(() -> new oeapiException(HttpStatus.BAD_REQUEST, "Offering not found",
                        "There is no " + typeLabel(offeringType) + " with Id: " + dto.getOfferingId()));

        if (dto.getPersonId() == null) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Person missing", "The association does not name its personId");
        }
        Person person = personService.getById(dto.getPersonId())
                .orElseThrow(() -> new oeapiException(HttpStatus.BAD_REQUEST, "Person not found",
                        "There is no person with Id: " + dto.getPersonId()));

        OfferingAssociation a = new OfferingAssociation();
        a.setAssociationId(dto.getAssociationId() == null ? UUID.randomUUID().toString() : dto.getAssociationId());
        a.setOffering(offering);
        a.setPerson(person);
        a.setRole(dto.getRole());
        a.setState(dto.getState());
        a.setRemoteState(dto.getRemoteState());
        a.setStartDateTime(dto.getStartDateTime());
        a.setExpectedEndDateTime(dto.getExpectedEndDateTime());
        a.setActualEndDateTime(dto.getActualEndDateTime());
        a.setResult(dto.getResult());
        a.setConsumers(dto.getConsumers());
        a.setExt(dto.getExt());
        if (dto instanceof CourseOfferingAssociationDTO course) {
            a.setStudyLoad(course.getStudyLoad());
        } else if (dto instanceof ProgrammeOfferingAssociationDTO programme) {
            a.setStudyLoad(programme.getStudyLoad());
        }
        return a;
    }

    public static String typeLabel(Class<?> offeringType) {
        if (offeringType == CourseOffering.class) {
            return "CourseOffering";
        } else if (offeringType == ProgrammeOffering.class) {
            return "ProgrammeOffering";
        } else if (offeringType == ComponentOffering.class) {
            return "LearningComponentOffering";
        }
        return "Offering";
    }
}
