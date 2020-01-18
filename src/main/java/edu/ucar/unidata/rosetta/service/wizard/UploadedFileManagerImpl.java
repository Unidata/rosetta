/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.GlobalMetadata;
import edu.ucar.unidata.rosetta.domain.RosettaAttribute;
import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.Variable;
import edu.ucar.unidata.rosetta.domain.VariableInfo;
import edu.ucar.unidata.rosetta.domain.VariableMetadata;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile.FileType;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.repository.wizard.GlobalMetadataDao;
import edu.ucar.unidata.rosetta.repository.wizard.UploadedFileDao;
import edu.ucar.unidata.rosetta.repository.wizard.VariableDao;
import edu.ucar.unidata.rosetta.repository.wizard.WizardDataDao;
import edu.ucar.unidata.rosetta.util.JsonUtils;
import edu.ucar.unidata.rosetta.util.PropertyUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements uploaded file manager functionality.
 */
public class UploadedFileManagerImpl implements UploadedFileManager {

  private static final Logger logger = LogManager.getLogger();

  private GlobalMetadataDao globalMetadataDao;
  private UploadedFileDao uploadedFileDao;
  private VariableDao variableDao;
  private WizardDataDao wizardDataDao;

  @Resource(name = "fileManager")
  private FileManager fileManager;

  @Resource(name = "metadataManager")
  private MetadataManager metadataManager;

  /**
   * Returns the data file corresponding to the given ID as an UploadedFile object.
   *
   * @param id The ID corresponding to the stored file.
   * @return The data file as an UploadedFile object.
   */
  public UploadedFile getDataFile(String id) {
    UploadedFile dataFile = null;
    UploadedFileCmd uploadedFileCmd = lookupPersistedDataById(id);
    for (UploadedFile uploadedFile : uploadedFileCmd.getUploadedFiles()) {
      if (uploadedFile.getFileType() == FileType.DATA) {
        dataFile = uploadedFile;
        break;
      }
    }
    return dataFile;
  }

  /**
   * Returns the positional file corresponding to the given ID as an UploadedFile object.
   *
   * @param id The ID corresponding to the stored file.
   * @return The positional file as an UploadedFile object.
   */
  public UploadedFile getPositionalFile(String id) {
    UploadedFile dataFile = null;
    UploadedFileCmd uploadedFileCmd = lookupPersistedDataById(id);
    for (UploadedFile uploadedFile : uploadedFileCmd.getUploadedFiles()) {
      if (uploadedFile.getFileType() == FileType.POSITIONAL) {
        dataFile = uploadedFile;
        break;
      }
    }
    return dataFile;
  }

  /**
   * Returns the template file corresponding to the given ID as an UploadedFile object.
   *
   * @param id The ID corresponding to the stored file.
   * @return The template file as an UploadedFile object.
   */
  public UploadedFile getTemplateFile(String id) {
    UploadedFile dataFile = null;
    UploadedFileCmd uploadedFileCmd = lookupPersistedDataById(id);
    for (UploadedFile uploadedFile : uploadedFileCmd.getUploadedFiles()) {
      if (uploadedFile.getFileType() == FileType.TEMPLATE) {
        dataFile = uploadedFile;
        break;
      }
    }
    return dataFile;
  }

  /**
   * Looks up and retrieves a uploaded file data using the given id.
   *
   * @param id The ID corresponding to the data to retrieve.
   * @return The persisted uploaded file data.
   */
  @Override
  public UploadedFileCmd lookupPersistedDataById(String id) {
    return uploadedFileDao.lookupById(id);
  }

  /**
   * Retrieves the data file from disk and parses it by line, converting it into a JSON string.
   *
   * @param id The unique id associated with the file (a subdir in the user files directory).
   * @param dataFileName The file to parse.
   * @return A JSON String of the file data parsed by line.
   * @throws RosettaFileException For any file I/O or JSON conversions problems.
   */
  @Override
  public String parseDataFileByLine(String id, String dataFileName) throws RosettaFileException {
    String filePath = FilenameUtils.concat(FilenameUtils.concat(PropertyUtils.getUserFilesDir(), id), dataFileName);
    return fileManager.parseByLine(filePath);
  }

  /**
   * Finds and persists and data from an uploaded template file.
   *
   * @param id The unique ID corresponding to thsi transaction.
   * @param templateFile The rosetta template file to use.
   * @throws RosettaFileException If unable to collect data from template file.
   */
  private void persistTemplateData(String id, UploadedFile templateFile) throws RosettaFileException {
    String jsonString =
        this.fileManager.getJsonStringFromTemplateFile(PropertyUtils.getUserFilesDir(), id, templateFile.getFileName());
    try {
      // Get the template data in the form of a Template object.
      Template templateData = JsonUtils.mapJsonToTemplateObject(jsonString);

      // Get persisted wizard data from the template.
      WizardData wizardData = wizardDataDao.lookupWizardDataById(id);
      // Delimiter.
      String delimiter = templateData.getDelimiter();
      if (delimiter != null) {
        wizardData.setDelimiter(delimiter);
      }
      // Header line numbers.
      List<Integer> headerLineNumberList = templateData.getHeaderLineNumbers();
      if (headerLineNumberList != null && !headerLineNumberList.isEmpty()) {
        String headerLineNumbers =
            headerLineNumberList.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(","));
        wizardData.setHeaderLineNumbers(headerLineNumbers);
      }
      // Persist data updated with template info.
      wizardDataDao.updatePersistedWizardData(wizardData);


      /* PERSIST GLOBAL METADATA FROM TEMPLATE */

      // Need to convert the list of RosettaGlobalAttribute objects to a list of GlobalMetadata objects.
      List<RosettaGlobalAttribute> rosettaGlobalAttributes = templateData.getGlobalMetadata();

      if (rosettaGlobalAttributes != null) {

        List<GlobalMetadata> globalMetadataObjects = new ArrayList<>(rosettaGlobalAttributes.size());
        for (RosettaGlobalAttribute rosettaGlobalAttribute : templateData.getGlobalMetadata()) {
          GlobalMetadata globalMetadata = new GlobalMetadata();
          globalMetadata.setWizardDataId(id);
          globalMetadata.setMetadataGroup(rosettaGlobalAttribute.getGroup());
          globalMetadata.setMetadataKey(rosettaGlobalAttribute.getName());
          globalMetadata.setMetadataValue(rosettaGlobalAttribute.getValue());
          globalMetadata.setMetadataValueType(rosettaGlobalAttribute.getType());
          globalMetadataObjects.add(globalMetadata);
        }

        // Look up any persisted data corresponding to the id.
        List<GlobalMetadata> persisted = globalMetadataDao.lookupGlobalMetadata(id);

        if (persisted.size() > 0) {
          // Update the persisted data.
          globalMetadataDao.updatePersistedGlobalMetadata(id, globalMetadataObjects);
        } else {
          // No persisted data; this is the first time we are persisting it.
          globalMetadataDao.persistGlobalMetadata(id, globalMetadataObjects);
        }
      }

      /* PERSIST VARIABLE METADATA FROM TEMPLATE */

      // Need to convert the list of VariableInfo objects to a list of Variable objects.
      List<VariableInfo> variableInfoObjects = templateData.getVariableInfoList();
      if (variableInfoObjects != null) {
        List<Variable> variableObjects = new ArrayList<>();
        for (VariableInfo variableInfo : variableInfoObjects) {
          Variable variable = new Variable();
          variable.setWizardDataId(id);
          variable.setColumnNumber(variableInfo.getColumnId());
          variable.setVariableName(variableInfo.getName());

          // Get rosetta control metadata.
          List<RosettaAttribute> rosettaControlMetadata = variableInfo.getRosettaControlMetadata();
          if (rosettaControlMetadata != null) {
            // This should always be true if column data is used.
            for (RosettaAttribute rosettaAttributeControlMetadata : rosettaControlMetadata) {
              if (rosettaAttributeControlMetadata.getName().equals("coordinateVariableType")) {
                variable.setMetadataTypeStructure(rosettaAttributeControlMetadata.getValue());
              }
              if (rosettaAttributeControlMetadata.getName().equals("coordinateVariable")) {
                variable.setMetadataType(rosettaAttributeControlMetadata.getValue());
              }
              if (rosettaAttributeControlMetadata.getName().equals("type")) {
                variable.setMetadataValueType(rosettaAttributeControlMetadata.getValue());
              }
              if (rosettaAttributeControlMetadata.getName().equals("positive")) {
                variable.setVerticalDirection(rosettaAttributeControlMetadata.getValue());
              }
            }
          }

          // Initialize empty VariableMetadata lists.
          List<VariableMetadata> required = new ArrayList<>();
          List<VariableMetadata> recommended = new ArrayList<>();
          List<VariableMetadata> additional = new ArrayList<>();

          // Get variable metadata.
          List<RosettaAttribute> variableMetadata = variableInfo.getVariableMetadata();
          if (variableMetadata != null) {

            // This should always be true if column data is used.
            for (RosettaAttribute rosettaAttributeVariableMetadata : variableMetadata) {
              // Ascertain the compliance level.
              String complianceLevel =
                  metadataManager.getComplianceLevelForAttribute(id, rosettaAttributeVariableMetadata.getName());
              if (complianceLevel != null) {
                VariableMetadata varMetadata = new VariableMetadata();
                varMetadata.setComplianceLevel(complianceLevel);
                varMetadata.setMetadataKey(rosettaAttributeVariableMetadata.getName());
                varMetadata.setMetadataValue(rosettaAttributeVariableMetadata.getValue());
                // Populate the correct list with the VariableMetadata object.
                if (complianceLevel.equals("required")) {
                  required.add(varMetadata);
                }
                if (complianceLevel.equals("recommended")) {
                  recommended.add(varMetadata);
                }
                if (complianceLevel.equals("additional")) {
                  additional.add(varMetadata);
                }
              }
            }
          }
          variable.setRequiredMetadata(required);
          if (!recommended.isEmpty()) {
            variable.setRecommendedMetadata(recommended);
          }
          if (!additional.isEmpty()) {
            variable.setAdditionalMetadata(additional);
          }
          variableObjects.add(variable);
        }

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
          for (Variable variable : variableObjects) {
            int variableId = variableMap.get(variable.getColumnNumber());
            variable.setVariableId(variableId);
            variable.setWizardDataId(id);
          }
          variableDao.updatePersistedVariables(variableObjects);
        } else {
          // No persisted data; this is the first time we are persisting it.
          variableDao.persistVariables(id, variableObjects);
        }
      }

    } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      throw new RosettaFileException("Unable to convert template file data to Template object: " + e);
    }
  }



  /**
   * Processes the data submitted by the user containing uploaded file information. Writes the
   * uploaded files to disk. Updates the persisted data corresponding to the provided unique ID
   * with the uploaded file information.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param uploadedFileCmd The user-submitted data containing the uploaded file information.
   * @throws RosettaFileException If unable to write file(s) to disk or a file conversion exception
   *         occurred.
   */
  @Override
  public void processFileUpload(String id, UploadedFileCmd uploadedFileCmd) throws RosettaFileException {

    // Get the persisted data corresponding to this ID
    // (This object is created even if there is not any persisted data.)
    UploadedFileCmd persistedData = lookupPersistedDataById(id);

    // Save uploaded files to disk.
    List<UploadedFile> uploadedFiles = uploadedFileCmd.getUploadedFiles();
    for (UploadedFile uploadedFile : uploadedFiles) {
      // Only bother with saving files that actually exist.
      if (StringUtils.trimToNull(uploadedFile.getFileName()) != null) {

        // Examine the UploadedFile objects (can be "empty") to determine if data
        // has been persisted prior (in which case the object will not be "empty").
        for (UploadedFile persistedFile : persistedData.getUploadedFiles()) {
          // Get the matching file type.
          if (uploadedFile.getFileType().equals(persistedFile.getFileType())) {

            // Save file only if name is NOT the same as the one that is persisted.
            // (If the user visits the prior step in the wizard and doesn't update
            // the uploaded file data, we don't need to process that resubmitted data.)
            if (!uploadedFile.getFileName().equals(persistedFile.getFileName())) {
              int index = uploadedFiles.indexOf(uploadedFile);
              // Write data file to disk.
              String fileName = fileManager.writeUploadedFileToDisk(PropertyUtils.getUserFilesDir(), id,
                  uploadedFile.getFileName(), uploadedFile.getFile());
              // Update file name with CSV version.
              uploadedFile.setFileName(fileName);
              // Update the uploaded files list.
              uploadedFiles.set(index, uploadedFile);

              // Persist file data.
              uploadedFileDao.persistData(id, uploadedFileCmd);

              // If uploaded file is a rosetta TEMPLATE file, parse and persist the data.
              if (uploadedFile.getFileType().equals(FileType.TEMPLATE)) {
                persistTemplateData(id, uploadedFile);
              }
            }
          }
        }
      }
    }
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

}
