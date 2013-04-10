package edu.ucar.unidata.pzhta.service;

import java.io.File;

import java.util.List;
import java.util.Map;

/**
 * Service for fetching pzhta resources from the file system. 
 */
public interface ResourceManager  {
   
    /**
     * Accesses the resources index file on disk and loads the resources into
     * a List<Map> for access.  Looks 
     * 
     * @return  Map<String, Object> containing the resources.
     */
    public Map<String, Object> loadResources();

    /**
     * Use the Spring Resource Interface to pull "configuration" information 
     * from the file system and inject that data into the Model to be used in the View.
     * 
     * @param file  The Resource file in the web application class path to use.
     * @param type  The element(s) to find in the file.
     * @return  A List of resource items, which are Maps containing the xml data.
     */
    public List<Map> fetchResources(File file, String type);
}
