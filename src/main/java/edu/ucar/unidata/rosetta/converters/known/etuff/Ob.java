/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.known.etuff;

public class Ob {

    private String value;
    private String unit;

    Ob(String value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    String getValue() {
        return value;
    }

    String getUnit() {
        return unit;
    }

}
