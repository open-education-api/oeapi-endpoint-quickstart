package oeapi.repository;

import oeapi.model.AcademicSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Repository;

/**
 *
 * @author itziar.urrutia
 */
@Repository
public interface AcademicSessionRepository extends oeapiUnitaRepositoryBase<AcademicSession> {

    Page<AcademicSession> findByAcademicSessionType(String academicSessionType, Pageable pageable);

}
