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
        log.error( "*** Reading NCML\n");
        try {
            NetcdfDataset ncd = NcMLReader.readNcML("file://"+ncmlFile, null);
            List<Attribute> globalAttributes = ncd.getGlobalAttributes();
            Iterator itr = globalAttributes.iterator();
            while(itr.hasNext()) {
                Attribute element = (Attribute) itr.next();
                log.error("  " + element.toString() + "\n");
            }
            log.error( "*** Writing skeleton netCDF file\n");
            NetcdfFile ncdnew = ucar.nc2.FileWriter.writeToFile(ncd, fileOut, true);
            ncd.close();
            ncdnew.close();
            log.error( "*** Done");

            log.error( "*** Open netCDF file and add coordinates attribute\n");
            NetcdfFileWriteable ncFileAddAttribute = NetcdfFileWriteable.openExisting(fileOut);
            ncFileAddAttribute.setRedefineMode(true);
            List<Variable> variables = ncFileAddAttribute.getVariables();
            // get time dim
            Iterator varIterator = variables.iterator();
            while(varIterator.hasNext()) {
                Variable tmpVar = (Variable) varIterator.next();
                String tmpVarName = tmpVar.getName();
                Attribute tmpAttr = tmpVar.findAttribute("_columnId");
                if ((tmpAttr != null) && (!tmpVarName.equals("time"))) {
                    ncFileAddAttribute.addVariableAttribute(tmpVarName, "coordinates", "time lat lon");
                }
            }
            // add lat/lon dimensions, varaibles, just in case not included in
            // in the ncml file (DEMO ONLY)
            /*
            Dimension latDim = ncfile_add_attr.addDimension("lat", 1);
            Dimension lonDim = ncfile_add_attr.addDimension("lon", 1);
            ArrayList dims = new ArrayList();
            dims.add(latDim);
            ncfile_add_attr.addVariable("lat", DataType.FLOAT, dims );

            ArrayList dims =ArrayList();
            dims.add(lonDim);
            ncfile_add_attr.addVariable("lon", DataType.FLOAT, dims );
            */

            ncFileAddAttribute.setRedefineMode(false);
            ncFileAddAttribute.close();

            // open netCDF file
            //NetcdfFileWriteable ncfile = NetcdfFileWriteable.openExisting(fileOut, true);
            log.error( "*** Open netCDF file to add 'special' variables\n");
            NetcdfFileWriteable ncfile = NetcdfFileWriteable.openExisting(fileOut);
            // add lat/lon dimensions, varaibles, just in case not included in
            // in the ncml file (DEMO ONLY)
            /*
            ArrayFloat.D0 dataLat = new ArrayFloat.D0();
            ArrayFloat.D0 dataLon = new ArrayFloat.D0();
            Float latVal = 69.2390F;
            Float lonVal = -51.0623F; 
            dataLat.set(latVal);
            dataLon.set(lonVal);
            ncfile.write("lat", dataLat);
            ncfile.write("lon", dataLon);
            // END DEMO SPECIFIC CODE
            */            
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
                    log.error("===============>>" + attr.toString());
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
                    log.error("vals: " + vals.toString());
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
