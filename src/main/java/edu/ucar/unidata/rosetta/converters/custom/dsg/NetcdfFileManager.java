/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.custom.dsg;

import edu.ucar.unidata.rosetta.converters.custom.dsg.util.DateTimeBluePrint;
import edu.ucar.unidata.rosetta.domain.AsciiFile;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

/**
 * Handle writing out dsg netcdf file data.
 *
 * @author sarms@ucar.edu
 */
public abstract class NetcdfFileManager {

  protected static Logger logger = Logger.getLogger(NetcdfFileManager.class);

  private String myCfRole;
  private String myDsgType;

  private Map<String, Map<String, String>> variableMetadataMap;
  private Map<String, String> variableNameMap;
  private Map<String, String> platformMetadataMap;
  private Map<String, String> generalMetadataMap;
  private Map<String, String> otherInfo;
  private List<String> header;

  private HashMap<String, Integer> nameCounts;
  private String coordAttr;
  private List<String> usedVarNames = new ArrayList<String>();
  private List<String> allVarNames = new ArrayList<String>();
  private String shapeAttr;
  private List<String> coordVarList;
  private DateTimeBluePrint dateTimeBluePrint;
  private HashMap<String, ArrayList<String>> coordVars;
  private ArrayList<String> buildTimeTriggers;
  private String relTimeVarName;
  private List<String> nonCoordVarList;
  private List<String> parseHeaderForMetadataList;

  public Map<String, String> getOtherInfo() {
    return this.otherInfo;
  }

  public void setOtherInfo(HashMap<String, String> otherInfo) {
    this.otherInfo = otherInfo;
  }

  public List<String> getHeader() {
    return header;
  }

  public void setHeader(List<String> header) {
    this.header = header;
  }

  public String getMyDsgType() {
    return myDsgType;
  }

  public void setMyDsgType(String myDsgType) {
    this.myDsgType = myDsgType;
  }

  public String getMyCfRole() {
    return myCfRole;
  }

  public void setMyCfRole(String cfRole) {
    this.myCfRole = cfRole;
  }

  public DateTimeBluePrint getDateTimeBluePrint() {
    return dateTimeBluePrint;
  }

  public void setDateTimeBluePrint(DateTimeBluePrint dateTimeBluePrint) {
    this.dateTimeBluePrint = dateTimeBluePrint;
  }

  public HashMap<String, Integer> getNameCounts() {
    return nameCounts;
  }

  public void setNameCounts(HashMap<String, Integer> nameCounts) {
    this.nameCounts = nameCounts;
  }

  public String getCoordAttr() {
    return coordAttr;
  }

  public void setCoordAttr(String coordAttr) {
    this.coordAttr = coordAttr;
  }

  public List<String> getUsedVarNames() {
    return usedVarNames;
  }

  public void setUsedVarNames(List<String> usedVarNames) {
    this.usedVarNames = usedVarNames;
  }

  public List<String> getAllVarNames() {
    return allVarNames;
  }

  public void setAllVarNames(List<String> allVarNames) {
    this.allVarNames = allVarNames;
  }

  public String getShapeAttr() {
    return shapeAttr;
  }

  public void setShapeAttr(String shapeAttr) {
    this.shapeAttr = shapeAttr;
  }

  public ArrayList<String> getBuildTimeTriggers() {
    return buildTimeTriggers;
  }

  public void setBuildTimeTriggers(ArrayList<String> buildTimeTriggers) {
    this.buildTimeTriggers = buildTimeTriggers;
  }

  public String getRelTimeVarName() {
    return relTimeVarName;
  }

  public void setRelTimeVarName(String relTimeVarName) {
    this.relTimeVarName = relTimeVarName;
  }

  public HashMap<String, ArrayList<String>> getCoordVars() {
    return coordVars;
  }

  public void setCoordVars(HashMap<String, ArrayList<String>> coordVars) {
    this.coordVars = coordVars;
  }

  public List<String> getNonCoordVarList() {
    return nonCoordVarList;
  }

  public void setNonCoordVarList(List<String> nonCoordVarList) {
    this.nonCoordVarList = nonCoordVarList;
  }

  public List<String> getCoordVarList() {
    return coordVarList;
  }

  public void setCoordVarList(List<String> coordVarList) {
    this.coordVarList = coordVarList;
  }

  public Map<String, Map<String, String>> getVariableMetadataMap() {
    return variableMetadataMap;
  }

  public void setVariableMetadataMap(Map<String, Map<String, String>> variableMetadataMap) {
    this.variableMetadataMap = variableMetadataMap;
  }

  private List<String> getParseHeaderForMetadataList() {
    return this.parseHeaderForMetadataList;
  }

  private void setParseHeaderForMetadataList(List<String> parseHeaderForMetadataList) {
    this.parseHeaderForMetadataList = parseHeaderForMetadataList;
  }

  public Map<String, String> getVariableNameMap() {
    return variableNameMap;
  }

  public void setVariableNameMap(Map<String, String> variableNameMap) {
    this.variableNameMap = variableNameMap;
  }

  public Map<String, String> getPlatformMetadataMap() {
    return platformMetadataMap;
  }

  public void setPlatformMetadataMap(Map<String, String> platformMetadataMap) {
    processPatternsOnMetadataMap(platformMetadataMap);
    this.platformMetadataMap = platformMetadataMap;
  }

  private void processPatternsOnMetadataMap(Map<String, String> metadataMap) {
    for (String key : this.getParseHeaderForMetadataList()) {
      String pattern = metadataMap.get(key);
      if (pattern != null) {
        boolean found = false;
        for (String line : header) {
          Pattern p = Pattern.compile(pattern);
          Matcher m = p.matcher(line);
          if (m.find() && m.groupCount() > 0) {
            metadataMap.put(key, m.group(1));
            found = true;
            break;
          }
        }
        if (!found) {
          throw new IllegalArgumentException(
              "The pattern for '" + key + "':'" + pattern + "' did not match any line");
        }
      }
    }
  }

  public Map<String, String> getGeneralMetadataMap() {
    return generalMetadataMap;
  }

  public void setGeneralMetadataMap(Map<String, String> generalMetadataMap) {
    processPatternsOnMetadataMap(generalMetadataMap);
    this.generalMetadataMap = generalMetadataMap;
  }

  public boolean isMine(String reqType) {
    boolean mine = false;
    if (getMyDsgType().equals(reqType)) {
      mine = true;
    }
    return mine;
  }

  protected void init(AsciiFile file, List<String> header) {

    setUsedVarNames(new ArrayList<String>());
    setAllVarNames(new ArrayList<String>());

    setHeader(header);
    setParseHeaderForMetadataList(file.getParseHeaderForMetadataList());
    setVariableNameMap(file.getVariableNameMap());
    setVariableMetadataMap(file.getVariableMetadataMap());
    setPlatformMetadataMap(file.getPlatformMetadataMap());
    setGeneralMetadataMap(file.getGeneralMetadataMap());
    setOtherInfo(file.getOtherInfo());

    // create a list of the various types of time variables a user
    // can supply. These need to be carefully handled in the code!
    // these can be found in the addContentToDialog() function in
    // src/main/webapp/resources/js/SlickGrid/custom/variableSpecification.js
    // Note that "relTime" isn't included, as a relative time (e.g. days since 1970)
    // can be handled as any other variable (i.e. we do not need to build
    // anything special to handle this).
    //
    String[] specialTimeNames = {"fullDateTime", "dateOnly", "timeOnly"};
    setBuildTimeTriggers(new ArrayList<String>());
    ArrayList<String> btc = getBuildTimeTriggers();
    btc.addAll(Arrays.asList(specialTimeNames));
    setBuildTimeTriggers(btc);
    setMyDsgType(file.getCfType());
    setNameCounts(new HashMap<String, Integer>());
  }

  protected NetcdfFileWriter writeRosettaInfo(NetcdfFileWriter ncFileWriter, String rosettaJson) {
    char[] rosettaJsonCharArray = rosettaJson.toCharArray();

    Variable theVar = ncFileWriter.findVariable("Rosetta");

    ArrayChar sa = new ArrayChar.D1(rosettaJson.length());
    for (int i = 0; i < rosettaJson.length(); i++) {
      sa.setChar(i, rosettaJsonCharArray[i]);
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

  protected NetcdfFileWriter addCfAttributes(NetcdfFileWriter ncFileWriter) {
    ncFileWriter.addGroupAttribute(null, new Attribute("Conventions", "CF-1.6"));
    ncFileWriter.addGroupAttribute(null, new Attribute("featureType", getMyDsgType()));

    return ncFileWriter;
  }

  protected Boolean findCoordAndNonCoordVars() {
    Set<String> variableNameKeys = getVariableNameMap().keySet();
    Iterator<String> variableNameKeysIterator = variableNameKeys.iterator();
    String key, value;
    Map<String, String> variableMetadata = new HashMap<>();
    // check to see if user supplied a relTime (i.e. days since yyyy-mm-dd) If so,
    // then we do not need to construct a time variable. If not, then we need to create
    // a time dimension.
    Boolean hasRelTime = false;
    nonCoordVarList = new ArrayList<String>();
    coordVarList = new ArrayList<String>();
    coordVars = new HashMap<String, ArrayList<String>>();
    while (variableNameKeysIterator.hasNext()) {
      //ex:
      // key = "variableName1"
      // value = "time"
      key = variableNameKeysIterator.next();
      value = getVariableNameMap().get(key);
      if (!value.equals("Do Not Use")) {
        //variableMetadata = getVariableMetadataMap().get(key + "Metadata");
        variableMetadata = getVariableMetadataMap().get(key);
        boolean updatedMetadataMap = false;
        // check if variable is a coordinate variable!
        if (variableMetadata.containsKey("_coordinateVariable")) {
          String coordVarType = variableMetadata.get("_coordinateVariableType");
          if (variableMetadata.get("_coordinateVariable").equals("coordinate")
              && (!getBuildTimeTriggers().contains(coordVarType))) {
            if (coordVarType.toLowerCase().equals("reltime")) {
              hasRelTime = true;
              relTimeVarName = value;
            }
            coordVarList = coordVars.get(coordVarType);
            if (coordVarList == null) {
              coordVarList = new ArrayList<String>();
            }
            coordVarList.add(key);
            coordVars.put(coordVarType, (ArrayList<String>) coordVarList);
          } else {
            if (coordVarType != null) {
              if (coordVarType.toLowerCase().contains("only") ||
                  coordVarType.toLowerCase().equals("fulldatetime")) {
                // this is a time variable that does not have udunit compliant unit,
                // so rename the unit attribute to format
                if (variableMetadata.containsKey("units")) {
                  variableMetadata.put("format", variableMetadata.get("units"));
                  variableMetadata.remove("units");
                  updatedMetadataMap = true;
                }
              }
            }
            nonCoordVarList.add(key);
          }
        } else {
          nonCoordVarList.add(key);
        }

        if (updatedMetadataMap) {
          variableMetadataMap.put(key + "Metadata", variableMetadata);
        }
      }
    }

    return hasRelTime;
  }

  protected HashMap<String, ArrayList<String>> extractTimeRelatedVars(NetcdfFileWriter ncFileWriter,
      List<String> ncFileVariableNames) {
    HashMap<String, ArrayList<String>> timeRelatedVars = new HashMap<String, ArrayList<String>>();
    String timeType;
    for (String varName : ncFileVariableNames) {
      Variable theVar = ncFileWriter.findVariable(varName);
      if (theVar != null) {
        Attribute triggerAttr = theVar.findAttributeIgnoreCase("_coordinateVariableType");
        if (triggerAttr != null) {
          timeType = theVar.findAttributeIgnoreCase("_coordinateVariableType").getStringValue(0);
          if (timeRelatedVars.containsKey(timeType)) {
            ArrayList<String> tmpArray = timeRelatedVars.get(timeType);
            tmpArray.add(varName);
            timeRelatedVars.put(timeType, tmpArray);
          } else {
            ArrayList<String> singleValArray = new ArrayList<>();
            singleValArray.add(varName);
            timeRelatedVars.put(timeType, singleValArray);
          }
        }
      }
    }

    return timeRelatedVars;
  }

  protected NetcdfFileWriter findAndCreateCoordVars(NetcdfFileWriter ncFileWriter,
      List<List<String>> parsedDataFile) {
    String key;
    for (String coordType : getCoordVars().keySet()) {
      Iterator<String> coordVarNameIterator = getCoordVars().get(coordType).iterator();
      while (coordVarNameIterator.hasNext()) {
        key = coordVarNameIterator.next();
        ncFileWriter = createNcfVariable(ncFileWriter, key, parsedDataFile);
      }
    }
    return ncFileWriter;
  }

  protected NetcdfFileWriter findAndCreateNonCoordVars(NetcdfFileWriter ncFileWriter,
      List<List<String>> parsedDataFile) {
    Iterator<String> nonCoordVarNameIterator = getNonCoordVarList().iterator();
    String key;
    while (nonCoordVarNameIterator.hasNext()) {
      key = nonCoordVarNameIterator.next();
      ncFileWriter = createNcfVariable(ncFileWriter, key, parsedDataFile);
    }

    return ncFileWriter;
  }

  protected NetcdfFileWriter createNcfVariable(NetcdfFileWriter ncFileWriter,
      String sessionStorageKey, List<List<String>> outerList) {
    ncFileWriter = createNcfVariable(ncFileWriter, sessionStorageKey, outerList, null);
    return ncFileWriter;
  }

  protected NetcdfFileWriter createNcfVariable(NetcdfFileWriter ncFileWriter,
      String sessionStorageKey, List<List<String>> outerList, String coordType) {
    String userName = getVariableNameMap().get(sessionStorageKey);
    String varName = userName.replace(" ", "_");

    if (!nameCounts.containsKey(varName)) {
      usedVarNames.add(varName);
      nameCounts.put(varName, 1);
    } else {
      Integer newCount = nameCounts.get(varName) + 1;
      nameCounts.put(varName, newCount);
      varName = varName + "_" + newCount.toString();
    }
    allVarNames.add(varName);
    //Map variableMetadata = getVariableMetadataMap().get(sessionStorageKey + "Metadata");
    Map variableMetadata = getVariableMetadataMap().get(sessionStorageKey);
    Object coordVarTypeOb = variableMetadata.get("_coordinateVariableType");
    String coordVarType = "";
    if (coordVarTypeOb != null) {
      coordVarType = coordVarTypeOb.toString();
    }

    String name = varName;
    String shape = shapeAttr.replace(" ", ",");
    shape = shapeAttr;

    if (getCoordVars().containsKey(coordVarType)) {
      if (getCoordVars().get(coordVarType).contains(sessionStorageKey)) {
        // FIXME: efficient line
        name = name;
        shape = coordVarType;
      }
    }

    Iterator it = variableMetadata.entrySet().iterator();
    String type = (String) variableMetadata.get("dataType");
    DataType ncType = null;
    if (type.equals("Text")) {
      ncType = DataType.CHAR;
    } else if (type.equals("Integer")) {
      ncType = DataType.INT;
    } else if (type.equals("Float")) {
      ncType = DataType.FLOAT;
    }

    Variable theVar = null;
    if (ncType == DataType.CHAR) {
      int colNum = Integer.parseInt(sessionStorageKey.replace("variableName", ""));
      int charLen = outerList.get(0).get(colNum).length();
      ArrayList<Dimension> dims = new ArrayList<>();
      for (String dimStr : shape.split(",")) {
        dims.add(ncFileWriter.renameDimension(null, dimStr, dimStr));
      }
      theVar = ncFileWriter.addStringVariable(null, name, dims, charLen);
    } else {
      theVar = ncFileWriter.addVariable(null, name, ncType, shape);
    }

    if (!userName.equals(varName)) {
      ncFileWriter.addVariableAttribute(theVar, new Attribute("_userSuppliedName", userName));
    }
    // These Keys will always be strings
    @SuppressWarnings("unchecked")
    Set<String> variableMetadataKeys = variableMetadata.keySet();
    Iterator<String> variableMetadataKeysIterator = variableMetadataKeys.iterator();
    String metadataKey;
    String metadataValue;
    while (variableMetadataKeysIterator.hasNext()) {
      metadataKey = variableMetadataKeysIterator.next();
      metadataValue = variableMetadata.get(metadataKey).toString();
      switch (metadataKey) {
        case "dataType":
          break;
        case "missing_value":
        case "_FillValue":
        case "valid_min":
        case "valid_max":
          // TODO: valid range?
          // TODO: flag_values?
          String[] arrayString = {metadataValue};
          DataType varType = ncType;
          if (varType == DataType.CHAR) {
            varType = DataType.STRING;
          }
          Array array = Array.makeArray(varType, arrayString);
          Attribute attribute = new Attribute(metadataKey, array);
          ncFileWriter.addVariableAttribute(theVar, attribute);
          break;
        default:
          ncFileWriter.addVariableAttribute(theVar, new Attribute(metadataKey, metadataValue));
          break;
      }
    }

    String columnId = sessionStorageKey.replace("variableName", "");
    ncFileWriter.addVariableAttribute(theVar, new Attribute("_columnId", columnId));

    if (getNonCoordVarList().contains(sessionStorageKey)) {
      // we can have coord like variables (time only, date only, etc) that are not
      // stored as coordinate variables, but they don't need the coordAttr. To filter
      // these out, check to see if coordVarType is "", which is the default.
      if (coordVarType == "") {
        ncFileWriter.addVariableAttribute(theVar, new Attribute("coordinates", coordAttr));
      }
    }

    if (coordType != null) {
      if (getBuildTimeTriggers().contains(coordType)) {
        ncFileWriter.addVariableAttribute(theVar, new Attribute("timeRelatedVariable", "true"));
      }
    }

    return ncFileWriter;
  }

  protected NetcdfFileWriter createTimeDimension(NetcdfFileWriter ncFileWriter,
      int parseFileDataSize) {
    ncFileWriter.addDimension(null, "dateTime", parseFileDataSize);
    if (getShapeAttr().equals("")) {
      setShapeAttr("dateTime");
    } else {
      setShapeAttr(getShapeAttr() + " dateTime");
    }

    return ncFileWriter;
  }

  protected NetcdfFileWriter createRosettaInfo(NetcdfFileWriter ncFileWriter, String rosettaJson) {
    //todo get version info from war file, similar to ServerInfoBean.java
    Variable theVar = ncFileWriter.addStringVariable(null, "Rosetta", new ArrayList<Dimension>(),
        rosettaJson.length());

    theVar.addAttribute(new Attribute("long_name", "Rosetta front-end sessionStorage JSON String"));
    theVar.addAttribute(new Attribute("version", "0.2"));

    return ncFileWriter;
  }

  protected void checkDownloadDir(String downloadDirPath) throws IOException {
    File downloadTarget = new File(downloadDirPath);
    if (!downloadTarget.exists()) {
      logger.warn("created download path");
      if (!downloadTarget.mkdirs()) {
        throw new IOException(
            "Unable to create download directory " + downloadTarget.getAbsolutePath());
      }
    }
  }

  protected NetcdfFileWriter createNetcdfFileHeader(AsciiFile file, String ncFilePath,
      List<List<String>> parseFileData, Boolean hasRelTime)
      throws IOException, InvalidRangeException {
    // create ncFileWriter
    NetcdfFileWriter ncFileWriter = NetcdfFileWriter
        .createNew(NetcdfFileWriter.Version.netcdf3, ncFilePath);

    // create dimensions based on the coordVars found above
    logger.warn("create dimensions in ncml/n");
    ncFileWriter = createDimensions(ncFileWriter, parseFileData);

    // now, if relTime was not supplied as a coordVarType, then we know we will need to build a time variable
    // later. Let's create a "time" dimension that will go with our (to-be-created-later) time variable
    if (!hasRelTime) {
      ncFileWriter = createTimeDimension(ncFileWriter, parseFileData.size());
    }

    // create a coordinate attribute based on CF1.6 DSG
    createCoordinateAttr(hasRelTime);

    // CF specific attribute elements
    ncFileWriter = addCfAttributes(ncFileWriter);

    // global metadata

    for (Map.Entry<String, String> entry : getGeneralMetadataMap().entrySet()) {
      if (entry.getValue() != null) {
        ncFileWriter.addGroupAttribute(null, new Attribute(entry.getKey(), entry.getValue()));
      }
    }

    // platform metadata

    // Latitude
    if (getPlatformMetadataMap().containsKey("latitude")) {
      ncFileWriter = createCoordVarsFromPlatform(ncFileWriter, "latitude");
    }

    // Longitude
    if (getPlatformMetadataMap().containsKey("longitude")) {
      ncFileWriter = createCoordVarsFromPlatform(ncFileWriter, "longitude");
    }

    // Altitude
    if (getPlatformMetadataMap().containsKey("altitude")) {
      ncFileWriter = createCoordVarsFromPlatform(ncFileWriter, "altitude");
    }

    // Platform ID
    if (getPlatformMetadataMap().containsKey("platformName")) {
      ncFileWriter = createPlatformInfo(ncFileWriter, file);
    }

    // look at coordVars and create the appropriate coordinate variables
    ncFileWriter = findAndCreateCoordVars(ncFileWriter, parseFileData);

    // create variables for the non-coordinate variables
    ncFileWriter = findAndCreateNonCoordVars(ncFileWriter, parseFileData);

    // Add rosetta specific info
    ncFileWriter = createRosettaInfo(ncFileWriter, file.getJsonStrSessionStorage());

    // check to se if we need to construct new dateTime variables
    HashMap<String, ArrayList<String>> timeRelatedVars = extractTimeRelatedVars(ncFileWriter,
        getUsedVarNames());
    if (!hasRelTime) {
      dateTimeBluePrint = new DateTimeBluePrint(timeRelatedVars, ncFileWriter);
      if (!dateTimeBluePrint.isEmpty()) {
        ncFileWriter = dateTimeBluePrint.createNewVars(ncFileWriter);
      }
    }
    ncFileWriter.create();

    return ncFileWriter;
  }

  protected NetcdfFileWriter createDimensions(NetcdfFileWriter ncFileWriter,
      List<List<String>> parseFileData) {
    Iterator<String> coordVarNameIterator;

    // we only want one time related dimension. So, if relTime is supplied, then
    // we only want to use it; if time spans multiple cols, then we will create
    // a "time" dimension, and assemble the "time" coordinate variable later
    setShapeAttr("");
    for (String coordType : getCoordVars().keySet()) {
      // don't create a dimension for the partial time variables that will need to
      // be assembled.
      if (!getBuildTimeTriggers().contains(coordType)) {
        // coordAttr is the attribute that defines the coord system for variables
        if (getShapeAttr().equals("")) {
          setShapeAttr(coordType);
        } else {
          setShapeAttr(getShapeAttr() + " " + coordType);
        }
        coordVarNameIterator = getCoordVars().get(coordType).iterator();
        while (coordVarNameIterator.hasNext()) {
          // set dimension name based on coordType
          ncFileWriter.addDimension(null, coordType, parseFileData.size());
          coordVarNameIterator.next();
        }
      }
    }

    return ncFileWriter;
  }

  protected NetcdfFileWriter writeUserVarData(List<List<String>> outerList,
      NetcdfFileWriter ncFileWriter, Locale locale)
      throws IOException, InvalidRangeException, ParseException {
    NumberFormat nFormat = NumberFormat.getInstance(locale);
    for (String var : getAllVarNames()) {
      Variable theVar = ncFileWriter.findVariable(var);
      if (theVar != null) {
        Attribute attr = theVar.findAttributeIgnoreCase("_columnId");
        if (attr != null) {
          String varName = theVar.getFullName();
          DataType dt = theVar.getDataType();
          int varIndex = Integer.parseInt(attr.getStringValue());
          int len = outerList.size();
          if (dt.equals(DataType.FLOAT)) {
            ArrayFloat.D1 vals =
                new ArrayFloat.D1(outerList.size());
            int i = 0;
            for (List<String> innerList : outerList) {
              Number number = nFormat.parse(innerList.get(varIndex));
              float f = number.floatValue();
              vals.set(i, f);
              i++;
            }
            ncFileWriter.write(theVar, vals);
          } else if (dt.equals(DataType.INT)) {
            ArrayInt.D1 vals =
                new ArrayInt.D1(outerList.size(), false);
            int i = 0;
            for (List<String> innerList : outerList) {
              int f = Integer.parseInt(
                  innerList.get(
                      varIndex));
              vals.set(i, f);
              i++;
            }
            ncFileWriter.write(theVar, vals);
          } else if (dt.equals(DataType.CHAR)) {
            assert theVar.getRank() == 2;
            int elementLength = theVar.getDimension(1).getLength();

            ArrayChar.D2 vals =
                new ArrayChar.D2(outerList.size(), elementLength);
            int i = 0;
            for (List<String> innerList : outerList) {

              String f = innerList.get(varIndex);
              vals.setString(i, f);
              i++;
            }
            ncFileWriter.write(theVar, vals);
          }
        }
      }
    }

    return ncFileWriter;
  }

  protected abstract NetcdfFileWriter writeCoordVarsFromPlatform(NetcdfFileWriter ncFileWriter,
      String name) throws IOException, InvalidRangeException;

  protected abstract NetcdfFileWriter createCoordVarsFromPlatform(NetcdfFileWriter ncFileWriter,
      String name) throws IOException, InvalidRangeException;

  protected abstract NetcdfFileWriter createPlatformInfo(NetcdfFileWriter ncFileWriter,
      AsciiFile file);

  protected abstract NetcdfFileWriter writePlatformInfo(NetcdfFileWriter ncFileWriter,
      AsciiFile file);

  protected abstract void createCoordinateAttr(boolean hasRelTime);

  public List<NetcdfFileManager> asciiToDsg() {
    List<NetcdfFileManager> dsgWriters = new ArrayList<NetcdfFileManager>();

    dsgWriters.add(new SingleStationTimeSeries());
    dsgWriters.add(new SingleStationTrajectory());

    return dsgWriters;
  }

  public String createNetcdfFile(AsciiFile file, List<List<String>> parseFileData,
      List<String> header, String downloadDirPath) throws IOException {
    try {
      String ncFilePath =
          downloadDirPath + File.separator + FilenameUtils.removeExtension(file.getFileName())
              + ".nc";
      logger.warn("create ncFilePath: " + ncFilePath);

      // make sure downloadDir exists and, if not, create it
      checkDownloadDir(downloadDirPath);

      init(file, header);

      // look for coordinate and non-coordinate variables
      Boolean hasRelTime = findCoordAndNonCoordVars();

      // write the header of the netCDF file (returns NetcdfFileWriter with define mode False, i.e. ready
      // to write data values.
      NetcdfFileWriter ncFileWriter = createNetcdfFileHeader(file, ncFilePath, parseFileData,
          hasRelTime);

      //
      // no longer in define mode...now need to actually write out the data!
      //
      // Latitude
      if (getPlatformMetadataMap().containsKey("latitude")) {
        ncFileWriter = writeCoordVarsFromPlatform(ncFileWriter, "latitude");
      }

      // Longitude
      if (getPlatformMetadataMap().containsKey("longitude")) {
        ncFileWriter = writeCoordVarsFromPlatform(ncFileWriter, "longitude");
      }

      // Altitude
      if (getPlatformMetadataMap().containsKey("altitude")) {
        ncFileWriter = writeCoordVarsFromPlatform(ncFileWriter, "altitude");
      }

      // Platform ID
      if (getPlatformMetadataMap().containsKey("platformName")) {
        ncFileWriter = writePlatformInfo(ncFileWriter, file);
      }

      //ncFileWriter = writeRosettaInfo(ncFileWriter, file.getJsonStrSessionStorage());
      // must write user data before any new dateTime variables!
      ncFileWriter = writeUserVarData(parseFileData, ncFileWriter,
          file.getDecimalSeparatorLocale());
      if (getDateTimeBluePrint() != null) {
        if (!getDateTimeBluePrint().isEmpty()) {
          ncFileWriter = getDateTimeBluePrint().writeNewVariables(ncFileWriter);
        }
      }

      ncFileWriter.close();

      Boolean success = new File(ncFilePath).exists();
      if (success) {
        return ncFilePath;
      } else {
        logger.error("Error! the netcdf file " + ncFilePath + "was not created.");
        return null;
      }

    } catch (IllegalArgumentException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors);
      //  e.printStackTrace();
      throw e;
    } catch (Exception e) {
      //TODO: Using this broad catch is not very good practice
      // e.printStackTrace();
      // logger.error(e.getMessage());
      // logger.error(e.getStackTrace());
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors);
      return null;
    }
  }


}


