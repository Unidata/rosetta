/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Community;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * The data access object representing a community.
 *
 * @author oxelson@ucar.edu
 */
public interface CommunityResourceDao {

  /**
   * Looks up and retrieves a list of persisted Community objects.
   *
   * @return A List of all persisted communities.
   * @throws DataAccessException If unable to retrieve persisted communities.
   */
  public List<Community> getCommunities() throws DataAccessException;

  /**
   * Looks up and retrieves a persisted Community object using the provided name.
   *
   * @param name The name of the community to retrieve.
   * @return The Community object matching the provided name.
   * @throws DataRetrievalFailureException If unable to retrieve persisted community.
   */
  public Community lookupCommunityByName(String name) throws DataRetrievalFailureException;

  /**
   * Lookups and returns a list of persisted Community objects using the provided file type.
   *
   * @param fileType The file type of the community to retrieve.
   * @return A List of persisted Communities matching the provided file type.
   * @throws DataRetrievalFailureException If unable to retrieve persisted Communities.
   */
  public List<Community> lookupCommunitiesByFileType(String fileType) throws DataRetrievalFailureException;

}
