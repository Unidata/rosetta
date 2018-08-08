/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile;
import org.springframework.dao.DataRetrievalFailureException;

import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;

public interface UploadedFileDao {

  /**
   * Looks up and retrieves persisted uploaded file data using the given id.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @return The UploadedFileCmd object corresponding to the given id.
   */
  public UploadedFileCmd lookupById(String id);

  /**
   * Looks up and retrieves persisted data file information using the given id.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @return The UploadedFile object for the data file.
   */
  public UploadedFile lookupDataFileById(String id);

  /**
   * Persists the uploaded file data.
   *
   * @param id The unique ID corresponding to the data.
   * @param uploadedFileCmd The UploadedFileCmd object to persist.
   * @throws DataRetrievalFailureException If unable to persist the uploaded file data..
   */
  public void persistData(String id, UploadedFileCmd uploadedFileCmd) throws DataRetrievalFailureException;

  /**
   * Updated the persisted uploaded file data with the given information.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param uploadedFileCmd The updated the persisted data.
   * @throws DataRetrievalFailureException If unable to update the uploaded file data..
   */
  public void updatePersistedData(String id, UploadedFileCmd uploadedFileCmd) throws DataRetrievalFailureException;
}
