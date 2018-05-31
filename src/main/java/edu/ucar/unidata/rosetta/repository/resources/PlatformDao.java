package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Platform;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.List;

/**
 * @author oxelson@ucar.edu
 */
public interface PlatformDao {

    /**
     * Looks up and retrieves a list of persisted Platform objects.
     *
     * @return  A List of all persisted Platforms.
     * @throws DataRetrievalFailureException  If unable to retrieve persisted Platforms.
     */
    public List<Platform> getPlatforms() throws DataRetrievalFailureException;

    /**
     * Lookups and returns persisted Platform using the provided name.
     *
     * @param name The name of the platform to retrieve.
     * @return  The Platform matching the provided name.
     * @throws DataRetrievalFailureException  If unable to retrieve persisted Platform.
     */
    public Platform lookupPlatformByName(String name) throws DataRetrievalFailureException;

    /**
     * Lookups and returns a list of persisted Platform using the provided CF type.
     *
     * @param cfType The CF type of the platforms to retrieve.
     * @return   A List of persisted Platforms matching the provided CF type.
     * @throws DataRetrievalFailureException  If unable to retrieve persisted Platforms.
     */
    public List<Platform> lookupPlatformsByCfType(String cfType) throws DataRetrievalFailureException;

    /**
     * Lookups and returns a list of persisted Platform using the provided community.
     *
     * @param community The community of the platforms to retrieve.
     * @return   A List of persisted Platforms matching the provided community.
     * @throws DataRetrievalFailureException  If unable to retrieve persisted Platforms.
     */
    public List<Platform> lookupPlatformsByCommunity(String community) throws DataRetrievalFailureException;

}
