/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.CustomFileAttributes;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;

public interface CustomFileAttributesManager {

  /**
   * Looks up and retrieves a custom file attributes using the given id.
   *
   * @param id  The ID corresponding to the data to retrieve.
   * @return  The persisted custom file attribute data.
   */
  public CustomFileAttributes lookupPersistedDataById(String id);

  /**
   * Retrieves the data file from disk and parses it by line, converting it into a JSON string.
   *
   * @param id The unique id associated with the file (a subdir in the uploads directory).
   * @return A JSON String of the file data parsed by line.
   * @throws RosettaFileException For any file I/O or JSON conversions problems.
   */
  public String parseDataFileByLine(String id) throws RosettaFileException;

  /**
   * Processes the data submitted by the user containing custom data file attributes.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param customFileAttributes The CustomFileAttributes containing user-submitted data.
   */
  public void processCustomFileTypeAttributes(String id, CustomFileAttributes customFileAttributes);
}
