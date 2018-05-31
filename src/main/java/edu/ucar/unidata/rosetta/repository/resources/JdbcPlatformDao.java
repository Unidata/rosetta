package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Platform;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcPlatformDao extends JdbcDaoSupport implements PlatformDao {

    protected static Logger logger = Logger.getLogger(JdbcPlatformDao.class);

    private SimpleJdbcInsert insertActor;

    /**
     * Looks up and retrieves a list of persisted Platform objects.
     *
     * @return  A List of all persisted Platforms.
     * @throws DataRetrievalFailureException  If unable to retrieve persisted Platforms.
     */
    public List<Platform> getPlatforms() throws DataRetrievalFailureException {
        String sql = "SELECT * FROM platform";
        List<Platform> platforms = getJdbcTemplate().query(sql, new JdbcPlatformDao.PlatformMapper());
        if (platforms.isEmpty()) {
            String message = "Unable to find persisted Platform objects.";
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        }
        return platforms;
    }

    /**
     * Lookups and returns persisted Platform using the provided name.
     *
     * @param name The name of the platform to retrieve.
     * @return  The Platform matching the provided name.
     * @throws DataRetrievalFailureException  If unable to retrieve persisted Platform.
     */
    public Platform lookupPlatformByName(String name) throws DataRetrievalFailureException {
        String sql = "SELECT * FROM platform WHERE name = ?";
        List<Platform> platforms = getJdbcTemplate().query(sql, new JdbcPlatformDao.PlatformMapper(), name);
        if (platforms.isEmpty()) {
            String message = "Unable to find persisted Platform object corresponding to name " + name;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        }
        return platforms.get(0);
    }

    /**
     * Lookups and returns a list of persisted Platform using the provided CF type.
     *
     * @param cfType The CF type of the platforms to retrieve.
     * @return   A List of persisted Platforms matching the provided CF type.
     * @throws DataRetrievalFailureException  If unable to retrieve persisted Platforms.
     */
    public List<Platform> lookupPlatformsByCfType(String cfType) throws DataRetrievalFailureException {
        String sql = "SELECT * FROM platform WHERE cfType = ?";
        List<Platform> platforms = getJdbcTemplate().query(sql, new JdbcPlatformDao.PlatformMapper(), cfType);
        if (platforms.isEmpty()) {
            String message = "Unable to find persisted Platform objects corresponding to cfType " + cfType;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        }
        return platforms;
    }

    /**
     * Lookups and returns a list of persisted Platform using the provided community.
     *
     * @param community The community of the platforms to retrieve.
     * @return   A List of persisted Platforms matching the provided community.
     * @throws DataRetrievalFailureException  If unable to retrieve persisted Platforms.
     */
    public List<Platform> lookupPlatformsByCommunity(String community) throws DataRetrievalFailureException {
        String sql = "SELECT * FROM platform WHERE community = ?";
        List<Platform> platforms = getJdbcTemplate().query(sql, new JdbcPlatformDao.PlatformMapper(), community);
        if (platforms.isEmpty()) {
            String message = "Unable to find persisted Platform objects corresponding to community " + community;
            logger.error(message);
            throw new DataRetrievalFailureException(message);
        }
        return platforms;
    }

    /**
     * This PlatformMapper only used by JdbcPlatformDao.
     */
    private static class PlatformMapper implements RowMapper<Platform> {
        /**
         * Maps each row of data in the ResultSet to the Platform object.
         *
         * @param rs  The ResultSet to be mapped.
         * @param rowNum  The number of the current row.
         * @return  The populated Platform object.
         * @throws SQLException  If an SQLException is encountered getting column values.
         */
        public Platform mapRow(ResultSet rs, int rowNum) throws SQLException {
            Platform platform = new Platform();
            platform.setId(rs.getInt("id"));
            platform.setName(rs.getString("name"));
            platform.setImgPath(rs.getString("imgPath"));
            platform.setCfType(rs.getString("cfType"));
            platform.setCommunity(rs.getString("community"));
            return platform;
        }
    }

}
