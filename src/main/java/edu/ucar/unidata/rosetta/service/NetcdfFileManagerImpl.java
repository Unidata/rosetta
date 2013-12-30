package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.DateTimeBluePrint;
import edu.ucar.unidata.rosetta.domain.AsciiFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ucar.ma2.ArrayScalar;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Service for parsing file data.
 */
public class NetcdfFileManagerImpl implements NetcdfFileManager {


    protected static Logger logger = Logger.getLogger(NetcdfFileManagerImpl.class);

    private Map<String, HashMap> variableMetadataMap;
    private Map<String,String> variableNameMap;
    private Map<String, String> platformMetadataMap;
    private Map<String, String> generalMetadataMap;


    private HashMap<String, ArrayList<String>> coordVars;
    private HashMap<String, Integer> nameCounts;

    private List<String> nonCoordVarList;
    private List<String> coordVarList;
    private List<String> usedVarNames = new ArrayList<String>();

    private ArrayList buildTimeTriggers;

    private String shapeAttr;
    private String coordAttr;

    private String relTimeVarName;
    private String ncFilePath;
    private String cfType;
    private DateTimeBluePrint dateTimeBluePrint;

    private void init(AsciiFile file) {
        // get and set the various metadata maps
        variableNameMap = file.getVariableNameMap();
        variableMetadataMap = file.getVariableMetadataMap();
        platformMetadataMap = file.getPlatformMetadataMap();
        generalMetadataMap = file.getGeneralMetadataMap();

        // create a list of the various types of time variables a user
        // can supply. These need to be carefully handled in the code!
        // these can be found in the addContentToDialog() function in
        // src/main/webapp/resources/js/SlickGrid/custom/variableSpecification.js
        // Note that "relTime" isn't included, as a relative time (e.g. days since 1970)
        // can be handled as any other variable (i.e. we do not need to build
        // anything special in Rosetta.java to handle this).
        //
        String[] specialTimeNames = {"fullDateTime", "dateOnly", "timeOnly"};
        buildTimeTriggers = new ArrayList();
        buildTimeTriggers.addAll(Arrays.asList(specialTimeNames));
        cfType = file.getCfType();
        nameCounts = new HashMap<String, Integer>();
    }

    private HashMap<String, ArrayList<String>> extractTimeRelatedVars(NetcdfFileWriter ncFileWriter, List<String> ncFileVariableNames) {
        HashMap<String, ArrayList<String>> timeRelatedVars = new HashMap<String, ArrayList<String>>();
        String timeType;
        for (String varName : ncFileVariableNames) {
            Variable theVar = ncFileWriter.findVariable(varName);
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

        return timeRelatedVars;
    }


    private NetcdfFileWriter createNcfVariable(NetcdfFileWriter ncFileWriter, String sessionStorageKey) {
        ncFileWriter = createNcfVariable(ncFileWriter, sessionStorageKey, null);
        return ncFileWriter;
    }

    private NetcdfFileWriter createNcfVariable(NetcdfFileWriter ncFileWriter, String sessionStorageKey, String coordType) {
        String userName = variableNameMap.get(sessionStorageKey);
        String varName = userName.replace(" ", "_");

        if (!nameCounts.containsKey(varName)) {
            usedVarNames.add(varName);
            nameCounts.put(varName, 1);
        } else {
            Integer newCount = nameCounts.get(varName) + 1;
            nameCounts.put(varName, newCount);
            varName = varName + "_" + newCount.toString();
        }

        HashMap variableMetadata = variableMetadataMap.get(sessionStorageKey + "Metadata");
        Object coordVarTypeOb = variableMetadata.get("_coordinateVariableType");
        String coordVarType = "";
        if (coordVarTypeOb != null) {
            coordVarType = coordVarTypeOb.toString();
        }

        String name = varName;
        String shape = shapeAttr.replace(" ", ",");

        if (coordVars.get(coordVarType).contains(sessionStorageKey)) {
            name = coordVarType;
            shape = coordVarType;
        }


        String type = (String) variableMetadata.get("dataType");
        DataType ncType = null;
        if (type.equals("Text")) {
            ncType = DataType.STRING;
        } else if (type.equals("Integer")) {
            ncType = DataType.INT;
        } else if (type.equals("Float")) {
            ncType = DataType.FLOAT;
        }


        Variable theVar = ncFileWriter.addVariable(null, name, ncType, shape);

        if (!userName.equals(varName)) {
            ncFileWriter.addVariableAttribute(theVar, new Attribute("_userSuppliedName", userName));
        }
        Set<String> variableMetadataKeys = variableMetadata.keySet();
        Iterator<String> variableMetadataKeysIterator = variableMetadataKeys.iterator();
        String metadataKey;
        String metadataValue;
        while (variableMetadataKeysIterator.hasNext()) {
            metadataKey = variableMetadataKeysIterator.next();
            metadataValue = variableMetadata.get(metadataKey).toString();
            if (!metadataKey.equals("dataType")) {
                ncFileWriter.addVariableAttribute(theVar, new Attribute(metadataKey, metadataValue));
            }
        }

        String columnId = sessionStorageKey.replace("variableName", "");
        ncFileWriter.addVariableAttribute(theVar, new Attribute("_columnId", columnId));

        if (nonCoordVarList.contains(sessionStorageKey)) {
            ncFileWriter.addVariableAttribute(theVar, new Attribute("coordinates", coordAttr));
        }

        if (coordType != null) {
            if (buildTimeTriggers.contains(coordType)) {
                ncFileWriter.addVariableAttribute(theVar, new Attribute("timeRelatedVariable", "true"));
            }
        }

        return ncFileWriter;
    }

    // name: "latitude", "longitude", "altitude"
    private NetcdfFileWriter createCoordVarsFromPlatform(NetcdfFileWriter ncFileWriter, String name) throws IOException, InvalidRangeException {

        String coordVarUnits = platformMetadataMap.get(name + "Units");
        Boolean unitChanged = false;
        if (coordVarUnits.equals("degrees_west")) {
            coordVarUnits = "degrees_east";
            unitChanged=true;
        } else if (coordVarUnits.equals("degrees_south")) {
            coordVarUnits = "degrees_north";
            unitChanged=true;
        }
        String coordVarVal = platformMetadataMap.get(name);

        if (unitChanged) {
            coordVarVal = "-" + coordVarVal;
        }

        Variable theVar = ncFileWriter.addVariable(null, name.substring(0,3), DataType.FLOAT, "");

        ncFileWriter.addVariableAttribute(theVar, new Attribute("Units", coordVarUnits));
        ncFileWriter.addVariableAttribute(theVar, new Attribute("standard_name", name));
        ncFileWriter.addVariableAttribute(theVar, new Attribute("_platformMetadata", "true"));

        if (!name.equals("altitude")) {
            ncFileWriter.addVariableAttribute(theVar, new Attribute("long_name", name));
        } else {
            ncFileWriter.addVariableAttribute(theVar, new Attribute("long_name", "height above mean seal-level"));
            ncFileWriter.addVariableAttribute(theVar, new Attribute("positive", "up"));
            ncFileWriter.addVariableAttribute(theVar, new Attribute("axis", "Z"));
        }

        return ncFileWriter;
    }

    private NetcdfFileWriter writeCoordVarsFromPlatform(NetcdfFileWriter ncFileWriter, String name) throws IOException, InvalidRangeException {

        String coordVarUnits = platformMetadataMap.get(name + "Units");
        Boolean unitChanged = false;
        if (coordVarUnits.equals("degrees_west")) {
            unitChanged=true;
        } else if (coordVarUnits.equals("degrees_south")) {
            unitChanged=true;
        }
        String coordVarVal = platformMetadataMap.get(name);

        if (unitChanged) {
            coordVarVal = "-" + coordVarVal;
        }

        Variable theVar = ncFileWriter.findVariable(name.substring(0,3));

        ncFileWriter.write(theVar, new ArrayScalar(Float.parseFloat(coordVarVal)));

        return ncFileWriter;
    }


    private NetcdfFileWriter createPlatformInfo(NetcdfFileWriter ncFileWriter, AsciiFile file){
        String stationName = platformMetadataMap.get("platformName").toString().replaceAll(" ", "_");

        Variable theVar = ncFileWriter.addStringVariable(null,"station_id", new ArrayList<Dimension>(),
                stationName.length());

        theVar.addAttribute(new Attribute("cf_role", file.getCfType()));
        theVar.addAttribute(new Attribute("long_name", "station_id"));
        theVar.addAttribute(new Attribute("standard_name", "station_id"));
        theVar.addAttribute(new Attribute("_platformMetadata", "true"));

        return ncFileWriter;
    }

    private NetcdfFileWriter writePlatformInfo(NetcdfFileWriter ncFileWriter, AsciiFile file){
        String stationName = platformMetadataMap.get("platformName").toString().replaceAll(" ", "_");
        Variable theVar = ncFileWriter.findVariable("station_id");

        ucar.ma2.ArrayString sa = new ucar.ma2.ArrayString.D0();
        sa.set(null, stationName);
        try {
            ncFileWriter.writeStringData(theVar, sa);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }

        return ncFileWriter;
    }


    private NetcdfFileWriter createDimensions(NetcdfFileWriter ncFileWriter, List<List<String>> parseFileData) {
        Iterator<String> coordVarNameIterator;

        // we only want one time related dimension. So, if relTime is supplied, then
        // we only want to use it; if time spans multiple cols, then we will create
        // a "time" dimension, and assemble the "time" coordinate variable in Rosetta.java
        shapeAttr = "";
        for (String coordType : coordVars.keySet()) {
            // don't create a dimension for the partial time variables that will need to
            // be assembled in Rosetta.java.
            if (!buildTimeTriggers.contains(coordType)) {
                // coordAttr is the attribute that defines the coord system for variables
                if (shapeAttr.equals("")) {
                    shapeAttr = coordType;
                } else {
                    shapeAttr = shapeAttr + " " + coordType;
                }
                coordVarNameIterator = coordVars.get(coordType).iterator();
                while (coordVarNameIterator.hasNext()) {
                    // set dimension name based on coordType
                    ncFileWriter.addDimension(null, coordType, parseFileData.size());
                }
            }
        }

        return ncFileWriter;
    }

    private NetcdfFileWriter createTimeDimension(NetcdfFileWriter ncFileWriter, int parseFileDataSize) {

        ncFileWriter.addDimension(null, "dateTime", parseFileDataSize);
        if (shapeAttr.equals("")) {
            shapeAttr = "dateTime";
        } else {
            shapeAttr = shapeAttr + " dateTime";
        }

        return ncFileWriter;
    }

    private void createCoordinateAttr(boolean hasRelTime) {
        if (cfType.equals("timeSeries")) {
            // if we have a relTime (a complete time variable), use that variable name
            // otherwise, use a (yet-to-be-created) variable called "time"
            if (hasRelTime) {
                coordAttr = relTimeVarName + " lat lon alt";
            } else {
                coordAttr = "time lat lon alt";
            }
        }
    }

    private Boolean findCoordAndNonCoordVars() {
        Set<String> variableNameKeys = variableNameMap.keySet();
        Iterator<String> variableNameKeysIterator = variableNameKeys.iterator();
        String key, value;
        Map<String, String> variableMetadata;
        // check to see if user supplied a relTime (i.e. days since yyyy-mm-dd) If so,
        // then we do not need to construct a time variable. If not, then we need to create
        // a time dimension which will then be assembled in Rosetta.java.
        Boolean hasRelTime = false;
        nonCoordVarList = new ArrayList<String>();
        coordVarList = new ArrayList<String>();
        coordVars = new HashMap<String, ArrayList<String>>();
        while (variableNameKeysIterator.hasNext()) {
            //ex:
            // key = "variableName1"
            // value = "time"
            key = variableNameKeysIterator.next();
            value = variableNameMap.get(key);
            if (!value.equals("Do Not Use")) {
                variableMetadata = variableMetadataMap.get(key + "Metadata");
                // check if variable is a coordinate variable!
                if (variableMetadata.containsKey("_coordinateVariable")) {
                    String coordVarType = variableMetadata.get("_coordinateVariableType");
                    if(variableMetadata.get("_coordinateVariable").equals("coordinate") && (!buildTimeTriggers.contains(coordVarType))){
                        if (coordVarType == "relTime") {
                            hasRelTime = true;
                            relTimeVarName = key;
                        }
                        coordVarList = coordVars.get(coordVarType);
                        if (coordVarList == null) {
                            coordVarList = new ArrayList<String>();
                        }
                        coordVarList.add(key);
                        coordVars.put(coordVarType, (ArrayList<String>) coordVarList);
                    } else {
                        nonCoordVarList.add(key);
                    }
                } else {
                    nonCoordVarList.add(key);
                }
            }
        }

        return hasRelTime;
    }

    private NetcdfFileWriter findAndCreateCoordVars(NetcdfFileWriter ncFileWriter) {
        String key;
        for (String coordType : coordVars.keySet()) {
            Iterator<String> coordVarNameIterator = coordVars.get(coordType).iterator();
            while (coordVarNameIterator.hasNext()) {
                key = coordVarNameIterator.next();
                ncFileWriter = createNcfVariable(ncFileWriter, key);
            }
        }
        return ncFileWriter;
    }

    private NetcdfFileWriter findAndCreateNonCoordVars(NetcdfFileWriter ncFileWriter) {
        Iterator<String> nonCoordVarNameIterator = nonCoordVarList.iterator();
        String key;
        while (nonCoordVarNameIterator.hasNext()) {
            key = nonCoordVarNameIterator.next();
            ncFileWriter = createNcfVariable(ncFileWriter, key);
        }

        return ncFileWriter;
    }

    private void checkDownloadDir(String downloadDirPath) throws IOException {
        File downloadTarget = new File(downloadDirPath);
        if (!downloadTarget.exists()) {
            logger.warn("created download path");
            if (!downloadTarget.mkdirs()) {
                throw new IOException("Unable to create download directory " + downloadTarget.getAbsolutePath());
            }
        }
    }

    private NetcdfFileWriter createRosettaInfo(NetcdfFileWriter ncFileWriter, String rosettaJson) {

        String rosetta_version = "0.1";

        String name = "Rosetta";
        String version = rosetta_version;

        Variable theVar = ncFileWriter.addStringVariable(null,"Rosetta", new ArrayList<Dimension>(),
                rosettaJson.length());

        theVar.addAttribute(new Attribute("long_name", "Rosetta front-end sessionStorage JSON String"));
        theVar.addAttribute(new Attribute("version", rosetta_version));

        return ncFileWriter;
    }


    private NetcdfFileWriter writeRosettaInfo(NetcdfFileWriter ncFileWriter, String rosettaJson) {

        String rosetta_version = "0.1";

        String name = "Rosetta";
        String version = rosetta_version;

        Variable theVar = ncFileWriter.findVariable("Rosetta");

        ucar.ma2.ArrayString sa = new ucar.ma2.ArrayString.D0();
        sa.set(null, rosettaJson);
        try {
            ncFileWriter.writeStringData(theVar, sa);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }

        return ncFileWriter;
    }


    private NetcdfFileWriter addCfAttributes(NetcdfFileWriter ncFileWriter){
        ncFileWriter.addGroupAttribute(null, new Attribute("Conventions", "CF-1.6"));
        ncFileWriter.addGroupAttribute(null, new Attribute("featureType", cfType));

        return ncFileWriter;
    }

    private NetcdfFileWriter createNetcdfFileHeader(AsciiFile file, String ncFilePath, List<List<String>> parseFileData, Boolean hasRelTime) throws IOException, InvalidRangeException {
        // create ncFileWriter
        NetcdfFileWriter ncFileWriter = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, ncFilePath);

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

        for (Map.Entry<String, String> entry : generalMetadataMap.entrySet()) {
            if (entry.getValue() != null) {
                ncFileWriter.addGroupAttribute(null, new Attribute(entry.getKey(), entry.getValue()));
            }
        }

        // platform metadata

        // Latitude
        if (platformMetadataMap.containsKey("latitude")) {
            ncFileWriter = createCoordVarsFromPlatform(ncFileWriter, "latitude");
        }

        // Longitude
        if (platformMetadataMap.containsKey("longitude")) {
            ncFileWriter = createCoordVarsFromPlatform(ncFileWriter, "longitude");
        }

        // Altitude
        if (platformMetadataMap.containsKey("altitude")) {
            ncFileWriter = createCoordVarsFromPlatform(ncFileWriter, "altitude");
        }

        // Platform ID
        if (platformMetadataMap.containsKey("platformName")) {
            ncFileWriter = createPlatformInfo(ncFileWriter, file);
        }

        // look at coordVars and create the appropriate coordinate variables
        ncFileWriter = findAndCreateCoordVars(ncFileWriter);

        // create variables for the non-coordinate variables
        ncFileWriter = findAndCreateNonCoordVars(ncFileWriter);

        // Add rosetta specific info
        ncFileWriter = createRosettaInfo(ncFileWriter, file.getJsonStrSessionStorage());


        // check to se if we need to construct new dateTime variables
        HashMap<String, ArrayList<String>> timeRelatedVars = extractTimeRelatedVars(ncFileWriter, usedVarNames);

        dateTimeBluePrint = new DateTimeBluePrint(timeRelatedVars, ncFileWriter);

        ncFileWriter = dateTimeBluePrint.createNewVariables(ncFileWriter);

        ncFileWriter.create();

        return ncFileWriter;
    }

    public String createNetcdfFile(AsciiFile file, List<List<String>> parseFileData, String downloadDirPath) throws IOException {
        try {

            ncFilePath = downloadDirPath + "/" + FilenameUtils.removeExtension(file.getFileName()) + ".nc";
            logger.warn("create ncFilePath: " + ncFilePath);

            // make sure downloadDir exists and, if not, create it
            checkDownloadDir(downloadDirPath);

            init(file);

            // look for coordinate and non-coordinate variables
            Boolean hasRelTime = findCoordAndNonCoordVars();

            // write the header of the netCDF file (returns NetcdfFileWriter with define mode False, i.e. ready
            // to write data values.
            NetcdfFileWriter ncFileWriter = createNetcdfFileHeader(file, ncFilePath, parseFileData, hasRelTime);

            //
            // no longer in define mode...now need to actually write out the data!
            //
            // Latitude
            if (platformMetadataMap.containsKey("latitude")) {
                ncFileWriter = writeCoordVarsFromPlatform(ncFileWriter, "latitude");
            }

            // Longitude
            if (platformMetadataMap.containsKey("longitude")) {
                ncFileWriter = writeCoordVarsFromPlatform(ncFileWriter, "longitude");
            }

            // Altitude
            if (platformMetadataMap.containsKey("altitude")) {
                ncFileWriter = writeCoordVarsFromPlatform(ncFileWriter, "altitude");
            }

            // Platform ID
            if (platformMetadataMap.containsKey("platformName")) {
                ncFileWriter = writePlatformInfo(ncFileWriter, file);
            }

            ncFileWriter = writeRosettaInfo(ncFileWriter, file.getJsonStrSessionStorage());

            ncFileWriter = dateTimeBluePrint.writeNewVariables(ncFileWriter);

            Boolean success = true;
            if (success) {
                return ncFilePath;
            } else {
                logger.error("Error!  ncml file " + ncFilePath + "was not created.");
                return null;
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}