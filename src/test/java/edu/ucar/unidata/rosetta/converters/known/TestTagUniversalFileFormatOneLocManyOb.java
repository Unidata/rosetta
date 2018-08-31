/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.known;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.ucar.unidata.rosetta.converters.known.etuff.TagUniversalFileFormat;
import edu.ucar.unidata.rosetta.util.test.util.TestUtils;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.time.CalendarDate;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Test the conversion of an eTUFF file
 */
public class TestTagUniversalFileFormatOneLocManyOb {

    private static String etuffDir = String
            .join(File.separator, "conversions", "TagUniversalFileFormat");
    private static String etuffFileTld = TestUtils.getTestDataDirStr() + etuffDir;

    private static String etuffFile = String
            .join(File.separator, etuffFileTld, "eTUFF-tuna-590051-small.txt");
    private static String etuffFileNc = String
            .join(File.separator, etuffFileTld, "eTUFF-tuna-590051-small.nc");
    private static NetcdfDataset ncd;

    private static int numRecords = 1594;

    private TagUniversalFileFormat etuffConvertor = new TagUniversalFileFormat();

    @BeforeClass
    public static void sanityCheckAndOutputRead() throws IOException, InvalidRangeException {
        TagUniversalFileFormat converter = new TagUniversalFileFormat();
        converter.setMatchupOneLocOneOb(false);
        // won't always have netCDF-C lib installed where running tests
        converter.setUseNetcdf4(false);

        // check to make sure no global metadata has been set
        assertEquals(converter.getGlobalMetadata().size(), 0);
        converter.parse(etuffFile);
        // check to make sure global metadata was parsed
        //assertEquals(converter.getGlobalMetadata().size(), 39);

        String ncfile = converter.convert(etuffFileNc);
        ncd = NetcdfDataset.openDataset(ncfile);
    }

    @AfterClass
    public static void cleanup() throws IOException {
        ncd.close();
        File tmp = new File(etuffFileNc);
        tmp.delete();
    }

    @Test
    public void testCoordinateAxes() {
        List<CoordinateAxis> cas = ncd.getCoordinateAxes();
        assertEquals(cas.size(), 5);
        Dimension timeDim = ncd.findDimension("time");
        assertEquals(numRecords, timeDim.getLength());
    }

    @Test
    public void testVariables() {
        List<Variable> vars = ncd.getVariables();
        assertEquals(vars.size(), 9);
        // all variables, except the trajectory id, should have a length of 25
        for (Variable var : vars) {
            if (!var.getFullName().equals("trajectory")) {
                List<Dimension> dims = var.getDimensions();
                assertEquals(dims.size(), 1);
                Dimension timeDim = dims.get(0);
                assertEquals(numRecords, timeDim.getLength());
            }
        }
    }

    @Test
    public void testNoMatchup() throws IOException {
        // for this date and ob:
        // "2005-04-15 21:33:00",6,23.30,"temperature","Celsius"
        // there is no lat/lon matchup
        // this tests makes sure it is matched with a location, as
        // we are doing a many ob to one loc matchup
        CalendarDate findTimeCd = CalendarDate.parseISOformat("gregorian", "2005-04-15 21:33:00");
        long findTime = findTimeCd.toDate().getTime() / 1000;
        Variable time = ncd.findVariable("time");
        Array timeVals = time.read();

        boolean found = false;
        int foundIndex = 0;
        for (int i = 0; i < timeVals.getSize(); i++) {
            long checkTime = timeVals.getLong(i);
            if (checkTime == findTime) {
                found = true;
                foundIndex = i;
            }
        }
        assertTrue(found);

        // check that temperature value is as expected
        Variable temp = ncd.findVariable("temperature");
        Array tempVals = temp.read();
        assertEquals(23.30, tempVals.getFloat(foundIndex), 0.001);
    }

    @Test
    public void testMatchup() throws IOException {
        // for this date and ob:
        //  "2005-04-16 00:00:00",6,21.50,"temperature","Celsius"
        // there is a lat/lon matchup
        CalendarDate findTimeCd = CalendarDate.parseISOformat("gregorian", "2005-04-16 00:00:00");
        long findTime = findTimeCd.toDate().getTime() / 1000;
        Variable time = ncd.findVariable("time");
        Array timeVals = time.read();
        int foundIndex = 0;
        boolean found = false;
        for (int i = 0; i < timeVals.getSize(); i++) {
            long checkTime = timeVals.getLong(i);
            if (checkTime == findTime) {
                found = true;
                foundIndex = i;
            }
        }
        assertTrue(found);

        // check that temperature value is as expected
        Variable temp = ncd.findVariable("temperature");
        Array tempVals = temp.read();
        assertEquals(21.50, tempVals.getFloat(foundIndex), 0.001);
    }
}