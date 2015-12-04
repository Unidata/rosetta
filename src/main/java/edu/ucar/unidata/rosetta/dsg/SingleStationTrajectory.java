package edu.ucar.unidata.rosetta.dsg;

import edu.ucar.unidata.rosetta.domain.AsciiFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import ucar.ma2.*;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Service for parsing file data.
 */
public class SingleStationTrajectory extends NetcdfFileManager {

    @Override
    protected NetcdfFileWriter writeCoordVarsFromPlatform(NetcdfFileWriter ncFileWriter, String name) throws IOException, InvalidRangeException {
        return null;
    }

    @Override
    protected NetcdfFileWriter createCoordVarsFromPlatform(NetcdfFileWriter ncFileWriter, String name) throws IOException, InvalidRangeException {
        return null;
    }

    @Override
    protected NetcdfFileWriter createPlatformInfo(NetcdfFileWriter ncFileWriter, AsciiFile file) {
        return null;
    }

    @Override
    protected NetcdfFileWriter writePlatformInfo(NetcdfFileWriter ncFileWriter, AsciiFile file) {
        return null;
    }

    private List<Variable> getAllVars(NetcdfFileWriter ncFileWriter, String sessionStorageKey, List<List<String>> outerList, String coordType) {
        String userName = getVariableNameMap().get(sessionStorageKey);
        String varName = userName.replace(" ", "_");
        List<String> uvn = getUsedVarNames();
        HashMap<String, Integer> nc = getNameCounts();
        List<String> avn = getAllVarNames();
        if (!getNameCounts().containsKey(varName)) {
            uvn.add(varName);
            setUsedVarNames(uvn);
            nc.put(varName, 1);
            setNameCounts(nc);
        } else {
            Integer newCount = nc.get(varName) + 1;
            nc.put(varName, newCount);
            varName = varName + "_" + newCount.toString();
        }
        avn.add(varName);

        HashMap variableMetadata = getVariableMetadataMap().get(sessionStorageKey + "Metadata");
        Object coordVarTypeOb = variableMetadata.get("_coordinateVariableType");
        String coordVarType = "";
        if (coordVarTypeOb != null) {
            coordVarType = coordVarTypeOb.toString();
        }

        String name = varName;
        //String shape = shapeAttr.replace(" ", ",");

        if (getCoordVars().containsKey(coordVarType)) {
            if (getCoordVars().get(coordVarType).contains(sessionStorageKey)) {
                name = name;
                // shape = coordVarType;
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
        setNameCounts(nc);
        setUsedVarNames(uvn);
        setAllVarNames(avn);
        return ncFileWriter;
    }


    @Override
    public String createNetcdfFile(AsciiFile file, List<List<String>> parseFileData, String downloadDirPath) throws IOException {
        try {
            String ncFilePath = downloadDirPath + "/" + FilenameUtils.removeExtension(file.getFileName()) + ".nc";
            logger.warn("create ncFilePath: " + ncFilePath);

            // make sure downloadDir exists and, if not, create it
            checkDownloadDir(downloadDirPath);

            init(file);

            // create a list of all variables
            List<Variable> vars = new ArrayList<Variable>();
            String key;
            for (String coordType : getCoordVars().keySet()) {
                Iterator<String> coordVarNameIterator = getCoordVars().get(coordType).iterator();
                while (coordVarNameIterator.hasNext()) {
                    key = coordVarNameIterator.next();
                    ncFileWriter = createNcfVariable(ncFileWriter, key, parsedDataFile);
                }
            }

            private List<Variable> getAllVars() {
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

                return ncFileWriter;
            }
            }
            protected NetcdfFileWriter createNcfVariable(NetcdfFileWriter ncFileWriter, String sessionStorageKey, List<List<String>> outerList, String coordType) {
                String userName = getVariableNameMap().get(sessionStorageKey);
                String varName = userName.replace(" ", "_");





            // look for coordinate and non-coordinate variables
            Boolean hasRelTime = findCoordAndNonCoordVars();

            // write the header of the netCDF file (returns NetcdfFileWriter with define mode False, i.e. ready
            // to write data values.
            NetcdfFileWriter ncFileWriter = createNetcdfFileHeader(file, ncFilePath, parseFileData, hasRelTime);

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

            ncFileWriter = writeRosettaInfo(ncFileWriter, file.getJsonStrSessionStorage());
            // must write user data before any new dateTime variables!
            ncFileWriter = writeUserVarData(parseFileData, ncFileWriter);
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
                logger.error("Error!  the netcdf file " + ncFilePath + "was not created.");
                return null;
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(e.getStackTrace());
            return null;
        }
    }





    protected static Logger logger = Logger.getLogger(SingleStationTrajectory.class);


    public SingleStationTrajectory() {
        super.setMyCfRole("trajectory_id");
        super.setMyDsgType("trajectory");
    }

}