package oeapi.payload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import oeapi.model.Organization;

/**
 * Unit tests for the organization reference on oeapiEducationDTO. No Spring context and no
 * database: these are plain setter calls.
 *
 * The relation is declared twice - "organization", the organizationId the wire format carries,
 * and the Organization itself, which ?expand=organization promotes into its place - and each
 * setter fills in its counterpart. setOrganization keeps the object it is given;
 * setOrganizationId builds an id-only reference stub, which is right on input, where the client
 * sends an id and nothing else, and wrong on a read, where ModelMapper has already supplied the
 * real organization. So setOrganizationId has to build a stub only when it is not already
 * holding the object for that id - and its guard compared Strings with "!=", which compares
 * references rather than contents and was therefore always true.
 *
 * Nothing visibly depended on that, because ModelMapper happens to call setOrganization last
 * for both a course and a program. These tests exist so the DTO no longer depends on an order
 * ModelMapper does not define. The class is abstract, so they go through its two subclasses.
 *
 * The ids below are deliberately two distinct String instances with equal contents, built with
 * new String(...) rather than written as the same literal twice. That is what happens at
 * runtime - one id comes from Hibernate reading the result set, the other from Jackson parsing
 * the request body - and it is the condition the old "!=" comparison got wrong. Two occurrences
 * of the same literal are interned to one instance, so a test written that way passes against
 * the bug and proves nothing.
 *
 * Reading the failures:
 *   - "expected same instance" -> the id setter is rebuilding a stub over an organization it
 *     was handed the id of, which is what reduces ?expand=organization to a bare id.
 */
class OrganizationReferenceSyncTest {

    @Test
    void CourseIdSetterKeepsTheMappedOrganization() {

        String stored = "O-1";
        String fromRequest = new String(stored);   // equal, deliberately not identical

        Organization org = new Organization(stored);
        org.setShortName("UPNA");

        CourseDTO dto = new CourseDTO();
        dto.setOrganization(org);
        dto.setOrganizationId(fromRequest);

        assertSame(org, dto.getOrganization(),
                   "setOrganizationId replaced the organization it was handed the id of");
        assertEquals("UPNA", dto.getOrganization().getShortName());
        assertEquals("O-1", dto.getOrganizationId());
    }

    /**
     * Same setter, inherited: a program has to behave the same way, since the order in which
     * ModelMapper calls the two setters is not guaranteed to match between the two DTOs.
     */
    @Test
    void ProgramIdSetterKeepsTheMappedOrganization() {

        String stored = "O-1";
        String fromRequest = new String(stored);   // equal, deliberately not identical

        Organization org = new Organization(stored);
        org.setShortName("UPNA");

        ProgramDTO dto = new ProgramDTO();
        dto.setOrganization(org);
        dto.setOrganizationId(fromRequest);

        assertSame(org, dto.getOrganization(),
                   "setOrganizationId replaced the organization it was handed the id of");
    }

    /**
     * A different id does replace it - the id is what the caller just asked for.
     */
    @Test
    void IdSetterReplacesADifferentOrganization() {

        CourseDTO dto = new CourseDTO();
        dto.setOrganization(new Organization("O-1"));
        dto.setOrganizationId("O-2");

        assertEquals("O-2", dto.getOrganization().getOrganizationId());
        assertEquals("O-2", dto.getOrganizationId());
    }

    /**
     * An id alone is enough on input: with nothing held, a stub is built, which is what
     * carries the reference into the entity mapper.
     */
    @Test
    void IdSetterBuildsAStubWhenNothingIsHeld() {

        CourseDTO dto = new CourseDTO();
        dto.setOrganizationId("O-1");

        assertEquals("O-1", dto.getOrganization().getOrganizationId());
    }

    @Test
    void NullIdClearsTheOrganization() {

        CourseDTO dto = new CourseDTO();
        dto.setOrganization(new Organization("O-1"));
        dto.setOrganizationId(null);

        assertNull(dto.getOrganization());
        assertNull(dto.getOrganizationId());
    }

    /**
     * And the other direction keeps the id in step, so the unexpanded response still carries
     * the link.
     */
    @Test
    void ObjectSetterPublishesTheId() {

        CourseDTO dto = new CourseDTO();
        dto.setOrganization(new Organization("O-1"));

        assertEquals("O-1", dto.getOrganizationId());

        dto.setOrganization(null);

        assertNull(dto.getOrganizationId());
    }
}
