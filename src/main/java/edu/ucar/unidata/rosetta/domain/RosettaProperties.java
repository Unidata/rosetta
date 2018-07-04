package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An object that represents an arbitrary rosetta configuration property.
 * This object is used to retrieve persisted property values.
 *
 * @author oxelson@ucar.edu
 */
public class RosettaProperties {

    private int id;
    private String propertyKey;
    private String propertyValue;

    /**
     * Returns the ID of the persisted property value.
     *
     * @return The ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the persisted property value.
     *
     * @param id The ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the property key.
     *
     * @return The property key.
     */
    public String getPropertyKey() {
        return propertyKey;
    }

    /**
     * Sets the property key.
     *
     * @param propertyKey The property key.
     */
    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * Returns the property value.
     *
     * @return The property value.
     */
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * Sets the property value.
     *
     * @param propertyValue The property value.
     */
    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    /**
     * Returns the string representation of this object.
     *
     * @return The string representation of this object.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

