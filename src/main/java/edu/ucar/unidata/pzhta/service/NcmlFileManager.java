package edu.ucar.unidata.pzhta.service;

import java.io.IOException;
import java.util.List;

import edu.ucar.unidata.pzhta.domain.AsciiFile;

/**
 * Service for parsing file data.
 */
public interface NcmlFileManager {

      public String createNcmlFile(AsciiFile file, List<List<String>> parseFileData, String downloadDirPath) throws IOException;

}


