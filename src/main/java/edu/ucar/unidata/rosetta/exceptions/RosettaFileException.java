/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.exceptions;

/**
 * Exception dealing with Rosetta File IO issues.
 *
 * @author oxelson@ucar.edu
 */
public class RosettaFileException extends Exception {

  public RosettaFileException() {
  }

  public RosettaFileException(String message) {
    super(message);
  }
}
