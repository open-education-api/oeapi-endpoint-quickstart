/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import oeapi.model.Programme;

/**
 *
 * @author michiel.de.mare
 */
public class ProgrammeOfferingDTO extends OfferingDTO {

    @JsonProperty("programme")
    private String programmeJSON;

    @JsonIgnore
    private Programme programme;


    /**
     * @return the programmeJSON
     */
    public String getProgrammeJSON() {
        return (programme == null) ? null : programme.getProgrammeId();
    }

    /**
     * @param programmeJSON the programmeJSON to set
     */
    public void setProgrammeJSON(String programmeJSON) {
        this.programmeJSON = programmeJSON;
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

    @Override
    public String getOfferingType() {
        return "programme";
    }

    @Override
    public String getTeachingLanguage() {

        if (super.getTeachingLanguage() == null) {
            return (programme == null) ? null : programme.getTeachingLanguage();
        }
        return super.getTeachingLanguage();

    }

}
