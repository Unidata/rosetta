/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata
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
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.util.TemplateFactory;
import edu.ucar.unidata.rosetta.util.XlsToCsvUtil;
import edu.ucar.unidata.rosetta.util.test.util.TestUtils;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class SingleTimeSeriesTest {

  private String tsNetcdfFile;
  private Path tsCsvFile;

  @Test
  public void convertStationXls() throws IOException, RosettaDataException, RosettaFileException {
    Path xlsfile =
        Paths.get(TestUtils.getTestDataDirStr(), "singleStationTimeSeries", "StationSoilTemp", "ilu01_07_10_small.xls");
    tsCsvFile =
        Paths.get(TestUtils.getTestDataDirStr(), "singleStationTimeSeries", "StationSoilTemp", "ilu01_07_10_small.csv");
    Path templatefile =
        Paths.get(TestUtils.getTestDataDirStr(), "singleStationTimeSeries", "StationSoilTemp", "rosetta.template");

    // make datafile
    Assert.assertTrue(XlsToCsvUtil.convert(xlsfile.toString(), null));

    // read template
    Template template = TemplateFactory.makeTemplateFromJsonFile(templatefile);

    // get converter
    NetcdfFileManager dsgWriter = new SingleTimeSeries();

    // hardcode delimiter as comma.
    tsNetcdfFile = dsgWriter.createNetcdfFile(tsCsvFile, template, ",");

    NetcdfFile ncf = NetcdfFile.open(tsNetcdfFile);
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

    ncf.close();
  }

  @After
  public void cleanup() {
    File ncf = new File(tsNetcdfFile);
    if (ncf.exists()) {
      ncf.delete();
    }
    File csvFile = tsCsvFile.toFile();
    if (csvFile.exists()) {
      csvFile.delete();
    }
  }
}
