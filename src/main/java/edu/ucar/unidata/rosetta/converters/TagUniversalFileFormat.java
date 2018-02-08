/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 */

package edu.ucar.unidata.rosetta.converters;

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
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.time.CalendarDate;

/**
 * Tag Base Archival Tags (flat file) following the OIIP Tag Universal File Format (TUFF)
 *
 * This class controls the conversion of Archival Tags as exported by TagBase.
 */

public class TagUniversalFileFormat {

    static Logger log = Logger.getLogger(TagUniversalFileFormat.class.getName());

    private String tuflFile = "";
    private List<Attribute> globalAttrs = new ArrayList<>();
    private HashMap<String, TreeMap<Long, Ob>> data = new HashMap<>();
    private HashMap<String, TreeMap<Integer, HistBin>> binInfoMin = new HashMap<>();
    private HashMap<String, TreeMap<Integer, HistBin>> binInfoMax = new HashMap<>();

    private boolean matchupOneLocOneOb = true;

    private String timeDimName = "time";
    private String latDimName = "latitude";
    private String lonDimName = "longitude";
    private String depthDimName = "depth";
    private String dateTimehDimName = "datetime";
    private String trajectoryIdName = "trajectory";
    private String freshnessVarName = "location_freshness";
    private String trajCoordVarNames = String.join(" ", timeDimName, latDimName, lonDimName, depthDimName, trajectoryIdName);

    private String timeUnit = "seconds since 1970-01-01T00:00:00 UTC";


    public TagUniversalFileFormat() {
    }

    class HistBin {
        private float binValue;
        private String binUnit;

        public HistBin(float value, String unit) {
            this.binValue = value;
            this.binUnit = unit;
        }

        public float getBinValue() {
            return binValue;
        }

        public String getBinUnit() {
            return binUnit;
        }
    }

    class Ob {

        private String value;
        private String unit;

        public String getValue() {
            return value;
        }

        public String getUnit() {
            return unit;
        }

        public Ob(String value, String unit) {
            this.value = value;
            this.unit = unit;
        }
    }

    /**
     * set how matchup should be done
     *
     * if true, matchup will be one ob per location
     *
     * if false, matchup will be many obs per location
     * 
     * @param matchupOneLocOneOb
     */
    public void setMatchupOneLocOneOb(boolean matchupOneLocOneOb) {
        this.matchupOneLocOneOb = matchupOneLocOneOb;
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

    private void updateBinInfo(HashMap<String, TreeMap<Integer, HistBin>> binInfo, String name, int binNumber, HistBin histBin) {
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
                globalAttrs.add(new Attribute(keyVals[0].trim(), keyVals[1].trim()));
            } else {
                // skip if the line is the column definition line, but without quotes
                if (!line.contains("VariableID")) {
                    // missed something
                    log.error("Global Attr line " + line + " not parsed correctly");
                }
            }
        }
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
            if (dataEntry.length == 5 |
                    (dataEntry.length == 4 && line.endsWith(","))) {
                // full data line, but could be missing units
                String datetime = dataEntry[0];
                int variableId = Integer.parseInt(dataEntry[1]);
                String value = dataEntry[2];
                String name = cleanVarName(dataEntry[3]);
                String unit = "";
                if (!line.endsWith(",")) {
                    unit = cleanUnitString(dataEntry[4]);
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

    private boolean writeNcFile(String ncFileName) {
        return false;
    }

    public void parse(String tuflFile) {
        log.debug("start conversion!");
        this.tuflFile = tuflFile;
        String line;
        boolean readSuccess;

        Path tbFilePath = Paths.get(tuflFile);
        BufferedReader br = null;
        try {
            if (tuflFile.endsWith(".gz")) {
                InputStream fileStream = new FileInputStream(tbFilePath.toString());
                InputStream gzipStream = new GZIPInputStream(fileStream);
                Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.ISO_8859_1);
                br = new BufferedReader(decoder);
            } else {
                br = Files.newBufferedReader(tbFilePath, StandardCharsets.ISO_8859_1);
            }

            log.debug("opened the etuff file!");
            line = br.readLine();
            line = line.trim().trim();
            log.debug("reading etuff file!");
            while (br.ready()) {
                // Read the header (delimited by the : character or //, but ignore // as these are
                // just comments)
                String first = line.substring(0, 1);
                if (first.equals(":") | first.equals("/") | first.equals("\"") | first.equals("D")) {
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
            readSuccess = false;
            log.error("Error reading eTuff file", ioe);
        }

        log.debug("done!");
        printInventory();
    }

    private void printInventory() {
        log.debug("Inventory:");
        List sortedKeys = new ArrayList(data.keySet());
        Collections.sort(sortedKeys);

        for (Object name : sortedKeys) {
            int len = data.get(name).size();
            log.debug("   " + name + " (" + len + ")");
        }
    }

    public HashMap<String, String> getGlobalMetadata() {
        // should probably encode in json to return to browser
        HashMap<String, String> globalMetadata = new HashMap<>();
        for (Attribute attr : globalAttrs) {
            globalMetadata.put(attr.getFullName(), attr.getStringValue());
        }
        return globalMetadata;
    }

    public String convert(String ncfile) throws InvalidRangeException {
        // create the trajectory file
        // open a netcdf file

        if (matchupOneLocOneOb) {
            ncfile = convertOneLocOneOb(ncfile);
        } else {
            ncfile = convertOneLocManyObs(ncfile);
        }
        return ncfile;
    }

    private String getTrajId() {
        String trajectoryId = "1"; // default as trajectory 1; otherwise, look for serial number
        // add global attribute
        for (Attribute ga : globalAttrs) {
            if (ga.getFullName().equals("serial_number")) {
                trajectoryId = ga.getStringValue();
            }
        }
        return trajectoryId;
    }

    private String convertOneLocOneOb(String ncfile) throws InvalidRangeException {
        try (NetcdfFileWriter ncfw = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, ncfile)) {
            Group group = null;

            int numTrajRecords = data.get(latDimName).size();
            int numTimeRecords = data.get(dateTimehDimName).size();
            String trajectoryId = getTrajId();

            // create coordinate dimensions
            ncfw.addDimension(group, timeDimName, numTrajRecords);

            // add global attribute
            for (Attribute ga : globalAttrs) {
                ncfw.addGroupAttribute(group, ga);
            }

            // add the needed CF traj attributes
            ncfw.addGroupAttribute(group, new Attribute("Conventions", "CF-1.6"));
            ncfw.addGroupAttribute(group, new Attribute("featureType", "trajectory"));

            ncfw.addDimension(null, "str_len", trajectoryId.length());
            Variable trajId = ncfw.addVariable(group, trajectoryIdName, DataType.CHAR, "str_len");
            trajId.addAttribute(new Attribute("cf_role", "trajectory_id"));

            // create time coordinate variable (special since it's not part of the data object
            Variable theNewVar = ncfw.addVariable(group, timeDimName, DataType.INT, timeDimName);
            theNewVar.addAttribute(new Attribute("standard_name", timeDimName));
            theNewVar.addAttribute(new Attribute("long_name", timeDimName));
            theNewVar.addAttribute(new Attribute("axis", "T"));
            theNewVar.addAttribute(new Attribute("units", timeUnit));

            // create variables:
            List sortedKeys = new ArrayList(data.keySet());
            Collections.sort(sortedKeys);
            List<String> trajVarNames = new ArrayList<>();

            for (Object name : sortedKeys) {
                if (trajCoordVarNames.contains(name.toString())) {
                    theNewVar = ncfw.addVariable(group, name.toString(), DataType.FLOAT, timeDimName);
                    String unit = data.get(name).firstEntry().getValue().getUnit();
                    if (!unit.isEmpty()) {
                        theNewVar.addAttribute(new Attribute("units", unit));
                    }
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
                } else {
                    int numVarRecords = data.get(name).size();
                    // only add variables which have time series available (i.e. not histogram)
                    // add a small fudge factor in identifying possible time series variables
                    // from which we want matchup with lat/lon values.
                    if (Math.abs(numVarRecords - numTimeRecords) <= 10) {
                        if (!name.equals("datetime")) {
                            theNewVar = ncfw.addVariable(group, name.toString(), DataType.FLOAT, timeDimName);
                            trajVarNames.add(name.toString());
                            String unit = data.get(name).firstEntry().getValue().getUnit();
                            if (!unit.isEmpty()) {
                                theNewVar.addAttribute(new Attribute("units", unit));
                            }
                            theNewVar.addAttribute(new Attribute("coordinates", trajCoordVarNames));
                        }
                    } else {
                        log.warn("Skipping non time series variable " + name.toString());
                    }
                }
            }

            // extend trajVarNames to include appropriate trajCoordVarNames (all but time)
            trajVarNames.add(latDimName);
            trajVarNames.add(lonDimName);
            trajVarNames.add(depthDimName);

            // create the file - writes medata and basic structure
            ncfw.create();

            // write the data, yo!

            // since this is a trajectory file, only write out data when we have lat, lon, and depth obs
            // for a given date

            // get latitude variable

            TreeMap<Long, Ob> latVar = data.get("latitude");

            // because we use a TreeMap, the iterator of the Setreturned by keySet() are sorted
            // in ascending order.
            Set<Long> times = latVar.keySet();
            List<String> varValues = new ArrayList<>();

            // find the matching obs closest to a trajectory time and write it out to
            // the netCDF file
            for (Object name : trajVarNames) {
                varValues = new ArrayList<>();
                // find a matching ob for the given trajectory time
                for (Long time : times) {
                    TreeMap<Long, Ob> varTM = data.get(name);
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
                    varValues.add(value.value);
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

            // close up
            ncfw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ncfile;
    }

    private String convertOneLocManyObs(String ncfile) throws InvalidRangeException {
        // here we try to matchup all obs to a location. The location data is obtained much less recently
        // than the observations, so it's a one to many matchup.
        try (NetcdfFileWriter ncfw = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, ncfile)) {

            Group group = null;

            int numTimeRecords = data.get(dateTimehDimName).size();
            String trajectoryId = getTrajId();

            // create coordinate dimensions
            ncfw.addDimension(group, timeDimName, numTimeRecords);

            // add global attributes
            for (Attribute ga : globalAttrs) {
                ncfw.addGroupAttribute(group, ga);
            }

            // add the needed CF traj attributes
            ncfw.addGroupAttribute(group, new Attribute("Conventions", "CF-1.6"));
            ncfw.addGroupAttribute(group, new Attribute("featureType", "trajectory"));

            ncfw.addDimension(null, "str_len", trajectoryId.length());
            Variable trajId = ncfw.addVariable(group, trajectoryIdName, DataType.CHAR, "str_len");
            trajId.addAttribute(new Attribute("cf_role", "trajectory_id"));

            // create time coordinate variable (special since it's not part of the data object
            Variable theNewVar = ncfw.addVariable(group, timeDimName, DataType.INT, timeDimName);
            theNewVar.addAttribute(new Attribute("standard_name", timeDimName));
            theNewVar.addAttribute(new Attribute("long_name", timeDimName));
            theNewVar.addAttribute(new Attribute("axis", "T"));
            theNewVar.addAttribute(new Attribute("units", timeUnit));

            // create freshness time coordinate variable (special since it's not part of the data object
            theNewVar = ncfw.addVariable(group, freshnessVarName, DataType.INT, timeDimName);
            theNewVar.addAttribute(new Attribute("long_name", freshnessVarName));
            theNewVar.addAttribute(new Attribute("units", "seconds"));
            theNewVar.addAttribute(new Attribute("description", "time since last latitude and longitude observation were obtained."));
            // create variables:
            List sortedKeys = new ArrayList(data.keySet());
            Collections.sort(sortedKeys);
            List<String> trajVarNames = new ArrayList<>();

            for (Object name : sortedKeys) {
                if (trajCoordVarNames.contains(name.toString())) {
                    theNewVar = ncfw.addVariable(group, name.toString(), DataType.FLOAT, timeDimName);
                    String unit = data.get(name).firstEntry().getValue().getUnit();
                    if (!unit.isEmpty()) {
                        theNewVar.addAttribute(new Attribute("units", unit));
                    }
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
                } else {
                    int numVarRecords = data.get(name).size();
                    // only add variables which have time series available (i.e. not histogram)
                    // add a small fudge factor in identifying possible time series variables
                    // from which we want matchup with lat/lon values.
                    if (Math.abs(numVarRecords - numTimeRecords) <= 10) {
                        if (!name.equals("datetime")) {
                            theNewVar = ncfw.addVariable(group, name.toString(), DataType.FLOAT, timeDimName);
                            trajVarNames.add(name.toString());
                            String unit = data.get(name).firstEntry().getValue().getUnit();
                            if (!unit.isEmpty()) {
                                theNewVar.addAttribute(new Attribute("units", unit));
                            }
                            theNewVar.addAttribute(new Attribute("coordinates", trajCoordVarNames));
                        }
                    } else {
                        log.warn("Skipping non time series variable " + name.toString());
                    }
                }
            }

            // extend trajVarNames to include appropriate trajCoordVarNames (all but time)
            trajVarNames.add(latDimName);
            trajVarNames.add(lonDimName);
            trajVarNames.add(depthDimName);

            // create the file - writes medata and basic structure
            ncfw.create();

            // write the data, yo!

            // since this is a trajectory file, only write out data when we have lat, lon, and depth obs
            // for a given date

            // get latitude variable

            TreeMap<Long, Ob> dateTimeVar = data.get(dateTimehDimName);

            // because we use a TreeMap, the iterator of the Setreturned by keySet() are sorted
            // in ascending order.
            Set<Long> times = dateTimeVar.keySet();
            List<String> varValues = new ArrayList<>();
            List<String> freshnessValues = new ArrayList<>();

            // find the matching obs closest to a trajectory time and write it out to
            // the netCDF file
            for (Object name : trajVarNames) {
                varValues = new ArrayList<>();
                // find a matching ob for the given trajectory time
                for (Long time : times) {
                    TreeMap<Long, Ob> varTM = data.get(name);
                    // ob time closest to before the datetime time
                    Long before = varTM.floorKey(time);

                    Ob value = null;

                    // find which is actually closest
                    if (before != null) {
                        value = varTM.get(before);
                    } else {
                        log.error("no time match found!");
                    }
                    varValues.add(value.value);
                    if (name.equals(latDimName)) {
                        freshnessValues.add(String.valueOf(time - before));
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

            // write out "freshness" of the location data
            thisData = ArrayInt.makeArray(DataType.INT, freshnessValues);
            thisVar = ncfw.findVariable(freshnessVarName);
            ncfw.write(thisVar, thisData);


            // close up
            ncfw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ncfile;
    }

}