/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util.test.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by sarms on 3/11/17.
 */
public class TestUtils {

    private static String pathSep = File.separator;

    /**
     * test data directory
     */
    private static String rosettaRelativeLocalTestDataDir = "src/test/data/";

    /**
     * gretty test server url
     */
    private static String testServerUrl = "http://localhost:8888/rosetta";

    private static String rosettaLocalTestDataDir =
            new File(TestUtils.rosettaRelativeLocalTestDataDir).getAbsolutePath() + pathSep;

    public static String getTestServerUrl() {
        return testServerUrl;
    }

    public static String getTestDataDirStr() {
        return rosettaLocalTestDataDir;
    }

    public static Path getTestDataDirPath() {
        return Paths.get(getTestDataDirStr());
    }

}
