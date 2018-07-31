package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.resources.CfType;
import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.Delimiter;
import edu.ucar.unidata.rosetta.domain.resources.FileType;
import edu.ucar.unidata.rosetta.domain.resources.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.resources.Platform;
import edu.ucar.unidata.rosetta.repository.resources.CfTypeDao;
import edu.ucar.unidata.rosetta.repository.resources.CommunityDao;
import edu.ucar.unidata.rosetta.repository.resources.DelimiterDao;
import edu.ucar.unidata.rosetta.repository.resources.FileTypeDao;
import edu.ucar.unidata.rosetta.repository.resources.MetadataProfileDao;
import edu.ucar.unidata.rosetta.repository.resources.PlatformDao;
import java.util.List;

/**
 * Implements resource manager functionality.
 *
 * @author oxelson@ucar.edu
 */
public class ResourceManagerImpl implements ResourceManager {

  // Resource DAOS.
  private CfTypeDao cfTypeDao;
  private CommunityDao communityDao;
  private DelimiterDao delimiterDao;
  private FileTypeDao fileTypeDao;
  private MetadataProfileDao metadataProfileDao;
  private PlatformDao platformDao;

  /**
   * Retrieves a list of all the persisted Delimiter objects.
   *
   * @return The Delimiter objects.
   */
  @Override
  public List<Delimiter> getDelimiters() {
    return delimiterDao.getDelimiters();
  }

  /**
   * Returns the symbol corresponding to the given delimiter string.
   *
   * @param delimiter The delimiter string.
   * @return The symbol corresponding to the given string.
   */
  @Override
  public String getDelimiterSymbol(String delimiter) {
    return delimiterDao.lookupDelimiterByName(delimiter).getCharacterSymbol();
  }


  /**
   * Retrieves the CF Types associated with the given platform.
   *
   * @param platform The platform.
   * @return The CF Types associated with the given platform.
   */
  @Override
  public String getCFTypeFromPlatform(String platform) {
    Platform persistedPlatform = platformDao.lookupPlatformByName(platform);
    return persistedPlatform.getCfType();
  }

  /**
   * Retrieves a list of all the persisted CfType objects.
   *
   * @return A list of CfType objects.
   */
  @Override
  public List<CfType> getCfTypes() {
    return cfTypeDao.getCfTypes();
  }

  /**
   * Retrieves a list of all the persisted communities.
   *
   * @return A list of Community objects.
   */
  @Override
  public List<Community> getCommunities() {
    List<Community> communities = communityDao.getCommunities();
    for (Community community : communities) {
      // Get the associated platforms and add them to the Community object.
      List<Platform> platforms = platformDao.lookupPlatformsByCommunity(community.getName());
      community.setPlatforms(platforms);
      communities.set(communities.indexOf(community), community);
    }
    return communities;
  }

  /**
   * Retrieves the community associated with the given platform.
   *
   * @param platform The platform.
   * @return The community associated with the given platform.
   */
  @Override
  public String getCommunityFromPlatform(String platform) {
    Platform persistedPlatform = platformDao.lookupPlatformByName(platform.replaceAll("_", " "));
    return persistedPlatform.getCommunity();
  }

  /**
   * Retrieves a list of all the persisted FileType objects.
   *
   * @return A list of FileType objects.
   */
  @Override
  public List<FileType> getFileTypes() {
    return fileTypeDao.getFileTypes();
  }

  /**
   * Retrieves a list of all the persisted MetadataProfile objects.
   *
   * @return A list of MetadataProfile objects.
   */
  @Override
  public List<MetadataProfile> getMetadataProfiles() {
    return metadataProfileDao.getMetadataProfiles();
  }

  /**
   * Lookups and returns a Platform using the provided name.
   *
   * @param name The name of the platform to retrieve.
   * @return The Platform matching the provided name.
   */
  @Override
  public Platform getPlatform(String name) {
    return platformDao.lookupPlatformByName(name);
  }

  /**
   * Retrieves a list of all the persisted Platform objects.
   *
   * @return A list of Platform objects.
   */
  @Override
  public List<Platform> getPlatforms() {
    return platformDao.getPlatforms();
  }

  /**
   * Sets the data access object (DAO) for the CFType object which will acquire and persist the data
   * passed to it via the methods of this DataManager.
   *
   * @param cfTypeDao The service DAO representing a CfType object.
   */
  public void setCfTypeDao(CfTypeDao cfTypeDao) {
    this.cfTypeDao = cfTypeDao;
  }

  /**
   * Sets the data access object (DAO) for the Community object which will acquire and persist the
   * data passed to it via the methods of this DataManager.
   *
   * @param communityDao The service DAO representing a Community object.
   */
  public void setCommunityDao(CommunityDao communityDao) {
    this.communityDao = communityDao;
  }

  /**
   * Sets the data access object (DAO) for the Delimiter object which will acquire and persist the
   * data passed to it via the methods of this DataManager.
   *
   * @param delimiterDao The service DAO representing a Delimiter object.
   */
  public void setDelimiterDao(DelimiterDao delimiterDao) {
    this.delimiterDao = delimiterDao;
  }

  /**
   * Sets the data access object (DAO) for the FileType object which will acquire and persist the
   * data passed to it via the methods of this DataManager.
   *
   * @param fileTypeDao The service DAO representing a FileType object.
   */
  public void setFileTypeDao(FileTypeDao fileTypeDao) {
    this.fileTypeDao = fileTypeDao;
  }

  /**
   * Sets the data access object (DAO) for the Platform object which will acquire and persist the
   * data passed to it via the methods of this DataManager.
   *
   * @param platformDao The service DAO representing a Platform object.
   */
  public void setPlatformDao(PlatformDao platformDao) {
    this.platformDao = platformDao;
  }

}
