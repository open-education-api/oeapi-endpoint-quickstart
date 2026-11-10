package oeapi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OEAPI v6 Service document, returned by {@code GET /}.
 *
 * {@code contactEmail}, {@code specification}, {@code documentation} and
 * {@code supportedConsumers} come from the {@code ooapi.metadata.*}
 * properties; {@code supportedOperations} and {@code supportedExpands} are
 * filled in by the controller from the request mappings.
 */
@Component
@ConfigurationProperties(prefix = "ooapi.metadata")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"contactEmail", "specification", "documentation", "supportedConsumers",
                    "supportedOperations", "supportedExpands", "ext"})
public class Metadata {

    @JsonProperty("contactEmail")
    private String contactEmail;
    @JsonProperty("specification")
    private String specification;
    @JsonProperty("documentation")
    private String documentation;
    @JsonProperty("supportedConsumers")
    private List<SupportedConsumer> supportedConsumers = new ArrayList<>();
    @JsonProperty("supportedOperations")
    private List<SupportedOperation> supportedOperations;
    @JsonProperty("supportedExpands")
    private List<SupportedExpand> supportedExpands;
    @JsonProperty("ext")
    private Map<String, Object> ext;

    /** A consumer the endpoint knows, with the version of that consumer definition. */
    public static class SupportedConsumer {
        private String consumerKey;
        private String version;

        public SupportedConsumer() {
        }

        public SupportedConsumer(String consumerKey, String version) {
            this.consumerKey = consumerKey;
            this.version = version;
        }

        public String getConsumerKey() {
            return consumerKey;
        }

        public void setConsumerKey(String consumerKey) {
            this.consumerKey = consumerKey;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    /** One path of the specification and the verbs implemented on it. */
    @JsonPropertyOrder({"verbs", "path"})
    public static class SupportedOperation {
        private List<String> verbs;
        private String path;

        public SupportedOperation(List<String> verbs, String path) {
            this.verbs = verbs;
            this.path = path;
        }

        public List<String> getVerbs() {
            return verbs;
        }

        public String getPath() {
            return path;
        }
    }

    /** One path and the objects that can be expanded on it. */
    @JsonPropertyOrder({"expandableObjects", "path"})
    public static class SupportedExpand {
        private List<String> expandableObjects;
        private String path;

        public SupportedExpand(List<String> expandableObjects, String path) {
            this.expandableObjects = expandableObjects;
            this.path = path;
        }

        public List<String> getExpandableObjects() {
            return expandableObjects;
        }

        public String getPath() {
            return path;
        }
    }

    // Getters and Setters
    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public List<SupportedConsumer> getSupportedConsumers() {
        return supportedConsumers;
    }

    public void setSupportedConsumers(List<SupportedConsumer> supportedConsumers) {
        this.supportedConsumers = supportedConsumers;
    }

    public List<SupportedOperation> getSupportedOperations() {
        return supportedOperations;
    }

    public void setSupportedOperations(List<SupportedOperation> supportedOperations) {
        this.supportedOperations = supportedOperations;
    }

    public List<SupportedExpand> getSupportedExpands() {
        return supportedExpands;
    }

    public void setSupportedExpands(List<SupportedExpand> supportedExpands) {
        this.supportedExpands = supportedExpands;
    }

    public Map<String, Object> getExt() {
        return ext;
    }

    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }
}
