package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.Data;

import java.io.IOException;

import ucar.ma2.InvalidRangeException;

/**
 * @author oxelson@ucar.edu
 */
public interface ConvertManager {

    public String convertToNetCDF(Data data) throws IOException, IllegalArgumentException, InvalidRangeException;
}
