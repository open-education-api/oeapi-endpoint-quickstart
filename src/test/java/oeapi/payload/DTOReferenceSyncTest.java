package oeapi.payload;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import oeapi.model.Person;
import oeapi.model.Program;

/**
 * Unit tests for the id / object pairing on expandable DTO relations. No Spring context and no
 * database: these are plain setter calls.
 *
 * The regression under test: ModelMapper writes both halves of a relation in an order nobody
 * controls, so an id setter that unconditionally replaced the object with an id-only stub
 * discarded the real object whenever it ran last - and ?expand=parent, ?expand=children,
 * ?expand=programs then answered with a bare id. The guard lives in oeapiDTORefs.
 *
 * Reading the failures:
 *   - "expected same instance" -> an id setter is building a stub even though it was handed
 *     the id of the object it already holds;
 *   - a null id list -> an object setter is not keeping its id counterpart in sync, so the
 *     unexpanded response would carry no relation at all.
 */
class DTOReferenceSyncTest {

    @Test
    void ProgramParentIdKeepsTheMappedParent() {

        Program parent = new Program("P-1");

        ProgramDTO dto = new ProgramDTO();
        dto.setParent(parent);
        dto.setParentId("P-1");

        assertSame(parent, dto.getParent(), "setParentId replaced the parent it was handed the id of");
        assertEquals("P-1", dto.getParentId());
    }

    @Test
    void ProgramParentIdReplacesADifferentParent() {

        ProgramDTO dto = new ProgramDTO();
        dto.setParent(new Program("P-1"));
        dto.setParentId("P-2");

        assertEquals("P-2", dto.getParent().getProgramId());
        assertEquals("P-2", dto.getParentId());
    }

    @Test
    void ProgramParentIdOfNullClearsTheParent() {

        ProgramDTO dto = new ProgramDTO();
        dto.setParent(new Program("P-1"));
        dto.setParentId(null);

        assertNull(dto.getParent());
        assertNull(dto.getParentId());
    }

    @Test
    void ProgramChildrenIdsKeepTheMappedChildren() {

        List<Program> children = Arrays.asList(new Program("C-1"), new Program("C-2"));

        ProgramDTO dto = new ProgramDTO();
        dto.setChildren(children);
        dto.setChildrenIds(Arrays.asList("C-1", "C-2"));

        assertSame(children, dto.getChildren(), "setChildrenIds replaced the children with stubs");
    }

    /**
     * Order matters, and only in one direction: an id list that does not describe the objects
     * in hand wins, because the ids are what the caller just asked for.
     */
    @Test
    void ProgramChildrenIdsReplaceADifferentSet() {

        ProgramDTO dto = new ProgramDTO();
        dto.setChildren(Arrays.asList(new Program("C-1"), new Program("C-2")));
        dto.setChildrenIds(Arrays.asList("C-3"));

        assertEquals(1, dto.getChildren().size());
        assertEquals("C-3", dto.getChildren().get(0).getProgramId());
    }

    /**
     * setChildren had no id counterpart at all, so a read that went through it left
     * childrenIds null and the unexpanded program carried no "children".
     */
    @Test
    void ProgramSetChildrenPublishesTheirIds() {

        ProgramDTO dto = new ProgramDTO();
        dto.setChildren(Arrays.asList(new Program("C-1"), new Program("C-2")));

        assertEquals(Arrays.asList("C-1", "C-2"), dto.getChildrenIds());
    }

    @Test
    void ProgramCoordinatorIdsKeepTheMappedCoordinators() {

        List<Person> coordinators = Arrays.asList(new Person("PER-1"));

        ProgramDTO dto = new ProgramDTO();
        dto.setCoordinators(coordinators);
        dto.setCoordinatorIds(Arrays.asList("PER-1"));

        assertSame(coordinators, dto.getCoordinators(), "setCoordinatorIds replaced the coordinators with stubs");
    }

    @Test
    void CourseProgramIdsKeepTheMappedPrograms() {

        List<Program> programs = Arrays.asList(new Program("P-1"), new Program("P-2"));

        CourseDTO dto = new CourseDTO();
        dto.setPrograms(programs);
        dto.setProgramIds(Arrays.asList("P-1", "P-2"));

        assertSame(programs, dto.getPrograms(), "setProgramIds replaced the programs with stubs");
    }

    @Test
    void CourseCoordinatorIdsKeepTheMappedCoordinators() {

        List<Person> coordinators = Arrays.asList(new Person("PER-1"), new Person("PER-2"));

        CourseDTO dto = new CourseDTO();
        dto.setCoordinators(coordinators);
        dto.setCoordinatorIds(Arrays.asList("PER-1", "PER-2"));

        assertSame(coordinators, dto.getCoordinators(), "setCoordinatorIds replaced the coordinators with stubs");
    }

    /**
     * A null id list must not throw: the previous loops called for-each on it directly.
     */
    @Test
    void NullIdListsAreTolerated() {

        ProgramDTO program = new ProgramDTO();
        program.setChildrenIds(null);
        program.setCoordinatorIds(null);

        assertNull(program.getChildren());
        assertNull(program.getCoordinators());

        CourseDTO course = new CourseDTO();
        course.setProgramIds(null);
        course.setCoordinatorIds(null);

        assertNull(course.getPrograms());
        assertNull(course.getCoordinators());
    }
}
