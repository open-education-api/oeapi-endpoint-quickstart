package oeapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.validation.ValidEnumYaml;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(name = "Person")
public class Person extends PrimaryCode {

    @Id
    @Column(name = "person_id")
    @JsonProperty(value = "personId")
    //@JsonProperty(value = "personId", access = JsonProperty.Access.WRITE_ONLY)
    private String personId = UUID.randomUUID().toString();
    //@ValidEnumYaml(yamlfile = "gender.yml")

    private enum GenderType {
        M, F, X, U
    }

    @Enumerated(EnumType.ORDINAL)
    private GenderType gender;

    private String givenName;
    private String initials;
    private String surname;
    private String displayName;
    private String mail;
    private String secondaryMail;
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
    private boolean activeEnrollment;

    @OneToMany(mappedBy = "person")
    @JsonManagedReference("associationPesonReference")
    private List<Association> associations;

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
     * @return the gender
     */
    public GenderType getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(GenderType gender) {
        this.gender = gender;
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
     * @return the mail
     */
    public String getMail() {
        return mail;
    }

    /**
     * @param mail the mail to set
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * @return the secondaryMail
     */
    public String getSecondaryMail() {
        return secondaryMail;
    }

    /**
     * @param secondaryMail the secondaryMail to set
     */
    public void setSecondaryMail(String secondaryMail) {
        this.secondaryMail = secondaryMail;
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
    public String getCountryOfBirth() {
        return countryOfBirth;
    }

    /**
     * @param countryOfBirth the countryOfBirth to set
     */
    public void setCountryOfBirth(String countryOfBirth) {
        this.countryOfBirth = countryOfBirth;
    }

    /**
     * @return the nationality
     */
    public String getNationality() {
        return nationality;
    }

    /**
     * @param nationality the nationality to set
     */
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
     * @return the activeEnrollment
     */
    public boolean isActiveEnrollment() {
        return activeEnrollment;
    }

    /**
     * @param activeEnrollment the activeEnrollment to set
     */
    public void setActiveEnrollment(boolean activeEnrollment) {
        this.activeEnrollment = activeEnrollment;
    }

    /**
     * @return the associations
     */
    public List<Association> getAssociations() {
        return associations;
    }

    /**
     * @param associations the associations to set
     */
    public void setAssociations(List<Association> associations) {
        this.associations = associations;
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
