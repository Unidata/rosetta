package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.resources.Platform;
import edu.ucar.unidata.rosetta.domain.wizard.CfTypeData;
import edu.ucar.unidata.rosetta.repository.resources.PlatformDao;
import edu.ucar.unidata.rosetta.repository.wizard.CfTypeDataDao;


public class CfTypeDataManagerImpl implements CfTypeDataManager {

  private CfTypeDataDao cfTypeDataDao;
  private PlatformDao platformDao;

  @Override
  public CfTypeData lookupPersistedCfTypeDataById(String id) {
    return cfTypeDataDao.lookupCfDataById(id);
  }

  @Override
  public void persistCfTypeData(CfTypeData cfTypeData) {
    // Get the community associated with the selected platform.
    if (cfTypeData.getPlatform() != null) {
      Platform platform = platformDao
          .lookupPlatformByName(cfTypeData.getPlatform().replaceAll("_", " "));
      cfTypeData.setCommunity(platform.getCommunity());
    }
    cfTypeDataDao.persistCfTypeData(cfTypeData);
  }

  /**
   * Sets the data access object (DAO) for the CfTypeData object.
   *
   * @param cfTypeDataDao The service DAO representing a CfTypeData object.
   */
  public void setCfTypeDataDao(CfTypeDataDao cfTypeDataDao) {
    this.cfTypeDataDao = cfTypeDataDao;
  }

  /**
   * Sets the data access object (DAO) for the Platform object.
   *
   * @param platformDao The service DAO representing a Platform object.
   */
  public void setPlatformDao(PlatformDao platformDao) {
    this.platformDao = platformDao;
  }

  @Override
  public void updatePersistedCfTypeData(CfTypeData cfTypeData) {
    cfTypeDataDao.updatePersistedCfTypeData(cfTypeData);
  }


}
