package edu.ucar.unidata.pzhta;

import org.grlea.log.SimpleLogger;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.NcMLReader;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.VariableDS;

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
}
