package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.GeneralMetadata;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.List;

/**
 * The data access object representing metadata.
 *
 * @author oxelson@ucar.edu
 */
public interface MetadataDao {

    /**
     * Looks up and retrieves a list of persisted GeneralMetadata objects using the given id.
     *
     * @param id    The id of the corresponding Data object.
     * @return      The GeneralMetadata object.
     * @throws DataRetrievalFailureException  If unable to lookup GeneralMetadata with the given id.
     */
    public List<GeneralMetadata> lookupMetadata(String id) throws DataRetrievalFailureException;

    /**
     * Looks up and retrieves a list of persisted GeneralMetadata objects using the given id & type.
     *
     * @param id    The id of the corresponding Data object.
     * @param type  The type of the GeneralMetadata.
     * @return      The GeneralMetadata object.
     * @throws DataRetrievalFailureException  If unable to lookup GeneralMetadata with the given id & type.
     */
    public List<GeneralMetadata> lookupMetadata(String id, String type) throws DataRetrievalFailureException;

    /**
     * Persists the information in the given list of metadata objects.
     *
     * @param metadata  The list of GeneralMetadata objects to persist.
     * @throws DataRetrievalFailureException  If unable to persist the GeneralMetadata objects.
     */
    public void persistMetadata(List<GeneralMetadata> metadata) throws DataRetrievalFailureException;
        
    /**
     * Persists the information in the give metadata object.
     *
     * @param metadata  The GeneralMetadata object to persist.
     * @throws DataRetrievalFailureException  If unable to persist the GeneralMetadata object.
     */
    public void persistMetadata(GeneralMetadata metadata) throws DataRetrievalFailureException;

    /**
     * Updated the information corresponding to the given list of metadata objects.
     *
     * @param metadata  The list of metadata objects to update.
     * @throws DataRetrievalFailureException  If unable to update persisted GeneralMetadata objects.
     */
    public void updatePersistedMetadata(List<GeneralMetadata> metadata) throws DataRetrievalFailureException;

    /**
     * Updated the information corresponding to the given metadata object.
     *
     * @param metadata  The metadata object to update.
     * @throws DataRetrievalFailureException  If unable to update persisted GeneralMetadata object.
     */
    public void updatePersistedMetadata(GeneralMetadata metadata) throws DataRetrievalFailureException;

    /**
     * Deletes the persisted metadata information using the given id.
     *
     * @param id  The id of the metadata information to delete.
     * @throws DataRetrievalFailureException  If unable to delete persisted metadata information.
     */
    public void deletePersistedMetadata(String id) throws DataRetrievalFailureException;

    /**
     * Deletes the persisted metadata object information using the given id & type.
     *
     * @param id The id of the metadata information to delete.
     * @param type  The type of the metadata information to delete.
     * @throws DataRetrievalFailureException  If unable to delete persisted metadata information.
     */
    public void deletePersistedMetadata(String id, String type) throws DataRetrievalFailureException;

}
