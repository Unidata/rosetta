/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.known.etuff;

import edu.ucar.unidata.rosetta.service.wizard.MetadataManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import edu.ucar.unidata.rosetta.domain.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.util.PathUtils;
import edu.ucar.unidata.rosetta.util.RosettaGlobalAttributeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.FileWriter2;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.jni.netcdf.Nc4Iosp;
import ucar.nc2.time.CalendarDate;

import javax.annotation.Resource;

import static edu.ucar.unidata.rosetta.converters.utils.VariableAttributeUtils.getMaxMinAttrs;

/**
 * Tag Base Archival Tags (flat file) following the OIIP Tag Universal File Format (TUFF)
 *
 * This class controls the conversion of Archival Tags as exported by TagBase.
 */

public class TagUniversalFileFormat {

    private static Logger log = Logger.getLogger(TagUniversalFileFormat.class.getName());

    @Resource(name = "metadataManager")
    private MetadataManager metadataManager;

    private static String etagGroup = "Meta_eTag";

    private List<RosettaGlobalAttribute> rosettaGlobalAttributes = new ArrayList<>();

    private HashMap<String, TreeMap<Long, Ob>> data = new HashMap<>();
    private HashMap<String, TreeMap<Integer, HistBin>> binInfoMin = new HashMap<>();
    private HashMap<String, TreeMap<Integer, HistBin>> binInfoMax = new HashMap<>();

    // match one loc to many obs by default
    private boolean matchupOneLocOneOb = false;

    private static String timeDimName = "time";
    private static String latDimName = "latitude";
    private static String lonDimName = "longitude";
    private static String depthDimName = "depth";
    private static String trajectoryIdName = "trajectory";
    private static String freshnessVarName = "location_freshness";

    private String maxTimeVar;
    private String trajCoordVarNames = String
            .join(" ", timeDimName, latDimName, lonDimName, depthDimName, trajectoryIdName);

    private Map<String, MetadataProfile> etuffMap;

    private NetcdfFileWriter ncfw;
    private Group rootGroup;

    private boolean useNetcdf4;

    public TagUniversalFileFormat() {

        useNetcdf4 = Nc4Iosp.isClibraryPresent();


        List<MetadataProfile> etuffProfile = metadataManager.getETUFFProfile();
        etuffMap = etuffProfile.stream()
                .collect(Collectors.toMap(
                        MetadataProfile::getAttributeName,
                        Function.identity(),
                        (metadataItem1, metadataItem2) -> {
                            System.out.println("duplicate metadata item found with attribute name " + metadataItem1.getAttributeName());
                            return metadataItem1;
                        }));
    }

    /**
     * set how matchup should be done
     *
     * if true, matchup will be one ob per location
     *
     * if false, matchup will be many obs per location
     */
    public void setMatchupOneLocOneOb(boolean matchupOneLocOneOb) {
        this.matchupOneLocOneOb = matchupOneLocOneOb;
    }

    public void setUseNetcdf4(boolean useNetcdf4) {
        if ((useNetcdf4) && (Nc4Iosp.isClibraryPresent())) {
            this.useNetcdf4 = useNetcdf4;
        } else if (useNetcdf4){
            log.error("Cannot enable netCDF-4 writing - c library is not loaded.");
        } else {
            this.useNetcdf4 = false;
        }
    }

    private int getMaxNumRecords() {
        int maxRecords = -1;
        maxTimeVar = "";
        for (Object name : data.keySet()) {
            String strName = name.toString();
            int numRecords = data.get(strName).size();
            if (numRecords > maxRecords) {
                maxRecords = numRecords;
                maxTimeVar = name.toString();
            }
        }
        return maxRecords;
    }

    private String cleanVarName(String name) {
        name = name.replace("\"", "");
        name = name.replace(" ", "_");
        return name;
    }

    private String cleanUnitString(String unit) {
        unit = unit.replace("\"", "");

        if (unit.equals("units")) {
            unit = "";
        }

        return unit;
    }

    private void updateBinInfo(HashMap<String, TreeMap<Integer, HistBin>> binInfo, String name,
                               int binNumber, HistBin histBin) {
        TreeMap<Integer, HistBin> tmpBinList;

        name = cleanVarName(name);

        if (binInfo.containsKey(name)) {
            tmpBinList = binInfo.get(name);
        } else {
            tmpBinList = new TreeMap<>();
        }

        tmpBinList.put(binNumber, histBin);
        binInfo.put(name, tmpBinList);
    }

    private void addData(String name, long time, Ob newOb) {
        // time is seconds since 1970-01-01 from java.util.Date.getTime()
        TreeMap<Long, Ob> tmpObList;

        name = cleanVarName(name);

        if (data.containsKey(name)) {
            tmpObList = data.get(name);
        } else {
            tmpObList = new TreeMap<>();
        }

        tmpObList.put(time, newOb);
        data.put(name, tmpObList);
    }

    private void processHeaderLine(String line) {
        String[] keyVals;
        if (!line.startsWith("//") && (!line.startsWith("\""))) {
            // hack until header output is all of the form key = value
            line = line.substring(1);
            line = line.replace("\"", "");
            int locOfEq = line.indexOf("=");
            int locOfColon = line.indexOf(":");
            if (locOfColon > 0) {
                // possible that key/value delimited by colon and not equal sign
                if (locOfEq > 0) {
                    // equal sign happens before colon - most likely delimted by equal sign
                    if (locOfEq < locOfColon) {
                        keyVals = line.split("=", 2);
                    } else {
                        keyVals = line.split(":", 2);
                    }
                } else {
                    keyVals = line.split(":", 2);
                }
            } else {
                // normal situation, where attrs of the form :key = value
                keyVals = line.split("=", 2);
            }
            if (keyVals.length == 2) {
                String name = keyVals[0].trim();
                String value = keyVals[1].trim();
                RosettaGlobalAttribute rosettaGlobalAttribute = makeRosettaGlobalAttribute(name, value);
                rosettaGlobalAttributes.add(rosettaGlobalAttribute);
            } else {
                // skip if the line is the column definition line, but without quotes
                if (!line.contains("VariableID")) {
                    // missed something
                    log.error("Global Attr line " + line + " not parsed correctly");
                }
            }
        }
    }

    private RosettaGlobalAttribute makeRosettaGlobalAttribute(String name, String value) {
        RosettaGlobalAttribute rosettaGlobalAttribute = new RosettaGlobalAttribute();
        rosettaGlobalAttribute.setName(name);
        rosettaGlobalAttribute.setValue(value);

        // by default, set group to root and type to string
        rosettaGlobalAttribute.setGroup("root");
        rosettaGlobalAttribute.setType("STRING");

        // Map<String, MetadataProfile> etuffMap;
        // if, based on attribute name, this piece of metadata belongs to the eTuff metadata profile,
        // update the group and type based on the eTuff metadata profile.
        if (etuffMap.containsKey(name)) {
            // attribute belongs to eTuff
            MetadataProfile metadataItem = etuffMap.get(name);

            // update type based on info in metadata profile
            String type = metadataItem.getMetadataValueType();
            rosettaGlobalAttribute.setType(type);

            // update group based on info in metadata profile
            String subgroup = metadataItem.getMetadataGroup();
            String fullGroupPath = etagGroup + "/" + subgroup;
            rosettaGlobalAttribute.setGroup(fullGroupPath);
        }

        return rosettaGlobalAttribute;
    }

    private void processBinLine(String line) {
        String[] dataEntry = line.split(",");
        int binNumber;
        String binName;
        // bin definition - no datetime associated with it
        float value = Float.parseFloat(dataEntry[2]);
        String unit = cleanUnitString(dataEntry[4]);
        String fullBinName = dataEntry[3];
        HistBin histBin = new HistBin(value, unit);

        String[] splitFullBinName = fullBinName.split("BinMin|BinMax");
        if (splitFullBinName.length == 2) {
            binNumber = Integer.parseInt(splitFullBinName[1]);
            binName = splitFullBinName[0].replace("Hist", "");
            if (fullBinName.contains("BinMin")) {
                updateBinInfo(binInfoMin, binName, binNumber, histBin);
            } else {
                updateBinInfo(binInfoMax, binName, binNumber, histBin);
            }
        } else {
            log.error("No idea how to get bin number from: " + line);
        }
    }

    private void processDataLine(String line) {
        String[] dataEntry = line.split(",");
        line = line.replaceAll("\"", "");
        if (line.startsWith(",")) {
            // starting with , means there is no date/time, so not an observation
            processBinLine(line);
        } else {
            // this could be an actual data line
            if ((!dataEntry[0].toLowerCase().contains("datetime")) &&
                    (dataEntry.length == 5 |
                    (dataEntry.length == 4 && line.endsWith(",")))) {
                // full data line, but could be missing units
                String datetime = dataEntry[0].replaceAll("\"", "");
                String value = dataEntry[2].replaceAll("\"", "");
                String name = cleanVarName(dataEntry[3]).replaceAll("\"", "");
                String unit = "";
                if (!line.endsWith(",")) {
                    unit = cleanUnitString(dataEntry[4].replaceAll("\"", ""));
                }
                Ob thisOb = new Ob(value, unit);
                CalendarDate calendarDate = CalendarDate.parseISOformat("gregorian", datetime);
                long msecSinceEpoch = calendarDate.toDate().getTime();
                long secSinceEpoch = msecSinceEpoch / 1000;
                addData(name, secSinceEpoch, thisOb);
            } else {
                // cannot identify data line
                log.error("No idea what this is: " + line);
            }
        }
    }

    public void parse(String etuffFile) {
        log.debug("start conversion!");
        String line;

        Path tbFilePath = Paths.get(etuffFile);
        BufferedReader br;
        try {
            if (etuffFile.endsWith(".gz")) {
                InputStream fileStream = new FileInputStream(tbFilePath.toString());
                InputStream gzipStream = new GZIPInputStream(fileStream);
                Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.ISO_8859_1);
                br = new BufferedReader(decoder);
            } else {
                br = Files.newBufferedReader(tbFilePath, StandardCharsets.ISO_8859_1);
            }

            log.debug("opened the etuff file!");
            line = br.readLine();
            line = line.trim();
            log.debug("reading etuff file!");
            while (br.ready()) {
                // Read the header (delimited by the : character or //, but ignore // as these are
                // just comments)
                String first = line.substring(0, 1);
                if (first.equals(":") | first.equals("/") | first.equals("D")) {
                    processHeaderLine(line);
                } else {
                    // data line
                    processDataLine(line);
                }

                // other stuff?

                // read next line
                line = br.readLine().trim();
            }
            // closes BufferedReader as well as InputStream(s)
            br.close();
        } catch (IOException ioe) {
            log.error("Error reading eTuff file", ioe);
        }

        log.debug("done!");
        printInventory();
    }

    private void printInventory() {
        log.debug("Inventory:");
        List<String> sortedKeys = new ArrayList<>(data.keySet());
        Collections.sort(sortedKeys);

        for (Object name : sortedKeys) {
            String strName = name.toString();
            int len = data.get(strName).size();
            log.debug("   " + name + " (" + len + ")");
        }
    }

    public HashMap<String, String> getGlobalMetadata() {
        // should probably encode in json to return to browser
        HashMap<String, String> globalMetadata = new HashMap<>();
        for (RosettaGlobalAttribute rga : rosettaGlobalAttributes) {
            globalMetadata.put(rga.getName(), rga.getValue());
        }
        return globalMetadata;
    }

    private String getTrajId() {
        String trajectoryId = "1"; // default as trajectory 1; otherwise, look for serial number
        // add global attribute
        for (RosettaGlobalAttribute rga : rosettaGlobalAttributes) {
            if (rga.getName().equals("serial_number")) {
                trajectoryId = rga.getValue();
            }
        }
        return trajectoryId;
    }

    private void makeMetadataGroups() {

        Map<String, Group> groupMap = new HashMap<>();
        groupMap.put("root", rootGroup);

        for (RosettaGlobalAttribute rga : rosettaGlobalAttributes) {
            String fullGroupPath = rga.getGroup();
            // make sure groups exists in the netcdf file writer
            String[] groups = fullGroupPath.split("/");
            if (groups.length >= 1) {
                String groupName = groups[0];
                if (!groupMap.containsKey(groupName)) {
                    groupMap.put(groupName, ncfw.addGroup(rootGroup, groupName));
                }
                if (groups.length == 2) {
                    String subgroupName = groups[1];
                    String groupPath = groupName + "/" + subgroupName;
                    if (!groupMap.containsKey(groupPath)) {
                        Group parent = groupMap.get(groupName);
                        groupMap.put(groupPath, ncfw.addGroup(parent, subgroupName));
                    }
                }
            }

            // now, add attribute to the group
            // first, create a "typed" netcdf-java attribute
            Attribute attr = RosettaGlobalAttributeUtils.getAttributeFromGlobalAttr(rga);
            Group targetGroup = groupMap.get(fullGroupPath);
            ncfw.addGroupAttribute(targetGroup, attr);
        }
    }



    private void makeCoordinateVariables(Object name, List<String> strData) {
        // compute max/min attrs
        List<Attribute> maxMinAttrs = getMaxMinAttrs(Array.makeArray(DataType.FLOAT, strData));
        String strName = name.toString();

        Variable theNewVar = ncfw.addVariable(rootGroup, name.toString(), DataType.FLOAT, timeDimName);
        String unit = data.get(strName).firstEntry().getValue().getUnit();
        if (!unit.isEmpty()) {
            theNewVar.addAttribute(new Attribute("units", unit));
        }
        theNewVar.addAll(maxMinAttrs);
        if (name.equals(latDimName)) {
            theNewVar.addAttribute(new Attribute("standard_name", "latitude"));
            theNewVar.addAttribute(new Attribute("long_name", "latitude"));
            theNewVar.addAttribute(new Attribute("axis", "Y"));
        } else if (name.equals(lonDimName)) {
            theNewVar.addAttribute(new Attribute("standard_name", "longitude"));
            theNewVar.addAttribute(new Attribute("long_name", "longitude"));
            theNewVar.addAttribute(new Attribute("axis", "X"));
        } else if (name.equals(depthDimName)) {
            theNewVar.addAttribute(new Attribute("standard_name", "depth"));
            theNewVar.addAttribute(new Attribute("long_name", "depth"));
            theNewVar.addAttribute(new Attribute("positive", "down"));
            theNewVar.addAttribute(new Attribute("axis", "Z"));
        } else {
            log.error("unhandled coordinate variable: " + name.toString());
        }
    }


    public String convert(String ncfileFinal) throws InvalidRangeException, IOException {
        return convert(ncfileFinal, null);
    }

    public String convert(String ncfileFinal, Template template) throws InvalidRangeException, IOException {
        Path ncPath = PathUtils.replaceExtension(Paths.get(ncfileFinal), "temp");
        String ncfile = ncPath.toString();

        // if a template is passed in, pull out the global metadata from it and update the
        // metadata extracted from the eTuff file
        if (template != null) {
            Template metadataFromFileTemplate = new Template();
            // make a template object containing just the global metadata from the eTuff file
            metadataFromFileTemplate.setGlobalMetadata(rosettaGlobalAttributes);
            // update the file metadata with template metadata
            metadataFromFileTemplate.update(template);
            // pull out the updated globalmetata list from the updated template
            rosettaGlobalAttributes = metadataFromFileTemplate.getGlobalMetadata();
        }

        rootGroup = null;

        if (useNetcdf4) {
            ncfw = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, ncfile);
            rootGroup = ncfw.addGroup(null, null);
        } else {
            ncfw = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, ncfile);
        }

        // create time dimension
        int numTimeRecords = matchupOneLocOneOb ? data.get(latDimName).size() : getMaxNumRecords();
        Dimension timeDimension = ncfw.addDimension(rootGroup, timeDimName, numTimeRecords);


        // add global attributes
        if (useNetcdf4) {
            // netCDF-4 installed - write global attributes in rootGroup
            makeMetadataGroups();
        } else {
            for (RosettaGlobalAttribute rga : rosettaGlobalAttributes) {
                Attribute ga = RosettaGlobalAttributeUtils.getAttributeFromGlobalAttr(rga);
                ncfw.addGroupAttribute(rootGroup, ga);
            }
        }

        // add the needed CF traj attributes
        ncfw.addGroupAttribute(rootGroup, new Attribute("Conventions", "CF-1.6"));
        ncfw.addGroupAttribute(rootGroup, new Attribute("featureType", "trajectory"));

        String trajectoryId = getTrajId();
        ncfw.addDimension(null, "str_len", trajectoryId.length());
        Variable trajId = ncfw.addVariable(rootGroup, trajectoryIdName, DataType.CHAR, "str_len");
        trajId.addAttribute(new Attribute("cf_role", "trajectory_id"));

        // create time coordinate variable (special since it's not part of the data object
        Variable theNewVar = ncfw.addVariable(rootGroup, timeDimName, DataType.INT, timeDimName);
        theNewVar.addAttribute(new Attribute("standard_name", timeDimName));
        theNewVar.addAttribute(new Attribute("long_name", timeDimName));
        theNewVar.addAttribute(new Attribute("axis", "T"));
        String timeUnit = "seconds since 1970-01-01T00:00:00 UTC";
        theNewVar.addAttribute(new Attribute("units", timeUnit));

        if (!matchupOneLocOneOb) {
            // create freshness time coordinate variable
            // because we are matching one location with more than one ob, it's important to know
            // how "fresh" the location data are compared to the individual data point
            theNewVar = ncfw.addVariable(rootGroup, freshnessVarName, DataType.FLOAT, timeDimName);
            theNewVar.addAttribute(new Attribute("long_name", freshnessVarName));
            theNewVar.addAttribute(new Attribute("units", "seconds"));
            theNewVar.addAttribute(new Attribute("description",
                    "time since last latitude and longitude observation were obtained."));
        }

        // create variables:
        List<String> sortedKeys = new ArrayList<>(data.keySet());
        Collections.sort(sortedKeys);
        List<String> trajVarNames = new ArrayList<>();

        for (Object name : sortedKeys) {
            String strName = name.toString();
            // todo: grab obs to find max/min, add to attributes
            if (trajCoordVarNames.contains(name.toString())) {
                List<String> strData = data.get(strName).values().stream()
                        .map(Ob::getValue)
                        .collect(Collectors.toList());
                makeCoordinateVariables(name, strData);
            } else {
                int numVarRecords = data.get(strName).size();
                // only add variables which have time series available (i.e. not histogram)
                // add a small fudge factor in identifying possible time series variables
                // from which we want matchup with lat/lon values.
                // one loc many obs check
                int expectedTimeseriesLength = matchupOneLocOneOb ? getMaxNumRecords() : timeDimension.getLength();
                boolean isVar = Math.abs(numVarRecords - expectedTimeseriesLength) <= 10;

                if (isVar) {
                    if (!name.equals("datetime")) {
                        List<String> strData = data.get(strName).values().stream()
                                .map(Ob::getValue)
                                .collect(Collectors.toList());
                        // compute max/min
                        List<Attribute> maxMinAttrs = getMaxMinAttrs(Array.makeArray(DataType.FLOAT, strData));

                        theNewVar = ncfw.addVariable(rootGroup, name.toString(), DataType.FLOAT, timeDimName);
                        trajVarNames.add(name.toString());
                        String unit = data.get(strName).firstEntry().getValue().getUnit();
                        if (!unit.isEmpty()) {
                            theNewVar.addAttribute(new Attribute("units", unit));
                        }
                        theNewVar.addAttribute(new Attribute("coordinates", trajCoordVarNames));
                        theNewVar.addAll(maxMinAttrs);
                    }
                } else {
                    log.warn("Skipping non time series variable " + name.toString());
                }
            }
        }

        trajVarNames.add(latDimName);
        trajVarNames.add(lonDimName);
        trajVarNames.add(depthDimName);

        // create the file - writes medata and basic structure
        ncfw.create();

        // write the data, yo!

        // since this is a trajectory file, only write out data when we have lat, lon, and depth obs
        // for a given date

        // get latitude variable

        TreeMap<Long, Ob> dateTimeVar = data.get(maxTimeVar);
        TreeMap<Long, Ob> latVar = data.get("latitude");

        // because we use a TreeMap, the iterator of the Set returned by keySet() are sorted
        // in ascending order.
        Set<Long> times = matchupOneLocOneOb ? latVar.keySet() : dateTimeVar.keySet();

        List<String> varValues;
        List<String> freshnessValues = new ArrayList<>();

        // find the matching obs closest to a trajectory time and write it out to
        // the netCDF file
        for (Object name : trajVarNames) {
            String strName = name.toString();
            varValues = new ArrayList<>();
            // find a matching ob for the given trajectory time
            if (matchupOneLocOneOb) {
                // we are matching one location to a single ob
                for (Long time : times) {
                    TreeMap<Long, Ob> varTM = data.get(strName);
                    // ob time closest to before the trajectory time
                    Long before = varTM.floorKey(time);
                    // ob time closest to after the trajectory time
                    Long after = varTM.ceilingKey(time);
                    Ob value = null;

                    // find which is actually closest
                    if ((before != null) && (after != null)) {
                        if ((time - before) < (after - time)) {
                            value = varTM.get(before);
                        } else {
                            value = varTM.get(after);
                        }
                    } else if (before != null) {
                        value = varTM.get(before);
                    } else if (after != null) {
                        value = varTM.get(after);
                    } else {
                        log.error("no time match found!");
                    }
                    varValues.add(value.getValue());
                }
            } else {
                // we are matching one location to many obs
                for (Long time : times) {
                    TreeMap<Long, Ob> varTM = data.get(strName);

                    // ob time closest to before the datetime time
                    Long before = varTM.floorKey(time);

                    Ob value = null;

                    // find which is actually closest
                    if (before != null) {
                        value = varTM.get(before);
                    } else {
                        log.error("no time match found!");
                    }
                    varValues.add(value.getValue());
                    if (name.equals(latDimName)) {
                        freshnessValues.add(String.valueOf(time - before));
                    }
                }
            }

            Array thisData = ArrayFloat.makeArray(DataType.FLOAT, varValues);
            Variable thisVar = ncfw.findVariable(name.toString());
            ncfw.write(thisVar, thisData);
        }

        // write out the values for the time variable
        varValues = new ArrayList<>();
        for (Long time : times) {
            varValues.add(time.toString());
        }

        Array thisData = ArrayInt.makeArray(DataType.INT, varValues);
        Variable thisVar = ncfw.findVariable(timeDimName);
        ncfw.write(thisVar, thisData);

        thisData = ArrayChar.makeFromString(trajectoryId, trajectoryId.length());
        thisVar = ncfw.findVariable(trajId.getFullName());
        ncfw.write(thisVar, thisData);

        if (!matchupOneLocOneOb) {
            // write out "freshness" of the location data
            thisData = Array.makeArray(DataType.FLOAT, freshnessValues);
            thisVar = ncfw.findVariable(freshnessVarName);
            ncfw.write(thisVar, thisData);
        }

        // close up
        ncfw.close();

        // we don't know all of the data values for all variables at the time we are writing
        // metadata, so we must re-write the file with the max/min metadata here.
        NetcdfFile ncf = NetcdfFile.open(ncfile);
        Variable theVar = ncf.findVariable(timeDimName);
        List<Attribute> theAttrs = getMaxMinAttrs(theVar.read());
        theVar.addAll(theAttrs);

        if (!matchupOneLocOneOb) {
            theVar = ncf.findVariable(freshnessVarName);
            theAttrs = getMaxMinAttrs(theVar.read());
            theVar.addAll(theAttrs);
        }

        if (useNetcdf4) {
            FileWriter2 fw2 = new FileWriter2(ncf, ncfileFinal, NetcdfFileWriter.Version.netcdf4, null);
            NetcdfFile ncfw2 = fw2.write();
            ncfw2.close();
        } else {
            FileWriter2 fw2 = new FileWriter2(ncf, ncfileFinal, NetcdfFileWriter.Version.netcdf3, null);
            NetcdfFile ncfw2 = fw2.write();
            ncfw2.close();
        }


        ncf.close();

        Files.delete(ncPath);

        return ncfileFinal;
    }
}