package oeapi.service;

import java.util.Optional;

/**
 *
 * @author itziar.urrutia
 */
public interface oeapiRelatedObjServiceInterface<T> {

    public Optional<T> manageRelated(T relate) throws Exception;

}
