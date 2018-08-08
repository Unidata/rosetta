/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.MetadataProfile;

import org.apache.commons.io.FilenameUtils;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Implementation for metadata profiles DAO.
 */
public class XmlMetadataProfileDao implements MetadataProfileDao {

  private static final Logger logger = Logger.getLogger(XmlMetadataProfileDao.class);

  public List<MetadataProfile> getMetadataProfileByType(String metadataProfileType) {
    List<MetadataProfile> metadataProfiles = new ArrayList<>();
    try {

      // Get the metadat profile file.
      Resource r = new ClassPathResource(FilenameUtils.concat("resources/MpsProfilesRosetta/", metadataProfileType + ".xml"));
      File file = r.getFile();

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
          if (attributeName.equals("attibutename")) {
            attributeName = "attributeName";
          }

          // Create a setter method name from the attribute name.
          String setterMethodName = "set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);

          // Get attribute value.
          String attributeValue = attribute.getNodeValue();

          // Access the actual setter method using the setter method name.
          Method setter = metadataProfile.getClass().getMethod(setterMethodName, String.class);

          setter.invoke(metadataProfile, attributeValue);

        }
        // Add our MetadataProfile object to the list.
        metadataProfiles.add(i, metadataProfile);
      }

    } catch (DOMException | ParserConfigurationException | SAXException | IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error("Unable to load resources: " + errors);
    }
    return metadataProfiles;
  }

}
