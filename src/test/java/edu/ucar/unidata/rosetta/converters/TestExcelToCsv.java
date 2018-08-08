/*
 * Copyright (c) 2012-2017 University Corporation for Atmospheric Research/Unidata
 */

package edu.ucar.unidata.rosetta.converters;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.util.TestDir;
import edu.ucar.unidata.rosetta.util.XlsToCsvUtil;

import static org.junit.Assert.assertTrue;

/**
 * Created by sarms on 3/11/17.
 */
public class TestExcelToCsv {

    @Test
    @Ignore
    public void testXlsConvert() throws RosettaFileException {
        String xlsFilePath = TestDir.rosettaLocalTestDataDir + "conversions/xls/test.xls";
        assertTrue(XlsToCsvUtil.convert(xlsFilePath, null));
    }

    @After
    public void cleanup() {
        File csvFilePath = new File(TestDir.rosettaLocalTestDataDir + "conversions/xls/test.csv");
        csvFilePath.delete();
    }
}
