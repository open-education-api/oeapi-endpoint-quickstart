package oeapi;

import java.io.Serializable;

/**
 *
 * @author Carlos Alonso <losalo@unavarra.es>
 */
public class oeapiError implements Serializable{

    private String status;
    private String title;
    private String detail;

    // Constructors
    public oeapiError(String status, String title, String detail) {
        this.status = status;
        this.title  = title;
        this.detail = detail;
    }

    public oeapiError(String status, String title) {
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
}    
    

