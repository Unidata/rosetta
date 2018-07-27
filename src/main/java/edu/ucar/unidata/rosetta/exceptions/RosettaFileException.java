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
