/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.custom.dsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.ucar.unidata.rosetta.domain.RosettaAttribute;
import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.VariableInfo;
import edu.ucar.unidata.rosetta.util.RosettaAttributeUtils;
import edu.ucar.unidata.rosetta.util.RosettaGlobalAttributeUtils;
import edu.ucar.unidata.rosetta.util.TemplateUtils;
import edu.ucar.unidata.rosetta.util.VariableInfoUtils;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.Variable;

public class SingleProfile extends NetcdfFileManager {

    SingleProfile() {
        super("profile");
    }

    /**
     * make variables specific to profile DSGs
     */
    @Override
    void makeOtherVariables() {
        // try to ge trajectory name

        int idLen;
        if (featureId != null) {
            idLen = featureId.length();
        } else {
            featureId = "profile1";
            idLen = featureId.length();
        }

        Dimension trajDim = ncf.addDimension("profile_char", idLen);
        Variable featureId = ncf.addVariable(null, "profile", DataType.CHAR, Collections.singletonList(trajDim));
        featureId.addAttribute(new Attribute("cf_role", "profile_id"));
    }

    /**
     * Create a coordinate variable for a non-time related coordinate variable
     * @param variableInfo non-time related coordinate variable
     */
    void makeNonElementCoordVars(VariableInfo variableInfo) {
        // for a profile, all coordinate variables will have a vertical dimension
        List<Dimension> coordVarDimensions = Collections.singletonList(elementDimension);

        Group group = null;

        String varName = variableInfo.getName();

        // only add if not already added (i.e. time variable almost certainly added)
        if (ncf.findVariable(varName) == null) {
            DataType dataType = VariableInfoUtils.getDataType(variableInfo);
            // if colId not -2, then use coordVarDimension; otherwise, will be scalar as the value
            // comes from a global attribute
            Variable var;
            if (variableInfo.getColumnId() == -2) {
                var = ncf.addVariable(group, varName, dataType, "");
            } else {
                var = ncf.addVariable(group, varName, dataType, coordVarDimensions);
            }

            // add all attributes from the variableInfo object
            List<Attribute> allVarAttrs = VariableInfoUtils.getAllVariableAttributes(variableInfo);

            // compute and add new computedAttrs too the allVarAttrs list
            List<Attribute> computedAttrs = calculateCoordVarAttrs(variableInfo);
            allVarAttrs.addAll(computedAttrs);

            var.addAll(allVarAttrs);
            coordAttrValues.add(varName);
        }
    }

    @Override
    void createNonElementCoordVars(Template template) {
        // use geospatial_lat_start, geospatial_lon_start attributes to create lat/lon, assuming
        // latitude and longitude not part of the columnar data
        Map<String, ArrayList<Attribute>> globalAttrs = TemplateUtils.getGlobalAttrsMap(template);
        ArrayList<Attribute> globalAttrsRoot = globalAttrs.get("root");
        String latName = TemplateUtils.findUniqueName("latitude", template);
        String lonName = TemplateUtils.findUniqueName("longitude", template);

        if (!coordVarTypes.contains(VariableInfoUtils.latitude)) {
            for (Attribute attr : globalAttrsRoot) {
                if (attr.getFullName().equalsIgnoreCase("geospatial_lat_start")) {
                    VariableInfo latVi = new VariableInfo();
                    // will tell converter to look for data in global attribute
                    latVi.setColumnId(-2);
                    latVi.setName("latitude");

                    List<RosettaAttribute> latRosettaAttrs = new ArrayList<>();
                    latRosettaAttrs.add(new RosettaAttribute("axis", "X", "STRING"));
                    latRosettaAttrs.add(new RosettaAttribute("long_name", "latitude", "STRING"));
                    latRosettaAttrs.add(new RosettaAttribute("standard_name", "latitude", "STRING"));
                    latRosettaAttrs.add(new RosettaAttribute("units", "degrees_north", "STRING"));

                    latVi.setVariableMetadata(latRosettaAttrs);

                    List<RosettaAttribute> latRosettaControlAttrs = new ArrayList<>();
                    latRosettaControlAttrs.add(new RosettaAttribute("GlobalAttributeName", "geospatial_lat_start", "STRING"));
                    latRosettaControlAttrs.add(new RosettaAttribute("type", "DOUBLE", "STRING"));
                    latRosettaControlAttrs.add(new RosettaAttribute("coordinateVariable", "true", "BOOLEAN"));
                    latRosettaControlAttrs.add(new RosettaAttribute("coordinateVariableType", "latitude", "STRING"));

                    latVi.setRosettaControlMetadata(latRosettaControlAttrs);

                    template.getVariableInfoList().add(latVi);
                } else if (attr.getFullName().equalsIgnoreCase("geospatial_lon_start")) {
                    VariableInfo lonVi = new VariableInfo();
                    // will tell converter to look for data in global attribute
                    lonVi.setColumnId(-2);
                    lonVi.setName("longitude");

                    List<RosettaAttribute> lonRosettaAttrs = new ArrayList<>();
                    lonRosettaAttrs.add(new RosettaAttribute("axis", "Y", "STRING"));
                    lonRosettaAttrs.add(new RosettaAttribute("long_name", "longitude", "STRING"));
                    lonRosettaAttrs.add(new RosettaAttribute("standard_name", "longitude", "STRING"));
                    lonRosettaAttrs.add(new RosettaAttribute("units", "degrees_east", "STRING"));

                    lonVi.setVariableMetadata(lonRosettaAttrs);

                    List<RosettaAttribute> lonRosettaControlAttrs = new ArrayList<>();
                    lonRosettaControlAttrs.add(new RosettaAttribute("GlobalAttributeName", "geospatial_lon_start", "STRING"));
                    lonRosettaControlAttrs.add(new RosettaAttribute("type", "DOUBLE", "STRING"));
                    lonRosettaControlAttrs.add(new RosettaAttribute("coordinateVariable", "true", "BOOLEAN"));
                    lonRosettaControlAttrs.add(new RosettaAttribute("coordinateVariableType", "longitude", "STRING"));

                    lonVi.setRosettaControlMetadata(lonRosettaControlAttrs);

                    template.getVariableInfoList().add(lonVi);
                }
            }
        }
    }

}
