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

    // Si le damos un programId
    public List<Course> findByPrograms_ProgramId(String programId);

    public List<Course> findByModeOfDelivery_Name(String modelOfdeliveryName);

    Page<Course> findByOrganization_OrganizationId(String organizationId, Pageable pageable);

    //Page<Course> findByLevelAndOrganization_OrganizationId(String level, String organizationId, Pageable pageable);
    Page<Course> findByTeachingLanguageAndOrganization_OrganizationId(String teachingLanguage, String organizationId, Pageable pageable);

    //Page<Course> findByLevel(String level, Pageable pageable);
    Page<Course> findByTeachingLanguage(String teachingLanguage, Pageable pageable);

    Page<Course> findByLevelId(Long levelId, Pageable pageable);

    Page<Course> findByFieldsOfStudyId(String fieldsOfStudyId, Pageable pageable);

}
