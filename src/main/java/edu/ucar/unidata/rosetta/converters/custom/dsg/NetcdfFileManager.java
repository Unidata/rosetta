/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.custom.dsg;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.ucar.unidata.rosetta.domain.ParsedFile;
import edu.ucar.unidata.rosetta.domain.RosettaAttribute;
import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.VariableInfo;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.repository.resources.DelimiterResourceDao;
import edu.ucar.unidata.rosetta.util.PathUtils;
import edu.ucar.unidata.rosetta.util.RosettaGlobalAttributeUtils;
import edu.ucar.unidata.rosetta.util.TemplateUtils;
import edu.ucar.unidata.rosetta.util.VariableInfoUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateFormatter;

import static edu.ucar.unidata.rosetta.converters.utils.VariableAttributeUtils.getMaxMinAttrs;
import static java.lang.Math.toIntExact;
import static ucar.nc2.time.CalendarDate.parseUdunits;

public abstract class NetcdfFileManager {

    protected static Logger logger = Logger.getLogger(NetcdfFileManager.class);

    private List<VariableInfo> coordVars = new ArrayList<>();
    private List<VariableInfo> dataVars = new ArrayList<>();
    private List<VariableInfo> timeVars = new ArrayList<>();

    private List<String> coordVarNames = new ArrayList<>();

    private List<String> timeVarTypes = new ArrayList<>();
    private Map<Integer, Array> arrayData;
    private Map<Integer, List<String>> stringData;
    String myDsgType;
    String featureId;
    String featureVarName;

    NetcdfFileWriter ncf;

    private Array timeArr;
    private String timeVarName;
    Dimension timeDim;

    List<String> coordAttrValues = new ArrayList<>();

    private boolean hasNetcdf4 = false;
    private String timeUnits = "seconds since 1970-01-01T00:00:00";
    private static String colIdAttrName = "_Rosetta_columnId";

    abstract void makeDataVars(VariableInfo variableInfo);

    abstract void makeNonTimeCoordVars(VariableInfo variableInfo);

    abstract void makeOtherVariables();

    /**
     * Get the Discrete Sampling Geometry type handled by the converter
     *
     * @return The discrete sampling geometry
     */
    String getMyDsgType() {
        return myDsgType;
    }

    /**
     * Check if this converter works with a given Discrete Sampling Geometry
     */
    public boolean isMine(String reqType) {
        boolean mine = false;
        if (getMyDsgType().equals(reqType)) {
            mine = true;
        }
        return mine;
    }

    /**
     * Get a list of converters
     */
    public static List<NetcdfFileManager> getConverters() {
        List<NetcdfFileManager> dsgWriters = new ArrayList<>();

        dsgWriters.add(new SingleTrajectory());

        return dsgWriters;
    }

    /**
     * Convert list of date/time string to a list of longs with values representative of seconds
     * since 1970-01-01T00:00:00UTC
     *
     * @param data   List of date/time strings
     * @param format formart of the date/time strings
     * @return list of values in seconds since 1970-01-01
     */
    private Array createLongTimeDataFromFullDateTime(List<String> data, String format) {
        // todo - support subsecond date/time strings
        CalendarDateFormatter fmt = new CalendarDateFormatter(format);
        int numVals = data.size();
        long[] newTimeVals = new long[numVals];

        for (int i = 0; i < numVals; i++) {

            CalendarDate dateTime = fmt.parse(data.get(i));
            newTimeVals[i] = dateTime.getMillis() / 1000L;
        }
        return Array.makeFromJavaArray(newTimeVals);
    }

    /**
     * Convert list of date/time string to a list of ints with values representative of seconds
     * since 1970-01-01T00:00:00UTC
     *
     * @param data   List of date/time strings
     * @param format formart of the date/time strings
     * @return list of values in seconds since 1970-01-01
     */
    private Array createIntTimeDataFromFullDateTime(List<String> data, String format) {
        CalendarDateFormatter fmt = new CalendarDateFormatter(format);
        int numVals = data.size();
        int[] newTimeVals = new int[numVals];

        for (int i = 0; i < numVals; i++) {

            CalendarDate dateTime = fmt.parse(data.get(i));
            newTimeVals[i] = toIntExact(dateTime.getMillis() / 1000L);
        }
        return Array.makeFromJavaArray(newTimeVals);
    }

    /**
     * Sort out three kinds of variable types: - Time related coordinate variables - Non-time
     * related coordinate variables - data variables
     *
     * @param template template containing VariableInfo objects
     */
    private void identifyVariables(Template template) {
        // identify coordinate variables and separate out time related coordinate variables
        List<VariableInfo> variableInfoList = template.getVariableInfoList();
        if (variableInfoList != null) {
            for (VariableInfo varInfo : variableInfoList) {
                if (VariableInfoUtils.isVarUsed(varInfo)) {
                    if (!VariableInfoUtils.isCoordinateVariable(varInfo)) {
                        // not a coordinate variable
                        dataVars.add(varInfo);
                    } else {
                        // some kind of coordinate variable
                        if (VariableInfoUtils.isTimeCoordVar(varInfo)) {
                            timeVars.add(varInfo);
                            timeVarTypes.add(VariableInfoUtils.getCoordVarType(varInfo));
                        } else {
                            coordVars.add(varInfo);
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper method to get the basic time coordinate attributes
     *
     * @return A list of time related attributes
     */
    private List<Attribute> getBaseTimeVarAttrs() {
        List<Attribute> baseTimeVarAttrs = new ArrayList<>();
        baseTimeVarAttrs.add(new Attribute("axis", "T"));
        baseTimeVarAttrs.add(new Attribute("long_name", "time"));
        baseTimeVarAttrs.add(new Attribute("standard_name", "time"));
        baseTimeVarAttrs.add(new Attribute("units", timeUnits));
        return baseTimeVarAttrs;
    }

    /**
     * Create the time variable based on a relative time coordinate variable
     *
     * @param relativeTimeVi The relative time coordinate variable
     * @return <code>true</code> if successfully created, otherwise false
     */
    private boolean makeTimeVarFromRelativeTime(VariableInfo relativeTimeVi) {
        boolean success;
        Group group = null;
        String timeDimName = "time";
        int numTimeVals;
        try {
            timeArr = arrayData.get(relativeTimeVi.getColumnId());
            numTimeVals = toIntExact(timeArr.getSize());
            timeDim = ncf.addDimension(timeDimName, toIntExact(numTimeVals));
            timeVarName = relativeTimeVi.getName();
            DataType dataType = VariableInfoUtils.getDataType(relativeTimeVi);
            RosettaAttribute timeUnitAttr = VariableInfoUtils.findAttributeByName("units", relativeTimeVi);
            timeUnits = timeUnitAttr.getValue();

            Variable timeVar = ncf.addVariable(group, timeVarName, dataType, Collections.singletonList(timeDim));
            List<Attribute> timeVarAttrs = getBaseTimeVarAttrs();
            timeVarAttrs.addAll(getMaxMinAttrs(timeArr));
            timeVar.addAll(timeVarAttrs);

            coordAttrValues.add(timeVarName);
            success = true;
        } catch (ArithmeticException ae) {
            logger.error("Size of the dimension could not fit in an integer value");
            success = false;
        }

        return success;

    }

    /**
     * Create the time variable based on a full date/time time coordinate variable
     *
     * @param fullDateTimeVi The full date/time coordinate variable
     * @return <code>true</code> if successfully created, otherwise false
     */
    private boolean makeTimeVarFromFullDateTime(Template template, VariableInfo fullDateTimeVi) {
        boolean success;
        Group group = null;
        String dimName = TemplateUtils.findUniqueName("time", template);

        int numTimeVals;
        try {
            List<String> fullDateTime = stringData.get(fullDateTimeVi.getColumnId());
            numTimeVals = toIntExact(fullDateTime.size());
            String dateTimeFormat = VariableInfoUtils.getUnit(fullDateTimeVi);

            DataType dataType = DataType.LONG;
            if (hasNetcdf4) {
                timeArr = createLongTimeDataFromFullDateTime(fullDateTime, dateTimeFormat);
            } else {
                timeArr = createIntTimeDataFromFullDateTime(fullDateTime, dateTimeFormat);
            }

            timeDim = ncf.addDimension(dimName, toIntExact(numTimeVals));

            // in this case, we are creating a totally new variable, so we need to check
            // if the variable "time" already exists in data and coordinate variables
            timeVarName = dimName;

            Variable timeVar = ncf.addVariable(group, timeVarName, dataType, Collections.singletonList(timeDim));
            List<Attribute> timeVarAttrs = getBaseTimeVarAttrs();
            timeVarAttrs.addAll(getMaxMinAttrs(timeArr));
            timeVar.addAll(timeVarAttrs);

            coordAttrValues.add(timeVarName);
            success = true;
        } catch (ArithmeticException ae) {
            logger.error("Size of the dimension could not fit in an integer value");
            success = false;
        }

        return success;

    }

    /**
     * Create the time variable based on a date and time coordinate variables, stored as different
     * VariableInfo objects
     *
     * @param template The template object
     * @param dateOnly The VariableInfo object containing only the date information
     * @param timeOnly The VariableInfo object containing only the time information
     * @return <code>true</code> if successfully created, otherwise false
     */
    private boolean makeTimeVarFromDateTimeOnly(Template template, VariableInfo dateOnly, VariableInfo timeOnly) {
        boolean success;
        Group group = null;
        String dimName = TemplateUtils.findUniqueName("time", template);
        int numTimeVals;

        List<String> dateTimeVals = stringData.get(dateOnly.getColumnId());
        numTimeVals = toIntExact(dateTimeVals.size());
        String dateFormat = VariableInfoUtils.getUnit(dateOnly);

        DataType dataType = DataType.INT;
        if (hasNetcdf4) {
            dataType = DataType.LONG;
        }

        // if timeOnly is not null, then we need to combine that info with the dateOnly info
        // (format and data values) to convert those into a new time variable
        if (timeOnly != null) {
            List<String> timeVals = stringData.get(timeOnly.getColumnId());
            String timeFormat = VariableInfoUtils.getUnit(timeOnly);
            for (int i = 0; i < dateTimeVals.size(); i++) {
                dateTimeVals.set(i, dateTimeVals.get(i) + timeVals.get(i));
            }
            dateFormat = dateFormat + timeFormat;
        }

        if (hasNetcdf4) {
            timeArr = createLongTimeDataFromFullDateTime(dateTimeVals, dateFormat);
        } else {
            timeArr = createIntTimeDataFromFullDateTime(dateTimeVals, dateFormat);
        }

        try {
            timeDim = ncf.addDimension(dimName, toIntExact(numTimeVals));
            // in this case, we are creating a totally new variabled, so we need to check
            // if the variable "time" already exists in data and coordinate variables
            timeVarName = dimName;

            Variable timeVar = ncf.addVariable(group, timeVarName, dataType, Collections.singletonList(timeDim));
            List<Attribute> timeVarAttrs = getBaseTimeVarAttrs();
            timeVarAttrs.addAll(getMaxMinAttrs(timeArr));
            timeVar.addAll(timeVarAttrs);

            coordAttrValues.add(timeVarName);

            success = true;
        } catch (ArithmeticException ae) {
            success = false;
        }

        return success;
    }

    /**
     * Get the auto-computed attributes for a given coordinate VariableInfo object
     *
     * @param variableInfo The coordinate variable for which attributes should be computed
     * @return A list of computed attributes
     */
    List<Attribute> calculateCoordVarAttrs(VariableInfo variableInfo) {
        List<Attribute> calculatedCoordVarAttrs = new ArrayList<>();

        //CoordinateVariable	axis
        String type = VariableInfoUtils.getCoordVarType(variableInfo);

        if (type.equals(VariableInfoUtils.longitude)) {
            calculatedCoordVarAttrs.add(new Attribute("axis", "X"));
        } else if (type.equals(VariableInfoUtils.latitude)) {
            calculatedCoordVarAttrs.add(new Attribute("axis", "Y"));
        } else if (type.equals(VariableInfoUtils.vertical)) {
            calculatedCoordVarAttrs.add(new Attribute("axis", "Z"));
            RosettaAttribute positive = VariableInfoUtils.findAttributeByName(VariableInfoUtils.positiveAttrName, variableInfo);
            if (positive != null) {
                calculatedCoordVarAttrs.add(new Attribute(positive.getName(), positive.getValue()));
            }
        }

        //CoordinateVariable	coverage_content_type
        // no good way to guess this - will need to come from the template

        //CoordinateVariable	valid_min, valid_max*
        Array data = arrayData.get(variableInfo.getColumnId());
        calculatedCoordVarAttrs.addAll(getMaxMinAttrs(data));

        // add columnId if it was initilized in attribute
        int colId = variableInfo.getColumnId();
        if (colId > 0) {
            calculatedCoordVarAttrs.add(new Attribute(colIdAttrName, variableInfo.getColumnId()));

        }

        return calculatedCoordVarAttrs;
    }

    /**
     * Get the auto-computed attributes for a given data VariableInfo object
     *
     * @param variableInfo The data variable for which attributes should be computed
     * @return A list of computed attributes
     */
    List<Attribute> calculateDataVarAttrs(VariableInfo variableInfo) {
        List<Attribute> calculatedDataVarAttrs = new ArrayList<>();
        //DataVariable	_FillValue
        //DataVariable	coordinates
        String coords = String.join(" ", coordAttrValues);
        calculatedDataVarAttrs.add(new Attribute("coordinates", coords));
        //DataVariable	coverage_content_type
        // no good way to guess this - will need to come from the template

        Array data = arrayData.get(variableInfo.getColumnId());
        calculatedDataVarAttrs.addAll(getMaxMinAttrs(data));

        // add columnId if it was initilized in attribute
        int colId = variableInfo.getColumnId();
        if (colId > 0) {
            calculatedDataVarAttrs.add(new Attribute(colIdAttrName, variableInfo.getColumnId()));
        }
        return calculatedDataVarAttrs;
    }

    /**
     * If the file to be converted follows the eTuff standard, auto-compute the needed attributes
     *
     * @return A list of computed eTuff attributes
     */
    private void addTuffGlobalAttrs() {
        int end = timeDim.getLength();
        for (VariableInfo coordVar : coordVars) {
            //CoordinateVariable	axis
            String type = VariableInfoUtils.getCoordVarType(coordVar);
            Array data = arrayData.get(coordVar.getColumnId());

            if (type.equals(VariableInfoUtils.longitude)) {
                ncf.addGlobalAttribute(new Attribute("geospatial_lon_start", data.getDouble(0)));
                ncf.addGlobalAttribute(new Attribute("geospatial_lon_end", data.getDouble(end)));
            } else if (type.equals(VariableInfoUtils.latitude)) {
                ncf.addGlobalAttribute(new Attribute("geospatial_lat_start", data.getDouble(0)));
                ncf.addGlobalAttribute(new Attribute("geospatial_lat_end", data.getDouble(end)));
            }
        }

        String startUdUnit = String.join(" ", String.valueOf(timeArr.getLong(0)), timeUnits);
        String stopUdUnit = String.join(" ", String.valueOf(timeArr.getLong(end)), timeUnits);

        CalendarDate start = parseUdunits(null, startUdUnit);
        CalendarDate stop = parseUdunits(null, stopUdUnit);

        ncf.addGlobalAttribute(new Attribute("time_coverage_start", CalendarDateFormatter.toDateTimeStringISO(start)));
        ncf.addGlobalAttribute(new Attribute("time_coverage_end", CalendarDateFormatter.toDateTimeStringISO(stop)));
    }

    /**
     * Get attributes related specifically to CF
     *
     * @param template template associated with the conversion
     * @return A list of cf specific attributes
     */
    private List<RosettaGlobalAttribute> getFeatureSpecificGlobalAttrs(Template template) {
        List<RosettaGlobalAttribute> featureSpecificAttrs = new ArrayList<>();

        featureSpecificAttrs.add(new RosettaGlobalAttribute("Conventions ", "CF-1.6", "STRING", "root"));
        featureSpecificAttrs.add(new RosettaGlobalAttribute("featureType ", template.getCfType(), "STRING", "root"));

        return featureSpecificAttrs;

    }

    /**
     * Create a netCDF file, following CF DSGs, based on the data contained within a data file and
     * the metadata contained within a template
     *
     * @param dataFile file containing observed data
     * @param template template associated with dataFile
     * @param delimiter the delimiter used to parse the file.
     * @return location of the created netCDF file
     */
    public String createNetcdfFile(Path dataFile, Template template, String delimiter) throws IOException, RosettaDataException {

        Path netcdfFile = PathUtils.replaceExtension(dataFile, ".nc");
        String netcdfFilePath = netcdfFile.toString();

        ParsedFile parsedFile = new ParsedFile(dataFile, template, delimiter);
        arrayData = parsedFile.getArrayData();
        stringData = parsedFile.getStringData();
        // TODO: add check to see if netCDF-4 is enabled

        ncf = NetcdfFileWriter.createNew(netcdfFilePath, false);

        identifyVariables(template);

        // create new time variable, if needed
        boolean timeVarHandled = false;
        if (timeVars.size() == 1) {
            if (timeVarTypes.contains(VariableInfoUtils.relativeTime)) {
                timeVarHandled = makeTimeVarFromRelativeTime(timeVars.get(0));
            } else if (timeVarTypes.contains(VariableInfoUtils.fullDateTime)) {
                timeVarHandled = makeTimeVarFromFullDateTime(template, timeVars.get(0));
            } else if (timeVarTypes.contains(VariableInfoUtils.dateOnly)) {
                timeVarHandled = makeTimeVarFromDateTimeOnly(template, timeVars.get(0), null);
            } else {
                String msg = "Do not understand how to handle a single time of type \"\"" +
                        VariableInfoUtils.getCoordVarType(timeVars.get(0));
                logger.error(msg);
            }
        } else if (timeVars.size() == 2) {
            if ((timeVarTypes.contains(VariableInfoUtils.dateOnly)) && (timeVarTypes.contains(VariableInfoUtils.timeOnly))) {
                // newTimeVarData = makeRelativeTime(timeVars.get(dateOnly), timeVars.get(timeOnly));
                String coordVarType = VariableInfoUtils.getCoordVarType(timeVars.get(0));
                if (coordVarType.equals(VariableInfoUtils.dateOnly)) {
                    timeVarHandled = makeTimeVarFromDateTimeOnly(template, timeVars.get(0), timeVars.get(1));
                }
            } else {
                logger.error("Two time vars founds, but not dateOnly and timeOnly.");
            }
        } else {
            logger.error("there should only be two time related variables defined - this found " + timeVars.size());
        }

        if (!timeVarHandled) {
            // stop conversion as a time variable was not created - indicate to use that they
            // need to check the log file
            logger.error("time variable not created. Stopping");
        }

        // add coordinate variable info
        for (VariableInfo coordVarInfo : coordVars) {
            makeNonTimeCoordVars(coordVarInfo);
        }

        // add data variable info
        for (VariableInfo dataVarInfo : dataVars) {
            makeDataVars(dataVarInfo);
        }

        // add global metadata
        // first, get from template

        Map<String, ArrayList<Attribute>> globalAttrs = TemplateUtils.getGlobalAttrsMap(template);

        // update global attribute lists obtained from template with new feature specific global attributes
        List<RosettaGlobalAttribute> featureSpecificGlobalAttrs = getFeatureSpecificGlobalAttrs(template);
        for (RosettaGlobalAttribute attr : featureSpecificGlobalAttrs) {
            String groupName = attr.getGroup();
            if (globalAttrs.containsKey(groupName)) {
                ArrayList<Attribute> globalAttr = globalAttrs.get(groupName);
                globalAttr.add(RosettaGlobalAttributeUtils.getAttributeFromGlobalAttr(attr));
                globalAttrs.put(groupName, globalAttr);
            } else {
                ArrayList<Attribute> globalAttr = new ArrayList<>();
                globalAttr.add(RosettaGlobalAttributeUtils.getAttributeFromGlobalAttr(attr));
                globalAttrs.put(groupName, globalAttr);
            }

        }

        // add globalAttributes to ncf
        for (String groupName : globalAttrs.keySet()) {
            // todo - add check for netCDF4, and use metadata groups if it is enabled
            for (Attribute globalAttr : globalAttrs.get(groupName)) {
                ncf.addGlobalAttribute(globalAttr);
            }
        }

        makeOtherVariables();

        // write all metadata and get ready for writing data values
        ncf.create();

        try {
            // write variable data to netCDF File

            // write time variable data
            ncf.write(timeVarName, timeArr);

            // write data to featureId variable
            ncf.write(featureVarName, Array.makeFromJavaArray(featureId.toCharArray()));

            // write data to all netCDF variables with an attribute containing the column id
            for (Variable var : ncf.getNetcdfFile().getVariables()) {
                Attribute colIdAttr = var.findAttribute(colIdAttrName);
                if (colIdAttr != null) {
                    Number colIdNum = colIdAttr.getNumericValue();
                    int colId = colIdNum.intValue();
                    ncf.write(var, arrayData.get(colId));
                }
            }

        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }

        ncf.close();

        return netcdfFilePath;
    }

}
