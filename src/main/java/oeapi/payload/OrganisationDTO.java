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
@JsonPropertyOrder({"organisationId", "primaryCode", "name", "shortName"})
public class OrganisationDTO extends PrimaryCode {

    @JsonProperty(value = "organisationId")
    private String organisationId = UUID.randomUUID().toString();

    @JsonProperty("organisationType")
    @ValidEnumYaml(yamlfile = "organisationType.yml")
    private String organisationType;

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
    
    // "parent" is serialized as the parent's organisationId; the full object
    // replaces it only when ?expand=parent is requested.
    @JsonProperty("parent")
    private String parentId;

    // Must be public: oeapiDTOMapper.toJSON() resolves expandable fields with
    // Class.getField(), which only sees public fields.
    @JsonIgnore
    @oeapiDTOExpandable
    public OrganisationDTO parent;

    // "children" is serialized as an array of child organisationIds; the full
    // objects are available only via ?expand=children.
    @JsonProperty("children")
    private List<String> childrenIds;

    // Must be public, same reason as parent above.
    // (@JsonManagedReference used to sit on this field; it became unpaired once
    //  parent's @JsonBackReference was disabled, which broke deserialization.)
    @JsonIgnore
    @oeapiDTOExpandable
    public List<OrganisationDTO> children;

    
    public OrganisationDTO() {
    }

    @JsonCreator
    public OrganisationDTO(String id) {
        this.organisationId = id;
    }

    /**
     * @return the organisationId
     */
    public String getOrganisationId() {
        return organisationId;
    }

    /**
     * @param organisationId the organisationId to set
     */
    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    /**
     * @return the organisationType
     */
    public String getOrganisationType() {
        return organisationType;
    }

    /**
     * @param organisationType the organisationType to set
     */
    public void setOrganisationType(String organisationType) {
        this.organisationType = organisationType;
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
     * @param parentId the parent organisationId to set
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;

        if (parentId == null) {
            this.parent = null;
        } else if (this.parent == null || !parentId.equals(this.parent.getOrganisationId())) {
            // Reference stub, used only to carry the id into the entity mapper.
            // OrganisationService.toEntity() replaces it with the real organisation,
            // or rejects the request when no such organisation exists.
            // Never overwrite an already mapped parent: on a read ModelMapper fills
            // the full object through setParent(), and rebuilding a stub here would
            // reduce ?expand=parent to a bare organisationId.
            this.parent = new OrganisationDTO(parentId);
        }
    }

    /**
     * @return the parent
     */
    public OrganisationDTO getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set. Keeps parentId in sync so the
     *               unexpanded response still carries the link.    
     */
    public void setParent(OrganisationDTO parent) {
        this.parent = parent;
        this.parentId = parent == null ? null : parent.getOrganisationId();
    }

    /**
     * @return the ids of the child organisations
     */
    public List<String> getChildrenIds() {
        return childrenIds;
    }

    /**
     * @param childrenIds the child organisationIds to set
     */
    public void setChildrenIds(List<String> childrenIds) {
        this.childrenIds = childrenIds;
    }

    /**
     * @return the children
     */
    public List<OrganisationDTO> getChildren() {
        return children;
    }

    /**
     * @param children the children to set. Keeps childrenIds in sync so the
     *                 unexpanded response carries the links.
     */
    public void setChildren(List<OrganisationDTO> children) {
        this.children = children;
        this.childrenIds = (children == null) ? null
                : children.stream()
                        .filter(c -> c != null && c.getOrganisationId() != null)
                        .map(OrganisationDTO::getOrganisationId)
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
