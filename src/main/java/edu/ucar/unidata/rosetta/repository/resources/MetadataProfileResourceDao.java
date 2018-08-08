/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.MetadataProfile;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * The data access object representing a metadata profile.
 *
 * @author oxelson@ucar.edu
 */
public interface MetadataProfileResourceDao {

  /**
   * Looks up and retrieves a list of persisted MetadataProfile objects.
   *
   * @return A List of all persisted metadata profiles.
   * @throws DataAccessException If unable to retrieve persisted metadata profile.
   */
  public List<MetadataProfile> getMetadataProfiles() throws DataAccessException;

  /**
   * Looks up and retrieves a persisted MetadataProfile object using the provided name.
   *
   * @param name The name of the metadata profile to retrieve.
   * @return The MetadataProfile object matching the provided name.
   * @throws DataRetrievalFailureException If unable to retrieve persisted metadata profile.
   */
  public MetadataProfile lookupMetadataProfileByName(String name)
      throws DataRetrievalFailureException;


}
