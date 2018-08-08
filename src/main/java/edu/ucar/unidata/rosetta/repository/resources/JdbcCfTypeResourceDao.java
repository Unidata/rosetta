/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.CfType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of a CfType DAO.
 *
 * @author oxelson@ucar.edu
 */
public class JdbcCfTypeResourceDao extends JdbcDaoSupport implements CfTypeResourceDao {

  private static final Logger logger = Logger.getLogger(JdbcCfTypeResourceDao.class);

  /**
   * Looks up and retrieves a list of persisted CfType objects.
   *
   * @return A List of all persisted CF types.
   * @throws DataRetrievalFailureException If unable to retrieve persisted CF types.
   */
  public List<CfType> getCfTypes() throws DataRetrievalFailureException {
    String sql = "SELECT * FROM cfTypes";
    List<CfType> cfTypes = getJdbcTemplate().query(sql, new JdbcCfTypeResourceDao.CfTypeMapper());
    if (cfTypes.isEmpty()) {
      String message = "Unable to find persisted CfType objects.";
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return cfTypes;
  }

  /**
   * Looks up and retrieves a persisted CfType object using the provided id.
   *
   * @param id The id of the CF type to retrieve.
   * @return The CfType object matching the provided id.
   * @throws DataRetrievalFailureException If unable to retrieve persisted CF type.
   */
  public CfType lookupCfTypeById(int id) throws DataRetrievalFailureException {
    String sql = "SELECT * FROM cfTypes WHERE id = ?";
    List<CfType> cfTypes = getJdbcTemplate().query(sql, new JdbcCfTypeResourceDao.CfTypeMapper(), id);
    if (cfTypes.isEmpty()) {
      String message = "Unable to find persisted CfType object corresponding to id " + id;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return cfTypes.get(0);
  }

  /**
   * Looks up and retrieves a persisted CfType object using the provided name.
   *
   * @param name The name of the CF type to retrieve.
   * @return The CfType object matching the provided name.
   * @throws DataRetrievalFailureException If unable to retrieve persisted CF type.
   */
  public CfType lookupCfTypeByName(String name) throws DataRetrievalFailureException {
    String sql = "SELECT * FROM cfTypes WHERE name = ?";
    List<CfType> cfTypes = getJdbcTemplate().query(sql, new JdbcCfTypeResourceDao.CfTypeMapper(), name);
    if (cfTypes.isEmpty()) {
      String message = "Unable to find persisted CfType object corresponding to name " + name;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return cfTypes.get(0);
  }

  /**
   * This CfTypeMapper only used by JdbcCfTypeResourceDao.
   */
  private static class CfTypeMapper implements RowMapper<CfType> {

    /**
     * Maps each row of data in the ResultSet to the CfType object.
     *
     * @param rs The ResultSet to be mapped.
     * @param rowNum The number of the current row.
     * @return The populated CfType object.
     * @throws SQLException If an SQLException is encountered getting column values.
     */
    public CfType mapRow(ResultSet rs, int rowNum) throws SQLException {
      CfType cfType = new CfType();
      cfType.setId(rs.getInt("id"));
      cfType.setName(rs.getString("name"));
      return cfType;
    }
  }

}
