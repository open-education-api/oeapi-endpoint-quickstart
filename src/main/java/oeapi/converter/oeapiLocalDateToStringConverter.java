package oeapi.converter;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author itziar.urrutia
 */

public class oeapiLocalDateToStringConverter implements Converter<LocalDate, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public String convert(MappingContext<LocalDate, String> context) {
        return (context.getSource() != null) ? context.getSource().format(FORMATTER) : null;
    }
}
