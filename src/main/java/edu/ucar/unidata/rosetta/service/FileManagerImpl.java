package edu.ucar.unidata.rosetta.service;


import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.util.*;

import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

import ucar.unidata.util.StringUtil2;

/**
 * Implements FileManager functionality.
 *
 * @author oxelson@ucar.edu
 */
public class FileManagerImpl implements FileManager {

    private static final Logger logger = Logger.getLogger(FileManagerImpl.class);

    /**
     * Converts .xls and .xlsx files to .csv files.
     *
     * @param uploadDirPath The path to the uploads directory.
     * @param id        The unique id associated with the file (a subdir in the uploads directory).
     * @param fileName  The name of the .xls or .xlsx file to convert.
     * @return  The name of the converted .csv file.
     * @throws RosettaFileException If unable to convert xls/xlsx file to csv.
     */
    private String convertToCSV(String uploadDirPath, String id, String fileName) throws RosettaFileException {
        // This check of file extension may seem superfluous, but we don't want to continue it it's not an xls* file.
        String extension = FilenameUtils.getExtension(fileName);
        if (!(extension.equals("xls") || extension.equals("xlsx")))
            throw new RosettaFileException("Attempting to convert a non .xls type file to .csv format.");

        String xlsFilePath = FilenameUtils.concat(FilenameUtils.concat(uploadDirPath, id), fileName);
        // Change the file on disk.
        boolean conversionSuccessful = XlsToCsvUtil.convert(xlsFilePath, null);
        String csvFileName;
        if (conversionSuccessful)
            csvFileName = FilenameUtils.removeExtension(fileName) + ".csv";
        else
            throw new RosettaFileException("Unable to convert .xls type file to .csv format.");

        return csvFileName;
    }

    /**
     * Creates a subdirectory in the Rosetta download directory with the name of the provided
     * unique ID, into which converted data files and Rosetta templates will be stashed and
     * made available for download by the user.
     *
     * @param downloadDir   The path to the Rosetta download directory.
     * @param id    The unique ID that will become the name of the subdirectory.
     * @return  The full path name to the created download sub directory.
     * @throws RosettaFileException  If unable to create download sub directory.
     */
    public String createDownloadSubDirectory(String downloadDir, String id) throws RosettaFileException {
        String filePathDownloadDir = FilenameUtils.concat(downloadDir, id);

        // File-ize the download sub directory.
        File localFileDir = new File(filePathDownloadDir);

        // Check to see if the download sub dir has been created yet; if not, create it.
        if (!localFileDir.exists())
            if (!localFileDir.mkdirs())
                throw new RosettaFileException("Unable to create " + id + " subdirectory in download directory.");

        return filePathDownloadDir;
    }


    /**
     * A simple method that reads each line of a file, and looks for blank lines.
     * Blank line = empty, only whitespace, or null (as per StringUtils).
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
        } catch ( IOException e) {
            throw new RosettaFileException("Unable to find blank lines from file: " + e);
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
     * @throws RosettaFileException If unable to retrieve the header lines from the file.
     */
    @Override
    public List<String> getHeaderLinesFromFile(String filePath, List<String> headerLineList) throws RosettaFileException {
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
        } catch (IOException e) {
            throw new RosettaFileException("Unable to find header lines from file: " + e);
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
     * @throws RosettaFileException  If unable to parse the file.
     */
    @Override
    public List<List<String>> parseByDelimiter(String filePath, List<String> headerLineList, String delimiter) throws RosettaFileException {
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
                            // the delimiter...not that the delimiter is " ".
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
        } catch (IOException e) {
            throw new RosettaFileException("Unable to parse file by delimiter: " + e);
        }
        return parsedData;
    }

    /**
     * A simple method that reads each line of a file, appends a new line
     * character & adds to a List. The list is then turned into a JSON string.
     *
     * @param filePath The path to the file on disk.
     * @return A JSON String of the file data parsed by line.
     * @throws RosettaFileException For any file I/O or JSON conversions problems.
     */
    @Override
    public String parseByLine(String filePath) throws RosettaFileException {
        String jsonFileData;

        List<String> fileContents = new ArrayList<>();
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
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
        String template = FilenameUtils.concat(filePathUploadDir, "template.dat");
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(template))) {
            data = (Data) objectInputStream.readObject();
        } catch (IOException| ClassNotFoundException | SecurityException e) {
            throw new RosettaFileException("Unable to write Data object: " + e);
        }
        return data;
    }

    /**
     * Writes a serializable Data object to disk in the template file.
     *
     * @param filePathDownloadDir The path to the download subdirectory in which to write the template file.
     * @param data The Data object write to disk.
     * @throws RosettaFileException If unable to write the Data object to the template file.
     */
    public void writeDataObject(String filePathDownloadDir, Data data) throws RosettaFileException {

        String downloadableTemplate = FilenameUtils.concat(filePathDownloadDir, "template.dat");
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(downloadableTemplate))) {
            objectOutputStream.writeObject(data);
        } catch (IOException | SecurityException e) {
            throw new RosettaFileException("Unable to write template object: " + e);
        }
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
     * @throws RosettaFileException  If unable to write file to disk.
     * @throws RosettaFileException  If a file conversion exception occurred.
     */
    @Override
    public String writeUploadedFileToDisk(String uploadDirPath, String id, String fileName, CommonsMultipartFile file) throws RosettaFileException {

        String filePath = FilenameUtils.concat(uploadDirPath, id);
        File localFileDir = new File(filePath);

        if (!localFileDir.exists())
            if (!localFileDir.mkdirs())
                throw new RosettaFileException("Unable to create " + id + " subdirectory in uploads directory.");

        logger.info("Writing uploaded file " + fileName + " to disk");
        File uploadedFile = new File(FilenameUtils.concat(filePath, fileName));

        try (FileOutputStream outputStream = new FileOutputStream(uploadedFile)) {
            outputStream.write(file.getFileItem().get());
        } catch (IOException e) {
            throw new RosettaFileException("Unable write uploaded file to disk: " + e);
        }

        // May need to process the uploaded file depending on its type (.zip, excel file, etc.).
        String dataFileNameExtension = FilenameUtils.getExtension(fileName);

        // Is it a Zip file?
        if (dataFileNameExtension.equals("zip")) {
            List<String> inventory = unZip(uploadDirPath, id, fileName);
        }

        // If the uploaded file was .xls or .xlsx, convert it to .csv
        if (dataFileNameExtension.equals("xls") || dataFileNameExtension.equals("xlsx"))
            fileName = convertToCSV(uploadDirPath, id, fileName);

        return fileName;
    }

    private List<String> unZip(String uploadDirPath, String id, String fileName) {
        return ZipFileUtil.unzipAndInventory(fileName, FilenameUtils.concat(uploadDirPath, id));

    }
}
