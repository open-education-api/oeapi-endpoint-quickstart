package oeapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import oeapi.converter.oeapiAddressConverter;
import oeapi.converter.oeapiExtConverter;
import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.validation.ValidEnumYaml;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(name = "Person")
public class Person extends PrimaryCode {

    @Id
    @Column(name = "person_id")
    @JsonProperty(value = "personId")
    //@JsonProperty(value = "personId", access = JsonProperty.Access.WRITE_ONLY)
    private String personId = UUID.randomUUID().toString();

    // OEAPI v6 gender (m, f, x, o, u, n). The v5 build stored an ordinal in
    // column "gender"; the v6 value lives in "gender_code" (see the SQL migration).
    @Column(name = "gender_code")
    @ValidEnumYaml(yamlfile = "gender.yml")
    private String gender;

    private String givenName;
    private String initials;
    private String surname;
    private String displayName;
    private String email;
    private String secondaryEmail;
    private String telephoneNumber;
    private String mobileNumber;
    private String cityOfBirth;
    private LocalDate dateOfBirth;
    private String countryOfBirth;
    private String nationality;
    private String photoSocial;
    private String photoOfficial;
    private String titlePrefix;
    private String titleSuffix;
    private String office;
    private String ICEName;
    private String ICEPhoneNumber;

    @ValidEnumYaml(yamlfile = "ICERelationType.yml")
    private String ICERelation;

    //@ValidEnumYaml(yamlfile = "personAffiliations.yml")
    @ElementCollection
    private List<String> affiliations;

    private LocalDate dateOfNationality;
    private boolean activeEnrolment;

    // ---- OEAPI v6 additions ----------------------------------------------------

    private String preferredName;
    private String alternateName;
    private String surnamePrefix;
    private String idCheckName;

    // RFC 5646 language tags, stored comma separated
    @JsonIgnore
    @Column(name = "language_of_choice")
    private String languageOfChoice;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiAddressConverter.class)
    private Address address;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListIdentifierEntryConverter.class)
    private List<oeapiIdentifierEntry> otherCodes;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiExtConverter.class)
    private Ext ext;

    @JsonProperty("consumers")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListConsumerConverter.class)
    private List<Consumer> consumers;

    @JsonCreator
    public Person(String id) {
        this.personId = id;
    }

    public Person() {

    }


    /**
     * @return the personId
     */
    public String getPersonId() {
        return personId;
    }

    /**
     * @param personId the personId to set
     */
    public void setPersonId(String personId) {
        this.personId = personId;
    }

    /**
     * @return the givenName
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * @param givenName the givenName to set
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     * @return the initials
     */
    public String getInitials() {
        return initials;
    }

    /**
     * @param initials the initials to set
     */
    public void setInitials(String initials) {
        this.initials = initials;
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the secondaryEmail
     */
    public String getSecondaryEmail() {
        return secondaryEmail;
    }

    /**
     * @param secondaryEmail the secondaryEmail to set
     */
    public void setSecondaryEmail(String secondaryEmail) {
        this.secondaryEmail = secondaryEmail;
    }

    /**
     * @return the telephoneNumber
     */
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    /**
     * @param telephoneNumber the telephoneNumber to set
     */
    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    /**
     * @return the mobileNumber
     */
    public String getMobileNumber() {
        return mobileNumber;
    }

    /**
     * @param mobileNumber the mobileNumber to set
     */
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    /**
     * @return the cityOfBirth
     */
    public String getCityOfBirth() {
        return cityOfBirth;
    }

    /**
     * @param cityOfBirth the cityOfBirth to set
     */
    public void setCityOfBirth(String cityOfBirth) {
        this.cityOfBirth = cityOfBirth;
    }

    /**
     * @return the dateOfBirth
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth the dateOfBirth to set
     */
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * @return the countryOfBirth
     */
    @JsonIgnore
    public String getCountryOfBirth() {
        return countryOfBirth;
    }

    /**
     * @param countryOfBirth the countryOfBirth to set
     */
    @JsonIgnore
    public void setCountryOfBirth(String countryOfBirth) {
        this.countryOfBirth = countryOfBirth;
    }

    /**
     * @return the nationality
     */
    @JsonIgnore
    public String getNationality() {
        return nationality;
    }

    /**
     * @param nationality the nationality to set
     */
    @JsonIgnore
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    /**
     * @return the photoSocial
     */
    public String getPhotoSocial() {
        return photoSocial;
    }

    /**
     * @param photoSocial the photoSocial to set
     */
    public void setPhotoSocial(String photoSocial) {
        this.photoSocial = photoSocial;
    }

    /**
     * @return the photoOfficial
     */
    public String getPhotoOfficial() {
        return photoOfficial;
    }

    /**
     * @param photoOfficial the photoOfficial to set
     */
    public void setPhotoOfficial(String photoOfficial) {
        this.photoOfficial = photoOfficial;
    }

    /**
     * @return the titlePrefix
     */
    public String getTitlePrefix() {
        return titlePrefix;
    }

    /**
     * @param titlePrefix the titlePrefix to set
     */
    public void setTitlePrefix(String titlePrefix) {
        this.titlePrefix = titlePrefix;
    }

    /**
     * @return the titleSuffix
     */
    public String getTitleSuffix() {
        return titleSuffix;
    }

    /**
     * @param titleSuffix the titleSuffix to set
     */
    public void setTitleSuffix(String titleSuffix) {
        this.titleSuffix = titleSuffix;
    }

    /**
     * @return the office
     */
    public String getOffice() {
        return office;
    }

    /**
     * @param office the office to set
     */
    public void setOffice(String office) {
        this.office = office;
    }

    /**
     * @return the ICEName
     */
    public String getICEName() {
        return ICEName;
    }

    /**
     * @param ICEName the ICEName to set
     */
    public void setICEName(String ICEName) {
        this.ICEName = ICEName;
    }

    /**
     * @return the ICEPhoneNumber
     */
    public String getICEPhoneNumber() {
        return ICEPhoneNumber;
    }

    /**
     * @param ICEPhoneNumber the ICEPhoneNumber to set
     */
    public void setICEPhoneNumber(String ICEPhoneNumber) {
        this.ICEPhoneNumber = ICEPhoneNumber;
    }

    /**
     * @return the ICERelation
     */
    public String getICERelation() {
        return ICERelation;
    }

    /**
     * @param ICERelation the ICERelation to set
     */
    public void setICERelation(String ICERelation) {
        this.ICERelation = ICERelation;
    }

    /**
     * @return the affiliations
     */
    public List<String> getAffiliations() {
        return affiliations;
    }

    /**
     * @param affiliations the affiliations to set
     */
    public void setAffiliations(List<String> affiliations) {
        this.affiliations = affiliations;
    }

    /**
     * @return the dateOfNationality
     */
    public LocalDate getDateOfNationality() {
        return dateOfNationality;
    }

    /**
     * @param dateOfNationality the dateOfNationality to set
     */
    public void setDateOfNationality(LocalDate dateOfNationality) {
        this.dateOfNationality = dateOfNationality;
    }

    /**
     * @return the activeEnrolment
     */
    public boolean isActiveEnrolment() {
        return activeEnrolment;
    }

    /**
     * @param activeEnrolment the activeEnrolment to set
     */
    public void setActiveEnrolment(boolean activeEnrolment) {
        this.activeEnrolment = activeEnrolment;
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


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        // v5 clients sent upper case (M, F, X, U)
        this.gender = gender == null ? null : gender.toLowerCase(java.util.Locale.ROOT);
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public String getAlternateName() {
        return alternateName;
    }

    public void setAlternateName(String alternateName) {
        this.alternateName = alternateName;
    }

    public String getSurnamePrefix() {
        return surnamePrefix;
    }

    public void setSurnamePrefix(String surnamePrefix) {
        this.surnamePrefix = surnamePrefix;
    }

    public String getIdCheckName() {
        return idCheckName;
    }

    public void setIdCheckName(String idCheckName) {
        this.idCheckName = idCheckName;
    }

    @JsonProperty("languageOfChoice")
    public List<String> getLanguageOfChoice() {
        return oeapi.oeapiUtils.splitCsv(languageOfChoice);
    }

    @JsonProperty("languageOfChoice")
    public void setLanguageOfChoice(List<String> languageOfChoice) {
        this.languageOfChoice = oeapi.oeapiUtils.joinCsv(languageOfChoice);
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<oeapiIdentifierEntry> getOtherCodes() {
        return otherCodes;
    }

    public void setOtherCodes(List<oeapiIdentifierEntry> otherCodes) {
        this.otherCodes = otherCodes;
    }

    public Ext getExt() {
        return ext;
    }

    public void setExt(Ext ext) {
        this.ext = ext;
    }

    // ---- v6 Country / Nationality objects over the stored ISO code -------------

    @JsonProperty("countryOfBirth")
    public Map<String, String> countryOfBirthObject() {
        return isoObject(countryOfBirth);
    }

    @JsonProperty("countryOfBirth")
    public void countryOfBirthObject(Map<String, String> country) {
        this.countryOfBirth = isoCode(country);
    }

    @JsonProperty("nationality")
    public Map<String, String> nationalityObject() {
        return isoObject(nationality);
    }

    @JsonProperty("nationality")
    public void nationalityObject(Map<String, String> value) {
        this.nationality = isoCode(value);
    }

    /** {"iso3166-1-alpha2": "NL"} for a two letter code, alpha3 for three letters; null otherwise. */
    static Map<String, String> isoObject(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        Map<String, String> object = new LinkedHashMap<>();
        if (code.length() == 2) {
            object.put("iso3166-1-alpha2", code);
        } else if (code.length() == 3) {
            object.put("iso3166-1-alpha3", code);
        } else {
            return null;    // a v5 free text value such as "Dutch" has no v6 representation
        }
        return object;
    }

    static String isoCode(Map<String, String> object) {
        if (object == null) {
            return null;
        }
        for (String key : new String[]{"iso3166-1-alpha2", "iso3166-1-alpha3", "iso3166-3", "iso3166-2"}) {
            String value = object.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
