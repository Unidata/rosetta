package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Metadata {

    private String id;
    private String type;
    private String metadataKey;
    private String metadataValue;

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
     * Returns the metadata type (global or variable).
     *
     * @return The metadata type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the the metadata type (global or variable).
     *
     * @param type The metadata type.
     */
    public void setType(String type) {
        this.type = type;
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
