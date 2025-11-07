package oeapi.repository;

import java.util.List;
import oeapi.model.Course;
import oeapi.model.CourseOffering;
import oeapi.model.Offering;

/**
 *
 * @author itziar.urrutia
 */
public interface OfferingRepository extends oeapiUnitaRepositoryBase<Offering> {

    List<CourseOffering> findByCourse(Course course);

    List<CourseOffering> findByCourse_CourseId(String courseId);

}
