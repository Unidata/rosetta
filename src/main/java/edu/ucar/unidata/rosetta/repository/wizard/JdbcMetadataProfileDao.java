/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.MetadataProfile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class JdbcMetadataProfileDao extends JdbcDaoSupport implements MetadataProfileDao {

    private static final Logger logger = Logger.getLogger(JdbcMetadataProfileDao.class);

    /**
     * Retrieves the metadata profile attributes to ignore in the wizard interface.
     *
     * @return  A list of MetadataProfile objects containing the attributes to ignore.
     */
    public List<MetadataProfile> getIgnoredMetadataProfileAttributes() {
        String sql = "SELECT * FROM ignoreList";
        return getJdbcTemplate().query(sql, (ResultSetExtractor<List<MetadataProfile>>) rs -> {
            List<MetadataProfile> metadataProfiles = new ArrayList<>();
            while (rs.next()) {
                // Create MetadataProfile object.
                MetadataProfile metadataProfile = new MetadataProfile();
                metadataProfile.setMetadataType(rs.getString(2));
                metadataProfile.setAttributeName(rs.getString(3));
                metadataProfiles.add(metadataProfile);
            }
            return metadataProfiles;
        });
    }

    /**
     * Retrieves the persisted metadata profile associated with the given type.
     *
     * @param metadataProfileType  The metadata profile type.
     * @return  A list of MetadataProfile objects created from the persisted metadata profile data.
     */
    public List<MetadataProfile> getMetadataProfileByType(String metadataProfileType) {
        String sql = "SELECT * FROM mpsMetadataProfiles WHERE metadataProfileName = ?";
        List<MetadataProfile> metadataProfile = getJdbcTemplate().query(sql, new JdbcMetadataProfileDao.MetadataProfileMapper(), metadataProfileType);
        if (metadataProfile.isEmpty()) {
            String message = "Unable to find persisted metadata profiles for metadataProfileType " + metadataProfileType;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        }
        return metadataProfile;

    }

    /**
     * Data mapper class for MetadataProfile data.
     */
    private static class MetadataProfileMapper implements RowMapper<MetadataProfile> {

        /**
         * Maps each row of data in the ResultSet to the MetadataProfile object.
         *
         * @param rs     The ResultSet to be mapped.
         * @param rowNum The number of the current row.
         * @return The populated MetadataProfile object.
         * @throws SQLException If an SQLException is encountered getting column values.
         */
        public MetadataProfile mapRow(ResultSet rs, int rowNum) throws SQLException {
            MetadataProfile metadataProfile = new MetadataProfile();
            metadataProfile.setId(rs.getString("id"));
            metadataProfile.setAttributeName(rs.getString("attributeName"));
            metadataProfile.setComplianceLevel(rs.getString("complianceLevel"));
            metadataProfile.setDescription(rs.getString("description"));
            metadataProfile.setDisplayName(rs.getString("displayName"));
            metadataProfile.setExampleValues(rs.getString("exampleValues"));
            metadataProfile.setMetadataGroup(rs.getString("metadataGroup"));
            metadataProfile.setMetadataProfileName(rs.getString("metadataProfileName"));
            metadataProfile.setMetadataProfileVersion(rs.getString("metadataProfileVersion"));
            metadataProfile.setMetadataType(rs.getString("metadataType"));
            metadataProfile.setMetadataTypeStructureName(rs.getString("metadataTypeStructureName"));
            metadataProfile.setMetadataValueType(rs.getString("metadataValueType"));
            return metadataProfile;
        }
    }

}
