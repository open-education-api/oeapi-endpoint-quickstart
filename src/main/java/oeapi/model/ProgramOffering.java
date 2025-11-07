package oeapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author itziar.urrutia
 */
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@DiscriminatorValue("ProgramOffering")
public class ProgramOffering extends Offering {

    @JsonProperty("program")
    @ManyToOne
    @JoinColumn(name = "program_id", nullable = true, unique = false)
    private Program program;

    @Override
    public String getOfferingType() {
        return "program";  
    }    
        
    /**
     * @return the program
     */
    public Program getProgram() {
        return program;
    }

    /**
     * @param program the program to set
     */
    public void setProgram(Program program) {
        this.program = program;
    }

}
