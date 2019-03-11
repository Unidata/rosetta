/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

/**
 * Object representing a metadata profile schema as outlined in
 * https://github.com/Unidata/rosetta/wiki/Metadata-Profile-Schema
 */
public class MetadataProfile {

    private String attributeName; // required
    private String complianceLevel = "additional";
    private String description;
    private String displayName;
    private String exampleValues;
    private String id;
    private String metadataGroup = "root";
    private String metadataProfileName; // required
    private String metadataProfileVersion; // required
    private String metadataType = "Global";
    private String metadataTypeStructureName = "Global Attributes";
    private String metadataValueType = "String";

    /**
     * Determines if this object and the provided object are equal.
     *
     * @param obj The comparison object.
     * @return true if equal; otherwise false;
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MetadataProfile) {
            MetadataProfile other = (MetadataProfile) obj;
            return Objects.equals(getAttributeName(), other.getAttributeName()) &&
                   Objects.equals(getComplianceLevel(), other.getComplianceLevel()) &&
                   Objects.equals(getDescription(), other.getDescription()) &&
                   Objects.equals(getDisplayName(), other.getDisplayName()) &&
                   Objects.equals(getExampleValues(), other.getExampleValues()) &&
                   Objects.equals(getId(), other.getId()) &&
                   Objects.equals(getMetadataGroup(), other.getMetadataGroup()) &&
                   Objects.equals(getMetadataProfileName(), other.getMetadataProfileName()) &&
                   Objects.equals(getMetadataProfileVersion(), other.getMetadataProfileVersion()) &&
                   Objects.equals(getMetadataType(), other.getMetadataType()) &&
                   Objects.equals(getMetadataTypeStructureName(), other.getMetadataTypeStructureName()) &&
                   Objects.equals(getMetadataValueType(), other.getMetadataValueType());
        }
        return false;
    }

    /**
     * Returns the hash code of a non-null argument and 0 for a null argument.
     *
     * @return Hash code for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getAttributeName(),
                getComplianceLevel(),
                getDescription(),
                getDisplayName(),
                getExampleValues(),
                getId(),
                getMetadataGroup(),
                getMetadataProfileName(),
                getMetadataProfileVersion(),
                getMetadataType(),
                getMetadataTypeStructureName(),
                getMetadataValueType());
    }

    /**
     * Returns the name of the metadata attribute.
     *
     * @return The attribute name.
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Sets the name of the metadata attribute.
     *
     * @param attributeName The attribute name.
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * Returns the compliance level of the metadata item.
     *
     * @return The compliance level.
     */
    public String getComplianceLevel() {
        return complianceLevel;
    }

    /**
     * Sets the compliance level of the metadata item.
     *
     * @param complianceLevel The compliance level.
     */
    public void setComplianceLevel(String complianceLevel) {
        this.complianceLevel = complianceLevel;
    }

    /**
     * Returns the description for the metadata attribute used for wizard display help tool tips.
     *
     * @return The metadata description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for the metadata attribute used for wizard display help tool tips.
     *
     * @param description The metadata description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the human-friendly name use in the front end display.
     *
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the human-friendly name use in the front end display.
     *
     * @param displayName The display name.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns a string of examples of that illustrate this metadata.
     *
     * @return The examples.
     */
    public String getExampleValues() {
        return exampleValues;
    }

    /**
     * Sets a string of examples of that illustrate this metadata.
     *
     * @param exampleValues The examples.
     */
    public void setExampleValues(String exampleValues) {
        this.exampleValues = exampleValues;
    }

    /**
     * Returns the unique id associated with this object. (Used during persistence.)
     *
     * @return The unique id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique id associated with this object. (Used during persistence.)
     *
     * @param id The unique id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the metadata group name within the produced netCDF-4 file.
     *
     * @return The metadata group name.
     */
    public String getMetadataGroup() {
        return metadataGroup;
    }

    /**
     * sets the metadata group name within the produced netCDF-4 file.
     *
     * @param metadataGroup metadata group name.
     */
    public void setMetadataGroup(String metadataGroup) {
        this.metadataGroup = metadataGroup;
    }

    /**
     * Returns the human-friendly name of the metadata profile.
     *
     * @return The metadata profile name.
     */
    public String getMetadataProfileName() {
        return metadataProfileName;
    }

    /**
     * Sets human-friendly name of the metadata profile.
     *
     * @param metadataProfileName The metadata profile name.
     */
    public void setMetadataProfileName(String metadataProfileName) {
        this.metadataProfileName = metadataProfileName;
    }

    /**
     * Returns the version of the metadata profile being documented.
     *
     * @return The metadata profile version.
     */
    public String getMetadataProfileVersion() {
        return metadataProfileVersion;
    }

    /**
     * Sets the version of the metadata profile being documented.
     *
     * @param metadataProfileVersion The metadata profile version.
     */
    public void setMetadataProfileVersion(String metadataProfileVersion) {
        this.metadataProfileVersion = metadataProfileVersion;
    }

    /**
     * Returns the designated metadata type.
     *
     * @return The metadata type.
     */
    public String getMetadataType() {
        return metadataType;
    }

    /**
     * Sets the designated metadata type.
     *
     * @param metadataType The metadata type.
     */
    public void setMetadataType(String metadataType) {
        this.metadataType = metadataType;
    }

    /**
     * Returns the structure with which the metadata attribute is associated.
     *
     * @return The metadata type structure.
     */
    public String getMetadataTypeStructureName() {
        return metadataTypeStructureName;
    }

    /**
     * Sets the structure with which the metadata attribute is associated.
     *
     * @param metadataTypeStructureName The metadata type structure.
     */
    public void setMetadataTypeStructureName(String metadataTypeStructureName) {
        this.metadataTypeStructureName = metadataTypeStructureName;
    }

    /**
     * Returns the data type of the metadata attribute value.
     *
     * @return The metadata type value.
     */
    public String getMetadataValueType() {
        return metadataValueType;
    }

    /**
     * Sets the data type of the metadata attribute value.
     *
     * @param metadataValueType The metadata type value.
     */
    public void setMetadataValueType(String metadataValueType) {
        this.metadataValueType = metadataValueType;
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
