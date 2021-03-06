/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.exceptions;

/**
 * Rosetta data-related exception.
 *
 * @author oxelson@ucar.edu
 */
public class RosettaDataException extends Exception {

  public RosettaDataException() {}

  public RosettaDataException(String message) {
    super(message);
  }
}
