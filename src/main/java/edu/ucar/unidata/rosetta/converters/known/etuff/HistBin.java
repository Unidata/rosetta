/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.known.etuff;

public class HistBin {
  private float binValue;
  private String binUnit;

  HistBin(float value, String unit) {
    this.binValue = value;
    this.binUnit = unit;
  }

  float getBinValue() {
    return binValue;
  }

  String getBinUnit() {
    return binUnit;
  }
}
