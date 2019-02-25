/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Delimiter;
import java.util.List;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * The data access object representing a delimiter.
 *
 * @author oxelson@ucar.edu
 */
public interface DelimiterResourceDao {

  /**
   * Looks up and retrieves a list of persisted Delimiters objects.
   *
   * @return A List of all persisted delimiters.
   * @throws DataRetrievalFailureException If unable to retrieve persisted delimiters.
   */
  public List<Delimiter> getDelimiters() throws DataRetrievalFailureException;

  /**
   * Looks up and retrieves a persisted Delimiter using the symbol name..
   *
   * @param name The name of the delimiter symbol to retrieve.
   * @return The Delimiter object matching the provided name.
   * @throws DataRetrievalFailureException If unable to retrieve persisted dlimiter.
   */
  public Delimiter lookupDelimiterByName(String name) throws DataRetrievalFailureException;
}
