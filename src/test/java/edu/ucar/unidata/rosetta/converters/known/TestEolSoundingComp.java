/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.known;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import edu.ucar.unidata.rosetta.converters.known.EolSoundingComp;
import edu.ucar.unidata.rosetta.util.test.util.TestUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by sarms on 3/11/17.
 */
public class TestEolSoundingComp {

  private Path eccFileTldPath = Paths.get(TestUtils.getTestDataDirStr(), "conversions", "esc");
  private String escFileTld = eccFileTldPath.toFile().getAbsolutePath();
  private EolSoundingComp escConvertor = new EolSoundingComp();

  @Test
  @Ignore
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
