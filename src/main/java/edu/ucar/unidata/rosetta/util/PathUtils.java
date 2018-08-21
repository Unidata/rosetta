/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {

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
