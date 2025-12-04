/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import oeapi.converter.oeapiUnitaExtConverter;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.validation.ValidEnumYaml;

/**
 *
 * @author itziar
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(name = "news_feed")
public class NewsFeed extends PrimaryCode {

    @Id
    @Column(name = "news_feed_id")
    @JsonProperty(value = "newsFeedId")
    private String newsFeedId = UUID.randomUUID().toString();

    @ValidEnumYaml(yamlfile = "newsFeedType.yml")
    private String newsFeedType;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    private List<oeapiLanguageTypedString> name;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListConsumerConverter.class)
    private List<Consumer> consumers;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaExtConverter.class)
    private Ext ext;

    /**
     * @return the newsFeedId
     */
    public String getNewsFeedId() {
        return newsFeedId;
    }

    /**
     * @param newsFeedId the newsFeedId to set
     */
    public void setNewsFeedId(String newsFeedId) {
        this.newsFeedId = newsFeedId;
    }

    /**
     * @return the newsFeedType
     */
    public String getNewsFeedType() {
        return newsFeedType;
    }

    /**
     * @param newsFeedType the newsFeedType to set
     */
    public void setNewsFeedType(String newsFeedType) {
        this.newsFeedType = newsFeedType;
    }

    /**
     * @return the name
     */
    public List<oeapiLanguageTypedString> getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(List<oeapiLanguageTypedString> name) {
        this.name = name;
    }

    /**
     * @return the consumers
     */
    public List<Consumer> getConsumers() {
        return consumers;
    }

    /**
     * @param consumers the consumers to set
     */
    public void setConsumers(List<Consumer> consumers) {
        this.consumers = consumers;
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

}
