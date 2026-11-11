package oeapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import oeapi.model.Membership;

public interface MembershipRepository extends JpaRepository<Membership, String> {

    List<Membership> findByGroup_GroupId(String groupId);

    Optional<Membership> findByGroup_GroupIdAndPerson_PersonId(String groupId, String personId);

    List<Membership> findByPerson_PersonId(String personId);
}
