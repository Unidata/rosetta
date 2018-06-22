package edu.ucar.unidata.rosetta.init.resources;

import edu.ucar.unidata.rosetta.domain.resources.*;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;

import java.io.File;
import java.io.IOException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
 * The resources are XML containing data used both server-side and client-side
 * (in the later case, the server passes the resources properties to the view
 * via the model attribute).
 *
 * @author oxelson@ucar.edu
 */
public class ResourceManager {

    private static final Logger logger = Logger.getLogger(ResourceManager.class);

    /**
     * Accesses the resources index file on disk and loads the resources into
     * a Map<String, Object> for access.
     *
     * @return A Map<String, Object> containing the resources to be added to the model.
     * @throws RosettaDataException  If unable to retrieve resources.
     */
    public List<RosettaResource> loadResources() throws RosettaDataException {
        List<RosettaResource> resources = new ArrayList<>();
        try {
            // Get the index file to access a list of available resources.
            Resource r = new ClassPathResource("resources/index.xml");
            File file = r.getFile();
            String fileName = "";
            String type = "";
            // Load 'menu' of available resources.
            List<Map<String, String>> availableResources = fetchAvailableResources(file);
            // Get the resource items
            for (Map<String, String> resourcesMap : availableResources) {
                // Get the resource file names and the main element from the map.
                Iterator<Map.Entry<String, String>> it = resourcesMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> pair = it.next();
                    String key = pair.getKey();
                    if (key.equals("fileName"))
                        fileName = pair.getValue();
                    else
                        type = pair.getValue();
                    it.remove(); // Avoids a ConcurrentModificationException
                }
                // Get the resource data.
                r = new ClassPathResource("resources/" + fileName);
                file = r.getFile();
                List<Map<String, List<String>>> resourceData = fetchResource(file, type);

                // Loop through the resource list and populate the relevant RosettaResource objects.
                for (Map<String, List<String>> resourceMap : resourceData) {

                    // Figure out which type of RosettaResource object we are going to populate.
                    String classToInstantiate = "edu.ucar.unidata.rosetta.domain.resources." + type.substring(0, 1).toUpperCase() + type.substring(1);
                    Object rosettaResource = Class.forName(classToInstantiate).getDeclaredConstructor().newInstance();

                    // Populate the RosettaResource object using generics.
                    ResourcePopulator<RosettaResource> populator = new ResourcePopulator<RosettaResource>();
                    populator.setRosettaResource((RosettaResource) rosettaResource);
                    resources.addAll(populator.populate(resourceMap));
                }
            }

        } catch (IOException | IllegalAccessException | NoSuchMethodException | InstantiationException | ClassNotFoundException | InvocationTargetException e) {
            throw new RosettaDataException("Unable to load resources: " + e);
        }
        return resources;
    }


    /**
     * Use the Spring ResourceManager Interface to pull "configuration" information
     * from the file system and inject that data into the Model to be used in
     * the View.
     *
     * Unfortunately, this method of fetching only works for simple XML files
     * in which the elements do not contain attributes.
     *
     * @param file The Resource file in the web application class path to use.
     * @param type The element(s) to find in the file.
     * @return A List of resources items, which are Maps containing the xml data.
     */
    private List<Map<String, List<String>>> fetchResource(File file, String type) {
        List<Map<String, List<String>>> resources = new ArrayList<>();
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
                Map<String, List<String>> resource = new HashMap<>(resourceChildNodes.getLength());

                // Process the child nodes.
                for (int x = 0; x < resourceChildNodes.getLength(); x++) {
                    Node n = resourceChildNodes.item(x);

                    // The map value will be held in a list.
                    List<String> values = new ArrayList<>();

                    // Child is an element node.
                    if (n.getNodeType() == Node.ELEMENT_NODE) {

                        // We may want to have multiple values for one key.
                        if (resource.containsKey(n.getNodeName())) {

                            // Get the existing value and assign to a placeholder.
                            values = resource.get(n.getNodeName());
                            resource.remove(n.getNodeName());
                        }

                        // Add the newest value.
                        values.add(n.getTextContent());

                        // Add List to Map
                        resource.put(n.getNodeName(), values);
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

    private List<Map<String, String>> fetchAvailableResources(File file) {
        List<Map<String, String>> resources = new ArrayList<>();
        try {
            // Need a DocumentBuilder to work with XML files.
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            // Parse file as an XML document and return a new DOM Document object.
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            // Get all elements in document with a given tag name of 'resource'.
            NodeList resourceNodeList = doc.getElementsByTagName("resource");

            // Process all the 'resource' nodes.
            for (int i = 0; i < resourceNodeList.getLength(); i++) {
                // Get the child elements of the 'type' node.
                NodeList resourceChildNodes = resourceNodeList.item(i).getChildNodes();

                // Store the data pulled from the child elements in a map.
                Map<String, String> resource = new HashMap<>(resourceChildNodes.getLength());

                // Process the child nodes.
                for (int x = 0; x < resourceChildNodes.getLength(); x++) {
                    Node n = resourceChildNodes.item(x);

                    // Child is an element node.
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        // Add to Map
                        resource.put(n.getNodeName(), n.getTextContent());
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


    public String getCommunity(String platform) {
        return "foo";
    }

}
