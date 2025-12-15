package oeapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

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
        return mapper;
    }

    /**
     * @param mapper the mapper to set
     */
    public void setMapper(oeapiDTOMapper<T, S> mapper) {
        this.mapper = mapper;
    }

    private ObjectMapper objectMapper = ooapiObjectMapper();

    /*
    @Value("${ooapi.config.autoCreateCoordinatorIfNotExists:false}")
    private boolean autoCreateIfNotExists;

    @Autowired
    protected R repository;

     */
    @Autowired
    private oeapiEnumConversionService enumService;

    private oeapiDTOMapper<T, S> mapper;

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
        if (this.getMapper().getEnumService() == null) {
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

    @Override
    public S toDTO(T item) {
        // This method is used with
        // get courses
        initializeMapper();
        S dto = getMapper().toDTO(item);
        return dto;
        //return mapper.CleanObject(dto);
    }

    @Override
    public String toDTOString(T item, String expand) {

        //initializeMapper();
        //    return mapper.toDTO(item, expand);
        return "";
    }
}
