/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.MetadataProfile;
import java.util.List;

/**
 * The data access object for metadata profiles.
 */
public interface MetadataProfileDao {


  public List<MetadataProfile> getMetadataProfileByType(String metadataProfileType);

}
