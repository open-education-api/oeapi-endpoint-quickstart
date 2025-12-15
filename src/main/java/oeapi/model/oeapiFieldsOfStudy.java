/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 *
 * @author itziar.urrutia
 */
@Entity
public class oeapiFieldsOfStudy {

    @Id
    private String fieldsOfStudyId;

    private String txtEn;
    private String txtFr;

    @Column(name = "levelFieldOfStudy")
    private Long level;

    private String parent;

    /**
     * @return the fieldsOfStudyId
     */
    public String getFieldsOfStudyId() {
        return fieldsOfStudyId;
    }

    /**
     * @param fieldsOfStudyId the fieldsOfStudyId to set
     */
    public void setFieldsOfStudyId(String fieldsOfStudyId) {
        this.fieldsOfStudyId = fieldsOfStudyId;
    }

    /**
     * @return the txtEn
     */
    public String getTxtEn() {
        return txtEn;
    }

    /**
     * @param txtEn the txtEn to set
     */
    public void setTxtEn(String txtEn) {
        this.txtEn = txtEn;
    }

    /**
     * @return the txtFr
     */
    public String getTxtFr() {
        return txtFr;
    }

    /**
     * @param txtFr the txtFr to set
     */
    public void setTxtFr(String txtFr) {
        this.txtFr = txtFr;
    }

    /**
     * @return the level
     */
    public Long getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(Long level) {
        this.level = level;
    }

    /**
     * @return the parent
     */
    public String getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(String parent) {
        this.parent = parent;
    }
}
