package edu.ucar.unidata.rosetta.domain.wizard;

import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A POJO to hold custom data file type attributes collected from
 * the user in the Rosetta application (acts as a form-backing-object).
 *
 * @author oxelson@ucar.edu
 */
public class CustomTypeAttributes implements Serializable {

    private String id;
    private String headerLineNumbers;
    private boolean noHeaderLines;
    private String delimiter;
    private String otherDelimiter;
    private Locale decimalSeparatorLocale = Locale.ENGLISH;

    /**
     * Returns the unique id associated with this object.
     *
     * @return  The unique id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique id associated with this object.
     *
     * @param id  The unique id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the header line numbers of the data file.
     *
     * @return  The header line numbers.
     */
    public String getHeaderLineNumbers() {
        return headerLineNumbers;
    }

    /**
     * Sets the header line numbers of the data file.
     *
     * @param headerLineNumbers The header line numbers.
     */
    public void setHeaderLineNumbers(String headerLineNumbers) {
        this.headerLineNumbers = headerLineNumbers;
    }


    /**
     * Returns the no leader lines value.
     *
     * @return  The no header lines value.
     */
    public boolean getNoHeaderLines(){
        return noHeaderLines;
    }

    /**
     * Sets the no leader lines value.
     *
     * @param noHeaderLines The no header lines value.
     */
    public void setNoHeaderLines(boolean noHeaderLines) {
        this.noHeaderLines = noHeaderLines;
    }

    /**
     * Returns the data file delimiter.
     *
     * @return  The delimiter.
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the data file delimiter.
     *
     * @param delimiter The data file delimiter.
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }


    /**
     * Returns the other data file delimiter.
     *
     * @return  The other delimiter.
     */
    public String getOtherDelimiter() {
        return otherDelimiter;
    }

    /**
     * Sets the other data file delimiter.
     *
     * @param otherDelimiter The other delimiter.
     */
    public void setOtherDelimiter(String otherDelimiter) {
        this.otherDelimiter = otherDelimiter;
    }

    /**
     * Returns the Locale to use for the decimal separator.
     *
     * @return The Locale.
     */
    public Locale getDecimalSeparatorLocale() {
        return decimalSeparatorLocale;
    }

    /**
     * Sets the locale to FRENCH if "Comma" is given as input.
     *
     * Else it sets it to ENGLISH (for Point as separator), which is the
     * default.
     *
     * @param decimalSeparator Text representation of the decimal separator to be used.
     */
    public void setDecimalSeparator(String decimalSeparator) {
        switch (decimalSeparator) {
            case "Comma":
                this.decimalSeparatorLocale = Locale.FRENCH;
                break;
            case "Point":
            default:
                this.decimalSeparatorLocale = Locale.ENGLISH;
                break;
        }
    }

    /**
     * String representation of this Data object.
     *
     * @return  The string representation.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
