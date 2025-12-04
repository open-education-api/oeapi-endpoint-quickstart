package oeapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import oeapi.validation.ValidEnumYaml;

/**
 * The type Study load.
 *
 * @author Carlos Alonso - losalo@unavarra.es
 */
@Valid
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
public class StudyLoad implements Serializable {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ValidEnumYaml(yamlfile = "studyLoadType.yml")
    private String studyLoadUnit;
    private float value;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the studyLoadUnit
     */
    public String getStudyLoadUnit() {
        return studyLoadUnit;
    }

    /**
     * @param studyLoadUnit the studyLoadUnit to set
     */
    public void setStudyLoadUnit(String studyLoadUnit) {
        this.studyLoadUnit = studyLoadUnit;
    }

    /**
     * @return the value
     */
    public float getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(float value) {
        this.value = value;
    }

}
