package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Community;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcCommunityDao  extends JdbcDaoSupport implements CommunityDao {
    
    protected static Logger logger = Logger.getLogger(JdbcCommunityDao.class);

    private SimpleJdbcInsert insertActor;
    
    /**
     * Looks up and retrieves a list of persisted Community objects.
     *
     * @return  A List of all persisted communities.
     * @throws DataRetrievalFailureException  If unable to retrieve persisted communities.
     */
    public List<Community> getCommunities() throws DataRetrievalFailureException {
        String sql = "SELECT * FROM community";
        List<Community> communities = getJdbcTemplate().query(sql, new JdbcCommunityDao.CommunityMapper());
        if (communities.isEmpty()) {
            String message = "Unable to find persisted Community objects.";
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        }
        return mergeCommunities(communities);
    }

    /**
     * Looks up and retrieves a persisted Community object using the provided name.
     *
     * @param name The name of the community to retrieve.
     * @return  The Community object matching the provided name.
     * @throws DataRetrievalFailureException If unable to retrieve persisted community.
     */
    public Community lookupCommunityByName(String name) throws DataRetrievalFailureException {
        String sql = "SELECT * FROM community WHERE name = ?";
        List<Community> communities = getJdbcTemplate().query(sql, new JdbcCommunityDao.CommunityMapper(), name);
        if (communities.isEmpty()) {
            String message = "Unable to find persisted Community object corresponding to name " + name;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        }
        return communities.get(0);
    }

    /**
     * Lookups and returns a list of persisted Community objects using the provided file type.
     *
     * @param fileType The file type of the community to retrieve.
     * @return   A List of persisted Communities matching the provided file type.
     * @throws DataRetrievalFailureException  If unable to retrieve persisted Communities.
     */
    public List<Community> lookupCommunitiesByFileType(String fileType) throws DataRetrievalFailureException {
        String sql = "SELECT * FROM community WHERE fileType = ?";
        List<Community> communities = getJdbcTemplate().query(sql, new JdbcCommunityDao.CommunityMapper(), fileType);
        if (communities.isEmpty()) {
            String message = "Unable to find persisted Community object corresponding to fileType " + fileType;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        }
        return communities;
    }

    /**
     * Merges Community objects so that there are no duplicates.
     * TODO: redo how community info is stored in the db to avoid this.
     *
     * @param communities The list of un-merged community objects.
     * @return The list of merged Community Objects.
     */
    private List<Community> mergeCommunities(List<Community> communities) {
        List<Community> mergedCommunities = new ArrayList<>();
        // Merge the community objects.
        Map<String, List<String>> communityData = new HashMap<>();
        for (Community community : communities) {
            String name = community.getName();
            if (!communityData.containsKey(name)) {
                communityData.put(name, community.getFileType());
            } else {
                List<String> fileTypes = communityData.get(name);
                fileTypes.addAll(community.getFileType());
                communityData.replace(name, fileTypes);
            }
        }
        for (Map.Entry<String, List<String>> entry : communityData.entrySet()) {
            Community community = new Community();
            community.setName(entry.getKey());
            community.setFileType(entry.getValue());
            mergedCommunities.add(community);
        }
        return mergedCommunities;
    }


    /**
     * This CommunityMapper only used by JdbcCommunityDao.
     */
    private static class CommunityMapper implements RowMapper<Community> {
        /**
         * Maps each row of data in the ResultSet to the Community object.
         *
         * @param rs  The ResultSet to be mapped.
         * @param rowNum  The number of the current row.
         * @return  The populated Community object.
         * @throws SQLException  If an SQLException is encountered getting column values.
         */
        public Community mapRow(ResultSet rs, int rowNum) throws SQLException {
            Community community = new Community();
            community.setId(rs.getInt("id"));
            community.setName(rs.getString("name"));
            community.setFileType(rs.getString("FileType"));
            return community;
        }
    }

}
