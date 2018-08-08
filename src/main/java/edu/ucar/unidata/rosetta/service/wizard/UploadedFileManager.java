/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;

public interface UploadedFileManager {

  /**
   * Looks up and retrieves a uploaded file data using the given id.
   *
   * @param id  The ID corresponding to the data to retrieve.
   * @return  The persisted uploaded file data.
   */
  public UploadedFileCmd lookupPersistedDataById(String id);

  /**
   * Processes the data submitted by the user containing uploaded file information. Writes the
   * uploaded files to disk. Updates the persisted data corresponding to the provided unique ID
   * with the uploaded file information.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param uploadedFileCmd The user-submitted data containing the uploaded file information.
   * @throws RosettaFileException If unable to write file(s) to disk or a file conversion exception
   * occurred.
   */
  public void processFileUpload(String id, UploadedFileCmd uploadedFileCmd) throws RosettaFileException;
}
