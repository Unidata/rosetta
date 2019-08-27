/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */
package edu.ucar.unidata.rosetta.init.resources;

import edu.ucar.unidata.rosetta.domain.MetadataProfile;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class MetadataProfileLoader {

  private static final Logger logger = Logger.getLogger(MetadataProfileLoader.class);
  private static final ArrayList<String> PROFILE_DIRS = new ArrayList<>();

  MetadataProfileLoader() {
    // Load the available metadata profiles.
    PROFILE_DIRS.add("resources/DsgProfiles/");
    PROFILE_DIRS.add("resources/MpsProfilesRosetta/");
  }

  public List<MetadataProfile> loadMetadataProfiles() throws RosettaDataException {
    List<MetadataProfile> metadataProfiles = new ArrayList<>();
    try {

      List<String> profileFiles = new ArrayList<>();

      // Access the available metadata profiles by directories.
      for (String resourceDir : PROFILE_DIRS) {
        Resource r = new ClassPathResource(resourceDir);
        File profiles = r.getFile();
        String[] files = profiles.list();
        for (String f : files) {
          if (resourceDir.contains("Mps")) {
            // Ignore any files that are not metadata profile files (e.g., SQL files).
            if (f.contains("MpsProfileOutput_")) {
              profileFiles.add(FilenameUtils.concat(resourceDir, f));
            }
          } else {
            profileFiles.add(FilenameUtils.concat(resourceDir, f));
          }
        }
      }
      // Load the metadata profiles
      for (String profileFile : profileFiles) {
        metadataProfiles.addAll(loadMetadataProfiles(profileFile));
      }
    } catch (IOException | NullPointerException e) {
      throw new RosettaDataException("Unable to load resources: " + e);
    }
    return metadataProfiles;
  }

  /**
   * Cracks open the XML file corresponding to the given file name, parses the data found using
   * the ROW name, and populates/returns a list of MetadataProfile objects using the data.
   *
   * @param fileName The name of the XML file to use.
   * @return A list of MetadataProfile objects created from the persisted metadata profile data.
   */
  private List<MetadataProfile> loadMetadataProfiles(String fileName) {
    List<MetadataProfile> profiles = new ArrayList<>();
    try {
      // Get the metadata profile file.
      Resource r = new ClassPathResource(fileName);
      File file = r.getFile();

      // Need a DocumentBuilder to work with XML files.
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

      // Parse file as an XML document and return a new DOM Document object.
      Document doc = dBuilder.parse(file);
      doc.getDocumentElement().normalize();

      // Get all elements in document with a given tag name.
      NodeList ignoreNodeList = doc.getElementsByTagName("ROW");

      // Process all the nodes.
      for (int i = 0; i < ignoreNodeList.getLength(); i++) {
        // Create a new MetadataProfile object.
        MetadataProfile metadataProfile = new MetadataProfile();

        // Get the attributes for the node.
        Node row = ignoreNodeList.item(i);
        NamedNodeMap attributes = row.getAttributes();

        for (int j = 0; j < attributes.getLength(); j++) {
          // Get the name of the attribute.
          Node attribute = attributes.item(j);
          String attributeName = attribute.getNodeName();

          // Create a setter method name from the attribute name.
          String setterMethodName = "set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);

          // Get attribute value.
          String attributeValue = attribute.getNodeValue();

          try {
            // Access the actual setter method using the setter method name.
            Method setter = metadataProfile.getClass().getMethod(setterMethodName, String.class);
            setter.invoke(metadataProfile, attributeValue);
          } catch (NoSuchMethodException e) {
            // Don't add that the attribute to the MetadataProfile object.
            continue;
          }
        }
        // Add our MetadataProfile object to the list.
        profiles.add(i, metadataProfile);
      }

    } catch (DOMException | ParserConfigurationException | SAXException | IOException | IllegalAccessException
        | InvocationTargetException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error("Unable to load resources: " + errors);
    }
    return profiles;
  }

}
