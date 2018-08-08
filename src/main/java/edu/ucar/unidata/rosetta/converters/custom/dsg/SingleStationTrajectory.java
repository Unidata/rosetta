/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.custom.dsg;

import edu.ucar.unidata.rosetta.domain.AsciiFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import org.apache.log4j.Logger;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayScalar;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;


/**
 * Service for parsing file data.
 */
public class SingleStationTrajectory extends NetcdfFileManager {

  protected static Logger logger = Logger.getLogger(SingleStationTrajectory.class);

  public SingleStationTrajectory() {
    super.setMyCfRole("trajectory_id");
    super.setMyDsgType("trajectory");
  }

  @Override
  protected void createCoordinateAttr(boolean hasRelTime) {
    // need to build the coord attr using the variables removed from the coordinate list
    // in the templatecontroller.
    Map<String, String> removedCoordVars = getOtherInfo();
    String ca = "";
    for (String coordType : removedCoordVars.keySet()) {
      ca = ca + " " + removedCoordVars.get(coordType);
    }

    // if we have a relTime (a complete time variable), use that variable name
    // otherwise, use a (yet-to-be-created) variable called "time"
    if (hasRelTime) {
      setCoordAttr(getRelTimeVarName() + ca);
    } else {
      setCoordAttr("dateTime" + ca);
    }
  }

  // name: "latitude", "longitude", "altitude"
  protected NetcdfFileWriter createCoordVarsFromPlatform(NetcdfFileWriter ncFileWriter, String name)
      throws IOException, InvalidRangeException {

    String coordVarUnits = getPlatformMetadataMap().get(name + "Units");
    Boolean unitChanged = false;
    //if (coordVarUnits.equals("degrees_west")) {
    //    coordVarUnits = "degrees_east";
    //    unitChanged=true;
    //} else if (coordVarUnits.equals("degrees_south")) {
    //    coordVarUnits = "degrees_north";
    //    unitChanged=true;
    //}
    String coordVarVal = getPlatformMetadataMap().get(name);

    //if (unitChanged) {
    //    coordVarVal = "-" + coordVarVal;
    //}

    Variable theVar = ncFileWriter.addVariable(null, name.substring(0, 3), DataType.FLOAT, "");

    ncFileWriter.addVariableAttribute(theVar, new Attribute("units", coordVarUnits));
    ncFileWriter.addVariableAttribute(theVar, new Attribute("standard_name", name));
    ncFileWriter.addVariableAttribute(theVar, new Attribute("_platformMetadata", "true"));

    if (!name.equals("altitude")) {
      ncFileWriter.addVariableAttribute(theVar, new Attribute("long_name", name));
    } else {
      ncFileWriter
          .addVariableAttribute(theVar, new Attribute("long_name", "height above mean sea-level"));
      ncFileWriter.addVariableAttribute(theVar, new Attribute("positive", "up"));
      ncFileWriter.addVariableAttribute(theVar, new Attribute("axis", "Z"));
    }

    return ncFileWriter;
  }

  protected NetcdfFileWriter writeCoordVarsFromPlatform(NetcdfFileWriter ncFileWriter, String name)
      throws IOException, InvalidRangeException {

    String coordVarUnits = getPlatformMetadataMap().get(name + "Units");
    Boolean unitChanged = false;
    //if (coordVarUnits.equals("degrees_west")) {
    //    unitChanged=true;
    //} else if (coordVarUnits.equals("degrees_south")) {
    //    unitChanged=true;
    //}
    String coordVarVal = getPlatformMetadataMap().get(name);

    //if (unitChanged) {
    //    coordVarVal = "-" + coordVarVal;
    //}

    Variable theVar = ncFileWriter.findVariable(name.substring(0, 3));

    ncFileWriter.write(theVar, new ArrayScalar(Float.parseFloat(coordVarVal)));

    return ncFileWriter;
  }

  protected NetcdfFileWriter createPlatformInfo(NetcdfFileWriter ncFileWriter, AsciiFile file) {
    String stationName = getPlatformMetadataMap().get("platformName").toString()
        .replaceAll(" ", "_");

    Variable theVar = ncFileWriter
        .addStringVariable(null, "station_name", new ArrayList<Dimension>(),
            stationName.length());

    theVar.addAttribute(new Attribute("cf_role", getMyCfRole()));
    theVar.addAttribute(new Attribute("long_name", "station name"));
    theVar.addAttribute(new Attribute("standard_name", "station_name"));
    theVar.addAttribute(new Attribute("_platformMetadata", "true"));

    return ncFileWriter;
  }

  protected NetcdfFileWriter writePlatformInfo(NetcdfFileWriter ncFileWriter, AsciiFile file) {
    String stationName = getPlatformMetadataMap().get("platformName").toString()
        .replaceAll(" ", "_");
    char[] stationNameCharArray = stationName.toCharArray();
    Variable theVar = ncFileWriter.findVariable("station_name");

    ArrayChar sa = new ArrayChar.D1(stationName.length());
    for (int i = 0; i < stationName.length(); i++) {
      sa.setChar(i, stationNameCharArray[i]);
    }

    try {
      ncFileWriter.write(theVar, sa);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InvalidRangeException e) {
      e.printStackTrace();
    }

    return ncFileWriter;
  }
}