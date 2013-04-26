package edu.ucar.unidata.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileWriter;
import java.io.IOException;

/**

 */
public class JsonUtil {

    private String name;

    public JsonUtil(String jsonFile) {
        this.setName(jsonFile);
    }

    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name =name;
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
        JSONParser jsonParser = new JSONParser();
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
