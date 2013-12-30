package edu.ucar.unidata.rosetta;

import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayLong;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateFormatter;

import java.io.IOException;
import java.util.*;

/**
 * Holds information needed to construct dateTime objects
 */

public class DateTimeBluePrint {

    private Variable dateTimeVarStr = null;
    private Variable dateTimeVar = null;
    private long dateTimeArray[] = null;
    private Dimension isoStrLen = null;
    private String dateTimeIsoString[];
    private int numObs = -1;
    private long numTimeObs = -2;
    private long numDateObs = -3;
    private Set<String> timeVarTypes;
    private Boolean timeOnly = null;
    private Boolean dateOnly = null;
    private Boolean fullDateTime = null;
    private Boolean isEmpty = null;
    private Boolean hasSingleDateAndTime = null;
    private Variable dateOnlyVar = null;
    private Variable timeOnlyVar = null;
    private Map<String, Dimension> dateTimeDims = new HashMap<>();

    public DateTimeBluePrint() {}

    public DateTimeBluePrint(HashMap<String, ArrayList<String>> timeRelatedVars, NetcdfFileWriter ncFileWriter) throws IOException {
        init(timeRelatedVars, ncFileWriter);
    }

    public Boolean isEmpty() {
        return isEmpty;
    }

    private HashMap<String, ArrayList<String>> extractTimeRelatedVars(List<Variable> ncFileVariables) {
        HashMap<String, ArrayList<String>> timeRelatedVars = new HashMap<String, ArrayList<String>>();
        Iterator  ncVarIterator = ncFileVariables.iterator();
        String timeType;
        String varName;
        while (ncVarIterator.hasNext()) {
            Variable  theVar  = (Variable) ncVarIterator.next();
            Attribute triggerAttr = theVar.findAttributeIgnoreCase("_coordinateVariableType");
            if (triggerAttr != null) {
                timeType = theVar.findAttributeIgnoreCase("_coordinateVariableType").getStringValue(0);
                varName = theVar.getFullName();
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


    public void initDateAndTimeArrays(NetcdfFileWriter ncFileWriter, HashMap<String, ArrayList<String>> timeRelatedVars) throws IOException {
        ArrayList<String> timeOnlyVarNames = timeRelatedVars.get("timeOnly");
        ArrayList<String> dateOnlyVarNames = timeRelatedVars.get("dateOnly");
        timeOnlyVar = ncFileWriter.findVariable(timeOnlyVarNames.get(0));
        dateOnlyVar = ncFileWriter.findVariable(dateOnlyVarNames.get(0));
        numTimeObs = timeOnlyVar.getShape(0);
        numDateObs = dateOnlyVar.getShape(0);
        ArrayChar timeDataArray = (ArrayChar) timeOnlyVar.read();
        ArrayChar dateDataArray = (ArrayChar) dateOnlyVar.read();
        String dateFmt = dateOnlyVar.getUnitsString();
        String timeFmt = timeOnlyVar.getUnitsString();
        CalendarDateFormatter fmt = new CalendarDateFormatter(dateFmt+timeFmt);
        String date, time, dateTimeStr;
        CalendarDate dateTime;
        if (numTimeObs == numDateObs) {
            if (checkLongToIntConversion(numTimeObs)) {
                // make string array of iso date/times as well as long array of seconds since 1970-01-01.
                numObs = (int) numTimeObs;

                dateTimeArray = new long[numObs];
                dateTimeIsoString = new String[numObs];
                Dimension isoStrLenDim = null;
                for(int ob = 0; ob < (int) numDateObs; ob++) {
                    date = dateDataArray.getString(ob);
                    time = timeDataArray.getString(ob);
                    dateTimeStr = date + time;
                    dateTime = fmt.parse(dateTimeStr);
                    dateTimeArray[ob] = dateTime.getMillis() / 1000l; // make seconds instead of milliseconds
                    dateTimeIsoString[ob] = dateTime.toString();
                    if (ob == 0) {
                        String dimName = "isoStrLen";
                        dateTimeDims.put(dimName, new Dimension("isoStrLen", dateTime.toString().length()));
                    }
                }
                // create time dimension
                String dimName = "dateTime2";
                dateTimeDims.put(dimName, new Dimension("dateTime2", numObs));
            }
        }
    }

    private boolean checkLongToIntConversion(long longVal) {
        int intVal = (int) longVal;
        long intValToLong = (long) intVal;
        if (longVal != intValToLong) {
            throw new InputMismatchException("The long " + Long.toString(longVal) + " cannot be correctly convertoed to an int value. That is, you have to many observations in your file.");
        } else {
            return true;
        }
    }

    public NetcdfFileWriter createNewVariables(NetcdfFileWriter ncFileWriter) throws IOException {
        assert ncFileWriter.isDefineMode() == true;
        ArrayChar timeDataArray = (ArrayChar) timeOnlyVar.read();
        ArrayChar dateDataArray = (ArrayChar) dateOnlyVar.read();
        String dateFmt = dateOnlyVar.getUnitsString();
        String timeFmt = timeOnlyVar.getUnitsString();
        CalendarDateFormatter fmt = new CalendarDateFormatter(dateFmt+timeFmt);
        String date, time, dateTimeStr;
        CalendarDate dateTime;
        if (numTimeObs == numDateObs) {
            List<Dimension> dtDims = new ArrayList<>();
            dtDims.add(dateTimeDims.get("dateTime2"));
            dtDims.add(dateTimeDims.get("isoStrLen"));
            Variable theVar = ncFileWriter.addVariable(null, "dateTimeStr", DataType.CHAR, dtDims);
            ncFileWriter.addVariableAttribute(theVar, new Attribute("format", "yyyy-MM-ddTHH:mm:ss.SSSZ"));
            ncFileWriter.addVariableAttribute(theVar, new Attribute("comment", "ISO8601 format; Created by Rosetta."));


            List<Dimension> dtDims2 = new ArrayList<>();
            dtDims2.add(dateTimeDims.get("dateTime2"));
            Variable theVar2 = ncFileWriter.addVariable(null, "dateTime", DataType.INT, dtDims2);
            ncFileWriter.addVariableAttribute(theVar2, new Attribute("units", "seconds since 1970-01-01T00:00:00Z"));
            ncFileWriter.addVariableAttribute(theVar2, new Attribute("comment", "Created by Rosetta"));
        }
        return ncFileWriter;
    }

    public NetcdfFileWriter writeNewVariables(NetcdfFileWriter ncFileWriter) throws IOException, InvalidRangeException {
        assert ncFileWriter.isDefineMode() == false;
        if (hasSingleDateAndTime) {
            // create data arrays

            assert dateTimeVarStr.getRank() == 2;
            ArrayChar.D2 strVals =
                    new ArrayChar.D2(numObs, isoStrLen.getLength());
            int      i                 = 0;
            for (String dtStr : dateTimeIsoString) {
                strVals.setString(i,dtStr);
                i++;
            }
            ncFileWriter.write(dateTimeVarStr, strVals);

            // makeDateTime(ncfile, numObs, dateTimeArray)

            ArrayLong.D1 longVals =
                    new ArrayLong.D1(numObs);
            i                 = 0;
            for (long dateTimeLong : dateTimeArray) {
                longVals.set(i, dateTimeLong);
                i++;
            }
        }
        return ncFileWriter;

    }

    private void init(HashMap<String, ArrayList<String>> timeRelatedVars, NetcdfFileWriter ncFileWriter) throws IOException {
        isEmpty = false;
        hasSingleDateAndTime = false;

        if (timeRelatedVars.isEmpty()) {
            isEmpty = true;
        } else if (!timeRelatedVars.isEmpty()) {
            timeVarTypes = timeRelatedVars.keySet();
            timeOnly = timeVarTypes.contains("timeOnly");
            dateOnly = timeVarTypes.contains("dateOnly");
            fullDateTime = timeVarTypes.contains("fullDateTime");

            if (timeOnly && dateOnly) {
                ArrayList<String> timeOnlyVarNames = timeRelatedVars.get("timeOnly");
                ArrayList<String> dateOnlyVarNames = timeRelatedVars.get("dateOnly");
                int numTimeOnlyVars = timeOnlyVarNames.size();
                int numDateOnlyVars = dateOnlyVarNames.size();
                if ((numTimeOnlyVars == 1) && (numDateOnlyVars == 1)) {
                    initDateAndTimeArrays(ncFileWriter, timeRelatedVars);

                }
            }
        }
    }
}