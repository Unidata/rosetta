/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.VariableInfo;
import ucar.nc2.Attribute;

public class TemplateUtils {

    /**
     * Extract the global attributes from a template
     *
     * Attribute are retuned in a map, where group is the key, and the value is a list of
     * netCDF-Java Attribute objects.
     *
     * @param template The template object which holds RosettaGlobalAttributes
     * @return a map with groupName keys corresponding to a list of attributes
     */
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

    /**
     * Find a unique variable name given the variable names present in a template
     *
     * The names encoded in the Template's VariableInfo objects are not guaranteed to be unique.
     * This method checks a name against those names stored in the VariableInfo objects of a
     * template, and returns a unique value if the name is already in use. A unique name is made by
     * appending an integer value to the input name until a unique version is found.
     *
     * @param name     the name to make unique
     * @param template template containing a series of VarInfo objects, which may already contain
     *                 name.
     * @return a unique version of the input name
     */
    public static String findUniqueName(String name, Template template) {
        boolean keepLooking = true; // assume it is found
        int pass = 0;
        while (keepLooking) {
            boolean uniqueNameFound = true; // assume we have a unique name
            for (VariableInfo vi : template.getVariableInfoList()) {
                if (vi.getName().equals(name)) {
                    pass = pass + 1;
                    name = name + String.valueOf(pass);
                    // well, heck, not a unique name
                    uniqueNameFound = false;
                }
            }

            if (uniqueNameFound) {
                keepLooking = false;
            }
        }
        return name;
    }


    /**
     * Copy a template object
     *
     * Make a copy of a Template object. This is done by serializing the object to json, and then
     * returning a new object based on the serialized version.
     *
     * @param in template to copy
     * @return copy of the input template
     */
    public static Template copy(Template in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(in);
        return mapper.readValue(json, Template.class);
    }

}
