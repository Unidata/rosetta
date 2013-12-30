/*
 * Copyright 1998-2013 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package edu.ucar.unidata.rosetta;


import ucar.ma2.*;

import ucar.nc2.*;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.NcMLReader;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateFormatter;

import java.io.File;

import java.io.IOException;

import java.util.*;

public class Rosetta {

    /**
     * Holds information needed to construct dateTime objects
     */
    private class DateTimeBluePrint {

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
        private NetcdfFileWriter ncfw = null;
        private FileWriter2 fileWriter2 = null;
        private Variable dateOnlyVar = null;
        private Variable timeOnlyVar = null;
        private Map<String, Dimension> dateTimeDims = new HashMap<>();

        public DateTimeBluePrint() {}

        public DateTimeBluePrint(HashMap<String, ArrayList<String>> timeRelatedVars, NetcdfFile ncDataFileIn) throws IOException {
            init(timeRelatedVars, ncDataFileIn);
        }

        public Boolean isEmpty() {
            return isEmpty;
        }

        public Map<String, Dimension> getDateTimeDims() {
            return dateTimeDims;
        }

        public FileWriter2 getFw2() {
            return this.fileWriter2;
        }

        public Boolean hasSingleDateAndTime() {
            return hasSingleDateAndTime;
        }


        protected Boolean getFullDateTime() {
            return fullDateTime;
        }

        private Variable getDateTimeVarStr() {
            return dateTimeVarStr;
        }

        private long[] getDateTimeArray() {
            return dateTimeArray;
        }

        private Dimension getIsoStrLen() {
            return isoStrLen;
        }

        private String[] getDateTimeIsoString() {
            return dateTimeIsoString;
        }

        private int getNumObs() {
            return numObs;
        }

        private long getNumTimeObs() {
            return numTimeObs;
        }

        private long getNumDateObs() {
            return numDateObs;
        }

        private Set<String> getTimeVarTypes() {
            return timeVarTypes;
        }

        private Boolean getTimeOnly() {
            return timeOnly;
        }

        private Boolean getDateOnly() {
            return dateOnly;
        }

        public void initDateAndTimeArrays(NetcdfFile ncDataFileIn, HashMap<String, ArrayList<String>> timeRelatedVars) throws IOException {
            ArrayList<String> timeOnlyVarNames = timeRelatedVars.get("timeOnly");
            ArrayList<String> dateOnlyVarNames = timeRelatedVars.get("dateOnly");
            timeOnlyVar = ncDataFileIn.findVariable(timeOnlyVarNames.get(0));
            dateOnlyVar = ncDataFileIn.findVariable(dateOnlyVarNames.get(0));
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

        private void writeNewVariables(FileWriter2 fw2) throws IOException {

            ArrayChar timeDataArray = (ArrayChar) timeOnlyVar.read();
            ArrayChar dateDataArray = (ArrayChar) dateOnlyVar.read();
            String dateFmt = dateOnlyVar.getUnitsString();
            String timeFmt = timeOnlyVar.getUnitsString();
            CalendarDateFormatter fmt = new CalendarDateFormatter(dateFmt+timeFmt);
            String date, time, dateTimeStr;
            CalendarDate dateTime;
            if (numTimeObs == numDateObs) {
                dateTimeVarStr = new Variable(fw2.getNetcdfFileWriter().getNetcdfFile(), null, null, "dateTimeStr");
                dateTimeVarStr.setDataType(DataType.CHAR);
                dateTimeVarStr.setDimension(0, dateTimeDims.get("dateTime2"));
                dateTimeVarStr.setDimension(0, dateTimeDims.get("isoStrLen"));
                dateTimeVarStr.addAttribute(new Attribute("format", "yyyy-MM-ddTHH:mm:ss.SSSZ"));
                dateTimeVarStr.addAttribute(new Attribute("comment", "ISO8601 format; Created by Rosetta."));
                assert dateTimeVarStr.getRank() == 2;
                fw2.addVariable(dateTimeVar);



                dateTimeVar = new Variable(fw2.getNetcdfFileWriter().getNetcdfFile(), null, null, "dateTime");
                dateTimeVar.setDataType(DataType.INT);
                dateTimeVar.setDimension(0,dateTimeDims.get("dateTime2"));
                dateTimeVar.addAttribute(new Attribute("units", "seconds since 1970-01-01T00:00:00Z"));
                dateTimeVar.addAttribute(new Attribute("comment", "Created by Rosetta"));
                fw2.addVariable(dateTimeVar);
            }
        }

        private void init(HashMap<String, ArrayList<String>> timeRelatedVars, NetcdfFile ncDataFileIn) throws IOException {
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
                        initDateAndTimeArrays(ncDataFileIn, timeRelatedVars);

                    }
                }
            }
            //this.fileWriter2 = fw2;
        }
    }




    /** Convert ASCII CSV file (simple, one station, one time per row) into netCDF
     * using metadata defined in the ncml file */

    private boolean checkLongToIntConversion(long longVal) {
        int intVal = (int) longVal;
        long intValToLong = (long) intVal;
        if (longVal != intValToLong) {
            throw new InputMismatchException("The long " + Long.toString(longVal) + " cannot be correctly convertoed to an int value. That is, you have to many observations in your file.");
        } else {
            return true;
        }
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

    private NetcdfFileWriter writeDataFromUsersFile(Variable theVar, List<List<String>> outerList, NetcdfFileWriter ncFileWriter) throws IOException, InvalidRangeException {
        Attribute attr    = theVar.findAttribute("_columnId");
        DataType  dt      = theVar.getDataType();
        if (attr != null) {
            int varIndex = Integer.parseInt(attr.getStringValue());
            int len      = outerList.size();
            if (dt.equals(DataType.FLOAT)) {
                ArrayFloat.D1 vals =
                        new ArrayFloat.D1(outerList.size());
                int      i                 = 0;
                for (List<String> innerList : outerList) {
                    float f = Float.parseFloat(
                            innerList.get(
                                    varIndex));
                    vals.set(i, f);
                    i++;
                }
                ncFileWriter.write(theVar, vals);
            } else if (dt.equals(DataType.INT)) {
                ArrayInt.D1 vals =
                        new ArrayInt.D1(outerList.size());
                int      i                 = 0;
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
                int      i                 = 0;
                for (List<String> innerList : outerList) {

                    String f = innerList.get(varIndex);
                    vals.setString(i,f);
                    i++;
                }
                ncFileWriter.write(theVar, vals);
            }
        }
        return ncFileWriter;
    }

    private NetcdfFileWriter writeRosettaConstructedTimeData(DateTimeBluePrint dateTimeBluePrint, NetcdfFileWriter ncFileWriter) throws IOException, InvalidRangeException {
        // are we constructing a new date/time variable from a single date and single time variable
        // as found in the users file?
        if (dateTimeBluePrint.hasSingleDateAndTime()) {
            // create data arrays
            int numObs = dateTimeBluePrint.getNumObs();
            assert dateTimeBluePrint.getDateTimeVarStr().getRank() == 2;
            ArrayChar.D2 strVals =
                    new ArrayChar.D2(numObs, dateTimeBluePrint.getIsoStrLen().getLength());
            int      i                 = 0;
            for (String dtStr : dateTimeBluePrint.getDateTimeIsoString()) {
                strVals.setString(i,dtStr);
                i++;
            }
            ncFileWriter.write(dateTimeBluePrint.getDateTimeVarStr(), strVals);

            // makeDateTime(ncfile, numObs, dateTimeArray)

            ArrayLong.D1 longVals =
                    new ArrayLong.D1(numObs);
            i                 = 0;
            for (long dateTimeLong : dateTimeBluePrint.getDateTimeArray()) {
                longVals.set(i, dateTimeLong);
                i++;
            }
        }
        return ncFileWriter;
    }

    private Boolean fileCreated(String fileOut) {
        Boolean fileCreated = false;
        File file = new File(fileOut);
        if (file.exists()) {
            fileCreated = true;
        }
        return fileCreated;
    }
    /**
     * Convert the list of list data, as obtained from the ascii file, into a netCDF file
     * using the metadata defined in the ncml template
     *
     * @param ncmlFile path to ncml template
     * @param fileOut path of netCDF output file
     * @param outerList data list-of-lists
     *
     * @return true, if successful, false if not
     */
    public boolean convert(String ncmlFile, String fileOut,
                           List<List<String>> outerList) {

        try {
            // TODO!!!!!!!

            /**
             *
             * So, the issue is that is seems as through you cannot directly modify a netcdf file once it is created.
             * In order to do so, you need to use, yes, NcML. I think what this means is that we will need to rewrite the NcmlFileManagerImpl to
             * not write out an NcML file, but rather write the actual netCDF file from the .json object from the frontend.
             *
             * This means that the "template" will indeed be the json object.
             *
             * We need to write an isop that can deal with the users file and the json object. Given these two things,
             * we should be able to have one Rosetta iosp,
             *
             * http://www.unidata.ucar.edu/software/thredds/v4.4/netcdf-java/tutorial/IOSPoverview4.html
             * http://www.unidata.ucar.edu/software/thredds/v4.4/netcdf-java/tutorial/IOSPexample1.html
             *
             * the netcdf file from which many types of user input files can be made into a
             * NetcdfFileWriter.
             *
             */
            // First, read in the ncml file and write out to a netCDF file
            NetcdfDataset ncmlDataset = NcMLReader.readNcML("file://" + ncmlFile,
                                    null);



            // check if we need to construct new time related variables by joining incomplete date / time
            // variables from the users data file.
            FileWriter2 ncFileFromNcml = new ucar.nc2.FileWriter2(ncmlDataset, fileOut, NetcdfFileWriter.Version.netcdf3, null);

            NetcdfFile ncout = ncFileFromNcml.write();
            ncmlDataset.close();
            ncout.close();

            // reopen netcdf file, write data from users file
            NetcdfFileWriter ncFileWriter =
                    NetcdfFileWriter.openExisting(fileOut);

            NetcdfFile ncfile = ncFileWriter.getNetcdfFile();

            // get variables and based on what is in the netcdf file
            // generated from the rosetta written NcML
            List<Variable> ncFileVariables = ncfile.getVariables();

            for (Variable ncvar : ncFileVariables) {
                ncFileWriter = writeDataFromUsersFile(ncvar, outerList, ncFileWriter);
            }

            ncfile.close();


            // write the the new time related dimensions
            
            // file all of the time related variables
            HashMap<String, ArrayList<String>> timeRelatedVars = extractTimeRelatedVars(ncFileVariables);
            // make blueprint that indicates how the new dateTime variable will be constructed.
            NetcdfFile ncfWithoutTime = NetcdfFile.open(fileOut);
            DateTimeBluePrint dateTimeBluePrint = new DateTimeBluePrint(timeRelatedVars, ncfWithoutTime);
            
            // open new netcdf file, based on the file used to create the blueprint, add
            // undefined dimensions
            
            
            // write new time related variables
            
            // open netcdf dataset that contains all of the data from the users files
            Map<String,Dimension> dimsToCheck = dateTimeBluePrint.getDateTimeDims();
            Set<String> dimsToCheckNames = dimsToCheck.keySet();

            NetcdfFile ncfWithTimeDims = NetcdfFile.open(fileOut);
            FileWriter2 ncwWithTimeDims = new FileWriter2(ncfWithTimeDims, fileOut, NetcdfFileWriter.Version.netcdf3, null);

            List<Dimension> existingDims = ncfWithTimeDims.getDimensions();
            for(String dimName : dimsToCheckNames) {
                Boolean replace = !existingDims.contains(ncfWithTimeDims.findDimension(dimName));
                if (replace != null) {
                    if (replace) {
                        Dimension d = ncfWithoutTime.addDimension(null,dimsToCheck.get(dimName));
                        if (d == null) {
                            System.out.println("hmm?");
                        }
                    }
                }
            }

            ncwWithTimeDims = new FileWriter2(ncfWithTimeDims, fileOut, NetcdfFileWriter.Version.netcdf3, null);
            ncwWithTimeDims.write();
            ncfWithTimeDims.close();

            // create new FileWriter2 based on the netcdf file that has all of the users data
            FileWriter2 fw2WithConstructedTime = new ucar.nc2.FileWriter2(ncfWithoutTime, fileOut, NetcdfFileWriter.Version.netcdf3, null);

            // make blueprint that indicates how the new dateTime variable will be constructed.
             //dateTimeBluePrint = new DateTimeBluePrint(timeRelatedVars, ncfWithoutTime, fw2WithConstructedTime);
            if (!dateTimeBluePrint.isEmpty) {
                //if the blueprint is not empty, get the file writer associated w
                fw2WithConstructedTime = dateTimeBluePrint.getFw2();
                NetcdfFile ncout2 = fw2WithConstructedTime.write();
                ncout2.close();
            }
            ncfWithoutTime.close();

            // finally, write the new date/time data to the netcdf file
            // write the new time related data

            if (!dateTimeBluePrint.isEmpty) {
                NetcdfFileWriter ncFileWriterFinal =
                        NetcdfFileWriter.openExisting(fileOut);
                // writeRosettaConstructedTimeData(DateTimeBluePrint dateTimeBluePrint, NetcdfFileWriter ncFileWriter
                // are we constructing a new date/time variable from a single date and single time variable
                // as found in the users file?
                ncFileWriterFinal = writeRosettaConstructedTimeData(dateTimeBluePrint, ncFileWriterFinal);
                ncfile = ncFileWriterFinal.getNetcdfFile();
                ncFileWriterFinal.close();
            }

            return fileCreated(ncfile.getLocation());
        } catch (IOException e) {
            //log.error("IOException: " + e.getMessage());
            return false;
        } catch (InvalidRangeException e) {
            //log.error("InvalidRangeException: " + e.getMessage());
            return false;
        }

    }



    public static void main2(String[] args) {
        String ncmlFile =
            "/Users/lesserwhirls/dev/unidata/rosetta/rosetta/src/edu/ucar/unidata/rosetta/test/test.ncml";
        String fileOutName =
            "/Users/lesserwhirls/dev/unidata/rosetta/rosetta/src/edu/ucar/unidata/rosetta/test/rosetta_test.nc";
        Rosetta pz        = new Rosetta();
        ArrayList<List<String>> outerList = new ArrayList<List<String>>();
        for (int j = 0; j < 10; j++) {
            ArrayList<String> innerList = new ArrayList<String>();
            for (int i = 0; i < 11; i++) {
                innerList.add(Integer.toString((i + j) * i));
            }
            outerList.add(innerList);
        }
        pz.convert(ncmlFile, fileOutName, outerList);
    }
}
