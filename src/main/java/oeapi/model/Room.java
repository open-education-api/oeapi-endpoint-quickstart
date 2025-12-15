package oeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import oeapi.converter.oeapiGeolocationConverter;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.validation.ValidEnumYaml;
import oeapi.validation.ValidLanguageTypedString;

/**
 *
 * @author itziar
 */
@Entity(name = "room")
public class Room extends PrimaryCode {

    @Id
    @Column(name = "room_id")
    @JsonProperty(value = "roomId", access = JsonProperty.Access.WRITE_ONLY)
    private String roomId = UUID.randomUUID().toString();

    @ValidEnumYaml(yamlfile = "roomType.yml")
    private String roomType;
    private Integer totalSeats;
    private Integer availableSeats;
    private String floor;
    private String wing;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiGeolocationConverter.class)
    private Geolocation geolocation;

    @JsonProperty("building")
    @ManyToOne
    @JoinColumn(name = "building_id", nullable = true)
    //@JsonBackReference
    private Building building;

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
     * @return the roomId
     */
    public String getRoomId() {
        return roomId;
    }

    /**
     * @param roomId the roomId to set
     */
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    /**
     * @return the roomType
     */
    public String getRoomType() {
        return roomType;
    }

    /**
     * @param roomType the roomType to set
     */
    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    /**
     * @return the totalSeats
     */
    public Integer getTotalSeats() {
        return totalSeats;
    }

    /**
     * @param totalSeats the totalSeats to set
     */
    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    /**
     * @return the availableSeats
     */
    public Integer getAvailableSeats() {
        return availableSeats;
    }

    /**
     * @param availableSeats the availableSeats to set
     */
    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    /**
     * @return the floor
     */
    public String getFloor() {
        return floor;
    }

    /**
     * @param floor the floor to set
     */
    public void setFloor(String floor) {
        this.floor = floor;
    }

    /**
     * @return the wing
     */
    public String getWing() {
        return wing;
    }

    /**
     * @param wing the wing to set
     */
    public void setWing(String wing) {
        this.wing = wing;
    }

    /**
     * @return the geolocation
     */
    public Geolocation getGeolocation() {
        return geolocation;
    }

    /**
     * @param geolocation the geolocation to set
     */
    public void setGeolocation(Geolocation geolocation) {
        this.geolocation = geolocation;
    }

    /**
     * @return the building
     */
    public Building getBuilding() {
        return building;
    }

    /**
     * @param building the building to set
     */
    public void setBuilding(Building building) {
        this.building = building;
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

}
