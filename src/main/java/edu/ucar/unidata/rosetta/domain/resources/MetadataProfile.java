package edu.ucar.unidata.rosetta.domain.resources;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;



/**
 * Object representing a metadata profile resource (see WEB-INF/classes/resources/metadataProfiles.xml).
 * Data from the resource XML file is used to populate this object which is then
 * persisted in the database for quick retrieval.
 *
 * @author oxelson@bu.edu
 */
public class MetadataProfile extends RosettaResource {

    private List<Community> communities;
    private int id;

    /**
     * Adds a community to the list of communities.
     *
     * @param community  The community to add.
     */
    public void addToFileType(Community community) {
        this.communities.add(community);
    }

    /**
     * Returns the list of communities associated with this resource.
     *
     * @return  The communities.
     */
    public List<Community> getCommunities() {
        return communities;
    }

    /**
     * Returns the persistence-layer ID associated with this resource.
     *
     * @return The ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the list of communities associated with this resource.
     *
     * @param communities  The communities.
     */
    public void setCommunities(List<Community> communities) {
        this.communities = communities;
    }

    /**
     * Sets the persistence-layer ID associated with this resource.
     *
     * @param id The ID.
     */
    public void setId(int id) {
        this.id = id;
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