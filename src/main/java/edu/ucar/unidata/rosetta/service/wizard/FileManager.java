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
   * Compresses the contents of a directory.
   *
   * @param directoryToCompress The path to the directory to compress.
   * @param fileName The name of the compressed file.
   * @throws RosettaFileException If unable to compress the contents of the directory.
   */
  public void compress(String directoryToCompress, String fileName) throws RosettaFileException;

  /**
   * Creates a subdirectory in the Rosetta download directory with the name of the provided unique
   * ID, into which converted data files and Rosetta templates will be stashed and made available
   * for download by the user.
   *
   * @param downloadDir The path to the Rosetta download directory.
   * @param id The unique ID that will become the name of the subdirectory.
   * @return The full path name to the created download subdirectory.
   * @throws RosettaFileException If unable to create download subdirectory.
   */
  public String createDownloadSubDirectory(String downloadDir, String id)
      throws RosettaFileException;

  /**
   * Creates a subdirectory in the Rosetta upload directory with the name of the provided unique ID,
   * into which uploaded files with be stashed.
   *
   * @param uploadDir The path to the Rosetta upload directory.
   * @param id The unique ID that will become the name of the subdirectory.
   * @return The full path name to the created upload subdirectory.
   * @throws RosettaFileException If unable to create upload subdirectory.
   */
  public String createUploadSubDirectory(String uploadDir, String id) throws RosettaFileException;

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
   * Uncompresses the provided file into the given directory.
   *
   * @param uploadDirPath The path to the uploads directory.
   * @param id The unique id associated with the file (a subdirectory in the uploads directory).
   * @param fileName The data file to uncompress.
   * @throws RosettaFileException If unable to uncompress data file.
   */
  public void uncompress(String uploadDirPath, String id, String fileName)
      throws RosettaFileException;

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
   * Creates a subdirectory in the designated uploads directory using the (unique) id and writes the
   * given file to the uploads subdirectory.
   *
   * @param uploadDirPath The path to the uploads directory.
   * @param id The unique id associated with the file (a subdirectory in the uploads directory).
   * @param fileName The name of the file to save to disk.
   * @param file The CommonsMultipartFile to save to disk.
   * @return The name of the saved file on disk (can be different than the downloaded file).
   * @throws RosettaFileException If unable to write file to disk.
   */
  public String writeUploadedFileToDisk(String uploadDirPath, String id, String fileName,
      CommonsMultipartFile file) throws RosettaFileException;
}


