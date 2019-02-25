/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {

    /**
     * Replace the extension of a file in a Path with a new extension
     *
     * @param inFile The path with the file whose extension will be replaced
     * @param newExt The new extension
     * @return The path of the file with the new extension
     */
    public static Path replaceExtension(Path inFile, String newExt) {

        if (newExt.startsWith(".")) {
            newExt = newExt.substring(1);
        }

        String dataFile = inFile.toString();
        String dataFileName = FilenameUtils.getName(dataFile);
        String ext = FilenameUtils.getExtension(dataFileName);
        String newFileName = dataFileName.replace(ext, newExt);
        String newFile = dataFile.replace(dataFileName, newFileName);

        return Paths.get(newFile);
    }
}
