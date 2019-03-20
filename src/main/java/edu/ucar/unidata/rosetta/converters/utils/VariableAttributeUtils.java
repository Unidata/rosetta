/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ucar.ma2.Array;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;

public class VariableAttributeUtils {

    /**
     * Create valid_min, valid_max attributes associated with the input data array
     *
     * @param data data from which to compute max/min values
     * @return A list containing the valid_max and valid_min attributes
     */
    public static List<Attribute> getMaxMinAttrs(Array data) {
        return getMaxMinAttrs(data, Optional.empty());
    }
    /**
     * Create valid_min, valid_max attributes associated with the input data array
     *
     * @param data data from which to compute max/min values
     * @return A list containing the valid_max and valid_min attributes
     */
    public static List<Attribute> getMaxMinAttrs(Array data, Optional<Double> missingValue) {
        MAMath.MinMax maxMinVals;
        List<Attribute> attrs = new ArrayList<>();

        if (missingValue.isPresent()) {
            Double actualMissingValue = missingValue.get();
            maxMinVals = MAMath.getMinMaxSkipMissingData(data, actualMissingValue);
        } else {
            maxMinVals = MAMath.getMinMax(data);
        }

        attrs.add(new Attribute("valid_max", maxMinVals.max));
        attrs.add(new Attribute("valid_min", maxMinVals.min));

        return attrs;
    }
}
