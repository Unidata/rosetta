/*
 * Copyright 1997-2012 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package edu.ucar.unidata.pzhta;


import org.grlea.log.SimpleLogger;

import ucar.ma2.*;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.NcMLReader;

import java.io.File;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: sarms
 */
public class Pzhta {

    /** _more_ */
    private static final SimpleLogger log = new SimpleLogger(Pzhta.class);

    /**
     * _more_
     *
     * @param ncmlFile _more_
     * @param fileOut _more_
     * @param outerList _more_
     *
     * @return _more_
     */
    public boolean convert(String ncmlFile, String fileOut,
                           ArrayList outerList) {

        try {
            log.error("*** Reading NCML\n");
            NetcdfDataset ncd = NcMLReader.readNcML("file://" + ncmlFile,
                                    null);
            NetcdfFile ncdnew = ucar.nc2.FileWriter.writeToFile(ncd, fileOut,
                                    true);
            ncd.close();
            ncdnew.close();
            log.error("*** Done");

            // open netCDF file
            //log.error( "*** Open netCDF file to add 'special' variables\n");
            NetcdfFileWriteable ncfile =
                NetcdfFileWriteable.openExisting(fileOut);
            List<Variable> ncFileVariables = ncfile.getVariables();
            // get time dim
            Dimension timeDim       = ncfile.findDimension("time");
            Iterator  ncVarIterator = ncFileVariables.iterator();
            while (ncVarIterator.hasNext()) {
                Variable  theVar  = (Variable) ncVarIterator.next();
                String    varName = theVar.getName();
                Attribute attr    = theVar.findAttribute("_columnId");
                DataType  dt      = theVar.getDataType();
                log.error("*** Look for _columnID in variable " + varName
                          + "\n");
                if (attr != null) {
                    int varIndex = Integer.parseInt(attr.getStringValue());
                    int len      = outerList.size();
                    log.error("\n");
                    log.error("Read " + varName + "\n");
                    if (dt.equals(DataType.FLOAT)) {
                        ArrayFloat.D1 vals =
                            new ArrayFloat.D1(outerList.size());
                        Iterator outerListIterator = outerList.iterator();
                        int      i                 = 0;
                        while (outerListIterator.hasNext()) {
                            List<String> innerList =
                                (List<String>) outerListIterator.next();
                            float f = new Float(
                                          (String) innerList.get(
                                              varIndex)).floatValue();
                            vals.set(i, f);
                            i++;
                        }
                        ncfile.write(varName, vals);
                    } else if (dt.equals(DataType.INT)) {
                        ArrayInt.D1 vals = new ArrayInt.D1(outerList.size());
                        Iterator    outerListIterator = outerList.iterator();
                        int         i                 = 0;
                        while (outerListIterator.hasNext()) {
                            List<String> innerList =
                                (List<String>) outerListIterator.next();
                            int f = new Integer(
                                        (String) innerList.get(
                                            varIndex)).intValue();
                            vals.set(i, f);
                            i++;
                        }
                        ncfile.write(varName, vals);
                    } else if (dt.equals(DataType.CHAR)) {
                        // toDo needs work, because of "FillValue" string being written to file first.
                        int elementLength =
                            ((ArrayList) outerList.get(0)).get(
                                0).toString().toCharArray().length;
                        ArrayChar.D2 vals =
                            new ArrayChar.D2(outerList.size(), elementLength);
                        Iterator outerListIterator = outerList.iterator();
                        int      i                 = 0;
                        while (outerListIterator.hasNext()) {
                            List<String> innerList =
                                (List<String>) outerListIterator.next();
                            String f = new String(innerList.get(varIndex));
                            char[] thisChar = f.toCharArray();
                            for (int j = 0; j < elementLength; j++) {
                                vals.set(i, j, thisChar[j]);
                            }
                            i++;
                        }
                        ncfile.write(varName, vals);
                    } else {
                        log.error("Unhandled DataType " + dt.toString()
                                  + "\n");
                    }
                    log.error("Write " + varName + "\n");
                }
            }
            ncfile.close();

            File file = new File(fileOut);

            if (file.exists()) {
                log.error("I'm here!!!");
                return true;
            } else {
                log.error("Error!  NetCDF file " + fileOut
                          + "was not created.");
                return false;
            }
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            return false;
        } catch (InvalidRangeException e) {
            log.error("InvalidRangeException: " + e.getMessage());
            return false;
        }

    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        String ncmlFile =
            "/Users/lesserwhirls/dev/unidata/pzhta/pzhta/src/edu/ucar/unidata/pzhta/test/test.ncml";
        String fileOutName =
            "/Users/lesserwhirls/dev/unidata/pzhta/pzhta/src/edu/ucar/unidata/pzhta/test/pzhta_test.nc";
        Pzhta                   pz        = new Pzhta();
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
