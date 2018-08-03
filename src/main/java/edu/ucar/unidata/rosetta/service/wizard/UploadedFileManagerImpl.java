package edu.ucar.unidata.rosetta.service.wizard;

import javax.annotation.Resource;

import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.repository.wizard.UploadedFileDao;
import edu.ucar.unidata.rosetta.util.PropertyUtils;
import org.apache.commons.lang3.StringUtils;


public class UploadedFileManagerImpl implements UploadedFileManager {

  private UploadedFileDao uploadedFileDao;

  @Resource(name = "fileManager")
  private FileManager fileManager;

  /**
   * Looks up and retrieves a uploaded file data using the given id.
   *
   * @param id  The ID corresponding to the data to retrieve.
   * @return  The persisted uploaded file data.
   */
  @Override
  public UploadedFileCmd lookupPersistedDataById(String id) {
    return uploadedFileDao.lookupById(id);
  }

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

    // Save files to disk.
    for (UploadedFile uploadedFile : uploadedFileCmd.getUploadedFiles()) {
      if(StringUtils.trimToNull(uploadedFile.getFileName()) != null) {
        // Write data file to disk.
        fileManager.writeUploadedFileToDisk(PropertyUtils.getUploadDir(),
            id, uploadedFile.getFileName(), uploadedFile.getFile());
      }
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
