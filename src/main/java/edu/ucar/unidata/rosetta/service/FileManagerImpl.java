package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.converters.XlsToCsv;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.util.JsonUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import ucar.unidata.util.StringUtil2;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Implements FileManager functionality.
 *
 * @author oxelson@ucar.edu
 */
public class FileManagerImpl implements FileManager {

    protected static Logger logger = Logger.getLogger(FileManagerImpl.class);

    /**
     * Converts .xls and .xlsx files to .csv files.
     *
     * @param uploadDirPath The path to the uploads directory.
     * @param id        The unique id associated with the file (a subdir in the uploads directory).
     * @param fileName  The name of the .xls or .xlsx file to convert.
     * @return  The name of the converted .csv file.
     * @throws RosettaDataException If unable to convert xls/xlsx file to csv.
     */
    @Override
    public String convertToCSV(String uploadDirPath, String id, String fileName) throws RosettaDataException {
        String extension = FilenameUtils.getExtension(fileName);
        if (!(extension.equals("xls") || extension.equals("xlsx")))
            throw new RosettaDataException("Attempting to convert a non .xls type file to .csv format.");

        String xlsFilePath = FilenameUtils.concat(FilenameUtils.concat(uploadDirPath, id), fileName);
        // Change the file on disk.
        boolean conversionSuccessful = XlsToCsv.convert(xlsFilePath, null);
        String csvFileName = null;
        if (conversionSuccessful)
            csvFileName = FilenameUtils.removeExtension(fileName) + ".csv";
        else
            throw new RosettaDataException("Unable to convert .xls type file to .csv format.");

        return csvFileName;
    }


    /**
     * A simple method that reads each line of a file, and looks for blank lines.
     * Blank line = empty, only whitespace, or null (as per StringUtils).
     *
     * @param file The path to the file on disk.
     * @return The number of blank lines in the file.
     */
    @Override
    public int getBlankLines(File file) {
        String currentLine;
        int blankLineCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while ((currentLine = reader.readLine()) != null) {
                if (StringUtils.isBlank(currentLine)) {
                    blankLineCount++;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return blankLineCount;
    }

    /**
     * Creates a list of header lines (Strings) from the data file useful for AsciiFile 
     * (custom file type) for netCDF conversion.  TODO: refactor to remove need for AsciiFile.
     * 
     * @param filePath  The path to the data file to parse.
     * @param headerLineList The persisted header line numbers declared by the user.
     * @return  A list of the header lines from the data file.
     * @throws IOException If unable to retrieve the header lines from the file.
     */
    @Override
    public List<String> getHeaderLinesFromFile(String filePath, List<String> headerLineList) throws IOException {
        List<String> headerData = new ArrayList<>();
        int lineCount = 0;
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
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
        }
        return headerData;
    }

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
    @Override
    public List<List<String>> parseByDelimiter(String filePath, List<String> headerLineList, String delimiter) throws IOException {
        List<List<String>> parsedData = new ArrayList<>();
        int lineCount = 0;
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (StringUtils.isNotBlank(currentLine)) {
                    // Ignore the header lines.
                    if (!headerLineList.contains(String.valueOf(lineCount))) {
                        // Parse line data based on delimiter.
                        if (delimiter.equals(" ")) {
                            // This will use ANY white space, variable number spaces, tabs, etc. as
                            // the delimiter...not that the delimiter is " ", and is defined
                            // in the convertDelimiters of FileParserManagerimpl.java.
                            //
                            // This special case also needs to be handled by variableSpecification.js
                            // in the gridForVariableSpecification function.
                            String[] tokens = StringUtil2.splitString(currentLine);
                            List<String> valList = Arrays.asList(tokens);
                            // Add tokenized line data to outer list.
                            parsedData.add(valList);
                        } else { // all other delimiters
                            // Tokenize the line using the delimiter.
                            String[] lineComponents = currentLine.split(delimiter);
                            List<String> list = new ArrayList<>(Arrays.asList(lineComponents));
                            // Add tokenized line data to outer list.
                            parsedData.add(list);
                        }
                    }
                }
                lineCount++;
            }
        }
        return parsedData;
    }

    /**
     * A simple method that reads each line of a file, appends a new line
     * character & adds to a List. The list is then turned into a JSON string.
     *
     * @param filePath The path to the file on disk.
     * @return A JSON String of the file data parsed by line.
     * @throws IOException For any file I/O or JSON conversions problems.
     */
    @Override
    public String parseByLine(String filePath) throws IOException {
        List<String> fileContents = new ArrayList<>();
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (StringUtils.isNotBlank(currentLine)) {
                    fileContents.add(StringEscapeUtils.escapeHtml4(currentLine));
                }
            }
        }
        return JsonUtil.mapObjectToJSON(fileContents);
    }

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
    @Override
    public String writeUploadedFileToDisk(String uploadDirPath, String id, String fileName, CommonsMultipartFile file) throws SecurityException, IOException, RosettaDataException {

        String filePath = FilenameUtils.concat(uploadDirPath, id);
        File localFileDir = new File(filePath);

        if (!localFileDir.exists())
            if (!localFileDir.mkdirs())
                throw new IOException("Unable to create " + id + " subdirectory in uploads directory.");

        logger.info("Writing uploaded file " + fileName + " to disk");
        File uploadedFile = new File(FilenameUtils.concat(filePath, fileName));

        FileOutputStream outputStream = new FileOutputStream(uploadedFile);
        outputStream.write(file.getFileItem().get());
        outputStream.flush();
        outputStream.close();

        // If the uploaded file was .xls or .xlsx, convert it to .csv
        String dataFileNameExtension = FilenameUtils.getExtension(fileName);
        if (dataFileNameExtension.equals("xls") || dataFileNameExtension.equals("xlsx"))
            fileName = convertToCSV(uploadDirPath, id, fileName);

        return fileName;
    }
}
