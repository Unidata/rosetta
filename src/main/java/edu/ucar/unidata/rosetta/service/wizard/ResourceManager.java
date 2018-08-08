/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.resources.CfType;
import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.Delimiter;
import edu.ucar.unidata.rosetta.domain.resources.FileType;
import edu.ucar.unidata.rosetta.domain.resources.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.resources.Platform;
import java.util.List;

/**
 * Service for handling collected resource information.
 *
 * @author oxelson@ucar.edu
 */
public interface ResourceManager {

  /**
   * Retrieves the CF Types associated with the given platform.
   *
   * @param platform The platform.
   * @return The CF Types associated with the given platform.
   */
  public String getCFTypeFromPlatform(String platform);

  /**
   * Retrieves a list of all the persisted CfType objects.
   *
   * @return A list of CfType objects.
   */
  public List<CfType> getCfTypes();

  /**
   * Retrieves a list of all the persisted communities.
   *
   * @return A list of Community objects.
   */
  public List<Community> getCommunities();

  /**
   * Retrieves the community associated with the given platform.
   *
   * @param platform The platform.
   * @return The community associated with the given platform.
   */
  public String getCommunityFromPlatform(String platform);

  /**
   * Retrieves a list of all the persisted Delimiter objects.
   *
   * @return The Delimiter objects.
   */
  public List<Delimiter> getDelimiters();

  /**
   * Returns the symbol corresponding to the given delimiter string.
   *
   * @param delimiter The delimiter string.
   * @return The symbol corresponding to the given string.
   */
  public String getDelimiterSymbol(String delimiter);

  /**
   * Retrieves a list of all the persisted FileType objects.
   *
   * @return A list of FileType objects.
   */
  public List<FileType> getFileTypes();

  /**
   * Retrieves a list of all the persisted MetadataProfile objects.
   *
   * @return A list of MetadataProfile objects.
   */
  public List<MetadataProfile> getMetadataProfiles();

  /**
   * Lookups and returns a Platform using the provided name.
   *
   * @param name The name of the platform to retrieve.
   * @return The Platform matching the provided name.
   */
  public Platform getPlatform(String name);

  /**
   * Retrieves a list of all the persisted Platform objects.
   *
   * @return A list of Platform objects.
   */
  public List<Platform> getPlatforms();

}
