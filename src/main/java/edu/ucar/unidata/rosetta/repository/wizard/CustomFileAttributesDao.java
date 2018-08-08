/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.CustomFileAttributes;
import org.springframework.dao.DataRetrievalFailureException;

public interface CustomFileAttributesDao {

  public CustomFileAttributes lookupById(String id) throws DataRetrievalFailureException;

  public void updatePersistedData(String id, CustomFileAttributes customFileAttributes) throws DataRetrievalFailureException;

}
