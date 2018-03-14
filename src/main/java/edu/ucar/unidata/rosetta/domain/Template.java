package edu.ucar.unidata.rosetta.domain;

/**
 * Object representing an Template.
 *
 * An arbitrary entity representing a Template.
 */
public class Template {

    private String name = null;

    /*
     * Returns the name of the template.
     *
     * @return  The template name.
     */
    public String getName() {
        return name;
    }

    /*
     * Sets the name of the template.
     *
     * @param name  The template name.
     */
    public void setName(String name) {
        this.name = name;
    }
}
