package edu.ucar.unidata.pzhta.service;

import org.apache.log4j.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Service for fetching pzhta resources from the file system. 
 */
public class ResourceManagerImpl implements ResourceManager  {

    protected static Logger logger = Logger.getLogger(ResourceManagerImpl.class);
   
    /**
     * Accesses the resources index file on disk and loads the resources into
     * a List<Map> for access.  Looks 
     * 
     * @return  A Map<String, Object> containing the resources to be added to the model.
     */
    public Map<String, Object> loadResources() {     
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            Resource r = new ClassPathResource("resources/index.xml");
            File file = r.getFile();
            List<Map> resources = fetchResources(file, "resource");
            Iterator<Map> iterator = resources.iterator();
	        while (iterator.hasNext()) {
		        Map<String, String> resource = iterator.next();
                String fileName = resource.get("fileName");
                String type = resource.get("type");
                r = new ClassPathResource("resources/" + fileName);
                file = r.getFile();
                List<Map> resourceList = fetchResources(file, type); 
                model.put(type + "s", resourceList); 
	        } 
        } catch (IOException e) { 
            logger.error(e.getMessage());
        }
        return model;        
    }

    /**
     * Use the Spring Resource Interface to pull "configuration" information 
     * from the file system and inject that data into the Model to be used in the View.
     * 
     * @param file  The Resource file in the web application class path to use.
     * @param type  The element(s) to find in the file.
     * @return  A List of resource items, which are Maps containing the xml data.
     */
    public List<Map> fetchResources(File file, String type) {
        List<Map> resources = new ArrayList<Map>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList resourceNodeList = doc.getElementsByTagName(type);
            for (int i = 0; i < resourceNodeList.getLength(); i++) {            
 		        NodeList resourceChildNodes = resourceNodeList.item(i).getChildNodes();
                Map<String, Object> resource = new HashMap<String, Object>(resourceChildNodes.getLength());
                for (int x = 0; x < resourceChildNodes.getLength(); x++) {
                    Node n = resourceChildNodes.item(x);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        if (resource.containsKey(n.getNodeName())) { 
                            Object ob = resource.get(n.getNodeName());     
                            resource.remove(n.getNodeName());
                            List<String> values = new ArrayList<String>();
                            if (ob instanceof String) {
                                values.add((String) ob);
                            } 
                            if (ob instanceof List) {
                                List<String> l = (List)ob;
                                Iterator<String> iterator = l.iterator();
	                            while (iterator.hasNext()) {
		                            String val = iterator.next();
                                    values.add(val);
	                            }
                            } 
                            values.add(n.getTextContent());
                            resource.put(n.getNodeName(), values);
                        } else {
                            resource.put(n.getNodeName(), n.getTextContent()); 
                        }
                    }    
		        }
                resources.add(i, resource);
	        }
        } catch (DOMException e) { 
            logger.error(e.getMessage());
        } catch (ParserConfigurationException e) { 
            logger.error(e.getMessage());
        } catch (SAXException e) { 
            logger.error(e.getMessage());
        } catch (IOException e) { 
            logger.error(e.getMessage());
        } 
        return resources;
    }
}
