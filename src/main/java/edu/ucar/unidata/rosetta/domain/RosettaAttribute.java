/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain;

import java.util.Objects;

public class RosettaAttribute {

    private String name;
    private String value;
    private String type;

    /**
     * Returns the name of the attribute.
     *
     * @return The attribute name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the attribute
     *
     * @param name name of the attribute
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the value of the attribute. Value is always a string. See {@link #type} to determine
     * the datatype of the #value
     *
     * @return The value as a string.
     * @see #type
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value of the attribute
     *
     * @param value value of the attribute, encoded as a string
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get the data type of the {@link #value} associated with the attribute
     *
     * @return type data type of the value
     */
    public String getType() {
        return type;
    }

    /**
     * Set the data type of the attribute value.
     *
     * @param type string representation of the data type of the attribute
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * noop constructor
     */
    public RosettaAttribute() {
    }

    /**
     * Hold information about an attribute
     *
     * @param name  name of the attribute
     * @param value value of the attribute
     * @param type  data type of the attribute
     */
    public RosettaAttribute(String name, String value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    /**
     * Override equals() for RosettaAttribute.
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) return true;
        if (!(obj instanceof RosettaAttribute)) {
            return false;
        }

        RosettaAttribute ra = (RosettaAttribute) obj;

        return Objects.equals(name, ra.getName()) &&
                Objects.equals(value, ra.getValue()) &&
                Objects.equals(type, ra.getType());
    }

    /**
     * Override Object.hashCode() to implement equals.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, value, type);
    }

}