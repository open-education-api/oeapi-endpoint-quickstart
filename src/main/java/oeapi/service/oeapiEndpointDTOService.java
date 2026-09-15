package oeapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import oeapi.controller.oeapiDTOMapper;
import static oeapi.oeapiUtils.ooapiObjectMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import oeapi.repository.oeapiUnitaRepositoryBase;

/**
 *
 * @author itziar.urrutia
 */
@Service
@Transactional
public abstract class oeapiEndpointDTOService<T, R extends oeapiUnitaRepositoryBase<T>, S> extends oeapiEndpointService<T, R> implements oeapiDTOServiceInterface<T, S> {

    /**
     * @return the mapper
     */
    public oeapiDTOMapper<T, S> getMapper() {
        if (mapper != null) mapper.mapperService = mapperService;
        return mapper;
    }

    /**
     * @param mapper the mapper to set
     */
    public void setMapper(oeapiDTOMapper<T, S> mapper) {
        this.mapper = mapper;
    }

    /*
    @Value("${ooapi.config.autoCreateCoordinatorIfNotExists:false}")
    private boolean autoCreateIfNotExists;

    @Autowired
    protected R repository;

     */
    @Autowired
    private oeapiEnumConversionService enumService;

    @Autowired
    private oeapiDTOMapperService mapperService;

    private oeapiDTOMapper<T, S> mapper;

    @PostConstruct
    public void registerMapper() {
        // A subclass builds its mapper in its constructor, where the @Autowired fields of this
        // class are still null - field injection happens after the constructor returns - so the
        // mapper is born without an enum conversion service. initializeMapper() repairs that,
        // but it only runs from this class's own methods, so a caller that reaches past the
        // service straight to the mapper got an unrepaired one: oeapiDTOController.get() does
        // that for a single item, and oeapiDTOMapperService does it for every expanded value.
        // The symptom was an enum-backed field answering with its raw enumeration id, and it
        // depended on whether some earlier request had happened to repair the mapper first.
        // @PostConstruct runs after injection, so doing it here settles it once for every
        // mapper, whatever calls it afterwards.
        initializeMapper();
        mapperService.register(getMapper());
    }

    static Logger logger = LoggerFactory.getLogger(oeapiEndpointDTOService.class);

    public Page<T> getByField(String capitalizedMethodName, String value, Pageable pageable) {

        Map.Entry<String, Long> mapValue = getMapper().mapValue(capitalizedMethodName, value, enumService);

        if (mapValue == null) {
            return super.getByField(capitalizedMethodName, value, pageable);
        } else {
            return super.getByField(mapValue.getKey(), mapValue.getValue(), pageable);
        }

    }

    public void initializeMapper(Class<T> objectTargetType, Class<S> dtoTargetType, oeapiEnumConversionService ooapiEnumService, List<String> enumFields) {
        if (getMapper() == null) {
            setMapper((oeapiDTOMapper<T, S>) new oeapiDTOMapper(objectTargetType, dtoTargetType, ooapiEnumService, enumFields));
        }
    }

    public void initializeMapper(Class<T> objectTargetType, Class<S> dtoTargetType, List<String> enumFields) {

        if (getMapper() == null) {
            setMapper((oeapiDTOMapper<T, S>) new oeapiDTOMapper(objectTargetType, dtoTargetType, enumService, enumFields));
        }
    }

    public void initializeMapper() {
        if (this.getMapper() != null && this.getMapper().getEnumService() == null) {
            this.getMapper().setEnumService(enumService);
        }
    }

    @Override
    public T toEntity(S dto) {
        initializeMapper();
        return getMapper().toEntity(dto);
    }

    @Override
    public List<S> toDTOList(List<T> items) {
        initializeMapper();
        List<S> dtos = getMapper().toDTOList(items);
        return dtos;
        //return mapper.Clean(dtos);
    }

    public Page<S> toPageDTO(Page<T> pages) {
        initializeMapper();
        return getMapper().toPageDTO(pages);
    }

    @Override
    public Page<S> toDTOPages(Page<T> pages) {
        initializeMapper();
        Page<S> dtoPages = getMapper().toPageDTO(pages);
        return dtoPages;
        //return mapper.Clean(dtoPages);
    }

    public String toJSON(T item) throws JsonProcessingException {
        initializeMapper();
        return getMapper().toJSON(item, null);
    }

    public String toJSON(T item, String expand) throws JsonProcessingException {
        initializeMapper();
        return getMapper().toJSON(item, expand);
    }
}
