/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.util.test.util.TestUtils;
import ucar.nc2.Attribute;

public class TemplateFactoryTest {

    @Test
    public void readMetadataFile() throws IOException {
        Path metadataFile = Paths.get(TestUtils.getTestDataDirStr(), "singleTrajectory", "waveglider", "ASL42.metadata");

        Template template = TemplateFactory.makeTemplateFromMetadataFile(metadataFile);
        Assert.assertNotNull(template);

        Map<String, ArrayList<Attribute>> globalAttrs = TemplateUtils.getGlobalAttrsMap(template);
        // should only have one group
        Assert.assertTrue(globalAttrs.keySet().size() == 1);

        // get the root level attributes
        ArrayList<Attribute> rootAttrs = globalAttrs.get("root");

        // make sure correct number of global attributes were read
        long expectedNumAttributes = Files.lines(metadataFile).count();
        long actualNumAttributes = rootAttrs.size();
        Assert.assertEquals(expectedNumAttributes, actualNumAttributes);

        // check one attribute for correct values
        Attribute testAttr = new Attribute("uuid", "new_uuid_ASL42");
        Assert.assertTrue(rootAttrs.contains(testAttr));
    }

}
