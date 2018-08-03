package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.CustomFileAttributes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class JdbcCustomFileAttributesDao extends JdbcDaoSupport implements
    CustomFileAttributesDao {

  protected static Logger logger = Logger.getLogger(JdbcCustomFileAttributesDao.class);

  private SimpleJdbcInsert insertActor;

  @Override
  public CustomFileAttributes lookupById(String id) throws DataRetrievalFailureException {
    String sql = "SELECT * FROM dataFiles WHERE id = ?";
    List<CustomFileAttributes> customFileAttributes = getJdbcTemplate().query(sql, new JdbcCustomFileAttributesDao.CustomDataFileMapper(), id);
    if (customFileAttributes.isEmpty()) {
      String message = "Unable to find persisted custom data file attributes corresponding to id " + id;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return customFileAttributes.get(0);
  }
  
  @Override
  public void updatePersistedData(String id, CustomFileAttributes customFileAttributes) throws DataRetrievalFailureException {
    String sql = "UPDATE dataFiles SET " +
        "headerLineNumbers = ?, " +
        "delimiter = ? " +
        "WHERE id = ?";
    int rowsAffected = getJdbcTemplate().update(sql, new Object[]{
        // order matters here
        customFileAttributes.getHeaderLineNumbers(),
        customFileAttributes.getDelimiter(),
        customFileAttributes.getId()
    });
    if (rowsAffected <= 0) {
      String message = "Unable to update persisted Data object corresponding to id " + id;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    } else {
      logger.info("Updated persisted Data object corresponding to id " + id);
    }
  }



  private static class CustomDataFileMapper implements RowMapper<CustomFileAttributes> {
    /**
     * Maps each row of data in the ResultSet to the CustomDataFile object.
     *
     * @param rs The ResultSet to be mapped.
     * @param rowNum The number of the current row.
     * @return The populated CustomDataFile object.
     * @throws SQLException If an SQLException is encountered getting column values.
     */
    public CustomFileAttributes mapRow(ResultSet rs, int rowNum) throws SQLException {
      CustomFileAttributes customDataFile = new CustomFileAttributes();
      customDataFile.setId(rs.getString("id"));
      customDataFile.setDataFileType(rs.getString("dataFileType"));
      customDataFile.setHeaderLineNumbers(rs.getString("headerLineNumbers"));
      customDataFile.setDelimiter(rs.getString("delimiter"));
      return customDataFile;
    }
  }
}
