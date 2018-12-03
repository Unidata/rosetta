/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Form-backing object for the wizard to collect a global metadata.
 */

public class GlobalMetadata {


    private String metadataGroup;
    private String metadataKey;
    private String metadataValue;
    private String metadataValueType; // STRING, INTEGER, etc.
    private String wizardDataId;

    /**
     * Returns the id of the wizardData object with which this object is associated.
     *
     * @return The wizardDataId.
     */
    public String getWizardDataId() {
        return wizardDataId;
    }

    /**
     * Sets the id of the wizardData object with which this object is associated.
     *
     * @param wizardDataId The wizardDataId.
     */
    public void setWizardDataId(String wizardDataId) {
        this.wizardDataId = wizardDataId;
    }

    public String getMetadataValueType() {
        return metadataValueType;
    }

    public void setMetadataValueType(String metadataValueType) {
        this.metadataValueType = metadataValueType;
    }

    /**
     * Returns the metadata metadataGroup.
     *
     * @return The metadata metadataGroup.
     */
    public String getMetadataGroup() {
        return metadataGroup;
    }

    /**
     * Sets the the metadata metadataGroup (global or variable).
     *
     * @param metadataGroup The metadata metadataGroup.
     */
    public void setMetadataGroup(String metadataGroup) {
        this.metadataGroup = metadataGroup;
    }

    /**
     * Returns the metadata key.
     *
     * @return The metadata key.
     */
    public String getMetadataKey() {
        return metadataKey;
    }

    /**
     * Sets the metadata key.
     *
     * @param metadataKey The metadata key.
     */
    public void setMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
    }

    /**
     * Returns the metadata value.
     *
     * @return The metadatavalue.
     */
    public String getMetadataValue() {
        return metadataValue;
    }

    /**
     * Sets the metadata value.
     *
     * @param metadataValue The metadata value.
     */
    public void setMetadataValue(String metadataValue) {
        this.metadataValue = metadataValue;
    }

    /**
     * String representation of this Data object.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}