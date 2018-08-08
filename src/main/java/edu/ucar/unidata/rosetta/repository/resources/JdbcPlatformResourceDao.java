package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Platform;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of a platform DAO.
 *
 * @author oxelson@ucar.edu
 */
public class JdbcPlatformResourceDao extends JdbcDaoSupport implements PlatformResourceDao {

  protected static Logger logger = Logger.getLogger(JdbcPlatformResourceDao.class);

  private SimpleJdbcInsert insertActor;

  /**
   * Looks up and retrieves a list of all persisted Platform objects.
   *
   * @return A List of all persisted Platforms.
   * @throws DataAccessException If unable to retrieve persisted Platforms.
   */
  public List<Platform> getPlatforms() throws DataAccessException {
    String sql = "SELECT platforms.id, platforms.name, platforms.imgPath, cfTypes.name, communities.name FROM platforms JOIN communities ON platforms.community = communities.id JOIN cfTypes ON platforms.cfType = cfTypes.id";
    return getPlatforms(sql);
  }

  /**
   * Lookups and returns persisted Platform using the provided name.
   *
   * @param name The name of the platform to retrieve.
   * @return The Platform matching the provided name.
   * @throws DataAccessException If unable to retrieve persisted Platform.
   */
  public Platform lookupPlatformByName(String name) throws DataAccessException {
    String sql =
        "SELECT platforms.id, platforms.name, platforms.imgPath, cfTypes.name, communities.name FROM platforms JOIN communities ON platforms.community = communities.id JOIN cfTypes ON platforms.cfType = cfTypes.id WHERE platforms.name = '"
            + name + "'";
    return getJdbcTemplate().query(sql, rs -> {
      Platform platform = new Platform();
      platform.setName(name);
      while (rs.next()) {
        platform.setCommunity(rs.getString(5));
      }
      return platform;
    });
  }

  /**
   * Lookups and returns a list of persisted Platform using the provided CF type.
   *
   * @param cfType The CF type of the platforms to retrieve.
   * @return A List of persisted Platforms matching the provided CF type.
   * @throws DataAccessException If unable to retrieve persisted Platforms.
   */
  public List<Platform> lookupPlatformsByCfType(String cfType) throws DataAccessException {
    String sql =
        "SELECT platforms.id, platforms.name, platforms.imgPath, cfTypes.name, communities.name FROM platforms JOIN communities ON platforms.community = communities.id JOIN cfTypes ON platforms.cfType = cfTypes.id WHERE cfTypes.name = '"
            + cfType + "'";
    return getPlatforms(sql);
  }

  /**
   * Lookups and returns a list of persisted Platform using the provided communities.
   *
   * @param community The community of the platforms to retrieve.
   * @return A List of persisted Platforms matching the provided community.
   * @throws DataAccessException If unable to retrieve persisted Platforms.
   */
  public List<Platform> lookupPlatformsByCommunity(String community) throws DataAccessException {
    String sql =
        "SELECT platforms.id, platforms.name, platforms.imgPath, cfTypes.name, communities.name FROM platforms JOIN communities ON platforms.community = communities.id JOIN cfTypes ON platforms.cfType = cfTypes.id WHERE communities.name = '"
            + community + "'";
    return getPlatforms(sql);
  }

  /**
   * Performs the given SQL query and returns a list of Platform objects.
   *
   * @param sql The SQL select query to perform.
   * @return A list of platform objects populated according to the SQL query.
   * @throws DataAccessException If unable to execute and retrieve the SQL query successfully.
   */
  private List<Platform> getPlatforms(String sql) throws DataAccessException {
    return getJdbcTemplate().query(sql, (ResultSetExtractor<List<Platform>>) rs -> {
      List<Platform> platforms = new ArrayList<>();
      while (rs.next()) {
        // Create Platform object.
        Platform platform = new Platform();
        platform.setId(rs.getInt(1));
        platform.setName(rs.getString(2));
        platform.setImgPath(rs.getString(3));
        platform.setCfType(rs.getString(4));
        platform.setCommunity(rs.getString(5));
        platforms.add(platform);
      }
      return platforms;
    });
  }

}
