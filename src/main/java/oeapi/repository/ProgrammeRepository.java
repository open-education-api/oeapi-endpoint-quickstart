package oeapi.repository;

import java.util.List;
import oeapi.model.Course;
import oeapi.model.Programme;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author Carlos Alonso - losalo@unavarra.es
 */
//public interface ProgrammeRepository extends JpaRepository<Programme, String> {
public interface ProgrammeRepository extends oeapiUnitaRepositoryBase<Programme> {

    Page<Programme> findByOrganisation_OrganisationId(String organisationId, Pageable pageable);

    Page<Programme> findByProgrammeTypeAndOrganisation_OrganisationId(String programmeType, String organisationId, Pageable pageable);

    Page<Programme> findByTeachingLanguagesContainingAndOrganisation_OrganisationId(String teachingLanguage, String organisationId, Pageable pageable);

    Page<Programme> findByQualificationAwardedAndOrganisation_OrganisationId(String qualificationAwarded, String organisationId, Pageable pageable);

    Page<Programme> findByLevelOfQualificationAndOrganisation_OrganisationId(String levelOfQualification, String organisationId, Pageable pageable);

    Page<Programme> findByProgrammeType(String programmeType, Pageable pageable);

    Page<Programme> findByQualificationAwarded(String qualificationAwarded, Pageable pageable);

    Page<Programme> findByLevelOfQualification(String levelOfQualification, Pageable pageable);

    Page<Programme> findByFieldsOfStudyId(String fieldsOfStudyId, Pageable pageable);

    Page<Programme> findByTeachingLanguagesContaining(String teachingLanguage, Pageable pageable);

}
