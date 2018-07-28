package edu.ucar.unidata.rosetta.repository.user;

import edu.ucar.unidata.rosetta.domain.user.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;


/**
 * Implementation of a user DAO.
 *
 * @author oxelson@ucar.edu
 */
public class JdbcUserDao extends JdbcDaoSupport implements UserDao {

  private static final Logger logger = Logger.getLogger(JdbcUserDao.class);

  /**
   * Persists a new user.
   *
   * @param user The user to be created.
   * @return The persisted user.
   * @throws DataAccessException If unable to persists the user.
   */
  public User createUser(User user) throws DataAccessException {
    if (userExists("userName", user.getUserName())) {
      throw new DataRetrievalFailureException(
          "User with user name \"" + user.getUserName() + "\" already exists.");
    } else {
      SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("users")
          .usingGeneratedKeyColumns("userId");
      SqlParameterSource parameters = new BeanPropertySqlParameterSource(user);
      Number newUserId = insertActor.executeAndReturnKey(parameters);
      if (newUserId != null) {
        user.setUserId(newUserId.intValue());
      } else {
        String message = "Unable to persist new user " + user.getUserName();
        logger.error(message);
        throw new DataRetrievalFailureException(message);
      }
    }
    return user;
  }

  /**
   * Finds and removes the persisted user with the given email address.
   *
   * @param emailAddress The email address of the user to locate (will be unique for each user).
   * @throws DataAccessException If unable to find and delete the user.
   */
  public void deleteUser(String emailAddress) throws DataAccessException {
    String sql = "DELETE FROM users WHERE emailAddress = ?";
    int rowsAffected = getJdbcTemplate().update(sql, emailAddress);
    if (rowsAffected <= 0) {
      String message = "Unable to delete User. No user found with email address " + emailAddress;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    } else {
      logger.info("Deleting user with email address " + emailAddress);
    }
  }

  /**
   * Requests a list of all persisted users.
   *
   * @return A list of users.
   */
  public List<User> getUsers() {
    String sql = "SELECT * FROM users ORDER BY dateCreated DESC";
    List<User> users = getJdbcTemplate().query(sql, new UserMapper());
    if (users.isEmpty()) {
      logger.info("No users persisted yet.");
    }
    return users;
  }

  /**
   * Looks up and retrieves a persisted user with the given user ID.
   *
   * @param userId The ID of the user to locate (will be unique for each user).
   * @return The user.
   * @throws DataAccessException If unable to find the user with the given user ID.
   */
  public User lookupUser(int userId) throws DataAccessException {
    String sql = "SELECT * FROM users WHERE userId = ?";
    List<User> users = getJdbcTemplate().query(sql, new UserMapper(), userId);
    if (users.isEmpty()) {
      String message = "Unable to find user with user ID " + userId;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return users.get(0);
  }

  /**
   * Looks up and retrieves a persisted user with the given user name.
   *
   * @param userName The user name of the user to locate (will be unique for each user).
   * @return The user.
   * @throws DataAccessException If unable to find the user with the given user name.
   */
  public User lookupUser(String userName) throws DataAccessException {
    String sql = "SELECT * FROM users WHERE userName = ?";
    List<User> users = getJdbcTemplate().query(sql, new UserMapper(), userName);
    if (users.isEmpty()) {
      String message = "Unable to find user with user name " + userName;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return users.get(0);
  }

  /**
   * Looks up and retrieves a persisted user with the given email address.
   *
   * @param emailAddress The email address of the user to locate (will be unique for each user).
   * @return The user.
   * @throws DataAccessException If unable to find the user with the given email address.
   */
  public User lookupUserByEmailAddress(String emailAddress) throws DataAccessException {
    String sql = "SELECT * FROM users WHERE emailAddress= ?";
    List<User> users = getJdbcTemplate().query(sql, new UserMapper(), emailAddress);
    if (users.isEmpty()) {
      String message = "Unable to find user with email address " + emailAddress;
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    }
    return users.get(0);
  }

  /**
   * Used to determine if the user with provided user name is the same user with the matching
   * (provided) email address.
   *
   * @param userName The user name of the user to locate.
   * @param emailAddress The email address of the user.
   * @return true if it is the same user; otherwise false.
   */
  public boolean sameUser(String userName, String emailAddress) {
    String sql = "SELECT * FROM users WHERE userName = ?";
    List<User> users = getJdbcTemplate().query(sql, new UserMapper(), userName);
    if (users.get(0).getEmailAddress().equals(emailAddress)) {
      return true;
    } else {
      return false;
    }
  }


  /**
   * Updates the persisted user's password .
   *
   * @param user The user whose password needs to be update.
   */
  public void updatePassword(User user) throws DataAccessException {
    String sql = "UPDATE users SET password = ?, dateModified = ? WHERE userName = ?";
    int rowsAffected = getJdbcTemplate().update(sql, new Object[]{
        // order matters here
        user.getPassword(),
        user.getDateModified(),
        user.getUserName()
    });
    if (rowsAffected <= 0) {
      String message =
          "Unable to update user's password.  User with user name " + user.getUserName()
              + " not found.";
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    } else {
      logger.info("Updated password for user: " + user.getUserName());
    }
  }

  /**
   * Saves changes made to an existing persisted User.
   *
   * @param user The existing user with changes that needs to be saved.
   * @return The updated user.
   * @throws DataAccessException If unable to update the persisted user.
   */
  public User updateUser(User user) throws DataAccessException {
    String sql = "UPDATE users SET userName = ?, emailAddress = ?, fullName = ?, accessLevel = ?, accountStatus = ?, dateModified = ? WHERE userId = ?";
    int rowsAffected = getJdbcTemplate().update(sql, new Object[]{
        // order matters here
        user.getUserName(),
        user.getEmailAddress(),
        user.getFullName(),
        user.getAccessLevel(),
        user.getAccountStatus(),
        user.getDateModified(),
        user.getUserId()
    });
    if (rowsAffected <= 0) {
      String message =
          "Unable to update User.  No user with user name " + user.getUserName() + " found.";
      logger.error(message);
      throw new DataRetrievalFailureException(message);
    } else {
      logger.info("Updated user " + user.getUserName());
    }
    return user;
  }

  /**
   * A boolean method used to determine if a user has already been persisted.
   *
   * @param columnName The table column against which run the query.
   * @param stringToQueryFor The data to query for.
   * @return true if the user has already been persisted; otherwise false.
   */
  public boolean userExists(String columnName, String stringToQueryFor) {
    String sql;
    if (columnName.equals("userName")) {
      sql = "SELECT * FROM users WHERE userName = ?";
    } else {
      sql = "SELECT * FROM users WHERE emailAddress = ?";
    }
    List<User> users = getJdbcTemplate().query(sql, new UserMapper(), stringToQueryFor);
    if (!users.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * This DataMapper only used by JdbcUserDao.
   */
  private static class UserMapper implements RowMapper<User> {

    /**
     * Maps each row of data in the ResultSet to the User object.
     *
     * @param rs The ResultSet to be mapped.
     * @param rowNum The number of the current row.
     * @return The populated User object.
     * @throws SQLException If a SQLException is encountered getting column values.
     */
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
      User user = new User();
      user.setUserId(rs.getInt("userId"));
      user.setUserName(rs.getString("userName"));
      user.setPassword(rs.getString("password"));
      user.setAccessLevel(rs.getInt("accessLevel"));
      user.setAccountStatus(rs.getInt("accountStatus"));
      user.setEmailAddress(rs.getString("emailAddress"));
      user.setFullName(rs.getString("fullName"));
      user.setDateCreated(rs.getTimestamp("dateCreated"));
      user.setDateModified(rs.getTimestamp("dateModified"));
      return user;
    }
  }
}