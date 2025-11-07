package oeapi.repository;

import java.util.Optional;
import oeapi.model.oeapiUnitaEnumeration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author itziar.urrutia
 */
@Repository
public interface oeapiUnitaEnumerationRepository extends JpaRepository<oeapiUnitaEnumeration, Long> {

    @Query("SELECT e from ooapi_unita_enumeration e  where e.name = :name and e.value = :value")
    Optional<oeapiUnitaEnumeration> findEnumeratorByNameAndValue(@Param("name") String name, @Param("value") String value);

}
