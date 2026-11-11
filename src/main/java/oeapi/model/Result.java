package oeapi.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * OEAPI v6 Result of an offering association.
 *
 * studyLoad is used by course and programme offering associations, weight by
 * learning component offering associations.
 *
 * @author itziar
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"state", "pass", "comment", "score", "rawScore", "maxRawScore", "final",
    "resultDateTime", "studyLoad", "weight", "assessorId", "ext"})
public class Result extends oeapiEquals implements Serializable {

    private String state;           // resultState: in_progress, postponed, completed, queued
    private String pass;            // passState: unknown, passed, failed
    private String comment;
    private String score;
    private Integer rawScore;
    private Integer maxRawScore;
    private Boolean finalResult;
    private OffsetDateTime resultDateTime;
    private StudyLoad studyLoad;
    private Integer weight;
    private String assessorId;
    private Ext ext;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public Integer getRawScore() {
        return rawScore;
    }

    public void setRawScore(Integer rawScore) {
        this.rawScore = rawScore;
    }

    public Integer getMaxRawScore() {
        return maxRawScore;
    }

    public void setMaxRawScore(Integer maxRawScore) {
        this.maxRawScore = maxRawScore;
    }

    // "final" is a Java keyword
    @com.fasterxml.jackson.annotation.JsonProperty("final")
    public Boolean getFinalResult() {
        return finalResult;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("final")
    public void setFinalResult(Boolean finalResult) {
        this.finalResult = finalResult;
    }

    public OffsetDateTime getResultDateTime() {
        return resultDateTime;
    }

    public void setResultDateTime(OffsetDateTime resultDateTime) {
        this.resultDateTime = resultDateTime;
    }

    public StudyLoad getStudyLoad() {
        return studyLoad;
    }

    public void setStudyLoad(StudyLoad studyLoad) {
        this.studyLoad = studyLoad;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getAssessorId() {
        return assessorId;
    }

    public void setAssessorId(String assessorId) {
        this.assessorId = assessorId;
    }

    public Ext getExt() {
        return ext;
    }

    public void setExt(Ext ext) {
        this.ext = ext;
    }
}
