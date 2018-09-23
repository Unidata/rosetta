/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
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

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }


    public String getMetadataType() {
        return metadataType;
    }

    public void setMetadataType(String metadataType) {
        this.metadataType = metadataType;
    }

    public String getMetadataTypeStructure() {
        return metadataTypeStructure;
    }

    public void setMetadataTypeStructure(String metadataTypeStructure) {
        this.metadataTypeStructure = metadataTypeStructure;
    }

    public String getVerticalDirection() {
        return verticalDirection;
    }

    public void setVerticalDirection(String verticalDirection) {
        this.verticalDirection = verticalDirection;
    }

    public String getMetadataValueType() {
        return metadataValueType;
    }

    public void setMetadataValueType(String metadataValueType) {
        this.metadataValueType = metadataValueType;
    }

    public List<VariableMetadata> getRequiredMetadata() {
        return requiredMetadata;
    }

    public void setRequiredMetadata(List<VariableMetadata> requiredMetadata) {
        this.requiredMetadata = requiredMetadata;
    }

    public List<VariableMetadata> getRecommendedMetadata() {
        return recommendedMetadata;
    }

    public void setRecommendedMetadata(List<VariableMetadata> recommendedMetadata) {
        this.recommendedMetadata = recommendedMetadata;
    }

    public List<VariableMetadata> getAdditionalMetadata() {
        return additionalMetadata;
    }

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
