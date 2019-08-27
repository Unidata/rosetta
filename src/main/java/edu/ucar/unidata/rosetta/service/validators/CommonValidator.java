/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;

/**
 * Common validation methods all validators will use.
 *
 * @author oxelson@ucar.edu
 */
public abstract class CommonValidator {

  protected static final String[] NAUGHTY_STRINGS = {"<script>", "../", "svg", "javascript", "::", "&quot;",
      "fromcharCode", "%3", "$#", "alert(", ".js", ".source", "\\", "scriptlet", ".css", "binding:", ".htc", "vbscript",
      "mocha:", "livescript:", "base64", "\00", "xss:", "%77", "0x", "IS NULL;", "1;", "; --", "1=1"};
  protected static final String[] NAUGHTY_CHARS = {"<", ">", "`", "^", "|", "}", "{"};

  /**
   * Checks if the provided string contains any known, dubious strings or chars.
   *
   * @param input The user input item to check.
   * @param errors Object in which to store any validation errors.
   */
  protected void validateInput(String input, Errors errors) {
    String badChar = checkForNaughtyChars(input);
    if (badChar != null) {
      errors.reject(null, "Bad value submitted: " + badChar);
    }
    String badString = checkForNaughtyStrings(input);
    if (badString != null) {
      errors.reject(null, "Bad value submitted: " + badString);
    }
  }

  /**
   * Checks if the provided string contains anything the NAUGHTY_STRINGS list.
   *
   * @param itemToCheck The string to check.
   * @return The item in NAUGHTY_STRINGS that matches the provided string; otherwise null.
   */
  private String checkForNaughtyStrings(String itemToCheck) {
    for (String item : NAUGHTY_STRINGS) {
      if (StringUtils.contains(StringUtils.lowerCase(itemToCheck), item)) {
        return item;
      }
    }
    return null;
  }

  /**
   * Checks if the provided string contains anything the NAUGHTY_CHARS list.
   *
   * @param itemToCheck The string to check.
   * @return The item in NAUGHTY_CHARS that matches the provided string; otherwise null.
   */
  private String checkForNaughtyChars(String itemToCheck) {
    for (String item : NAUGHTY_CHARS) {
      if (StringUtils.contains(itemToCheck, item)) {
        return item;
      }
    }
    return null;
  }
}
