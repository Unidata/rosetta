/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.MetadataProfile;

import java.util.List;

/**
 * The data access object for metadata profiles.
 */
public interface MetadataProfileDao {

    /**
     * Retrieves the compliance level for the give attribute.
     *
     * @param attributeName  The name of the attribute.
     * @param profile        The metadata profile.
     * @return  The compliance level of the attribute.
     */
    public String getComplianceLevelForAttribute(String attributeName, String profile);

    /**
     * Retrieves the metadata profile attributes to ignore in the wizard interface.
     *
     * @return  A list of MetadataProfile objects containing the attributes to ignore.
     */
    public List<MetadataProfile> getIgnoredMetadataProfileAttributes();

    /**
     * Retrieves the persisted metadata profile associated with the given type.
     *
     * @param metadataProfileType  The metadata profile type.
     * @return  A list of MetadataProfile objects created from the persisted metadata profile data.
     */
    public List<MetadataProfile> getMetadataProfileByType(String metadataProfileType);
}
