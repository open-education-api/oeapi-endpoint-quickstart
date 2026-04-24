package oeapi.repository;

import java.util.List;

import oeapi.model.CourseOffering;
import oeapi.model.Offering;
import oeapi.model.ProgramOffering;

/**
 *
 * @author itziar.urrutia
 */
public interface OfferingRepository extends oeapiUnitaRepositoryBase<Offering> {

    List<CourseOffering> findByCourse_CourseId(String courseId);

    List<ProgramOffering> findByProgram_ProgramId(String programId);

}
