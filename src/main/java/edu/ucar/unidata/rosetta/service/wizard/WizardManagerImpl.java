/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.repository.wizard.UploadedFileDao;
import edu.ucar.unidata.rosetta.repository.wizard.WizardDataDao;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.util.PropertyUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Implements wizard manager functionality.
 */
public class WizardManagerImpl implements WizardManager {

    private static final Logger logger = Logger.getLogger(WizardManagerImpl.class);

    private UploadedFileDao uploadedFileDao;
    private WizardDataDao wizardDataDao;

    @Resource(name = "fileManager")
    private FileManager fileManager;

    @Resource(name = "resourceManager")
    private ResourceManager resourceManager;

    /**
     * Determines whether the custom file attributes step needs to be visited in the wizard.
     *
     * @param id  The ID corresponding to the persisted data needed to make this determination.
     * @return  true if custom file attributes step needs to be visited; otherwise false;
     */
    public boolean customFileAttributesStep(String id) {
        WizardData wizardData = lookupPersistedWizardDataById(id);
        String dataFileType =  wizardData.getDataFileType();
        if (dataFileType != null) {
            return wizardData.getDataFileType().equals("Custom_File_Type");
        }
        return false;
    }

    /**
     * Determines the metadata profile to use based on the data contained in the
     * provided WizardData object.  The user may have explicitly specified the
     * profile(s) to use, or we may have to determine them from the community info.
     *
     * @param wizardData The wizardData object containing the user input data.
     * @return The name of the metadata profile.
     * @throws RosettaDataException If unable to determine the metadata profile.
     */
    private String determineMetadataProfile(WizardData wizardData) throws RosettaDataException {
        // Assign metadata profile to value specified in WizardData object (can be null).
        String metadataProfile = wizardData.getMetadataProfile();

        // If community value isn't null, determine the metadata profile(s).
        if (wizardData.getCommunity() != null) {

            // Use the provided community/platform to figure out metadata profile.
            String userSelectedCommunityName = wizardData.getCommunity();
            if (userSelectedCommunityName != null) {
                StringBuilder sb = new StringBuilder();
                for (MetadataProfile metadataProfileResource : resourceManager.getMetadataProfiles()) {
                    String match = getMetadataProfileFromCommunity(metadataProfileResource, userSelectedCommunityName);
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
                        + wizardData.toString());
            }
        } else {
            // Everybody gets the CF metadata type profile.  Make sure it's there.
            if (metadataProfile == null) {
                metadataProfile = "CF";
            } else {
                if (!metadataProfile.contains("CF")) {
                    metadataProfile = metadataProfile + ",CF";
                }
            }
        }
        return metadataProfile;
    }

    /**
     * Looks up and retrieves persisted wizard data using the given ID.
     *
     * @param id The ID corresponding to the data to retrieve.
     * @return The persisted wizard data.
     */
    @Override
    public WizardData lookupPersistedWizardDataById(String id) {
        return wizardDataDao.lookupWizardDataById(id);
    }

    /**
     * Examines the given MetadataProfile object to see if one of its communities matches the provided community name.
     *
     * @param metadataProfileResource The MetadataProfile object to examine.
     * @param communityName           The community name ot match.
     * @return The name of the metadata profile if matches; otherwise null.
     */
    private String getMetadataProfileFromCommunity(MetadataProfile metadataProfileResource, String communityName) {
        String metadataProfile = null;

        List<Community> communities = metadataProfileResource.getCommunities();
        for (Community community : communities) {
            if (community.getName().equals(communityName)) {
                metadataProfile = metadataProfileResource.getName();
                break;
            }
        }
        return metadataProfile;
    }

    /**
     * Retrieves the data file from disk and parses it by line, converting it into a JSON string.
     * Used in the wizard for header line selection.
     *
     * @param id The unique id associated with the file (a sub directory in the uploads directory).
     * @return A JSON string of the file data parsed by line.
     * @throws RosettaFileException For any file I/O or JSON conversions problems.
     */
    @Override
    public String parseDataFileByLine(String id) throws RosettaFileException {
        UploadedFile dataFile = uploadedFileDao.lookupDataFileById(id);
        String filePath = FilenameUtils
                .concat(FilenameUtils.concat(PropertyUtils.getUploadDir(), id), dataFile.getFileName());
        return fileManager.parseByLine(filePath);
    }

    /**
     * Persists the provided wizard data for the first time.
     *
     * @param wizardData The wizard data to persist.
     */
    @Override
    public void persistWizardData(WizardData wizardData) {
        wizardDataDao.persistWizardData(wizardData);
    }

    /**
     * Processes the data collected from the wizard for the CF type step. If an ID already
     * exists, the persisted data corresponding to that ID is collected and updated with the newly
     * submitted data.  If no ID exists (is null), the data is persisted for the first time.
     *
     * @param id         The unique ID corresponding to already persisted data (may be null).
     * @param wizardData The WizardData object containing user-submitted CF type information.
     * @param request    HttpServletRequest used to make unique IDs for new data.
     * @throws RosettaDataException If unable to lookup the metadata profile.
     */
    @Override
    public void processCfType(String id, WizardData wizardData, HttpServletRequest request)
            throws RosettaDataException {

        // If the ID is present, then there is a cookie.  Combine new with previous persisted data.
        if (id != null) {

            // Get the persisted CF type data corresponding to this ID.
            WizardData persistedData = lookupPersistedWizardDataById(id);

            // Update platform value (can be null).
            persistedData.setPlatform(wizardData.getPlatform());

            // Update community if needed.
            if (wizardData.getPlatform() != null) {
                // Set community.
                String community = resourceManager.getCommunityFromPlatform(wizardData.getPlatform());
                persistedData.setCommunity(community);

                // Update this object too, as we need it to get the metadata profile info.
                wizardData.setCommunity(community);

                // Set metadata profile.
                persistedData.setMetadataProfile(determineMetadataProfile(wizardData));
            } else {

                // No platform provided so set community to null.
                persistedData.setCommunity(null);

                // Set the metadata profile to user-selected values.
                persistedData.setMetadataProfile(wizardData.getMetadataProfile());
            }

            // Set the CF type.
            persistedData.setCfType(wizardData.getCfType());

            // Update persisted CF type data.
            updatePersistedWizardData(persistedData);

        } else {
            // No ID yet.  First time persisting CF type data.

            // Create a unique ID for this object.
            wizardData.setId(PropertyUtils.createUniqueDataId(request));

            // Set the community if applicable.
            if (wizardData.getPlatform() != null) {
                wizardData.setCommunity(resourceManager.getCommunityFromPlatform(wizardData.getPlatform()));
            }

            // Set metadata profile.
            wizardData.setMetadataProfile(determineMetadataProfile(wizardData));

            // Persist the Cf type data.
            persistWizardData(wizardData);
        }
    }

    /**
     * Processes the data submitted by the user containing custom data file attributes.
     *
     * @param id The unique ID corresponding to already persisted data.
     * @param wizardData The WizardData containing custom data file attributes.
     */
    @Override
    public void processCustomFileTypeAttributes(String id, WizardData wizardData) {
        // Get the persisted CF type data corresponding to this ID.
        WizardData persistedData = lookupPersistedWizardDataById(id);

        // Handle the no header lines value.
        if (wizardData.isNoHeaderLines()) {
            persistedData.setHeaderLineNumbers(null);
        } else {
            persistedData.setHeaderLineNumbers(wizardData.getHeaderLineNumbers());
        }

        // Add the delimiter.
        persistedData.setDelimiter(wizardData.getDelimiter());

        // Technically, an entry for this ID already exists in the wizardData table from file upload step.
        // We just need to add/update the header line number and delimiter values.
        wizardDataDao.updatePersistedWizardData(persistedData);
    }

    /**
     * Determines the next step in the wizard based the user specified data file type.
     * This method is called when there is a divergence of possible routes through the wizard.
     *
     * @param id The unique ID corresponding to already persisted data.
     * @return The next step to redirect the user to in the wizard.
     */
    @Override
    public String processNextStep(String id) {

        // The placeholder for what we are going to return.
        String nextStep;

        // The next step depends on what the user specified for the data file type.
        if (customFileAttributesStep(id)) {
            nextStep = "/customFileTypeAttributes";
        } else {
            nextStep = "/generalMetadata";
        }
        return nextStep;
    }

    /**
     * Determines the previous step in the wizard based the user specified data file type.
     * This method is called when there is a divergence of possible routes through the wizard.
     *
     * @param id The unique ID corresponding to already persisted data.
     * @return The previous step to redirect the user to in the wizard.
     */
    @Override
    public String processPreviousStep(String id) {

        // The placeholder for what we are going to return.
        String previousStep;

        // The previous step (if the user chooses to go there) depends
        // on what the user specified for the data file type.
        if (customFileAttributesStep(id)) {
            previousStep = "/variableMetadata";
        } else {
            previousStep = "/fileUpload";
        }

        return previousStep;
    }

    /**
     * Sets the data access object (DAO) for the UploadedFile object.
     *
     * @param uploadedFileDao The service DAO representing a UploadedFile object.
     */
    public void setUploadedFileDao(UploadedFileDao uploadedFileDao) {
        this.uploadedFileDao = uploadedFileDao;
    }


    /**
     * Sets the data access object (DAO) for the WizardData object.
     *
     * @param wizardDataDao The service DAO representing a WizardData object.
     */
    public void setWizardDataDao(WizardDataDao wizardDataDao) {
        this.wizardDataDao = wizardDataDao;
    }

    /**
     * Updates persisted wizard data with the information in the provided WizardData object.
     *
     * @param wizardData The updated wizard data.
     */
    @Override
    public void updatePersistedWizardData(WizardData wizardData) {
        wizardDataDao.updatePersistedWizardData(wizardData);
    }
}
