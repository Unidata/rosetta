/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representing a variable.
 */
public class Variable {

    private int variableId;
    private String wizardDataId;
    private int columnNumber;
    private String variableName;
    private String metadataType;
    private String metadataTypeStructure;
    private String verticalDirection;
    private String metadataValueType;
    private List<VariableMetadata> requiredMetadata = new ArrayList<>();
    private List<VariableMetadata> recommendedMetadata = new ArrayList<>();
    private List<VariableMetadata> additionalMetadata = new ArrayList<>();

    /**
     * Returns the unique id associated with this object.
     *
     * @return The unique id.
     */
    public int getVariableId() {
        return variableId;
    }

    /**
     * Sets the unique id associated with this object.
     *
     * @param variableId The unique variableId.
     */
    public void setVariableId(int variableId) {
        this.variableId = variableId;
    }

    /**
     * Returns the id of the wizard data associated with this object.
     *
     * @return The id of the wizard data.
     */
    public String getWizardDataId() {
        return wizardDataId;
    }

    /**
     * Sets the id of the wizard data  associated with this object.
     *
     * @param wizardDataId The id of the wizard data .
     */
    public void setWizardDataId(String wizardDataId) {
        this.wizardDataId = wizardDataId;
    }

    /**
     * Returns the column number in the wizard grid that corresponds to this variable.
     *
     * @return  The column number.
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Sets the column number in the wizard grid that corresponds to this variable.
     *
     * @param columnNumber  The column number.
     */
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * Returns the name assigned to this variable.
     *
     * @return The variable name.
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * Sets the name assigned to this variable.
     *
     * @param variableName  The variable name.
     */
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    /**
     * Returns the variable metadata type (coordinate or non-coordinate).
     *
     * @return The variable metadata type.
     */
    public String getMetadataType() {
        return metadataType;
    }

    /**
     * sets the variable metadata type (coordinate or non-coordinate).
     *
     * @param metadataType  The variable metadata type.
     */
    public void setMetadataType(String metadataType) {
        this.metadataType = metadataType;
    }

    /**
     * Returns the variable metadata type structure (lat, lon, etc.).
     *
     * @return The variable metadata type structure.
     */
    public String getMetadataTypeStructure() {
        return metadataTypeStructure;
    }

    /**
     * Sets the variable metadata type structure (lat, lon, etc.).
     *
     * @param metadataTypeStructure  The variable metadata type structure.
     */
    public void setMetadataTypeStructure(String metadataTypeStructure) {
        this.metadataTypeStructure = metadataTypeStructure;
    }

    /**
     * Returns the vertical direction of the variable (if metadata type structure is vertical).
     *
     * @return The vertical direction.
     */
    public String getVerticalDirection() {
        return verticalDirection;
    }

    /**
     * sets the vertical direction of the variable (if metadata type structure is vertical).
     *
     * @param verticalDirection  The vertical direction.
     */
    public void setVerticalDirection(String verticalDirection) {
        this.verticalDirection = verticalDirection;
    }

    /**
     * Returns the variable metadata type value (string, float, etc).
     *
     * @return The variable metadata type value.
     */
    public String getMetadataValueType() {
        return metadataValueType;
    }

    /**
     * Sets the variable metadata type value (string, float, etc).
     *
     * @param metadataValueType  The variable metadata type value.
     */
    public void setMetadataValueType(String metadataValueType) {
        this.metadataValueType = metadataValueType;
    }

    /**
     * Returns the required metadata for the variable.
     *
     * @return The required metadata.
     */
    public List<VariableMetadata> getRequiredMetadata() {
        return requiredMetadata;
    }

    /**
     * Sets the required metadata for the variable.
     *
     * @param requiredMetadata  The required metadata.
     */
    public void setRequiredMetadata(List<VariableMetadata> requiredMetadata) {
        this.requiredMetadata = requiredMetadata;
    }

    /**
     * Returns the recommended metadata for the variable.
     *
     * @return The recommended metadata.
     */
    public List<VariableMetadata> getRecommendedMetadata() {
        return recommendedMetadata;
    }

    /**
     * Sets the recommended metadata for the variable.
     *
     * @param recommendedMetadata  The recommended metadata.
     */
    public void setRecommendedMetadata(List<VariableMetadata> recommendedMetadata) {
        this.recommendedMetadata = recommendedMetadata;
    }

    /**
     * Returns the additional metadata for the variable.
     *
     * @return The additional metadata.
     */
    public List<VariableMetadata> getAdditionalMetadata() {
        return additionalMetadata;
    }

    /**
     * Sets the additional metadata for the variable.
     *
     * @param additionalMetadata  The additional metadata.
     */
    public void setAdditionalMetadata(List<VariableMetadata> additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
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
