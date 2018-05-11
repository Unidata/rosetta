package edu.ucar.unidata.rosetta.service;

import java.io.File;
import java.io.IOException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

/**
 * Service for fetching rosetta resources from WEB_INF/classes/resources.
 * The Resources are XML containing data used both server-side and client-side
 * (in the later case, the server passes the resource properties to the view
 * via the model attribute).
 */
public class ResourceManagerImpl implements ResourceManager {

    private static final Logger logger = Logger.getLogger(ResourceManagerImpl.class);

    /**
     * Accesses the resources index file on disk and loads the resources into
     * a Map<String, Object> for access.
     *
     * @return A Map<String, Object> containing the resources to be added to the model.
     */
    public Map<String, Object> loadResources() {
        Map<String, Object> model = new HashMap<>();
        String fileName = "";
        String type = "";
        try {
            // Get the index file to access a list of available resources.
            Resource r = new ClassPathResource("resources/index.xml");
            File file = r.getFile();

            // Load 'menu' of available resources.
            List<Map> resources = fetchResources(file, "resource");

            // Now get all of the resources from menu.
            for (Map resource : resources) {
                Object ob = resource.get("fileName");
                if (ob instanceof String) {
                    fileName = (String) ob;
                }
                ob = resource.get("type");
                if (ob instanceof String) {
                    type = (String) ob;
                }
                r = new ClassPathResource("resources/" + fileName);
                file = r.getFile();
                // Load all the items in the resource file.
                List<Map> resourceList = fetchResources(file, type);
                model.put(type + "s", resourceList);
            }
        } catch (IOException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logger.error("Unable to load resources: " + errors);
        }
        return model;
    }

    /**
     * Use the Spring Resource Interface to pull "configuration" information
     * from the file system and inject that data into the Model to be used in
     * the View.
     *
     * Unfortunately, this method of fetching only works for simple XML files
     * in which the elements do not contain attributes.
     * TODO: refactor so more inclusive with XML file types.
     *
     * @param file The Resource file in the web application class path to use.
     * @param type The element(s) to find in the file.
     * @return A List of resource items, which are Maps containing the xml data.
     */
    public List<Map> fetchResources(File file, String type) {
        List<Map> resources = new ArrayList<>();
        try {
            // Need a DocumentBuilder to work with XML files.
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            // Parse file as an XML document and return a new DOM Document object.
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            // Get all elements in document with a given tag name of 'type'.
            NodeList resourceNodeList = doc.getElementsByTagName(type);

            // Process all the 'type' nodes.
            for (int i = 0; i < resourceNodeList.getLength(); i++) {
                // Get the child elements of the 'type' node.
                NodeList resourceChildNodes = resourceNodeList.item(i).getChildNodes();

                // Store the data pulled from the child elements in a map.
                Map<String, Object> resource = new HashMap<>(resourceChildNodes.getLength());

                // Process the child nodes.
                for (int x = 0; x < resourceChildNodes.getLength(); x++) {
                    Node n = resourceChildNodes.item(x);

                    // Child is an element node  (see above TODO).
                    if (n.getNodeType() == Node.ELEMENT_NODE) {

                        // We may want to have multiple values for one key.
                        if (resource.containsKey(n.getNodeName())) {

                            // Get the existing value and assign to a placeholder.
                            Object ob = resource.get(n.getNodeName());
                            resource.remove(n.getNodeName());

                            // The map value will be held in a list.
                            List<String> values = new ArrayList<>();

                            // The value of the key is merely a string.
                            if (ob instanceof String)
                                values.add((String) ob);

                            // Existing value is already a list; reassign values to ob.
                            if (ob instanceof List)
                                values = (List)ob;

                            // Add the newest value.
                            values.add(n.getTextContent());

                            // Reassign to
                            resource.put(n.getNodeName(), values);
                        } else {
                            // Add to map with node name as key and content as value.
                            resource.put(n.getNodeName(), n.getTextContent());
                        }
                    }
                }
                // Add our Map to our list.
                resources.add(i, resource);
            }
        } catch (DOMException| ParserConfigurationException | SAXException | IOException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logger.error("Unable to load resources: " + errors);
        }
        return resources;
    }
  }
