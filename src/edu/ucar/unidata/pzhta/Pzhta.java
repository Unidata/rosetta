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
        System.out.print( "*** Reading NCML\n");
        try{
            System.out.print("in pz.convert " + ncmlFile);
            NetcdfDataset ncd = NcMLReader.readNcML("file://"+ncmlFile, null);
            System.out.print("in pz.convert");
            List globalAttributes = ncd.getGlobalAttributes();
            System.out.print("Global Attributes in file:\n");
            Iterator itr = globalAttributes.iterator();
            while(itr.hasNext()) {
                Object element = itr.next();
                System.out.print("  " + element + "\n");
            }
            System.out.print("\n");
            System.out.print("Variables in file:\n");
            List vars = ncd.getVariables();
            for (int var = 0; var < vars.size(); var = var + 1){
                System.out.print("  " + vars.get(var)  + "\n");

            }
            System.out.print(" ");
            System.out.print( "*** Writing netCDF file");
            NetcdfFile ncdnew = ucar.nc2.FileWriter.writeToFile(ncd, fileOut, true);
            ncd.close();
            ncdnew.close();
            System.out.print( "*** Done");

            NetcdfFileWriteable ncfile_add_attr = NetcdfFileWriteable.openExisting(fileOut);
            ncfile_add_attr.setRedefineMode(true);
            vars = ncfile_add_attr.getVariables();
            // get time dim
            for (int var = 0; var < vars.size(); var = var + 1){
                Variable tmp_var = (Variable) vars.get(var);
                String varName = tmp_var.getName();
                Attribute attr = tmp_var.findAttribute("_columnId");
                //String thing = attr.getStringValue();
                if ((attr != null) && (!varName.equals("time"))) {
                    ncfile_add_attr.addVariableAttribute(varName, "coordinates", "time lat lon");
                }
            }
            ncfile_add_attr.setRedefineMode(false);
            ncfile_add_attr.close();

            // open netCDF file
            //NetcdfFileWriteable ncfile = NetcdfFileWriteable.openExisting(fileOut, true);
            NetcdfFileWriteable ncfile = NetcdfFileWriteable.openExisting(fileOut);
            // add lat/lon dimensions, varaibles, just in case not included in
            // in the ncml file (DEMO ONLY)
            ArrayFloat.D0 dataLat = new ArrayFloat.D0();
            ArrayFloat.D0 dataLon = new ArrayFloat.D0();
            Float latVal = 69.2390F;
            Float lonVal = -51.0623F; 
            dataLat.set(latVal);
            dataLon.set(lonVal);
            ncfile.write("lat", dataLat);
            ncfile.write("lon", dataLon);
            // END DEMO SPECIFIC CODE
            vars = ncfile.getVariables();
            // get time dim
            Dimension timeDim = ncfile.findDimension("time");
            for (int var = 0; var < vars.size(); var = var + 1){
                Variable tmp_var = (Variable) vars.get(var);
                String varName = tmp_var.getName();
                Attribute attr = tmp_var.findAttribute("_columnId");
                DataType dt = tmp_var.getDataType();
                //String thing = attr.getStringValue();
                if (attr != null) {

                    int varIndex = Integer.parseInt(attr.getStringValue());
                    int len = outerList.size();
                    //if (dt.toString() == "float") {
                    ArrayFloat.D1 vals = new ArrayFloat.D1(timeDim.getLength());
                    //} else {
                    for (int i = 0; i < timeDim.getLength(); i++) {
                        List row = (List) outerList.get(i);
                        vals.set(i,Float.valueOf((String) row.get(varIndex)).floatValue());
                        System.out.print("  " + (String) row.get(varIndex) + "\n");
                    }
                    //try {
                    ncfile.write(varName, vals);
                    //} catch (ucar.ma2.InvalidRangeException e) {
                    //    System.out.print(e.getStackTrace().toString());
                    //    return false;
                    //}
                }
            }
            ncfile.flush();
            ncfile.close();

            File file = new File(fileOut);

            if(file.exists()) { 
                System.out.print("I'm here!!!");
                return true;
            } else {
                System.out.print("Error!  NetCDF file " + fileOut + "was not created.");
                return false;
            }
        } catch (IOException e) {
            System.out.print(e.getMessage());
            System.out.print(e.getStackTrace().toString());
            return false;
        } catch (InvalidRangeException e) {
            System.out.print(e.getMessage());
            System.out.print(e.getStackTrace().toString());
            return false;
        }
    }

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
}
