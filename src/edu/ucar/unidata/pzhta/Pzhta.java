package edu.ucar.unidata.pzhta;

import org.grlea.log.SimpleLogger;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.NcMLReader;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.DataType;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayChar;

import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: sarms
 */
public class Pzhta {

    private static final SimpleLogger log = new SimpleLogger(Pzhta.class);  

    public boolean convert(String ncmlFile, String fileOut, ArrayList outerList) {
        try {
            log.error( "*** Reading NCML\n");
            NetcdfDataset ncd = NcMLReader.readNcML("file://"+ncmlFile, null);
            NetcdfFile ncdnew = ucar.nc2.FileWriter.writeToFile(ncd, fileOut, true);
            ncd.close();
            ncdnew.close();
            log.error( "*** Done");

            // open netCDF file
            //log.error( "*** Open netCDF file to add 'special' variables\n");
            NetcdfFileWriteable ncfile = NetcdfFileWriteable.openExisting(fileOut);
            List<Variable> ncFileVariables = ncfile.getVariables();
            // get time dim
            Dimension timeDim = ncfile.findDimension("time");
            Iterator ncVarIterator = ncFileVariables.iterator();
            while(ncVarIterator.hasNext()) {
                Variable theVar = (Variable) ncVarIterator.next();
                String varName = theVar.getName();
                Attribute attr = theVar.findAttribute("_columnId");
                DataType dt = theVar.getDataType();
                log.error( "*** Look for _columnID in variable " + varName  + "\n");
                if (attr != null) {
                    int varIndex = Integer.parseInt(attr.getStringValue());
                    int len = outerList.size();
                    log.error("\n");
                    log.error("Read " + varName + "\n");
                    ArrayFloat.D1 vals = new ArrayFloat.D1(outerList.size());
                    Iterator outerListIterator = outerList.iterator();
                    int i = 0;                    
                    while(outerListIterator.hasNext()) {
                        List<String> innerList = (List<String>) outerListIterator.next();
                        float f = new Float((String) innerList.get(varIndex)).floatValue();
                        vals.set(i, f);
                        i++;
                    }
                    log.error("Write " + varName + "\n");
                    ncfile.write(varName, vals);
                }
            }
            ncfile.close();

            File file = new File(fileOut);

            if(file.exists()) { 
                log.error("I'm here!!!");
                return true;
            } else {
                log.error("Error!  NetCDF file " + fileOut + "was not created.");
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

/*
    public static void main(String[] args) {
        String ncmlFile =    "/Users/lesserwhirls/dev/unidata/pzhta/src/edu/ucar/unidata/pzhta/test/test.ncml";
        String fileOutName = "/Users/lesserwhirls/dev/unidata/pzhta/src/edu/ucar/unidata/pzhta/test/pzhta_test.nc";
        Pzhta pz = new Pzhta();
        ArrayList<List<String>> outerList = new ArrayList<List<String>>();
        for (int j=0; j<10; j++) {
            ArrayList<String> innerList = new ArrayList<String>();
            for (int i=0; i<11; i++) {
                innerList.add(Integer.toString((i+j)*i));
            }
            outerList.add(innerList);
        }
        pz.convert(ncmlFile, fileOutName, outerList);
    }

*/
}
