package oeapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.validation.Valid;
import oeapi.converter.oeapiGeolocationConverter;
import oeapi.converter.oeapiResultConverter;
import oeapi.converter.oeapiUnitaExtConverter;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.validation.ValidLanguageTypedString;

/**
 * The type Address.
 *
 * @author Carlos Alonso - losalo@unavarra.es
 */
@Valid
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Address {

    private String addressType;

    private String street;

    private String streetNumber;

    private String postalCode;

    private String city;

    private String countryCode;

    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString
    private List<oeapiLanguageTypedString> additional;

    @Convert(converter = oeapiGeolocationConverter.class)
    private Geolocation geolocation;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaExtConverter.class)
    private Ext ext = new Ext();

    /**
     * @return the addressType
     */
    public String getAddressType() {
        return addressType;
    }

    /**
     * @param addressType the addressType to set
     */
    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    /**
     * @return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * @param street the street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * @return the streetNumber
     */
    public String getStreetNumber() {
        return streetNumber;
    }

    /**
     * @param streetNumber the streetNumber to set
     */
    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    /**
     * @return the postalCode
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * @param postalCode the postalCode to set
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the countryCode
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * @param countryCode the countryCode to set
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * @return the additional
     */
    public List<oeapiLanguageTypedString> getAdditional() {
        return additional;
    }

    /**
     * @param additional the additional to set
     */
    public void setAdditional(List<oeapiLanguageTypedString> additional) {
        this.additional = additional;
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
