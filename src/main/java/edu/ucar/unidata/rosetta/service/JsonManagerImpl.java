package edu.ucar.unidata.rosetta.service;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for handling Java object to JSON conversion (and visa versa).
 */
public class JsonManagerImpl implements JsonManager {

    protected static Logger logger = Logger.getLogger(JsonManagerImpl.class);

    /**
     * Converts a Java object to a JSON string.
     *
     * @param o  The Java object to convert.
     * @return   A JSON string (or nothing if unsuccessful).
     */
    public String convertToJsonString(Object o) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            logger.error("Unable to convert object to JSON string: " + e);
            return "";
        }
    }
}
