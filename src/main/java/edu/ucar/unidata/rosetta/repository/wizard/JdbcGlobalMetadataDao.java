/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.GlobalMetadata;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of a global metadata DAO.
 *
 * @author oxelson@ucar.edu
 */
public class JdbcGlobalMetadataDao extends JdbcDaoSupport implements GlobalMetadataDao {

    protected static Logger logger = Logger.getLogger(JdbcGlobalMetadataDao.class);

    private SimpleJdbcInsert insertActor;

    /**
     * Looks up and retrieves a list of persisted Global objects using the given id.
     *
     * @param wizardDataId The id of the corresponding WizardData object.
     * @return The Global object.
     */
    public List<GlobalMetadata> lookupGlobalMetadata(String wizardDataId) {
        String sql = "SELECT * FROM globalMetadata WHERE wizardDataId = ?";
        List<GlobalMetadata> globalMetadata = getJdbcTemplate().query(sql, new JdbcGlobalMetadataDao.GlobalMetadataMapper(), wizardDataId);
        return globalMetadata;
    }

    /**
     * Persists the information in the given list of globalMetadata objects.
     *
     * @param wizardDataId The id of the corresponding WizardData object.
     * @param globalMetadata The list of GlobalMetadata objects to persist.
     * @throws DataRetrievalFailureException If unable to persist the GlobalMetadata objects.
     */
    public void persistGlobalMetadata(String wizardDataId, List<GlobalMetadata> globalMetadata) throws DataRetrievalFailureException {
        for (GlobalMetadata item : globalMetadata) {
            persistGlobalMetadata(wizardDataId, item);
        }
    }

    /**
     * Persists the information in the given globalMetadata object.
     *
     * @param wizardDataId The id of the corresponding WizardData object.
     * @param globalMetadata The GlobalMetadata object to persist.
     * @throws DataRetrievalFailureException If unable to persist the GlobalMetadata object.
     */
    public void persistGlobalMetadata(String wizardDataId, GlobalMetadata globalMetadata) throws DataRetrievalFailureException {
        // Set wizard id value.
        globalMetadata.setWizardDataId(wizardDataId);
        
        this.insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("globalMetadata");
        SqlParameterSource params = new BeanPropertySqlParameterSource(globalMetadata);
        int rowsAffected = insertActor.execute(params);
        if (rowsAffected <= 0) {
            String message = "Unable to persist global metadata information for  " + wizardDataId;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Persisted global metadata corresponding to wizardData " + wizardDataId);
        }
    }

    /**
     * Updated the information corresponding to the given list of globalMetadata objects.
     *
     * @param wizardDataId The id of the corresponding WizardData object.
     * @param globalMetadata The list of globalMetadata objects to update.
     * @throws DataRetrievalFailureException If unable to update persisted GlobalMetadata objects.
     */
    public void updatePersistedGlobalMetadata(String wizardDataId, List<GlobalMetadata> globalMetadata) throws DataRetrievalFailureException {
        for (GlobalMetadata item : globalMetadata) {
            updatePersistedGlobalMetadata(wizardDataId, item);
        }
    }

    /**
     * Updated the information corresponding to the given globalMetadata object.
     *
     * @param wizardDataId The id of the corresponding WizardData object.
     * @param globalMetadata The globalMetadata object to update.
     * @throws DataRetrievalFailureException If unable to update persisted GlobalMetadata object.
     */
    public void updatePersistedGlobalMetadata(String wizardDataId, GlobalMetadata globalMetadata) throws DataRetrievalFailureException {

        // Set the wizard Id.
        globalMetadata.setWizardDataId(wizardDataId);

        String sql = "UPDATE globalMetadata SET " +
                "wizardDataId = ?, " +
                "metadataGroup = ?, " +
                "metadataValueType = ?, " +
                "metadataKey = ?, " +
                "metadataValue = ? " +
                "WHERE wizardDataId = ? " +
                "AND metadataGroup = ? " +
                "AND metadataKey = ? ";

        int rowsAffected = getJdbcTemplate().update(sql, new Object[]{
                // order matters here
                globalMetadata.getWizardDataId(),
                globalMetadata.getMetadataGroup(),
                globalMetadata.getMetadataValueType(),
                globalMetadata.getMetadataKey(),
                globalMetadata.getMetadataValue(),
                globalMetadata.getWizardDataId(),
                globalMetadata.getMetadataGroup(),
                globalMetadata.getMetadataKey()
        });
        if (rowsAffected <= 0) {
            // Metadata entry wasn't persisted prior. Add it.
            // (This can happen in the case of template restoration.)
            persistGlobalMetadata(wizardDataId, globalMetadata);
        } else {
            logger.info("Updated persisted GlobalMetadata  object " + globalMetadata.toString());
        }

        // Delete the existing globalMetadata metadata.
        //deletePersistedGlobalMetadata(globalMetadata);

        // Persists the newer data.
        //persistGlobalMetadata(wizardDataId, globalMetadata);
    }

    /**
     * Deletes the persisted list of globalMetadata information using the given id.
     *
     * @param globalMetadata The list of globalMetadata objects to update.
     * @throws DataRetrievalFailureException If unable to delete persisted globalMetadata information.
     */
    public void deletePersistedGlobalMetadata(List<GlobalMetadata> globalMetadata) throws DataRetrievalFailureException {
        for (GlobalMetadata item : globalMetadata) {
            deletePersistedGlobalMetadata(item);
        }
    }

    /**
     * Deletes the persisted globalMetadata object information using the given id.
     *
     * @param globalMetadata The globalMetadata object to delete.
     * @throws DataRetrievalFailureException If unable to delete persisted globalMetadata information.
     */
    public void deletePersistedGlobalMetadata(GlobalMetadata globalMetadata) throws DataRetrievalFailureException {
        String sql = "DELETE FROM globalMetadata WHERE wizardDataId = ?";
        int rowsAffected = getJdbcTemplate().update(sql, globalMetadata.getWizardDataId());
        if (rowsAffected <= 0) {
            String message =
                    "Unable to delete global metadata corresponding to id " + globalMetadata.getWizardDataId();
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Deleted global metadata corresponding to id " + globalMetadata.getWizardDataId());
        }
    }

    
    /**
     * This GlobalMetadataMapper only used by JdbcGlobalMetadataDao.
     */
    private static class GlobalMetadataMapper implements RowMapper<GlobalMetadata> {

        /**
         * Maps each row of globalMetadata in the ResultSet to the GlobalMetadata object.
         *
         * @param rs     The ResultSet to be mapped.
         * @param rowNum The number of the current row.
         * @return The populated GlobalMetadata object.
         * @throws SQLException If an SQLException is encountered getting column values.
         */
        public GlobalMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            GlobalMetadata globalMetadata = new GlobalMetadata();
            globalMetadata.setWizardDataId(rs.getString("wizardDataId"));
            globalMetadata.setMetadataValueType(rs.getString("metadataValueType"));
            globalMetadata.setMetadataGroup(rs.getString("metadataGroup"));
            globalMetadata.setMetadataKey(rs.getString("metadataKey"));
            globalMetadata.setMetadataValue(rs.getString("metadataValue"));
            return globalMetadata;
        }
    }
}
