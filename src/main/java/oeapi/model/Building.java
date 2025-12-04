package oeapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import oeapi.converter.oeapiAddressConverter;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.validation.ValidLanguageTypedString;

/**
 *
 * @author itziar
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(name = "building")
public class Building extends PrimaryCode {

    @Id
    @Column(name = "building_id")
    @JsonProperty(value = "buildingId", access = JsonProperty.Access.WRITE_ONLY)
    private String buildingId = UUID.randomUUID().toString();

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiAddressConverter.class)
    private Address address;

//    @Valid
//    @Embedded
//    @JsonUnwrapped
//    private ooapiExtInfo extInfo;
    @JsonProperty("otherCodes")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListIdentifierEntryConverter.class)
    private List<oeapiIdentifierEntry> otherCodes;

    @JsonProperty("ext")
    @Column(columnDefinition = "text")
    private Ext ext;

    @JsonProperty("abbreviation")
    private String abbreviation;

    @JsonProperty("name")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(message = "Null or Invalid language-typed string elements")
    private List<oeapiLanguageTypedString> name;

    @JsonProperty("description")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(isNull = true, message = "Null or Invalid language-typed string elements")
    private List<oeapiLanguageTypedString> description;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListConsumerConverter.class)
    private List<Consumer> consumers;

    /**
     * @return the buildingId
     */
    public String getBuildingId() {
        return buildingId;
    }

    /**
     * @param buildingId the buildingId to set
     */
    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    /**
     * @return the address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(Address address) {
        this.address = address;
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
     * @return the description
     */
    public List<oeapiLanguageTypedString> getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(List<oeapiLanguageTypedString> description) {
        this.description = description;
    }

    /**
     * @return the abbreviation
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * @param abbreviation the abbreviation to set
     */
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * @return the otherCodes
     */
    public List<oeapiIdentifierEntry> getOtherCodes() {
        return otherCodes;
    }

    /**
     * @param otherCodes the otherCodes to set
     */
    public void setOtherCodes(List<oeapiIdentifierEntry> otherCodes) {
        this.otherCodes = otherCodes;
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

}
