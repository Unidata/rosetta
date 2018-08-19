/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.custom.dsg;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.util.test.util.TestUtils;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

public class SingleTrajectoryTest {
    private String wavegliderNetcdfFile;

    @Test
    public void convertWaveglider() throws IOException {
        Path datafile = Paths.get(TestUtils.getTestDataDirStr(), "singleTrajectory", "waveglider", "ASL32.txt");
        Path templatefile = Paths.get(TestUtils.getTestDataDirStr(), "singleTrajectory", "waveglider", "rosetta.template");

        // read template
        ObjectMapper templateMapper = new ObjectMapper();
        FileReader templateFileReader = new FileReader(templatefile.toFile());
        Template template = templateMapper.readValue(templateFileReader, Template.class);
        templateFileReader.close();

        // find correct writer
        NetcdfFileManager dsgWriter = null;
        for (NetcdfFileManager potentialDsgWriter : NetcdfFileManager.getConverters()) {
            if (potentialDsgWriter.isMine(template.getCfType())) {
                dsgWriter = potentialDsgWriter;
                break;
            }
        }

        Assert.assertNotNull(dsgWriter);

        wavegliderNetcdfFile = dsgWriter.createNetcdfFile(datafile.toFile(), template);

        NetcdfFile ncf = NetcdfFile.open(wavegliderNetcdfFile);
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

        ncf.close();
    }

    @After
    public void cleanup() {
        File ncf = new File(wavegliderNetcdfFile);
        if (ncf.exists()) {
            ncf.delete();
        }
    }
}
