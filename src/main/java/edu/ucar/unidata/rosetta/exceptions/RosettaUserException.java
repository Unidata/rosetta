/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.exceptions;

/**
 * Rosetta User object exception.
 *
 * @author oxelson@ucar.edu
 */
public class RosettaUserException extends Exception {

  public RosettaUserException() {
  }

  public RosettaUserException(String message) {
    super(message);
  }
}
