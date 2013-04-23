package edu.ucar.unidata.pzhta.service;

import edu.ucar.unidata.pzhta.domain.AsciiFile;

import java.io.IOException;
import java.util.List;

/**
 * Service for parsing file data.
 */
public interface NcmlFileManager {

    public String createNcmlFile(AsciiFile file, List<List<String>> parseFileData, String downloadDirPath) throws IOException;

}


