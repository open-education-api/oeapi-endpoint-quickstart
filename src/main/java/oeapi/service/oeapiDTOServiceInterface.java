package oeapi.service;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.data.domain.Page;

import oeapi.controller.oeapiDTOMapper;

/**
 *
 * @author itziar.urrutia
 */
public interface oeapiDTOServiceInterface<T, S> extends oeapiServiceInterface<T> {
    public T toEntity(S item);

    public List<S> toDTOList(List<T> list);

    public Page<S> toDTOPages(Page<T> page);

    public oeapiDTOMapper<T, S> getMapper();

    public String toJSON(T item) throws JsonProcessingException;
    public String toJSON(T item , String expand) throws JsonProcessingException;
}
