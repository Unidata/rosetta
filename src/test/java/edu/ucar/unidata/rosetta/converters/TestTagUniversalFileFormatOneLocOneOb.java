/*
 * Copyright (c) 2012-2017 University Corporation for Atmospheric Research/Unidata
 */

package edu.ucar.unidata.rosetta.converters;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.ucar.unidata.rosetta.util.TestDir;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.time.CalendarDate;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test the conversion of an eTUFF file
 */
public class TestTagUniversalFileFormatOneLocOneOb {
    private static String etuffDir = String.join(File.separator, "conversions", "TagUniversalFileFormat");
    private static String etuffFileTld = TestDir.rosettaLocalTestDataDir + etuffDir;
    //private static String etuffFile = String.join(File.separator, etuffFileTld, "eTUFF-tuna-590051.gz");
    private static String etuffFile = String.join(File.separator, etuffFileTld, "TagDataFlatFileExample.gz");
    private static String etuffFileNc = String.join(File.separator, etuffFileTld, "eTUFF-tuna-590051.nc");
    private static NetcdfDataset ncd;

    private TagUniversalFileFormat etuffConvertor = new TagUniversalFileFormat();

    @BeforeClass
    public static void sanityCheckAndOutputRead() {
        TagUniversalFileFormat converter = new TagUniversalFileFormat();
        converter.setMatchupOneLocOneOb(true);
        // check to make sure no global metadata has been set
        assertEquals(converter.getGlobalMetadata().size(), 0);
        converter.parse(etuffFile);
        // check to make sure global metadata was parsed
        assertEquals(converter.getGlobalMetadata().size(), 39);


        try {
            String ncfile = converter.convert(etuffFileNc);
            ncd = NetcdfDataset.openDataset(ncfile);

        } catch (InvalidRangeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCoordinateAxes() {
        List<CoordinateAxis> cas = ncd.getCoordinateAxes();
        assertEquals(cas.size(), 5);
        Dimension timeDim = ncd.findDimension("time");
        assertEquals(timeDim.getLength(), 25);
    }

    @Test
    public void testVariables() {
        List<Variable> vars = ncd.getVariables();
        assertEquals(vars.size(), 8);
        // all variables, except the trajectory id, should have a length of 25
        for (Variable var : vars) {
            if (!var.getFullName().equals("trajectory")) {
                List<Dimension> dims = var.getDimensions();
                assertEquals(dims.size(), 1);
                Dimension timeDim = dims.get(0);
                assertEquals(timeDim.getLength(), 25);
            }
        }
    }

    @Test
    public void testNoMatchup() throws IOException {
        // for this date and ob:
        //   2005-07-01 0:06:00,6,14.60,"temperature","Celsius"
        // there is no lat/lon matchup
        CalendarDate findTimeCd = CalendarDate.parseISOformat("gregorian", "2005-07-01 0:06:00");
        long findTime = findTimeCd.toDate().getTime() / 1000;
        Variable time = ncd.findVariable("time");
        Array timeVals = time.read();

        boolean found = false;
        for (int i = 0; i < timeVals.getSize(); i++) {
            long checkTime = timeVals.getLong(i);
            if (checkTime == findTime) {
                found = true;
            }
        }
        assertFalse(found);
    }

    @Test
    public void testMatchup() throws IOException {
        // for this date and ob:
        //   2005-07-10 0:00:00,6,15.10,"temperature","Celsius"
        // there is a lat/lon matchup
        CalendarDate findTimeCd = CalendarDate.parseISOformat("gregorian", "2005-07-10 0:00:00");
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
        assertEquals(tempVals.getFloat(foundIndex), 15.10, 0.001);
    }

    @AfterClass
    public static void cleanup() {
        File tmp = new File(etuffFileNc);
        tmp.delete();
    }
}