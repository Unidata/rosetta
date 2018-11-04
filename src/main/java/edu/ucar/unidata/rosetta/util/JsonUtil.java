/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import edu.ucar.unidata.rosetta.domain.GlobalMetadata;
import edu.ucar.unidata.rosetta.domain.RosettaAttribute;
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
     * Converts the global metadata held in a string of JSON data into a list of GlobalMetadata objects.
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

    /**
     * Converts the data in the provided JSON string into a list of VariableInfo objects.
     *
     * @param jsonString    The JSOn string.
     * @return  A list of VariableInfo objects.
     * @throws IllegalAccessException If unable to invoke the setter method on the RosettaAttribute object.
     * @throws NoSuchMethodException If the RosettaAttribute object doesn't contain the required setter method.
     * @throws InvocationTargetException  If unable to locate the target method to invoke.
     */
    private static List<VariableInfo> convertVariableInfoDataFromJson(String jsonString) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<VariableInfo> variableInfoObjects = new ArrayList<>();
        if (jsonString != null) {
            // Convert string to JSON object.
            JsonNode jsonVariables = mapStringToJson(jsonString);
            // If there is something to work with.
            if (jsonVariables != null) {
                // Get the elements.
                for (Iterator<Map.Entry<String, JsonNode>> iterator = jsonVariables.fields(); iterator.hasNext(); ) {
                    VariableInfo variableInfo = new VariableInfo();
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
                        List<RosettaAttribute> rosettaControlMetadata = populateRosettaAttributeDataFromJson(value.textValue());
                        variableInfo.setRosettaControlMetadata(rosettaControlMetadata);
                    }
                    // If the variableMetadata is not null.
                    if (key.equals("variableMetadata") && !value.isNull()) {
                        List<RosettaAttribute> variableMetadata = populateRosettaAttributeDataFromJson(value.textValue());
                        variableInfo.setRosettaControlMetadata(variableMetadata);
                    }
                    variableInfoObjects.add(variableInfo);
                }
            }
        }
        return variableInfoObjects;
    }

    /**
     * Converts a JSON string of template data into a Template object.
     *
     * @param jsonString  The JSON string containing the template data.
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

                    // Using reflection to populate the Template object.

                    // The setter method.
                    String setterMethodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);

                    // Get the setter method.
                    Method setter = (Method) template.getClass().getMethod(setterMethodName, String.class);

                    // Get the data value.
                    JsonNode value = field.getValue();

                    // Set the params that contain actual data.
                    if (value.isTextual()) {
                        setter.invoke(template, value.textValue().replaceAll("\"", ""));
                    } else {                    logger.info(field);

                        // Either value is null or one of the more complex data types.
                        if (key.equals("globalMetadataData") && !value.isNull()) {
                            List<GlobalMetadata> globalMetadata = convertGlobalDataFromJson(value.textValue());
                            if (!globalMetadata.isEmpty()) {
                                setter.invoke(template, globalMetadata);
                            }
                        }
                        if (key.equals("headerLineNumbers") && !value.isNull()) {
                            int [] headerLineNumbersAsInts = Stream.of(value.textValue().replaceAll("\"", "").split(",")).mapToInt(Integer::parseInt).toArray();
                            List<Integer> headerLineNumbers = Arrays.stream(headerLineNumbersAsInts).boxed().collect(Collectors.toList());
                            setter.invoke(template, headerLineNumbers);
                        }
                        if (key.equals("variableInfoList") && !value.isNull()) {
                            List<VariableInfo> variableInfo = convertVariableInfoDataFromJson(value.textValue());
                            if (!variableInfo.isEmpty()) {
                                setter.invoke(template, variableInfo);
                            }
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
     * @throws JsonProcessingException If unable to map Object to JSON string.
     */
    public static String mapObjectToJson(Object obj) throws JsonProcessingException {
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
     * Converts the data in the provided JSON string to a list of RosettaAttribute objects.
     *
     * @param jsonString  The JSON string.
     * @return  A list of RosettaAttribute objects.
     * @throws IllegalAccessException If unable to invoke the setter method on the RosettaAttribute object.
     * @throws NoSuchMethodException If the RosettaAttribute object doesn't contain the required setter method.
     * @throws InvocationTargetException  If unable to locate the target method to invoke.
     */
    private static List<RosettaAttribute> populateRosettaAttributeDataFromJson(String jsonString) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException  {
        List<RosettaAttribute> rosettaAttributeObjects = new ArrayList<>();

        if (jsonString != null) {
            // Convert string to JSON object.
            JsonNode jsonVariables = mapStringToJson(jsonString);

            // If there is something to work with.
            if (jsonVariables != null) {
                // Get the elements (variables).
                for (Iterator<Map.Entry<String, JsonNode>> iterator = jsonVariables.fields(); iterator.hasNext(); ) {
                    RosettaAttribute rosettaAttribute = new RosettaAttribute();
                    Map.Entry<String, JsonNode> field = iterator.next();
                    String key = field.getKey().replaceAll("\"", "");
                    JsonNode value = field.getValue();

                    // Using reflection to populate the Template object.

                    // The setter method.
                    String setterMethodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);

                    // Get the setter method.
                    Method setter = (Method) rosettaAttribute.getClass().getMethod(setterMethodName, String.class);

                    if (value.isTextual()) {
                        // A well-formed template will always have a value to add here...
                        setter.invoke(rosettaAttribute, value.textValue().replaceAll("\"", ""));
                    }
                    rosettaAttributeObjects.add(rosettaAttribute);
                }
            }
        }
        return rosettaAttributeObjects;
    }
}
