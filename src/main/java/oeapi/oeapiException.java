package oeapi;

import java.io.Serializable;
import org.springframework.http.HttpStatus;

/**
 *
 * @author Carlos Alonso <losalo@unavarra.es>
 */
public class oeapiException extends RuntimeException implements Serializable {

    private HttpStatus status;
    private String title;
    private String detail;

    // Basic handler, just in case
    public oeapiException(String message) {
        super(message);
    }

    // Following OOAPI Spec
    public oeapiException(HttpStatus status, String title, String detail) {
        super(title);  // default RuntimeException message
        this.status = status;
        this.title = title;
        this.detail = detail;
    }

    public oeapiException(HttpStatus status, String title) {
        super(title);  // default RuntimeException message
        this.status = status;
        this.title = title;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }
}
