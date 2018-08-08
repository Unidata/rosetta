package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.domain.Metadata;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import java.util.List;
import java.util.Map;

/**
 * Service for handling metadata in its various permutations.
 *
 * @author oxelson@ucar.edu
 */
public interface MetadataManager {

  /**
   * Deletes the persisted metadata information using the given id.
   *
   * @param id The id of the metadata information to delete.
   */
  public void deletePersistedMetadata(String id);

  /**
   * Deletes the persisted metadata object information using the given id & type.
   *
   * @param id The id of the metadata information to delete.
   * @param type The type of the metadata information to delete.
   */
  public void deletePersistedMetadata(String id, String type);

  /**
   * Converts metadata to a format useful for AsciiFile (custom file type) for netCDF conversion.
   * TODO: refactor to remove need for AsciiFile.
   *
   * @param id The unique ID corresponding to the metadata to find.
   * @param type The type of metadata (general or variable).
   * @return The metadata in Map<String, String> form.
   */
  public Map<String, String> getGeneralMetadataMap(String id, String type);

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
  public GeneralMetadata getMetadataFromKnownFile(String filePath, String fileType,
      GeneralMetadata metadata) throws RosettaDataException;

  /**
   * Retrieves the persisted metadata associated with the given id & type. Creates and returns
   * string version of the metadata used by client side.
   *
   * @param id The id of the metadata.
   * @param type The metadata type.
   * @return The string version of the metadata used by client side.
   */
  public String getMetadataStringForClient(String id, String type);

  /**
   * Creates a string version of metadata used by client side.
   *
   * @param metadataList The parsed metadata.
   * @return The string version of the metadata used by client side.
   */
  public String getStringFromParsedVariableMetadata(List<Metadata> metadataList);

  /**
   * Converts metadata to a format useful for AsciiFile (custom file type) for netCDF conversion.
   * TODO: refactor to remove need for AsciiFile.
   *
   * @param id The unique ID corresponding to the metadata to find.
   * @param type The type of metadata (general or variable).
   * @return The metadata in Map<String, Map<String,String>> form.
   */
  public Map<String, Map<String, String>> getVariableMetadataMap(String id, String type);

  /**
   * Converts metadata to a format useful for AsciiFile (custom file type) for netCDF conversion.
   * TODO: refactor to remove need for AsciiFile.
   *
   * @param id The unique ID corresponding to the metadata to find.
   * @param type The type of metadata (general or variable).
   * @return The metadata in Map<String,String> form.
   */
  public Map<String, String> getVariableNameMap(String id, String type);

  /**
   * Looks up and retrieves a list of persisted Metadata objects using the given id.
   *
   * @param id The id of the corresponding Data object.
   * @return The Metadata object.
   */
  public List<Metadata> lookupPersistedMetadata(String id);

  /**
   * Looks up and retrieves a list of persisted Metadata objects using the given id & type.
   *
   * @param id The id of the corresponding Data object.
   * @param type The type of the Metadata.
   * @return The Metadata object.
   */
  public List<Metadata> lookupPersistedMetadata(String id, String type);

  /**
   * Populates metadata objects from the user input provided and places the objects into a list.
   *
   * @param metadata The metadata inputted by the user.
   * @param id The id of the Data object to which this metadata corresponds.
   * @return A list containing Metadata objects.
   * @throws RosettaDataException If unable to populate the metadata object by reflection.
   */
  public List<Metadata> parseGeneralMetadata(GeneralMetadata metadata, String id)
      throws RosettaDataException;

  /**
   * Parses a string of metadata into Metadata objects and places them into a list.
   *
   * @param goryStringOfMetadata The string of metadata sent from the client-side.
   * @param id The id of the corresponding Data object to which the metadata belongs.
   * @return A list containing Metadata objects.
   */
  public List<Metadata> parseVariableMetadata(String goryStringOfMetadata, String id);

  /**
   * Persists the information in the given list of metadata objects.
   *
   * @param metadata The list of Metadata objects to persist.
   */
  public void persistMetadata(List<Metadata> metadata);

  /**
   * Persists the information in the give metadata object.
   *
   * @param metadata The Metadata object to persist.
   */
  public void persistMetadata(Metadata metadata);

  /**
   * Updated the information corresponding to the given list of metadata objects.
   *
   * @param metadata The list of metadata objects to update.
   */
  public void updatePersistedMetadata(List<Metadata> metadata);

  /**
   * Updated the information corresponding to the given metadata object.
   *
   * @param metadata The metadata object to update.
   */
  public void updatePersistedMetadata(Metadata metadata);
}
