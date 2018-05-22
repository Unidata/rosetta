package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.domain.Metadata;
import edu.ucar.unidata.rosetta.service.exceptions.RosettaDataException;

import java.util.List;

/**
 * @author oxelson@ucar.edu
 */
public interface MetadataManager {

    /**
     * Looks up and retrieves a list of persisted Metadata objects using the given id.
     *
     * @param id    The id of the corresponding Data object.
     * @return      The Metadata object.
     */
    public List<Metadata> lookupMetadata(String id);

    /**
     * Looks up and retrieves a list of persisted Metadata objects using the given id & type.
     *
     * @param id    The id of the corresponding Data object.
     * @param type  The type of the Metadata.
     * @return      The Metadata object.
     */
    public List<Metadata> lookupMetadata(String id,String type);

    /**
     * Persists the information in the given list of metadata objects.
     *
     * @param metadata  The list of Metadata objects to persist.
     */
    public void persistMetadata(List<Metadata> metadata);

    /**
     * Persists the information in the give metadata object.
     *
     * @param metadata  The Metadata object to persist.
     */
    public void persistMetadata(Metadata metadata);

    /**
     * Updated the information corresponding to the given list of metadata objects.
     *
     * @param metadata  The list of metadata objects to update.
     */
    public void updateMetadata(List<Metadata> metadata);

    /**
     * Updated the information corresponding to the given metadata object.
     *
     * @param metadata  The metadata object to update.
     */
    public void updateMetadata(Metadata metadata);

    /**
     * Deletes the persisted metadata information using the given id.
     *
     * @param id  The id of the metadata information to delete.
     */
    public void deleteMetadata(String id);

    /**
     * Deletes the persisted metadata object information using the given id & type.
     *
     * @param id The id of the metadata information to delete.
     * @param type  The type of the metadata information to delete.
     */
    public void deleteMetadata(String id, String type);

    /**
     * Parses a string of metadata into Metadata objects and places them into a list.
     *
     * @param goryStringOfMetadata  The string of metadata sent from the client-side.
     * @param id The id of the corresponding Data object to which the metadata belongs.
     * @return  A list containing Metadata objects.
     */
    public List<Metadata> parseVariableMetadata(String goryStringOfMetadata, String id);


    /**
     * Populates metadata objects from the user input provided and places the objects into a list.
     *
     * @param metadata  The metadata inputted by the user.
     * @param id    The id of the Data object to which this metadata corresponds.
     * @return  A list containing Metadata objects.
     * @throws RosettaDataException  If unable to populate the metadata object by reflection.
     */
    public List<Metadata> parseGeneralMetadata(GeneralMetadata metadata, String id) throws RosettaDataException;

    /**
     * Creates a string version of metadata used by client side.
     *
     * @param metadataList  The parsed metadata.
     * @return  The string version of the metadata used by client side.
     */
    public String getStringFromParsedVariableMetadata(List<Metadata> metadataList);

    /**
     * Retrieves the persisted metadata associated with the given id & type.
     * Creates and returns string version of the metadata used by client side.
     *
     * @param id    The id of the metadata.
     * @param type  The metadata type.
     * @return  The string version of the metadata used by client side.
     */
    public String getMetadataStringForClient(String id, String type);
}
