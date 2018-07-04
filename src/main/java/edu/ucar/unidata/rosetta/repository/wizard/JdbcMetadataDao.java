package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.Metadata;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of a metadata DAO.
 *
 * @author oxelson@ucar.edu
 */
public class JdbcMetadataDao extends JdbcDaoSupport implements MetadataDao {

    protected static Logger logger = Logger.getLogger(JdbcMetadataDao.class);

    private SimpleJdbcInsert insertActor;

    /**
     * Looks up and retrieves a list of persisted Metadata objects using the given id.
     *
     * @param id    The id of the corresponding Data object.
     * @return      The Metadata object.
     * @throws DataRetrievalFailureException  If unable to lookup Metadata with the given id.
     */
    public List<Metadata> lookupMetadata(String id) throws DataRetrievalFailureException {
        String sql = "SELECT * FROM metadata WHERE id = ?";
        List<Metadata> metadata = getJdbcTemplate().query(sql, new JdbcMetadataDao.MetadataMapper(), id);
        if (metadata.isEmpty()) {
            String message = "Unable to find persisted metadata corresponding to id " + id;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        }
        return metadata;
    }

    /**
     * Looks up and retrieves a list of persisted Metadata objects using the given id & type.
     *
     * @param id    The id of the corresponding Data object.
     * @param type  The type of the Metadata.
     * @return      The Metadata object.
     * @throws DataRetrievalFailureException  If unable to lookup Metadata with the given id & type.
     */
    @Override
    public List<Metadata> lookupMetadata(String id,String type) throws DataRetrievalFailureException {
        String sql = "SELECT * FROM metadata WHERE id = ? AND type = ?";
        List<Metadata> metadata = getJdbcTemplate().query(sql, new JdbcMetadataDao.MetadataMapper(), id, type);
        if (metadata.isEmpty()) {
            String message = "Unable to find persisted metadata corresponding to id " + id + " and type " + type;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        }
        return metadata;
    }

    /**
     * Persists the information in the given list of metadata objects.
     *
     * @param metadataList  The list of Metadata objects to persist.
     * @throws DataRetrievalFailureException  If unable to persist the Metadata objects.
     */
    public void persistMetadata(List<Metadata> metadataList) throws DataRetrievalFailureException {
        for (Metadata metadata : metadataList)
            persistMetadata(metadata);

    }

    /**
     * Persists the information in the give metadata object.
     *
     * @param metadata  The Metadata object to persist.
     * @throws DataRetrievalFailureException  If unable to persist the Metadata object.
     */
    public void persistMetadata(Metadata metadata) throws DataRetrievalFailureException {
        // Persist the metadata object.
        this.insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("metadata");
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(metadata);
        int rowsAffected = insertActor.execute(parameters);
        if (rowsAffected <= 0) {
            String message = "Unable to persist Metadata object  " + metadata.toString();
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Metadata object persisted " + metadata.toString());
        }

    }

    /**
     * Updated the information corresponding to the given list of metadata objects.
     *
     * @param metadataList  The list of metadata objects to update.
     * @throws DataRetrievalFailureException  If unable to update persisted Metadata objects.
     */
    public void updatePersistedMetadata(List<Metadata> metadataList) throws DataRetrievalFailureException {
        for (Metadata metadata : metadataList)
            updatePersistedMetadata(metadata);
    }

    /**
     * Updated the information corresponding to the given metadata object.
     *
     * @param metadata  The metadata object to update.
     * @throws DataRetrievalFailureException  If unable to update persisted Metadata object.
     */
    public void updatePersistedMetadata(Metadata metadata) throws DataRetrievalFailureException {
        String sql = "UPDATE metadata SET " +
                "type = ?, " +
                "metadataKey = ?, " +
                "metadataValue = ?, " +
                "WHERE id = ?";
        int rowsAffected  = getJdbcTemplate().update(sql, new Object[] {
                // order matters here
                metadata.getType(),
                metadata.getMetadataKey(),
                metadata.getMetadataValue(),
                metadata.getId()
        });
        if (rowsAffected  <= 0) {
            String message ="Unable to update persisted Metadata object " + metadata.toString();
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Updated persisted Metadata object " + metadata.toString());
        }

    }

    /**
     * Deletes the persisted metadata information using the given id.
     *
     * @param id  The id of the metadata information to delete.
     * @throws DataRetrievalFailureException  If unable to delete persisted metadata information.
     */
    public void deletePersistedMetadata(String id) throws DataRetrievalFailureException {
        String sql = "DELETE FROM metadata WHERE id = ?";
        int rowsAffected  = getJdbcTemplate().update(sql, id);
        if (rowsAffected <= 0) {
            String message = "Unable to delete metadata entries corresponding to id " + id;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Deleted metadata entries corresponding to id " + id);
        }
    }

    /**
     * Deletes the persisted metadata object information using the given id & type.
     *
     * @param id The id of the metadata information to delete.
     * @param type  The type of the metadata information to delete.
     * @throws DataRetrievalFailureException  If unable to delete persisted metadata information.
     */
    public void deletePersistedMetadata(String id, String type) throws DataRetrievalFailureException {
        String sql = "DELETE FROM metadata WHERE id = ? AND type =?";
        int rowsAffected  = getJdbcTemplate().update(sql, id);
        if (rowsAffected <= 0) {
            String message = "Unable to delete metadata entries corresponding to id " + id + " and type " + type;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Deleted metadata entries corresponding to id " + id + " and type " + type);
        }
    }


    /**
     * This MetadataMapper only used by JdbcMetadataDao.
     */
    private static class MetadataMapper implements RowMapper<Metadata> {
        /**
         * Maps each row of metadata in the ResultSet to the Metadata object.
         *
         * @param rs  The ResultSet to be mapped.
         * @param rowNum  The number of the current row.
         * @return  The populated Metadata object.
         * @throws SQLException  If an SQLException is encountered getting column values.
         */
        public Metadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            Metadata metadata = new Metadata();
            metadata.setId(rs.getString("id"));
            metadata.setType(rs.getString("type"));
            metadata.setMetadataKey(rs.getString("metadataKey"));
            metadata.setMetadataValue(rs.getString("metadataValue"));
            return metadata;
        }
    }
}
