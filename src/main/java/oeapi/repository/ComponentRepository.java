package oeapi.repository;

import java.util.List;
import oeapi.model.Component;
import oeapi.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 *
 * @author itziar.urrutia
 */
@Repository
public interface ComponentRepository extends oeapiUnitaRepositoryBase<Component> {

    Page<Component> findByCourse_CourseId(String courseId, Pageable pageable);

    Page<Component> findByOrganization_OrganizationId(String organizationId, Pageable pageable);

    Page<Component> findByComponentTypeId(Long componentTypeId, Pageable pageable);
    
    List<Component> findByCourse(Course course);

    List<Component> findByCourse_CourseId(String courseId);

}
