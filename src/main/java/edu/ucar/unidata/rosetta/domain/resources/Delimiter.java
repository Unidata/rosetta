package edu.ucar.unidata.rosetta.domain.resources;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author oxelson@ucar.edu
 */
public class Delimiter extends RosettaResource {

    private int id;
    private String characterSymbol;

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

    public String getCharacterSymbol() {
        return characterSymbol;
    }

    public void setCharacterSymbol(String characterSymbol) {
        this.characterSymbol = characterSymbol;
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
