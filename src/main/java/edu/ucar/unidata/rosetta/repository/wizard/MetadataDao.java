package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.Metadata;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.List;

/**
 * The data access object representing metadata.
 *
 * @author oxelson@ucar.edu
 */
public interface MetadataDao {

  /**
   * Looks up and retrieves a list of persisted Metadata objects using the given id.
   *
   * @param id The id of the corresponding Data object.
   * @return The Metadata object.
   * @throws DataRetrievalFailureException If unable to lookup Metadata with the given id.
   */
  public List<Metadata> lookupMetadata(String id) throws DataRetrievalFailureException;

  /**
   * Looks up and retrieves a list of persisted Metadata objects using the given id & type.
   *
   * @param id The id of the corresponding Data object.
   * @param type The type of the Metadata.
   * @return The Metadata object.
   * @throws DataRetrievalFailureException If unable to lookup Metadata with the given id & type.
   */
  public List<Metadata> lookupMetadata(String id, String type) throws DataRetrievalFailureException;

  /**
   * Persists the information in the given list of metadata objects.
   *
   * @param metadata The list of Metadata objects to persist.
   * @throws DataRetrievalFailureException If unable to persist the Metadata objects.
   */
  public void persistMetadata(List<Metadata> metadata) throws DataRetrievalFailureException;

  /**
   * Persists the information in the give metadata object.
   *
   * @param metadata The Metadata object to persist.
   * @throws DataRetrievalFailureException If unable to persist the Metadata object.
   */
  public void persistMetadata(Metadata metadata) throws DataRetrievalFailureException;

  /**
   * Updated the information corresponding to the given list of metadata objects.
   *
   * @param metadata The list of metadata objects to update.
   * @throws DataRetrievalFailureException If unable to update persisted Metadata objects.
   */
  public void updatePersistedMetadata(List<Metadata> metadata) throws DataRetrievalFailureException;

  /**
   * Updated the information corresponding to the given metadata object.
   *
   * @param metadata The metadata object to update.
   * @throws DataRetrievalFailureException If unable to update persisted Metadata object.
   */
  public void updatePersistedMetadata(Metadata metadata) throws DataRetrievalFailureException;

  /**
   * Deletes the persisted metadata information using the given id.
   *
   * @param id The id of the metadata information to delete.
   * @throws DataRetrievalFailureException If unable to delete persisted metadata information.
   */
  public void deletePersistedMetadata(String id) throws DataRetrievalFailureException;

  /**
   * Deletes the persisted metadata object information using the given id & type.
   *
   * @param id The id of the metadata information to delete.
   * @param type The type of the metadata information to delete.
   * @throws DataRetrievalFailureException If unable to delete persisted metadata information.
   */
  public void deletePersistedMetadata(String id, String type) throws DataRetrievalFailureException;

}
