package oeapi.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;

import oeapi.converter.oeapiUnitaExtConverter;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;

/**
 *
 * @author itziar
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Cost implements Serializable {

    private String costType;

    private String amount;

    private String vatAmount;

    private String amountWithoutVat;

    private String currency;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    private List<oeapiLanguageTypedString> displayAmount;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaExtConverter.class)
    private Ext ext;

    /**
     * @return the costType
     */
    public String getCostType() {
        return costType;
    }

    /**
     * @param costType the costType to set
     */
    public void setCostType(String costType) {
        this.costType = costType;
    }

    /**
     * @return the amount
     */
    public String getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(String amount) {
        this.amount = amount;
    }

    /**
     * @return the vatAmount
     */
    public String getVatAmount() {
        return vatAmount;
    }

    /**
     * @param vatAmount the vatAmount to set
     */
    public void setVatAmount(String vatAmount) {
        this.vatAmount = vatAmount;
    }

    /**
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @param currency the currency to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * @return the displayAmount
     */
    public List<oeapiLanguageTypedString> getDisplayAmount() {
        return displayAmount;
    }

    /**
     * @param displayAmount the displayAmount to set
     */
    public void setDisplayAmount(List<oeapiLanguageTypedString> displayAmount) {
        this.displayAmount = displayAmount;
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
     * @return the amountWithoutVat
     */
    public String getAmountWithoutVat() {
        return amountWithoutVat;
    }

    /**
     * @param amountWithoutVat the amountWithoutVat to set
     */
    public void setAmountWithoutVat(String amountWithoutVat) {
        this.amountWithoutVat = amountWithoutVat;
    }

}
