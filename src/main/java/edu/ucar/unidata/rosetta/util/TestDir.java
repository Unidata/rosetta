package edu.ucar.unidata.rosetta.util;

import java.io.File;

/**
 * Created by sarms on 3/11/17.
 */
public class TestDir {

  private static String pathSep = File.separator;
  /**
   * test data directory
   */
  private static String rosettaRelativeLocalTestDataDir = "src/test/data/";
  public static String rosettaLocalTestDataDir =
      new File(TestDir.rosettaRelativeLocalTestDataDir).getAbsolutePath() + pathSep;
}
