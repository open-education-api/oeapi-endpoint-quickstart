package oeapi.service;

import java.util.HashMap;
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
}
