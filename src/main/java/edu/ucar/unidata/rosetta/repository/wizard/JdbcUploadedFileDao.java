/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;
import org.apache.commons.lang3.StringUtils;
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

public class JdbcUploadedFileDao extends JdbcDaoSupport implements UploadedFileDao {

  private static Logger logger = Logger.getLogger(JdbcUploadedFileDao.class);

  private SimpleJdbcInsert insertActor;

  /**
   * Looks up and retrieves persisted uploaded file data using the given ID.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @return The UploadedFileCmd object corresponding to the given ID.
   */
  @Override
  public UploadedFileCmd lookupById(String id) {
    // Get the uploaded file data.
    String sql = "SELECT * FROM uploadedFiles WHERE id = ?";
    List<UploadedFile> uploadedFiles = getJdbcTemplate().query(sql, new JdbcUploadedFileDao.UploadedFileMapper(), id);
    UploadedFileCmd uploadedFileCmd = new UploadedFileCmd(uploadedFiles);

    // Get the data file type.
    sql = "SELECT * FROM wizardData WHERE id = ?";
    List<String> dataFileTypes = getJdbcTemplate().query(sql, new JdbcUploadedFileDao.WizardDataMapper(), id);
    if (!dataFileTypes.isEmpty()) {
      // Add data file type to object.
      uploadedFileCmd.setDataFileType(dataFileTypes.get(0));
    }
    return uploadedFileCmd;
  }

  /**
   * Looks up and retrieves persisted data file information using the given ID.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @return The UploadedFile object for the data file.
   */
  @Override
  public UploadedFile lookupDataFileById(String id) {
    // Get the uploaded file data.
    String sql = "SELECT * FROM uploadedFiles WHERE id = ? AND fileType = 'DATA'";
    List<UploadedFile> uploadedFiles = getJdbcTemplate().query(sql, new JdbcUploadedFileDao.UploadedFileMapper(), id);
    if (uploadedFiles.isEmpty()) {
      throw new DataRetrievalFailureException("No persisted data file information found corresponding to id " + id);
    }
    return uploadedFiles.get(0);
  }

  /**
   * Persists, for the first time, the uploaded file data.
   *
   * @param id The unique ID corresponding to the data.
   * @param uploadedFileCmd The UploadedFileCmd object to persist.
   * @throws DataRetrievalFailureException If unable to persist the uploaded file data.
   */
  @Override
  public void persistData(String id, UploadedFileCmd uploadedFileCmd) throws DataRetrievalFailureException {
    // Verify entry doesn't already exist (it shouldn't).
    String sql = "SELECT * FROM uploadedFiles WHERE id = ?";
    List<UploadedFile> uploadedFiles = getJdbcTemplate().query(sql, new JdbcUploadedFileDao.UploadedFileMapper(), id);
    /*
     * if (!uploadedFiles.isEmpty()) {
     * // Data already exists. :-(
     * throw new DataRetrievalFailureException(
     * "Uploaded file data corresponding to id " + id + " already exists.");
     * } else {
     */
    // Persist the uploaded file data.
    for (UploadedFile uploadedFile : uploadedFileCmd.getUploadedFiles()) {
      if (StringUtils.trimToNull(uploadedFile.getFileName()) != null) {
        uploadedFile.setId(id);
        this.insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("uploadedFiles");
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(uploadedFile);
        int rowsAffected = insertActor.execute(parameters);
        if (rowsAffected <= 0) {
          String message = "Unable to persist uploaded file data corresponding to id " + id;
          logger.error(message);
          throw new DataRetrievalFailureException(message);
        } else {
          logger.info("Uploaded file data corresponding to id " + id + " persisted.");
        }
      }
    }
    // }
    // NOTE: the dataType entry is inserted into the wizardData table, for which the corresponding ID should already
    // exist. Hence, the type of SQL for this entry switches from INSERT to UPDATE.
    sql = "SELECT * FROM wizardData WHERE id = ?";
    List<String> dataFileTypes = getJdbcTemplate().query(sql, new JdbcUploadedFileDao.WizardDataMapper(), id);
    if (dataFileTypes.isEmpty()) {
      // This data should already exist. :-(
      throw new DataRetrievalFailureException("No persisted data file information found corresponding to id " + id);
    } else {
      uploadedFileCmd.setId(id);
      sql = "UPDATE wizardData SET " + "dataFileType = ? " + "WHERE id = ?";
      int rowsAffected = getJdbcTemplate().update(sql, new Object[] {
          // order matters here
          uploadedFileCmd.getDataFileType(), uploadedFileCmd.getId()});
      if (rowsAffected <= 0) {
        String message = "Unable to persist data file type corresponding to id " + id;
        logger.error(message);
        throw new DataRetrievalFailureException(message);
      } else {
        logger.info("Data file type corresponding to id " + id + " persisted.");
      }

    }
  }

  /**
   * Updated the persisted uploaded file data with the given information.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param uploadedFileCmd The updated the persisted data.
   * @throws DataRetrievalFailureException If unable to update the uploaded file data.
   */
  public void updatePersistedData(String id, UploadedFileCmd uploadedFileCmd) throws DataRetrievalFailureException {
    for (UploadedFile uploadedFile : uploadedFileCmd.getUploadedFiles()) {
      if (StringUtils.trimToNull(uploadedFile.getFileName()) != null) {
        uploadedFile.setId(id);
        String sql = "UPDATE uploadedFiles SET " + "fileName = ?, " + "fileType = ? " + "WHERE id = ?";
        int rowsAffected = getJdbcTemplate().update(sql, new Object[] {
            // order matters here
            uploadedFile.getFileName(), uploadedFile.getFileType().toString(), uploadedFile.getId()});
        if (rowsAffected <= 0) {
          String message = "Unable to update persisted uploaded file corresponding to id " + id;
          logger.error(message);
          throw new DataRetrievalFailureException(message);
        } else {
          logger.info("Updated persisted uploaded file data corresponding to id " + id);
        }
      }
    }
    uploadedFileCmd.setId(id);
    String sql = "UPDATE wizardData SET " + "dataFileType = ? " + "WHERE id = ?";
    int rowsAffected = getJdbcTemplate().update(sql, new Object[] {
        // order matters here
        uploadedFileCmd.getDataFileType(), uploadedFileCmd.getId()});
    if (rowsAffected <= 0) {
      String message = "Unable to update persisted data file type corresponding to id " + id;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    } else {
      logger.info("Updated persisted data file type corresponding to id " + id);
    }


  }


  private static class WizardDataMapper implements RowMapper<String> {
    /**
     * Maps each row of data in the ResultSet to a String.
     *
     * @param rs The ResultSet to be mapped.
     * @param rowNum The number of the current row.
     * @return The data file type.
     * @throws SQLException If an SQLException is encountered getting column values.
     */
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
      String dataFileType = null;
      dataFileType = rs.getString("dataFileType");
      return dataFileType;
    }
  }


  private static class UploadedFileMapper implements RowMapper<UploadedFile> {
    /**
     * Maps each row of data in the ResultSet to the UploadedFile object.
     *
     * @param rs The ResultSet to be mapped.
     * @param rowNum The number of the current row.
     * @return The populated UploadedFile object.
     * @throws SQLException If an SQLException is encountered getting column values.
     */
    public UploadedFile mapRow(ResultSet rs, int rowNum) throws SQLException {
      UploadedFile uploadedFile = new UploadedFile();
      uploadedFile.setId(rs.getString("id"));
      uploadedFile.setFileName(rs.getString("fileName"));
      uploadedFile.setFileType(UploadedFile.FileType.valueOf(rs.getString("fileType")));
      return uploadedFile;
    }
  }
}
