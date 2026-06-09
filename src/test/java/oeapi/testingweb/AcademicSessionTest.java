package oeapi.testingweb;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import oeapi.model.AcademicSession;
import org.junit.jupiter.api.Test;

class AcademicSessionTest {

    @Test
    void DeserializeAcademicSessionInsertPayloadWithName() {
        String payload = """
                {
                  "endDate": "2030-01-04",
                  "academicSessionType": "quarter",
                  "startDate": "2029-09-01",
                  "name": [
                    {
                      "language": "en-GB",
                      "value": "EN TRANSLATION: semester 1"
                    },
                    {
                      "language": "nl-NL",
                      "value": "NL VERTALING: semester 1"
                    }
                  ]
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        AcademicSession academicSession = assertDoesNotThrow(
                () -> objectMapper.readValue(payload, AcademicSession.class));

        assertEquals("quarter", academicSession.getAcademicSessionType());
        assertEquals("en-GB", academicSession.getName().get(0).getLanguage());
        assertEquals("nl-NL", academicSession.getName().get(1).getLanguage());
    }
}
