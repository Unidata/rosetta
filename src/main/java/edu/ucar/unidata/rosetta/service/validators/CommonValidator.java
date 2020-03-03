/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;

/**
 * Common validation methods all validators will use.
 */
@SuppressWarnings("SpellCheckingInspection")
public abstract class CommonValidator {

  private static final String[] NAUGHTY_STRINGS = {"<script>", "../", "svg", "javascript", "::", "&quot;",
      "fromcharCode", "%3", "$#", "alert(", ".js", ".source", "\\", "scriptlet", ".css", "binding:", ".htc", "vbscript",
      "mocha:", "livescript:", "base64", "\00", "xss:", "%77", "0x", "IS NULL;", "1;", "; --", "1=1"};
  private static final String[] NAUGHTY_CHARS = {"<", ">", "`", "^", "|", "}", "{"};

  /**
   * Checks if the provided string contains any known, dubious strings or chars.
   *
   * @param field The form field to be validated.
   * @param input The user input for the form form field.
   * @param errors Object to store validation errors.
   */
  protected void validateInput(String field, String input, Errors errors) {
    String badChar = checkForNaughtyChars(input);
    if (badChar != null) {
      errors.rejectValue(field, "bad input data", "Bad value submitted: " + badChar);
    }
    if (errors.getFieldErrorCount(field) > 0) {
      String badString = checkForNaughtyStrings(input);
      if (badString != null) {
        errors.rejectValue(field, "bad input data", "Bad value submitted: " + badString);
      }
    }
  }

  /**
   * Checks if provided string is empty (""), null or whitespace only.
   *
   * @param input The user input for the form form field.
   */
  protected boolean validateNotEmpty(String input) {
    return StringUtils.isBlank(input);
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
