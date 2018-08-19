/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ucar.unidata.rosetta.domain.RosettaAttribute;
import edu.ucar.unidata.rosetta.domain.VariableInfo;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

public class VariableInfoUtils {

    public static String relativeTime = "relativeTime";
    public static String fullDateTime = "fullDateTime";
    public static String timeOnly = "timeOnly";
    public static String dateOnly = "dateOnly";
    public static String latitude = "latitude";
    public static String longitude = "longitude";
    public static String vertical = "vertical";

    public static String positiveAttrName = "positive";

    private static List<String> timeVarTypes =
            Arrays.asList(timeOnly, dateOnly, fullDateTime);

    public static boolean isVarUsed(VariableInfo vi) {
        boolean used = false;
        if (vi.getName() != null) {
            if (!vi.getName().toLowerCase().equals("do_not_use")) {
                used = true;
            }
        }
        return used;
    }

    public static boolean isCoordinateVariable(VariableInfo vi) {
        boolean coordVar = false;
        List<RosettaAttribute> rosettaControlMetadata = vi.getRosettaControlMetadata();
        if (rosettaControlMetadata != null) {
            for (RosettaAttribute attr : rosettaControlMetadata) {
                if (attr.getName().equals("coordinateVariable")) {
                    coordVar = Boolean.parseBoolean(attr.getValue());
                }
            }
        }
        return coordVar;
    }

    public static String getCoordVarType(VariableInfo vi) {
        String coordVarType = "";
        if (isCoordinateVariable(vi)) {
            List<RosettaAttribute> rosettaControlMetadata = vi.getRosettaControlMetadata();
            if (rosettaControlMetadata != null) {
                for (RosettaAttribute attr : rosettaControlMetadata) {
                    if (attr.getName().equals("coordinateVariableType")) {
                        coordVarType = attr.getValue();
                    }
                }
            }
        }
        return coordVarType;
    }

    public static boolean isTimeCoordVar(VariableInfo vi) {
        boolean isTimeCoordVar = false;
        String coordVarType = VariableInfoUtils.getCoordVarType(vi);
        if (coordVarType != null) {
            // is time related coord var?
            if (timeVarTypes.contains(coordVarType)) {
                isTimeCoordVar = true;
            }
        }
        return isTimeCoordVar;
    }

    public static DataType getDataType(VariableInfo vi) {
        DataType dt;
        String dtStr = null;
        List<RosettaAttribute> rosettaControlMetadata = vi.getRosettaControlMetadata();
        if (rosettaControlMetadata != null) {
            for (RosettaAttribute attr : rosettaControlMetadata) {
                if (attr.getName().equals("type")) {
                     dtStr = attr.getValue();
                }
            }
        }
        dt = getDataType(dtStr);
        return dt;
    }

    public static DataType getDataType(String dtStr) {
        // for now, this just calls getType, but it could get more complicated
        // depending on how the data type is passed around from the front-end to the
        // backend.

        return DataType.getType(dtStr);
    }

    public static String getUnit(VariableInfo vi) {
        String unit = "";
        List<RosettaAttribute> variableMetadata = vi.getVariableMetadata();
        if (variableMetadata != null) {
            for (RosettaAttribute attr : variableMetadata) {
                if (attr.getName().toLowerCase().equals("units")) {
                    unit = attr.getValue();
                }
            }
        }
        return unit;
    }

    public static RosettaAttribute getAttributeByName(String attrName, VariableInfo vi) {
        RosettaAttribute attrWanted = null;
        List<RosettaAttribute> variableMetadata = vi.getVariableMetadata();
        if (variableMetadata != null) {
            for (RosettaAttribute ra : variableMetadata) {
                if (ra.getName().equals(attrName)) {
                    attrWanted = ra;
                }
            }
        }
        return attrWanted;
    }

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
}
