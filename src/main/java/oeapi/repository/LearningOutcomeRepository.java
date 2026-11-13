package oeapi.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import oeapi.model.LearningOutcome;

@Repository
public interface LearningOutcomeRepository extends oeapiUnitaRepositoryBase<LearningOutcome> {

    List<LearningOutcome> findByParents_LearningOutcomeId(String parentId);
}
