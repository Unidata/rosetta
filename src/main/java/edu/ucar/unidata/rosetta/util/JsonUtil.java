package edu.ucar.unidata.rosetta.util;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**

 */
public class JsonUtil {

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

    public JSONObject ssHashMapToJson(Map<String, HashMap> jsonHashMap) {

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
