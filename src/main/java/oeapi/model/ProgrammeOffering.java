package oeapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 *
 * @author itziar.urrutia
 */
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@DiscriminatorValue("ProgrammeOffering")
public class ProgrammeOffering extends Offering {

    @JsonProperty("programme")
    @ManyToOne
    @JoinColumn(name = "programme_id", nullable = true, unique = false)
    private Programme programme;

    @Override
    public String getOfferingType() {
        return "programme";  
    }    
        
    /**
     * @return the programme
     */
    public Programme getProgramme() {
        return programme;
    }

    /**
     * @param programme the programme to set
     */
    public void setProgramme(Programme programme) {
        this.programme = programme;
    }

}
