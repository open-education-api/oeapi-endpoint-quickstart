package oeapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class OeapiVersionFilterTest {

    @Test
    void parsesVersionAndConsumerTuple() {
        Map<String, String> p = OeapiVersionFilter.oeapiAcceptParameters(
                "application/vnd.oeapi+json;version=6.1;consumer=rio;consumer-version=2.0");
        assertEquals("6.1", p.get("version"));
        assertEquals("rio", p.get("consumer"));
        assertEquals("2.0", p.get("consumer-version"));
    }

    @Test
    void ignoresOtherMediaTypes() {
        assertNull(OeapiVersionFilter.oeapiAcceptParameters("application/json"));
        assertNull(OeapiVersionFilter.oeapiAcceptParameters("*/*"));
        assertNull(OeapiVersionFilter.oeapiAcceptParameters(null));
    }

    @Test
    void acceptsOnlyMajorVersionSix() {
        assertTrue(OeapiVersionFilter.isSupported("6.0"));
        assertTrue(OeapiVersionFilter.isSupported("6.3"));
        assertFalse(OeapiVersionFilter.isSupported("5.0"));
        assertFalse(OeapiVersionFilter.isSupported("7.0"));
        assertFalse(OeapiVersionFilter.isSupported("abc"));
    }

    @Test
    void contentTypeEchoesConsumer() {
        assertEquals("application/vnd.oeapi+json;version=6.0", OeapiMediaType.contentType(null, null));
        assertEquals("application/vnd.oeapi+json;version=6.0;consumer=rio;consumer-version=2.0",
                     OeapiMediaType.contentType("rio", "2.0"));
    }
}
