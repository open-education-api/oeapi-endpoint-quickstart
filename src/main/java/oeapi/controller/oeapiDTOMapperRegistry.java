package oeapi.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves the DTO mapper that owns a given entity type, so that an expanded value can be
 * converted to its OOAPI shape before it is serialized.
 *
 * <p>Why this exists: expandable fields are declared with the ENTITY type, not the DTO type
 * ({@code oeapiEducationDTO.organization} is an {@code Organization},
 * {@code CourseDTO.programs} is a {@code List<Program>}). Unexpanded that does not matter -
 * only the companion id field is serialized. With {@code ?expand=...} the value itself is
 * serialized, and Jackson then writes the ENTITY's shape: internal columns such as
 * {@code organizationTypeId} instead of the spec's {@code organizationType}, and none of the
 * naming or hiding the DTO applies. Mapping the value through the mapper registered for its
 * type fixes every expansion at once instead of one field at a time.</p>
 *
 * <p>An entity type with no registered mapper - {@code Person},
 * {@code EducationSpecification} and {@code AcademicSession} have no DTO in this codebase yet
 * - is serialized exactly as before. Adding a DTO plus a mapper for one of them is therefore
 * enough to improve its expansions; nothing here needs to change.</p>
 *
 * <p>Registration happens in {@code oeapiEndpointDTOService.setMapper()}, i.e. once a mapper
 * has become THE mapper for its endpoint. When a service replaces a generic mapper with a
 * specialized one (CourseService, ProgramService) the last registration wins, which is the
 * specialized one. The map is static because mappers are created with {@code new} inside the
 * services rather than by Spring, so there is no bean to inject; a later application context
 * simply overwrites the entries of an earlier one.</p>
 */
public final class oeapiDTOMapperRegistry {

    static Logger logger = LoggerFactory.getLogger(oeapiDTOMapperRegistry.class);

    private static final Map<Class<?>, oeapiDTOMapper<?, ?>> MAPPERS_BY_ENTITY = new ConcurrentHashMap<>();

    /**
     * The destination types of every registered mapper. Used to recognize a value that is
     * already a DTO - {@code OrganizationDTO.parent} and {@code .children} hold
     * OrganizationDTOs, because ModelMapper maps that relation itself - and leave it alone.
     */
    private static final Set<Class<?>> DTO_TYPES = ConcurrentHashMap.newKeySet();

    private oeapiDTOMapperRegistry() {
    }

    /**
     * Records a mapper as the one to use for its entity type. Null-tolerant so that a caller
     * never has to guard, and idempotent.
     */
    public static void register(oeapiDTOMapper<?, ?> mapper) {

        if (mapper == null || mapper.getObjectTargetType() == null) {
            return;
        }

        oeapiDTOMapper<?, ?> previous = MAPPERS_BY_ENTITY.put(mapper.getObjectTargetType(), mapper);

        if (mapper.getDtoTargetType() != null) {
            DTO_TYPES.add(mapper.getDtoTargetType());
        }

        if (previous != null && previous != mapper) {
            logger.debug("Expansion mapper for {} replaced: {} -> {}",
                    mapper.getObjectTargetType().getSimpleName(),
                    previous.getClass().getSimpleName(),
                    mapper.getClass().getSimpleName());
        }
    }

    /**
     * Converts an expanded value to its DTO form: a single object, or every element of a
     * collection. Anything without a registered mapper, and anything that is already a DTO,
     * is returned unchanged - so this is safe to call on any expandable field.
     */
    public static Object toDTOGraph(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Collection<?>) {
            Collection<?> items = (Collection<?>) value;
            List<Object> mapped = new ArrayList<>(items.size());

            for (Object item : items) {
                mapped.add(toDTOOrSelf(item));
            }
            return mapped;
        }

        return toDTOOrSelf(value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object toDTOOrSelf(Object entity) {

        if (entity == null) {
            return null;
        }

        oeapiDTOMapper<?, ?> mapper = findMapper(entity.getClass());

        if (mapper == null) {
            return entity;
        }

        try {
            // Raw on purpose: the mapper was found by the value's runtime class, so the
            // types line up at runtime but cannot be expressed here.
            oeapiDTOMapper raw = mapper;
            return raw.toDTO(entity);

        } catch (RuntimeException ex) {
            // An expansion that cannot be converted is still better served entity-shaped
            // than dropped, so fall back rather than failing the request.
            logger.warn("Could not map an expanded {} through its DTO mapper: {}",
                    entity.getClass().getSimpleName(), ex.getLocalizedMessage());
            return entity;
        }
    }

    /**
     * Finds the mapper for a type, walking up the hierarchy. The walk is what makes this work
     * for a lazily loaded association: Hibernate hands out a generated SUBCLASS of the entity,
     * which is not itself a registered key. Hitting a known DTO type first means the value has
     * already been mapped, and null is returned so it is left as it is.
     */
    static oeapiDTOMapper<?, ?> findMapper(Class<?> type) {

        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {

            if (DTO_TYPES.contains(current)) {
                return null;
            }

            oeapiDTOMapper<?, ?> mapper = MAPPERS_BY_ENTITY.get(current);

            if (mapper != null) {
                return mapper;
            }
        }
        return null;
    }
}
