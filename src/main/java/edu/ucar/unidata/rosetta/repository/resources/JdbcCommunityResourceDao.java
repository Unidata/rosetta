/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Community;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of a community DAO.
 *
 * @author oxelson@ucar.edu
 */
public class JdbcCommunityResourceDao extends JdbcDaoSupport implements CommunityResourceDao {

  /**
   * Looks up and retrieves a list of persisted Community objects.
   *
   * @return A List of all persisted communities.
   * @throws DataAccessException If unable to retrieve persisted communities.
   */
  public List<Community> getCommunities() throws DataAccessException {
    String sql =
        "SELECT communities.id, communities.name, fileTypes.name FROM communities INNER JOIN fileTypes ON communities.fileType = fileTypes.id";
    return getCommunities(sql);
  }

  /**
   * Looks up and retrieves a persisted Community object using the provided name.
   *
   * @param name The name of the community to retrieve.
   * @return The Community object matching the provided name.
   * @throws DataAccessException If unable to retrieve persisted community.
   */
  public Community lookupCommunityByName(String name) throws DataAccessException {
    String sql =
        "SELECT communities.id, communities.name, fileTypes.name FROM communities LEFT JOIN fileTypes ON communities.fileType = fileTypes.id WHERE communities.name = '"
            + name + "'";
    return getJdbcTemplate().query(sql, rs -> {
      Community comm = new Community();
      comm.setName(name);
      while (rs.next()) {
        comm.addToFileType(rs.getString(3));
      }
      return comm;
    });
  }

  /**
   * Lookups and returns a list of persisted Community objects using the provided file type.
   *
   * @param fileType The file type of the community to retrieve.
   * @return A List of persisted Communities matching the provided file type.
   * @throws DataAccessException If unable to retrieve persisted Communities.
   */
  public List<Community> lookupCommunitiesByFileType(String fileType) throws DataAccessException {
    String sql =
        "SELECT communities.id, communities.name, fileTypes.name FROM communities LEFT JOIN fileTypes ON communities.fileType = fileTypes.id WHERE fileTypes.name = '"
            + fileType + "'";
    return getCommunities(sql);
  }

  /**
   * Performs the given SQL query and returns a list of Community objects.
   *
   * @param sql The SQL select query to perform.
   * @return A list of community objects populated according to the SQL query.
   * @throws DataAccessException If unable to execute and retrieve the SQL query successfully.
   */
  private List<Community> getCommunities(String sql) throws DataAccessException {
    return getJdbcTemplate().query(sql, (ResultSetExtractor<List<Community>>) rs -> {
      Map<String, Community> communityMap = new HashMap<>();
      while (rs.next()) {
        String communityName = rs.getString(2);
        if (!communityMap.containsKey(communityName)) {
          // Update Community object.
          Community community = new Community();
          community.setId(rs.getInt(1));
          community.setName(communityName);
          community.addToFileType(rs.getString(3));
          communityMap.put(communityName, community);
        } else {
          // Create new Community object.
          Community community = communityMap.get(communityName);
          community.addToFileType(rs.getString(3));
          communityMap.replace(communityName, community);
        }
      }
      return new ArrayList<>(communityMap.values());
    });
  }
}
