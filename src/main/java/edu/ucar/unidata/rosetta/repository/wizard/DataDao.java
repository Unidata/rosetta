/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.Data;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * The data access object representing data.
 *
 * @author oxelson@ucar.edu
 */
public interface DataDao {

  /**
   * Looks up and retrieves a persisted Data object using the given id.
   *
   * @param id The id of the Data object.
   * @return The Data object corresponding to the given id.
   * @throws DataRetrievalFailureException If unable to lookup Data with the given id.
   */
  public Data lookupById(String id) throws DataRetrievalFailureException;

  /**
   * Persists the information in the given data object.
   *
   * @param data The Data object to persist.
   * @throws DataRetrievalFailureException If unable to persist the Data object.
   */
  public void persistData(Data data) throws DataRetrievalFailureException;

  /**
   * Updated the information corresponding to the given data object.
   *
   * @param data The data object to update.
   * @throws DataRetrievalFailureException If unable to update persisted Data object.
   */
  public void updatePersistedData(Data data) throws DataRetrievalFailureException;

  /**
   * Deletes the persisted data object information.
   *
   * @param id The id of the Data object to delete.
   * @throws DataRetrievalFailureException If unable to delete persisted Data object.
   */
  public void deletePersistedData(String id) throws DataRetrievalFailureException;
}
