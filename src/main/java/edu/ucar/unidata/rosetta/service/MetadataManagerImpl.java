package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.domain.Metadata;
import edu.ucar.unidata.rosetta.repository.MetadataDao;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.ArrayList;
import java.util.List;

public class MetadataManagerImpl implements MetadataManager {

    protected static final Logger logger = Logger.getLogger(MetadataManagerImpl.class);

    private MetadataDao metadataDao;

    /**
     * Sets the data access object (DAO) for the Metadata object which will acquire and persist
     * the data passed to it via the methods of this MetadataManager.
     *
     * @param dataDao  The service DAO representing a Metadata object.
     */
    public void setMetadataDao(MetadataDao dataDao) {
        this.metadataDao = dataDao;
    }

    /**
     * Looks up and retrieves a list of persisted Metadata objects using the given id.
     *
     * @param id    The id of the corresponding Data object.
     * @return      The Metadata object.
     */
    @Override
    public List<Metadata> lookupMetadata(String id) {
        return metadataDao.lookupMetadata(id);
    }

    /**
     * Looks up and retrieves a list of persisted Metadata objects using the given id & type.
     *
     * @param id    The id of the corresponding Data object.
     * @param type  The type of the Metadata.
     * @return      The Metadata object.
     */
    @Override
    public List<Metadata> lookupMetadata(String id,String type) {
        return metadataDao.lookupMetadata(id, type);
    }

    /**
     * Persists the information in the given list of metadata objects.
     *
     * @param metadata  The list of Metadata objects to persist.
     */
    @Override
    public void persistMetadata(List<Metadata> metadata) {
        metadataDao.persistMetadata(metadata);
    }

    /**
     * Persists the information in the give metadata object.
     *
     * @param metadata  The Metadata object to persist.
     */
    @Override
    public void persistMetadata(Metadata metadata) {
        metadataDao.persistMetadata(metadata);
    }

    /**
     * Updated the information corresponding to the given list of metadata objects.
     *
     * @param metadata  The list of metadata objects to update.
     */
    @Override
    public void updateMetadata(List<Metadata> metadata) {
        metadataDao.updatePersistedMetadata(metadata);
    }

    /**
     * Updated the information corresponding to the given metadata object.
     *
     * @param metadata  The metadata object to update.
     */
    @Override
    public void updateMetadata(Metadata metadata) {
        metadataDao.updatePersistedMetadata(metadata);
    }

    /**
     * Deletes the persisted metadata information using the given id.
     *
     * @param id  The id of the metadata information to delete.
     */
    @Override
    public void deleteMetadata(String id) {
        metadataDao.deletePersistedMetadata(id);
    }

    /**
     * Deletes the persisted metadata object information using the given id & type.
     *
     * @param id The id of the metadata information to delete.
     * @param type  The type of the metadata information to delete.
     */
    @Override
    public void deleteMetadata(String id, String type) {
        metadataDao.deletePersistedMetadata(id, type);
    }

    /**
     * Parses a string of metadata into Metadata objects and places them into a list.
     *
     * @param goryStringOfMetadata  The string of metadata sent from the client-side.
     * @param id The id of the corresponding Data object to which the metadata belongs.
     * @return  A list containing Metadata objects.
     */
    @Override
    public List<Metadata> parseVariableMetadata(String goryStringOfMetadata, String id) {
        List<Metadata> parsedVariableMetadata = new ArrayList<>();

        String[] keyValuePairs = goryStringOfMetadata.split("<=>");
        for (String pair: keyValuePairs) {
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
     * Creates a string version of metadata used by client side.
     *
     * @param metadataList  The parsed metadata.
     * @return  The string version of the metadata used by client side.
     */
    public String getStringFromParsedVariableMetadata(List<Metadata> metadataList) {
        StringBuilder metadataString = new StringBuilder();
        for (Metadata metadata: metadataList) {
            metadataString.append("<=>");
            metadataString.append(metadata.getMetadataKey());
            metadataString.append("<>");
            metadataString.append(metadata.getMetadataValue());
        }
        return metadataString.delete(0,3).toString();
    }

    /**
     * Retrieves the persisted metadata associated with the given id & type.
     * Creates and returns string version of the metadata used by client side.
     *
     * @param id    The id of the metadata.
     * @param type  The metadata type.
     * @return  The string version of the metadata used by client side.
     */
    public String getMetadataStringForClient(String id, String type) {
        try {
            List<Metadata> metadata = lookupMetadata(id, type);
            return getStringFromParsedVariableMetadata(metadata);
        } catch (DataRetrievalFailureException e) {
            // No data persisted.
            return null;
        }
    }

    public List<Metadata> parseGeneralMetadata(GeneralMetadata metadata, String id) {
        List<Metadata> parsedVariableMetadata = new ArrayList<>();

        String[] keyValuePairs = goryStringOfMetadata.split("<=>");
        for (String pair: keyValuePairs) {
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

}
