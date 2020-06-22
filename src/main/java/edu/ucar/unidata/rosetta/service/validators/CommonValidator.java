/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.validators;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.Errors;

/**
 * Common validation methods used by all validators.
 */
@SuppressWarnings("SpellCheckingInspection")
public abstract class CommonValidator {

  protected static final Logger logger = LogManager.getLogger(CommonValidator.class);

  private static final String[] NAUGHTY = {"<", ">", "`", "^", "|", "}", "{", "script", "../", "svg", "javascript", "::", "&quot;",
      "fromcharCode", "%3", "$#", "alert(", ".js", ".source", "\\", "scriptlet", ".css", "binding:", ".htc", "vbscript",
      "mocha:", "livescript:", "base64", "\00", "xss:", "%77", "0x", "IS NULL;", "1;", "; --", "1=1"};
  private String ipAddress;

  /**
   * Checks if the provided string contains any known, dubious strings or chars contained in the NAUGHTY list.
   * <p>
   * Populates the Errors object if validation fails.
   *  @param input The user input for the form form field.
   * @param errors Object to store validation errors.
   */
  protected void findDubiousInput(String input, Errors errors) {
    for (String naughty : NAUGHTY) {
      if (StringUtils.contains(input, naughty)) {
        logError("Bad input submitted: " + naughty);
        errors.reject("badInputData");
      }
    }
  }

  /**
   * Returns the IP address of the remote user.
   * (Used for logging purposes.)
   *
   * @return the IP address.
   */
  private String getIpAddress() {
    return ipAddress;
  }

  /**
   * Records the validation error message in the logs.
   *
   * @param message The message to log.
   */
  protected void logError(String message) {
    logger.info("VALIDATION ERROR  [" + getIpAddress() + "]  " + message);
  }

  /**
   * Sets the IP address of the remote user.
   * (Used for logging purposes.)
   *
   * @param ipAddress The IP address.
   */
  public void setIpAddress (String ipAddress) {
    this.ipAddress = ipAddress;
  }

  /**
   * Checks if provided string is empty (""), null or whitespace only.
   * <p>
   * The StringTrimmerEditor property editor is registered with the DataBinder in the controllers, which trims strings
   * in input data and converts empty strings to null.  HOWEVER, we are still using a method to check for empty strings,
   * blank string, and null values in case something happens on the controller-side of things and this trim to null
   * behavior is removed.
   *
   * @param input The user input for the form form field.
   * @return eturn true if the CharSequence is null, empty or whitespace only
   */
  protected boolean validateNotEmpty(String input) {
    return StringUtils.isBlank(input);
  }
}
