/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.custom.dsg;

import java.util.Collections;
import java.util.List;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.VariableInfo;
import edu.ucar.unidata.rosetta.util.VariableInfoUtils;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.Variable;

public class SingleTrajectory extends NetcdfFileManager {

  SingleTrajectory() {
    super("trajectory");
  }

  /**
   * make variables specific to trajectory DSGs
   */
  @Override
  void makeOtherVariables() {
    // try to ge trajectory name

    int idLen;
    if (featureId != null) {
      idLen = featureId.length();
    } else {
      featureId = "feature1";
      idLen = featureId.length();
    }

    Dimension trajDim = ncf.addDimension("trajectory_char", idLen);
    Variable featureId = ncf.addVariable(null, "trajectory", DataType.CHAR, Collections.singletonList(trajDim));
    featureId.addAttribute(new Attribute("cf_role", "trajectory_id"));
  }


  /**
   * Create a coordnate variable for a non-time related coordinate variable
   * 
   * @param variableInfo non-time related coordinate variable
   */
  void makeNonElementCoordVars(VariableInfo variableInfo) {
    // for a trajectory, all coordinate variables will have a dimension of time
    List<Dimension> coordVarDimensions = Collections.singletonList(elementDimension);

    Group group = null;

    String varName = variableInfo.getName();

    DataType dataType = VariableInfoUtils.getDataType(variableInfo);
    Variable var = ncf.addVariable(group, varName, dataType, coordVarDimensions);

    // add all attributes from the variableInfo object
    List<Attribute> allVarAttrs = VariableInfoUtils.getAllVariableAttributes(variableInfo);

    List<Attribute> computedAttrs = calculateCoordVarAttrs(variableInfo);
    // add new computedAttrs too the allVarAttrs list
    allVarAttrs.addAll(computedAttrs);

    var.addAll(allVarAttrs);
    coordAttrValues.add(varName);
  }

  @Override
  void createNonElementCoordVars(Template template) {
    // no-op - all coordinate data should be defined in the time series data
  }
}
