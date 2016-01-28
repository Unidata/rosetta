package edu.ucar.unidata.rosetta.dsg;

import edu.ucar.unidata.rosetta.domain.AsciiFile;
import edu.ucar.unidata.rosetta.dsg.util.DateTimeBluePrint;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayScalar;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.units.RaiseException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Service for parsing file data.
 */
public class SingleStationTrajectory2  {
    private String myCfRole;
    private String myDsgType;
    private Map<String, HashMap> variableMetadataMap;
    private Map<String,String> variableNameMap;
    private List<String> nonCoordVarList;
    private List<String> coordVarList;
    private Map<String, List<String>> coordVarsMap;
    private List<String> buildTimeTriggers;
    private String relTimeVarName;
    private List<Variable> coordVars;
    private List<Variable> nonCoordVars;
    private Map<String, Integer> nameCounts;
    boolean hasRelTime;

    protected static Logger logger = Logger.getLogger(SingleStationTrajectory2.class);


    private void setMyCfRole(String myCfRole) {
        this.myCfRole = myCfRole;
    }

    private String getMyCfRole() {
        return this.myCfRole;
    }

    private void setMyDsgType(String myDsgType) {
        this.myDsgType = myDsgType;
    }

    private String getMyDsgType() {
        return this.myDsgType;
    }

    private void findCoordAndNonCoordVars() {

        Set<String> variableNameKeys = variableNameMap.keySet();
        Iterator<String> variableNameKeysIterator = variableNameKeys.iterator();
        String key, value;
        Map<String, String> variableMetadata;
        // check to see if user supplied a relTime (i.e. days since yyyy-mm-dd) If so,
        // then we do not need to construct a time variable. If not, then we need to create
        // a time dimension.
        nonCoordVarList = new ArrayList<String>();
        coordVarList = new ArrayList<String>();
        coordVarsMap = new HashMap<String, List<String>>();
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
                        if (coordVarType.toLowerCase().equals("reltime")) {
                            hasRelTime = true;
                            relTimeVarName = value;
                        }
                        coordVarList = coordVarsMap.get(coordVarType);
                        if (coordVarList == null) {
                            coordVarList = new ArrayList<String>();
                        }
                        coordVarList.add(key);
                        coordVarsMap.put(coordVarType, coordVarList);
                    } else {
                        nonCoordVarList.add(key);
                    }
                } else {
                    nonCoordVarList.add(key);
                }
            }
        }
    }

    private void setCoordVars() throws Exception {
        coordVars = new ArrayList<Variable>();
        // check to see if we have time defined as a relative time - if we dont, we
        //   need to create one to be CF compliant.
        /**
        if (!hasRelTime) {
            throw new Exception("non-relative time not implemented");
            //DateTimeBluePrint dateTimeBluePrint = new DateTimeBluePrint(timeRelatedVars, ncFileWriter);
            //if (!dateTimeBluePrint.isEmpty()) {
            //    dateTimeBluePrint.createNewVars();
            //}
        }

        for (String sessionStorageKeyName : coordVarList) {
            //findAndCreateCoordVars
            for (String coordType : getCoordVars().keySet()) {
                Iterator<String> coordVarNameIterator = getCoordVars().get(coordType).iterator();
                while (coordVarNameIterator.hasNext()) {
                    key = coordVarNameIterator.next();
                    ncFileWriter = createNcfVariable(ncFileWriter, key, parsedDataFile);

            //createNcfVariable(NetcdfFileWriter ncFileWriter, String sessionStorageKey, List<List<String>> outerList)
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
            HashMap variableMetadata = getVariableMetadataMap().get(sessionStorageKey + "Metadata");
            Object coordVarTypeOb = variableMetadata.get("_coordinateVariableType");
            String coordVarType = "";
            if (coordVarTypeOb != null) {
                coordVarType = coordVarTypeOb.toString();
            }

            String name = varName;
            String shape = shapeAttr.replace(" ", ",");

            if (getCoordVars().containsKey(coordVarType)) {
                if (getCoordVars().get(coordVarType).contains(sessionStorageKey)) {
                    name = name;
                    shape = coordVarType;
                }
            }


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
            }   else {
                theVar = ncFileWriter.addVariable(null, name, ncType, shape);
            }

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
        }
        */

    }

    private void setNonCoordVars() {
        nonCoordVars = new ArrayList<Variable>();
    }

    public SingleStationTrajectory2() {
        setMyCfRole("trajectory_id");
        setMyDsgType("trajectory");

    }


    private void init(AsciiFile file) {
        String[] specialTimeNames = {"fullDateTime", "dateOnly", "timeOnly"};
        buildTimeTriggers.addAll(Arrays.asList(specialTimeNames));
        nameCounts = new HashMap<String, Integer>();
        variableNameMap = file.getVariableNameMap();
        variableMetadataMap = file.getVariableMetadataMap();
        //platformMetadataMap(file.getPlatformMetadataMap());
        //generalMetadataMap(file.getGeneralMetadataMap());

        findCoordAndNonCoordVars();

        try {
            setCoordVars();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setNonCoordVars();

    }

    public String createNetcdfFile(AsciiFile file, List<List<String>> parseFileData, String downloadDirPath) throws IOException {
        String ncFilePath = "";
        try {
            ncFilePath = downloadDirPath + "/" + FilenameUtils.removeExtension(file.getFileName()) + ".nc";
            logger.warn("create ncFilePath: " + ncFilePath);

            // make sure downloadDir exists and, if not, create it
            //checkDownloadDir(downloadDirPath);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(e.getStackTrace());
        }

        return ncFilePath;
    }

}