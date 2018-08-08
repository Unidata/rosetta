/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.resources.Platform;
import edu.ucar.unidata.rosetta.domain.wizard.CfTypeData;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.repository.resources.PlatformResourceDao;
import edu.ucar.unidata.rosetta.repository.wizard.CfTypeDataDao;
import edu.ucar.unidata.rosetta.util.PropertyUtils;

/**
 * Implements CF type manager functionality.
 *
 * @author oxelson@ucar.edu
 */
public class CfTypeDataManagerImpl implements CfTypeDataManager {

  private CfTypeDataDao cfTypeDataDao;

  @Resource(name = "resourceManager")
  private ResourceManager resourceManager;

  /**
   * Determines the metadata profile to use based on the data contained in the
   * provided CfTypeData object.  The user may have explicitly specified the
   * profile(s) to use, or we may have to determine them from the community info.
   *
   * @param cfTypeData  The cfTypeData object containing the user input data.
   * @return  The name of the metadata profile.
   * @throws RosettaDataException If unable to determine the metadata profile.
   */
  private String determineMetadataProfile(CfTypeData cfTypeData) throws RosettaDataException {
    // Assign metadata profile to value specified in CfTypeData object.
    String metadataProfile = cfTypeData.getMetadataProfile();

    // If metadata profile value isn't null we can return (below).
    if(metadataProfile == null) {

      // Use the provided community/platform to figure out metadata profile.
      String userSelectedCommunityName = cfTypeData.getCommunity();

      if(userSelectedCommunityName != null) {
        StringBuilder sb = new StringBuilder();
        for (MetadataProfile metadataProfileResource: resourceManager.getMetadataProfiles()) {
          String match = getMetadataProfilefromCommunity(metadataProfileResource, userSelectedCommunityName);
          if (match != null) {
            sb.append(match);
            sb.append(",");
          }
        }
        metadataProfile = sb.toString();
        if (metadataProfile.substring(metadataProfile.length() - 1).equals(",")) {
          metadataProfile = metadataProfile.substring(0, metadataProfile.length() - 1);
        }
      } else {
        // This shouldn't happen!  Something has gone very wrong.
        // Either the platform/community or the the CF type/metadata profile must exist.
        throw new RosettaDataException("Neither metadata profile or community values present: "
                + cfTypeData.toString());
      }
    }
    return metadataProfile;
  }

  /**
   * Looks up and retrieves persisted Cf type data using the given ID.
   *
   * @param id  The ID corresponding to the data to retrieve.
   * @return    The persisted Cf type data.
   */
  @Override
  public CfTypeData lookupPersistedCfTypeDataById(String id) {
    return cfTypeDataDao.lookupCfDataById(id);
  }

  /**
   * Examines the given MetadataProfile object to see if one
   * of its communities matches the provided community name.
   *
   * @param metadataProfileResource The MetadataProfile object to examine.
   * @param communityName   The community name ot match.
   * @return  The name of the metadata profile if matches; otherwise null.
   */
  private String getMetadataProfilefromCommunity(MetadataProfile metadataProfileResource, String communityName) {
    String metadataProfile = null;

    List<Community> communities = metadataProfileResource.getCommunities();

    for (Community community: communities) {
      if (community.getName().equals(communityName)) {
        metadataProfile = metadataProfileResource.getName();
        break;
      }
    }
    return metadataProfile;
  }

  /**
   * Persists the provided CF type data.
   *
   * @param cfTypeData  The CF type data to persist.
   */
  @Override
  public void persistCfTypeData(CfTypeData cfTypeData) {
    // Get the community associated with the selected platform.
    if (cfTypeData.getPlatform() != null) {
      Platform platform = resourceManager.getPlatform(cfTypeData.getPlatform().replaceAll("_", " "));
      cfTypeData.setCommunity(platform.getCommunity());
    }
    cfTypeDataDao.persistCfTypeData(cfTypeData);
  }

  /**
   * Processes the data submitted by the user containing CF type information. If an ID already
   * exists, the persisted data corresponding to that ID is collected and updated with the newly
   * submitted data.  If no ID exists (is null), the data is persisted for the first time.
   *
   * @param id The unique ID corresponding to already persisted data (may be null).
   * @param cfTypeData The CfTypeData object containing user-submitted CF type information.
   * @param request HttpServletRequest used to make unique IDs for new data.
   * @throws RosettaDataException If unable to lookup the metadata profile.
   */
  @Override
  public void processCfType(String id, CfTypeData cfTypeData, HttpServletRequest request)
          throws RosettaDataException {

    // If the ID is present, then there is a cookie.  Combine new with previous persisted data.
    if (id != null) {

      // Get the persisted CF type data corresponding to this ID.
      CfTypeData persistedData = lookupPersistedCfTypeDataById(id);

      // Update platform value (can be null).
      persistedData.setPlatform(cfTypeData.getPlatform());

      // Update community if needed.
      if (cfTypeData.getPlatform() != null) {
        // Set community.
        String community = resourceManager.getCommunityFromPlatform(cfTypeData.getPlatform());
        persistedData.setCommunity(community);

        // Update this object too, as we need it to get the metadata profile info.
        cfTypeData.setCommunity(community);

        // Set metadata profile.
        persistedData.setMetadataProfile(determineMetadataProfile(cfTypeData));

      } else {

        // No platform provided so set community to null.
        persistedData.setCommunity(null);

        // Set the metadata profile to user-selected values.
        persistedData.setMetadataProfile(cfTypeData.getMetadataProfile());
      }

      // Set the CF type.
      persistedData.setCfType(cfTypeData.getCfType());

      // Update persisted CF type data.
      updatePersistedCfTypeData(persistedData);

    } else {
      // No ID yet.  First time persisting CF type data.

      // Create a unique ID for this object.
      cfTypeData.setId(PropertyUtils.createUniqueDataId(request));

      // Set the community if applicable.
      if (cfTypeData.getPlatform() != null) {
        cfTypeData.setCommunity(resourceManager.getCommunityFromPlatform(cfTypeData.getPlatform()));
      }

      // Set metadata profile.
      cfTypeData.setMetadataProfile(determineMetadataProfile(cfTypeData));

      // Persist the Cf type data.
      persistCfTypeData(cfTypeData);
    }
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
   * Updates persisted CF type data with the information in the provided CFTypeData object.
   *
   * @param cfTypeData  The updated CF type data.
   */
  @Override
  public void updatePersistedCfTypeData(CfTypeData cfTypeData) {
    cfTypeDataDao.updatePersistedCfTypeData(cfTypeData);
  }





}
