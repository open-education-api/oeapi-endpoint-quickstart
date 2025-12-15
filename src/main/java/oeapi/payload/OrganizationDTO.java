package oeapi.payload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Convert;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListAddressConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.model.Address;
import oeapi.model.PrimaryCode;
import oeapi.model.oeapiIdentifierEntry;
import oeapi.model.oeapiLanguageTypedString;
import oeapi.validation.ValidLanguageTypedString;
import oeapi.validation.ValidAddresses;
import oeapi.validation.ValidEnumYaml;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"organizationId", "primaryCode", "name", "shortName"})
public class OrganizationDTO extends PrimaryCode {

    @JsonProperty(value = "organizationId")
    private String organizationId = UUID.randomUUID().toString();

    @JsonProperty("organizationType")
    @ValidEnumYaml(yamlfile = "organizationType.yml")
    private String organizationType;

    @JsonProperty("shortName")
    private String shortName;

    @JsonProperty("otherCodes")
    @Convert(converter = oeapiUnitaListIdentifierEntryConverter.class)
    private List<oeapiIdentifierEntry> otherCodes;

    @JsonProperty("link")
    private String link;

    @JsonProperty("logo")
    private String logo;

    @JsonProperty("name")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    //@ValidItemYaml(yamlfile = "LanguageTypedString.yml")
    @ValidLanguageTypedString(message = "Null or Invalid language-typed string elements")
    private List<oeapiLanguageTypedString> name;

    @JsonProperty("addresses")
    @Convert(converter = oeapiUnitaListAddressConverter.class)
    @ValidAddresses(message = "Null or Invalid address string elements")
    private List<Address> addresses;

    @JsonProperty("parent")
    /*
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "parent_id", nullable = true, referencedColumnName = "organization_id")
    @JsonBackReference("parentchildrenOrganization") */
    private OrganizationDTO parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JsonManagedReference("parentchildrenOrganization")
    private List<OrganizationDTO> children;

    /*

    @JsonManagedReference("groupOrganization")
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Group> groups;

    @JsonManagedReference("offeringOrganization")
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Offering> offerings;

    @JsonManagedReference("courseOrganization")
    //@JsonBackReference // This side will not be serialized
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Course> courses;

    @JsonManagedReference("programOrganization")
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Program> programs;

    @JsonManagedReference("componentOrganization")
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Component> components;

    @JsonManagedReference("educationSpecificationOrganization")
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EducationSpecification> educationSpecifications;

     */
    public OrganizationDTO() {
    }

    @JsonCreator
    public OrganizationDTO(String id) {
        this.organizationId = id;
    }

    /**
     * @return the organizationId
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * @param organizationId the organizationId to set
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * @return the organizationType
     */
    public String getOrganizationType() {
        return organizationType;
    }

    /**
     * @param organizationType the organizationType to set
     */
    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
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
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link the link to set
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * @return the logo
     */
    public String getLogo() {
        return logo;
    }

    /**
     * @param logo the logo to set
     */
    public void setLogo(String logo) {
        this.logo = logo;
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
     * @return the addresses
     */
    public List<Address> getAddresses() {
        return addresses;
    }

    /**
     * @param addresses the addresses to set
     */
    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    /**
     * @return the parent
     */
    public OrganizationDTO getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(OrganizationDTO parent) {
        this.parent = parent;
    }

    /**
     * @return the children
     */
    public List<OrganizationDTO> getChildren() {
        return children;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(List<OrganizationDTO> children) {
        this.children = children;
    }

//    /**
//     * @return the groups
//     */
//    public List<Group> getGroups() {
//        return groups;
//    }
//
//    /**
//     * @param groups the groups to set
//     */
//    public void setGroups(List<Group> groups) {
//        this.groups = groups;
//    }
//
//    /**
//     * @return the offerings
//     */
//    public List<Offering> getOfferings() {
//        return offerings;
//    }
//
//    /**
//     * @param offerings the offerings to set
//     */
//    public void setOfferings(List<Offering> offerings) {
//        this.offerings = offerings;
//    }
//
//    /**
//     * @return the courses
//     */
//    public List<Course> getCourses() {
//        return courses;
//    }
//
//    /**
//     * @param courses the courses to set
//     */
//    public void setCourses(List<Course> courses) {
//        this.courses = courses;
//    }
//
//    /**
//     * @return the programs
//     */
//    public List<Program> getPrograms() {
//        return programs;
//    }
//
//    /**
//     * @param programs the programs to set
//     */
//    public void setPrograms(List<Program> programs) {
//        this.programs = programs;
//    }
//
//    /**
//     * @return the components
//     */
//    public List<Component> getComponents() {
//        return components;
//    }
//
//    /**
//     * @param components the components to set
//     */
//    public void setComponents(List<Component> components) {
//        this.components = components;
//    }
//
//    /**
//     * @return the educationSpecifications
//     */
//    public List<EducationSpecification> getEducationSpecifications() {
//        return educationSpecifications;
//    }
//
//    /**
//     * @param educationSpecifications the educationSpecifications to set
//     */
//    public void setEducationSpecifications(List<EducationSpecification> educationSpecifications) {
//        this.educationSpecifications = educationSpecifications;
//    }
}
