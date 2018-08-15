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
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import edu.ucar.unidata.rosetta.util.test.util.TestUtils;


public class TemplateTest {

    private List<Template> templates = new ArrayList<>();
    private Template bogusTemplate, bogusTemplate2;
    private Template wavegliderTemplate;

    private String attrString = "STRING";
    private String attrBool = "BOOLEAN";
    private String attrInt = "INTEGER";
    private String attrFloat = "FLOAT";

    private RosettaAttribute modifiedGlobalAttrOld;
    private RosettaAttribute modifiedGlobalAttrUpdated;
    private RosettaAttribute newGlobalAttr;
    private RosettaAttribute removedGlobalAttr;

    private RosettaAttribute modifiedVarAttrOld;
    private RosettaAttribute modifiedVarAttrUpdated;
    private RosettaAttribute newVarAttr;
    private RosettaAttribute removedVarAttr;
    private VariableInfo modifiedVarInfo;
    private VariableInfo newVarInfo;
    private VariableInfo removedVarInfo;

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
        // will test this later to see if it has been modified
        modifiedGlobalAttrOld = new RosettaAttribute("gastr", "yo", attrString);
        globalMetadata.add(modifiedGlobalAttrOld);
        globalMetadata.add(new RosettaAttribute("gaint", "1", attrInt));
        globalMetadata.add(new RosettaAttribute("ga3float", "2.3", attrFloat));
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
        modifiedVarAttrOld = new RosettaAttribute("units", "K", attrString);
        var0Attrs.add(modifiedVarAttrOld);
        var0Attrs.add(new RosettaAttribute("name", "varname0", attrString));
        removedVarAttr = new RosettaAttribute("long_name", "", attrString);
        var0Attrs.add(removedVarAttr);

        var0.setVariableMetadata(var0Attrs);

        List<RosettaAttribute> rosettaControlMetadata0 = new ArrayList<>();
        rosettaControlMetadata0.add(new RosettaAttribute("type", "int", attrString));
        rosettaControlMetadata0.add(new RosettaAttribute("coordinateVariable", "true", attrBool));
        rosettaControlMetadata0.add(new RosettaAttribute("coordinateVariableType", "lat", attrString));
        var0.setRosettaControlMetadata(rosettaControlMetadata0);


        VariableInfo var1 = new VariableInfo();
        var1.setColumnId(1);
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

        removedVarInfo = new VariableInfo();
        removedVarInfo.setColumnId(2);
        removedVarInfo.setName("variable2");
        variableInfoList.add(removedVarInfo);

        bogusTemplate.setVariableInfoList(variableInfoList);

    }

    private void createBogusTemplateModifier() {
        // template to modify bogusTemplate
        bogusTemplate2 = new Template();

        List<Integer> headerLineNumbers = new ArrayList<>();
        // update header line numbers
        headerLineNumbers.add(0);
        headerLineNumbers.add(1);
        bogusTemplate2.setHeaderLineNumbers(headerLineNumbers);

        // update existing global attr and add new
        List<RosettaAttribute> globalMetadata = new ArrayList<>();
        modifiedGlobalAttrUpdated = new RosettaAttribute(modifiedGlobalAttrOld.getName(), "yo-updated", modifiedGlobalAttrOld.getType());
        globalMetadata.add(modifiedGlobalAttrUpdated);
        newGlobalAttr = new RosettaAttribute("ga-new", "w00t", attrString);
        globalMetadata.add(newGlobalAttr);
        bogusTemplate2.setGlobalMetadata(globalMetadata);

        // variable 0
        modifiedVarInfo = new VariableInfo();
        modifiedVarInfo.setColumnId(0);
        modifiedVarInfo.setName("variable0");
        // change attr
        List<RosettaAttribute> var0Attrs = new ArrayList<>();
        modifiedVarAttrUpdated = new RosettaAttribute(modifiedVarAttrOld.getName(), "degC", modifiedVarAttrOld.getType());
        var0Attrs.add(modifiedVarAttrUpdated);
        newVarAttr = new RosettaAttribute("new-attr-name", "new-attr-value", attrString);
        var0Attrs.add(newVarAttr);
        modifiedVarInfo.setVariableMetadata(var0Attrs);

        VariableInfo var1 = new VariableInfo();
        var1.setColumnId(1);
        var1.setName("variable1");
        // add new attr
        List<RosettaAttribute> var1Attrs = new ArrayList<>();
        var1Attrs.add(newVarAttr);

        // remove variable2
        VariableInfo var2 = new VariableInfo();
        var2.setColumnId(-9); // this tells the template code to remove the variable
        var2.setName(removedVarInfo.getName());

        // add new variable
        newVarInfo = new VariableInfo();
        newVarInfo.setColumnId(2);
        newVarInfo.setName("newvariable");
        // add new attr
        List<RosettaAttribute> var2Attrs = new ArrayList<>();
        var2Attrs.add(new RosettaAttribute("new-attr-new-var-name", "new-attr-new-var-value", attrString));
        newVarInfo.setVariableMetadata(var2Attrs);

        List<VariableInfo> variableInfoList = new ArrayList<>();
        variableInfoList.add(modifiedVarInfo);
        variableInfoList.add(var1);
        variableInfoList.add(newVarInfo);
        variableInfoList.add(var2);

        bogusTemplate2.setVariableInfoList(variableInfoList);
    }

    private void readWavegliderTemplate() throws IOException {
        File wavegliderTemplateFile = Paths.get(TestUtils.getTestDataDirStr(),
                "singleTrajectory", "waveglider", "rosetta.template").toFile();

        com.fasterxml.jackson.databind.ObjectMapper templateMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        FileReader templateFileReader = new FileReader(wavegliderTemplateFile);
        wavegliderTemplate = templateMapper.readValue(templateFileReader, Template.class);
        templateFileReader.close();
    }

    private Template copyBogusTemplate() throws IOException {
        // make a copy of bogus template that we can modify without
        // screwing up the rest of the tests that might rely on bogusTemplate
        // --> should probably make Template clonable...but for now, we'll do this
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(bogusTemplate);
        return mapper.readValue(json, Template.class);
    }

    @Before
    public void setupTemplates() throws IOException {
        createBogusTemplate();
        createBogusTemplateModifier();
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

    @Test
    public void testBogusModifiedHeaderLines() throws IOException {
        // get a copy the bogus template for modification
        Template modifiedTemplate = copyBogusTemplate();

        // now let's modify the template
        modifiedTemplate.update(bogusTemplate2);

        // let's check headerline modifications
        Assert.assertNotEquals(bogusTemplate.getHeaderLineNumbers(), bogusTemplate2.getHeaderLineNumbers());
        Assert.assertEquals(bogusTemplate2.getHeaderLineNumbers(), modifiedTemplate.getHeaderLineNumbers());
    }

    @Test
    public void testBogusModifiedGlobalMetadata() throws IOException {
        // get a copy the bogus template for modification
        Template modifiedTemplate = copyBogusTemplate();

        // now let's modify the template
        modifiedTemplate.update(bogusTemplate2);

        // updated and new global metadata
        Assert.assertNotEquals(bogusTemplate.getGlobalMetadata().size(), modifiedTemplate.getGlobalMetadata().size());
        Assert.assertFalse(bogusTemplate.getGlobalMetadata().contains(newGlobalAttr));
        List<RosettaAttribute> modifiedGlobalMetadata = modifiedTemplate.getGlobalMetadata();
        Assert.assertTrue(modifiedGlobalMetadata.contains(modifiedGlobalAttrUpdated));
        Assert.assertTrue(modifiedGlobalMetadata.contains(newGlobalAttr));
        Assert.assertFalse(modifiedGlobalMetadata.contains(modifiedGlobalAttrOld));
    }

    @Test
    public void testBogusModifiedVariableMetadata() throws IOException {
        // get a copy the bogus template for modification
        Template modifiedTemplate = copyBogusTemplate();

        // now let's modify the template
        modifiedTemplate.update(bogusTemplate2);

        // updated and new variableinfo
        Assert.assertFalse(bogusTemplate.getVariableInfoList().contains(newVarInfo));
        List<VariableInfo> modifiedVariableInfoList = modifiedTemplate.getVariableInfoList();
        // make sure new VarInfo was added
        Assert.assertTrue(modifiedVariableInfoList.contains(newVarInfo));
        // check specific VarInfo entries
        boolean varRemoved = true;
        for (VariableInfo var : modifiedVariableInfoList) {
            // find the variable that was modified, and check it to see if the existing attr was
            // modified, and a new attr was added
            if (var.getName().equals(modifiedVarInfo.getName())) {
                // check if attribute was modified
                Assert.assertTrue(var.getVariableMetadata().contains(modifiedVarAttrUpdated));
                Assert.assertFalse(var.getVariableMetadata().contains(modifiedVarAttrOld));

                // check if new variable attribute added
                Assert.assertTrue(var.getVariableMetadata().contains(newVarAttr));

                // check if variable attribute marked for removal was actually removed
                Assert.assertFalse(var.getVariableMetadata().contains(removedVarAttr));
            } else if (var.getName().equals(removedVarInfo.getName())){
                // should not be in modified template, as the columnId was set to -9
                varRemoved = false;
            }
        }
        // make sure the variable marked for removal was actually removed
        Assert.assertTrue(varRemoved);

    }

    @Test
    public void testBogusModifiedGeneral() throws IOException {
        // get a copy the bogus template for modification
        Template modifiedTemplate = copyBogusTemplate();
        // first, let's make sure we cloned the template correctly
        Assert.assertEquals(bogusTemplate, modifiedTemplate);

        // now let's modify the template
        modifiedTemplate.update(bogusTemplate2);

        // finally, let's make sure that the template updated (not a test for correctness of update, just that it was modified)
        Assert.assertNotEquals(bogusTemplate, modifiedTemplate);

    }
}
