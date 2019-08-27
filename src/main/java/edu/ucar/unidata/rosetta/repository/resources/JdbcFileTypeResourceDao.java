/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.FileType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of a file type DAO.
 *
 * @author oxelson@ucar.edu
 */
public class JdbcFileTypeResourceDao extends JdbcDaoSupport implements FileTypeResourceDao {

  private static final Logger logger = Logger.getLogger(JdbcFileTypeResourceDao.class);

  /**
   * Looks up and retrieves a list of persisted FileType objects.
   *
   * @return A List of all persisted file types.
   * @throws DataRetrievalFailureException If unable to retrieve persisted file types.
   */
  public List<FileType> getFileTypes() throws DataRetrievalFailureException {
    String sql = "SELECT * FROM fileTypes";
    List<FileType> fileTypes = getJdbcTemplate().query(sql, new JdbcFileTypeResourceDao.FileTypeMapper());
    if (fileTypes.isEmpty()) {
      String message = "Unable to find persisted FileType objects.";
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return fileTypes;
  }

  /**
   * Looks up and retrieves a persisted FileType object using the provided id.
   *
   * @param id The id of the file type to retrieve.
   * @return The FileType object matching the provided id.
   * @throws DataRetrievalFailureException If unable to retrieve persisted file type.
   */
  public FileType lookupFileTypeById(int id) throws DataRetrievalFailureException {
    String sql = "SELECT * FROM fileTypes WHERE id = ?";
    List<FileType> fileTypes = getJdbcTemplate().query(sql, new JdbcFileTypeResourceDao.FileTypeMapper(), id);
    if (fileTypes.isEmpty()) {
      String message = "Unable to find persisted FileType object corresponding to id " + id;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return fileTypes.get(0);
  }

  /**
   * Looks up and retrieves a persisted FileType object using the provided name.
   *
   * @param name The name of the file type to retrieve.
   * @return The FileType object matching the provided name.
   * @throws DataRetrievalFailureException If unable to retrieve persisted file type.
   */
  public FileType lookupFileTypeByName(String name) throws DataRetrievalFailureException {
    String sql = "SELECT * FROM fileTypes WHERE name = ?";
    List<FileType> fileTypes = getJdbcTemplate().query(sql, new JdbcFileTypeResourceDao.FileTypeMapper(), name);
    if (fileTypes.isEmpty()) {
      String message = "Unable to find persisted FileType object corresponding to name " + name;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return fileTypes.get(0);
  }

  /**
   * This FileTypeMapper only used by JdbcFileTypeResourceDao.
   */
  private static class FileTypeMapper implements RowMapper<FileType> {

    /**
     * Maps each row of data in the ResultSet to the FileType object.
     *
     * @param rs The ResultSet to be mapped.
     * @param rowNum The number of the current row.
     * @return The populated FileType object.
     * @throws SQLException If an SQLException is encountered getting column values.
     */
    public FileType mapRow(ResultSet rs, int rowNum) throws SQLException {
      FileType fileType = new FileType();
      fileType.setId(rs.getInt("id"));
      fileType.setName(rs.getString("name"));
      return fileType;
    }
  }

}
