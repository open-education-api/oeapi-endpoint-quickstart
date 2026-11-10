package oeapi.repository;

import java.util.Optional;

import oeapi.model.Organisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Repository;

/**
 *
 * @author itziar.urrutia
 */
@Repository
public interface OrganisationRepository extends oeapiUnitaRepositoryBase<Organisation> {

    Optional<Organisation> findByOrganisationId(String organisationId);

    Page<Organisation> findByOrganisationTypeId(Long organisationTypeId, Pageable pageable);

    boolean existsByShortName(String shortName);

}
