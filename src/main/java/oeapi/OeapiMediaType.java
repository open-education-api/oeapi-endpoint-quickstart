package oeapi;

import java.util.List;

/**
 * Media types and versions of the OEAPI v6 specification.
 *
 * OEAPI v6 uses a "closed" versioning approach: the client asks for exactly one
 * version in the Accept header, e.g.
 * {@code Accept: application/vnd.oeapi+json;version=6.0}, and the server
 * answers with the version it actually served in the Content-Type header.
 */
public final class OeapiMediaType {

    /** Media type of every successful OEAPI response. */
    public static final String VND_OEAPI_JSON = "application/vnd.oeapi+json";

    /** Media type of every OEAPI error response (RFC 7807). */
    public static final String PROBLEM_JSON = "application/problem+json";

    /** Major version this endpoint implements. */
    public static final int MAJOR_VERSION = 6;

    /** Version reported in the Content-Type header. */
    public static final String SERVED_VERSION = "6.0";

    /** Versions listed in a 406 response and in the service document. */
    public static final List<String> SUPPORTED_VERSIONS = List.of(SERVED_VERSION);

    private OeapiMediaType() {
    }

    /** Content-Type value for a successful response, optionally echoing the consumer tuple. */
    public static String contentType(String consumer, String consumerVersion) {
        StringBuilder sb = new StringBuilder(VND_OEAPI_JSON).append(";version=").append(SERVED_VERSION);
        if (consumer != null && !consumer.isBlank()) {
            sb.append(";consumer=").append(consumer);
            if (consumerVersion != null && !consumerVersion.isBlank()) {
                sb.append(";consumer-version=").append(consumerVersion);
            }
        }
        return sb.toString();
    }
}
