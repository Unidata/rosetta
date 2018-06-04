package edu.ucar.unidata.rosetta.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * JSON conversion utilities.
 *
 * @author sarms@ucar.edu
 * @author oxelson@ucar.edu
 */
public class JsonUtil {

    /**
     * Maps a Java Object to a JSON String.
     *
     * @param obj   The object to convert.
     * @return      The Object as a JSOn string.
     * @throws JsonProcessingException  If unable to map Object to JSON string.
     */
    public static String mapObjectToJSON(Object obj) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }





    private String name;

    public JsonUtil(String jsonFile) {
        this.setName(jsonFile);
        File jsonDir = new File(FilenameUtils.getFullPath(jsonFile));
        if (!jsonDir.exists()) {
            jsonDir.mkdirs();
        }
    }

    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public JSONObject ssHashMapToJson(HashMap<String, HashMap<String,String>> jsonHashMap) {

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
            e.printStackTrace();
        }
    }
}
