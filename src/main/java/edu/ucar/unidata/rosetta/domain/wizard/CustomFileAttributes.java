/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain.wizard;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Form-backing object for the wizard to collect custom data file attributes.
 *
 * @author oxelson@ucar.edu
 */
public class CustomFileAttributes extends WizardData {

  private String dataFileType;
  private String delimiter;
  private String headerLineNumbers;
  private boolean noHeaderLines;

  /**
   * Returns the data file type.
   *
   * @return  The data file type.
   */
  public String getDataFileType() {
    return dataFileType;
  }

  /**
   * Returns the custom data file delimiter.
   *
   * @return The delimiter.
   */
  public String getDelimiter() {
    return delimiter;
  }

  /**
   * Returns the header line numbers of the custom data file.
   *
   * @return The header line numbers.
   */
  public String getHeaderLineNumbers() {
    return headerLineNumbers;
  }

  /**
   * Returns whether there are no leader lines in the file.
   *
   * @return true if no header lines; otherwise false.
   */
  public boolean isNoHeaderLines() {
    return noHeaderLines;
  }

  /**
   * Sets the data file type.
   *
   * @param dataFileType  The data file type.
   */
  public void setDataFileType(String dataFileType) {
    this.dataFileType = dataFileType;
  }

  /**
   * Sets the custom data file delimiter.
   *
   * @param delimiter The data file delimiter.
   */
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  /**
   * Sets the header line numbers of the custom data file.
   *
   * @param headerLineNumbers The header line numbers.
   */
  public void setHeaderLineNumbers(String headerLineNumbers) {
    this.headerLineNumbers = headerLineNumbers;
  }

  /**
   * Sets whether there are no leader lines in the file.
   *
   * @param noHeaderLines true if no header lines; otherwise false.
   */
  public void setNoHeaderLines(boolean noHeaderLines) {
    this.noHeaderLines = noHeaderLines;
  }

  /**
   * String representation of this object.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
