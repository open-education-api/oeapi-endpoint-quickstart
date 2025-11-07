package oeapi.payload;

/**
 *
 * @author itziar.urrutia
 */
public interface oeapiDTOExpandable<T> {

    public T expands(String expand);
}
