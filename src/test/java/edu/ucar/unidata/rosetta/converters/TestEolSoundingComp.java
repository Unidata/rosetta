/*
 * Copyright (c) 2012-2017 University Corporation for Atmospheric Research/Unidata
 */

package edu.ucar.unidata.rosetta.converters;

import org.junit.After;
import org.junit.Test;

import java.io.File;

import edu.ucar.unidata.rosetta.util.TestDir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by sarms on 3/11/17.
 */
public class TestEolSoundingComp {
    private String escDir = String.join(File.separator, "conversions", "esc");
    private String escFileTld = TestDir.rosettaLocalTestDataDir + escDir;
    private EolSoundingComp escConvertor = new EolSoundingComp();

    @Test
    public void sanityCheck() {
        String escFile = String.join(File.separator, escFileTld, "ELLIS_20150610_reduced.cls");
        assertFalse(escConvertor.convert(escFile).isEmpty());

        String[] files = (new File(escFileTld)).list();
        // the conversion of the test cls file should produce three netCDF files
        assertEquals(files.length, 4);
    }

    @After
    public void cleanup() {
        String[] files = (new File(escFileTld)).list();
        // remove the newly created netCDF files
        for (String file : files) {
            File tmp = new File(String.join(File.separator, escFileTld, file));
            if (file.endsWith(".nc")) {
                tmp.delete();
            }
        }
    }
}
