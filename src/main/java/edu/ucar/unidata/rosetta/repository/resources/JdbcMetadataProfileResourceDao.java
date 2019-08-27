/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.MetadataProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of a metadata profile DAO.
 *
 * @author oxelson@ucar.edu
 */
public class JdbcMetadataProfileResourceDao extends JdbcDaoSupport implements MetadataProfileResourceDao {

  /**
   * Looks up and retrieves a list of persisted MetadataProfile objects.
   *
   * @return A List of all persisted metadata profiles.
   * @throws DataAccessException If unable to retrieve persisted metadata profile.
   */
  public List<MetadataProfile> getMetadataProfiles() throws DataAccessException {
    String sql = "SELECT metadataProfiles.id, metadataProfiles.name, communities.name FROM "
        + "metadataProfiles INNER JOIN communities ON metadataProfiles.community = communities.id";

    return getJdbcTemplate().query(sql, (ResultSetExtractor<List<MetadataProfile>>) rs -> {
      Map<String, MetadataProfile> metadataProfileMap = new HashMap<>();
      while (rs.next()) {
        String metadataProfileName = rs.getString(2);
        if (!metadataProfileMap.containsKey(metadataProfileName)) {
          // Update MetadataProfile object.
          MetadataProfile metadataProfile = new MetadataProfile();
          metadataProfile.setId(rs.getInt(1));
          metadataProfile.setName(metadataProfileName);
          Community community = new Community();
          community.setName(rs.getString(3));
          metadataProfile.addToCommunities(community);
          metadataProfileMap.put(metadataProfileName, metadataProfile);
        } else {
          // Create new MetadataProfile object.
          MetadataProfile metadataProfile = metadataProfileMap.get(metadataProfileName);
          Community community = new Community();
          community.setName(rs.getString(3));
          metadataProfile.addToCommunities(community);
          metadataProfileMap.replace(metadataProfileName, metadataProfile);
        }
      }
      return new ArrayList<>(metadataProfileMap.values());
    });
  }

  /**
   * Looks up and retrieves a persisted MetadataProfile object using the provided name.
   *
   * @param name The name of the metadata profile to retrieve.
   * @return The MetadataProfile object matching the provided name.
   * @throws DataRetrievalFailureException If unable to retrieve persisted metadata profile.
   */
  public MetadataProfile lookupMetadataProfileByName(String name) throws DataRetrievalFailureException {
    String sql =
        "SELECT metadataProfiles.id, metadataProfiles.name, communities.name FROM metadataProfiles LEFT JOIN communities ON metadataProfiles.fileType = communities.id WHERE metadataProfiles.name = '"
            + name + "'";
    return getJdbcTemplate().query(sql, rs -> {
      MetadataProfile metadataProfile = new MetadataProfile();
      metadataProfile.setName(name);
      while (rs.next()) {
        Community community = new Community();
        community.setName(rs.getString(3));
        metadataProfile.addToCommunities(community);
      }
      return metadataProfile;
    });
  }

}
