/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.custom.dsg;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.util.TemplateFactory;
import edu.ucar.unidata.rosetta.util.test.util.TestUtils;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class SingleProfileTest2 {
    private String ctdNetcdfFile;

    @Test
    public void convertCtd() throws IOException, RosettaDataException, InvalidRangeException {
        Path datafile = Paths.get(TestUtils.getTestDataDirStr(), "singleProfile", "axctd", "OMG_09132016160633_Ch12_333.edf");
        Path templatefile = Paths.get(TestUtils.getTestDataDirStr(), "singleProfile", "axctd", "rosetta.template");

        // read template
        Template template = TemplateFactory.makeTemplateFromJsonFile(templatefile);

        // get converter
        NetcdfFileManager dsgWriter = new SingleProfile();

        // hardcode delimiter as comma.
        ctdNetcdfFile = dsgWriter.createNetcdfFile(datafile, template, "\\s+");

        NetcdfFile ncf = NetcdfFile.open(ctdNetcdfFile);
        Assert.assertNotNull(ncf);

        List<RosettaGlobalAttribute> templateGlobalMetadata = template.getGlobalMetadata();
        List<Attribute> actualGlobalMetadata = ncf.getGlobalAttributes();

        // should be more global medata in the netCDF file
        Assert.assertTrue(actualGlobalMetadata.size() > templateGlobalMetadata.size());

        // check that template global metadata are in the netCDF file
        for (RosettaGlobalAttribute rga : templateGlobalMetadata) {
            Attribute ncAttr = ncf.findGlobalAttribute(rga.getName());
            Assert.assertNotNull(ncAttr);
        }

        // check latitude
        Variable lat = ncf.findVariable("latitude");
        Array arr = lat.read();
        Number expected = ncf.findGlobalAttributeIgnoreCase("geospatial_lat_start").getNumericValue();
        Assert.assertEquals(expected.doubleValue(), arr.getDouble(0), 0.0001);

        // check longitude
        Variable lon = ncf.findVariable("longitude");
        arr = lon.read();
        expected = ncf.findGlobalAttributeIgnoreCase("geospatial_lon_start").getNumericValue();
        Assert.assertEquals(expected.doubleValue(), arr.getDouble(0), 0.0001);

        // check that status variable was actually written (string / char check)
        Variable status = ncf.findVariable("status");
        int[] origin;
        int[] shape = new int[] {1, 4};
        int numVals = status.getShape()[0];
        // all of the status values in this file are "good", or "0000"
        for (int i = 0; i < numVals; i++) {
            origin = new int[] {i,0};
            String val = status.read(origin, shape).toString();
            Assert.assertEquals("0000", val);
        }

        ncf.close();
    }

    @After
    public void cleanup() {
        File ncf = new File(ctdNetcdfFile);
        if (ncf.exists()) {
            ncf.delete();
        }
    }
}
