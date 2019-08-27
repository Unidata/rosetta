/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import ucar.nc2.Attribute;

public class RosettaGlobalAttributeUtils {

  /**
   * Create a netCDF-Java Attribute from a RosettaGlobalAttribute
   *
   * @param globalAttr The RosettaGlobalAttribute to convert
   * @return the equivalent netCDF-Java Attribute
   */
  public static Attribute getAttributeFromGlobalAttr(RosettaGlobalAttribute globalAttr) {
    String name = globalAttr.getName();
    String value = globalAttr.getValue();
    Attribute attr = new Attribute(name, value);
    if (RosettaAttributeUtils.isValueNumeric(globalAttr)) {
      attr = new Attribute(name, RosettaAttributeUtils.convertValueToNumber(globalAttr));
    }
    return attr;
  }
}
