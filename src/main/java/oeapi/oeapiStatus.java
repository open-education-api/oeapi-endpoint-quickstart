package oeapi;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * @author Carlos Alonso <losalo@unavarra.es>
 */
public class oeapiStatus {

    private String status;
    private String title;
    private String detail;

    // Constructors
    public oeapiStatus(String status, String title, String detail) {
        this.status = status;
        this.title = title;
        this.detail = detail;

    }

    public oeapiStatus(String status, String title) {
        this.status = status;
        this.title = title;
        this.detail = null; // Optional

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {

        ObjectNode jsonStatus = JsonNodeFactory.instance.objectNode();
        jsonStatus.put("status", this.status);
        jsonStatus.put("title", this.title);
        jsonStatus.put("detail", this.detail);

        return jsonStatus.asText();
    }

}
