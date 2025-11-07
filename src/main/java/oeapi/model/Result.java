package oeapi.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 *
 * @author itziar
 */
public class Result implements Serializable {

    private String resultState;
    private String pass;
    private String comment;
    private String score;
    private LocalDate resultDate;
    private Ext ext;
    private StudyLoad studyLoad;
    private Integer weight;

    /**
     * @return the resultState
     */
    public String getResultState() {
        return resultState;
    }

    /**
     * @param resultState the resultState to set
     */
    public void setResultState(String resultState) {
        this.resultState = resultState;
    }

    /**
     * @return the pass
     */
    public String getPass() {
        return pass;
    }

    /**
     * @param pass the pass to set
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the score
     */
    public String getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(String score) {
        this.score = score;
    }

    /**
     * @return the resultDate
     */
    public LocalDate getResultDate() {
        return resultDate;
    }

    /**
     * @param resultDate the resultDate to set
     */
    public void setResultDate(LocalDate resultDate) {
        this.resultDate = resultDate;
    }

    /**
     * @return the ext
     */
    public Ext getExt() {
        return ext;
    }

    /**
     * @param ext the ext to set
     */
    public void setExt(Ext ext) {
        this.ext = ext;
    }

    /**
     * @return the studyLoad
     */
    public StudyLoad getStudyLoad() {
        return studyLoad;
    }

    /**
     * @param studyLoad the studyLoad to set
     */
    public void setStudyLoad(StudyLoad studyLoad) {
        this.studyLoad = studyLoad;
    }

    /**
     * @return the weight
     */
    public Integer getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(Integer weight) {
        this.weight = weight;
    }

}
