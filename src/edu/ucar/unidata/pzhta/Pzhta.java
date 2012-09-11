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

    public boolean convert(String ncmlFile, String fileOutName, ArrayList outerList) {
        String ncml_filename = "file:///Users/lesserwhirls/dev/unidata/pzhta/src/edu/ucar/unidata/pzhta/test/test.ncml";
        String fileout_name = "/Users/lesserwhirls/dev/unidata/pzhta/src/edu/ucar/unidata/pzhta/test/pzhta_test.nc";
        log.info( "*** Reading NCML\n");
        try{
            NetcdfDataset ncd = NcMLReader.readNcML(ncml_filename, null);
            List globalAttributes = ncd.getGlobalAttributes();
            // Use iterator to display all Global Attributes
            log.info("Global Attributes in file:\n");
            Iterator itr = globalAttributes.iterator();
            while(itr.hasNext()) {
                Object element = itr.next();
                log.info("  " + element + "\n");
            }
            log.info("\n");
            log.info("Variables in file:\n");
            List vars = ncd.getVariables();
            for (int var = 0; var < vars.size(); var = var + 1){
                log.info("  " + vars.get(var)  + "\n");
            }
            log.info(" ");
            log.info( "*** Writing netCDF file");
            NetcdfFile ncdnew = ucar.nc2.FileWriter.writeToFile(ncd, fileout_name, true);
            ncd.close();
            ncdnew.close();
            log.info( "*** Done");

            // open netCDF file
            NetcdfFileWriteable ncfile = NetcdfFileWriteable.openExisting(fileout_name, true);
            vars = ncfile.getVariables();
            // get time dim
            Dimension timeDim = ncfile.findDimension("time");
            for (int var = 0; var < vars.size(); var = var + 1){
                Variable tmp_var = (Variable) vars.get(var);
                String varName = tmp_var.getName();
                Attribute attr = tmp_var.findAttribute("_colNum");
                DataType dt = tmp_var.getDataType();
                //String thing = attr.getStringValue();
                if (attr != null) {
                    int varIndex = Integer.parseInt(attr.getStringValue());
                    int len = outerList.size();

                    //if (dt.toString().equals("float")) {
                    ArrayFloat.D1 vals = new ArrayFloat.D1(timeDim.getLength());
                    //} else {
                    for (int i = 0; i < timeDim.getLength(); i++) {
                        List row = (List) outerList.get(i);
                        vals.set(i,Float.valueOf((String) row.get(varIndex)).floatValue());
                        System.out.print("  " + (String) row.get(varIndex) + "\n");
                    }
                    try {
                        ncfile.write(varName, vals);
                    } catch (ucar.ma2.InvalidRangeException e) {
                        log.error(e.getStackTrace().toString());
                        return false;
                    }
                }
            }
            ncfile.close();

            File file = new File(fileout_name);
            if(file.exists()) { 
                return true;
            } else {
                log.error("Error!  NetCDF file " + fileout_name + "was not created.");
                return false;
            }
        } catch (IOException e) {
            log.error(e.getStackTrace().toString());
            return false;
        }
    }
    public static void main(String[] args) {
        Pzhta pz = new Pzhta();
        ArrayList<List<String>> outerList = new ArrayList<List<String>>();
        for (int j=0; j<10; j++) {
            ArrayList<String> innerList = new ArrayList<String>();
            for (int i=0; i<11; i++) {
                innerList.add(Integer.toString((i+j)*i));
            }
            outerList.add(innerList);
        }
        pz.convert(" ", " ", outerList);

    }
}
