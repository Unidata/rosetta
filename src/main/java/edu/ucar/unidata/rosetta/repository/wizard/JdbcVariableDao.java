/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.Variable;
import edu.ucar.unidata.rosetta.domain.VariableMetadata;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a variable DAO.
 *
 * @author oxelson@ucar.edu
 */
public class JdbcVariableDao extends JdbcDaoSupport implements VariableDao {

    protected static Logger logger = Logger.getLogger(JdbcVariableDao.class);

    private SimpleJdbcInsert insertActor;

    /**
     * Looks up and retrieves a list of persisted Variable objects using the given id.
     *
     * @param wizardDataId The id of the corresponding WizardData object.
     * @return The Variable object.
     */
    public List<Variable> lookupVariables(String wizardDataId)  {
        String sql = "SELECT * FROM variables WHERE wizardDataId = ?";
        List<Variable> variables = getJdbcTemplate().query(sql, new JdbcVariableDao.VariableMapper(), wizardDataId);
        sql = "SELECT * FROM variableMetadata WHERE variableId = ?";
        for (Variable variable : variables) {
            List<VariableMetadata> variableMetadataValues = getJdbcTemplate().query(sql, new JdbcVariableDao.VariableMetadataMapper(), variable.getVariableId());
            List<VariableMetadata> required = new ArrayList<>();
            List<VariableMetadata> recommended = new ArrayList<>();
            List<VariableMetadata> additional = new ArrayList<>();
            for (VariableMetadata variableMetadata : variableMetadataValues) {
                if (variableMetadata.getComplianceLevel().equals("required")) {
                    required.add(variableMetadata);
                } else if (variableMetadata.getComplianceLevel().equals("recommended")) {
                    recommended.add(variableMetadata);
                } else {
                    additional.add(variableMetadata);
                }
            }
            variable.setRequiredMetadata(required);
            variable.setRecommendedMetadata(recommended);
            variable.setAdditionalMetadata(additional);
        }
        return variables;
    }



    /**
     * Persists the information in the given list of variable objects.
     *
     * @param wizardDataId The id of the corresponding WizardData object.
     * @param variables The list of Variable objects to persist.
     * @throws DataRetrievalFailureException If unable to persist the Variable objects.
     */
    public void persistVariables(String wizardDataId, List<Variable> variables) throws DataRetrievalFailureException {
        for (Variable variable : variables) {
            persistVariable(wizardDataId, variable);
        }
    }

    /**
     * Persists the information in the given variable object.
     *
     * @param wizardDataId The id of the corresponding WizardData object.
     * @param variable The Variable object to persist.
     * @throws DataRetrievalFailureException If unable to persist the Variable object.
     */
    public void persistVariable(String wizardDataId, Variable variable) throws DataRetrievalFailureException {
        // Set wizard id value.
        variable.setWizardDataId(wizardDataId);

        // Persist the variable object.
        this.insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("variables").usingGeneratedKeyColumns("variableId");
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(variable);
        int generatedId = insertActor.executeAndReturnKey(parameters).intValue();
        /*
        if (rowsAffected <= 0) {
            String message = "Unable to persist Variable object  " + variable.toString();
            logger.error(message);
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Variable object persisted " + variable.toString());
        }
        */
        // Get the compliance level variable metadata and persist.
        List<VariableMetadata> required = variable.getRequiredMetadata();
        if (required.size() > 0) {
            persistVariableMetadata(generatedId, required, "required");
        }

        List<VariableMetadata> recommended = variable.getRecommendedMetadata();
        if (recommended.size() > 0) {
                persistVariableMetadata(generatedId, recommended, "recommended");
        }

        List<VariableMetadata> additional = variable.getAdditionalMetadata();
        if (additional.size() > 0) {
            persistVariableMetadata(generatedId, additional, "additional");
        }
    }


    private void persistVariableMetadata(int variableId, List<VariableMetadata> variableMetadataValues, String complianceLevel) throws DataRetrievalFailureException {
        this.insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("variableMetadata");
        for (VariableMetadata variableMetadata : variableMetadataValues) {
            variableMetadata.setComplianceLevel(complianceLevel);
            variableMetadata.setVariableId(variableId);
            SqlParameterSource params = new BeanPropertySqlParameterSource(variableMetadata);
            int rowsAffected = insertActor.execute(params);
            if (rowsAffected <= 0) {
                String message = "Unable to persist variable metadata information for  " + variableId;
                logger.error(message);
                throw new DataRetrievalFailureException(message);
            } else {
                logger.info("Persisted variable metadata corresponding to variable " + variableId);
            }
        }
    }

    /**
     * Updated the information corresponding to the given list of variable objects.
     *
     * @param variables The list of variable objects to update.
     * @throws DataRetrievalFailureException If unable to update persisted Variable objects.
     */
    public void updatePersistedVariables(List<Variable> variables) throws DataRetrievalFailureException {
        for (Variable variable : variables) {
            updatePersistedVariable(variable);
        }
    }

    /**
     * Updated the information corresponding to the given variable object.
     *
     * @param variable The variable object to update.
     * @throws DataRetrievalFailureException If unable to update persisted Variable object.
     */
    public void updatePersistedVariable(Variable variable) throws DataRetrievalFailureException {
        String sql = "UPDATE variables SET " +
                "wizardDataId = ?, " +
                "columnNumber = ?, " +
                "variableName = ?, " +
                "metadataType = ?, " +
                "metadataTypeStructure = ?, " +
                "verticalDirection = ?, " +
                "metadataValueType = ? " +
                "WHERE variableId = ?";
        int rowsAffected = getJdbcTemplate().update(sql, new Object[]{
                // order matters here
                variable.getWizardDataId(),
                variable.getColumnNumber(),
                variable.getVariableName(),
                variable.getMetadataType(),
                variable.getMetadataTypeStructure(),
                variable.getVerticalDirection(),
                variable.getMetadataValueType(),
                variable.getVariableId()
        });
        if (rowsAffected <= 0) {
            String message = "Unable to update persisted Variable object " + variable.toString();
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Updated persisted Variable object " + variable.toString());
        }

        // Delete the existing compliance level variable metadata and replace with new data.
        deletePersistedVariableMetadata(variable.getVariableId());

        List<VariableMetadata> required = variable.getRequiredMetadata();
        if (required.size() > 0) {
            persistVariableMetadata(variable.getVariableId(), required, "required");
        }

        List<VariableMetadata> recommended = variable.getRecommendedMetadata();
        if (recommended.size() > 0) {
            persistVariableMetadata(variable.getVariableId(), recommended, "recommended");
        }

        List<VariableMetadata> additional = variable.getAdditionalMetadata();
        if (additional.size() > 0) {
            persistVariableMetadata(variable.getVariableId(), additional, "additional");
        }
    }

    private void deletePersistedVariableMetadata(int variableId) {
        // Get the compliance level variable metadata and update.
        String sql = "DELETE FROM variableMetadata WHERE variableId = ?";
        int rowsAffected = getJdbcTemplate().update(sql, variableId);

        if (rowsAffected <= 0) {
            String message = "Unable to delete variable metadata entries corresponding to variable " + variableId;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Deleted variable metadata entries corresponding to variable " + variableId);
        }
    }

    /**
     * Deletes the persisted list of variable information using the given id.
     *
     * @param variables The list of variable objects to update.
     * @throws DataRetrievalFailureException If unable to delete persisted variable information.
     */
    public void deletePersistedVariables(List<Variable> variables) throws DataRetrievalFailureException {
        for (Variable variable : variables) {
            deletePersistedVariable(variable);
        }
    }

    /**
     * Deletes the persisted variable object information using the given id.
     *
     * @param variable The variable object to delete.
     * @throws DataRetrievalFailureException If unable to delete persisted variable information.
     */
    public void deletePersistedVariable(Variable variable) throws DataRetrievalFailureException {
        // Delete the existing compliance level variable metadata and replace with new data.
        deletePersistedVariableMetadata(variable.getVariableId());

        String sql = "DELETE FROM variables WHERE variableId = ?";
        int rowsAffected = getJdbcTemplate().update(sql, variable);
        if (rowsAffected <= 0) {
            String message =
                    "Unable to delete variable corresponding to id " + variable.getVariableId();
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Deleted variable corresponding to id " + variable.getVariableId());
        }
    }


    /**
     * This VariableMapper only used by JdbcVariableDao.
     */
    private static class VariableMapper implements RowMapper<Variable> {

        /**
         * Maps each row of variable in the ResultSet to the Variable object.
         *
         * @param rs     The ResultSet to be mapped.
         * @param rowNum The number of the current row.
         * @return The populated Variable object.
         * @throws SQLException If an SQLException is encountered getting column values.
         */
        public Variable mapRow(ResultSet rs, int rowNum) throws SQLException {
            Variable variable = new Variable();
            variable.setVariableId(rs.getInt("variableId"));
            variable.setWizardDataId(rs.getString("wizardDataId"));
            variable.setColumnNumber(rs.getInt("columnNumber"));
            variable.setVariableName(rs.getString("variableName"));
            variable.setMetadataType(rs.getString("metadataType"));
            variable.setMetadataTypeStructure(rs.getString("metadataTypeStructure"));
            variable.setVerticalDirection(rs.getString("verticalDirection"));
            variable.setMetadataValueType(rs.getString("metadataValueType"));
            return variable;
        }
    }


    /**
     * This VariableMetadataMapper only used by JdbcVariableMetadataDao.
     */
    private static class VariableMetadataMapper implements RowMapper<VariableMetadata> {

        /**
         * Maps each row of variable in the ResultSet to the VariableMetadata object.
         *
         * @param rs     The ResultSet to be mapped.
         * @param rowNum The number of the current row.
         * @return The populated VariableMetadata object.
         * @throws SQLException If an SQLException is encountered getting column values.
         */
        public VariableMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            VariableMetadata variableMetadata = new VariableMetadata();
            variableMetadata.setVariableId(rs.getInt("variableId"));
            variableMetadata.setComplianceLevel(rs.getString("complianceLevel"));
            variableMetadata.setMetadataKey(rs.getString("metadataKey"));
            variableMetadata.setMetadataValue(rs.getString("metadataValue"));
            return variableMetadata;
        }
    }
}
