package oeapi.repository;

import java.util.List;
import oeapi.model.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 *
 * @author itziar.urrutia
 */
@Repository
public interface GroupRepository extends oeapiUnitaRepositoryBase<Group> {

    List<Group> findByOrganisation_OrganisationId(String organisationId);

    Page<Group> findByGroupType(String groupType, Pageable pageable);

}
