package oeapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import oeapi.model.ComponentOffering;

/**
 * Queries on learning component offerings (entity ComponentOffering), whose
 * relations live on the subtype rather than on Offering.
 */
public interface ComponentOfferingRepository extends JpaRepository<ComponentOffering, String> {

    List<ComponentOffering> findByComponent_ComponentId(String componentId);

    List<ComponentOffering> findByCourseOffering_OfferingId(String courseOfferingId);
}
