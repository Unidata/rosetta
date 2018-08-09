/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TemplateTest {

    Template template;

    String attrString = "STRING";
    String attrInt = "INT";
    String attrDouble = "DOUBLE";
    String attrBool = "BOOLEAN";

    @Before
    public void setupTemplate() {
        template = new Template();

        template.setCfType("cftype");
        template.setCommunity("community");
        template.setCreationDate("2018-08-09T130412.345");
        template.setDelimiter(" ");
        template.setFormat("custom");
        template.setPlatform("platform");
        template.setRosettaVersion("0.4");
        template.setServerId("serverId");
        template.setTemplateVersion("2.0");

        List<RosettaAttribute> globalMetadata = new ArrayList<>();
        globalMetadata.add(new RosettaAttribute("gastr", "yo", attrString));
        globalMetadata.add(new RosettaAttribute("gaint", "1", attrString));
        globalMetadata.add(new RosettaAttribute("ga3float", "2.3", attrString));
        template.setGlobalMetadata(globalMetadata);

        List<Integer> headerLineNumbers = new ArrayList<>();
        headerLineNumbers.add(0);
        template.setHeaderLineNumbers(headerLineNumbers);

        List<VariableInfo> variableInfoList = new ArrayList<>();

        // variable 0
        VariableInfo var0 = new VariableInfo();
        var0.setColumnId(0);
        var0.setName("variable0");

        List<RosettaAttribute> var0Attrs =  new ArrayList<>();
        var0Attrs.add(new RosettaAttribute("units", "K", attrString));
        var0Attrs.add(new RosettaAttribute("name", "varname0", attrString));
        var0Attrs.add(new RosettaAttribute("long_name", "var0 long name", attrString));

        var0.setVariableMetadata(var0Attrs);

        List<RosettaAttribute> rosettaControlMetadata0 =  new ArrayList<>();
        rosettaControlMetadata0.add(new RosettaAttribute("type","int", attrString));
        rosettaControlMetadata0.add(new RosettaAttribute("coordinateVariable", "true", attrBool));
        rosettaControlMetadata0.add(new RosettaAttribute("coordinateVariableType", "lat", attrString));
        var0.setRosettaControlMetadata(rosettaControlMetadata0);


        VariableInfo var1 = new VariableInfo();
        var1.setColumnId(0);
        var1.setName("variable1");

        List<RosettaAttribute> var1Attrs =  new ArrayList<>();
        var1Attrs.add(new RosettaAttribute("units", "K", attrString));
        var1Attrs.add(new RosettaAttribute("name", "varname0", attrString));
        var1Attrs.add(new RosettaAttribute("long_name", "var0 long name", attrString));

        var1.setVariableMetadata(var1Attrs);

        List<RosettaAttribute> rosettaControlMetadata1 =  new ArrayList<>();
        rosettaControlMetadata1.add(new RosettaAttribute("type","int", attrString));
        rosettaControlMetadata1.add(new RosettaAttribute("coordinateVariable", "true", attrBool));
        rosettaControlMetadata1.add(new RosettaAttribute("coordinateVariableType", "lat", attrString));
        var1.setRosettaControlMetadata(rosettaControlMetadata1);

        variableInfoList.add(var0);
        variableInfoList.add(var1);
        template.setVariableInfoList(variableInfoList);
    }

    @Test
    public void roundTripJson() throws IOException {
        // make sure we can read the templates we write
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(template);
        Assert.assertNotNull(json);
        Template template2 = mapper.readValue(json, Template.class);
        Assert.assertEquals(template, template2);

    }
}
