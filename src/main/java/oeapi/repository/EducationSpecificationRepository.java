package oeapi.repository;

import java.util.List;

import oeapi.model.EducationSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 *
 * @author itziar.urrutia
 */
@Repository
public interface EducationSpecificationRepository extends oeapiUnitaRepositoryBase<EducationSpecification> {

    List<EducationSpecification> findByOrganisation_OrganisationId(String organisationId);

    Page<EducationSpecification> findByEducationSpecificationType(String educationSpecificationType, Pageable pageable);

}
