/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package edu.ucar.unidata.rosetta.util;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;

/**
 * Utils class with methods for dealing with file IO.
 */
public class IoUtils {

    /**
     * Creates a subdirectory in the Rosetta user_files directory with the name of the provided
     * unique transaction ID, into which converted data files and Rosetta templates will be
     * stashed and made available for download by the user.
     *
     * @param id  The unique transaction ID that will become the name of the subdirectory.
     * @return The full path name to the created user files subdirectory.
     * @throws RosettaFileException If unable to create user files subdirectory.
     */
    public static String createUserFilesSubDirectory(String id)
            throws RosettaFileException {
        String filePathUserFilesDir = FilenameUtils.concat(PropertyUtils.getUserFilesDir(), id);

        // File-ize the subdirectory.
        File localFileDir = new File(filePathUserFilesDir);

        // Check to see if the subdirectory has been created yet; if not, create it.
        if (!localFileDir.exists()) {
            if (!localFileDir.mkdirs()) {
                throw new RosettaFileException(
                        "Unable to create " + id + " subdirectory in user_files directory.");
            }
        }

        return filePathUserFilesDir;
    }
}
