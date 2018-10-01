/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.MetadataProfile;

import java.util.List;

/**
 * Service for handling metadata in its various permutations.
 */
public interface MetadataManager {

    /**
     * Returns all eTUFF metadata profiles.
     *
     * @return  All eTUFF metadata profiles.
     */
    public List<MetadataProfile> getMetadataProfile(String metadataProfile);

    /**
     * Finds and returns all the metadata profiles corresponding to the given ID
     * that have the given metadataType value.
     *
     * @param id    The unique ID associated with the corresponding WizardData object.
     * @param metadataType  The metadataType of interest (variable or global).
     * @return  A list of all matching metadata profiles.
     */
    public List<MetadataProfile> getMetadataProfiles(String id, String metadataType);
}
