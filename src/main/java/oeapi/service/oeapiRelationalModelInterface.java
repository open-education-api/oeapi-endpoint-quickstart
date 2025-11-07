package oeapi.service;

import java.util.List;

/**
 *
 * @author itziar.urrutia
 */
public interface oeapiRelationalModelInterface<T> {

    public T checkRelations(T obj);

    public T normalizeAttributes(T obj);

}
