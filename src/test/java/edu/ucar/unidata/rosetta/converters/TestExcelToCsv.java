/*
 * Copyright (c) 2012-2017 University Corporation for Atmospheric Research/Unidata
 */

package edu.ucar.unidata.rosetta.converters;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import java.io.File;
import java.nio.file.Paths;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.util.test.util.TestUtils;
import edu.ucar.unidata.rosetta.util.XlsToCsvUtil;
import static org.junit.Assert.assertTrue;

/**
 * Created by sarms on 3/11/17.
 */
public class TestExcelToCsv {

  @Test
  @Ignore
  public void testXlsConvert() throws RosettaFileException {
    File xlsFilePath = Paths.get(TestUtils.getTestDataDirStr(), "conversions", "xls", "test.xls").toFile();
    assertTrue(XlsToCsvUtil.convert(xlsFilePath.getAbsolutePath(), null));
  }

  @After
  public void cleanup() {
    File csvFilePath = Paths.get(TestUtils.getTestDataDirStr(), "conversions", "xls", "test.xls").toFile();
    csvFilePath.delete();
  }
}
