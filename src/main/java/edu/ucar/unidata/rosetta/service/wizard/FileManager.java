/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;


/**
 * Service for handling files and parsing file data.
 *
 * @author oxelson@ucar.edu
 */
public interface FileManager {

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
            throws RosettaFileException;


    /**
     * A simple method that reads each line of a file, appends a new line character & adds to a
     * List. The list is then turned into a JSON string.  This method is used to format the file
     * data for display in the client-side wizard.
     *
     * @param filePath The path to the file on disk.
     * @return A JSON String of the file data parsed by line.
     * @throws RosettaFileException For any file I/O or JSON conversions problems.
     */
    public String parseByLine(String filePath) throws RosettaFileException;

    public String getJsonStringFromTemplateFile(String userFilesDirPath, String id, String fileName) throws RosettaFileException;


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
    public String writeUploadedFileToDisk(String userFilesDirPath, String id, String fileName,
                                          CommonsMultipartFile file) throws RosettaFileException;
}


