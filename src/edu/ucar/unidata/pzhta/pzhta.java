package edu.ucar.unidata.pzhta;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.NcMLReader;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.VariableDS;

import java.io.IOException;
import java.util.List;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: sarms
 */
public class pzhta {
    public static void main(String[] args) {
        String ncml_filename = "file:///Users/lesserwhirls/dev/unidata/pzhta/src/edu/ucar/unidata/pzhta/test/test.ncml";
        String fileout_name = "/Users/lesserwhirls/dev/unidata/pzhta/src/edu/ucar/unidata/pzhta/test/pzhta_test.nc";
        System.out.println( "*** Reading NCML\n");
        try{
            NetcdfDataset ncd = NcMLReader.readNcML(ncml_filename, null);
            List globalAttributes = ncd.getGlobalAttributes();
            // Use iterator to display all Global Attributes
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
            System.out.println();
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
