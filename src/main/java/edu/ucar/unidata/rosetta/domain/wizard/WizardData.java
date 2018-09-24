/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain.wizard;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Form-backing object to collect data from the wizard.
 */
public class WizardData {

    private String cfType;
    private String community;
    private String dataFileType;
    private String delimiter;
    private String headerLineNumbers;
    private String id;
    private String metadataProfile;
    private boolean noHeaderLines;
    private String platform;
    private String variableMetadata;


    /**
     * Returns the CF Type selected by the user.
     *
     * @return The CF Type.
     */
    public String getCfType() {
        return cfType;
    }

    /**
     * Sets the CF Type selected by the user.
     *
     * @param cfType The CF Type.
     */
    public void setCfType(String cfType) {
        this.cfType = cfType;
    }

    /**
     * Returns the community selected by the user.
     *
     * @return The community.
     */
    public String getCommunity() {
        return community;
    }

    /**
     * Sets the community selected by the user.
     *
     * @param community The community.
     */
    public void setCommunity(String community) {
        this.community = community;
    }

    /**
     * Returns the data file type.
     *
     * @return  The data file type.
     */
    public String getDataFileType() {
        return dataFileType;
    }

    /**
     * Sets the data file type.
     *
     * @param dataFileType  The data file type.
     */
    public void setDataFileType(String dataFileType) {
        this.dataFileType = dataFileType;
    }

    /**
     * Returns the custom data file delimiter.
     *
     * @return The delimiter.
     */
    public String getDelimiter() {
        return delimiter;
    }


    /**
     * Sets the custom data file delimiter.
     *
     * @param delimiter The data file delimiter.
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Returns the header line numbers of the custom data file.
     *
     * @return The header line numbers.
     */
    public String getHeaderLineNumbers() {
        return headerLineNumbers;
    }

    /**
     * Sets the header line numbers of the custom data file.
     *
     * @param headerLineNumbers The header line numbers.
     */
    public void setHeaderLineNumbers(String headerLineNumbers) {
        this.headerLineNumbers = headerLineNumbers;
    }

    /**
     * Returns the unique id associated with this object.
     *
     * @return The unique id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique id associated with this object.
     *
     * @param id The unique id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the metadata profile selected by the user.
     *
     * @return The metadata profile.
     */
    public String getMetadataProfile() {
        return metadataProfile;
    }

    /**
     * Sets the metadata profile selected by the user.
     *
     * @param metadataProfile The metadata profile.
     */
    public void setMetadataProfile(String metadataProfile) {
        this.metadataProfile = metadataProfile;
    }

    /**
     * Returns whether there are no leader lines in the file.
     *
     * @return true if no header lines; otherwise false.
     */
    public boolean isNoHeaderLines() {
        return noHeaderLines;
    }

    /**
     * Sets whether there are no leader lines in the file.
     *
     * @param noHeaderLines true if no header lines; otherwise false.
     */
    public void setNoHeaderLines(boolean noHeaderLines) {
        this.noHeaderLines = noHeaderLines;
    }

    /**
     * Returns the platform selected by the user.
     *
     * @return The platform.
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Sets the platform selected by the user.
     *
     * @param platform The platform.
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * Returns the variableMetadata selected by the user.
     *
     * @return The variableMetadata.
     */
    public String getVariableMetadata() {
        return variableMetadata;
    }

    /**
     * Sets the variableMetadata selected by the user.
     *
     * @param variableMetadata The variableMetadata.
     */
    public void setVariableMetadata(String variableMetadata) {
        this.variableMetadata = variableMetadata;
    }

    /**
     * String representation of this object.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
