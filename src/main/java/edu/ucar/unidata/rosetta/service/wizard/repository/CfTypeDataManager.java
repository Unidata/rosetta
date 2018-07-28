package edu.ucar.unidata.rosetta.service.wizard.repository;

import edu.ucar.unidata.rosetta.domain.wizard.CfTypeData;

public interface CfTypeDataManager {

  public CfTypeData lookupPersistedCfTypeDataById(String id);

  public void persistCfTypeData(CfTypeData cfTypeData);

  public void updatePersistedCfTypeData(CfTypeData cfTypeData);

}
