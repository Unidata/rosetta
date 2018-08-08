/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain;

import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ucar.nc2.Attribute;

/**
 * Object representing a variable-specific information as outlined in
 * https://github.com/Unidata/rosetta/wiki/Rosetta-Template-Attributes#variableinfo-object-information
 */
public class VariableInfo {

  private int columnId;
  private String name;
  private List<Attribute> rosettaControlMetadata;
  private List<Attribute> variableMetadata;

  /**
   * Returns ID of the column in the CSV file.
   *
   * @return The column ID.
   */
  public int getColumnId() {
    return columnId;
  }

  /**
   * Sets ID of the column in the CSV file.
   *
   * @param columnId The column ID.
   */
  public void setColumnId(int columnId) {
    this.columnId = columnId;
  }

  /**
   * Returns the assigned name of the metadata item.
   *
   * @return The metadata name.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the assigned name of the metadata item.
   *
   * @param name The metadata name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the rosetta control metadata (used to determine/control the other metadata collected).
   *
   * @return The rosetta control metadata.
   */
  public List<Attribute> getRosettaControlMetadata() {
    return rosettaControlMetadata;
  }

  /**
   * Sets the rosetta control metadata (used to determine/control the other metadata collected).
   *
   * @param rosettaControlMetadata The rosetta control metadata.
   */
  public void setRosettaControlMetadata(List<Attribute> rosettaControlMetadata) {
    this.rosettaControlMetadata = rosettaControlMetadata;
  }

  /**
   * Returns the metadata specific to the variable.
   *
   * @return The variable metadata.
   */
  public List<Attribute> getVariableMetadata() {
    return variableMetadata;
  }

  /**
   * Sets the metadata specific to the variable.
   *
   * @param variableMetadata The variable metadata.
   */
  public void setVariableMetadata(List<Attribute> variableMetadata) {
    this.variableMetadata = variableMetadata;
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
