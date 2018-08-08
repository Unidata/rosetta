/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.CfTypeData;
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

public class JdbcCfTypeDataDao extends JdbcDaoSupport implements CfTypeDataDao {

  protected static Logger logger = Logger.getLogger(JdbcCfTypeDataDao.class);

  private SimpleJdbcInsert insertActor;

  @Override
  public CfTypeData lookupCfDataById(String id) throws DataRetrievalFailureException {
    String sql = "SELECT * FROM cfTypeData WHERE id = ?";
    List<CfTypeData> cfTypeData = getJdbcTemplate().query(sql, new CfTypeDataMapper(), id);
    if (cfTypeData.isEmpty()) {
      String message = "Unable to find persisted CF type data corresponding to id " + id;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return cfTypeData.get(0);
  }


  @Override
  public void persistCfTypeData(CfTypeData cfTypeData) throws DataRetrievalFailureException {
    // Verify entry doesn't already exist (it shouldn't).
    String sql = "SELECT * FROM cfTypeData WHERE id = ?";
    List<CfTypeData> persisted = getJdbcTemplate()
        .query(sql, new CfTypeDataMapper(), cfTypeData.getId());
    // If there is an entry, see that it doesn't contain the data we are about to persist.
    if (!persisted.isEmpty()) {
      CfTypeData persistedData = persisted.get(0);
      if (persistedData.getCfType() != null && persistedData.getCommunity() != null
          && persistedData.getPlatform() != null) {
        throw new DataRetrievalFailureException(
            "CF type data corresponding to id " + cfTypeData.getId() + " already exists.");
      }
    } else {
      // Persist the CF type data object.
      this.insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("cfTypeData");
      SqlParameterSource parameters = new BeanPropertySqlParameterSource(cfTypeData);
      int rowsAffected = insertActor.execute(parameters);
      if (rowsAffected <= 0) {
        String message = "Unable to persist CF type data corresponding to id " + cfTypeData.getId();
        logger.error(message);
        throw new DataRetrievalFailureException(message);
      } else {
        logger.info("CF type data corresponding to id " + cfTypeData.getId() + " persisted.");
      }
    }
  }


  @Override
  public void updatePersistedCfTypeData(CfTypeData cfTypeData)
      throws DataRetrievalFailureException {
    String sql = "UPDATE cfTypeData SET " +
        "cfType = ?, " +
        "community = ?, " +
        "metadataProfile = ?," +
        "platform = ? " +
        "WHERE id = ?";
    int rowsAffected = getJdbcTemplate().update(sql, new Object[]{
        // order matters here
        cfTypeData.getCfType(),
        cfTypeData.getCommunity(),
        cfTypeData.getMetadataProfile(),
        cfTypeData.getPlatform(),
        cfTypeData.getId()
    });
    if (rowsAffected <= 0) {
      String message =
          "Unable to update persisted CF type data corresponding to id " + cfTypeData.getId();
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    } else {
      logger.info("Updated persisted CF type data corresponding to id " + cfTypeData.getId());
    }
  }


  private static class CfTypeDataMapper implements RowMapper<CfTypeData> {

    /**
     * Maps each row of data in the ResultSet to the CfTypeData object.
     *
     * @param rs The ResultSet to be mapped.
     * @param rowNum The number of the current row.
     * @return The populated CfTypeData object.
     * @throws SQLException If an SQLException is encountered getting column values.
     */
    public CfTypeData mapRow(ResultSet rs, int rowNum) throws SQLException {
      CfTypeData cfTypeData = new CfTypeData();
      cfTypeData.setId(rs.getString("id"));
      cfTypeData.setCfType(rs.getString("cfType"));
      cfTypeData.setCommunity(rs.getString("community"));
      cfTypeData.setMetadataProfile(rs.getString("metadataProfile"));
      cfTypeData.setPlatform(rs.getString("platform"));
      return cfTypeData;
    }
  }
}
