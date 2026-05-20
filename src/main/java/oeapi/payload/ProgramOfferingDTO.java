/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import oeapi.model.Program;

/**
 *
 * @author michiel.de.mare
 */
public class ProgramOfferingDTO extends OfferingDTO {

    @JsonProperty("program")
    private String programJSON;

    @JsonIgnore
    private Program program;


    /**
     * @return the programJSON
     */
    public String getProgramJSON() {
        return (program == null) ? null : program.getProgramId();
    }

    /**
     * @param programJSON the programJSON to set
     */
    public void setProgramJSON(String programJSON) {
        this.programJSON = programJSON;
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

    @Override
    public String getOfferingType() {
        return "program";
    }

    @Override
    public String getTeachingLanguage() {

        if (super.getTeachingLanguage() == null) {
            return (program == null) ? null : program.getTeachingLanguage();
        }
        return super.getTeachingLanguage();

    }

}
