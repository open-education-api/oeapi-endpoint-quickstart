package oeapi.controller;

import java.util.List;

import oeapi.model.Metadata.SupportedExpand;

/**
 * The expand values this implementation honours, per path. Reported in the
 * Service document ({@code GET /}) as supportedExpands.
 */
public final class SupportedExpands {

    public static final List<SupportedExpand> ALL = List.of(
            new SupportedExpand(List.of("parent", "children", "year"), "/academic-sessions/{academicSessionId}"),
            new SupportedExpand(List.of("organisation", "programmes", "coordinators"), "/courses/{courseId}"),
            new SupportedExpand(List.of("course", "organisation", "academic_session"), "/course-offerings/{courseOfferingId}"),
            new SupportedExpand(List.of("course", "organisation"), "/learning-components/{learningComponentId}"),
            new SupportedExpand(List.of("learning_component", "organisation", "academic_session"), "/learning-component-offerings/{learningComponentOfferingId}"),
            new SupportedExpand(List.of("parent", "children"), "/organisations/{organisationId}"),
            new SupportedExpand(List.of("parent", "children", "organisation", "coordinators"), "/programmes/{programmeId}"),
            new SupportedExpand(List.of("programme", "organisation", "academic_session"), "/programme-offerings/{programmeOfferingId}"),
            new SupportedExpand(List.of("building"), "/rooms/{roomId}"));

    private SupportedExpands() {
    }
}
