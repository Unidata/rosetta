/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.batch;

import java.io.IOException;

import edu.ucar.unidata.rosetta.domain.batch.BatchProcessZip;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;

/**
 * Service for handling collected data information.
 *
 * @author sarms@ucar.edu
 */
public interface BatchFileManager {

  /**
   * processes the uploaded data file and converts all data files within to netCDF.
   *
   * @param batchZipFile The batchZipFile object representing the uploaded zip file to process.
   * @return The path to the zip file containing all converted files.
   * @throws RosettaFileException If unable to create the template file from the Data object.
   */
  String batchProcess(BatchProcessZip batchZipFile) throws RosettaFileException, IOException;

}
