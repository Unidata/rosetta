/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import edu.ucar.unidata.rosetta.util.test.util.TestUtils;


public class TemplateTest {

    private List<Template> templates = new ArrayList<>();
    private Template bogusTemplate;
    private Template wavegliderTemplate;

    private String attrString = "STRING";
    private String attrBool = "BOOLEAN";

    private void createBogusTemplate() {
        bogusTemplate = new Template();

        bogusTemplate.setCfType("cftype");
        bogusTemplate.setCommunity("community");
        bogusTemplate.setCreationDate("2018-08-09T130412.345");
        bogusTemplate.setDelimiter(" ");
        bogusTemplate.setFormat("custom");
        bogusTemplate.setPlatform("platform");
        bogusTemplate.setRosettaVersion("0.4");
        bogusTemplate.setServerId("serverId");
        bogusTemplate.setTemplateVersion("2.0");

        List<RosettaAttribute> globalMetadata = new ArrayList<>();
        globalMetadata.add(new RosettaAttribute("gastr", "yo", attrString));
        globalMetadata.add(new RosettaAttribute("gaint", "1", attrString));
        globalMetadata.add(new RosettaAttribute("ga3float", "2.3", attrString));
        bogusTemplate.setGlobalMetadata(globalMetadata);

        List<Integer> headerLineNumbers = new ArrayList<>();
        headerLineNumbers.add(0);
        bogusTemplate.setHeaderLineNumbers(headerLineNumbers);

        List<VariableInfo> variableInfoList = new ArrayList<>();

        // variable 0
        VariableInfo var0 = new VariableInfo();
        var0.setColumnId(0);
        var0.setName("variable0");

        List<RosettaAttribute> var0Attrs = new ArrayList<>();
        var0Attrs.add(new RosettaAttribute("units", "K", attrString));
        var0Attrs.add(new RosettaAttribute("name", "varname0", attrString));
        var0Attrs.add(new RosettaAttribute("long_name", "var0 long name", attrString));

        var0.setVariableMetadata(var0Attrs);

        List<RosettaAttribute> rosettaControlMetadata0 = new ArrayList<>();
        rosettaControlMetadata0.add(new RosettaAttribute("type", "int", attrString));
        rosettaControlMetadata0.add(new RosettaAttribute("coordinateVariable", "true", attrBool));
        rosettaControlMetadata0.add(new RosettaAttribute("coordinateVariableType", "lat", attrString));
        var0.setRosettaControlMetadata(rosettaControlMetadata0);


        VariableInfo var1 = new VariableInfo();
        var1.setColumnId(0);
        var1.setName("variable1");

        List<RosettaAttribute> var1Attrs = new ArrayList<>();
        var1Attrs.add(new RosettaAttribute("units", "K", attrString));
        var1Attrs.add(new RosettaAttribute("name", "varname0", attrString));
        var1Attrs.add(new RosettaAttribute("long_name", "var0 long name", attrString));

        var1.setVariableMetadata(var1Attrs);

        List<RosettaAttribute> rosettaControlMetadata1 = new ArrayList<>();
        rosettaControlMetadata1.add(new RosettaAttribute("type", "int", attrString));
        rosettaControlMetadata1.add(new RosettaAttribute("coordinateVariable", "true", attrBool));
        rosettaControlMetadata1.add(new RosettaAttribute("coordinateVariableType", "lat", attrString));
        var1.setRosettaControlMetadata(rosettaControlMetadata1);

        variableInfoList.add(var0);
        variableInfoList.add(var1);
        bogusTemplate.setVariableInfoList(variableInfoList);
    }

    private void readWavegliderTemplate() throws IOException {
        File wavegliderTemplateFile = Paths.get(TestUtils.getTestDataDirStr(),
                "singleTrajectory", "waveglider", "rosetta.template").toFile();

        com.fasterxml.jackson.databind.ObjectMapper templateMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        FileReader templateFileReader = new FileReader(wavegliderTemplateFile);
        wavegliderTemplate = templateMapper.readValue(templateFileReader, Template.class);
        templateFileReader.close();
    }

    @Before
    public void setupTemplates() throws IOException {
        createBogusTemplate();
        readWavegliderTemplate();

        // any template added to templates will be round-trip tested
        List<Template> templates = new ArrayList<>();
        templates.add(bogusTemplate);
        templates.add(wavegliderTemplate);
    }

    @Test
    public void roundTripJson() throws IOException {
        for (Template template : templates) {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(template);
            Assert.assertNotNull(json);
            Template template2 = mapper.readValue(json, Template.class);
            Assert.assertEquals(template, template2);
        }
    }

    @Test
    public void testWavegliderTemplate() {
        // check a few items to make sure things are there, as expected
        Assert.assertEquals(wavegliderTemplate.getCfType(),"trajectoryProfile");
        Assert.assertEquals(wavegliderTemplate.getHeaderLineNumbers().size(), 1);
        int headerLine = wavegliderTemplate.getHeaderLineNumbers().get(0);
        Assert.assertNotNull(headerLine);
        Assert.assertEquals(headerLine, 0);
        wavegliderTemplate.getGlobalMetadata().contains(new RosettaAttribute("sea_name", "Atlantic", "String"));
    }
}
