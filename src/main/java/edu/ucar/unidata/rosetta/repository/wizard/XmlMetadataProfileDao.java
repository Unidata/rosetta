package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.MetadataProfile;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlMetadataProfileDao implements MetadataProfileDao {

  private static final Logger logger = Logger.getLogger(XmlMetadataProfileDao.class);

  public void getMetadataProfileByType(String type) {
    try {
      logger.info("GETTING CF FILE");
      // Get the index file to access a list of available resources.
      Resource r = new ClassPathResource("resources/MpsProfilesRosetta/CF.xml");
      File file = r.getFile();
      fetchProfileData(file);
    } catch (IOException e) {
      logger.info("IO exception accessing metadata profile resource file: " + e);
    }
  }

  private void fetchProfileData(File file) {
    logger.info("FILE INFO: " + file.getAbsolutePath());
    List<Map<String, List<String>>> resources = new ArrayList<>();
    try {
      // Need a DocumentBuilder to work with XML files.
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

      // Parse file as an XML document and return a new DOM Document object.
      Document doc = dBuilder.parse(file);
      doc.getDocumentElement().normalize();

      // Get all elements in document with a given tag name of 'type'.
      NodeList resourceNodeList = doc.getElementsByTagName("ROW");

      // Process all the ROW nodes.
      for (int i = 0; i < resourceNodeList.getLength(); i++) {
        // Create a new MetadataProfile object.
        MetadataProfile metadataProfile = new MetadataProfile();

        // Get the attributes for the node.
        Node row = resourceNodeList.item(i);
        NamedNodeMap attributes = row.getAttributes();

        for (int j = 0; j < attributes.getLength(); j++) {
          // Get the name of the attribute.
          Node attribute = attributes.item(j);
          String attributeName = attribute.getNodeName();

          // Kludge until we can get this fixed!
          if (attributeName.equals("attributename")) {
            attributeName = "attributeName";
          }

          // Create a setter method name from the attribute name.
          String setterMethodName = "set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
          logger.info(setterMethodName);

          String attributeValue = attribute.getNodeValue();
          logger.info(attributeValue);

          // Access the actual setter method using the setter method name.
          Method setter = (Method) metadataProfile.getClass().getMethod(setterMethodName, String.class);

          //setter.invoke(metadataProfile, );

        }
/*
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
        */
      }

    } catch (DOMException | ParserConfigurationException | SAXException | IOException | NoSuchMethodException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error("Unable to load resources: " + errors);
    }

  }

}
