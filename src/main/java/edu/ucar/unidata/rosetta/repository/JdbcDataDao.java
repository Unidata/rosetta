package edu.ucar.unidata.rosetta.repository;

import edu.ucar.unidata.rosetta.domain.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * @author oxelson@ucar.edu
 */
public class JdbcDataDao extends JdbcDaoSupport implements DataDao {


    protected static Logger logger = Logger.getLogger(JdbcDataDao.class);

    private SimpleJdbcInsert insertActor;

    /**
     * Looks up and retrieves a persisted Data object using the given id.
     *
     * @param id    The id of the Data object.
     * @return      The Data object corresponding to the given id.
     * @throws DataRetrievalFailureException  If unable to lookup Data with the given id.
     */
    @Override
    public Data lookupById(String id) throws DataRetrievalFailureException {
        String sql = "SELECT * FROM data WHERE id = ?";
        List<Data> data = getJdbcTemplate().query(sql, new DataMapper(), id);
        if (data.isEmpty()) {
            String message = "Unable to find persisted Data object corresponding to id " + id;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        }
        return data.get(0);
    }

    /**
     * Persists the information in the given data object.
     *
     * @param data  The Data object to persist.
     * @throws DataRetrievalFailureException  If unable to persist the Data object.
     */
    @Override
    public void persistData(Data data) throws DataRetrievalFailureException {
        // Verify entry doesn't already exist (it shouldn't).
        String sql = "SELECT * FROM data WHERE id = ?";
        List<Data> persisted = getJdbcTemplate().query(sql, new DataMapper(), data.getId());
        if (!persisted.isEmpty()) {
            throw new DataRetrievalFailureException("Data corresponding to id " +  data.getId() + " already exists.");
        } else {
            // Persist the data object.
            this.insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("data");
            SqlParameterSource parameters = new BeanPropertySqlParameterSource(data);
            int rowsAffected = insertActor.execute(parameters);
            if (rowsAffected <= 0) {
                String message = "Unable to persist Data object corresponding to id " + data.getId();
                logger.error(message);
                throw new DataRetrievalFailureException(message);
            } else {
                logger.info("Data object corresponding to id " + data.getId() + " persisted.");
            }
        }
    }

    /**
     * Updated the information corresponding to the given data object.
     *
     * @param data  The data object to update.
     * @throws DataRetrievalFailureException  If unable to update persisted Data object.
     */
    @Override
    public void updatePersistedData(Data data) throws DataRetrievalFailureException {
        String sql = "UPDATE data SET " +
                "cfType = ?, " +
                "community = ?, " +
                "platform = ?, " +
                "dataFileName = ?, " +
                "dataFileType= ?, " +
                "positionalFileName = ?, " +
                "templateFileName = ?, " +
                "headerLineNumbers = ?, " +
                "noHeaderLines = ?, " +
                "delimiter = ? " +
                "WHERE id = ?";
        int rowsAffected  = getJdbcTemplate().update(sql, new Object[] {
                // order matters here
                data.getCfType(),
                data.getCommunity(),
                data.getPlatform(),
                data.getDataFileName(),
                data.getDataFileType(),
                data.getPositionalFileName(),
                data.getTemplateFileName(),
                data.getHeaderLineNumbers(),
                String.valueOf(data.getNoHeaderLines()),
                data.getDelimiter(),
                data.getId()
        });
        if (rowsAffected  <= 0) {
            String message ="Unable to update persisted Data object corresponding to id " + data.getId();
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Updated persisted Data object corresponding to id " + data.getId());
        }
    }

    /**
     * Deletes the persisted data object information.
     *
     * @param id    The id of the Data object to delete.
     * @throws DataRetrievalFailureException  If unable to delete persisted Data object.
     */
    @Override
    public void deletePersistedData(String id) throws DataRetrievalFailureException {
        String sql = "DELETE FROM data WHERE id = ?";
        int rowsAffected  = getJdbcTemplate().update(sql, id);
        if (rowsAffected <= 0) {
            String message = "Unable to delete Data object corresponding to id " + id;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        } else {
            logger.info("Deleted Data object corresponding to id " + id);
        }
    }

    /**
     * This DataMapper only used by JdbcDataDao.
     */
    private static class DataMapper implements RowMapper<Data> {
        /**
         * Maps each row of data in the ResultSet to the Data object.
         *
         * @param rs  The ResultSet to be mapped.
         * @param rowNum  The number of the current row.
         * @return  The populated Data object.
         * @throws SQLException  If an SQLException is encountered getting column values.
         */
        public Data mapRow(ResultSet rs, int rowNum) throws SQLException {
            Data data = new Data();
            data.setId(rs.getString("id"));
            data.setCfType(rs.getString("cfType"));
            data.setCommunity(rs.getString("community"));
            data.setPlatform(rs.getString("platform"));
            data.setDataFileName(rs.getString("dataFileName"));
            data.setDataFileType(rs.getString("dataFileType"));
            data.setPositionalFileName(rs.getString("positionalFileName"));
            data.setTemplateFileName(rs.getString("templateFileName"));
            data.setHeaderLineNumbers(rs.getString("headerLineNumbers"));
            data.setNoHeaderLines(Boolean.parseBoolean(rs.getString("noHeaderLines")));
            data.setDelimiter(rs.getString("delimiter"));
            return data;
        }
    }

}
