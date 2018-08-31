/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.converters.known.etuff.TagUniversalFileFormat;
import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.domain.Metadata;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.repository.wizard.MetadataDao;
import edu.ucar.unidata.rosetta.repository.wizard.MetadataProfileDao;

import java.beans.Statement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Implements MetadataManager functionality.
 *
 * @author oxelson@ucar.edu
 */
public class MetadataManagerImpl implements MetadataManager {

  protected static final Logger logger = Logger.getLogger(MetadataManagerImpl.class);

  private MetadataDao metadataDao;
  private MetadataProfileDao metadataProfileDao;


  public void getMetadataProfileData() {
    metadataProfileDao.getMetadataProfileByType("CF");
  }


  /**
   * Deletes the persisted metadata information using the given id.
   *
   * @param id The id of the metadata information to delete.
   */
  @Override
  public void deletePersistedMetadata(String id) {
    metadataDao.deletePersistedMetadata(id);
  }

  /**
   * Deletes the persisted metadata object information using the given id & type.
   *
   * @param id The id of the metadata information to delete.
   * @param type The type of the metadata information to delete.
   */
  @Override
  public void deletePersistedMetadata(String id, String type) {
    metadataDao.deletePersistedMetadata(id, type);
  }

  /**
   * Converts metadata to a format useful for AsciiFile (custom file type) for netCDF conversion.
   * TODO: refactor to remove need for AsciiFile.
   *
   * @param id The unique ID corresponding to the metadata to find.
   * @param type The type of metadata (general or variable).
   * @return The metadata in Map<String, String> form.
   */
  @Override
  public Map<String, String> getGeneralMetadataMap(String id, String type) {
    List<Metadata> metadataList = lookupPersistedMetadata(id, type);
    // Hack to convert metadata to a format useful for AsciiFile for netcdf conversion.
    Map<String, String> generalMetadataMap = new HashMap<>();
    for (Metadata metadata : metadataList) {
      generalMetadataMap.put(metadata.getMetadataKey(), metadata.getMetadataValue());
    }
    return generalMetadataMap;
  }

  /**
   * Pulls the general metadata from a data known file and populates the provided GeneralMetadata
   * object. Uses reflection to perform the population step. If the data file type is a custom file
   * (not a known type) then an empty, non-populated GeneralMetadata object is returned.
   *
   * @param filePath The path to the data file which may contain the metadata we need.
   * @param fileType The data file type.
   * @param metadata The GeneralMetadata object to populate.
   * @return The GeneralMetadata object to populated with the general metadata.
   * @throws RosettaDataException If unable to populate the GeneralMetadata object.
   */
  @Override
  public GeneralMetadata getMetadataFromKnownFile(String filePath, String fileType,
      GeneralMetadata metadata) throws RosettaDataException {

    // Only process known file types.
    if (!fileType.equals("Custom_File_Type")) {
      Map<String, String> globalMetadata = new HashMap<>();

      // Get the metadata from the file by calling the relevant converter.

      if (fileType.equals("eTuff")) {
        // Tag-base archive flat file.
        TagUniversalFileFormat tuffConverter = new TagUniversalFileFormat();
        tuffConverter.parse(filePath);
        globalMetadata = tuffConverter.getGlobalMetadata();
      }

      for (String key : globalMetadata.keySet()) {
        try {
          // Make setter method string.
          String setMethod = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
          for (Method method : metadata.getClass().getDeclaredMethods()) {
            if (method.getName().equals(setMethod)) {
              Statement statement = new Statement(metadata, setMethod,
                  new Object[]{globalMetadata.get(key).trim().replace("\"", "")});
              statement.execute();
            }
          }
        } catch (Exception e) {
                /*
                NOTE: code in the try block actually throws a bunch of different exceptions, including
                java.lang.Exception itself.  Hence, the use catch of the generic Exception class to
                catch them all (otherwise I normally would not catch with just java.lang.Exception).
                */
          throw new RosettaDataException("Unable to populate data object by reflection: " + e);
        }
      }
    }
    return metadata;
  }

  /**
   * Retrieves the persisted metadata associated with the given id & type. Creates and returns
   * string version of the metadata used by client side.
   *
   * @param id The id of the metadata.
   * @param type The metadata type.
   * @return The string version of the metadata used by client side.
   */
  @Override
  public String getMetadataStringForClient(String id, String type) {
    try {
      List<Metadata> metadata = lookupPersistedMetadata(id, type);
      return getStringFromParsedVariableMetadata(metadata);
    } catch (DataRetrievalFailureException e) {
      // No data persisted.
      return null;
    }
  }

  /**
   * Creates a string version of metadata used by client side.
   *
   * @param metadataList The parsed metadata.
   * @return The string version of the metadata used by client side.
   */
  @Override
  public String getStringFromParsedVariableMetadata(List<Metadata> metadataList) {
    StringBuilder metadataString = new StringBuilder();
    for (Metadata metadata : metadataList) {
      metadataString.append("<=>");
      metadataString.append(metadata.getMetadataKey());
      metadataString.append("<>");
      metadataString.append(metadata.getMetadataValue());
    }
    return metadataString.delete(0, 3).toString();
  }

  /**
   * Converts metadata to a format useful for AsciiFile (custom file type) for netCDF conversion.
   * TODO: refactor to do remove need for AsciiFile.
   *
   * @param id The unique ID corresponding to the metadata to find.
   * @param type The type of metadata (general or variable).
   * @return The metadata in Map<String, Map<String,String>> form.
   */
  @Override
  public Map<String, Map<String, String>> getVariableMetadataMap(String id, String type) {
    List<Metadata> metadataList = lookupPersistedMetadata(id, type);
    // Hack to convert metadata to a format useful for AsciiFile for netcdf conversion.
    Map<String, Map<String, String>> variableMetadataMap = new HashMap<>();
    for (Metadata metadata : metadataList) {
      Map<String, String> metadataMapping = new HashMap<>();
      // Omit the 'do not use' entries.
      if (!metadata.getMetadataValue().equals("Do Not Use")) {
        // Only look at the metadata entries
        if (metadata.getMetadataKey().contains("Metadata")) {
          String[] metadataValues = metadata.getMetadataValue().split(",");

          for (int i = 0; i < metadataValues.length; i++) {
            String[] pairs = metadataValues[i].split(":");

            if (pairs.length == 1) {
              metadataMapping.put(pairs[0], "");
            } else {
              metadataMapping.put(pairs[0], pairs[1]);
            }
          }
        }
      }
      variableMetadataMap.put(metadata.getMetadataKey().replace("Metadata", ""), metadataMapping);
    }
    return variableMetadataMap;
  }

  /**
   * Converts metadata to a format useful for AsciiFile (custom file type) for netCDF conversion.
   * TODO: refactor to do remove need for AsciiFile.
   *
   * @param id The unique ID corresponding to the metadata to find.
   * @param type The type of metadata (general or variable).
   * @return The metadata in Map<String,String> form.
   */
  @Override
  public Map<String, String> getVariableNameMap(String id, String type) {
    List<Metadata> metadataList = lookupPersistedMetadata(id, type);
    // Hack to convert metadata to a format useful for AsciiFile for netcdf conversion.
    Map<String, String> variableNameMap = new HashMap<>();
    for (Metadata metadata : metadataList) {
      // Omit the 'do not use' entries.
      if (!metadata.getMetadataValue().equals("Do Not Use")) {
        // Omit the metadata entries and just grab the names
        if (!metadata.getMetadataKey().contains("Metadata")) {
          variableNameMap.put(metadata.getMetadataKey(), metadata.getMetadataValue());
        }
      }
    }
    return variableNameMap;
  }

  /**
   * Looks up and retrieves a list of persisted Metadata objects using the given id.
   *
   * @param id The id of the corresponding Data object.
   * @return The Metadata object.
   */
  @Override
  public List<Metadata> lookupPersistedMetadata(String id) {
    return metadataDao.lookupMetadata(id);
  }

  /**
   * Looks up and retrieves a list of persisted Metadata objects using the given id & type.
   *
   * @param id The id of the corresponding Data object.
   * @param type The type of the Metadata.
   * @return The Metadata object.
   */
  @Override
  public List<Metadata> lookupPersistedMetadata(String id, String type) {
    return metadataDao.lookupMetadata(id, type);
  }

  /**
   * Populates metadata objects from the user input provided and places the objects into a list.
   *
   * @param metadata The metadata inputted by the user.
   * @param id The id of the Data object to which this metadata corresponds.
   * @return A list containing Metadata objects.
   * @throws RosettaDataException If unable to populate the metadata object by reflection.
   */
  @Override
  public List<Metadata> parseGeneralMetadata(GeneralMetadata metadata, String id)
      throws RosettaDataException {
    List<Metadata> parsedGeneralMetadata = new ArrayList<>();

    try {
      for (Method method : metadata.getClass().getDeclaredMethods()) {

        if (Modifier.isPublic(method.getModifiers())
            && method.getParameterTypes().length == 0
            && method.getReturnType() != void.class
            && (method.getName().startsWith("get"))
            ) {

          Object value = method.invoke(metadata);

          if (value != null) {
            if (value instanceof String) {

              if (!"".equals(value)) {
                Metadata m = new Metadata();
                m.setId(id);
                m.setType("general");

                Statement keyStatement = new Statement(m, "setMetadataKey",
                    new Object[]{method.getName().replaceFirst("get", "").toLowerCase()});
                keyStatement.execute();
                Statement valStatement = new Statement(m, "setMetadataValue", new Object[]{value});
                valStatement.execute();
                parsedGeneralMetadata.add(m);
              }
            }
          }
        }
      }
    } catch (Exception e) {
             /*
             NOTE: code in the try block actually throws a bunch of different exceptions, including
             java.lang.Exception itself.  Hence, the use catch of the generic Exception class to
             catch them all (otherwise I normally would not catch with just java.lang.Exception).
              */
      throw new RosettaDataException("Unable to populate data object by reflection: " + e);
    }
    return parsedGeneralMetadata;
  }

  /**
   * Parses a string of metadata into Metadata objects and places them into a list.
   *
   * @param goryStringOfMetadata The string of metadata sent from the client-side.
   * @param id The id of the corresponding Data object to which the metadata belongs.
   * @return A list containing Metadata objects.
   */
  @Override
  public List<Metadata> parseVariableMetadata(String goryStringOfMetadata, String id) {
    List<Metadata> parsedVariableMetadata = new ArrayList<>();

    String[] keyValuePairs = goryStringOfMetadata.split("<=>");
    for (String pair : keyValuePairs) {
      String[] metadata = pair.split("<>");
      Metadata m = new Metadata();
      m.setId(id);
      m.setType("variable");
      m.setMetadataKey(metadata[0]);
      m.setMetadataValue(metadata[1]);
      parsedVariableMetadata.add(m);
    }
    return parsedVariableMetadata;
  }

  /**
   * Persists the information in the given list of metadata objects.
   *
   * @param metadata The list of Metadata objects to persist.
   */
  @Override
  public void persistMetadata(List<Metadata> metadata) {
    metadataDao.persistMetadata(metadata);
  }

  /**
   * Persists the information in the give metadata object.
   *
   * @param metadata The Metadata object to persist.
   */
  @Override
  public void persistMetadata(Metadata metadata) {
    metadataDao.persistMetadata(metadata);
  }

  /**
   * Sets the data access object (DAO) for the MetadataProfile object.
   *
   * @param metadataProfileDao The service DAO representing a MetadataProfile object.
   */
  public void setMetadataProfileDao(MetadataProfileDao metadataProfileDao) {
    this.metadataProfileDao = metadataProfileDao;
  }

  /**
   * Sets the data access object (DAO) for the Metadata object.
   *
   * @param dataDao The service DAO representing a Metadata object.
   */
  public void setMetadataDao(MetadataDao dataDao) {
    this.metadataDao = dataDao;
  }

  /**
   * Updated the information corresponding to the given list of metadata objects.
   *
   * @param metadata The list of metadata objects to update.
   */
  @Override
  public void updatePersistedMetadata(List<Metadata> metadata) {
    metadataDao.updatePersistedMetadata(metadata);
  }

  /**
   * Updated the information corresponding to the given metadata object.
   *
   * @param metadata The metadata object to update.
   */
  @Override
  public void updatePersistedMetadata(Metadata metadata) {
    metadataDao.updatePersistedMetadata(metadata);
  }
}
