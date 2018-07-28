package edu.ucar.unidata.rosetta.service.wizard.repository;

import edu.ucar.unidata.rosetta.domain.wizard.CfTypeData;
import javax.annotation.Resource;


public class DaoManagerImpl implements DaoManager {

  @Resource(name = "cfTypeDataManager")
  private CfTypeDataManager cfTypeDataManager;

  @Override
  public CfTypeData lookupPersistedCfTypeDataById(String id) {
    return cfTypeDataManager.lookupPersistedCfTypeDataById(id);
  }

  @Override
  public void persistCfTypeData(CfTypeData cfTypeData) {
    cfTypeDataManager.persistCfTypeData(cfTypeData);
  }

  @Override
  public void updatePersistedCfTypeData(CfTypeData cfTypeData) {
    cfTypeDataManager.updatePersistedCfTypeData(cfTypeData);
  }

}
