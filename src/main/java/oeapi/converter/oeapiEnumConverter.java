package oeapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import javax.persistence.AttributeConverter;
import oeapi.model.oeapiUnitaEnumeration;
import oeapi.oeapiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import oeapi.repository.oeapiUnitaEnumerationRepository;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiEnumConverter<Long> implements AttributeConverter<Long, String> {

    private final String enumType;

    @Autowired
    private oeapiUnitaEnumerationRepository ooapiUnitaEnumerationRepository;

    public oeapiEnumConverter(String enumType) {
        this.enumType = enumType;
    }

    @Override
    public String convertToDatabaseColumn(Long attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            java.lang.Long id = (java.lang.Long) attribute;
            Optional<oeapiUnitaEnumeration> ooapiEnum = ooapiUnitaEnumerationRepository.findById(id);
            if (ooapiEnum.isPresent()) {
                return ooapiEnum.get().getValue();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert attribute to JSON", e.getLocalizedMessage());
        }
    }

    @Override
    public Long convertToEntityAttribute(String value) {

        try {
            Optional<oeapiUnitaEnumeration> ooapiEnum = ooapiUnitaEnumerationRepository.findEnumeratorByNameAndValue(this.enumType, value);
            if (ooapiEnum.isPresent()) {
                return (Long) ooapiEnum.get().getId();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert attribute to JSON", e.getLocalizedMessage());
        }
    }

}
