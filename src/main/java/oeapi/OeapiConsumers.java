package oeapi;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import oeapi.model.Consumer;

/**
 * OEAPI v6 consumer handling.
 *
 * <p>The endpoint stores, per resource, a list of consumer objects (one per
 * consumerKey: rio, eduxchange...). On the wire v6 has a single
 * {@code consumer} object, chosen by the {@code consumer} query parameter (or
 * the consumer of the Accept header):</p>
 * <ul>
 * <li>{@code ?consumer=rio}: every object that has consumer data gets
 * {@code "consumer": {...}} for rio (when present);</li>
 * <li>no consumer requested: no consumer data is published;</li>
 * <li>{@code ?consumer=all}: implementation extension, the stored
 * {@code "consumers"} list is published as is (used by the dashboard).</li>
 * </ul>
 * <p>On input a single {@code "consumer"} object is merged into the stored
 * list (replacing the entry with the same consumerKey); a {@code "consumers"}
 * list (extension) replaces the stored list.</p>
 */
public final class OeapiConsumers {

    public static final String ALL = "all";

    /** Request attribute set when the body carried a single v6 "consumer". */
    public static final String MERGE_ATTRIBUTE = OeapiConsumers.class.getName() + ".merge";

    private OeapiConsumers() {
    }

    // ---- responses -------------------------------------------------------------

    /** Applies the consumer rule to every object of a response tree. */
    public static void publish(JsonNode node, String requested) {
        if (node instanceof ObjectNode object) {
            JsonNode consumers = object.get("consumers");
            if (consumers != null && consumers.isArray() && !ALL.equals(requested)) {
                object.remove("consumers");
                if (requested != null) {
                    for (JsonNode consumer : consumers) {
                        JsonNode key = consumer.get("consumerKey");
                        if (key != null && requested.equals(key.asString())) {
                            object.set("consumer", consumer);
                            break;
                        }
                    }
                }
            }
            for (Iterator<Map.Entry<String, JsonNode>> it = object.properties().iterator(); it.hasNext();) {
                publish(it.next().getValue(), requested);
            }
        } else if (node instanceof ArrayNode array) {
            for (JsonNode item : array) {
                publish(item, requested);
            }
        }
    }

    // ---- requests --------------------------------------------------------------

    /**
     * Turns a top-level v6 "consumer" into the stored "consumers" form. Returns
     * true when it did, so the caller can ask for a merge with the stored list.
     */
    public static boolean acceptSingle(JsonNode body) {
        if (body instanceof ObjectNode object && object.has("consumer") && !object.has("consumers")) {
            JsonNode consumer = object.remove("consumer");
            if (consumer != null && consumer.isObject()) {
                object.putArray("consumers").add(consumer);
                return true;
            }
        }
        return false;
    }

    /** True when the current request sent a single "consumer" to be merged. */
    public static boolean mergeRequested() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes != null
                && Boolean.TRUE.equals(attributes.getAttribute(MERGE_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST));
    }

    /**
     * Before an update: when the request carried one "consumer", keep the stored
     * consumers with other keys. Works on any entity with getConsumers()/setConsumers().
     */
    @SuppressWarnings("unchecked")
    public static void mergeWithStored(Object incoming, Object stored) {
        if (incoming == null || stored == null || !mergeRequested()) {
            return;
        }
        try {
            Method getter = incoming.getClass().getMethod("getConsumers");
            Method setter = incoming.getClass().getMethod("setConsumers", List.class);
            List<Consumer> sent = (List<Consumer>) getter.invoke(incoming);
            List<Consumer> kept = (List<Consumer>) stored.getClass().getMethod("getConsumers").invoke(stored);
            List<Consumer> merged = new ArrayList<>();
            if (kept != null) {
                for (Consumer consumer : kept) {
                    boolean replaced = sent != null && sent.stream()
                            .anyMatch(s -> s.getConsumerKey() != null && s.getConsumerKey().equals(consumer.getConsumerKey()));
                    if (!replaced) {
                        merged.add(consumer);
                    }
                }
            }
            if (sent != null) {
                merged.addAll(sent);
            }
            setter.invoke(incoming, merged);
        } catch (ReflectiveOperationException | ClassCastException ex) {
            // the entity has no consumers: nothing to merge
        }
    }
}
