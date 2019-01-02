/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import edu.ucar.unidata.rosetta.domain.GlobalMetadata;
import edu.ucar.unidata.rosetta.domain.RosettaAttribute;
import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.Variable;
import edu.ucar.unidata.rosetta.domain.VariableInfo;
import edu.ucar.unidata.rosetta.domain.VariableMetadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

/**
 * JSON conversion utilities.
 */
public class JsonUtil {

    protected static final Logger logger = Logger.getLogger(JsonUtil.class);

    /**
     * Converts the data held in a list of VariableMetadata objects into a string of JSON data.
     *
     * @param variableMetadataList  The list of VariableMetadata objects to convert.
     * @return  A string of JSON data gleaned from the list of VariableMetadata objects.
     */
    private static String convertComplianceLevelDataToJson(List<VariableMetadata> variableMetadataList) {
        StringBuilder jsonString = new StringBuilder("{");
        for (VariableMetadata variableMetadata : variableMetadataList) {
            jsonString.append("\"").append(variableMetadata.getMetadataKey()).append("\":\"").append(variableMetadata.getMetadataValue()).append("\",");
        }
        jsonString = new StringBuilder(jsonString.substring(0, jsonString.length() - 1));
        jsonString.append("}");
        return jsonString.toString();
    }

    /**
     * Converts the provided JSON data into a list of RosettaGlobalAttribute objects.
     *
     * @param node  The JSON data containing the global attribute data.
     * @return  A list of RosettaGlobalAttribute objects created from the JSON data.
     */
    private static List<RosettaGlobalAttribute> convertGlobalAttributeFromJson(JsonNode node) {
        List<RosettaGlobalAttribute> globalAttributeObjects = new ArrayList<>();

        // Get an iterator of the nodes.
        for (Iterator<JsonNode> jsonNodeIterator = node.elements(); jsonNodeIterator.hasNext();) {
            RosettaGlobalAttribute rosettaGlobalAttribute = new RosettaGlobalAttribute();
            // Get the elements.
            for (Iterator<Map.Entry<String, JsonNode>> iterator = jsonNodeIterator.next().fields(); iterator.hasNext();) {
                Map.Entry<String, JsonNode> field = iterator.next();
                String key = field.getKey().replaceAll("\"", "");
                JsonNode value = field.getValue();
                if (key.equals("name") && !value.isNull()) {
                    rosettaGlobalAttribute.setName(value.textValue().replaceAll("\"", ""));
                }
                if (key.equals("value") && !value.isNull()) {
                    rosettaGlobalAttribute.setValue(value.textValue().replaceAll("\"", ""));
                }
                if (key.equals("type") && !value.isNull()) {
                    rosettaGlobalAttribute.setType(value.textValue().replaceAll("\"", ""));
                }
                if (key.equals("group") && !value.isNull()) {
                    rosettaGlobalAttribute.setGroup(value.textValue().replaceAll("\"", ""));
                }
            }
            globalAttributeObjects.add(rosettaGlobalAttribute);
        }
        return globalAttributeObjects;
    }

    /**
     * Converts the global metadata held in a JSON string into a list of GlobalMetadata objects.
     *
     * @param jsonString  The string of JSON data containing the global metadata data.
     * @return  A list of GlobalMetadata objects created from the JSON string.
     */
    public static List<GlobalMetadata> convertGlobalDataFromJson(String jsonString) {
        List<GlobalMetadata> globalMetadataObjects = new ArrayList<>();

        if (jsonString != null) {
            // Convert string to JSON object.
            JsonNode jsonVariables = mapStringToJson(jsonString);
            // If there is something to work with.
            if (jsonVariables != null) {
                // Get the elements.
                for (Iterator<Map.Entry<String, JsonNode>> iterator = jsonVariables.fields(); iterator.hasNext(); ) {
                    GlobalMetadata globalMetadata = new GlobalMetadata();
                    Map.Entry<String, JsonNode> field = iterator.next();
                    String key = field.getKey().replaceAll("\"", "");
                    String[] splitted = key.split("__");
                    JsonNode value = field.getValue();
                    globalMetadata.setMetadataGroup(splitted[1]);
                    globalMetadata.setMetadataKey(splitted[0]);
                    globalMetadata.setMetadataValue(value.textValue());
                    globalMetadataObjects.add(globalMetadata);
                }
            }
        }
        return globalMetadataObjects;
    }

    /**
     * Converts the variable data held in a string of JSON data into a list of Variable objects.
     *
     * @param jsonString    The string of JSON data containing the variable data.
     * @return  A list of Variable objects created from the JSON string.
     */
    public static List<Variable> convertVariableDataFromJson(String jsonString) {
        List<Variable> variableObjects = new ArrayList<>();

        if (jsonString != null) {
            // Convert string to JSON object.
            JsonNode jsonVariables = mapStringToJson(jsonString);

            // If there is something to work with.
            if (jsonVariables != null) {
                // Get the elements (variables).
                for (Iterator<JsonNode> iterator = jsonVariables.elements(); iterator.hasNext(); ) {
                    JsonNode jsonVariable = iterator.next();
                    Variable variable = new Variable();
                    // Parse the variable elements.
                    for (Iterator<Map.Entry<String, JsonNode>> it = jsonVariable.fields(); it.hasNext(); ) {
                        Map.Entry<String, JsonNode> field = it.next();
                        String key = field.getKey().replaceAll("\"", "");
                        JsonNode value = field.getValue();
                        if (key.equals("column")) {
                            variable.setColumnNumber(value.intValue());
                        }
                        if (key.equals("name")) {
                            variable.setVariableName(value.textValue());
                            if (value.textValue().equals("DO_NOT_USE")) {
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
                            String type = value.textValue();
                            variable.setMetadataValueType(type);
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
        }
        return variableObjects;
    }

    /**
     * Converts the data held in a Variable object into a string of JSON data.
     *
     * @param variable  The Variable object to convert.
     * @return  A string of JSON data gleaned from the Variable object.
     */
    public static String convertVariableDataToJson(Variable variable) {
        String variableAsJsonString =
                "{" +
                        "\"column\":" + variable.getColumnNumber() +
                        ",\"name\":\"" + variable.getVariableName() + "\"";
        if (variable.getVariableName().equals("DO_NOT_USE")) {
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

    /**
     * Converts the provided JSON data into a list of VariableInfo objects.
     *
     * @param node    The JSON data
     * @return  A list of VariableInfo objects.
     */
    private static List<VariableInfo> convertVariableInfoDataFromJson(JsonNode node) {
        List<VariableInfo> variableInfoObjects = new ArrayList<>();

        // Get an iterator of the nodes.
        for (Iterator<JsonNode> jsonNodeIterator = node.elements(); jsonNodeIterator.hasNext();) {
            VariableInfo variableInfo = new VariableInfo();
            // Get the elements.
            for (Iterator<Map.Entry<String, JsonNode>> iterator = jsonNodeIterator.next().fields(); iterator.hasNext();) {
                Map.Entry<String, JsonNode> field = iterator.next();
                String key = field.getKey().replaceAll("\"", "");
                JsonNode value = field.getValue();
                // Set the column Id if not null.
                if (key.equals("columnId") && !value.isNull()) {
                    variableInfo.setColumnId(value.intValue());
                }
                // Set the name is not null.
                if (key.equals("name") && !value.isNull()) {
                    variableInfo.setName(value.textValue());
                }
                // If the rosetta control metadata is not null.
                if (key.equals("rosettaControlMetadata") && !value.isNull()) {
                    List<RosettaAttribute> rosettaControlMetadata = populateRosettaAttributeDataFromJson(value);
                    variableInfo.setRosettaControlMetadata(rosettaControlMetadata);
                }
                // If the variableMetadata is not null.
                if (key.equals("variableMetadata") && !value.isNull()) {
                    List<RosettaAttribute> variableMetadata = populateRosettaAttributeDataFromJson(value);
                    variableInfo.setVariableMetadata(variableMetadata);
                }
            }
            variableInfoObjects.add(variableInfo);
        }
        return variableInfoObjects;
    }

    /**
     * Converts a JSON string from template file into a Template object.
     *
     * @param jsonString  The JSON string containing the template data from the JSOn file.
     * @return  The Template object.
     * @throws IllegalAccessException If unable to invoke the setter method on the Template object.
     * @throws NoSuchMethodException If the Template object doesn't contain the required setter method.
     * @throws InvocationTargetException  If unable to locate the target method to invoke.
     */
    public static Template mapJsonToTemplateObject(String jsonString) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        Template template = new Template();

        if (jsonString != null) {
            // Convert string to JSON object.
            JsonNode jsonVariables = mapStringToJson(jsonString);
            // If there is data to work with.
            if (jsonVariables != null) {
                // Get the elements.
                for (Iterator<Map.Entry<String, JsonNode>> iterator = jsonVariables.fields(); iterator.hasNext(); ) {
                    Map.Entry<String, JsonNode> field = iterator.next();

                    // Get the key.
                    String key = field.getKey().replaceAll("\"", "");

                    // Get the data value.
                    JsonNode value = field.getValue();

                    // One of the more complex data types.
                    if (key.equals("globalMetadata") && !value.isNull()) {
                        List<RosettaGlobalAttribute> globalAttributes = convertGlobalAttributeFromJson(value);
                        if (!globalAttributes.isEmpty()) {
                            template.setGlobalMetadata(globalAttributes);
                        }
                    } else if (key.equals("headerLineNumbers") && !value.isNull()) {
                        List<Integer> headerLineNumbers = new ArrayList<>();
                        for (Iterator<JsonNode> headerLineNumberIterator = value.elements(); headerLineNumberIterator.hasNext();) {
                            headerLineNumbers.add(headerLineNumberIterator.next().asInt());
                        }
                        template.setHeaderLineNumbers(headerLineNumbers);
                    } else if (key.equals("variableInfoList") && !value.isNull()) {
                        List<VariableInfo> variableInfo = convertVariableInfoDataFromJson(value);
                        if (!variableInfo.isEmpty()) {
                            template.setVariableInfoList(variableInfo);
                        }
                    } else {
                        // Set the params that contain actual data (ignore null values).
                        if (!value.isNull()) {
                            // Using reflection to populate the Template object.

                            // The setter method.
                            String setterMethodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);

                            // Get the setter method.
                            Method setter = template.getClass().getMethod(setterMethodName, String.class);
                            setter.invoke(template, value.textValue().replaceAll("\"", ""));
                        }
                    }
                }
            }
        }
        return template;
    }

    /**
     * Maps a Java Object to a JSON String.
     *
     * @param obj The object to convert.
     * @return The Object as a JSOn string.
     */
    public static String mapObjectToJson(Object obj){
        String jsonString = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonString = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error(e);
        }
        return jsonString;
    }

    /**
     * Converts a string of JSON data to JsonNode object.
     *
     * @param jsonString  The string-ified JSON data.
     * @return  The data as a JsonNode.
     */
    private static JsonNode mapStringToJson(String jsonString) {
        JsonNode actualObj = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            actualObj = mapper.readTree(jsonString);
        } catch (IOException e) {
            logger.error(e);
        }
        return actualObj;
    }

    /**
     * Creates and populates a list of VariableMetadata objects from the given JsonNode and assigns the given compliance level value.
     *
     * @param node  The JsonNode containing the variable data.
     * @param complianceLevel   The compliance level (required, recommended, or additional).
     * @return  A list of VariableMetadata objects.
     */
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

    /**
     * Converts the provided JSON data into a list of RosettaAttribute objects.
     *
     * @param node  The JSON data.
     * @return  A list of RosettaAttribute objects.
     */
    private static List<RosettaAttribute> populateRosettaAttributeDataFromJson(JsonNode node)  {
        List<RosettaAttribute> rosettaAttributeObjects = new ArrayList<>();

        // Get an iterator of the nodes.
        for (Iterator<JsonNode> jsonNodeIterator = node.elements(); jsonNodeIterator.hasNext();) {
            RosettaAttribute rosettaAttribute = new RosettaAttribute();

            // Toggle these values when we are dealing with metadataType or metadataValueType.
            boolean isMetadataType = false;
            boolean isMetadataValueType = false;
            boolean isCoordinateVariableType = false;

            // Get the elements.
            for (Iterator<Map.Entry<String, JsonNode>> iterator = jsonNodeIterator.next().fields(); iterator.hasNext(); ) {
                Map.Entry<String, JsonNode> field = iterator.next();
                String key = field.getKey().replaceAll("\"", "");
                JsonNode value = field.getValue();

                if (key.equals("name") && !value.isNull()) {
                    // If metadataType.
                    if (value.textValue().equals("coordinateVariable")) {
                        isMetadataType = true;
                    }
                    // If metadataValueType.
                    if (value.textValue().equals("type")) {
                        isMetadataValueType = true;
                    }
                    // If coordinateVariableType.
                    if (value.textValue().equals("coordinateVariableType")) {
                        isCoordinateVariableType = true;
                    }

                    rosettaAttribute.setName(value.textValue().replaceAll("\"", ""));
                }
                if (key.equals("value") && !value.isNull()) {
                    String attributeValue = value.textValue().replaceAll("\"", "");

                    // The metadataType value being set.  Change to appropriate value for wizard.
                    if (isMetadataType)  {
                        attributeValue = attributeValue.toLowerCase(); // Just in case we get non-standard template data.
                        if (attributeValue.equals("true")) {
                            attributeValue = "coordinate";
                        } else {
                            attributeValue = "non-coordinate";
                        }
                    }

                    // The metadataValueType value being set.  Change to appropriate value for wizard.
                    if (isMetadataValueType)  {
                        attributeValue = attributeValue.toLowerCase(); // Just in case we get non-standard template data.
                        if (attributeValue.equals("string")) {
                            attributeValue = "Text";
                        }
                        if (attributeValue.equals("integer") || attributeValue.equals("int")) {
                            attributeValue = "Integer";
                        }
                        if (attributeValue.equals("double")) {
                            attributeValue = "Float";
                        }
                    }

                    // The coordinateVariableType value being set.  Change to appropriate value for wizard.
                    if (isCoordinateVariableType)  {
                        attributeValue = attributeValue.toLowerCase(); // Just in case we get non-standard template data.
                        if (attributeValue.equals("dateonly")) {
                            attributeValue = "dateOnly";
                        }
                        if (attributeValue.equals("timeonly")) {
                            attributeValue = "timeOnly";
                        }
                        if (attributeValue.equals("relativedate")) {
                            attributeValue = "relativeDate";
                        }
                        if (attributeValue.equals("fulldatetime")) {
                            attributeValue = "fullDateTime";
                        };
                    }

                    rosettaAttribute.setValue(attributeValue);
                }
                if (key.equals("type") && !value.isNull()) {
                    rosettaAttribute.setType(value.textValue().replaceAll("\"", ""));
                }
            }
            rosettaAttributeObjects.add(rosettaAttribute);
        }
        return rosettaAttributeObjects;
    }
}
