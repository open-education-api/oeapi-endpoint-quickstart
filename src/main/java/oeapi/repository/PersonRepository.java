package oeapi.repository;

import oeapi.model.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 *
 * @author itziar.urrutia
 */
@Repository
public interface PersonRepository extends oeapiUnitaRepositoryBase<Person> {

    Page<Person> findByAffiliations(String affiliations, Pageable pageable);

}
