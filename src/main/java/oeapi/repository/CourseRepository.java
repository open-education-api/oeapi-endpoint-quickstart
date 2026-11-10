package oeapi.repository;

import java.util.List;
import java.util.Optional;

import oeapi.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * The interface Courses repository.
 *
 * @author Carlos Alonso - losalo@unavarra.es
 */
@Repository
public interface CourseRepository extends oeapiUnitaRepositoryBase<Course> {

    Optional<Course> findByCourseId(String courseId);

    // Si le damos un programmeId
    public List<Course> findByProgrammes_ProgrammeId(String programmeId);

    public List<Course> findByModeOfDelivery_Name(String modelOfdeliveryName);

    Page<Course> findByOrganisation_OrganisationId(String organisationId, Pageable pageable);

    //Page<Course> findByLevelAndOrganisation_OrganisationId(String level, String organisationId, Pageable pageable);
    Page<Course> findByTeachingLanguagesContainingAndOrganisation_OrganisationId(String teachingLanguage, String organisationId, Pageable pageable);

    //Page<Course> findByLevel(String level, Pageable pageable);
    Page<Course> findByTeachingLanguagesContaining(String teachingLanguage, Pageable pageable);

    Page<Course> findByLevelId(Long levelId, Pageable pageable);

    Page<Course> findByFieldsOfStudyId(String fieldsOfStudyId, Pageable pageable);

}
