/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.VariableInfo;
import ucar.nc2.Attribute;

public class TemplateUtils {

    public static Map<String, ArrayList<Attribute>> getGlobalAttrsMap(Template template) {
        HashMap<String, ArrayList<Attribute>> globalAttrsMap = new HashMap<>();

        for (RosettaGlobalAttribute globalAttr : template.getGlobalMetadata()) {
            String groupName = globalAttr.getGroup();
            ArrayList<Attribute> groupAttrs = globalAttrsMap.getOrDefault(groupName, new ArrayList<>());
            Attribute attr = RosettaGlobalAttributeUtils.getAttributeFromGlobalAttr(globalAttr);
            groupAttrs.add(attr);
            globalAttrsMap.put(groupName, groupAttrs);

        }

        return globalAttrsMap;
    }

    public static String findUniqueName(String name, Template template) {
        boolean uniqueName = true;
        int pass = 0;
        while (uniqueName) {
            for (VariableInfo vi : template.getVariableInfoList()) {
                if (vi.getName().equals(name)) {
                    pass = pass + 1;
                    name = name + String.valueOf(pass);
                    uniqueName = false;
                }
            }
        }
        return name;
    }
}
