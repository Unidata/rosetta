/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.repository.wizard.MetadataProfileDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

/**
 * Implements MetadataManager functionality.
 *
 * @author oxelson@ucar.edu
 */
public class MetadataManagerImpl implements MetadataManager {

    protected static final Logger logger = Logger.getLogger(MetadataManagerImpl.class);

    private MetadataProfileDao metadataProfileDao;

    // The other managers we make use of in this file.
    @Resource(name = "wizardManager")
    private WizardManager wizardManager;

    /**
     * Returns all eTUFF metadata profiles.
     *
     * @return  All eTUFF metadata profiles.
     */
    public List<MetadataProfile> getETUFFProfile() {
        return metadataProfileDao.getMetadataProfileByType("eTUFF");
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
            profiles.removeIf(p -> filterOutIgnored(p));
            // Add to metadataProfiles list.
            metadataProfiles.addAll(profiles);
        }
        // Remove any duplicates between different profiles.
        return removeDuplicates(metadataProfiles);
    }

    /**
     * This method compares the metadata profile entries and removes any duplicates that may exist in
     * one or more profiles.
     *
     * @param metadataProfiles  The list of metadata profiles to cull (or not).
     * @return  A list of unique metadata profiles.
     */
    protected List<MetadataProfile> removeDuplicates(List<MetadataProfile> metadataProfiles) {
        Map<String, List<MetadataProfile>> profileByName = new HashMap<>();
        List<String> requestedProfiles = new ArrayList<>();


        for (MetadataProfile metadataProfile : metadataProfiles) {
            String attributeName = metadataProfile.getAttributeName();

            if (requestedProfiles.size() <= 1) {
                // First profile we're examining; just add to map.  No comparisons; just add to the map.
                if (requestedProfiles.size() < 1) {
                    // First time adding profile to list.
                    requestedProfiles.add(metadataProfile.getMetadataProfileName());
                } else {
                    // One profile already exists in the list.

                    if (!requestedProfiles.contains(metadataProfile.getMetadataProfileName())) {
                        // Profile name is not in the list; add additional profile to list.
                        requestedProfiles.add(metadataProfile.getMetadataProfileName());

                        // NOT the first profile we're examining; need to compare values in the map.
                        profileByName = addProfileToMap(profileByName, attributeName, metadataProfile);
                    }
                }

                // First profile - just add.
                if (profileByName.containsKey(attributeName)) {
                    // Attribute name already in map; append to list of metadata profile objects & update map.
                    List<MetadataProfile> updatedList = new ArrayList<>(profileByName.get(attributeName));
                    updatedList.add(metadataProfile);
                    profileByName.remove(attributeName);
                    profileByName.put(attributeName, updatedList);
                } else {
                    // Attribute name is not a key in the map; add it for the first time.
                    List<MetadataProfile> profiles = new ArrayList<>();
                    profiles.add(metadataProfile);
                    profileByName.put(attributeName, profiles);
                }


            } else {
                // NOT the first profile we're examining; need to compare values in the map.
                profileByName = addProfileToMap(profileByName, attributeName, metadataProfile);
            }
        }

        List<MetadataProfile> unique = new ArrayList<>();
        for (Map.Entry<String, List<MetadataProfile>> entry : profileByName.entrySet()) {
            unique.addAll(entry.getValue());
        }
        return unique;
    }

    /**
     * If the provided attribute name is NOT in the map, the profile is added to the map and returned.  If
     * the attribute name IS in the map, the contents of the corresponding list is compared to the provided
     * metadata profile.  If the metadata profile is unique, the profile is added to the map and returned;
     * otherwise that particular profile is not added to the map.
     *
     * @param profileByName     The map containing metadata profile info with the key being the attribute name.
     * @param attributeName     The attribute name of the profile to add to the map (or not).
     * @param metadataProfile   Th profile to add to the map (or not).
     * @return  The updated map of metadata profiles.
     */
    private Map<String, List<MetadataProfile>> addProfileToMap(Map<String, List<MetadataProfile>> profileByName, String attributeName, MetadataProfile metadataProfile) {
        if (profileByName.containsKey(attributeName)) {
            // Attribute name already in map; compare to what is already in map.
            List<MetadataProfile> profiles = profileByName.get(attributeName);
            if (!compareMapContents(profiles, metadataProfile)) {
                // Attribute name already in map; append to list of metadata profile objects & update map.
                List<MetadataProfile> updatedList = new ArrayList<>(profileByName.get(attributeName));
                updatedList.add(metadataProfile);
                profileByName.remove(attributeName);
                profileByName.put(attributeName, updatedList);
            }
        } else {
            // Attribute name is not a key in the map; add it for the first time.
            List<MetadataProfile> profiles = new ArrayList<>();
            profiles.add(metadataProfile);
            profileByName.put(attributeName, profiles);
        }

        return profileByName;
    }

    /**
     * Compares the provided metadata profile to those in the provided list.  Determines if key attributes
     * of the profiles (compliance level, metadata type, and metadata type structure) match.
     *
     * @param profiles  A list of unique profiles.
     * @param metadataProfile  The metadata profile to compare.
     * @return  true if the provided metadata profile matches one in the list in key areas; otherwise false.
     */
    protected boolean compareMapContents(List<MetadataProfile> profiles, MetadataProfile metadataProfile) {
        boolean matches = false;
        for (int i = 0; i < profiles.size(); i++) {
            MetadataProfile profile = profiles.get(i);
            if (metadataProfile.getComplianceLevel().equals(profile.getComplianceLevel()) &&
                    metadataProfile.getMetadataType().equals(profile.getMetadataType()) &&
                    metadataProfile.getMetadataTypeStructureName().equals(profile.getMetadataTypeStructureName())) {
                matches = true;
                break;
            }
        }
        return matches;
    }




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
     * Retrieves the persisted metadata profile attributes to ignore in the wizard interface.
     *
     * @return  A list of MetadataProfile objects containing the attributes to ignore.
     */
    private List<MetadataProfile> getIgnoredMetadataProfileAttributes() {
        return metadataProfileDao.getIgnoredMetadataProfileAttributes();
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
