/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
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
 * Implementation of the Wizard DAO.
 */
public class JdbcWizardDataDao extends JdbcDaoSupport implements WizardDataDao {

    protected static Logger logger = Logger.getLogger(JdbcWizardDataDao.class);

    private SimpleJdbcInsert insertActor;

    /**
     * Looks up and returns the persisted wizard data using the given ID.
     *
     * @param id  The ID associated with the wizard data..
     * @return  The data in a WizardData object.
     * @throws DataRetrievalFailureException If unable to find persisted wizard data corresponding to ID.
     */
    @Override
    public WizardData lookupWizardDataById(String id) throws DataRetrievalFailureException {
        String sql = "SELECT * FROM wizardData WHERE id = ?";
        List<WizardData> wizardData = getJdbcTemplate().query(sql, new WizardDataDataMapper(), id);
        if (wizardData.isEmpty()) {
            String message = "Unable to find persisted wizard data corresponding to id " + id;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        }
        return wizardData.get(0);
    }

    /**
     * Persists the provided wizard data for the first time.
     *
     * @param wizardData  The WizardData object containing the data to persist.
     * @throws DataRetrievalFailureException  If unable to persist wizard data or if wizard data already is persisted.
     */
    @Override
    public void persistWizardData(WizardData wizardData) throws DataRetrievalFailureException {
        // Verify entry doesn't already exist (it shouldn't).
        String sql = "SELECT * FROM wizardData WHERE id = ?";
        List<WizardData> persisted = getJdbcTemplate()
                .query(sql, new WizardDataDataMapper(), wizardData.getId());
        // If there is an entry, see that it doesn't contain the data we are about to persist.
        // We should not be using this method to update already persisted data!
        if (!persisted.isEmpty()) {
            WizardData persistedData = persisted.get(0);
            if (persistedData.getCfType() != null && persistedData.getCommunity() != null
                    && persistedData.getPlatform() != null) {
                throw new DataRetrievalFailureException(
                        "Wizard data corresponding to id " + wizardData.getId() + " already exists.");
            }
        } else {
            // Persist the CF type data object.
            this.insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("wizardData");
            SqlParameterSource parameters = new BeanPropertySqlParameterSource(wizardData);
            int rowsAffected = insertActor.execute(parameters);
            if (rowsAffected <= 0) {
                String message = "Unable to persist wizard data corresponding to id " + wizardData.getId();
                logger.error(message);
                throw new DataRetrievalFailureException(message);
            } else {
                logger.info("Wizard data corresponding to id " + wizardData.getId() + " persisted.");
            }
        }
    }

    /**
     * Updates persisted wizard data with the provided new values.
     *
     * @param wizardData The WizardData object containing the data to update.
     * @throws DataRetrievalFailureException  If unable to update the persisted wizard data.
     */
    @Override
    public void updatePersistedWizardData(WizardData wizardData)
            throws DataRetrievalFailureException {
        String sql = "UPDATE wizardData SET " +
                "cfType = ?, " +
                "community = ?, " +
                "metadataProfile = ?," +
                "platform = ?, " +
                "headerLineNumbers = ?, " +
                "delimiter = ?, " +
                "dataFileType = ? " +
                "WHERE id = ?";
        int rowsAffected = getJdbcTemplate().update(sql, new Object[]{
                // order matters here
                wizardData.getCfType(),
                wizardData.getCommunity(),
                wizardData.getMetadataProfile(),
                wizardData.getPlatform(),
                wizardData.getHeaderLineNumbers(),
                wizardData.getDelimiter(),
                wizardData.getDataFileType(),
                wizardData.getId()
        });
        if (rowsAffected <= 0) {
            String message =
                    "Unable to update persisted wizard data corresponding to id " + wizardData.getId();
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Updated persisted wizard data corresponding to id " + wizardData.getId());
        }
    }

    /**
     * Data mapper class for CF Type and related data.
     */
    private static class WizardDataDataMapper implements RowMapper<WizardData> {

        /**
         * Maps each row of data in the ResultSet to the WizardData object.
         *
         * @param rs     The ResultSet to be mapped.
         * @param rowNum The number of the current row.
         * @return The populated WizardData object.
         * @throws SQLException If an SQLException is encountered getting column values.
         */
        public WizardData mapRow(ResultSet rs, int rowNum) throws SQLException {
            WizardData wizardData = new WizardData();
            wizardData.setId(rs.getString("id"));
            wizardData.setCfType(rs.getString("cfType"));
            wizardData.setCommunity(rs.getString("community"));
            wizardData.setMetadataProfile(rs.getString("metadataProfile"));
            wizardData.setPlatform(rs.getString("platform"));
            wizardData.setHeaderLineNumbers(rs.getString("headerLineNumbers"));
            wizardData.setDelimiter(rs.getString("delimiter"));
            wizardData.setDataFileType(rs.getString("dataFileType"));
            return wizardData;
        }
    }
}
