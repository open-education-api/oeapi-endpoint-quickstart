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

    List<Group> findByMembers_PersonId(String personId);

    List<Group> findByOrganization_OrganizationId(String organizationId);

    Page<Group> findByGroupType(String groupType, Pageable pageable);

}
