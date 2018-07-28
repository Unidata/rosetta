package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.CfType;
import java.util.List;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * The data access object representing a CF type.
 *
 * @author oxelson@ucar.edu
 */
public interface CfTypeDao {

  /**
   * Looks up and retrieves a list of persisted CfType objects.
   *
   * @return A List of all persisted file types.
   * @throws DataRetrievalFailureException If unable to retrieve persisted file types.
   */
  public List<CfType> getCfTypes() throws DataRetrievalFailureException;

  /**
   * Looks up and retrieves a persisted CfType object using the provided id.
   *
   * @param id The id of the file type to retrieve.
   * @return The CfType object matching the provided id.
   * @throws DataRetrievalFailureException If unable to retrieve persisted file type.
   */
  public CfType lookupCfTypeById(int id) throws DataRetrievalFailureException;

  /**
   * Looks up and retrieves a persisted CfType object using the provided name.
   *
   * @param name The name of the file type to retrieve.
   * @return The CfType object matching the provided name.
   * @throws DataRetrievalFailureException If unable to retrieve persisted file type.
   */
  public CfType lookupCfTypeByName(String name) throws DataRetrievalFailureException;
}
