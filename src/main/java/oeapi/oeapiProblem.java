package oeapi;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * OEAPI v6 error body: RFC 7807 Problem Details, served as application/problem+json.
 *
 * {@code type}, {@code title} and {@code status} are required by the
 * specification; {@code detail} and {@code instance} are optional. Extra members
 * (such as {@code requestedVersion} on a 406) go in {@link #getExtensions()}.
 *
 * @author Carlos Alonso <losalo@unavarra.es>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "title", "status", "detail", "instance"})
public class oeapiProblem implements Serializable {

    /** Default problem type, as defined by RFC 7807 section 4.2. */
    public static final String DEFAULT_TYPE = "about:blank";

    private String type = DEFAULT_TYPE;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private final Map<String, Object> extensions = new LinkedHashMap<>();

    public oeapiProblem() {
    }

    public oeapiProblem(int status, String title, String detail) {
        this.status = status;
        this.title = title;
        this.detail = detail;
    }

    public oeapiProblem(int status, String title) {
        this(status, title, null);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public oeapiProblem with(String name, Object value) {
        extensions.put(name, value);
        return this;
    }
}
