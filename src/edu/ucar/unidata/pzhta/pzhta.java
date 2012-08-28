package edu.ucar.unidata.pzhta;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.NcMLReader;
import ucar.nc2.NetcdfFile;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: sarms
 */
public class pzhta {
    public static void main(String args[]){
        String ncml_filename = "file:///Users/lesserwhirls/dev/unidata/pzhta/src/edu/ucar/unidata/pzhta/test/test.ncml";
        String fileout_name = "/Users/lesserwhirls/dev/unidata/pzhta/src/edu/ucar/unidata/pzhta/test/pzhta_test.nc";
        System.out.println( "*** Reading NCML");
        try{
            NetcdfDataset ncd = NcMLReader.readNcML(ncml_filename, null);
            System.out.println( "*** Writing netCDF file");
            NetcdfFile ncdnew = ucar.nc2.FileWriter.writeToFile(ncd, fileout_name, true);
            ncd.close();
            ncdnew.close();
            System.out.println( "*** Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}