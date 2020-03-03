/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Form-backing object for the wizard to collect a metadata associated with a variable.
 */
public class VariableMetadata {

  private int variableId;
  private String complianceLevel;
  private String metadataKey;
  private String metadataValue;

  /**
   * Returns the id of the variable with which this object is associated.
   *
   * @return The variable id.
   */
  public int getVariableId() {
    return variableId;
  }

  /**
   * Sets the id of the variable with which this object is associated.
   *
   * @param variableId The variable variableId.
   */
  public void setVariableId(int variableId) {
    this.variableId = variableId;
  }


  /**
   * Returns the metadata complianceLevel.
   *
   * @return The metadata complianceLevel.
   */
  public String getComplianceLevel() {
    return complianceLevel;
  }

  /**
   * Sets the the metadata complianceLevel.
   *
   * @param complianceLevel The metadata complianceLevel.
   */
  public void setComplianceLevel(String complianceLevel) {
    this.complianceLevel = complianceLevel;
  }

  /**
   * Returns the metadata key.
   *
   * @return The metadata key.
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * Sets the metadata key.
   *
   * @param metadataKey The metadata key.
   */
  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

  /**
   * Returns the metadata value.
   *
   * @return The metadatavalue.
   */
  public String getMetadataValue() {
    return metadataValue;
  }

  /**
   * Sets the metadata value.
   *
   * @param metadataValue The metadata value.
   */
  public void setMetadataValue(String metadataValue) {
    this.metadataValue = metadataValue;
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
