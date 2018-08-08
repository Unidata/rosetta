/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Platform;
import java.util.List;
import org.springframework.dao.DataAccessException;

/**
 * The data access object representing a platform.
 *
 * @author oxelson@ucar.edu
 */
public interface PlatformResourceDao {

  /**
   * Looks up and retrieves a list of all persisted Platform objects.
   *
   * @return A List of all persisted Platforms.
   * @throws DataAccessException If unable to retrieve persisted Platforms.
   */
  public List<Platform> getPlatforms() throws DataAccessException;

  /**
   * Lookups and returns persisted Platform using the provided name.
   *
   * @param name The name of the platform to retrieve.
   * @return The Platform matching the provided name.
   * @throws DataAccessException If unable to retrieve persisted Platform.
   */
  public Platform lookupPlatformByName(String name) throws DataAccessException;

  /**
   * Lookups and returns a list of persisted Platform using the provided CF type.
   *
   * @param cfType The CF type of the platforms to retrieve.
   * @return A List of persisted Platforms matching the provided CF type.
   * @throws DataAccessException If unable to retrieve persisted Platforms.
   */
  public List<Platform> lookupPlatformsByCfType(String cfType) throws DataAccessException;

  /**
   * Lookups and returns a list of persisted Platform using the provided community.
   *
   * @param community The community of the platforms to retrieve.
   * @return A List of persisted Platforms matching the provided community.
   * @throws DataAccessException If unable to retrieve persisted Platforms.
   */
  public List<Platform> lookupPlatformsByCommunity(String community) throws DataAccessException;

}
