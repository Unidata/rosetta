/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.util.JsonUtil;
import edu.ucar.unidata.rosetta.util.XlsToCsvUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
     * Converts .xls and .xlsx files to .csv files.
     *
     * @param userFilesDirPath The path to the uploads directory.
     * @param id               The unique id associated with the file (a subdirectory in the uploads
     *                         directory).
     * @param fileName         The name of the .xls or .xlsx file to convert.
     * @return The name of the converted .csv file.
     * @throws RosettaFileException If unable to convert xls/xlsx file to csv.
     */
    private String convertToCSV(String userFilesDirPath, String id, String fileName)
            throws RosettaFileException {
        // This check of file extension may seem superfluous, but we don't want to continue it it's not an xls* file.
        String extension = FilenameUtils.getExtension(fileName);
        if (!(extension.equals("xls") || extension.equals("xlsx"))) {
            throw new RosettaFileException(
                    "Attempting to convert a non .xls type file to .csv format.");
        }

        String xlsFilePath = FilenameUtils.concat(FilenameUtils.concat(userFilesDirPath, id), fileName);
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
     * Creates a subdirectory in the Rosetta user_files directory with the name of the provided
     * unique ID, into which converted data files and Rosetta templates will be stashed and made
     * available for download by the user.
     *
     * @param userFilesDir The path to the Rosetta user files directory.
     * @param id           The unique ID that will become the name of the subdirectory.
     * @return The full path name to the created user files subdirectory.
     * @throws RosettaFileException If unable to create user files subdirectory.
     */
    public String createUserFilesSubDirectory(String userFilesDir, String id)
            throws RosettaFileException {
        String filePathUserFilesDir = FilenameUtils.concat(userFilesDir, id);

        // File-ize the download subdirectory.
        File localFileDir = new File(filePathUserFilesDir);

        // Check to see if the download subdirectory has been created yet; if not, create it.
        if (!localFileDir.exists()) {
            if (!localFileDir.mkdirs()) {
                throw new RosettaFileException(
                        "Unable to create " + id + " subdirectory in user_files directory.");
            }
        }

        return filePathUserFilesDir;
    }

    /**
     * Opens the given template file on disk and returns the contents as a string.
     *
     * @param userFilesDirPath  The location of the user files directory on disk.
     * @param id    The unique ID corresponding to the location of the file on disk.
     * @param fileName  The name of the template file.
     * @return  The template data in JSON string format.
     * @throws RosettaFileException If unable to read JSON data from template file.
     */
    public String getJsonStringFromTemplateFile(String userFilesDirPath, String id, String fileName) throws RosettaFileException {
        String jsonString;

        String filePath = FilenameUtils.concat(userFilesDirPath, id);
        filePath = FilenameUtils.concat(filePath, fileName);
        try {
            byte[] encodedData = Files.readAllBytes(Paths.get(filePath));
            jsonString = new String(encodedData, StandardCharsets.UTF_8);
            //return Template;
        } catch (IOException e) {
            throw new RosettaFileException(
                    "Unable to read JSON data from template file: " + e);
        }
        return jsonString;
    }

    /**
     * A simple method that reads each line of a file, appends a new line character & adds to a
     * List. The list is then turned into a JSON string.  This method is used to format the file
     * data for display in the client-side wizard.
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
            jsonFileData = JsonUtil.mapObjectToJson(fileContents);
        } catch (IOException e) {
            throw new RosettaFileException("Unable to parse file by line: " + e);
        }
        return jsonFileData;
    }



    /**
     * Creates a subdirectory in the designated user files directory using the (unique) id and
     * writes the given file to the user files subdirectory.
     *
     * @param userFilesDirPath The path to the user files directory.
     * @param id               The unique id associated with the file (a subdirectory in the user
     *                         files directory).
     * @param fileName         The name of the file to save to disk.
     * @param file             The CommonsMultipartFile to save to disk.
     * @return The name of the saved file on disk (can be different than the downloaded file).
     * @throws SecurityException    If unable to write file to disk because of a JVM security
     *                              manager violation.
     * @throws RosettaFileException If unable to write file to disk.
     * @throws RosettaFileException If a file conversion exception occurred.
     */
    @Override
    public String writeUploadedFileToDisk(String userFilesDirPath, String id, String fileName,
                                          CommonsMultipartFile file) throws RosettaFileException {

        // Create full file path to user file subdirectory.
        String filePathUploadDir = createUserFilesSubDirectory(userFilesDirPath, id);

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
            fileName = convertToCSV(userFilesDirPath, id, fileName);
        }

        return fileName;
    }
}
