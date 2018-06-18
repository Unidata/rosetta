package edu.ucar.unidata.rosetta.domain.wizard;

import java.io.Serializable;

import com.sun.tools.javah.Gen;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A POJO to hold variable metadata collected from the user
 * in the Rosetta application (acts as a form-backing-object).
 *
 * @author oxelson@ucar.edu
 */
public class VariableMetadata implements Serializable {

    private String id;
    private String metadataKey;   // The name of the metadata.
    private String metadataValue; // The actual metadata.
    private String variableName;
    private boolean coordinateVariable;
    private String variableType;
    private String dataType;
    private String variableDescription;
    private String instrumentDescription;
    private String missingValue;
    private String units;

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
     * @return  The string representation.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
