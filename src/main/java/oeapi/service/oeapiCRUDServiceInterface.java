package oeapi.service;

/**
 *
 * @author itziar.urrutia
 */
public interface oeapiCRUDServiceInterface<T> {

    public T create(T object);

    public T update(T object);

    public T update(String id, T object);

    public boolean delete(String id);

}
