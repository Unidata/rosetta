/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Object representing a variable-specific information as outlined in
 * https://github.com/Unidata/rosetta/wiki/Rosetta-Template-Attributes#variableinfo-object-information
 */
public class VariableInfo {

    private int columnId = -1; // -1 does not make sense, so check for this value to see if it has not been set.
    private String name;
    private List<RosettaAttribute> rosettaControlMetadata;
    private List<RosettaAttribute> variableMetadata;

    /**
     * Override equals for VariableInfo.
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) return true;
        if (!(obj instanceof VariableInfo)) {
            return false;
        }

        VariableInfo vi = (VariableInfo) obj;

        return columnId == vi.getColumnId() &&
                Objects.equals(name, vi.getName()) &&
                Objects.equals(rosettaControlMetadata, vi.getRosettaControlMetadata()) &&
                Objects.equals(variableMetadata, vi.getVariableMetadata());
    }

    /**
     * Override Object.hashCode() to implement equals.
     */
    @Override
    public int hashCode() {
        return Objects.hash(columnId, name, rosettaControlMetadata, variableMetadata);
    }

    /**
     * Returns ID of the column in the CSV file.
     *
     * @return The column ID.
     */
    public int getColumnId() {
        return columnId;
    }

    /**
     * Returns the assigned name of the metadata item.
     *
     * @return The metadata name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the rosetta control metadata (used to determine/control the other metadata
     * collected).
     *
     * @return The rosetta control metadata.
     */
    public List<RosettaAttribute> getRosettaControlMetadata() {
        return rosettaControlMetadata;
    }

    /**
     * Returns the metadata specific to the variable.
     *
     * @return The variable metadata.
     */
    public List<RosettaAttribute> getVariableMetadata() {
        return variableMetadata;
    }

    /**
     * Sets ID of the column in the CSV file.
     *
     * @param columnId The column ID.
     */
    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }

    /**
     * Sets the assigned name of the metadata item.
     *
     * @param name The metadata name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the rosetta control metadata (used to determine/control the other metadata collected).
     *
     * @param rosettaControlMetadata The rosetta control metadata.
     */
    public void setRosettaControlMetadata(List<RosettaAttribute> rosettaControlMetadata) {
        this.rosettaControlMetadata = rosettaControlMetadata;
    }

    /**
     * Sets the metadata specific to the variable.
     *
     * @param variableMetadata The variable metadata.
     */
    public void setVariableMetadata(List<RosettaAttribute> variableMetadata) {
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

    /**
     * update this VariableInfo object with new values from another VariableInfo object.
     *
     * @param updatedVariableInfo The VariableInfo object.
     */
    public void updateVariableInfo(VariableInfo updatedVariableInfo) {

        if (updatedVariableInfo.getName() != null) {
            this.name = updatedVariableInfo.getName();
        }

        if (updatedVariableInfo.getColumnId() != -1) {
            this.columnId = updatedVariableInfo.getColumnId();
        }

        List<RosettaAttribute> variableMetadataUpdates = updatedVariableInfo.getVariableMetadata();
        if (variableMetadataUpdates != null) {
            // a little more complex. We need to go through each new piece of variable metadata,
            // search for it by name in the existing list of variable metadata, and remove it if it
            // exits. Then, we can merge the new and existing lists of variable metadata.
            for (RosettaAttribute variableMetadataUpdate : variableMetadataUpdates) {
                Predicate<RosettaAttribute> attributePredicate = attr -> attr.getName().equals(variableMetadataUpdate.getName());
                this.variableMetadata.removeIf(attributePredicate);
            }
            this.variableMetadata.addAll(variableMetadataUpdates);
            // remove any attributes with empty values
            Predicate<RosettaAttribute> emptyValuePredicate = attr -> attr.getValue().equals("");
            this.variableMetadata.removeIf(emptyValuePredicate);
        }

        List<RosettaAttribute> rosettaControlMetadataUpdates = updatedVariableInfo.getRosettaControlMetadata();
        if (rosettaControlMetadataUpdates != null) {
            // a little more complex. We need to go through each new piece of control metadata,
            // search for it by name in the existing list of control metadata, and remove it if it
            // exits. Then, we can merge the new and existing lists of control metadata.
            for (RosettaAttribute rosettaControlMetadataUpdate : rosettaControlMetadataUpdates) {
                Predicate<RosettaAttribute> attributePredicate = attr -> attr.getName().equals(rosettaControlMetadataUpdate.getName());
                this.rosettaControlMetadata.removeIf(attributePredicate);
            }
            this.rosettaControlMetadata.addAll(rosettaControlMetadataUpdates);
            // remove any attributes with empty values
            Predicate<RosettaAttribute> emptyValuePredicate = attr -> attr.getValue().equals("");
            this.rosettaControlMetadata.removeIf(emptyValuePredicate);
        }
    }

    /**
     * Created/returns a nicely formatted string representation of this object
     * for printing in the transaction log.
     *
     * @return  Formatted string representation of this object.
     */
    public String transactionLogFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("\tColumn ID: ").append(getColumnId()).append("\n");
        if (getName() != null) {
            sb.append("\tName: ").append(getName()).append("\n");
        }
        if (getRosettaControlMetadata() != null) {
            if (!getRosettaControlMetadata().isEmpty()) {
                sb.append("Rosetta Control Metadata: \n");
                for (RosettaAttribute rosettaAttribute : getRosettaControlMetadata()) {
                    sb.append(rosettaAttribute.transactionLogFormat()).append("\n");
                }
            }
        }
        if (getVariableMetadata() != null) {
            if (!getVariableMetadata().isEmpty()) {
                sb.append("Variable Metadata: \n");
                for (RosettaAttribute rosettaAttribute : getVariableMetadata()) {
                    sb.append(rosettaAttribute.transactionLogFormat()).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
