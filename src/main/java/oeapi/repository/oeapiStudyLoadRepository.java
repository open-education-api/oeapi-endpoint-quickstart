package oeapi.repository;

import java.util.Optional;
import oeapi.model.StudyLoad;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Carlos Alonso - losalo@unavarra.es
 */
public interface oeapiStudyLoadRepository extends JpaRepository<StudyLoad, Long> {

    public Optional<StudyLoad> findByStudyLoadUnitAndValue(String StudyLoadUnit, float value);

}
