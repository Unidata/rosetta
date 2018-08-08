package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.resources.CfType;
import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.Delimiter;
import edu.ucar.unidata.rosetta.domain.resources.FileType;
import edu.ucar.unidata.rosetta.domain.resources.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.resources.Platform;
import edu.ucar.unidata.rosetta.repository.resources.CfTypeResourceDao;
import edu.ucar.unidata.rosetta.repository.resources.CommunityResourceDao;
import edu.ucar.unidata.rosetta.repository.resources.DelimiterResourceDao;
import edu.ucar.unidata.rosetta.repository.resources.FileTypeResourceDao;
import edu.ucar.unidata.rosetta.repository.resources.MetadataProfileResourceDao;
import edu.ucar.unidata.rosetta.repository.resources.PlatformResourceDao;

import java.util.List;

/**
 * Implements resource manager functionality.
 *
 * @author oxelson@ucar.edu
 */
public class ResourceManagerImpl implements ResourceManager {

  // Resource DAOS.
  private CfTypeResourceDao cfTypeResourceDao;
  private CommunityResourceDao communityResourceDao;
  private DelimiterResourceDao delimiterResourceDao;
  private FileTypeResourceDao fileTypeResourceDao;
  private MetadataProfileResourceDao metadataProfileResourceDao;
  private PlatformResourceDao platformResourceDao;

  /**
   * Retrieves a list of all the persisted Delimiter objects.
   *
   * @return The Delimiter objects.
   */
  @Override
  public List<Delimiter> getDelimiters() {
    return delimiterResourceDao.getDelimiters();
  }

  /**
   * Returns the symbol corresponding to the given delimiter string.
   *
   * @param delimiter The delimiter string.
   * @return The symbol corresponding to the given string.
   */
  @Override
  public String getDelimiterSymbol(String delimiter) {
    return delimiterResourceDao.lookupDelimiterByName(delimiter).getCharacterSymbol();
  }


  /**
   * Retrieves the CF Types associated with the given platform.
   *
   * @param platform The platform.
   * @return The CF Types associated with the given platform.
   */
  @Override
  public String getCFTypeFromPlatform(String platform) {
    Platform persistedPlatform = platformResourceDao.lookupPlatformByName(platform);
    return persistedPlatform.getCfType();
  }

  /**
   * Retrieves a list of all the persisted CfType objects.
   *
   * @return A list of CfType objects.
   */
  @Override
  public List<CfType> getCfTypes() {
    return cfTypeResourceDao.getCfTypes();
  }

  /**
   * Retrieves a list of all the persisted communities.
   *
   * @return A list of Community objects.
   */
  @Override
  public List<Community> getCommunities() {
    List<Community> communities = communityResourceDao.getCommunities();
    for (Community community : communities) {
      // Get the associated platforms and add them to the Community object.
      List<Platform> platforms = platformResourceDao.lookupPlatformsByCommunity(community.getName());
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
    Platform persistedPlatform = platformResourceDao.lookupPlatformByName(platform.replaceAll("_", " "));
    return persistedPlatform.getCommunity();
  }

  /**
   * Retrieves a list of all the persisted FileType objects.
   *
   * @return A list of FileType objects.
   */
  @Override
  public List<FileType> getFileTypes() {
    return fileTypeResourceDao.getFileTypes();
  }

  /**
   * Retrieves a list of all the persisted MetadataProfile objects.
   *
   * @return A list of MetadataProfile objects.
   */
  @Override
  public List<MetadataProfile> getMetadataProfiles() {
    return metadataProfileResourceDao.getMetadataProfiles();
  }

  /**
   * Lookups and returns a Platform using the provided name.
   *
   * @param name The name of the platform to retrieve.
   * @return The Platform matching the provided name.
   */
  @Override
  public Platform getPlatform(String name) {
    return platformResourceDao.lookupPlatformByName(name);
  }

  /**
   * Retrieves a list of all the persisted Platform objects.
   *
   * @return A list of Platform objects.
   */
  @Override
  public List<Platform> getPlatforms() {
    return platformResourceDao.getPlatforms();
  }

  /**
   * Sets the data access object (DAO) for the CFType object.
   *
   * @param cfTypeResourceDao The service DAO representing a CfType object.
   */
  public void setCfTypeResourceDao(CfTypeResourceDao cfTypeResourceDao) {
    this.cfTypeResourceDao = cfTypeResourceDao;
  }

  /**
   * Sets the data access object (DAO) for the Community object.
   *
   * @param communityResourceDao The service DAO representing a Community object.
   */
  public void setCommunityResourceDao(CommunityResourceDao communityResourceDao) {
    this.communityResourceDao = communityResourceDao;
  }

  /**
   * Sets the data access object (DAO) for the Delimiter object.
   *
   * @param delimiterResourceDao The service DAO representing a Delimiter object.
   */
  public void setDelimiterResourceDao(DelimiterResourceDao delimiterResourceDao) {
    this.delimiterResourceDao = delimiterResourceDao;
  }

  /**
   * Sets the data access object (DAO) for the MetadataProfile object.
   *
   * @param metadataProfileResourceDao The service DAO representing a MetadataProfile object.
   */
  public void setMetadataProfileResourceDao(MetadataProfileResourceDao metadataProfileResourceDao) {
    this.metadataProfileResourceDao = metadataProfileResourceDao;
  }

  /**
   * Sets the data access object (DAO) for the FileType object.
   *
   * @param fileTypeResourceDao The service DAO representing a FileType object.
   */
  public void setFileTypeResourceDao(FileTypeResourceDao fileTypeResourceDao) {
    this.fileTypeResourceDao = fileTypeResourceDao;
  }

  /**
   * Sets the data access object (DAO) for the Platform object.
   *
   * @param platformResourceDao The service DAO representing a Platform object.
   */
  public void setPlatformResourceDao(PlatformResourceDao platformResourceDao) {
    this.platformResourceDao = platformResourceDao;
  }

}
