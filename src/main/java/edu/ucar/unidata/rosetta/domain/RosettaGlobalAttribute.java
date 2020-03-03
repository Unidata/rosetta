/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */


package edu.ucar.unidata.rosetta.domain;

import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Class representing a Rosetta Attribute for Global data
 * (as per https://github.com/Unidata/rosetta/wiki/Rosetta-Template-Attributes#variableinfo-object-information)
 */
public class RosettaGlobalAttribute extends RosettaAttribute {

  private String group;
  private static String DEFAULT_GROUP = "root";

  /**
   * noop constructor
   */
  public RosettaGlobalAttribute() {}

  /**
   * Hold information about a global attribute
   *
   * @param name name of the attribute
   * @param value value of the attribute
   * @param type data type of the attribute
   * @param group group to which attribute belongs
   */
  public RosettaGlobalAttribute(String name, String value, String type, String group) {
    super.setName(name);
    super.setValue(value);
    super.setType(type);
    this.group = (group != null) ? group : DEFAULT_GROUP;
  }

  /**
   * Hold information about a global attribute (attached to root group)
   *
   * @param name name of the attribute
   * @param value value of the attribute
   * @param type data type of the attribute
   */
  public RosettaGlobalAttribute(String name, String value, String type) {
    super.setName(name);
    super.setValue(value);
    super.setType(type);
    this.group = DEFAULT_GROUP;
  }

  /**
   * Override equals() for RosettaGlobalAttribute.
   */
  @Override
  public boolean equals(Object obj) {

    if (obj == this)
      return true;
    if (!(obj instanceof RosettaGlobalAttribute)) {
      return false;
    }

    RosettaGlobalAttribute rga = (RosettaGlobalAttribute) obj;

    return Objects.equals(super.getName(), rga.getName()) && Objects.equals(super.getValue(), rga.getValue())
        && Objects.equals(super.getType(), rga.getType()) && Objects.equals(this.group, rga.getGroup());
  }

  /**
   * Override Object.hashCode() to implement equals.
   */
  @Override
  public int hashCode() {
    int superHash = super.hashCode();
    return Objects.hash(superHash, this.group);
  }

  /**
   * Returns the group to which attribute belongs.
   *
   * @return The attribute name.
   */
  public String getGroup() {
    // never return a null group - at least default to "root"
    return (group != null) ? group : DEFAULT_GROUP;
  }

  /**
   * Set the group to which attribute belongs (example: Meta_eTuff/device)
   *
   * @param group name of the attribute
   */
  public void setGroup(String group) {
    this.group = group;
  }

  /**
   * String representation of this object.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, TransactionLogStyle.EMBEDDED_OBJECT_STYLE);
  }
}
