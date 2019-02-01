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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucar.unidata.rosetta.domain.ParsedFile;
import edu.ucar.unidata.rosetta.domain.RosettaAttribute;
import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.VariableInfo;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.util.PathUtils;
import edu.ucar.unidata.rosetta.util.RosettaAttributeUtils;
import edu.ucar.unidata.rosetta.util.RosettaGlobalAttributeUtils;
import edu.ucar.unidata.rosetta.util.TemplateUtils;
import edu.ucar.unidata.rosetta.util.VariableInfoUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateFormatter;

import static edu.ucar.unidata.rosetta.converters.utils.VariableAttributeUtils.getMaxMinAttrs;
import static java.lang.Math.toIntExact;
import static ucar.nc2.time.CalendarDate.parseISOformat;
import static ucar.nc2.time.CalendarDate.parseUdunits;

public abstract class NetcdfFileManager {

    protected static Logger logger = Logger.getLogger(NetcdfFileManager.class);

    private List<VariableInfo> nonElementCoordVarInfo = new ArrayList<>();
    private List<VariableInfo> dataVarInfo = new ArrayList<>();
    private Map<String, List<VariableInfo>> elementCoordVarInfo = new HashMap<String, List<VariableInfo>>();

    private List<String> coordVarNames = new ArrayList<>();

    private List<String> timeVarTypes = new ArrayList<>();
    private Map<Integer, Array> arrayData;
    Map<Integer, List<String>> stringData;
    String featureId;
    final String myDsgType;
    final String featureVarName;

    NetcdfFileWriter ncf;

    private Array timeCoordVarArr;
    private String timeCoordVarName;
    private Array verticalCoordVarArr;
    private String verticalCoordVarName;
    private Array timeCoordVarDetailArr; // potentially for profile datasets
    private String timeCoordVarDetailName; // potentially for profile datasets

    Dimension elementDimension;

    List<String> coordAttrValues = new ArrayList<>();
    List<String> coordVarTypes = new ArrayList<>();

    private boolean hasNetcdf4 = false;
    private String timeUnits = "seconds since 1970-01-01T00:00:00";
    private static String colIdAttrName = "_Rosetta_columnId";

    abstract void makeNonElementCoordVars(VariableInfo variableInfo);

    abstract void createNonElementCoordVars(Template template);

    abstract void makeOtherVariables();

    public NetcdfFileManager(String myDsgType) {
        this.myDsgType = myDsgType;
        this.featureVarName = myDsgType;
    }

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
        if (getMyDsgType().equalsIgnoreCase(reqType)) {
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
        dsgWriters.add(new SingleProfile());
        dsgWriters.add(new SingleTimeSeries());

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
        // todo - allow unit to be ISO, parse accordingly
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
                        dataVarInfo.add(varInfo);
                    } else {
                        // some kind of coordinate variable
                        String coordvarType = VariableInfoUtils.getCoordVarType(varInfo);
                        // time and vertical coordinates could be an element coordinate variable, so
                        // handle special
                        if (VariableInfoUtils.isTimeCoordVar(varInfo)) {
                            elementCoordVarInfo.computeIfAbsent("time", k -> new ArrayList<>()).add(varInfo);
                            timeVarTypes.add(VariableInfoUtils.getCoordVarType(varInfo));
                        } else if (VariableInfoUtils.getCoordVarType(varInfo).equalsIgnoreCase(VariableInfoUtils.vertical)) {
                            if (myDsgType.equalsIgnoreCase("profile")) {
                                elementCoordVarInfo.computeIfAbsent("z", k -> new ArrayList<>()).add(varInfo);
                            } else {
                                nonElementCoordVarInfo.add(varInfo);
                                coordVarTypes.add(VariableInfoUtils.getCoordVarType(varInfo));
                            }
                        } else {
                            nonElementCoordVarInfo.add(varInfo);
                            coordVarTypes.add(VariableInfoUtils.getCoordVarType(varInfo));
                        }
                    }
                }
            }
        }
    }

    private void createTimeVarInfoFromGlobalAttr(Template template) {
        List<RosettaGlobalAttribute> rosettaGlobalAttributes = template.getGlobalMetadata();
        Attribute timeCoverageStart = null;
        for (RosettaGlobalAttribute rga : rosettaGlobalAttributes) {
            if (rga.getName().equalsIgnoreCase("time_coverage_start")) {
                timeCoverageStart = RosettaGlobalAttributeUtils.getAttributeFromGlobalAttr(rga);
            }
        }

        if (timeCoverageStart != null) {
            VariableInfo tvi = new VariableInfo();
            String name = TemplateUtils.findUniqueName("time", template);
            tvi.setName(name);
            tvi.setColumnId(-2);
            // make rosetta control metadata
            List<RosettaAttribute> rosettaControlMetadata = new ArrayList<>();
            rosettaControlMetadata.add(new RosettaAttribute("type", "STRING", "STRING"));
            rosettaControlMetadata.add(new RosettaAttribute("coordinateVariable", "true", "BOOLEAN"));
            rosettaControlMetadata.add(new RosettaAttribute("coordinateVariableType", "fullDateTime", "STRING"));
            rosettaControlMetadata.add(new RosettaAttribute("globalAttributeName", "time_coverage_start", "STRING"));
            tvi.setRosettaControlMetadata(rosettaControlMetadata);

            // make rosetta control metadata
            List<RosettaAttribute> rosettaVariableMetadata = new ArrayList<>();
            rosettaVariableMetadata.add(new RosettaAttribute("units", "ISO", "STRING"));
            tvi.setVariableMetadata(rosettaVariableMetadata);

            template.getVariableInfoList().add(tvi);

            makeTimeVarFromFullDateTime(template, tvi);
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
        try {
            timeCoordVarArr = arrayData.get(relativeTimeVi.getColumnId());
            int numTimeObs = toIntExact(timeCoordVarArr.getSize());
            // only create element dimension if time is the element dimension
            if (myDsgType.equalsIgnoreCase("trajectory") | myDsgType.equalsIgnoreCase("timeseries")) {
                elementDimension = ncf.addDimension(timeDimName, toIntExact(numTimeObs));
            }
            timeCoordVarName = relativeTimeVi.getName();
            DataType dataType = VariableInfoUtils.getDataType(relativeTimeVi);
            RosettaAttribute timeUnitAttr = VariableInfoUtils.findAttributeByName("units", relativeTimeVi);
            timeUnits = timeUnitAttr.getValue();

            Variable timeVar = ncf.addVariable(group, timeCoordVarName, dataType, Collections.singletonList(elementDimension));
            List<Attribute> timeVarAttrs = getBaseTimeVarAttrs();
            timeVarAttrs.addAll(getMaxMinAttrs(timeCoordVarArr));
            timeVar.addAll(timeVarAttrs);
            // not to be used as part of coordinate attribute for profile data
            if (myDsgType != "profile") {
                coordAttrValues.add(timeCoordVarName);
            }
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

        try {
            List<String> fullDateTime = new ArrayList<>();
            DataType dataType = DataType.LONG;
            if (fullDateTimeVi.getColumnId() > 0) {
                fullDateTime = stringData.get(fullDateTimeVi.getColumnId());
                int numTimeObs = toIntExact(fullDateTime.size());
                String dateTimeFormat = VariableInfoUtils.getUnit(fullDateTimeVi);

                if (hasNetcdf4) {
                    timeCoordVarArr = createLongTimeDataFromFullDateTime(fullDateTime, dateTimeFormat);
                } else {
                    timeCoordVarArr = createIntTimeDataFromFullDateTime(fullDateTime, dateTimeFormat);
                }

                // only create element dimension if time is the element dimension
                if (myDsgType.equalsIgnoreCase("trajectory") | myDsgType.equalsIgnoreCase("timeseries")) {
                    elementDimension = ncf.addDimension(dimName, toIntExact(numTimeObs));
                }

                // in this case, we are creating a totally new variable, so we need to check
                // if the variable "time" already exists in data and coordinate variables
                timeCoordVarName = dimName;
            } else {
                // extract scalar value out of global attribute
                for (RosettaAttribute ra : fullDateTimeVi.getRosettaControlMetadata()) {
                    // if not netCDF4, use INT instead of LONG
                    if (!hasNetcdf4) {
                        dataType = DataType.INT;
                    }

                    String name = ra.getName();
                    String attrDate = "";
                    if (name.equalsIgnoreCase("globalattributename")) {
                        String attrName = ra.getValue();
                        // find attribute in template
                        Map<String, ArrayList<Attribute>> gam = TemplateUtils.getGlobalAttrsMap(template);
                        for (String k : gam.keySet()) {
                            ArrayList<Attribute> attrList = gam.get(k);
                            for (Attribute attr : attrList) {
                                if (attr.getFullName().equalsIgnoreCase(attrName)) {
                                    attrDate = attr.getStringValue();
                                }
                            }
                        }
                        Array scalar = null;
                        scalar = new ArrayInt.D0(false);
                        CalendarDate cd = parseISOformat(null, attrDate);
                        scalar.setFloat(0, cd.getMillis() / 1000L);
                        timeCoordVarArr = scalar;
                        timeCoordVarName = dimName;
                    }
                }
            }

            Variable timeVar = null;
            if (fullDateTimeVi.getColumnId() > 0) {
                timeVar = ncf.addVariable(group, timeCoordVarName, dataType, Collections.singletonList(elementDimension));
            } else {
                // created from globalAttr - scalar
                timeVar = ncf.addVariable(group, timeCoordVarName, dataType, "");
            }
            List<Attribute> timeVarAttrs = getBaseTimeVarAttrs();
            timeVarAttrs.addAll(getMaxMinAttrs(timeCoordVarArr));
            timeVar.addAll(timeVarAttrs);
            // generally not to be used as part of coordinate attribute for profile data
            // if profile, then check if this is created from global attribute
            if ((myDsgType != "profile") | (fullDateTimeVi.getColumnId() == -2)) {
                coordAttrValues.add(timeCoordVarName);
            }
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

        List<String> dateTimeVals = stringData.get(dateOnly.getColumnId());
        int numTimeObs = toIntExact(dateTimeVals.size());
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

        if (myDsgType != "profile") {
            if (hasNetcdf4) {
                timeCoordVarArr = createLongTimeDataFromFullDateTime(dateTimeVals, dateFormat);
            } else {
                timeCoordVarArr = createIntTimeDataFromFullDateTime(dateTimeVals, dateFormat);
            }
        } else {
            if (hasNetcdf4) {
                timeCoordVarDetailArr = createLongTimeDataFromFullDateTime(dateTimeVals, dateFormat);
            } else {
                timeCoordVarDetailArr = createIntTimeDataFromFullDateTime(dateTimeVals, dateFormat);
            }
        }

        try {
            String timeCoordVarNameLocal = "";
            // only create element dimension if time is the element dimension
            if (myDsgType.equalsIgnoreCase("trajectory") | myDsgType.equalsIgnoreCase("timeseries")) {
                elementDimension = ncf.addDimension(dimName, toIntExact(numTimeObs));
                // in this case, we are creating a totally new variabled, so we need to check
                // if the variable "time" already exists in data and coordinate variables
                timeCoordVarNameLocal = dimName;
                timeCoordVarName = dimName;
            } else {
                timeCoordVarNameLocal = timeCoordVarName + "_detail";
                timeCoordVarDetailName = timeCoordVarNameLocal;
            }


            Variable timeVar = ncf.addVariable(group, timeCoordVarNameLocal, dataType, Collections.singletonList(elementDimension));
            List<Attribute> timeVarAttrs = getBaseTimeVarAttrs();
            timeVarAttrs.addAll(getMaxMinAttrs(timeCoordVarArr));
            timeVar.addAll(timeVarAttrs);

            // not to be used as part of coordinate attribute for profile data
            if (myDsgType != "profile") {
                coordAttrValues.add(timeCoordVarName);
            }

            success = true;
        } catch (ArithmeticException ae) {
            success = false;
        }

        return success;
    }

    /**
     * Create the potential element coordinate variable for time
     *
     * @param template - rosetta template object
     * @return timeVarHandled - variable creation successful
     */
    private boolean createElementCoordVarTime(Template template) {
        List<VariableInfo> timeCoordVarInfo = elementCoordVarInfo.get("time");
        boolean timeVarHandled = false;
        if (timeCoordVarInfo.size() == 1) {
            if (timeVarTypes.contains(VariableInfoUtils.relativeTime)) {
                timeVarHandled = makeTimeVarFromRelativeTime(timeCoordVarInfo.get(0));
            } else if (timeVarTypes.contains(VariableInfoUtils.fullDateTime)) {
                timeVarHandled = makeTimeVarFromFullDateTime(template, timeCoordVarInfo.get(0));
            } else if (timeVarTypes.contains(VariableInfoUtils.dateOnly)) {
                timeVarHandled = makeTimeVarFromDateTimeOnly(template, timeCoordVarInfo.get(0), null);
            } else {
                String msg = "Do not understand how to handle a single time of type \"\"" +
                        VariableInfoUtils.getCoordVarType(timeCoordVarInfo.get(0));
                logger.error(msg);
            }
        } else if (timeCoordVarInfo.size() == 2) {
            if ((timeVarTypes.contains(VariableInfoUtils.dateOnly)) && (timeVarTypes.contains(VariableInfoUtils.timeOnly))) {
                // newTimeVarData = makeRelativeTime(elementCoordVarInfo.get(dateOnly), elementCoordVarInfo.get(timeOnly));
                String coordVarType = VariableInfoUtils.getCoordVarType(timeCoordVarInfo.get(0));
                if (coordVarType.equalsIgnoreCase(VariableInfoUtils.dateOnly)) {
                    timeVarHandled = makeTimeVarFromDateTimeOnly(template, timeCoordVarInfo.get(0), timeCoordVarInfo.get(1));
                }
            } else {
                logger.error("Two time vars founds, but not dateOnly and timeOnly.");
            }
        } else {
            logger.error("there should only be two time related variables defined - this found " + elementCoordVarInfo.size());
        }

        return timeVarHandled;
    }

    /**
     * Create the potential element coordinate variable for the vertical dimension
     *
     * @return timeVarHandled - variable creation successful
     */
    private boolean createElementCoordVarVertical() {
        boolean vertVarHandled = false;
        Group group = null;
        String verticalDimName = "z";
        List<VariableInfo> vertCoordVarInfo = elementCoordVarInfo.get("z");
        try{
            if (!vertCoordVarInfo.isEmpty() && (vertCoordVarInfo.size() == 1)) {
                // should only be one vertical coordinate variable
                VariableInfo vertVarInfoSingle = vertCoordVarInfo.get(0);

                verticalCoordVarArr = arrayData.get(vertVarInfoSingle.getColumnId());
                int numVertObs = toIntExact(verticalCoordVarArr.getSize());
                elementDimension = ncf.addDimension(verticalDimName, toIntExact(numVertObs));
                verticalCoordVarName = vertVarInfoSingle.getName();
                DataType dataType = VariableInfoUtils.getDataType(vertVarInfoSingle);

                Variable verticalVar = ncf.addVariable(group, verticalCoordVarName, dataType, Collections.singletonList(elementDimension));

                // add all attributes from the variableInfo object
                List<Attribute> verticalVarAttrs = VariableInfoUtils.getAllVariableAttributes(vertVarInfoSingle);

                // add max/min
                verticalVarAttrs.addAll(getMaxMinAttrs(verticalCoordVarArr));

                verticalVar.addAll(verticalVarAttrs);

                // add axis attribute
                verticalVar.addAttribute(new Attribute("axis", "Z"));

                coordAttrValues.add(verticalCoordVarName);
                vertVarHandled = true;
            }
        } catch (ArithmeticException ae) {
            logger.error("Size of the dimension could not fit in an integer value");
        }

        return vertVarHandled;
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

        if (type.equalsIgnoreCase(VariableInfoUtils.longitude)) {
            calculatedCoordVarAttrs.add(new Attribute("axis", "X"));
        } else if (type.equalsIgnoreCase(VariableInfoUtils.latitude)) {
            calculatedCoordVarAttrs.add(new Attribute("axis", "Y"));
        } else if (type.equalsIgnoreCase(VariableInfoUtils.vertical)) {
            calculatedCoordVarAttrs.add(new Attribute("axis", "Z"));
            RosettaAttribute positive = VariableInfoUtils.findAttributeByName(VariableInfoUtils.positiveAttrName, variableInfo);
            if (positive != null) {
                calculatedCoordVarAttrs.add(new Attribute(positive.getName(), positive.getValue()));
            }
        }

        //CoordinateVariable	coverage_content_type
        // no good way to guess this - will need to come from the template

        int colId = variableInfo.getColumnId();
        calculatedCoordVarAttrs.add(new Attribute(colIdAttrName, variableInfo.getColumnId()));

        // only for coordinate variables defined in columnar data block (i.e. non-attribute based)
        if (colId > 0) {
            //CoordinateVariable	valid_min, valid_max*
            Array data = arrayData.get(variableInfo.getColumnId());
            calculatedCoordVarAttrs.addAll(getMaxMinAttrs(data));

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

        if (data.getDataType() != DataType.CHAR && data.getDataType() != DataType.STRING) {
            calculatedDataVarAttrs.addAll(getMaxMinAttrs(data));
        }

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
        int end = elementDimension.getLength();
        for (VariableInfo coordVar : nonElementCoordVarInfo) {
            //CoordinateVariable	axis
            String type = VariableInfoUtils.getCoordVarType(coordVar);
            Array data = arrayData.get(coordVar.getColumnId());

            if (type.equalsIgnoreCase(VariableInfoUtils.longitude)) {
                ncf.addGlobalAttribute(new Attribute("geospatial_lon_start", data.getDouble(0)));
                ncf.addGlobalAttribute(new Attribute("geospatial_lon_end", data.getDouble(end)));
            } else if (type.equalsIgnoreCase(VariableInfoUtils.latitude)) {
                ncf.addGlobalAttribute(new Attribute("geospatial_lat_start", data.getDouble(0)));
                ncf.addGlobalAttribute(new Attribute("geospatial_lat_end", data.getDouble(end)));
            }
        }

        String startUdUnit = String.join(" ", String.valueOf(timeCoordVarArr.getLong(0)), timeUnits);
        String stopUdUnit = String.join(" ", String.valueOf(timeCoordVarArr.getLong(end)), timeUnits);

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
     * Add new dimension for character data
     *
     * @param variableInfo variable's variableInfo object created from template
     * @param dimensionList list of dimensions to augment
     * @return new dimension list with new char length dimension
     */
    List<Dimension> augmentCharDimension(VariableInfo variableInfo, List<Dimension> dimensionList) {
        int maxLen = 1;
        for (String sd: stringData.get(variableInfo.getColumnId())) {
            int len = sd.length();
            maxLen = len > maxLen ? len : maxLen;
        }
        Dimension strDim = ncf.addDimension(variableInfo.getName() + "_len", maxLen);
        List<Dimension> newDimensionList = new ArrayList<Dimension>();
        newDimensionList.addAll(dimensionList);
        newDimensionList.add(strDim);

        return newDimensionList;
    }

    /**
     * Create a data variable
     * @param variableInfo Variable to create
     */
    void makeDataVars(VariableInfo variableInfo) {

        List<Dimension> coordVarDimensions = Collections.singletonList(elementDimension);

        Group group = null;

        String dataVarName = variableInfo.getName();
        DataType dataType = VariableInfoUtils.getDataType(variableInfo);

        Variable var;
        if (dataType == DataType.CHAR || dataType == DataType.STRING) {
            // make sure to add extra dimension for the max length of characters
            coordVarDimensions = augmentCharDimension(variableInfo, coordVarDimensions);
            var = ncf.addVariable(group, dataVarName, DataType.CHAR, coordVarDimensions);
        } else {
            var = ncf.addVariable(group, dataVarName, dataType, coordVarDimensions);
        }

        // add all attributes from the variableInfo object
        List<Attribute> allVarAttrs = VariableInfoUtils.getAllVariableAttributes(variableInfo);

        List<Attribute> computedAttrs = calculateDataVarAttrs(variableInfo);
        // add new computedAttrs too the allVarAttrs list
        allVarAttrs.addAll(computedAttrs);

        var.addAll(allVarAttrs);
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

        // before we do anything, we need to modify the template to add variables that are constructed
        // from global attributes. This way, they will be picked up as if they were defined in the
        // data block of the csv file
        createNonElementCoordVars(template);

        ncf = NetcdfFileWriter.createNew(netcdfFilePath, false);

        identifyVariables(template);

        // create Element Coordinate Variable
        boolean elementDimCreated = false;

        if (myDsgType.equalsIgnoreCase("trajectory") | myDsgType.equalsIgnoreCase("timeseries")) {
            elementDimCreated = createElementCoordVarTime(template);
        } else if (myDsgType.equalsIgnoreCase("profile")) {
            // create vertical coordinate coord var
            elementDimCreated = createElementCoordVarVertical();
            // create time coordvar from global attr
            createTimeVarInfoFromGlobalAttr(template);
            // create time variable (non coord var). Although it is not the element coordinate in this case, we
            // will use the same functions as if it were. Inside these functions, the element dimension will
            // not be created, as we will use the element dimension created during the createElementCoordVarVertical()
            // call
            boolean timeVarCreated = createElementCoordVarTime(template);
            // for profile, create time coord var from global attribute
            if (!timeVarCreated) {
                // stop conversion as a time variable was not created - indicate to use that they
                // need to check the log file
                logger.error("time coordinate variable not created. Stopping");
            }
        }

        if (!elementDimCreated) {
            // stop conversion as a time variable was not created - indicate to use that they
            // need to check the log file
            logger.error("element dimension not created. Stopping");
        }

        // add coordinate variable info
        for (VariableInfo coordVarInfo : nonElementCoordVarInfo) {
            makeNonElementCoordVars(coordVarInfo);
        }

        // add data variable info
        for (VariableInfo dataVarInfo : dataVarInfo) {
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
            ncf.write(timeCoordVarName, timeCoordVarArr);

            // if profile, detailed time might exist
            if (myDsgType == "profile") {
                // if profile, detailed time might exist
                if (timeCoordVarDetailName != null){
                    ncf.write(timeCoordVarDetailName, timeCoordVarDetailArr);
                }
                // write data for vertical coord variable
                ncf.write(verticalCoordVarName, verticalCoordVarArr);
            }

            // write data to featureId variable
            ncf.write(featureVarName, Array.makeFromJavaArray(featureId.toCharArray()));

            // write data to all netCDF variables with an attribute containing the column id
            for (Variable var : ncf.getNetcdfFile().getVariables()) {
                Attribute colIdAttr = var.findAttribute(colIdAttrName);
                if (colIdAttr != null) {
                    Number colIdNum = colIdAttr.getNumericValue();
                    int colId = colIdNum.intValue();
                    // defined in columnar data block
                    if (colId > 0) {
                        Array thisData = arrayData.get(colId);
                        if (thisData.getDataType() == DataType.CHAR) {
                            // CHAR arrays are backed by a list of strings in the ParsedData object
                            // so need to handle special when writing
                            ncf.writeStringData(var, Array.makeArray(DataType.STRING, stringData.get(colId)));
                        } else {
                            ncf.write(var, thisData);
                        }
                    } else {
                        // write data to variables extracted from global metadata
                        for (VariableInfo vi : nonElementCoordVarInfo) {
                            if (var.getFullNameEscaped().contains(vi.getName())) {
                                for (RosettaAttribute ra : vi.getRosettaControlMetadata()) {
                                    String name = ra.getName();
                                    if (name.equalsIgnoreCase("globalattributename")) {
                                        Attribute ga = ncf.findGlobalAttribute(ra.getValue());
                                        DataType dt = ga.getDataType();
                                        Number val = ga.getNumericValue();
                                        Array scalar = null;

                                        if (dt == DataType.FLOAT) {
                                            scalar = new ArrayFloat.D0();
                                            scalar.setFloat(0, val.floatValue());
                                        } else if (dt == DataType.DOUBLE) {
                                            scalar = new ArrayDouble.D0();
                                            scalar.setDouble(0, val.doubleValue());
                                        } else if ((val != null) && (dt == DataType.STRING)) {
                                            // ok, we were able to get a non-null Number out of the attribute, but
                                            // the datatype on the attribute is string - let's store it as a double
                                            scalar = new ArrayDouble.D0();
                                            scalar.setDouble(0, val.doubleValue());
                                        }

                                        if (scalar != null) {
                                            ncf.write(var, scalar);
                                        } else {
                                            logger.error("failed to write scalar value to var " + var.getFullNameEscaped());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (InvalidRangeException e) {
            logger.error(e.getMessage());
            logger.error(e.getStackTrace());
        }

        ncf.close();

        return netcdfFilePath;
    }

}
