package edu.ucar.unidata.rosetta.service.wizard;

import org.apache.commons.io.FilenameUtils;

import javax.annotation.Resource;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.repository.wizard.UploadedFileDao;
import edu.ucar.unidata.rosetta.util.PropertyUtils;

public class UploadedFileManagerImpl implements UploadedFileManager {

  private UploadedFileDao uploadedFileDao;

  @Resource(name = "fileManager")
  private FileManager fileManager;

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
  @Override
  public void processFileUpload(String id, UploadedFileCmd uploadedFileCmd) throws RosettaFileException {

    // Get the persisted data corresponding to this ID.
    UploadedFileCmd persistedData = uploadedFileDao.lookupById(id);

    // Do we have persisted data to update?
    if (!persistedData.getUploadedFiles().isEmpty() && persistedData.getDataFileType() != null) {
      // Persisted uploaded file data exists.

      // Update persisted data.
      uploadedFileDao.updatePersistedData(id, uploadedFileCmd);

    } else {
      // No persisted uploaded file data.
      uploadedFileDao.persistData(id, uploadedFileCmd);
    }

    for (UploadedFile uploadedFile : uploadedFileCmd.getUploadedFiles()) {
      // Write data file to disk.
      fileManager.writeUploadedFileToDisk(PropertyUtils.getUploadDir(),
              id, uploadedFile.getFileName(), uploadedFile.getFile());
    }
  }

  /**
   * Sets the data access object (DAO) for the UploadedFile object.
   *
   * @param uploadedFileDao The service DAO representing a UploadedFile object.
   */
  public void setUploadedFileDao(UploadedFileDao uploadedFileDao) {
    this.uploadedFileDao = uploadedFileDao;
  }

}
