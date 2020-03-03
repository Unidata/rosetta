/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */
package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.GlobalMetadata;
import java.util.List;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * The data access object representing a GlobalMetadata object.
 *
 * @author oxelson@ucar.edu
 */
public interface GlobalMetadataDao {

  /**
   * Looks up and retrieves a list of persisted Variable objects using the given id.
   *
   * @param wizardDataId The id of the corresponding WizardData object.
   * @return The Variable object.
   */
  public List<GlobalMetadata> lookupGlobalMetadata(String wizardDataId);

  /**
   * Persists the information in the given list of globalMetadata objects.
   *
   * @param wizardDataId The id of the corresponding WizardData object.
   * @param globalMetadata The list of GlobalMetadata objects to persist.
   * @throws DataRetrievalFailureException If unable to persist the GlobalMetadata objects.
   */
  public void persistGlobalMetadata(String wizardDataId, List<GlobalMetadata> globalMetadata)
      throws DataRetrievalFailureException;

  /**
   * Persists the information in the given globalMetadata object.
   *
   * @param wizardDataId The id of the corresponding WizardData object.
   * @param globalMetadata The GlobalMetadata object to persist.
   * @throws DataRetrievalFailureException If unable to persist the GlobalMetadata object.
   */
  public void persistGlobalMetadata(String wizardDataId, GlobalMetadata globalMetadata)
      throws DataRetrievalFailureException;

  /**
   * Updated the information corresponding to the given list of globalMetadata objects.
   *
   * @param wizardDataId The id of the corresponding WizardData object.
   * @param globalMetadata The list of globalMetadata objects to update.
   * @throws DataRetrievalFailureException If unable to update persisted GlobalMetadata objects.
   */
  public void updatePersistedGlobalMetadata(String wizardDataId, List<GlobalMetadata> globalMetadata)
      throws DataRetrievalFailureException;

  /**
   * Updated the information corresponding to the given globalMetadata object.
   *
   * @param wizardDataId The id of the corresponding WizardData object.
   * @param globalMetadata The globalMetadata object to update.
   * @throws DataRetrievalFailureException If unable to update persisted GlobalMetadata object.
   */
  public void updatePersistedGlobalMetadata(String wizardDataId, GlobalMetadata globalMetadata)
      throws DataRetrievalFailureException;

  /**
   * Deletes the persisted list of globalMetadata information using the given id.
   *
   * @param globalMetadata The list of globalMetadata objects to update.
   * @throws DataRetrievalFailureException If unable to delete persisted globalMetadata information.
   */
  public void deletePersistedGlobalMetadata(List<GlobalMetadata> globalMetadata) throws DataRetrievalFailureException;

  /**
   * Deletes the persisted globalMetadata object information using the given id.
   *
   * @param globalMetadata The globalMetadata object to delete.
   * @throws DataRetrievalFailureException If unable to delete persisted globalMetadata information.
   */
  public void deletePersistedGlobalMetadata(GlobalMetadata globalMetadata) throws DataRetrievalFailureException;
}
