package oeapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import oeapi.model.oeapiIdentifierEntry;
import oeapi.model.PrimaryCode;
import oeapi.oeapiException;
import oeapi.oeapiUtils;

import javax.validation.ValidationException;
import javax.transaction.Transactional;
import oeapi.model.Course;
import oeapi.model.Room;
import static oeapi.oeapiUtils.ooapiObjectMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
public abstract class oeapiEndpointService<T, R extends oeapiUnitaRepositoryBase<T>> implements oeapiServiceInterface<T> {

    private ObjectMapper objectMapper = ooapiObjectMapper();

    @Value("${ooapi.config.autoCreateByDefault:false}")
    private boolean autoCreateIfNotExists;

    @Autowired
    protected R repository;

    static Logger logger = LoggerFactory.getLogger(oeapiEndpointService.class);

    @Override
    public Page<T> getByField(String field, String value, Pageable pageable) {
        String capitalizedMethodName = "findBy" + field.substring(0, 1).toUpperCase() + field.substring(1);
        return findByDynamicMethod(capitalizedMethodName, value, pageable);
    }

    public Page<T> getByField(String field, Long value, Pageable pageable) {
        String capitalizedMethodName = "findBy" + field.substring(0, 1).toUpperCase() + field.substring(1);
        return findByDynamicMethod(capitalizedMethodName, value, pageable);
    }

    @Override
    public Page<T> getByPrimaryCode(String code, Pageable pageable) {

        oeapiIdentifierEntry item = null;

        try {
            item = objectMapper.readValue("{\"codeType\": \"identifier\",\"code\": \"" + code + "\"}", oeapiIdentifierEntry.class);
        } catch (JsonProcessingException ex) {
            logger.error(ex.getLocalizedMessage());
            return Page.empty(pageable);
        }
        return repository.findByPrimaryCode(item, pageable);
    }

    protected List<T> getObjectByPrimaryCode(String code) {

        oeapiIdentifierEntry item = null;

        try {
            item = objectMapper.readValue("{\"codeType\": \"identifier\",\"code\": \"" + code + "\"}", oeapiIdentifierEntry.class);
        } catch (JsonProcessingException ex) {
            logger.error(ex.getLocalizedMessage());
            return new ArrayList<T>();
        }
        return repository.findByPrimaryCode(item);
    }

    @Override
    public Optional<T> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<T> getAll() {
        return repository.findAll();
    }

    protected void delete(T object) {
        repository.delete(object);
    }

    @Override
    public boolean delete(String id) {
        Optional<T> existing = this.getById(id);
        if (existing.isPresent()) {
            this.delete(existing.get());
            return true;
        } else {
            return false;
        }
    }

    protected Page<T> findByDynamicMethod(String sufixMethod, String id, Map.Entry<String, String> filter, Pageable pageable) {
        String findBy = "findBy";
        String capitalizedMethodName;
        if (filter == null) {
            capitalizedMethodName = findBy + sufixMethod;
            return this.findByDynamicMethod(capitalizedMethodName, id, pageable);
        }
        String field = filter.getKey();
        String value = filter.getValue();
        capitalizedMethodName = findBy + field.substring(0, 1).toUpperCase() + field.substring(1) + "And" + sufixMethod;
        return this.findByDynamicMethod(capitalizedMethodName, value, id, pageable);

    }

    protected Page<T> findByDynamicMethod(String capitalizedMethodName, String value, String id, Pageable pageable) {

        Class<?>[] parameterTypes;
        if (pageable != null) {
            parameterTypes = new Class<?>[]{value.getClass(), id.getClass(), Pageable.class};
        } else {
            parameterTypes = new Class<?>[]{value.getClass(), id.getClass()};
        }

        logger.debug("findByDynamicMethod FilterFinder: " + capitalizedMethodName);
        try {
            // Capitalize method name to match the repository method convention (e.g., findByName)
            //String capitalizedMethodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);

            // Get the corresponding method from the repository using reflection
            Method method = repository.getClass().getMethod(capitalizedMethodName, parameterTypes);

            // Invoke the method on the repository and return the result
            return (Page<T>) method.invoke(repository, value, id, pageable);
        } catch (Exception e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Error invoking method: ", e.getLocalizedMessage());
        }
    }

    protected Page<T> findByDynamicMethod(String capitalizedMethodName, Long value, Pageable pageable) {

        Class<?>[] parameterTypes;
        if (pageable != null) {
            parameterTypes = new Class<?>[]{value.getClass(), Pageable.class};
        } else {
            parameterTypes = new Class<?>[]{value.getClass()};
        }

        logger.debug("findByDynamicMethod FilterFinder: " + capitalizedMethodName);
        try {
            // Capitalize method name to match the repository method convention (e.g., findByName)
            //String capitalizedMethodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);

            // Get the corresponding method from the repository using reflection
            Method method = repository.getClass().getMethod(capitalizedMethodName, parameterTypes);

            // Invoke the method on the repository and return the result
            return (Page<T>) method.invoke(repository, value, pageable);
        } catch (Exception e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Error invoking method: ", e.getLocalizedMessage());
        }
    }

    protected Page<T> findByDynamicMethod(String capitalizedMethodName, String value, Pageable pageable) {

        Class<?>[] parameterTypes;
        if (pageable != null) {
            parameterTypes = new Class<?>[]{value.getClass(), Pageable.class};
        } else {
            parameterTypes = new Class<?>[]{value.getClass()};
        }

        logger.debug("findByDynamicMethod FilterFinder: " + capitalizedMethodName);
        try {
            // Capitalize method name to match the repository method convention (e.g., findByName)
            //String capitalizedMethodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);

            // Get the corresponding method from the repository using reflection
            Method method = repository.getClass().getMethod(capitalizedMethodName, parameterTypes);

            // Invoke the method on the repository and return the result
            return (Page<T>) method.invoke(repository, value, pageable);
        } catch (Exception e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Error invoking method: ", e.getLocalizedMessage());
        }
    }

    public boolean exists(T obj) {
        String id = findIdOfObject(obj);
        return repository.existsById(id);
    }

    @Override
    public T create(T obj) {

        String id = findIdOfObject(obj);

        Optional<T> existing = (id == null ? Optional.empty() : repository.findById(id));
        if (existing.isPresent()) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "This element [" + id + "] already exists, cannot create it ");
        } else {
            PrimaryCode ooapiObject = (PrimaryCode) obj;
            List<T> existingElements = getObjectByPrimaryCode(ooapiObject.getPrimaryCode().getCode());

            if (!existingElements.isEmpty()) {
                throw new oeapiException(HttpStatus.BAD_REQUEST, "This element [" + ooapiObject.getPrimaryCode().getCode() + "] already exists, cannot create it ");

            } else {
                return this.save(obj);

            }
        }
    }

    public T create(T obj, boolean uniquePrimaryCode) {

        logger.debug("T Create with uniquePrimaryCode=" + uniquePrimaryCode + " " + oeapiUtils.debugJSON(obj));

        if (uniquePrimaryCode) {
            logger.debug("T Create has uniquePrimaryCode, exiting by create");
            return this.create(obj);
        }

        String id = findIdOfObject(obj);

        Optional<T> existing = (id == null ? Optional.empty() : repository.findById(id));
        if (existing.isPresent()) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "This element [" + id + "] already exists, cannot create it ");
        }
        logger.debug("T Create, object is not on DB, saving it...");
        return this.save(obj);
    }

    private T save(T obj) {

        logger.debug("ooapiEndPointService T Save obj=> " + oeapiUtils.debugJSON(obj));

        try {
            T created = repository.save(obj);
            repository.flush();
            return created;
        } catch (ValidationException ve) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "ooapiEndPointService T Save: Json validation error", ve.getCause().getMessage());
        } catch (Exception e) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "ooapiEndPointService T Save:" + e.getMessage());
        }

    }

    @Override
    public T update(T obj) {

        try {
            T created = repository.save(obj);
            repository.flush();
            return created;
        } catch (Exception e) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public T update(String id, T obj) {
        Optional<T> existing = this.getById(id);
        if (existing.isPresent()) {
            T updated = this.update(existing.get());
            return updated;
        } else {
            return this.create(obj);
        }
    }

    @Override
    public Page<T> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    protected String findIdOfObject(T obj) {

        String id;

        logger.debug("findIdOfObject, Object Type :" + obj.getClass());

        try {

            logger.debug("findIdOfObject, looking for ID in :" + obj.getClass());

            logger.debug("findIdOfObject, searching internal fields for @Id...");
            id = (String) oeapiUtils.getId(obj);

            if (id == null) {
                logger.debug("findIdOfObject, no field for @Id found. Has this object some getId() method (i.e from superclass)?");
                Method method = obj.getClass().getMethod("getId");
                if (method != null) {
                    logger.debug("findIdOfObject, Method getId exists for class " + obj.getClass());
                    id = (String) method.invoke(obj);
                }

                logger.debug("findIdOfObject, Id is : " + id);
            }
        } catch (Exception e) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "findIdOfObject says maybe this is not an entity. No @Id field found: " + e.getLocalizedMessage());
        }

        return id;
    }

    public List<T> manageRelated(List<T> relateList) throws Exception {
        return this.manageRelated(relateList, autoCreateIfNotExists);
    }

    public List<T> manageRelated(List<T> relateList, boolean autoCreateIfNotExists) throws Exception {

        logger.debug("manageRelated autoCreateIfNotExists is: " + autoCreateIfNotExists);
        List<T> newRelateList = new ArrayList<>();

        for (T relate : relateList) {
            String id = oeapiUtils.getId(relate).toString();
            logger.debug("manageRelated examining Id: " + id);
            Optional<T> relExisting = manageRelated(id);

            if (relExisting.isPresent()) {
                logger.debug("manageRelated found Id. Then add Id: " + id + " to owner object");
                newRelateList.add(relExisting.get());
            } else if (!autoCreateIfNotExists) {
                throw new oeapiException(HttpStatus.BAD_REQUEST, "Check related list", "Element [" + id + "] not found");
            } else {
                // Autocreate should be done at each item service
                logger.debug("manageRelated has NOT FOUND Id. " + id + " but cannot create it at this level");
                throw new oeapiException(HttpStatus.BAD_REQUEST, "Check related list", "Element [" + id + "] not found and cannot autocreate it at this level");
            }
        }

        return newRelateList;
    }

    public Optional<T> manageRelated(String id) throws Exception {
        Optional<T> objExisting = getById(id);
        return objExisting;
    }

    public Optional<T> manageRelated(T obj) throws Exception {
        String id = oeapiUtils.getId(obj).toString();
        return manageRelated(id);
    }

}
