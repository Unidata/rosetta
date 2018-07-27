package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Community;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.List;

/**
 * The data access object representing a community.
 *
 * @author oxelson@ucar.edu
 */
public interface CommunityDao {

  /**
   * Looks up and retrieves a list of persisted Community objects.
   *
   * @return A List of all persisted communities.
   * @throws DataAccessException If unable to retrieve persisted communities.
   */
  public List<Community> getCommunities() throws DataAccessException;

  /**
   * Looks up and retrieves a persisted Community object using the provided name.
   *
   * @param name The name of the community to retrieve.
   * @return The Community object matching the provided name.
   * @throws DataRetrievalFailureException If unable to retrieve persisted community.
   */
  public Community lookupCommunityByName(String name) throws DataRetrievalFailureException;

  /**
   * Lookups and returns a list of persisted Community objects using the provided file type.
   *
   * @param fileType The file type of the community to retrieve.
   * @return A List of persisted Communities matching the provided file type.
   * @throws DataRetrievalFailureException If unable to retrieve persisted Communities.
   */
  public List<Community> lookupCommunitiesByFileType(String fileType)
      throws DataRetrievalFailureException;

}
