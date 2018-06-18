package edu.ucar.unidata.rosetta.domain.wizard;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A POJO to hold CF Type data collected from the user in
 * the Rosetta application (acts as a form-backing-object).
 *
 * @author oxelson@ucar.edu
 */
public class CfType implements Serializable {

    private String id;
    private String cfType;
    private String community;
    private String platform;

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
     * Returns the cfType.
     *
     * @return  The cfType.
     */
    public String getCfType() {
        return cfType;
    }

    /**
     * Sets the cfType.
     *
     * @param cfType  The cfType.
     */
    public void setCfType(String cfType) {
        this.cfType = cfType;
    }

    /**
     * Returns the research community.
     *
     * @return  The community.
     */
    public String getCommunity() {
        return community;
    }

    /**
     * Sets the research community.
     *
     * @param community The user's community.
     */
    public void setCommunity(String community) {
        this.community = community;
    }

    /**
     * Returns the platform.
     *
     * @return  The platform.
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Sets the platform.
     *
     * @param platform  The platform.
     */
    public void setPlatform(String platform) {
        this.platform = platform;
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
