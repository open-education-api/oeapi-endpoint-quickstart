package oeapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import java.util.UUID;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.validation.ValidLanguageTypedString;
import oeapi.validation.ValidObjectYaml;

@Entity(name = "ooapiGroup")
public class Group extends PrimaryCode {

    @Id
    @Column(name = "group_id")
    @JsonProperty("groupId")
    private String groupId = UUID.randomUUID().toString();

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer personCount;

    @JsonProperty("otherCodes")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListIdentifierEntryConverter.class)
    private List<oeapiIdentifierEntry> otherCodes;

    @JsonProperty("ext")
    @Column(columnDefinition = "text")
    private Ext ext;

    @ValidObjectYaml(yamlfile = "enum/groupType.yml")
    private String groupType;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = true)
    @JsonBackReference("groupOrganization")
    private Organization organization;

    @ManyToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER // fetches the child entities along with parent (not when required)
    )
    @JoinTable(
            name = "person_group",
            joinColumns = @JoinColumn(
                    name = "group_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "person_id"
            )
    )
    @JsonBackReference("personsGroups")
    private List<Person> members;

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

    public void addMember(Person user) {
        if (getMembers() == null) {
            setMembers(new ArrayList<>());
        }

        getMembers().add(user);
    }

    /**
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * @return the startDate
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the personCount
     */
    public Integer getPersonCount() {
        return personCount;
    }

    /**
     * @param personCount the personCount to set
     */
    public void setPersonCount(Integer personCount) {
        this.personCount = personCount;
    }

    /**
     * @return the extInfo
     */
//    public ooapiExtInfo getExtInfo() {
//        return extInfo;
//    }
    /**
     * @param extInfo the extInfo to set
     */
//    public void setExtInfo(ooapiExtInfo extInfo) {
//        this.extInfo = extInfo;
//    }
    /**
     * @return the groupType
     */
    public String getGroupType() {
        return groupType;
    }

    /**
     * @param groupType the groupType to set
     */
    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    /**
     * @return the organization
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * @param organization the organization to set
     */
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * @return the members
     */
    public List<Person> getMembers() {
        return members;
    }

    /**
     * @param members the members to set
     */
    public void setMembers(List<Person> members) {
        this.members = members;
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
