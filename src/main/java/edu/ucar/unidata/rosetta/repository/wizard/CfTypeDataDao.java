/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.CfTypeData;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * The data access object for CF type and related data .
 */
public interface CfTypeDataDao {

  public void persistCfTypeData(CfTypeData cfTypeData) throws DataRetrievalFailureException;

  public CfTypeData lookupCfDataById(String id) throws DataRetrievalFailureException;

  public void updatePersistedCfTypeData(CfTypeData cfTypeData) throws DataRetrievalFailureException;
}
