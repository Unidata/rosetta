/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import edu.ucar.unidata.rosetta.domain.GlobalMetadata;
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
        String jsonString = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
             jsonString = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.info(e);
        }
        return jsonString;
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

    public static String convertGlobalDataToJson(GlobalMetadata globalMetadata, HashMap<String, String> fileGlobals) {

        String value = globalMetadata.getMetadataValue();

        // We have globals from a file.
        if (fileGlobals != null) {
            if (value.equals("")) {
                value = fileGlobals.get(globalMetadata.getMetadataKey());
            }
        }
        String jsonString =
                "\"" +
                globalMetadata.getMetadataKey() + "__" +
                globalMetadata.getMetadataValueType() + "\":" +
                "\"" + value + "\"";
        return jsonString;
    }

    public static String convertVariableDataToJson(Variable variable) {
        String variableAsJsonString =
                "{" +
                    "\"column\":" + variable.getColumnNumber() +
                    ",\"name\":\"" + variable.getVariableName() + "\"";
        if (variable.getVariableName().equals("do_not_use")) {
            variableAsJsonString = variableAsJsonString +
                    ",\"required\":{}" +
                    ",\"recommended\":{}" +
                    ",\"additional\":{}";
        } else {
            variableAsJsonString = variableAsJsonString +
                    ",\"metadataType\":\"" + variable.getMetadataType() + "\"" +
                    ",\"metadataValueType\":\"" + variable.getMetadataValueType() + "\"";

            if (variable.getMetadataTypeStructure() != null) {
                variableAsJsonString = variableAsJsonString +
                    ",\"metadataTypeStructure\":\"" + variable.getMetadataTypeStructure() + "\"";
            }

            if (variable.getVerticalDirection() != null) {
                variableAsJsonString = variableAsJsonString +
                        ",\"verticalDirection\":\"" + variable.getVerticalDirection() + "\"";
            }

            if (variable.getRequiredMetadata().size() > 0) {
                String requiredJson = convertComplianceLevelDataToJson(variable.getRequiredMetadata());
                variableAsJsonString = variableAsJsonString +
                        ",\"required\":" +  requiredJson;
            }
            if (variable.getRecommendedMetadata().size() > 0) {
                String recommendedJson = convertComplianceLevelDataToJson(variable.getRecommendedMetadata());
                variableAsJsonString = variableAsJsonString +
                        ",\"recommended\":" +  recommendedJson;
            }
            if (variable.getAdditionalMetadata().size() > 0) {
                String additionalJson = convertComplianceLevelDataToJson(variable.getAdditionalMetadata());
                variableAsJsonString = variableAsJsonString +
                        ",\"additional\":" +  additionalJson;
            }
        }
        variableAsJsonString = variableAsJsonString + "}";
        return variableAsJsonString;

    }

    private static String convertComplianceLevelDataToJson(List<VariableMetadata> variableMetadataList) {
        StringBuilder jsonString = new StringBuilder("{");
        for (VariableMetadata variableMetadata : variableMetadataList) {
            jsonString.append("\"").append(variableMetadata.getMetadataKey()).append("\":\"").append(variableMetadata.getMetadataValue()).append("\",");
        }
        jsonString = new StringBuilder(jsonString.substring(0, jsonString.length() - 1));
        jsonString.append("}");
        return jsonString.toString();
    }


    public static List<GlobalMetadata> convertGlobalDataFromJSON(String jsonString) {
        List<GlobalMetadata> globalMetadataObjects = new ArrayList<>();

        // Convert string to JSON object.
        JsonNode jsonVariables = mapStringToJson(jsonString);
        // If there is something to work with.
        if (jsonVariables != null) {
            // Get the elements.
            for (Iterator<Map.Entry<String, JsonNode>> it = jsonVariables.fields(); it.hasNext();) {
                GlobalMetadata globalMetadata = new GlobalMetadata();
                Map.Entry<String, JsonNode> field = it.next();
                String key = field.getKey().replaceAll("\"", "");
                String[] splitted = key.split("__");
                JsonNode value = field.getValue();
                globalMetadata.setMetadataValueType(splitted[2]);
                globalMetadata.setMetadataGroup(splitted[1]);
                globalMetadata.setMetadataKey(splitted[0]);
                globalMetadata.setMetadataValue(value.textValue());
                globalMetadataObjects.add(globalMetadata);
            }
        }
        return globalMetadataObjects;
    }

    public static List<Variable> convertVariableDataFromJSON(String jsonString) {
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
