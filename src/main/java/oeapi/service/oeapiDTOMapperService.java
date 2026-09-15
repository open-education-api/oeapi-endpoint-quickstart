package oeapi.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import oeapi.controller.oeapiDTOMapper;

@Service
public class oeapiDTOMapperService {
    public static class MapperNotFound extends RuntimeException { }

    private Map<Class<?>, oeapiDTOMapper<?, ?>> mappers = new HashMap<Class<?>, oeapiDTOMapper<?, ?>>();

    public void register(oeapiDTOMapper<?, ?> mapper) {
        mappers.put(mapper.objectTargetType, mapper);
    }

    @SuppressWarnings({"rawtypes"})
    public oeapiDTOMapper get(Class<?> objectTargetType) throws MapperNotFound {
        oeapiDTOMapper<?, ?> mapper = mappers.get(objectTargetType);

        if (mapper == null)
            throw new MapperNotFound();

        return mapper;
    }

    @SuppressWarnings({"unchecked"})
    public Object toDTO(Object obj) {
        if (obj == null)
            return null;

        return get(obj.getClass()).toDTO(obj);
    }

    /**
     * The value of an expandable field, converted: a single object, or every element of a
     * collection. Never throws - a type with no registered mapper is returned unchanged - so
     * it is safe to call on any expandable field.
     *
     * A to-many relation is why this exists. ?expand=programs and ?expand=coordinators on a
     * course, and ?expand=children on a program, hand over a List (a Hibernate PersistentBag,
     * in fact), and a List's own class will never have a mapper, so looking the value up
     * directly finds nothing and the expansion answers with entity-shaped elements. Each
     * element has to be converted individually.
     */
    public Object toExpandedValue(Object value) {
        if (value == null)
            return null;

        if (value instanceof Collection<?>) {
            Collection<?> items = (Collection<?>) value;
            List<Object> mapped = new ArrayList<>(items.size());

            for (Object item : items)
                mapped.add(toDTOOrSelf(item));

            return mapped;
        }

        return toDTOOrSelf(value);
    }

    private Object toDTOOrSelf(Object value) {
        if (value == null)
            return null;

        try {
            return toDTO(value);
        } catch (MapperNotFound ex) {
            return value; // no DTO for this type: leave it as it is
        }
    }
}
