/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.CustomFileAttributes;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.repository.wizard.CustomFileAttributesDao;
import edu.ucar.unidata.rosetta.repository.wizard.UploadedFileDao;
import edu.ucar.unidata.rosetta.util.PropertyUtils;
import javax.annotation.Resource;
import org.apache.commons.io.FilenameUtils;

public class CustomFileAttributesManagerImpl implements CustomFileAttributesManager {

  private CustomFileAttributesDao customFileAttributesDao;
  private UploadedFileDao uploadedFileDao;

  @Resource(name = "fileManager")
  private FileManager fileManager;

  /**
   * Looks up and retrieves a custom file attributes using the given id.
   *
   * @param id  The ID corresponding to the data to retrieve.
   * @return  The persisted custom file attribute data.
   */
  @Override
  public CustomFileAttributes lookupPersistedDataById(String id) {
    CustomFileAttributes customFileAttributes = customFileAttributesDao.lookupById(id);
    // Handle the no header lines value.
    if (customFileAttributes.getHeaderLineNumbers() == null) {
      customFileAttributes.setNoHeaderLines(true);
    }
    return customFileAttributes;
  }

  /**
   * Retrieves the data file from disk and parses it by line, converting it into a JSON string.
   *
   * @param id The unique id associated with the file (a subdir in the uploads directory).
   * @return A JSON String of the file data parsed by line.
   * @throws RosettaFileException For any file I/O or JSON conversions problems.
   */
  @Override
  public String parseDataFileByLine(String id) throws RosettaFileException {
    UploadedFile dataFile = uploadedFileDao.lookupDataFileById(id);
    String filePath = FilenameUtils
        .concat(FilenameUtils.concat(PropertyUtils.getUploadDir(), id), dataFile.getFileName());
    return fileManager.parseByLine(filePath);
  }

  /**
   * Processes the data submitted by the user containing custom data file attributes.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param customFileAttributes The CustomFileAttributes containing user-submitted data.
   */
  @Override
  public void processCustomFileTypeAttributes(String id, CustomFileAttributes customFileAttributes) {
    // Handle the no header lines value.
    if (customFileAttributes.isNoHeaderLines()) {
      customFileAttributes.setHeaderLineNumbers(null);
    }

    // Technically, the database entry for this data already exists from file upload step.
    // We just need to add/update the header line number and delimiter values.
    customFileAttributesDao.updatePersistedData(id, customFileAttributes);
  }

  /**
   * Sets the data access object (DAO) for the CustomFileAttributes object.
   *
   * @param customFileAttributesDao The service DAO representing a CustomFileAttributes object.
   */
  public void setCustomFileAttributesDao(CustomFileAttributesDao customFileAttributesDao) {
    this.customFileAttributesDao = customFileAttributesDao;
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
