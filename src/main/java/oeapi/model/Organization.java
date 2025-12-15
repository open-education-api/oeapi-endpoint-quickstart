package oeapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListAddressConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.validation.ValidLanguageTypedString;

@Entity(name = "organization")
public class Organization extends PrimaryCode {

    @Id
    @Column(name = "organization_id")
    private String organizationId = UUID.randomUUID().toString();
    
    private Long organizationTypeId;
    
    private String shortName;
    
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListIdentifierEntryConverter.class)
    private List<oeapiIdentifierEntry> otherCodes;
    
    private String link;
    
    private String logo;
    
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    //@ValidItemYaml(yamlfile = "LanguageTypedString.yml")
    @ValidLanguageTypedString(message = "Null or Invalid language-typed string elements")
    private List<oeapiLanguageTypedString> name;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListAddressConverter.class)
    //@ValidAddresses(message = "Null or Invalid address string elements")
    private List<Address> addresses;

    private Organization parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JsonManagedReference("parentchildrenOrganization")
    private List<Organization> children;

    public Organization() {
    }

    @JsonCreator
    public Organization(String id) {
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
     * @return the organizationTypeId
     */
    public Long getOrganizationTypeId() {
        return organizationTypeId;
    }

    /**
     * @param organizationTypeId the organizationTypeId to set
     */
    public void setOrganizationTypeId(Long organizationTypeId) {
        this.organizationTypeId = organizationTypeId;
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
    public Organization getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(Organization parent) {
        this.parent = parent;
    }

    /**
     * @return the children
     */
    public List<Organization> getChildren() {
        return children;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(List<Organization> children) {
        this.children = children;
    }

}
