package oeapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import oeapi.oeapiException;
import oeapi.oeapiUtils;
import oeapi.model.LearningOutcome;
import oeapi.model.PrimaryCode;
import oeapi.model.oeapiEducation;
import oeapi.model.oeapiIdentifierEntry;
import oeapi.model.oeapiLanguageTypedString;
import oeapi.repository.LearningOutcomeRepository;

/**
 * OEAPI v6 learning outcomes, and their links from programmes, courses and
 * learning components.
 */
@Service
@Transactional
public class LearningOutcomeService extends oeapiEndpointService<LearningOutcome, LearningOutcomeRepository> {

    /** Fills childIds, which is derived from the parent links of the other outcomes. */
    public LearningOutcome withChildIds(LearningOutcome outcome) {
        outcome.setChildIds(repository.findByParents_LearningOutcomeId(outcome.getLearningOutcomeId()).stream()
                .map(LearningOutcome::getLearningOutcomeId).collect(Collectors.toList()));
        return outcome;
    }

    public List<LearningOutcome> getChildren(String id) {
        return repository.findByParents_LearningOutcomeId(id);
    }

    /** Replaces id-only references (as received in a request) by the stored outcomes. */
    public List<LearningOutcome> resolve(List<LearningOutcome> references) {
        List<LearningOutcome> resolved = new ArrayList<>();
        for (LearningOutcome reference : references) {
            String id = reference.getLearningOutcomeId();
            resolved.add(repository.findById(id).orElseThrow(() ->
                    new oeapiException(HttpStatus.BAD_REQUEST, "Learning outcome not found",
                            "There is no learning outcome with Id: " + id)));
        }
        return resolved;
    }

    /**
     * Settles the learning outcomes of a programme, course or learning component
     * before it is saved:
     * <ul>
     * <li>learningOutcomeIds given: the referenced outcomes must exist;</li>
     * <li>v5 style texts given: the item's outcomes become exactly those texts,
     * reusing the linked outcomes whose name matches and creating the others;</li>
     * <li>neither given (null) on an existing item: its stored links are kept.</li>
     * </ul>
     */
    public void link(oeapiEducation item, oeapiEducation stored) {
        List<List<oeapiLanguageTypedString>> texts = item.getLearningOutcomeTexts();
        item.setLearningOutcomeTexts(null);

        if (texts != null && !texts.isEmpty()) {
            List<LearningOutcome> current = stored == null || stored.getLearningOutcomes() == null
                    ? new ArrayList<>() : stored.getLearningOutcomes();
            List<LearningOutcome> outcomes = new ArrayList<>();
            for (List<oeapiLanguageTypedString> name : texts) {
                Optional<LearningOutcome> same = current.stream()
                        .filter(outcome -> sameName(outcome.getName(), name)).findFirst();
                outcomes.add(same.orElseGet(() -> repository.save(fromText(name, item, outcomes.size() + 1))));
            }
            item.setLearningOutcomes(outcomes);
        } else if (item.getLearningOutcomes() != null) {
            item.setLearningOutcomes(resolve(item.getLearningOutcomes()));
        } else if (stored != null) {
            item.setLearningOutcomes(stored.getLearningOutcomes() == null
                    ? null : new ArrayList<>(stored.getLearningOutcomes()));
        }
    }

    private static boolean sameName(List<oeapiLanguageTypedString> a, List<oeapiLanguageTypedString> b) {
        if (a == null || b == null || a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!Objects.equals(a.get(i).getLanguage(), b.get(i).getLanguage())
                    || !Objects.equals(a.get(i).getValue(), b.get(i).getValue())) {
                return false;
            }
        }
        return true;
    }

    private LearningOutcome fromText(List<oeapiLanguageTypedString> name, PrimaryCode owner, int index) {
        LearningOutcome outcome = new LearningOutcome();
        outcome.setName(name);
        String ownerCode = owner.getPrimaryCode() == null ? null : owner.getPrimaryCode().getCode();
        String code = (ownerCode == null ? outcome.getLearningOutcomeId() : ownerCode) + "-LO-" + index
                + "-" + outcome.getLearningOutcomeId().substring(0, 8);
        oeapiIdentifierEntry primaryCode = new oeapiIdentifierEntry();
        primaryCode.setCodeType("identifier");
        primaryCode.setCode(code);
        outcome.setPrimaryCode(primaryCode);
        return outcome;
    }

    @Override
    public LearningOutcome create(LearningOutcome outcome) {
        outcome.setParents(resolve(outcome.getParents() == null ? new ArrayList<>() : outcome.getParents()));
        return super.create(outcome);
    }

    @Override
    public LearningOutcome update(LearningOutcome outcome) {
        outcome.setParents(resolve(outcome.getParents() == null ? new ArrayList<>() : outcome.getParents()));
        if (outcome.getParentIds().contains(outcome.getLearningOutcomeId())) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Invalid parent", "A learning outcome cannot be its own parent");
        }
        return super.update(outcome);
    }

    @Override
    public LearningOutcome autoGenerateBasicItem(String id) {
        LearningOutcome outcome = new LearningOutcome(id);
        outcome.setName(oeapiUtils.defaultName());
        outcome.setPrimaryCode(oeapiUtils.defaultPrimaryCode("identifier", id + "-AutoGenerated"));
        return outcome;
    }

    @Override
    public LearningOutcome autoGenerateBasicItem() {
        return autoGenerateBasicItem(UUID.randomUUID().toString());
    }
}
