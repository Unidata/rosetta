/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.custom.dsg.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Set;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayLong;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateFormatter;

/**
 * Holds information needed to construct dateTime objects
 */

public class DateTimeBluePrint {

  private String dateTimeVarStrName = "dateTimeStr";
  private String dateTimeVarName = "dateTime";
  private long dateTimeArray[] = null;
  private String dateTimeIsoString[];
  private int numObs = -1;
  private long numTimeObs = -2;
  private long numDateObs = -3;
  private long numFullDateTimeObs = -4;
  private Set<String> timeVarTypes;
  private Boolean timeOnly = false;
  private Boolean dateOnly = false;
  private Boolean fullDateTime = false;
  private Boolean isEmpty = false;
  private Boolean hasSingleDateAndTime = false;
  private Variable dateOnlyVar = null;
  private Variable timeOnlyVar = null;
  private Variable fullDateTimeVar = null;
  private HashMap<String, ArrayList<String>> timeRelatedVars;
  private String isoFmt = "yyyy-MM-ddTHH:mm:ss.SSSZ";

  public DateTimeBluePrint() {
  }

  public DateTimeBluePrint(HashMap<String, ArrayList<String>> timeRelatedVars,
      NetcdfFileWriter ncFileWriter) throws IOException {
    this.timeRelatedVars = timeRelatedVars;
    init(ncFileWriter);
  }

  public Boolean isEmpty() {
    return isEmpty;
  }

  public void initDateAndTimeArrays() throws IOException {
    if (timeOnly && dateOnly) {
      ArrayChar timeDataArray = (ArrayChar) timeOnlyVar.read();
      ArrayChar dateDataArray = (ArrayChar) dateOnlyVar.read();
      String dateFmt = dateOnlyVar.findAttributeIgnoreCase("format").getStringValue();
      String timeFmt = timeOnlyVar.findAttributeIgnoreCase("format").getStringValue();
      CalendarDateFormatter fmt = new CalendarDateFormatter(dateFmt + timeFmt);
      String date, time, dateTimeStr;
      CalendarDate dateTime;
      if ((numTimeObs == numDateObs) & (checkLongToIntConversion(numTimeObs))) {
        numObs = (int) numTimeObs;
        dateTimeArray = new long[numObs];
        dateTimeIsoString = new String[numObs];
        for (int ob = 0; ob < (int) numDateObs; ob++) {
          date = dateDataArray.getString(ob);
          time = timeDataArray.getString(ob);
          dateTimeStr = date + time;
          dateTime = fmt.parse(dateTimeStr);
          dateTimeArray[ob] = dateTime.getMillis() / 1000l; // make seconds instead of milliseconds
          dateTimeIsoString[ob] = dateTime.toString();
        }
      }
    } else if (fullDateTime) {
      ArrayChar fullDateTimeArray = (ArrayChar) fullDateTimeVar.read();
      String dateFmt = fullDateTimeVar.findAttributeIgnoreCase("format").toString();
      CalendarDateFormatter fmt = new CalendarDateFormatter(dateFmt);
      String dateTimeStr;
      CalendarDate dateTime;
      if (checkLongToIntConversion(numFullDateTimeObs)) {
        numObs = (int) numFullDateTimeObs;
        dateTimeArray = new long[numObs];
        dateTimeIsoString = new String[numObs];
        for (int ob = 0; ob < (int) numDateObs; ob++) {
          dateTimeStr = fullDateTimeArray.getString(ob);
          dateTime = fmt.parse(dateTimeStr);
          dateTimeArray[ob] = dateTime.getMillis() / 1000l; // make seconds instead of milliseconds
          dateTimeIsoString[ob] = dateTime.toString();
        }
      }
    }
  }

  private boolean checkLongToIntConversion(long longVal) {
    int intVal = (int) longVal;
    long intValToLong = (long) intVal;
    if (longVal != intValToLong) {
      throw new InputMismatchException("The long " + Long.toString(longVal)
          + " cannot be correctly convertoed to an int value. That is, you have to many observations in your file.");
    } else {
      return true;
    }
  }

  public NetcdfFileWriter createNewVars(NetcdfFileWriter ncFileWriter) throws IOException {
    // create new variables and dimensions
    if ((ncFileWriter.isDefineMode()) & (hasSingleDateAndTime)) {
      if (timeOnly && dateOnly) {
        ArrayList<String> timeOnlyVarNames = timeRelatedVars.get("timeOnly");
        ArrayList<String> dateOnlyVarNames = timeRelatedVars.get("dateOnly");
        timeOnlyVar = ncFileWriter.findVariable(timeOnlyVarNames.get(0));
        dateOnlyVar = ncFileWriter.findVariable(dateOnlyVarNames.get(0));
        numTimeObs = timeOnlyVar.getShape(0);
        numDateObs = dateOnlyVar.getShape(0);

        if ((numTimeObs == numDateObs) & checkLongToIntConversion(numObs)) {
          numObs = (int) numTimeObs;
        }
      } else if (fullDateTime) {
        ArrayList<String> fullDateTimeVarNames = timeRelatedVars.get("fullDateTime");
        fullDateTimeVar = ncFileWriter.findVariable(fullDateTimeVarNames.get(0));
        numFullDateTimeObs = fullDateTimeVar.getShape(0);
        if (checkLongToIntConversion(numFullDateTimeObs)) {
          numObs = (int) numFullDateTimeObs;
        }
      }

      if (numObs > 0) {
        List<Dimension> dtDims = new ArrayList<>();
        // todo is there a better way to get the dateTime dimension???
        Dimension dtDim = ncFileWriter.renameDimension(null, dateTimeVarName, dateTimeVarName);
        dtDims.add(dtDim);
        Variable theVar = ncFileWriter
            .addStringVariable(null, dateTimeVarStrName, dtDims, isoFmt.length());
        ncFileWriter.addVariableAttribute(theVar, new Attribute("format", isoFmt));
        ncFileWriter.addVariableAttribute(theVar,
            new Attribute("comment", "ISO8601 format; Created by Rosetta."));

        Variable theVar2 = ncFileWriter.addVariable(null, dateTimeVarName, DataType.INT, dtDims);
        ncFileWriter.addVariableAttribute(theVar2,
            new Attribute("units", "seconds since 1970-01-01T00:00:00Z"));
        ncFileWriter.addVariableAttribute(theVar2, new Attribute("comment", "Created by Rosetta"));
      }
    }

    return ncFileWriter;
  }

  public NetcdfFileWriter writeNewVariables(NetcdfFileWriter ncFileWriter)
      throws IOException, InvalidRangeException {
    if ((hasSingleDateAndTime) & (!ncFileWriter.isDefineMode())) {
      initDateAndTimeArrays();
      // create data arrays
      Variable dateTimeVarStr = ncFileWriter.findVariable(dateTimeVarStrName);
      assert dateTimeVarStr.getRank() == 2;
      ArrayChar.D2 strVals =
          new ArrayChar.D2(numObs, isoFmt.length());
      int i = 0;
      for (String dtStr : dateTimeIsoString) {
        strVals.setString(i, dtStr);
        i++;
      }
      ncFileWriter.write(dateTimeVarStr, strVals);

      // makeDateTime(ncfile, numObs, dateTimeArray)
      Variable dateTimeVar = ncFileWriter.findVariable(dateTimeVarName);

      ArrayLong.D1 longVals =
          new ArrayLong.D1(numObs);
      i = 0;
      for (long dateTimeLong : dateTimeArray) {
        longVals.set(i, dateTimeLong);
        i++;
      }
      ncFileWriter.write(dateTimeVar, longVals);
    }
    return ncFileWriter;

  }

  private void init(NetcdfFileWriter ncFileWriter) throws IOException {
    if (timeRelatedVars.isEmpty()) {
      isEmpty = true;
    } else if (!timeRelatedVars.isEmpty()) {
      timeVarTypes = timeRelatedVars.keySet();
      timeOnly = timeVarTypes.contains("timeOnly");
      dateOnly = timeVarTypes.contains("dateOnly");
      fullDateTime = timeVarTypes.contains("fullDateTime");
      int numTimeOnlyVars = 0;
      int numDateOnlyVars = 0;
      int numFullDateTime = 0;

      if (timeRelatedVars.containsKey("timeOnly")) {
        ArrayList<String> timeOnlyVarNames = timeRelatedVars.get("timeOnly");
        numTimeOnlyVars = timeOnlyVarNames.size();
      }
      if (timeRelatedVars.containsKey("dateOnly")) {
        ArrayList<String> dateOnlyVarNames = timeRelatedVars.get("dateOnly");
        numDateOnlyVars = dateOnlyVarNames.size();
      }
      if (timeRelatedVars.containsKey("fullDateTime")) {
        ArrayList<String> fullDateVarNames = timeRelatedVars.get("fullDateTime");
        numFullDateTime = fullDateVarNames.size();
      }
      // right now, this only works if there is only one date and one time variable
      // and if date and time are separated into two variables, so assert that this
      // is true (i.e. only works for single station time series)
      Boolean sepDateTime = ((timeOnly && dateOnly) && (numTimeOnlyVars == 1) && (numDateOnlyVars
          == 1));
      Boolean singleDateTime = (fullDateTime && numFullDateTime == 1);
      hasSingleDateAndTime = false;
      if (sepDateTime && singleDateTime) {
        hasSingleDateAndTime = false;
      } else if (sepDateTime) {
        hasSingleDateAndTime = true;
      } else if (singleDateTime) {
        hasSingleDateAndTime = true;
      }
    }
  }
}