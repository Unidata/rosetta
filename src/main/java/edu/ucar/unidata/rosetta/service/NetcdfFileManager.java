package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.AsciiFile;

import java.io.IOException;
import java.util.List;

/**
 * Service for parsing file data.
 */
public interface NetcdfFileManager {

    public String createNetcdfFile(AsciiFile file, List<List<String>> parseFileData, String downloadDirPath) throws IOException;

}


