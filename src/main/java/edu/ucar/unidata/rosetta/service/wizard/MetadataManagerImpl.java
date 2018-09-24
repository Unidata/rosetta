/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.converters.known.etuff.TagUniversalFileFormat;
import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.domain.VariableMetadata;
import edu.ucar.unidata.rosetta.domain.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.repository.wizard.VariableDao;
import edu.ucar.unidata.rosetta.repository.wizard.MetadataProfileDao;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;

import javax.annotation.Resource;
import java.beans.Statement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Implements MetadataManager functionality.
 *
 * @author oxelson@ucar.edu
 */
public class MetadataManagerImpl implements MetadataManager {

    protected static final Logger logger = Logger.getLogger(MetadataManagerImpl.class);

    private VariableDao variableDao;
    private MetadataProfileDao metadataProfileDao;

    // The other managers we make use of in this file.
    @Resource(name = "wizardManager")
    private WizardManager wizardManager;

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
     * Deletes the persisted metadata information using the given id.
     *
     * @param id The id of the metadata information to delete.

    @Override
    public void deletePersistedMetadata(String id) {
        //variableDao.deletePersistedMetadata(id);
    }
         */

    /**
     * Deletes the persisted metadata object information using the given id & type.
     *
     * @param id   The id of the metadata information to delete.
     * @param type The type of the metadata information to delete.

    @Override
    public void deletePersistedMetadata(String id, String type) {

        //variableDao.deletePersistedMetadata(id, type);
    }
         */

    /**
     * Converts metadata to a format useful for AsciiFile (custom file type) for netCDF conversion.
     * TODO: refactor to remove need for AsciiFile.
     *
     * @param id   The unique ID corresponding to the metadata to find.
     * @param type The type of metadata (general or variable).
     * @return The metadata in Map<String, String> form.

    @Override
    public Map<String, String> getGeneralMetadataMap(String id, String type) {
        List<VariableMetadata> metadataList = lookupPersistedMetadata(id, type);
        // Hack to convert metadata to a format useful for AsciiFile for netcdf conversion.
        Map<String, String> generalMetadataMap = new HashMap<>();
        for (VariableMetadata metadata : metadataList) {
            generalMetadataMap.put(metadata.getMetadataKey(), metadata.getMetadataValue());
        }
        return generalMetadataMap;
    }
     */
    /**
     * Pulls the general metadata from a data known file and populates the provided GeneralMetadata
     * object. Uses reflection to perform the population step. If the data file type is a custom file
     * (not a known type) then an empty, non-populated GeneralMetadata object is returned.
     *
     * @param filePath The path to the data file which may contain the metadata we need.
     * @param fileType The data file type.
     * @param metadata The GeneralMetadata object to populate.
     * @return The GeneralMetadata object to populated with the general metadata.
     * @throws RosettaDataException If unable to populate the GeneralMetadata object.

    @Override
    public GeneralMetadata getMetadataFromKnownFile(String filePath, String fileType,
                                                    GeneralMetadata metadata) throws RosettaDataException {

        // Only process known file types.
        if (!fileType.equals("Custom_File_Type")) {
            Map<String, String> globalMetadata = new HashMap<>();

            // Get the metadata from the file by calling the relevant converter.

            if (fileType.equals("eTuff")) {
                // Tag-base archive flat file.
                TagUniversalFileFormat tuffConverter = new TagUniversalFileFormat();
                tuffConverter.parse(filePath);
                globalMetadata = tuffConverter.getGlobalMetadata();
            }

            for (String key : globalMetadata.keySet()) {
                try {
                    // Make setter method string.
                    String setMethod = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
                    for (Method method : metadata.getClass().getDeclaredMethods()) {
                        if (method.getName().equals(setMethod)) {
                            Statement statement = new Statement(metadata, setMethod,
                                    new Object[]{globalMetadata.get(key).trim().replace("\"", "")});
                            statement.execute();
                        }
                    }
                } catch (Exception e) {
                //
                //NOTE: code in the try block actually throws a bunch of different exceptions, including
                //java.lang.Exception itself.  Hence, the use catch of the generic Exception class to
                //catch them all (otherwise I normally would not catch with just java.lang.Exception).

                    throw new RosettaDataException("Unable to populate data object by reflection: " + e);
                }
            }
        }
        return metadata;
    }
                    */

    /**
     * Retrieves the persisted metadata associated with the given id & type. Creates and returns
     * string version of the metadata used by client side.
     *
     * @param id   The id of the metadata.
     * @param type The metadata type.
     * @return The string version of the metadata used by client side.

    @Override
    public String getMetadataStringForClient(String id, String type) {
        try {
            List<VariableMetadata> metadata = lookupPersistedMetadata(id, type);
            return getStringFromParsedVariableMetadata(metadata);
        } catch (DataRetrievalFailureException e) {
            // No data persisted.
            return null;
        }
    }
     */
    /**
     * Creates a string version of metadata used by client side.
     *
     * @param metadataList The parsed metadata.
     * @return The string version of the metadata used by client side.

    @Override
    public String getStringFromParsedVariableMetadata(List<VariableMetadata> metadataList) {
        StringBuilder metadataString = new StringBuilder();
        for (VariableMetadata metadata : metadataList) {
            metadataString.append("<=>");
            metadataString.append(metadata.getMetadataKey());
            metadataString.append("<>");
            metadataString.append(metadata.getMetadataValue());
        }
        return metadataString.delete(0, 3).toString();
    }
     */


    /**
     * Converts metadata to a format useful for AsciiFile (custom file type) for netCDF conversion.
     * TODO: refactor to do remove need for AsciiFile.
     *
     * @param id   The unique ID corresponding to the metadata to find.
     * @param type The type of metadata (general or variable).
     * @return The metadata in Map<String, Map<String,String>> form.

    @Override
    public Map<String, Map<String, String>> getVariableMetadataMap(String id, String type) {
        List<VariableMetadata> metadataList = lookupPersistedMetadata(id, type);
        // Hack to convert metadata to a format useful for AsciiFile for netcdf conversion.
        Map<String, Map<String, String>> variableMetadataMap = new HashMap<>();
        for (VariableMetadata metadata : metadataList) {
            Map<String, String> metadataMapping = new HashMap<>();
            // Omit the 'do not use' entries.
            if (!metadata.getMetadataValue().equals("Do Not Use")) {
                // Only look at the metadata entries
                if (metadata.getMetadataKey().contains("VariableMetadata")) {
                    String[] metadataValues = metadata.getMetadataValue().split(",");

                    for (int i = 0; i < metadataValues.length; i++) {
                        String[] pairs = metadataValues[i].split(":");

                        if (pairs.length == 1) {
                            metadataMapping.put(pairs[0], "");
                        } else {
                            metadataMapping.put(pairs[0], pairs[1]);
                        }
                    }
                }
            }
            variableMetadataMap.put(metadata.getMetadataKey().replace("VariableMetadata", ""), metadataMapping);
        }
        return variableMetadataMap;
    }

     */

    /**
     * Converts metadata to a format useful for AsciiFile (custom file type) for netCDF conversion.
     * TODO: refactor to do remove need for AsciiFile.
     *
     * @param id   The unique ID corresponding to the metadata to find.
     * @param type The type of metadata (general or variable).
     * @return The metadata in Map<String,String> form.

    @Override
    public Map<String, String> getVariableNameMap(String id, String type) {
        List<VariableMetadata> metadataList = lookupPersistedMetadata(id, type);
        // Hack to convert metadata to a format useful for AsciiFile for netcdf conversion.
        Map<String, String> variableNameMap = new HashMap<>();
        for (VariableMetadata metadata : metadataList) {
            // Omit the 'do not use' entries.
            if (!metadata.getMetadataValue().equals("Do Not Use")) {
                // Omit the metadata entries and just grab the names
                if (!metadata.getMetadataKey().contains("VariableMetadata")) {
                    variableNameMap.put(metadata.getMetadataKey(), metadata.getMetadataValue());
                }
            }
        }
        return variableNameMap;
    }
     */

    /**
     * Looks up and retrieves a list of persisted VariableMetadata objects using the given id.
     *
     * @param id The id of the corresponding Data object.
     * @return The VariableMetadata object.

    @Override
    public List<VariableMetadata> lookupPersistedMetadata(String id) {

        return variableDao.lookupMetadata(id);
    }
     */

    /**
     * Looks up and retrieves a list of persisted VariableMetadata objects using the given id & type.
     *
     * @param id   The id of the corresponding Data object.
     * @param type The type of the VariableMetadata.
     * @return The VariableMetadata object.

    @Override
    public List<VariableMetadata> lookupPersistedMetadata(String id, String type) {
       // return variableDao.lookupMetadata(id, type);
    }
     */

    /**
     * Populates metadata objects from the user input provided and places the objects into a list.
     *
     * @param metadata The metadata inputted by the user.
     * @param id       The id of the Data object to which this metadata corresponds.
     * @return A list containing VariableMetadata objects.
     * @throws RosettaDataException If unable to populate the metadata object by reflection.

    @Override
    public List<VariableMetadata> parseGeneralMetadata(GeneralMetadata metadata, String id)
            throws RosettaDataException {
        List<VariableMetadata> parsedGeneralMetadata = new ArrayList<>();

        try {
            for (Method method : metadata.getClass().getDeclaredMethods()) {

                if (Modifier.isPublic(method.getModifiers())
                        && method.getParameterTypes().length == 0
                        && method.getReturnType() != void.class
                        && (method.getName().startsWith("get"))
                        ) {

                    Object value = method.invoke(metadata);

                    if (value != null) {
                        if (value instanceof String) {

                            if (!"".equals(value)) {
                                VariableMetadata m = new VariableMetadata();
                               // m.setId(id);
                              //  m.setType("general");

                                Statement keyStatement = new Statement(m, "setMetadataKey",
                                        new Object[]{method.getName().replaceFirst("get", "").toLowerCase()});
                                keyStatement.execute();
                                Statement valStatement = new Statement(m, "setMetadataValue", new Object[]{value});
                                valStatement.execute();
                                parsedGeneralMetadata.add(m);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

             //NOTE: code in the try block actually throws a bunch of different exceptions, including
            //// java.lang.Exception itself.  Hence, the use catch of the generic Exception class to
             catch them all (otherwise I normally would not catch with just java.lang.Exception).

            throw new RosettaDataException("Unable to populate data object by reflection: " + e);
        }
        return parsedGeneralMetadata;
    }

                */

    /**
     * Parses a string of metadata into VariableMetadata objects and places them into a list.
     *
     * @param goryStringOfMetadata The string of metadata sent from the client-side.
     * @param id                   The id of the corresponding Data object to which the metadata belongs.
     * @return A list containing VariableMetadata objects.

    @Override
    public List<VariableMetadata> parseVariableMetadata(String goryStringOfMetadata, String id) {
        List<VariableMetadata> parsedVariableMetadata = new ArrayList<>();

        String[] keyValuePairs = goryStringOfMetadata.split("<=>");
        for (String pair : keyValuePairs) {
            String[] metadata = pair.split("<>");
            VariableMetadata m = new VariableMetadata();
           // m.setId(id);
         //   m.setType("variable");
            m.setMetadataKey(metadata[0]);
            m.setMetadataValue(metadata[1]);
            parsedVariableMetadata.add(m);
        }
        return parsedVariableMetadata;
    }
     */

    /**
     * Persists the information in the given list of metadata objects.
     *
     * @param metadata The list of VariableMetadata objects to persist.

    @Override
    public void persistMetadata(List<VariableMetadata> metadata) {

       // variableDao.persistMetadata(metadata);
    }
     */

    /**
     * Persists the information in the give metadata object.
     *
     * @param metadata The VariableMetadata object to persist.

    @Override
    public void persistMetadata(VariableMetadata metadata) {

       // variableDao.persistMetadata(metadata);
    }
     */

    /**
     * Sets the data access object (DAO) for the MetadataProfile object.
     *
     * @param metadataProfileDao The service DAO representing a MetadataProfile object.
     */
    public void setMetadataProfileDao(MetadataProfileDao metadataProfileDao) {
        this.metadataProfileDao = metadataProfileDao;
    }


    /**
     * Sets the data access object (DAO) for the VariableMetadata object.
     *
     * @param dataDao The service DAO representing a VariableMetadata object.

    public void setVariableDao(VariableDao dataDao) {
        this.variableDao = dataDao;
    }
     */

    /**
     * Updated the information corresponding to the given list of metadata objects.
     *
     * @param metadata The list of metadata objects to update.

    @Override
    public void updatePersistedMetadata(List<VariableMetadata> metadata) {

        //variableDao.updatePersistedMetadata(metadata);
    }
     */

    /**
     * Updated the information corresponding to the given metadata object.
     *
     * @param metadata The metadata object to update.

    @Override
    public void updatePersistedMetadata(VariableMetadata metadata) {

        //variableDao.updatePersistedMetadata(metadata);
    }
     */
}
