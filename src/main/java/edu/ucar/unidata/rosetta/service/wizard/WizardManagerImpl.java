/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.converters.custom.dsg.NetcdfFileManager;
import edu.ucar.unidata.rosetta.converters.known.etuff.TagUniversalFileFormat;
import edu.ucar.unidata.rosetta.domain.GlobalMetadata;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.Variable;
import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.Delimiter;
import edu.ucar.unidata.rosetta.domain.resources.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.repository.resources.DelimiterResourceDao;
import edu.ucar.unidata.rosetta.repository.wizard.GlobalMetadataDao;
import edu.ucar.unidata.rosetta.repository.wizard.UploadedFileDao;
import edu.ucar.unidata.rosetta.repository.wizard.VariableDao;
import edu.ucar.unidata.rosetta.repository.wizard.WizardDataDao;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.util.JsonUtils;
import edu.ucar.unidata.rosetta.util.PropertyUtils;
import edu.ucar.unidata.rosetta.util.TemplateFactory;
import edu.ucar.unidata.rosetta.util.TransactionLogUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import ucar.ma2.InvalidRangeException;

/**
 * Implements wizard manager functionality.
 */
public class WizardManagerImpl implements WizardManager {

  private static final Logger logger = LogManager.getLogger();

  private DelimiterResourceDao delimiterResourceDao;
  private GlobalMetadataDao globalMetadataDao;
  private UploadedFileDao uploadedFileDao;
  private VariableDao variableDao;
  private WizardDataDao wizardDataDao;

  @Resource(name = "fileManager")
  private FileManager fileManager;

  @Resource(name = "metadataManager")
  private MetadataManager metadataManager;

  @Resource(name = "resourceManager")
  private ResourceManager resourceManager;

  @Resource(name = "templateManager")
  private TemplateManager templateManager;

  @Resource(name = "uploadedFileManager")
  private UploadedFileManager uploadedFileManager;

  private static String convertGlobalDataToJson(GlobalMetadata globalMetadata, HashMap<String, String> fileGlobals) {
    // Get the persisted metadata value.
    String value = globalMetadata.getMetadataValue();

    // We have globals from a file.
    if (fileGlobals != null) {
      // If there is no persisted value.
      if (value == null || value.equals("")) {
        value = fileGlobals.get(globalMetadata.getMetadataKey());
      }
    }
    return "\"" + globalMetadata.getMetadataKey() + "__" + globalMetadata.getMetadataGroup() + "\":" + "\"" + value
        + "\"";
  }

  /**
   * Processes all of the provided data and creates a netCDF file.
   *
   * @param id The unique ID corresponding to this transaction.
   * @return The location of the created netCDF file.
   * @throws RosettaFileException If unable to create the template file.
   * @throws RosettaDataException If unable to parse data file with delimiter.
   */
  public String convertToNetcdf(String id) throws RosettaFileException, RosettaDataException {
    String netcdfFile = null;

    Template template = templateManager.createTemplate(id);

    String userFilesDirPath = FilenameUtils.concat(PropertyUtils.getUserFilesDir(), id);
    File userFilesDir = new File(userFilesDirPath);
    if (!userFilesDir.exists()) {
      // This should exist by now.
      throw new RosettaFileException("Unable to locate user files directory for " + id);
    }

    String templateFilePath = FilenameUtils.concat(userFilesDirPath, "rosetta.template");
    String dataFilePath = FilenameUtils.concat(userFilesDirPath, uploadedFileManager.getDataFile(id).getFileName());

    // Load main template.
    try {
      Template baseTemplate = TemplateFactory.makeTemplateFromJsonFile(Paths.get(templateFilePath));
      String format = baseTemplate.getFormat();
      baseTemplate.setFormat(format.toLowerCase());
      template.setFormat(template.getFormat().toLowerCase());

      // If custom.
      if (format.equals("custom")) {
        baseTemplate.setCfType(baseTemplate.getCfType().toLowerCase().replaceAll(" ", "").replaceAll("_", ""));
        template.setCfType(template.getCfType());
        logger.info("Creating netCDF file for custom data file " + dataFilePath);
        // now find the proper converter
        NetcdfFileManager dsgWriter = null;
        for (NetcdfFileManager potentialDsgWriter : NetcdfFileManager.getConverters()) {
          if (potentialDsgWriter.isMine(baseTemplate.getCfType())) {
            dsgWriter = potentialDsgWriter;
            break;
          }
        }
        // Get the delimiter symbol.
        String delimiter;
        try {
          // Try using the delimiter (standard) passed from the db.
          Delimiter delimiterName = delimiterResourceDao.lookupDelimiterByName(template.getDelimiter());
          delimiter = delimiterName.getCharacterSymbol();
        } catch (DataRetrievalFailureException e) {
          // Delimiter is not standard. Try parsing using the delimiter provided by the user.
          delimiter = template.getDelimiter();
        }
        netcdfFile = dsgWriter.createNetcdfFile(Paths.get(dataFilePath), template, delimiter);
      }

      // If eTUFF.
      if (template.getFormat().equals("etuff")) {
        logger.info("Creating netCDF file for eTUFF file " + dataFilePath);
        TagUniversalFileFormat tuff = new TagUniversalFileFormat();
        tuff.parse(dataFilePath);
        String fullFileNameExt = FilenameUtils.getExtension(dataFilePath);
        String ncfile = dataFilePath.replace(fullFileNameExt, "nc");
        ncfile = FilenameUtils.concat(PropertyUtils.getUserFilesDir(), ncfile);
        netcdfFile = tuff.convert(ncfile, template);
      }


    } catch (IOException | InvalidRangeException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      throw new RosettaFileException("Unable to create template file " + errors);
    }

    return netcdfFile;
  }


  /**
   * Determines whether the custom file attributes step needs to be visited in the wizard.
   *
   * @param id The ID corresponding to the persisted data needed to make this determination.
   * @return true if custom file attributes step needs to be visited; otherwise false;
   */
  public boolean customFileAttributesStep(String id) {
    WizardData wizardData = lookupPersistedWizardDataById(id);
    String dataFileType = wizardData.getDataFileType();
    if (dataFileType != null) {
      return wizardData.getDataFileType().equals("Custom_File_Type");
    }
    return false;
  }

  /**
   * Determines the metadata profile (and the CF type) based on the data contained in the provided WizardData object.
   * The user may have explicitly specified the profile(s) to use, or we may have to determine them from the
   * community info.
   *
   * @param wizardData The wizardData object containing the user input data.
   * @return The name of the metadata profile.
   * @throws RosettaDataException If unable to determine the metadata profile.
   */
  private String determineMetadataProfile(WizardData wizardData) throws RosettaDataException {
    // Assign metadata profile to value specified in WizardData object (can be null).
    String metadataProfile = wizardData.getMetadataProfile();

    // Determine if the user explicitly specified the metadata profiles in the advanced section of the wizard interface.
    // If not, then use the community value to determine what metadata profiles to use.
    if (wizardData.getCommunity() != null) { // Community isn't null. Use to determine the metadata profile(s).

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
        // This shouldn't happen! Something has gone very wrong.
        // Either the platform/community or the the CF type/metadata profile must exist.
        throw new RosettaDataException(
            "Neither metadata profile or community values present: " + wizardData.toString());
      }
    } else { // No community provided.
      // Everybody gets the CF metadata type profile. Make sure it's there.
      if (metadataProfile == null) {
        metadataProfile = "CF";
      } else {
        if (!metadataProfile.contains("CF")) {
          metadataProfile = metadataProfile + ",CF";
        }
      }
    }

    // Determine if the user explicitly specified the CF type in the advanced section of the wizard interface.
    // If not, then use the platform value to determine the CF type.
    String cfType = wizardData.getCfType();
    if (Objects.isNull(cfType)) { // No CF type was inputted. Use the platform to determine CF type.
      if (Objects.isNull(wizardData.getPlatform())) {
        // This shouldn't happen! Something has gone very wrong.
        // Either the platform/community or the the CF type/metadata profile must exist.
        throw new RosettaDataException(
            "Neither metadata profile or community values present: " + wizardData.toString());
      }
      cfType = resourceManager.getCFTypeFromPlatform(wizardData.getPlatform()).replaceAll("_", " ");
    }
    // Get the user specified CF type to determine if the appropriate DSG metadata profiles need to be added.
    if (cfType.equals("Profile")) { // CF type is profile
      metadataProfile = metadataProfile + ",RosettaProfileDsg";
    }
    if (cfType.equals("Time Series")) { // CF type is time series
      metadataProfile = metadataProfile + ",RosettaTimeSeriesDsg";
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
    // Get the persisted wizard data.
    WizardData wizardData = wizardDataDao.lookupWizardDataById(id);

    // Get persisted variable metadata if it exists.
    List<Variable> variables = variableDao.lookupVariables(id);
    if (variables.size() > 0) {
      StringBuilder variableMetadata = new StringBuilder("[");
      for (Variable variable : variables) {
        String jsonVariable = JsonUtils.convertVariableDataToJson(variable);
        variableMetadata.append(jsonVariable).append(",");
      }
      variableMetadata = new StringBuilder(variableMetadata.substring(0, variableMetadata.length() - 1) + "]");
      wizardData.setVariableMetadata(variableMetadata.toString());
    }
    // Get persisted global metadata if it exists.
    List<GlobalMetadata> persisted = globalMetadataDao.lookupGlobalMetadata(id);

    // Get any global metadata that may exist in the data file (this assumes the data file has already been uploaded).
    HashMap<String, String> fileGlobals = null;
    if (wizardData.getDataFileType() != null) {
      if (wizardData.getDataFileType().equals("eTUFF")) {
        fileGlobals = getGlobalMetadataFromDataFile(id);
      }
    }
    // Build the json string to the global metadata to the client.
    StringBuilder globalMetadata = new StringBuilder();
    if (persisted.size() > 0) { // We have persisted global metadata.
      for (GlobalMetadata item : persisted) {
        String jsonGlobalMetadataString = convertGlobalDataToJson(item, fileGlobals);
        globalMetadata.append(jsonGlobalMetadataString).append(",");
      }
    } else { // No persisted global metadata.
      if (fileGlobals != null) {
        // Kludge to get the corresponding metadata group from the eTuff profile (as it is not included
        // in the global metadata we glean from the data file.
        List<edu.ucar.unidata.rosetta.domain.MetadataProfile> eTUFF = metadataManager.getMetadataProfile("eTUFF");

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
            String jsonString = "\"" + pair.getKey() + "__" + group + "\":" + "\"" + pair.getValue() + "\"";
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


  /**
   * Returns a map of global metadata gleaned from the uploaded data file.
   *
   * @param id The ID corresponding to the transaction.
   * @return A map of global metadata.
   */
  private HashMap<String, String> getGlobalMetadataFromDataFile(String id) {
    // Get the path to the user_files directory corresponding to the given ID.
    String userFilesDirPath = FilenameUtils.concat(PropertyUtils.getUserFilesDir(), id);
    // Get the path to the uploaded data file.
    String dataFilePath = FilenameUtils.concat(userFilesDirPath, uploadedFileManager.getDataFile(id).getFileName());

    // Right now eTUFF is the only cf Type we are going this for.
    TagUniversalFileFormat tuff = new TagUniversalFileFormat();
    tuff.parse(dataFilePath);
    return tuff.getGlobalMetadata();
  }

  /**
   * Examines the given MetadataProfile object to see if one of its communities matches the provided community name.
   *
   * @param metadataProfileResource The MetadataProfile object to examine.
   * @param communityName The community name ot match.
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


  public String getTemplateFile(String id) {
    String userFilesDirPath = FilenameUtils.concat(PropertyUtils.getUserFilesDir(), id);
    return FilenameUtils.concat(userFilesDirPath, "rosetta.template");
  }


  /**
   * Retrieves the data file from disk and parses it by line, converting it into a JSON string. Used in the wizard for
   * header line selection.
   *
   * @param id The unique id associated with the file (a sub directory in the user_files directory).
   * @return A JSON string of the file data parsed by line.
   * @throws RosettaFileException For any file I/O or JSON conversions problems.
   */
  @Override
  public String parseDataFileByLine(String id) throws RosettaFileException {
    UploadedFile dataFile = uploadedFileDao.lookupDataFileById(id);
    String filePath =
        FilenameUtils.concat(FilenameUtils.concat(PropertyUtils.getUserFilesDir(), id), dataFile.getFileName());
    return fileManager.parseByLine(filePath);
  }

  /**
   * Processes the data collected from the wizard for the CF type step.
   * Persisted data corresponding to the ID ALREADY EXISTS.
   * This prior persisted data is collected and updated with the new data contained WizardData object.
   *
   * @param id The unique ID corresponding to already persisted data (may be null).
   * @param wizardData The WizardData object containing user-submitted CF type information.
   * @throws RosettaDataException If unable to lookup/persist wizard data.
   */
  @Override
  public void processCfType(String id, WizardData wizardData) throws RosettaDataException, RosettaFileException {
    // Create the transaction log.
    TransactionLogUtils.createLog(id);

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
  }

  /**
   * Processes the data collected from the wizard for the CF type step for the FIRST TIME.
   *
   * @param wizardData The WizardData object containing user-submitted CF type information.
   * @param request HttpServletRequest used to make unique IDs for new data.
   * @throws RosettaDataException If unable to lookup/persist wizard data.
   */
  @Override
  public void processCfType(WizardData wizardData, HttpServletRequest request)
      throws RosettaDataException, RosettaFileException {
    // Create a unique ID for this object.
    wizardData.setId(PropertyUtils.createUniqueDataId(request));

    // Create the transaction log.
    TransactionLogUtils.createLog(wizardData.getId());

    // Set the community if applicable.
    if (wizardData.getPlatform() != null) {
      wizardData.setCommunity(resourceManager.getCommunityFromPlatform(wizardData.getPlatform()));
    }

    // Set metadata profile.
    wizardData.setMetadataProfile(determineMetadataProfile(wizardData));

    // Persist the Cf type data.
    wizardDataDao.persistWizardData(wizardData);
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
    if (wizardData.hasNoHeaderLines()) {
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
   * Processes the data submitted by the user containing global metadata information. Since this is the final step of
   * collecting data in the wizard, the uploaded data file is converted to netCDF format in preparation for user
   * download.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param wizardData The WizardData containing the global metadata.
   */
  @Override
  public void processGlobalMetadata(String id, WizardData wizardData) {
    // Parse the JSON to get GlobalMetadata objects.
    List<GlobalMetadata> globalMetadata = JsonUtils.convertGlobalDataFromJson(wizardData.getGlobalMetadata());

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
   * Determines the next step in the wizard based the user specified data file type. This method is called when there is
   * a divergence of possible routes through the wizard.
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
      nextStep = "/globalMetadata";
    }
    return nextStep;
  }

  /**
   * Determines the previous step in the wizard based the user specified data file type. This method is called when
   * there is a divergence of possible routes through the wizard.
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
    List<Variable> variables = JsonUtils.convertVariableDataFromJson(wizardData.getVariableMetadata());
    // Look up any persisted data corresponding to the id.
    // (If we are restoring from a template, or using the back button, there will be persisted data.)
    List<Variable> persisted = variableDao.lookupVariables(id);

    // Get the variable IDs and columns numbers from persisted data.
    Map<Integer, Integer> variableMap = new HashMap<>(variables.size());

    if (persisted.size() > 0) {

      // Populate the variable Map with the variable ID (persisted ID) and column number (used below).
      for (Variable persistedVar : persisted) {
        int variableId = persistedVar.getVariableId();
        int columnNumber = persistedVar.getColumnNumber();
        variableMap.put(columnNumber, variableId);
      }

      // Update new (submitted) variables with column numbers from the Map.
      for (Variable variable : variables) {

        // Determine if the variable is has been persisted.
        if (variableMap.containsKey(variable.getColumnNumber())) {
          // The variable has been persisted.
          int variableId = variableMap.get(variable.getColumnNumber());
          variable.setVariableId(variableId); // Set the persisted variable Id.
          variable.setWizardDataId(id); // Set the wizard Id.
        } else {
          // The variable has NOT been persisted yet.
          // This can happen if a restored template variable count is less than
          // (submitted) data file variable count. This results in the persisted
          // variable data being incomplete.

          variable.setWizardDataId(id); // Set the wizard Id.
          // Persist the variable & get the ID.
          int variableId = variableDao.persistVariable(id, variable);
          variable.setVariableId(variableId); // Set the persisted variable Id.
        }

      }
      variableDao.updatePersistedVariables(variables);
    } else {
      // No persisted data; this is the first time we are persisting it.
      variableDao.persistVariables(id, variables);
    }
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
   * Sets the data access object (DAO) for the GlobalMetadata object.
   *
   * @param globalMetadataDao The service DAO representing a GlobalMetadata object.
   */
  public void setGlobalMetadataDao(GlobalMetadataDao globalMetadataDao) {
    this.globalMetadataDao = globalMetadataDao;
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
