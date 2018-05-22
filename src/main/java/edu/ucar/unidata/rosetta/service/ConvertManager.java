package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.Data;

import java.io.IOException;

/**
 * @author oxelson@ucar.edu
 */
public interface ConvertManager {

    public String convertToNetCDF(Data data) throws IOException, IllegalArgumentException;
}
