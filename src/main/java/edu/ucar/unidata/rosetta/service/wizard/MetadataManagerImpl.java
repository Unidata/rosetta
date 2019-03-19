/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.repository.wizard.MetadataProfileDao;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

/**
 * Implements MetadataManager functionality.
 */
public class MetadataManagerImpl implements MetadataManager {

    private MetadataProfileDao metadataProfileDao;

    // The other managers we make use of in this file.
    @Resource(name = "wizardManager")
    private WizardManager wizardManager;

    /**
     * Method to filter metadata attributes that will be auto-computed.  These attributes do not need
     * to be displayed in the wizard.  As per
     * https://github.com/Unidata/rosetta/wiki/Using-Metadata-Profiles-in-Rosetta#which-metadata-profiles-to-use
     *
     * @param profile   The MetadataProfile object to examine.
     * @return  true if the MetadataProfile object matches an ignored attribute; otherwise false.
     */
    private boolean filterOutIgnored(MetadataProfile profile) {
        for (MetadataProfile ignored : getIgnoredMetadataProfileAttributes()) {
            if (profile.getMetadataType().equals(ignored.getMetadataType()) && profile.getAttributeName().equals(ignored.getAttributeName())) {
                if (profile.getMetadataProfileName().equals("eTUFF")) {
                    if (profile.getMetadataGroup().equals("deployment") || profile.getMetadataGroup().equals("end_of_mission")) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves the compliance level for the give attribute.
     *
     * @param attributeName  The name of the attribute.
     * @return  The compliance level of the attribute.
     */
    public String getComplianceLevelForAttribute(String id, String attributeName) {
        String complianceLevel = null;
        // Get the metadata profiles corresponding to the ID.
        WizardData wizardData = wizardManager.lookupPersistedWizardDataById(id);
        for (String metadataProfile : wizardData.getMetadataProfile().split(",")) {
            complianceLevel = metadataProfileDao.getComplianceLevelForAttribute(attributeName, metadataProfile);
            if (complianceLevel != null) {
                break;
            }
        }
        return complianceLevel;
    }

    /**
     * Retrieves the persisted metadata profile attributes to ignore in the wizard interface.
     *
     * @return  A list of MetadataProfile objects containing the attributes to ignore.
     */
    private List<MetadataProfile> getIgnoredMetadataProfileAttributes() {
        return metadataProfileDao.getIgnoredMetadataProfileAttributes();
    }

    /**
     * Returns all eTUFF metadata profiles.
     *
     * @return  All eTUFF metadata profiles.
     */
    public List<MetadataProfile> getMetadataProfile(String metadataProfile) {
        return metadataProfileDao.getMetadataProfileByType(metadataProfile);
    }

    /**
     * Finds and returns all the metadata profiles corresponding to the given ID
     * that have the given metadataType value.
     *
     * @param id    The unique ID associated with the corresponding WizardData object.
     * @param metadataType  The metadataType of interest (variable or global).
     * @return  A list of all matching metadata profiles.
     */
    public List<MetadataProfile> getMetadataProfiles(String id, String metadataType) {
        List<MetadataProfile> metadataProfiles = new ArrayList<>();

        // Create a list of metadata type values for filtering based on the given metadataType value.
        List<String> metadataTypeValues = new ArrayList<>();
        if (metadataType.equals("variable")) {
            // Variable metadata.
            metadataTypeValues.add("CoordinateVariable");
            metadataTypeValues.add("DataVariable");
        } else {
            // Global metadata.
            metadataTypeValues.add("Global");
            metadataTypeValues.add("MetadataGroup");
        }

        // Get the metadata profiles corresponding to the ID.
        WizardData wizardData = wizardManager.lookupPersistedWizardDataById(id);
        for (String metadataProfile : wizardData.getMetadataProfile().split(",")) {
            List<MetadataProfile> profiles = metadataProfileDao.getMetadataProfileByType(metadataProfile);
            // Remove the unwanted metadata types (keep either global or variable from assignment above).
            profiles.removeIf(p -> !metadataTypeValues.contains(p.getMetadataType()));
            // Filter out metadata attributes that will be auto-computed.
            profiles.removeIf(this::filterOutIgnored);
            // Add to metadataProfiles list.
            metadataProfiles.addAll(profiles);
        }
        return metadataProfiles.stream().distinct().collect(Collectors.toList());

    }

    /**
     * Sets the data access object (DAO) for the MetadataProfile object.
     *
     * @param metadataProfileDao The service DAO representing a MetadataProfile object.
     */
    public void setMetadataProfileDao(MetadataProfileDao metadataProfileDao) {
        this.metadataProfileDao = metadataProfileDao;
    }
}
