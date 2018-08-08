package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Delimiter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of a delimiter DAO.
 *
 * @author oxelson@ucar.edu
 */
public class JdbcDelimiterDao extends JdbcDaoSupport implements DelimiterDao {

  private static final Logger logger = Logger.getLogger(JdbcDelimiterDao.class);

  /**
   * Looks up and retrieves a list of persisted Delimiters objects.
   *
   * @return A List of all persisted delimiters.
   * @throws DataRetrievalFailureException If unable to retrieve persisted delimiters.
   */
  public List<Delimiter> getDelimiters() throws DataRetrievalFailureException {
    String sql = "SELECT * FROM delimiters ORDER BY name";
    List<Delimiter> delimiters = getJdbcTemplate()
        .query(sql, new JdbcDelimiterDao.DelimiterMapper());
    if (delimiters.isEmpty()) {
      String message = "Unable to find persisted Delimiter objects.";
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return delimiters;
  }

  /**
   * Looks up and retrieves a persisted Delimiter using the symbol name..
   *
   * @param name The name of the delimiter symbol to retrieve.
   * @return The Delimiter object matching the provided name.
   * @throws DataRetrievalFailureException If unable to retrieve persisted dlimiter.
   */
  public Delimiter lookupDelimiterByName(String name) throws DataRetrievalFailureException {
    String sql = "SELECT * FROM delimiters WHERE name = ?";
    List<Delimiter> delimiters = getJdbcTemplate()
        .query(sql, new JdbcDelimiterDao.DelimiterMapper(), name);
    if (delimiters.isEmpty()) {
      String message = "Unable to find persisted Delimiter object corresponding to name " + name;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return delimiters.get(0);
  }

  /**
   * This DelimiterMapper only used by JdbcDelimiterDao.
   */
  private static class DelimiterMapper implements RowMapper<Delimiter> {

    /**
     * Maps each row of data in the ResultSet to the Delimiter object.
     *
     * @param rs The ResultSet to be mapped.
     * @param rowNum The number of the current row.
     * @return The populated Delimiter object.
     * @throws SQLException If an SQLException is encountered getting column values.
     */
    public Delimiter mapRow(ResultSet rs, int rowNum) throws SQLException {
      Delimiter delimiter = new Delimiter();
      delimiter.setId(rs.getInt("id"));
      delimiter.setName(rs.getString("name"));
      delimiter.setCharacterSymbol(rs.getString("characterSymbol"));
      return delimiter;
    }
  }

}
