package oeapi.repository;

import java.util.List;
import java.util.Optional;
import oeapi.model.AcademicSession;
import oeapi.model.Organization;
import oeapi.model.oeapiFieldsOfStudy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

/**
 *
 * @author itziar.urrutia
 */
@Repository
public interface oeapiFieldsOfStudyRepository extends JpaRepository< oeapiFieldsOfStudy, String> {

    Page<oeapiFieldsOfStudy> findByLevel(Long level, Pageable pageable);

    List<oeapiFieldsOfStudy> findByTxtEn(String txtEn);

    Optional<oeapiFieldsOfStudy> findByTxtEnAndLevel(String txtEn, Long level);

    boolean existsByTxtEn(String txtEn);

}
