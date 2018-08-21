/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import java.util.Arrays;
import java.util.List;

import edu.ucar.unidata.rosetta.domain.RosettaAttribute;
import ucar.ma2.DataType;

public class RosettaAttributeUtils {

    // Numeric datatypes that Rosetta knows about
    private static List<DataType> numericTypes = Arrays.asList(
            DataType.FLOAT,
            DataType.INT,
            DataType.DOUBLE);

    /**
     * Check if the attribute has a numeric DataType
     *
     * @param ra The attribute to check
     * @return <code>true</code> if numeric; <code>false</code> otherwise
     */
    public static boolean isValueNumeric(RosettaAttribute ra) {
        DataType dt = VariableInfoUtils.getDataType(ra.getType());
        return numericTypes.contains(dt);
    }

    /**
     * Convert the value stored in a RosettaAttribute into a Number
     *
     * @param ra the attribute whose value to convert
     * @return the Number value stored in the numeric attributes
     */
    public static Number convertValueToNumber(RosettaAttribute ra) {
        String type = ra.getType();
        Number val = null;
        switch (type.toLowerCase()) {
            case "integer":
                val = Integer.parseInt(ra.getValue());
                break;
            case "float":
                val = Float.parseFloat(ra.getValue());
                break;
            case "double":
                val = Double.parseDouble(ra.getValue());
                break;
        }

        return val;
    }
}
