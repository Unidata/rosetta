/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.units.SimpleUnit;

/**
 * EOL Sounding Composite (ESC).
 *
 * This class controls the conversion of ESC data to netCDF.
 *
 * Each trajectory in the ESC file (which can contain multiple trajectories) will be converted into
 * a single netCDF file of the format:
 *
 *
 * "Launch Date"_"Site ID".nc
 *
 * An example description of this format can be found here:
 *
 * http://data.eol.ucar.edu/datafile/nph-get/485.043/readme_PECAN_FP2_radiosonde.pdf
 *
 * (thanks to Scot Loehrer <loehrer@ucar.edu>)
 *
 * @author Sean Arms
 */

public class EolSoundingComp {

  private static final String LAUNCH_DATE_KEY = "UTC Release Time (y,m,d,h,m,s)".toLowerCase();
  private static final String SITE_ID_KEY = "Release Site Type/Site ID".toLowerCase();
  private static final String COORDS_ATTR_NAME = "coordinates";
  private static final String AXIS_ATTR_NAME = "axis";
  private static final String POSITIVE_ATTR_NAME = "positive";
  private static final String STD_NAME_ATTR_NAME = "standard_name";
  static Logger log = Logger.getLogger(EolSoundingComp.class.getName());
  private HashMap<String, List<String>> data;
  private List<String> variableNames;
  private List<String> variableUnits;
  private List<Attribute> globalAttrs;
  private String launchDateIsoStr;
  private String siteId;


  public EolSoundingComp() {
  }

  public static void main(String[] args) {
    String escFile = "/Users/lesserwhirls/dev/unidata/rosetta/src/test/testFiles/esc/ELLIS_20150610_reduced.cls";
    escFile = "/Users/lesserwhirls/Downloads/nc/ELLIS_20150610.cls";
    EolSoundingComp escConvertor = new EolSoundingComp();
    if (!escConvertor.convert(escFile).isEmpty()) {
      log.debug("Conversion successful");
    }
  }

  private String cleanUnit(String unitStr) {
    switch (unitStr) {
      case "C":
        unitStr = "degC";
        break;
      case "m/s":
        unitStr = "m.s-1";
        break;
      case "g/kg":
        unitStr = "g.kg-1";
        break;
      case "%":
        unitStr = "0.01";
        break;
      case "code":
        unitStr = "";
        break;
    }
    SimpleUnit unitObj = SimpleUnit.factory(unitStr);

    if (unitObj.isUnknownUnit()) {
      log.debug("Unknown unit: " + unitObj.toString());
    }

    return unitStr;
  }

  private List<Attribute> processLaunchHeaderLine(String line) {
    String headerKey = line.substring(0, 35).trim().replace("\\s+", "-").replace(":", "");
    String headerValue = line.substring(35).trim();
    if (headerKey.toLowerCase().equals(LAUNCH_DATE_KEY)) {
      String[] launchDateVals = headerValue.split(",");
      assert launchDateVals.length == 4;
      launchDateIsoStr = launchDateVals[0] + "-" + launchDateVals[1] + "-" + launchDateVals[2] + "T"
          + launchDateVals[3];
      launchDateIsoStr = launchDateIsoStr.trim().replace(" ", "");
    } else if (headerKey.toLowerCase().equals(SITE_ID_KEY)) {
      String[] siteIdKeys = headerValue.split(",");
      if (siteIdKeys.length == 2) {
        siteId = siteIdKeys[1];
      } else {
        siteId = headerValue;
      }
      siteId = siteId.trim();
      siteId = siteId.replace("\\s+", "_");
      siteId = siteId.replace("\\", "-");
      siteId = siteId.replace("/", "-");
    }
    Attribute attr = new Attribute(headerKey, headerValue);
    if (!globalAttrs.contains(attr)) {
      globalAttrs.add(attr);
    }

    return globalAttrs;
  }

  private void processDataHeaderLine(String line, int dataHeaderLineNum) {
    if (dataHeaderLineNum == 2) {
      // process the variable names
      for (String varName : line.trim().split("\\s+")) {
        varName = varName.trim().replace(" ", "_");
        variableNames.add(varName);
      }
    } else if (dataHeaderLineNum == 3) {
      // process the units
      for (String unitStr : line.trim().split("\\s+")) {
        variableUnits.add(cleanUnit(unitStr));
      }
    }
  }

  private void processDataLine(String line) {
    String[] dataLine = line.trim().split("\\s+");
    List<String> tmpDataList;
    String varName;

    // make sure the number of data variable names is the same as the
    // number of datum contained in a line of data.
    // if this is not true, then the header parsing is incorrect, or the
    // header is wrong
    assert dataLine.length == variableNames.size();

    int varCount = 0;
    for (String datum : dataLine) {
      varName = variableNames.get(varCount);

      // check if data list for variable exists, and if so, update it.
      // if not, create new list
      if (data.containsKey(varName)) {
        tmpDataList = data.get(varName);
      } else {
        tmpDataList = new ArrayList<>();
      }

      tmpDataList.add(datum);
      data.put(varName, tmpDataList);
      varCount++;
    }
  }

  private boolean writeNcFile(String ncFileName) {

    boolean success = false;
    int timeIndex = -1;
    int latIndex = -1;
    int lonIndex = -1;
    int altIndex = -1;
    String varName;
    List<Attribute> attrsToAdd;

    // get launch date from launch header

    // try to find lat, lon, height, and time variableName indexes
    for (int var = 0; var < variableNames.size(); var++) {
      varName = variableNames.get(var);
      if (varName.toLowerCase().contains("lat")) {
        latIndex = var;
      } else if (varName.toLowerCase().contains("lon")) {
        lonIndex = var;
      } else if (varName.toLowerCase().contains("time")) {
        timeIndex = var;
      } else if (varName.toLowerCase().contains("alt")) {
        altIndex = var;
      }
    }

    // use the index info from above to create the coordinates attribute
    String coordsStr = variableNames.get(timeIndex) + " " + variableNames.get(latIndex) + " " +
        variableNames.get(lonIndex) + " " + variableNames.get(altIndex);
    Attribute coordsAttr = new Attribute(COORDS_ATTR_NAME, coordsStr);

    int numberOfTimes = data.get(variableNames.get(0)).size();
    Dimension timeDim = new Dimension("time", numberOfTimes);

    try (NetcdfFileWriter ncfw = NetcdfFileWriter
        .createNew(NetcdfFileWriter.Version.netcdf3, ncFileName)) {
      Group group = null;

      // create time dimension
      ncfw.addDimension(group, timeDim.getFullName(), timeDim.getLength());

      // add global attribute
      for (Attribute ga : globalAttrs) {
        ncfw.addGroupAttribute(group, ga);
      }

      // add the needed CF traj attributes
      ncfw.addGroupAttribute(group, new Attribute("Conventions", "CF-1.6"));
      ncfw.addGroupAttribute(group, new Attribute("featureType", "trajectory"));

      // create list of data variables:
      for (int varInd = 0; varInd < variableNames.size(); varInd++) {
        attrsToAdd = new ArrayList<>();

        if (varInd == timeIndex) {
          attrsToAdd.add(new Attribute(AXIS_ATTR_NAME, "T"));
          attrsToAdd.add(new Attribute(STD_NAME_ATTR_NAME, "time"));
          String tmpTimeUnit = variableUnits.get(varInd);
          if (tmpTimeUnit.equals("s") || tmpTimeUnit.equals("sec")) {
            tmpTimeUnit = "seconds";
          }
          attrsToAdd.add(new Attribute("units", tmpTimeUnit + " since " + launchDateIsoStr));
        } else if (varInd == lonIndex) {
          attrsToAdd.add(new Attribute(AXIS_ATTR_NAME, "X"));
          attrsToAdd.add(new Attribute(STD_NAME_ATTR_NAME, "longitude"));
          attrsToAdd.add(new Attribute("units", variableUnits.get(varInd)));
        } else if (varInd == latIndex) {
          attrsToAdd.add(new Attribute(AXIS_ATTR_NAME, "Y"));
          attrsToAdd.add(new Attribute(STD_NAME_ATTR_NAME, "latitude"));
          attrsToAdd.add(new Attribute("units", variableUnits.get(varInd)));
        } else if (varInd == altIndex) {
          attrsToAdd.add(new Attribute(AXIS_ATTR_NAME, "Z"));
          attrsToAdd.add(new Attribute(STD_NAME_ATTR_NAME, "altitude"));
          attrsToAdd.add(new Attribute(POSITIVE_ATTR_NAME, "UP"));
          attrsToAdd.add(new Attribute("units", variableUnits.get(varInd)));

        } else {
          attrsToAdd.add(new Attribute("units", variableUnits.get(varInd)));
          attrsToAdd.add(coordsAttr);
        }

        // create the variable and add the attributes
        Variable theNewVar = ncfw
            .addVariable(group, variableNames.get(varInd), DataType.FLOAT, timeDim.getFullName());
        for (Attribute attr : attrsToAdd) {
          ncfw.addVariableAttribute(theNewVar, attr);
        }
      }

      // add the special trajectory variable which has a cf role as "trajectory_id"
      Dimension trajIdDim = new Dimension("str_len", siteId.length());

      ncfw.addDimension(group, trajIdDim.getFullName(), trajIdDim.getLength());

      Variable trajId = ncfw
          .addVariable(group, "trajectory", DataType.CHAR, trajIdDim.getFullName());
      trajId.addAttribute(new Attribute("cf_role", "trajectory_id"));

      // create the file - writes medata and basic structure
      ncfw.create();

      // write the data, yo!
      for (String dataVar : data.keySet()) {
        Array thisData = ArrayFloat.makeArray(DataType.FLOAT, data.get(dataVar));
        Variable thisVar = ncfw.findVariable(dataVar);
        ncfw.write(thisVar, thisData);
      }

      // write out trajectory id
      Array siteIdArr = ArrayChar.makeFromString(siteId, siteId.length());
      ncfw.write(trajId, siteIdArr);

      // close shop
      ncfw.close();
      success = true;
    } catch (IOException | InvalidRangeException ioe) {
      log.debug(ioe.getMessage());
    }

    return success;
  }

  private void initNewDataBlock() {
    variableNames = new ArrayList<>();
    variableUnits = new ArrayList<>();
    globalAttrs = new ArrayList<>();
    data = new HashMap<>();
  }

  public List<String> convert(String escFile) {

    // local vars
    List<String> convertedFiles = new ArrayList<>();
    boolean readSuccess = false;
    boolean parseSuccess = false;
    boolean writeSuccess = false;
    int dataHeaderLineNum;
    String line;

    Path escPath = Paths.get(escFile);
    try (BufferedReader br = Files.newBufferedReader(escPath)) {
      // read first line in file - should start off with the
      // launch header.
      line = br.readLine();
      while (br.ready() && line != null) {
        readSuccess = true;
        // read through the lines in the esc file as long as
        // there are data to read
        //
        // Assume we are starting a new block of data, so reset all
        // fields
        this.initNewDataBlock();
        // this assumes data file starts with a launch header
        // stop when the "/" character is reached
        log.debug("Processing Launch Header");
        while (!line.equals("/")) {
          processLaunchHeaderLine(line);
          line = br.readLine();
        }
        parseSuccess = true;
        // the "/" character represents the break between
        // the launch header and the data header, so just read over those;
        while (line.equals("/")) {
          line = br.readLine();
        }
        // Now we read the data header lines
        // Stop at the line with "------" as this splits the data header
        // from the actual data
        log.debug("Processing Data Header");
        dataHeaderLineNum = 1;
        while (!line.contains("------")) {
          processDataHeaderLine(line, dataHeaderLineNum);
          line = br.readLine();
          dataHeaderLineNum++;
        }
        // Check to see if we have the same number of variables and number of units
        assert this.variableNames.size() == this.variableUnits.size();

        // the current line will be the seperator between the data header
        // and the data block. Let's read the next line and skip it.
        line = br.readLine();

        boolean readData = true;
        log.debug("Processing Data Block");
        while (readData) {
          try {
            // if this is a data line then the first value from a split on " " should be convertible to
            // a float value
            float v = Float.parseFloat(line.trim().split("\\s+")[0]);
            processDataLine(line);
            line = br.readLine();
            if (line == null) {
              readData = false;
            }
            parseSuccess = true;
          } catch (NumberFormatException nfe) {
            // this is what happens when you try to convert a string to a float,
            // and the string does not represent a number. This is our clue that we've
            // reached the end of a data block.
            readData = false;
          }
        }

        // now we have parsed the launch header, the data header, and the data for
        // a given block of data within the ESC file. Now time to setup the netCDF
        // file for this block of data!
        String ncFile = launchDateIsoStr.replace(":", "") + "-" + siteId + ".nc";
        String ncFileName = escPath.getParent().toString() + File.separator + ncFile;
        if (writeNcFile(ncFileName)) {
          convertedFiles.add(ncFileName);
        }
      }
    } catch (IOException ioe) {
      readSuccess = false;
      log.debug(ioe.getMessage());
    }

    return convertedFiles;
  }
}
