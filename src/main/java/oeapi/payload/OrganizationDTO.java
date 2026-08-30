package oeapi.payload;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Convert;
import jakarta.persistence.JoinColumn;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListAddressConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.model.Address;
import oeapi.model.Consumer;
import oeapi.model.PrimaryCode;
import oeapi.model.oeapiIdentifierEntry;
import oeapi.model.oeapiLanguageTypedString;
import oeapi.validation.ValidAddresses;
import oeapi.validation.ValidEnumYaml;
import oeapi.validation.ValidLanguageTypedString;

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
    
    @JsonProperty("description")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(message = "Null or Invalid language-typed string elements")    
    private List<oeapiLanguageTypedString> description;    

    @JsonProperty("addresses")
    @Convert(converter = oeapiUnitaListAddressConverter.class)
    @ValidAddresses(message = "Null or Invalid address string elements")
    private List<Address> addresses;
    
    // Same pattern as ProgramDTO: the id is always serialized as "parent",
    // the full object replaces it only when ?expand=parent is requested.
    // It must be public - oeapiDTOMapper.toJSON() looks it up with
    // Class.getField(), which only sees public fields.    
    
    @JsonProperty("parent")
    private String parentId;

    @JsonIgnore
    @oeapiDTOExpandable        
    @JoinColumn(name = "parent_id")
//    @JsonBackReference    
    public OrganizationDTO parent;

    // Same pattern as parent above: "children" is serialized as an array of
    // organizationIds; the full objects are available only via ?expand=children.
    // The object list must be public - oeapiDTOMapper.toJSON() resolves
    // expandable fields with Class.getField().
    // (@JsonManagedReference used to sit on this field; it became unpaired when
    //  parent's @JsonBackReference was disabled, which broke deserialization -> 415.)
    @JsonProperty("children")
    private List<String> childrenIds;

    @JsonIgnore
    @oeapiDTOExpandable
    public List<OrganizationDTO> children;

    
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
     * @return the parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parent organizationId to set
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
        this.parent = parentId == null ? null : new OrganizationDTO(parentId); 
    }

    /**
     * @return the parent
     */
    public OrganizationDTO getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set. Keeps parentId in sync so the
     *               unexpanded response still carries the link.    
     */
    public void setParent(OrganizationDTO parent) {
        this.parent = parent;
        this.parentId = parent == null ? null : parent.getOrganizationId();
    }

    /**
     * @return the ids of the child organizations
     */
    public List<String> getChildrenIds() {
        return childrenIds;
    }

    /**
     * @param childrenIds the child organizationIds to set
     */
    public void setChildrenIds(List<String> childrenIds) {
        this.childrenIds = childrenIds;
    }

    /**
     * @return the children
     */
    public List<OrganizationDTO> getChildren() {
        return children;
    }

    /**
     * @param children the children to set. Keeps childrenIds in sync so the
     *                 unexpanded response carries the links.
     */
    public void setChildren(List<OrganizationDTO> children) {
        this.children = children;
        this.childrenIds = (children == null) ? null
                : children.stream()
                        .filter(c -> c != null && c.getOrganizationId() != null)
                        .map(OrganizationDTO::getOrganizationId)
                        .collect(Collectors.toList());
    }

    // consumers

    @JsonProperty("consumers")
    private List<Consumer> consumers;

    public List<Consumer> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<Consumer> consumers) {
        this.consumers = consumers;
    }
}
