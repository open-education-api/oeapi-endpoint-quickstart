package oeapi.service;

import java.util.List;

/**
 *
 * @author itziar.urrutia
 */
public interface oeapiRelatedListServiceInterface<T> {

    public List<T> manageRelated(List<T> relateList) throws Exception;

}
