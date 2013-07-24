/*
 * Copyright 1998-2011 University Corporation for Atmospheric Research/Unidata
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

import java.io.File;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Rosetta {

    /** Convert ASCII CSV file (simple, one station, one time per row) into netCDF
     * using metadata defined in the ncml file */


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
            NetcdfDataset ncd = NcMLReader.readNcML("file://" + ncmlFile,
                                    null);
            FileWriter2 ncdnew = new ucar.nc2.FileWriter2(ncd, fileOut, NetcdfFileWriter.Version.netcdf3, null);
            NetcdfFile ncout = ncdnew.write();
            ncd.close();
            ncout.close();

            NetcdfFileWriter ncFileWriter =
               NetcdfFileWriter.openExisting(fileOut);

            NetcdfFile ncfile = ncFileWriter.getNetcdfFile();
            List<Variable> ncFileVariables = ncfile.getVariables();
            // get time dim
            Dimension timeDim       = ncfile.findDimension("time");
            Iterator  ncVarIterator = ncFileVariables.iterator();
            while (ncVarIterator.hasNext()) {
                Variable  theVar  = (Variable) ncVarIterator.next();
                String    varName = theVar.getFullName();
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
            }
            ncfile.close();

            File file = new File(fileOut);

            if (file.exists()) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            //log.error("IOException: " + e.getMessage());
            return false;
        } catch (InvalidRangeException e) {
            //log.error("InvalidRangeException: " + e.getMessage());
            return false;
        }

    }


    public static void main(String[] args) {
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
