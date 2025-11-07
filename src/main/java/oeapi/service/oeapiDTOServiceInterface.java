package oeapi.service;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 *
 * @author itziar.urrutia
 */
//public interface oeapiDTOServiceInterface<T, S> {
public interface oeapiDTOServiceInterface<T, S> extends oeapiServiceInterface<T> {

    public S toDTO(T item);

    public T toEntity(S item);

    public String toDTOString(T item, String expand);

    public List<S> toDTOList(List<T> list);

    public Page<S> toDTOPages(Page<T> page);

}
