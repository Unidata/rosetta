package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.FileType;
import java.util.List;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * The data access object representing a file type.
 *
 * @author oxelson@ucar.edu
 */
public interface FileTypeDao {

  /**
   * Looks up and retrieves a list of persisted FileType objects.
   *
   * @return A List of all persisted file types.
   * @throws DataRetrievalFailureException If unable to retrieve persisted file types.
   */
  public List<FileType> getFileTypes() throws DataRetrievalFailureException;

  /**
   * Looks up and retrieves a persisted FileType object using the provided id.
   *
   * @param id The id of the file type to retrieve.
   * @return The FileType object matching the provided id.
   * @throws DataRetrievalFailureException If unable to retrieve persisted file type.
   */
  public FileType lookupFileTypeById(int id) throws DataRetrievalFailureException;

  /**
   * Looks up and retrieves a persisted FileType object using the provided name.
   *
   * @param name The name of the file type to retrieve.
   * @return The FileType object matching the provided name.
   * @throws DataRetrievalFailureException If unable to retrieve persisted file type.
   */
  public FileType lookupFileTypeByName(String name) throws DataRetrievalFailureException;

}
