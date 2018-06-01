package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 * Service for handling files and parsing file data.
 *
 * @author oxelson@ucar.edu
 */
public interface FileManager {

    /**
     * Converts .xls and .xlsx files to .csv files.
     *
     * @param uploadDirPath The path to the uploads directory.
     * @param id        The unique id associated with the file (a subdir in the uploads directory).
     * @param fileName  The name of the .xls or .xlsx file to convert.
     * @return  The name of the converted .csv file.
     * @throws RosettaDataException If unable to convert xls/xlsx file to csv.
     */
    public String convertToCSV(String uploadDirPath, String id, String fileName) throws RosettaDataException;

    /**
     * A simple method that reads each line of a file, and looks for blank lines.
     * Blank line = empty, only whitespace, or null (as per StringUtils).
     *
     * @param file The path to the file on disk.
     * @return The number of blank lines in the file.
     */
    public int getBlankLines(File file);

    /**
     * Creates a list of header lines (Strings) from the data file useful for AsciiFile
     * (custom file type) for netCDF conversion.  TODO: refactor to remove need for AsciiFile.
     *
     * @param filePath  The path to the data file to parse.
     * @param headerLineList The persisted header line numbers declared by the user.
     * @return  A list of the header lines from the data file.
     * @throws IOException If unable to retrieve the header lines from the file.
     */
    public List<String>getHeaderLinesFromFile(String filePath, List<String> headerLineList) throws IOException;

    /**
     * Creates a list of header lines (Strings) from the data file useful for AsciiFile
     * (custom file type) for netCDF conversion.
     *
     * @param filePath  The path to the data file to parse.
     * @param headerLineList A list of the header lines.
     * @param delimiter The delimiter to parse the non-header line data.
     * @return  The parsed file data in List<List<String>> format (inner list is tokens of each line parsed by delimiter).
     * @throws IOException  If unable to parse the file.
     */
    public List<List<String>> parseByDelimiter(String filePath, List<String> headerLineList, String delimiter) throws IOException;

    /**
     * A simple method that reads each line of a file, appends a new line
     * character & adds to a List. The list is then turned into a JSON string.
     *
     * @param filePath The path to the file on disk.
     * @return A JSON String of the file data parsed by line.
     * @throws IOException For any file I/O or JSON conversions problems.
     */
    public String parseByLine(String filePath) throws IOException;

    /**
     * Creates a subdirectory in the designated uploads directory using the (unique) id
     * and writes the given file to the uploads subdirectory.
     *
     * @param uploadDirPath The path to the uploads directory.
     * @param id        The unique id associated with the file (a subdir in the uploads directory).
     * @param fileName  The name of the file to save to disk.
     * @param file      The CommonsMultipartFile to save to disk.
     * @return fileName The name of the saved file on disk (can be different than the downloaded file).
     * @throws SecurityException  If unable to write file to disk because of a JVM security manager violation.
     * @throws IOException  If unable to write file to disk.
     * @throws RosettaDataException  If a file conversion exception occurred.
     */
    public String writeUploadedFileToDisk(String uploadDirPath, String id, String fileName, CommonsMultipartFile file) throws SecurityException, IOException, RosettaDataException;
}


