package edu.ucar.unidata.rosetta.domain.resources;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CfType extends RosettaResource {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return super.getName();
    }

    public void setName(String name) {
        super.setName(name);
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
