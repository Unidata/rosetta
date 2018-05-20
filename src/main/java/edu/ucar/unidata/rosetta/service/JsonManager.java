package edu.ucar.unidata.rosetta.service;

/**
 * Service for handling Java object to JSON conversion (and visa versa).
 */
public interface JsonManager {

    /**
     * Converts a Java object to a JSON string.
     *
     * @param o  The Java object to convert.
     * @return   A JSON string.
     */
    public String convertToJsonString(Object o);

}

