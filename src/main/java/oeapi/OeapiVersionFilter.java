package oeapi;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * OEAPI v6 version check ("closed" versioning, see
 * https://oeapi.eu/v6.0/#/technical/versioning).
 *
 * <ul>
 * <li>An Accept header naming {@code application/vnd.oeapi+json} with a
 * {@code version} parameter whose major version is not 6 is answered with
 * 406 and a {@code ProblemVersionNotAcceptable} body.</li>
 * <li>Downstream, the request is presented with {@code Accept: application/json}
 * so the existing controllers keep working.</li>
 * <li>Every {@code application/json} response is relabelled as
 * {@code application/vnd.oeapi+json;version=6.0}, echoing the consumer tuple
 * when one was sent.</li>
 * </ul>
 *
 * Non OEAPI paths (authentication, administration, the dashboard
 * configuration, static files) are left alone.
 *
 * @author Carlos Alonso <losalo@unavarra.es>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class OeapiVersionFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PREFIXES = List.of(
            "/auth", "/admin", "/health", "/_quickdashboard", "/css/", "/js/", "/img/", "/error");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.endsWith(".html") || path.endsWith(".ico")) {
            return true;
        }
        return EXCLUDED_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        Map<String, String> params = oeapiAcceptParameters(request.getHeader("Accept"));

        if (params != null) {
            String requested = params.get("version");
            if (requested != null && !isSupported(requested)) {
                writeNotAcceptable(request, response, requested, params.get("consumer"));
                return;
            }
        }

        String consumer = params == null ? null : params.get("consumer");
        String consumerVersion = params == null ? null : params.get("consumer-version");
        String contentType = OeapiMediaType.contentType(consumer, consumerVersion);

        chain.doFilter(new JsonAcceptRequest(request, params != null),
                       new VndContentTypeResponse(response, contentType));
    }

    /** True when the requested version has the major version this server implements. */
    static boolean isSupported(String requested) {
        String major = requested.trim();
        int dot = major.indexOf('.');
        if (dot >= 0) {
            major = major.substring(0, dot);
        }
        try {
            return Integer.parseInt(major) == OeapiMediaType.MAJOR_VERSION;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Returns the media type parameters of the {@code application/vnd.oeapi+json}
     * entry of an Accept header, or null if the header has no such entry.
     */
    static Map<String, String> oeapiAcceptParameters(String accept) {
        if (accept == null) {
            return null;
        }
        for (String range : accept.split(",")) {
            String[] parts = range.split(";");
            if (!parts[0].trim().equalsIgnoreCase(OeapiMediaType.VND_OEAPI_JSON)) {
                continue;
            }
            Map<String, String> params = new LinkedHashMap<>();
            for (int i = 1; i < parts.length; i++) {
                int eq = parts[i].indexOf('=');
                if (eq > 0) {
                    String name = parts[i].substring(0, eq).trim().toLowerCase(Locale.ROOT);
                    String value = parts[i].substring(eq + 1).trim().replace("\"", "");
                    params.put(name, value);
                }
            }
            return params;
        }
        return null;
    }

    private void writeNotAcceptable(HttpServletRequest request, HttpServletResponse response,
            String requested, String consumer) throws IOException {

        oeapiProblem problem = new oeapiProblem(HttpServletResponse.SC_NOT_ACCEPTABLE,
                "Version not acceptable",
                "The requested OEAPI version '" + requested + "' cannot be served.");
        problem.setInstance(request.getRequestURL().toString());
        if (consumer != null) {
            problem.with("consumer", Map.of("consumerKey", consumer));
        }
        problem.with("requestedVersion", requested);
        problem.with("supportedVersions", OeapiMediaType.SUPPORTED_VERSIONS);

        response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        response.setContentType(OeapiMediaType.PROBLEM_JSON);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), problem);
    }

    /** Presents an OEAPI Accept header as application/json to the controllers. */
    private static final class JsonAcceptRequest extends HttpServletRequestWrapper {

        private final boolean replace;

        JsonAcceptRequest(HttpServletRequest request, boolean replace) {
            super(request);
            this.replace = replace;
        }

        @Override
        public String getHeader(String name) {
            if (replace && "Accept".equalsIgnoreCase(name)) {
                return "application/json";
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (replace && "Accept".equalsIgnoreCase(name)) {
                return Collections.enumeration(List.of("application/json"));
            }
            return super.getHeaders(name);
        }
    }

    /** Relabels application/json responses with the OEAPI v6 media type. */
    private static final class VndContentTypeResponse extends HttpServletResponseWrapper {

        private final String vndContentType;

        VndContentTypeResponse(HttpServletResponse response, String vndContentType) {
            super(response);
            this.vndContentType = vndContentType;
        }

        private String translate(String value) {
            if (value != null && value.toLowerCase(Locale.ROOT).startsWith("application/json")) {
                return vndContentType;
            }
            return value;
        }

        @Override
        public void setContentType(String type) {
            super.setContentType(translate(type));
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, "Content-Type".equalsIgnoreCase(name) ? translate(value) : value);
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, "Content-Type".equalsIgnoreCase(name) ? translate(value) : value);
        }
    }
}
