package oeapi.repository;

import java.util.Optional;

import oeapi.model.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Repository;

/**
 *
 * @author itziar.urrutia
 */
@Repository
public interface OrganizationRepository extends oeapiUnitaRepositoryBase<Organization> {

    Optional<Organization> findByOrganizationId(String organizationId);

    Page<Organization> findByOrganizationTypeId(Long organizationTypeId, Pageable pageable);

    boolean existsByShortName(String shortName);

}
