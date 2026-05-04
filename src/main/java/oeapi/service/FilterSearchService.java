package oeapi.service;

import oeapi.model.Filter;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oeapi.model.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FilterSearchService {

    private static Logger logger = LoggerFactory.getLogger(FilterSearchService.class);

    private static final Pattern FILTER_PATTERN = Pattern.compile("filter_query\\[(.+?)\\]\\[(.+?)\\]");

    // Helpers to build the correct JsonPath
    private static final Set<String> ARRAY_FIELDS = Set.of("consumers", "alliances", "themes","modeOfDelivery", "name");
    private static final Set<String> PRIMITIVE_ARRAY_FIELDS = Set.of("themes", "targetAudience", "language"); // arrays of strings/numbers    


    public List<Course> search(List<Course> courses, Map<String, String> params) {

        List<Filter> filters = parseFilters(params);

        return courses.stream()
                .filter(course -> matchesAllFilters(course.toString(), filters))
                .toList();

    }

    private List<Filter> parseFilters(Map<String, String> params) {

        List<Filter> filters = new ArrayList<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {

            Matcher matcher = FILTER_PATTERN.matcher(entry.getKey());

            if (matcher.matches()) {
                String field = matcher.group(1);
                String operator = matcher.group(2);

                validate(field, operator);

                filters.add(new Filter(field, operator, entry.getValue()));
            }
        }

        return filters;
    }

    private boolean matchesAllFilters(String json, List<Filter> filters) {

        ReadContext ctx = JsonPath.parse(json);

        for (Filter filter : filters) {

            String jsonPath = buildJsonPath(filter, false);

            logger.debug("JsonPath for search: " + jsonPath);
            logger.debug("Filter: " + filter.getField() + "=" + filter.getValue());

            try {
                Object result = ctx.read(jsonPath);

                logger.debug("Result: " + result);

                if (result instanceof List<?> list) {
                    if (list.isEmpty()) {
                        return false;
                    }
                } else if (result == null) {
                    return false;
                }

            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    private String buildJsonPath(Filter filter, boolean forJavaJayway) {
        String[] parts = filter.getField().split("\\.");
        String lastPart = parts[parts.length - 1];
        String value = escape(filter.getValue());

        String operator = switch (filter.getOperator().toLowerCase()) {
            case "eq" ->
                "==";
            case "neq" ->
                "!=";
            case "gt" ->
                ">";
            case "lt" ->
                "<";
            default ->
                throw new IllegalArgumentException("Unsupported operator: " + filter.getOperator());
        };

        if (forJavaJayway) {
            StringBuilder path = new StringBuilder("$");

            for (int i = 0; i < parts.length; i++) {
                path.append(".").append(parts[i]);

                if (ARRAY_FIELDS.contains(parts[i])) {
                    path.append("[*]");
                }
            }

            if (PRIMITIVE_ARRAY_FIELDS.contains(lastPart)) {
                // use .indexOf for arrays of primitives
                path = new StringBuilder("$[?(" + path + ".indexOf('" + value + "') != -1)]");
            } else {
                path = new StringBuilder("$[?(" + path + " " + operator + " '" + value + "')]");
            }

            return path.toString();
        } else {
            // Online evaluator: detect match only
            if (PRIMITIVE_ARRAY_FIELDS.contains(lastPart)) {
                // e.g., $..themes[?(@ == 'Ecology')]
                return "$.." + lastPart + "[?(@ " + operator + " '" + value + "')]";
            } else {
                // scalar fields inside object arrays
                StringBuilder path = new StringBuilder("$");
                for (int i = 0; i < parts.length - 1; i++) {
                    path.append(".").append(parts[i]);
                    if (ARRAY_FIELDS.contains(parts[i])) {
                        path.append("[*]");
                    }
                }
                path.append("[?(@.").append(lastPart).append(" ").append(operator).append(" '").append(value).append("')]");
                return path.toString();
            }
        }
    }

    private String escape(String value) {
        return value.replace("'", "\\'");
    }

    private void validate(String field, String operator) {

//        if (!field.startsWith("consumers")) {
//            throw new IllegalArgumentException("Only consumers filtering allowed");
//        }
        if (!Set.of("eq", "neq", "gt", "lt", "in").contains(operator)) {
            throw new IllegalArgumentException("Invalid operator");
        }

        if (field.length() > 100) {
            throw new IllegalArgumentException("Field too long");
        }
    }

}
