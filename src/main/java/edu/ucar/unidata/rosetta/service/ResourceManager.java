package edu.ucar.unidata.rosetta.service;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Service for fetching rosetta resources from WEB_INF/classes/resources.
 */
public interface ResourceManager {

    /**
     * Accesses the resources index file on disk and loads the resources into
     * a Map<String, Object> for access.
     *
     * @return A Map<String, Object> containing the resources to be added to the model.
     */
    public Map<String, Object> loadResources();

    /**
     * Use the Spring Resource Interface to pull "configuration" information
     * from the file system and inject that data into the Model to be used in
     * the View.
     *
     * @param file The Resource file in the web application class path to use.
     * @param type The element(s) to find in the file.
     * @return A List of resource items, which are Maps containing the xml data.
     */
    public List<Map> fetchResources(File file, String type);
}
