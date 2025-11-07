package oeapi.repository;

import java.util.List;
import oeapi.model.Course;
import oeapi.model.Program;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author Carlos Alonso - losalo@unavarra.es
 */
//public interface ProgramRepository extends JpaRepository<Program, String> {
public interface ProgramRepository extends oeapiUnitaRepositoryBase<Program> {

    Page<Program> findByOrganization_OrganizationId(String organizationId, Pageable pageable);

    Page<Program> findBySectorAndOrganization_OrganizationId(String sector, String organizationId, Pageable pageable);

    Page<Program> findByProgramTypeAndOrganization_OrganizationId(String programType, String organizationId, Pageable pageable);

    Page<Program> findByTeachingLanguageAndOrganization_OrganizationId(String teachingLanguage, String organizationId, Pageable pageable);

    Page<Program> findByQualificationAwardedAndOrganization_OrganizationId(String qualificationAwarded, String organizationId, Pageable pageable);

    Page<Program> findByLevelOfQualificationAndOrganization_OrganizationId(String levelOfQualification, String organizationId, Pageable pageable);

    Page<Program> findByProgramType(String programType, Pageable pageable);

    Page<Program> findByQualificationAwarded(String qualificationAwarded, Pageable pageable);

    Page<Program> findByLevelOfQualification(String levelOfQualification, Pageable pageable);

    Page<Program> findBySector(String sector, Pageable pageable);

    Page<Program> findByFieldsOfStudyId(String fieldsOfStudyId, Pageable pageable);

    Page<Program> findByTeachingLanguage(String teachingLanguage, Pageable pageable);

}
