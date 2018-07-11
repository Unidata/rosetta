package edu.ucar.unidata.rosetta.domain.wizard;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Object representing a metadata profile schema as outlined in
 * https://github.com/Unidata/rosetta/wiki/Metadata-Profile-Schema
 */
public class MetadataProfile {

    private String attributeName;
    private ComplianceLevel complianceLevel;
    private String description;
    private String displayName;
    private String id;
    private String metadataGroup;
    private String metadataProfileName;
    private String metadataProfileVersion;
    private MetadataType metadataType = MetadataType.GLOBAL;
    private MetadataTypeStructure metadataTypeStructure = MetadataTypeStructure.GLOBAL_ATTRIBUTES;
    private MetadataValueType metadataValueType = MetadataValueType.STRING;

    public enum ComplianceLevel {
        REQUIRED,
        RECOMMENDED,
        ADDITIONAL;
    }

    public enum MetadataType {
        GLOBAL,
        COORDINATE_VARIABLE,
        DATA_VARIABLE,
        MASK_VARIABLE,
        COUNT_VARIABLE,
        INDEX_VARIABLE;
    }

    public enum MetadataTypeStructure {
        GLOBAL_ATTRIBUTES,
        LAT,
        LON,
        DEPTH,
        TIME,
        CRS,
        BOUNDS,
        DATA,
        MASK,
        COUNT,
        INDEX;
    }

    public enum MetadataValueType {
        BOOLEAN,
        BYTE,
        CHAR,
        DOUBLE,
        FLOAT,
        INT,
        LONG,
        SHORT,
        STRING,
        UBYTE,
        UINT,
        ULONG,
        USHORT,
        SAME
    }

    /**
     * Returns the name of the metadata attribute.
     *
     * @return  The attribute name.
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Returns the compliance level of the metadata item.
     *
     * @return  The compliance level.
     */
    public ComplianceLevel getComplianceLevel() {
        return complianceLevel;
    }

    /**
     * Returns the description for the metadata attribute
     * used for wizard display help tool tips.
     *
     * @return  The metadata description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the human-friendly name use in the front end display.
     *
     * @return  The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the unique id associated with this object.
     * (Used during persistence.)
     *
     * @return  The unique id.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the metadata group name within the produced netCDF-4 file.
     *
     * @return  The metadata group name.
     */
    public String getMetadataGroup() {
        return metadataGroup;
    }

    /**
     * Returns the human-friendly name of the metadata profile.
     *
     * @return  The metadata profile name.
     */
    public String getMetadataProfileName() {
        return metadataProfileName;
    }

    /**
     * Returns the version of the metadata profile being documented.
     *
     * @return  The metadata profile version.
     */
    public String getMetadataProfileVersion() {
        return metadataProfileVersion;
    }

    /**
     * Returns the designated metadata type.
     *
     * @return The metadata type.
     */
    public MetadataType getMetadataType() {
        return metadataType;
    }

    /**
     * Returns the structure with which the metadata attribute is associated.
     *
     * @return  The metadata type structure.
     */
    public MetadataTypeStructure getMetadataTypeStructure() {
        return metadataTypeStructure;
    }

    /**
     * Returns the data type of the metadata attribute value.
     *
     * @return  The metadata type value.
     */
    public MetadataValueType getMetadataValueType() {
        return metadataValueType;
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
     * Sets the compliance level of the metadata item.
     *
     * @param complianceLevel   The compliance level.
     */
    public void setComplianceLevel(ComplianceLevel complianceLevel) {
        this.complianceLevel = complianceLevel;
    }

    /**
     * Sets the description for the metadata attribute
     * used for wizard display help tool tips.
     *
     * @param description The metadata description.
     */
    public void setDescription(String description) {
        this.description = description;
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
     * Sets the unique id associated with this object.
     * (Used during persistence.)
     *
     * @param id  The unique id.
     */
    public void setId(String id) {
        this.id = id;
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
     * Sets human-friendly name of the metadata profile.
     *
     * @param metadataProfileName  The metadata profile name.
     */
    public void setMetadataProfileName(String metadataProfileName) {
        this.metadataProfileName = metadataProfileName;
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
     * sets the designated metadata type.
     *
     * @param metadataType The metadata type.
     */
    public void setMetadataType(MetadataType metadataType) {
        this.metadataType = metadataType;
    }

    /**
     * Sets the structure with which the metadata attribute is associated.
     *
     * @param metadataTypeStructure  The metadata type structure.
     */
    public void setMetadataTypeStructure(MetadataTypeStructure metadataTypeStructure) {
        this.metadataTypeStructure = metadataTypeStructure;
    }

    /**
     * Sets the data type of the metadata attribute value.
     *
     * @param metadataValueType  The metadata type value.
     */
    public void setMetadataValueType(MetadataValueType metadataValueType) {
        this.metadataValueType = metadataValueType;
    }

    /**
     * String representation of this object.
     *
     * @return  The string representation.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
