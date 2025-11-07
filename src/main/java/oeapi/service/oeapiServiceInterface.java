package oeapi.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author itziar.urrutia
 */
public interface oeapiServiceInterface<T> extends oeapiCRUDServiceInterface<T> {

    public Page<T> getByPrimaryCode(String code, Pageable pageable);

    public Page<T> getAll(Pageable pageable);

    public Page<T> getByField(String field, String value, Pageable pageable);

    public Page<T> getByField(String field, Long value, Pageable pageable);

    public Optional<T> getById(String id);

    public List<T> getAll();

    public T autoGenerateBasicItem();

    public T autoGenerateBasicItem(String id);

}
