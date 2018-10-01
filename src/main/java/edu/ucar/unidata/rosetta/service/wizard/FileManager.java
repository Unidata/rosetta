/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import java.io.File;
import java.util.List;

import org.springframework.web.multipart.commons.CommonsMultipartFile;


/**
 * Service for handling files and parsing file data.
 *
 * @author oxelson@ucar.edu
 */
public interface FileManager {

  /**
   * Creates a subdirectory in the Rosetta user_files directory with the name of the provided unique
   * ID, into which converted data files and Rosetta templates will be stashed and made available
   * for download by the user.
   *
   * @param userFilesDir The path to the Rosetta user files directory.
   * @param id The unique ID that will become the name of the subdirectory.
   * @return The full path name to the created user files subdirectory.
   * @throws RosettaFileException If unable to create user files subdirectory.
   */
  public String createUserFilesSubDirectory(String userFilesDir, String id)
          throws RosettaFileException;

  /**
   * A simple method that reads each line of a file, and looks for blank lines. Blank line = empty,
   * only whitespace, or null (as per StringUtils).
   *
   * @param file The path to the file on disk.
   * @return The number of blank lines in the file.
   * @throws RosettaFileException If unable to find blank lines from file.
   */
  public int getBlankLines(File file) throws RosettaFileException;

  /**
   * Creates a list of header lines (Strings) from the data file useful for AsciiFile (custom file
   * type) for netCDF conversion.  TODO: refactor to remove need for AsciiFile.
   *
   * @param filePath The path to the data file to parse.
   * @param headerLineList The persisted header line numbers declared by the user.
   * @return A list of the header lines from the data file.
   * @throws RosettaFileException If unable to retrieve the header lines from the file.
   */
  public List<String> getHeaderLinesFromFile(String filePath, List<String> headerLineList)
      throws RosettaFileException;

  /**
   * Retrieves the contents of a compressed file's inventory.
   *
   * @param filePath The path to the inventory file.
   * @return The compressed file contents.
   * @throws RosettaFileException If unable to get the inventory contents.
   */
  public List<String> getInventoryData(String filePath) throws RosettaFileException;

  /**
   * Creates a list of header lines (Strings) from the data file useful for AsciiFile (custom file
   * type) for netCDF conversion.
   *
   * @param filePath The path to the data file to parse.
   * @param headerLineList A list of the header lines.
   * @param delimiter The delimiter to parse the non-header line data.
   * @return The parsed file data in List<List<String>> format (inner list is tokens of each line
   * parsed by delimiter).
   * @throws RosettaFileException If unable to parse the file.
   */
  public List<List<String>> parseByDelimiter(String filePath, List<String> headerLineList,
      String delimiter) throws RosettaFileException;

  /**
   * A simple method that reads each line of a file, appends a new line character & adds to a List.
   * The list is then turned into a JSON string.
   *
   * @param filePath The path to the file on disk.
   * @return A JSON String of the file data parsed by line.
   * @throws RosettaFileException For any file I/O or JSON conversions problems.
   */
  public String parseByLine(String filePath) throws RosettaFileException;


  /**
   * Reads a serializable Data object stored on disk in the template file.
   *
   * @param filePathUploadDir the upload subdirectory in which to look for the template file.
   * @return The Data object read from disk.
   * @throws RosettaFileException If unable to read the Data object from the template file.
   */
  public Data readDataObject(String filePathUploadDir) throws RosettaFileException;

  /**
   * Writes a serializable Data object to disk in the template file.
   *
   * @param filePathDownloadDir The path to the download sub directory in which to write the
   * template file.
   * @param data The Data object write to disk.
   * @throws RosettaFileException If unable to write the Data object to the template file.
   */
  public void writeDataObject(String filePathDownloadDir, Data data) throws RosettaFileException;

  /**
   * Creates a subdirectory in the designated user files directory using the (unique) id and writes the
   * given file to the user files subdirectory.
   *
   * @param userFilesDirPath The path to the user files directory.
   * @param id The unique id associated with the file (a subdirectory in the user files directory).
   * @param fileName The name of the file to save to disk.
   * @param file The CommonsMultipartFile to save to disk.
   * @return The name of the saved file on disk (can be different than the downloaded file).
   * @throws SecurityException If unable to write file to disk because of a JVM security manager
   * violation.
   * @throws RosettaFileException If unable to write file to disk.
   * @throws RosettaFileException If a file conversion exception occurred.
   */
  public String writeUploadedFileToDisk(String userFilesDirPath, String id, String fileName,
                                        CommonsMultipartFile file) throws RosettaFileException;
}


