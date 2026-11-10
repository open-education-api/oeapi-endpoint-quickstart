package oeapi.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import oeapi.model.Metadata;

import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * OEAPI v6 Service endpoint ({@code GET /}).
 *
 * supportedOperations is derived from the controllers' request mappings, so
 * it always describes what this build really serves. Only OEAPI verbs
 * (GET, PUT, PATCH, POST) are listed and implementation specific paths
 * (loading, administration, authentication...) are left out.
 *
 * @author Carlos Alonso <losalo@unavarra.es>
 */
@RestController
@RequestMapping("/")
public class MetadataController {

    /** Paths of this implementation that are not part of the OEAPI specification. */
    private static final List<String> NON_SPEC_PATH_PARTS = List.of(
            "/auth", "/admin", "/health", "/_quickdashboard", "/enumerator", "/fieldsofstudy",
            "/loadbyjson", "/update", "/deleteBy", "/error");

    private static final Set<String> OEAPI_VERBS = Set.of("GET", "PUT", "PATCH", "POST");

    private final Metadata metadata;
    private final ApplicationContext context;

    // The handler mapping is looked up on first use rather than injected, so this
    // controller never takes part in the creation of the mapping that detects it.
    public MetadataController(Metadata metadata, ApplicationContext context) {
        this.metadata = metadata;
        this.context = context;
    }

    @GetMapping(produces = "application/json")
    public Metadata getMetadata() {
        if (metadata.getSupportedOperations() == null) {
            metadata.setSupportedOperations(supportedOperations());
        }
        if (metadata.getSupportedExpands() == null) {
            metadata.setSupportedExpands(SupportedExpands.ALL);
        }
        return metadata;
    }

    private List<Metadata.SupportedOperation> supportedOperations() {
        Map<String, Set<String>> verbsByPath = new TreeMap<>();
        RequestMappingHandlerMapping handlerMapping =
                context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = entry.getKey();
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();

            for (String path : info.getPatternValues()) {
                if (path.isEmpty() || NON_SPEC_PATH_PARTS.stream().anyMatch(path::contains)) {
                    continue;
                }
                for (RequestMethod method : methods) {
                    if (OEAPI_VERBS.contains(method.name())) {
                        verbsByPath.computeIfAbsent(path, k -> new TreeSet<>()).add(method.name());
                    }
                }
            }
        }

        List<Metadata.SupportedOperation> operations = new ArrayList<>();
        verbsByPath.forEach((path, verbs) -> operations.add(new Metadata.SupportedOperation(new ArrayList<>(verbs), path)));
        return operations;
    }
}
