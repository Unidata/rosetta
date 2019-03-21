/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import edu.ucar.unidata.rosetta.domain.RosettaAttribute;
import edu.ucar.unidata.rosetta.domain.VariableInfo;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

public class VariableInfoUtils {

    // todo: move these to a constants class?
    public static String relativeTime = "relativetime";
    public static String fullDateTime = "fulldatetime";
    public static String timeOnly = "timeonly";
    public static String dateOnly = "dateonly";
    public static String latitude = "latitude";
    public static String longitude = "longitude";
    public static String vertical = "vertical";

    public static String positiveAttrName = "positive";

    private static List<String> timeVarTypes =
            Arrays.asList(relativeTime, timeOnly, dateOnly, fullDateTime);

    /**
     * Check if a VariableInfo object contains information, or if it is to be ignored.
     *
     * @param vi - VariableInfo object to check if used
     * @return <code>true</code> if used; <code>false</code> otherwise
     */
    public static boolean isVarUsed(VariableInfo vi) {
        boolean used = false;
        if (vi.getName() != null) {
            if (!vi.getName().equalsIgnoreCase("do_not_use")) {
                used = true;
            }
        }
        return used;
    }

    /**
     * Check if the provided VariableInfo object contains information on a coordinate variable
     *
     * @param vi VariableInfo object to check
     * @return <code>true</code> if VariableInfo contains coordinate variable information;
     * <code>false</code> otherwise
     */
    public static boolean isCoordinateVariable(VariableInfo vi) {
        boolean coordVar = false;
        List<RosettaAttribute> rosettaControlMetadata = vi.getRosettaControlMetadata();
        if (rosettaControlMetadata != null) {
            for (RosettaAttribute attr : rosettaControlMetadata) {
                if (attr.getName().equalsIgnoreCase("coordinateVariable")) {
                    coordVar = Boolean.parseBoolean(attr.getValue());
                }
            }
        }
        return coordVar;
    }

    /**
     * Find the coordnatevariableType of the VariableInfo object
     *
     * @param vi The VariableInfo object from which to get the coordinate variable type
     * @return the coordinate variable type (empty string indicates that the VariableInfo object is
     * not a coordinate variable)
     */
    public static String getCoordVarType(VariableInfo vi) {
        String coordVarType = "";
        if (isCoordinateVariable(vi)) {
            List<RosettaAttribute> rosettaControlMetadata = vi.getRosettaControlMetadata();
            if (rosettaControlMetadata != null) {
                for (RosettaAttribute attr : rosettaControlMetadata) {
                    if (attr.getName().equalsIgnoreCase("coordinateVariableType")) {
                        coordVarType = attr.getValue().toLowerCase();
                    }
                }
            }
        }
        return coordVarType;
    }

    /**
     * Check if the provided VariableInfo object contains informatoin related to a time coordinate
     * variable.
     *
     * @param vi VariableInfo object to check
     * @return <code>true</code> if VariableInfo is a time coordinate variable; <code>false</code>
     * otherwise
     */
    public static boolean isTimeCoordVar(VariableInfo vi) {
        boolean isTimeCoordVar = false;
        String coordVarType = VariableInfoUtils.getCoordVarType(vi);
        if (coordVarType != null) {
            // is time related coord var?
            if (timeVarTypes.contains(coordVarType.toLowerCase())) {
                isTimeCoordVar = true;
            }
        }
        return isTimeCoordVar;
    }

    /**
     * Get the data type of a VariableInfo object
     *
     * @param vi The VariableInfo object from which to get the data type
     * @return The VariableInfo objects DataType
     */
    public static DataType getDataType(VariableInfo vi) {
        DataType dt;
        String dtStr = null;
        List<RosettaAttribute> rosettaControlMetadata = vi.getRosettaControlMetadata();
        if (rosettaControlMetadata != null) {
            for (RosettaAttribute attr : rosettaControlMetadata) {
                if (attr.getName().equalsIgnoreCase("type")) {
                    dtStr = attr.getValue();
                }
            }
        }
        if (dtStr.equalsIgnoreCase("integer")) {
            dtStr = "int";
        } else if (dtStr.equalsIgnoreCase("text")) {
            dtStr = "char";
        }

        dt = getDataType(dtStr);
        return dt;
    }

    /**
     * Get the DataType based on its string representation
     *
     * @param dtStr string representation of a DataType
     * @return the DataType
     */
    public static DataType getDataType(String dtStr) {
        // for now, this just calls getType, but it could get more complicated
        // depending on how the data type is passed around from the front-end to the
        // backend.

        return DataType.getType(dtStr);
    }

    /**
     * Get the unit associated with a VariableInfo object
     *
     * @param vi The VariableInfo object to search
     * @return the unit string
     */
    public static String getUnit(VariableInfo vi) {
        String unit = "";
        List<RosettaAttribute> variableMetadata = vi.getVariableMetadata();
        if (variableMetadata != null) {
            for (RosettaAttribute attr : variableMetadata) {
                if (attr.getName().equalsIgnoreCase("units")) {
                    unit = attr.getValue();
                }
            }
        }
        return unit;
    }

    /**
     * Find a VariableInfo object's attribute by name
     *
     * This will return the last matching attribute, if multiple are found. If no attribute found,
     * will return null
     *
     * @param attrName Name of the wanted attribute
     * @param vi       the VariableInfo object to search
     * @return The attribute with matching name
     */
    @Nullable
    public static RosettaAttribute findAttributeByName(String attrName, VariableInfo vi) {
        RosettaAttribute attrWanted = null;
        List<RosettaAttribute> variableMetadata = vi.getVariableMetadata();
        if (variableMetadata != null) {
            for (RosettaAttribute ra : variableMetadata) {
                if (ra.getName().equalsIgnoreCase(attrName)) {
                    attrWanted = ra;
                }
            }
        }
        return attrWanted;
    }

    /**
     * Get all of the attributes of a VariableInfo object
     *
     * @param vi the VariableInfo object from which to get the attributes
     * @return A list of the attributes of the VariableInfo object
     */
    public static List<Attribute> getAllVariableAttributes(VariableInfo vi) {
        // add all attributes from template
        List<Attribute> allVarAttrs = new ArrayList<>();
        for (RosettaAttribute attr : vi.getVariableMetadata()) {
            // assume string
            String name = attr.getName();
            String value = attr.getValue();
            Attribute varAttr = new Attribute(name, value);
            if (RosettaAttributeUtils.isValueNumeric(attr)) {
                varAttr = new Attribute(name, RosettaAttributeUtils.convertValueToNumber(attr));
            }
            allVarAttrs.add(varAttr);
        }
        return allVarAttrs;
    }

    /**
     * Look at a VariableInfo object, if it contains a missing value attribute, and return it
     *
     * @param vi the VariableInfo object from which to get the attributes
     * @return The missing value using Java 8s Optional
     */
    public static Optional<Double> findMissingValue(VariableInfo vi) {
        Optional<Double> missingValue = Optional.empty();
        Optional<RosettaAttribute> missingValueRosettaAttr = Optional.ofNullable(VariableInfoUtils.findAttributeByName("missing_value", vi));
        if (missingValueRosettaAttr.isPresent()) {
            missingValue = Optional.of(Double.valueOf(missingValueRosettaAttr.get().getValue()));
        }

        return missingValue;
    }
}
