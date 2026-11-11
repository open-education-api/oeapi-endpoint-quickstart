package oeapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import oeapi.model.OfferingAssociation;

public interface OfferingAssociationRepository extends JpaRepository<OfferingAssociation, String> {

    List<OfferingAssociation> findByOffering_OfferingId(String offeringId);

    List<OfferingAssociation> findByPerson_PersonId(String personId);
}
