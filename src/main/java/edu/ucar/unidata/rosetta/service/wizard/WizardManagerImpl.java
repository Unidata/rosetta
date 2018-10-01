/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.converters.custom.dsg.NetcdfFileManager;
import edu.ucar.unidata.rosetta.converters.known.etuff.TagUniversalFileFormat;
import edu.ucar.unidata.rosetta.domain.Variable;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.GlobalMetadata;
import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.repository.wizard.GlobalMetadataDao;
import edu.ucar.unidata.rosetta.repository.wizard.UploadedFileDao;
import edu.ucar.unidata.rosetta.repository.wizard.VariableDao;
import edu.ucar.unidata.rosetta.repository.wizard.WizardDataDao;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.util.JsonUtil;
import edu.ucar.unidata.rosetta.util.PropertyUtils;
import edu.ucar.unidata.rosetta.util.TemplateFactory;

import ucar.ma2.InvalidRangeException;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;


import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;

/**
 * Implements wizard manager functionality.
 */
public class WizardManagerImpl implements WizardManager {

    private static final Logger logger = Logger.getLogger(WizardManagerImpl.class);

    private UploadedFileDao uploadedFileDao;
    private VariableDao variableDao;
    private WizardDataDao wizardDataDao;
    private GlobalMetadataDao globalMetadataDao;

    @Resource(name = "fileManager")
    private FileManager fileManager;

    @Resource(name = "uploadedFileManager")
    private UploadedFileManager uploadedFileManager;

    @Resource(name = "resourceManager")
    private ResourceManager resourceManager;

    @Resource(name = "templateManager")
    private TemplateManager templateManager;

    @Resource(name = "metadataManager")
    private MetadataManager metadataManager;


    public String convertToNetcdf(String id) throws RosettaFileException {
        String netcdfFile = null;

        Template template = templateManager.createTemplate(id);
        String downloadDirPath = FilenameUtils.concat(PropertyUtils.getDownloadDir(), id);
        File downloadDir = new File(downloadDirPath);
        if (!downloadDir.exists()) {
            logger.info("Creating downloads directory at " + downloadDir.getPath());
            if (!downloadDir.mkdirs()) {
                throw new RosettaFileException("Unable to create downloads directory for " + id);
            }
        }

        String templateFilePath = FilenameUtils.concat(downloadDirPath, "rosetta.template");
        String uploadDirPath = FilenameUtils.concat(PropertyUtils.getUploadDir(), id);
        String dataFilePath = FilenameUtils.concat(uploadDirPath, uploadedFileManager.getDataFile(id).getFileName());

        String dest = null;
        // Load main template.
        try {
            Template baseTemplate = TemplateFactory.makeTemplateFromJsonFile(Paths.get(templateFilePath));
            String format = baseTemplate.getFormat();
            baseTemplate.setFormat(format.toLowerCase());
            baseTemplate.setCfType(baseTemplate.getCfType().toLowerCase());
            template.setCfType(template.getCfType().toLowerCase());
            template.setFormat(template.getFormat().toLowerCase());

            // If custom.
            if (format.equals("custom")) {
                logger.info("Creating netCDF file for custom data file " + dataFilePath);
                // now find the proper converter
                NetcdfFileManager dsgWriter = null;
                for (NetcdfFileManager potentialDsgWriter : NetcdfFileManager.getConverters()) {
                    if (potentialDsgWriter.isMine(baseTemplate.getCfType())) {
                        dsgWriter = potentialDsgWriter;
                        break;
                    }
                }
                netcdfFile = dsgWriter.createNetcdfFile(Paths.get(dataFilePath), template);
                logger.info(netcdfFile);

                dest = netcdfFile.replace("uploads", "downloads");
                logger.info(dest);
                FileUtils.copyFile(new File(netcdfFile), new File(dest));

            }

            // If eTUFF.
            if (template.getFormat().equals("etuff")) {
                logger.info("Creating netCDF file for eTUFF file " + dataFilePath);
                TagUniversalFileFormat tuff = new TagUniversalFileFormat();
                tuff.parse(dataFilePath);
                String fullFileNameExt = FilenameUtils.getExtension(dataFilePath);
                String ncfile = dataFilePath.replace(fullFileNameExt, "nc");
                ncfile = FilenameUtils.concat(PropertyUtils.getDownloadDir(), ncfile);
                netcdfFile = tuff.convert(ncfile, template);

                logger.info(netcdfFile);

                dest = netcdfFile.replace("uploads", "downloads");
                logger.info(dest);
                FileUtils.copyFile(new File(netcdfFile), new File(dest));
            }



        } catch (IOException | InvalidRangeException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            throw new RosettaFileException("Unable to create template file " + errors);
        }


        return dest;
    }

    public String getTemplateFile(String id) {
        String downloadDirPath = FilenameUtils.concat(PropertyUtils.getDownloadDir(), id);
        return FilenameUtils.concat(downloadDirPath, "rosetta.template");
    }

    /**
     * Returns a map of global metadata gleaned from the uploaded data file.
     *
     * @param id The ID corresponding to the transaction.
     * @return  A map of global metadata.
     */
    private HashMap<String, String> getGlobalMetadataFromDataFile(String id) {
        // Get the path to the upload directory corresponding to the given ID.
        String uploadDirPath = FilenameUtils.concat(PropertyUtils.getUploadDir(), id);
        // Get the path to the uploaded data file.
        String dataFilePath = FilenameUtils.concat(uploadDirPath, uploadedFileManager.getDataFile(id).getFileName());

        // Right now eTUFF is the only cf Type we are going this for.
        TagUniversalFileFormat tuff = new TagUniversalFileFormat();
        tuff.parse(dataFilePath);
        return tuff.getGlobalMetadata();
    }

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
     * @param id The ID corresponding to the data to retemperaturetrieve.
     * @return The persisted wizard data.
     */
    @Override
    public WizardData lookupPersistedWizardDataById(String id) {
        // Get the persisted wizard data.
        WizardData wizardData = wizardDataDao.lookupWizardDataById(id);

        // Get persisted variable metadata if it exists.
        List<Variable> variables = variableDao.lookupVariables(id);
        if (variables.size() > 0) {
            StringBuilder variableMetadata = new StringBuilder("[");
            for (Variable variable : variables) {
                String jsonVariable = JsonUtil.convertVariableDataToJson(variable);
                variableMetadata.append(jsonVariable).append(",");
            }
            variableMetadata = new StringBuilder(variableMetadata.substring(0, variableMetadata.length() - 1) + "]");
            wizardData.setVariableMetadata(variableMetadata.toString());
        }
        // Get persisted global metadata if it exists.
        List<GlobalMetadata> persisted = globalMetadataDao.lookupGlobalMetadata(id);

        // Get any global metadata that may exist in the data file.
        HashMap<String, String> fileGlobals = null;
        if (wizardData.getDataFileType() != null) {
            if (wizardData.getDataFileType().equals("eTUFF")) {
                fileGlobals = getGlobalMetadataFromDataFile(id);
            }
        }
        // Build the json string to the global metadata to the client.
        StringBuilder globalMetadata = new StringBuilder();
        if (persisted.size() > 0) {
            // We have persisted global metadata.
            for (GlobalMetadata item : persisted) {
                String jsonGlobalMetadataString = convertGlobalDataToJson(item, fileGlobals);
                globalMetadata.append(jsonGlobalMetadataString).append(",");
            }
        } else {
            // No persisted global metadata.

            if (fileGlobals != null) {
                // Kludge to get the corresponding metadata group from the eTuff profile (as it is not included
                // in the global metadata we glean from the data file.
                List<edu.ucar.unidata.rosetta.domain.MetadataProfile> eTUFF = metadataManager.getETUFFProfile();

                // Iterate through the file globals and add the metadataGroup information.
                Iterator it = fileGlobals.entrySet().iterator();
                while (it.hasNext()) {
                    String group = null;
                    Map.Entry pair = (Map.Entry) it.next();
                    for (edu.ucar.unidata.rosetta.domain.MetadataProfile profile : eTUFF) {

                        if (profile.getAttributeName().equals(pair.getKey())) {
                             group = profile.getMetadataGroup();
                        }
                    }
                    if (group != null) {
                        String jsonString =
                                "\"" + pair.getKey() + "__" + group + "\":" + "\"" + pair.getValue() + "\"";
                        it.remove(); // Avoids a ConcurrentModificationException.
                        globalMetadata.append(jsonString).append(",");
                    }
                }
            }
        }
        String jsonString = globalMetadata.toString();
        if (!jsonString.equals("")) {
            jsonString = jsonString.substring(0, jsonString.length() - 1);
            jsonString = "{" + jsonString + "}";
        }
        wizardData.setGlobalMetadata(jsonString);
        return wizardData;
    }


    public static String convertGlobalDataToJson(GlobalMetadata globalMetadata, HashMap<String, String> fileGlobals) {

        String value = globalMetadata.getMetadataValue();

        // We have globals from a file.
        if (fileGlobals != null) {
            if (value.equals("")) {
                value = fileGlobals.get(globalMetadata.getMetadataKey());
            }
        }
        return "\"" +
                globalMetadata.getMetadataKey() + "__" +
                globalMetadata.getMetadataGroup() + "\":" +
                "\"" + value + "\"";
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
     * Processes the data submitted by the user containing general metadata information.  Since this
     * is the final step of collecting data in the wizard, the uploaded data file is converted to
     * netCDF format in preparation for user download.
     *
     * @param id The unique ID corresponding to already persisted data.
     * @param wizardData The WizardData containing the general metadata.
     * @throws RosettaDataException If unable to populate the metadata object.
     */
    @Override
    public void processGeneralMetadata(String id, WizardData wizardData) throws RosettaDataException {
        // Parse the JSON to get GlobalMetadata objects.
        List<GlobalMetadata> globalMetadata = JsonUtil.convertGlobalDataFromJSON(wizardData.getGlobalMetadata());

        // Look up any persisted data corresponding to the id.
        List<GlobalMetadata> persisted = globalMetadataDao.lookupGlobalMetadata(id);

        if (persisted.size() > 0) {
            // Update the persisted data.
            globalMetadataDao.updatePersistedGlobalMetadata(id, globalMetadata);
        } else {
            // No persisted data; this is the first time we are persisting it.
            globalMetadataDao.persistGlobalMetadata(id, globalMetadata);
        }
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
     * Processes the data submitted by the user containing variable metadata information.
     *
     * @param id The unique ID corresponding to already persisted data.
     * @param wizardData The WizardData containing variable metadata information.
     */
    @Override
    public void processVariableMetadata(String id, WizardData wizardData) {
        // Parse the JSON to get Variable objects.
        List<Variable> variables = JsonUtil.convertVariableDataFromJSON(wizardData.getVariableMetadata());

        // Look up any persisted data corresponding to the id.
        List<Variable> persisted = variableDao.lookupVariables(id);

        // Get the variable IDs and columns numbers from persisted data.
        Map<Integer, Integer> variableMap = new HashMap<>(persisted.size());
        if (persisted.size() > 0) {
            // Create map of column numbers to variable ids.
            for (Variable persistedVar : persisted) {
                int variableId = persistedVar.getVariableId();
                int columnNumber = persistedVar.getColumnNumber();
                variableMap.put(columnNumber, variableId);
            }
            // Update new variables with column numbers.
            for (Variable variable : variables) {
                int variableId = variableMap.get(variable.getColumnNumber());
                variable.setVariableId(variableId);
                variable.setWizardDataId(id);
            }
            variableDao.updatePersistedVariables(variables);
        } else {
            // No persisted data; this is the first time we are persisting it.
            variableDao.persistVariables(id, variables);
        }
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
     * Sets the data access object (DAO) for the Variable object.
     *
     * @param variableDao The service DAO representing a Variable object.
     */
    public void setVariableDao(VariableDao variableDao) {
        this.variableDao = variableDao;
    }

    /**
     * Sets the data access object (DAO) for the GlobalMetadata object.
     *
     * @param globalMetadataDao The service DAO representing a GlobalMetadata object.
     */
    public void setGlobalMetadataDao(GlobalMetadataDao globalMetadataDao) {
        this.globalMetadataDao = globalMetadataDao;
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
