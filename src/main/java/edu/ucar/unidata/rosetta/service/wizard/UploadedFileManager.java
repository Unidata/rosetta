package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;

public interface UploadedFileManager {

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
