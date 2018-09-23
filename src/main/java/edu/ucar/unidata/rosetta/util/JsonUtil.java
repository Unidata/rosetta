/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import edu.ucar.unidata.rosetta.domain.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.Variable;
import edu.ucar.unidata.rosetta.domain.VariableMetadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;


import org.apache.log4j.Logger;

/**
 * JSON conversion utilities.
 *
 * @author sarms@ucar.edu
 * @author oxelson@ucar.edu
 */
public class JsonUtil {

    protected static final Logger logger = Logger.getLogger(JsonUtil.class);
    private String name;


    public JsonUtil(String jsonFile) {
        this.setName(jsonFile);
        File jsonDir = new File(FilenameUtils.getFullPath(jsonFile));
        if (!jsonDir.exists()) {
            jsonDir.mkdirs();
        }
    }


    /**
     * Maps a Java Object to a JSON String.
     *
     * @param obj The object to convert.
     * @return The Object as a JSOn string.
     * @throws JsonProcessingException If unable to map Object to JSON string.
     */
    public static String mapObjectToJSON(Object obj) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }


    public static JsonNode mapStringToJson(String jsonString) {
        JsonNode actualObj = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            actualObj = mapper.readTree(jsonString);
        } catch (IOException e) {
            logger.error(e);
        }
        return actualObj;
    }

    public static List<Variable> convertFromJSON(String jsonString) {
        List<Variable> variableObjects = new ArrayList<>();

        // Convert string to JSON object.
        JsonNode jsonVariables = mapStringToJson(jsonString);

        // If there is something to work with.
        if (jsonVariables != null) {
            // Get the elements (variables).
            for (Iterator<JsonNode> iterator = jsonVariables.elements(); iterator.hasNext(); ) {
                JsonNode jsonVariable = iterator.next();
                Variable variable = new Variable();
                // Parse the variable elements.
                for (Iterator<Map.Entry<String, JsonNode>> it = jsonVariable.fields(); it.hasNext();) {
                    Map.Entry<String, JsonNode> field = it.next();
                    String key = field.getKey().replaceAll("\"", "");
                    JsonNode value = field.getValue();

                    if (key.equals("column")) {
                        variable.setColumnNumber(value.intValue());
                    }
                    if (key.equals("name")) {
                        variable.setVariableName(value.textValue());
                        if (value.textValue().equals("do_not_use")) {
                            break;
                        }
                    }
                    if (key.equals("metadataType")) {
                        variable.setMetadataType(value.textValue());
                    }
                    if (key.equals("metadataTypeStructure")) {
                        variable.setMetadataTypeStructure(value.textValue());
                    }
                    if (key.equals("verticalDirection")) {
                        variable.setVerticalDirection(value.textValue());
                    }
                    if (key.equals("metadataValueType")) {
                        variable.setMetadataValueType(value.textValue());
                    }

                    if (key.equals("required")) {
                        if (value.size() > 0) {
                            List<VariableMetadata> required = populateComplianceLevelData(value, "required");
                            variable.setRequiredMetadata(required);
                        }
                    }
                    if (key.equals("recommended")) {
                        if (value.size() > 0) {
                            List<VariableMetadata> recommended = populateComplianceLevelData(value, "recommended");
                            variable.setRecommendedMetadata(recommended);
                        }
                    }
                    if (key.equals("additional")) {
                        if (value.size() > 0) {
                            List<VariableMetadata> additional = populateComplianceLevelData(value, "additional");
                            variable.setAdditionalMetadata(additional);
                        }
                    }

                }
                variableObjects.add(variable);
            }
        }
        return variableObjects;
    }

    private static List<VariableMetadata> populateComplianceLevelData(JsonNode node, String complianceLevel) {
        List<VariableMetadata> variableMetadataValues = new ArrayList<>();
        for (Iterator<Map.Entry<String, JsonNode>> iter = node.fields(); iter.hasNext();) {
            VariableMetadata variableMetadata = new VariableMetadata();
            Map.Entry<String, JsonNode> f = iter.next();
            String k = f.getKey();
            JsonNode v = f.getValue();
            variableMetadata.setComplianceLevel(complianceLevel);
            variableMetadata.setMetadataKey(k);
            variableMetadata.setMetadataValue(v.textValue());
            variableMetadataValues.add(variableMetadata);
        }
        return variableMetadataValues;
    }


    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public JSONObject ssHashMapToJson(HashMap<String, HashMap<String, String>> jsonHashMap) {

        JSONObject json = null;
        json = new JSONObject(jsonHashMap);

        return json;
    }

    public JSONObject strToJson(String jsonStr) {
        JSONParser jsonParser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) jsonParser.parse(jsonStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return json;
    }


    public void writeJsonStrToFile(String jsonStr) {
        JSONObject json = this.strToJson(jsonStr);
        this.writeJsonToFile(json);
    }

    public void writeJsonToFile(JSONObject json) {
        FileWriter jsonOutFw = null;
        try {
            jsonOutFw = new FileWriter(getName());
            jsonOutFw.write(json.toJSONString());
            jsonOutFw.flush();
            jsonOutFw.close();
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
