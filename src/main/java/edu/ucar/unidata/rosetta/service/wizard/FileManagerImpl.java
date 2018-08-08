package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.util.JsonUtil;
import edu.ucar.unidata.rosetta.util.XlsToCsvUtil;
import edu.ucar.unidata.rosetta.util.ZipFileUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * Implements FileManager functionality.
 *
 * @author oxelson@ucar.edu
 */
public class FileManagerImpl implements FileManager {

  private static final Logger logger = Logger.getLogger(FileManagerImpl.class);


  /**
   * Compresses the contents of a directory.
   *
   * @param directoryToCompress The path to the directory to compress.
   * @param fileName The name of the compressed file.
   * @throws RosettaFileException If unable to compress the contents of the directory.
   */
  public void compress(String directoryToCompress, String fileName) throws RosettaFileException {
    ZipFileUtil.zip(directoryToCompress, FilenameUtils.removeExtension(fileName));
  }

  /**
   * Converts .xls and .xlsx files to .csv files.
   *
   * @param uploadDirPath The path to the uploads directory.
   * @param id The unique id associated with the file (a subdirectory in the uploads directory).
   * @param fileName The name of the .xls or .xlsx file to convert.
   * @return The name of the converted .csv file.
   * @throws RosettaFileException If unable to convert xls/xlsx file to csv.
   */
  private String convertToCSV(String uploadDirPath, String id, String fileName)
      throws RosettaFileException {
    // This check of file extension may seem superfluous, but we don't want to continue it it's not an xls* file.
    String extension = FilenameUtils.getExtension(fileName);
    if (!(extension.equals("xls") || extension.equals("xlsx"))) {
      throw new RosettaFileException(
          "Attempting to convert a non .xls type file to .csv format.");
    }

    String xlsFilePath = FilenameUtils.concat(FilenameUtils.concat(uploadDirPath, id), fileName);
    // Change the file on disk.
    boolean conversionSuccessful = XlsToCsvUtil.convert(xlsFilePath, null);
    String csvFileName;
    if (conversionSuccessful) {
      csvFileName = FilenameUtils.removeExtension(fileName) + ".csv";
    } else {
      throw new RosettaFileException("Unable to convert .xls type file to .csv format.");
    }

    return csvFileName;
  }

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
      throws RosettaFileException {
    String filePathDownloadDir = FilenameUtils.concat(downloadDir, id);

    // File-ize the download subdirectory.
    File localFileDir = new File(filePathDownloadDir);

    // Check to see if the download subdirectory has been created yet; if not, create it.
    if (!localFileDir.exists()) {
      if (!localFileDir.mkdirs()) {
        throw new RosettaFileException(
            "Unable to create " + id + " subdirectory in download directory.");
      }
    }

    return filePathDownloadDir;
  }

  /**
   * Creates a subdirectory in the Rosetta upload directory with the name of the provided unique ID,
   * into which uploaded files with be stashed.
   *
   * @param uploadDir The path to the Rosetta upload directory.
   * @param id The unique ID that will become the name of the subdirectory.
   * @return The full path name to the created upload subdirectory.
   * @throws RosettaFileException If unable to create upload subdirectory.
   */
  public String createUploadSubDirectory(String uploadDir, String id) throws RosettaFileException {
    String filePathUploadDir = FilenameUtils.concat(uploadDir, id);

    // File-ize the upload subdirectory.
    File localFileDir = new File(filePathUploadDir);

    // Check to see if the upload subdirectory has been created yet; if not, create it.
    if (!localFileDir.exists()) {
      if (!localFileDir.mkdirs()) {
        throw new RosettaFileException(
            "Unable to create " + id + " subdirectory in upload directory.");
      }
    }

    return filePathUploadDir;
  }


  /**
   * A simple method that reads each line of a file, and looks for blank lines. Blank line = empty,
   * only whitespace, or null (as per StringUtils).
   *
   * @param file The path to the file on disk.
   * @return The number of blank lines in the file.
   * @throws RosettaFileException If unable to find blank lines from file.
   */
  @Override
  public int getBlankLines(File file) throws RosettaFileException {
    String currentLine;
    int blankLineCount = 0;
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      while ((currentLine = reader.readLine()) != null) {
        if (StringUtils.isBlank(currentLine)) {
          blankLineCount++;
        }
      }
    } catch (IOException e) {
      throw new RosettaFileException("Unable to find blank lines from file: " + e);
    }
    return blankLineCount;
  }

  /**
   * Creates a list of header lines (Strings) from the data file useful for AsciiFile (custom file
   * type) for netCDF conversion.  TODO: refactor to remove need for AsciiFile.
   *
   * @param filePath The path to the data file to parse.
   * @param headerLineList The persisted header line numbers declared by the user.
   * @return A list of the header lines from the data file.
   * @throws RosettaFileException If unable to retrieve the header lines from the file.
   */
  @Override
  public List<String> getHeaderLinesFromFile(String filePath, List<String> headerLineList)
      throws RosettaFileException {
    List<String> headerData = new ArrayList<>();
    int lineCount = 0;
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
      String currentLine;
      while ((currentLine = bufferedReader.readLine()) != null) {
        if (StringUtils.isNotBlank(currentLine)) {
          // Get the header lines.
          if (headerLineList.contains(String.valueOf(lineCount))) {
            headerData.add(currentLine);
          }
        }
        lineCount++;
      }
    } catch (IOException e) {
      throw new RosettaFileException("Unable to find header lines from file: " + e);
    }
    return headerData;
  }

  /**
   * Retrieves the contents of a compressed file's inventory.
   *
   * @param filePath The path to the inventory file.
   * @return The compressed file contents.
   * @throws RosettaFileException If unable to get the inventory contents.
   */
  public List<String> getInventoryData(String filePath) throws RosettaFileException {
    return ZipFileUtil.getInventory(filePath);
  }

  /**
   * Creates a list of header lines (Strings) using streams and lambdas from the data file useful
   * for custom file type for netCDF conversion.
   *
   * @param filePath The path to the data file to parse.
   * @param headerLineList A list of the header lines.
   * @param delimiter The delimiter to parse the non-header line data.
   * @return The parsed file data in List<List<String>> format (inner list is tokens of each line
   * parsed by delimiter).
   * @throws RosettaFileException If unable to parse the file.
   */
  @Override
  public List<List<String>> parseByDelimiter(String filePath, List<String> headerLineList,
      String delimiter) throws RosettaFileException {

    List<List<String>> parsedData = new ArrayList<>();

    try (LineNumberReader lineNumberReader = new LineNumberReader(
        Files.newBufferedReader(Paths.get(filePath)))) {
      lineNumberReader.lines() // Turns this into a Stream.
          .filter(StringUtils::isNotBlank)  // Filter out blank lines.
          .filter(line -> !headerLineList.contains(
              String.valueOf(lineNumberReader.getLineNumber() - 1))) // Filter out header lines.
          .map(line -> line.split(delimiter)) // Tokenize the line using provided delimiter.
          .forEach(tokenizedLineData -> parsedData
              .add(Arrays.asList(tokenizedLineData))); // Add tokenized line data to list.

    } catch (IOException e) {
      throw new RosettaFileException("Unable to parse file by delimiter: " + e);
    }
    return parsedData;
  }

  /**
   * A simple method that reads each line of a file, appends a new line character & adds to a List.
   * The list is then turned into a JSON string.
   *
   * @param filePath The path to the file on disk.
   * @return A JSON String of the file data parsed by line.
   * @throws RosettaFileException For any file I/O or JSON conversions problems.
   */
  @Override
  public String parseByLine(String filePath) throws RosettaFileException {
    String jsonFileData;

    List<String> fileContents = new ArrayList<>();
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
      String currentLine;
      while ((currentLine = bufferedReader.readLine()) != null) {
        if (StringUtils.isNotBlank(currentLine)) {
          fileContents.add(StringEscapeUtils.escapeHtml4(currentLine));
        }
      }
      jsonFileData = JsonUtil.mapObjectToJSON(fileContents);
    } catch (IOException e) {
      throw new RosettaFileException("Unable to parse file by line: " + e);
    }
    return jsonFileData;
  }

  /**
   * Reads a serializable Data object stored on disk in the template file.
   *
   * @param filePathUploadDir the upload subdirectory in which to look for the template file.
   * @return The Data object read from disk.
   * @throws RosettaFileException If unable to read the Data object from the template file.
   */
  public Data readDataObject(String filePathUploadDir) throws RosettaFileException {
    Data data;
    String template = FilenameUtils.concat(filePathUploadDir, "rosetta.template");
    try (ObjectInputStream objectInputStream = new ObjectInputStream(
        new FileInputStream(template))) {
      data = (Data) objectInputStream.readObject();
    } catch (IOException | ClassNotFoundException | SecurityException e) {
      throw new RosettaFileException("Unable to write Data object: " + e);
    }
    return data;
  }

  /**
   * Uncompress the provided file into the given directory.
   *
   * @param uploadDirPath The path to the uploads directory.
   * @param id The unique id associated with the file (a subdirectory in the uploads directory).
   * @param fileName The data file to uncompress.
   * @throws RosettaFileException If unable to uncompress data file.
   */
  public void uncompress(String uploadDirPath, String id, String fileName)
      throws RosettaFileException {
    ZipFileUtil.unZip(fileName, FilenameUtils.concat(uploadDirPath, id));
  }

  /**
   * Writes a serializable Data object to disk in the template file.
   *
   * @param filePathDownloadDir The path to the download subdirectory in which to write the template
   * file.
   * @param data The Data object write to disk.
   * @throws RosettaFileException If unable to write the Data object to the template file.
   */
  public void writeDataObject(String filePathDownloadDir, Data data) throws RosettaFileException {

    String downloadableTemplate = FilenameUtils.concat(filePathDownloadDir, "rosetta.template");
    try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
        new FileOutputStream(downloadableTemplate))) {
      objectOutputStream.writeObject(data);
    } catch (IOException | SecurityException e) {
      throw new RosettaFileException("Unable to write template object: " + e);
    }
  }

  /**
   * Creates a subdirectory in the designated uploads directory using the (unique) id and writes the
   * given file to the uploads subdirectory.
   *
   * @param uploadDirPath The path to the uploads directory.
   * @param id The unique id associated with the file (a subdirectory in the uploads directory).
   * @param fileName The name of the file to save to disk.
   * @param file The CommonsMultipartFile to save to disk.
   * @return The name of the saved file on disk (can be different than the downloaded file).
   * @throws SecurityException If unable to write file to disk because of a JVM security manager
   * violation.
   * @throws RosettaFileException If unable to write file to disk.
   * @throws RosettaFileException If a file conversion exception occurred.
   */
  @Override
  public String writeUploadedFileToDisk(String uploadDirPath, String id, String fileName,
      CommonsMultipartFile file) throws RosettaFileException {

    // Create full file path to upload subdirectory.
    String filePathUploadDir = createUploadSubDirectory(uploadDirPath, id);

    logger.info("Writing uploaded file " + fileName + " to disk");
    File uploadedFile = new File(FilenameUtils.concat(filePathUploadDir, fileName));

    try (FileOutputStream outputStream = new FileOutputStream(uploadedFile)) {
      outputStream.write(file.getFileItem().get());
    } catch (IOException e) {
      throw new RosettaFileException("Unable write uploaded file to disk: " + e);
    }

    // May need to process the uploaded file depending on its type (.zip, excel file, etc.).
    String dataFileNameExtension = FilenameUtils.getExtension(fileName);

    // If the uploaded file was .xls or .xlsx, convert it to .csv
    if (dataFileNameExtension.equals("xls") || dataFileNameExtension.equals("xlsx")) {
      fileName = convertToCSV(uploadDirPath, id, fileName);
    }

    return fileName;
  }
}
