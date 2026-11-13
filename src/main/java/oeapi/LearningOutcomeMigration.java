package oeapi;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import oeapi.model.oeapiEducation;
import oeapi.repository.ComponentRepository;
import oeapi.repository.CourseRepository;
import oeapi.repository.ProgrammeRepository;
import oeapi.service.LearningOutcomeService;

/**
 * One-off conversion of the v5 inline learning outcomes: on start-up, every
 * programme, course and learning component that still has texts in its
 * learning_outcomes column gets them as LearningOutcome resources, and the
 * column is emptied. Nothing happens once all items are converted.
 */
@Component
public class LearningOutcomeMigration {

    private static final Logger logger = LoggerFactory.getLogger(LearningOutcomeMigration.class);

    @Autowired
    private LearningOutcomeService learningOutcomeService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProgrammeRepository programmeRepository;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void convertInlineLearningOutcomes() {
        transactionTemplate.executeWithoutResult(status -> {
            int converted = convert(courseRepository.findAll(), courseRepository::save)
                    + convert(programmeRepository.findAll(), programmeRepository::save)
                    + convert(componentRepository.findAll(), componentRepository::save);
            if (converted > 0) {
                logger.info("Converted the v5 inline learning outcomes of {} items into learning outcome resources", converted);
            }
        });
    }

    private <T extends oeapiEducation> int convert(List<T> items, java.util.function.Function<T, T> save) {
        int converted = 0;
        for (T item : items) {
            if (item.getLearningOutcomeTexts() != null && !item.getLearningOutcomeTexts().isEmpty()) {
                learningOutcomeService.link(item, item);
                save.apply(item);
                converted++;
            }
        }
        return converted;
    }
}
