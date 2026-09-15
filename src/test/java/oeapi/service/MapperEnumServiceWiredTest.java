package oeapi.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Every DTO mapper must have its enum conversion service by the time the context is up.
 *
 * A subclass of oeapiEndpointDTOService builds its mapper in its constructor, where this
 * class's @Autowired fields are still null - field injection happens after the constructor
 * returns - so the mapper is born without one. initializeMapper() repairs that, but it only
 * runs from the service's own methods, so anything reaching past the service straight to the
 * mapper got an unrepaired one: oeapiDTOController.get() does exactly that for a single item,
 * and oeapiDTOMapperService does it for every expanded value.
 *
 * The symptom was an enum-backed field answering with its raw enumeration id -
 * GET /organizations/{id} returning "organizationType": "2" instead of "root" - and whether it
 * happened depended on whether an earlier request had repaired the mapper by accident. Listing
 * /organizations first made the next single GET correct, which is what made it look
 * intermittent.
 *
 * @DirtiesContext is what makes this a regression test rather than a formality: the assertion
 * is about the state of a freshly built context, and any other test class that has already run
 * would otherwise have repaired these mappers through ordinary use.
 *
 * Reading the failures:
 *   - a null enum service here means registerMapper() is no longer repairing the mapper at
 *     startup, and enum-backed fields will intermittently answer with raw enumeration ids.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class MapperEnumServiceWiredTest {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ProgramService programService;

    @Test
    void OrganizationMapperHasItsEnumServiceAfterStartup() {

        assertNotNull(organizationService.getMapper().getEnumService(),
                      "the Organization mapper cannot resolve organizationType without it");
    }

    @Test
    void CourseMapperHasItsEnumServiceAfterStartup() {

        assertNotNull(courseService.getMapper().getEnumService(),
                      "the Course mapper cannot resolve level without it");
    }

    @Test
    void ProgramMapperHasItsEnumServiceAfterStartup() {

        assertNotNull(programService.getMapper().getEnumService(),
                      "the Program mapper cannot resolve its enum fields without it");
    }
}
