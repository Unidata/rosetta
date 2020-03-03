/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * The data access object representing a rosetta property.
 *
 * @author oxelson@ucar.edu
 */
public interface PropertiesDao {

  /**
   * Looks up and retrieves the persisted user files directory.
   *
   * @return The persisted user files directory.
   * @throws DataRetrievalFailureException If unable to lookup user files directory.
   */
  public String lookupUserFilesDirectory() throws DataRetrievalFailureException;
}
