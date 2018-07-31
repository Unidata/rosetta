package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.converters.TagUniversalFileFormat;
import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.resources.Platform;
import edu.ucar.unidata.rosetta.domain.wizard.CfTypeData;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.repository.PropertiesDao;
import edu.ucar.unidata.rosetta.repository.wizard.DataDao;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import ucar.ma2.InvalidRangeException;

/**
 * Implements DataManager functionality.
 *
 * @author oxelson@ucar.edu
 */
public class DataManagerImpl implements DataManager {

  private static final Logger logger = Logger.getLogger(DataManagerImpl.class);

  private DataDao dataDao;
  private PropertiesDao propertiesDao;

  // The other managers we make use of in this file.
  @Resource(name = "cfTypeDataManager")
  private CfTypeDataManager cfTypeDataManager;

  @Resource(name = "fileParserManager")
  private FileManager fileManager;

  @Resource(name = "metadataManager")
  private MetadataManager metadataManager;

  @Resource(name = "resourceManager")
  private ResourceManager resourceManager;

  /**
   * Converts the uploaded data file(s) to netCDF and writes a template for to aid in future
   * conversions.
   *
   * @param data The Data object representing the uploaded data file to convert.
   * @return The updated data object containing the converted file information.
   * @throws InvalidRangeException If unable to convert the data file to netCDF.
   * @throws RosettaFileException If unable to create the template file from the Data object.
   */
  public Data convertToNetCDF(Data data) throws InvalidRangeException, RosettaFileException {

    // Create full file path to upload sub directory where uploaded data is stored.
    String filePathUploadDir = FilenameUtils.concat(getUploadDir(), data.getId());
    // Create full file path to download sub directory into which the converted .nc files & template will be written.
    String filePathDownloadDir = fileManager
        .createDownloadSubDirectory(getDownloadDir(), data.getId());

    // Create the new name of the datafile with the .nc extension (netCDF version of the file).
    String netcdfFileName = FilenameUtils.removeExtension(data.getDataFileName()) + ".nc";
    // Create the full path to the converted netCDF file in the download sub directory.
    String ncFileToCreate = FilenameUtils.concat(filePathDownloadDir, netcdfFileName);

    // The data file type uploaded by the user decides how it is converted to netCDF.
    // TODO: remove hard coding and get these & converter file processors from DB.
    if (data.getDataFileType().equals("eTuff")) {
      // Tag Universal File Format.
      TagUniversalFileFormat tagUniversalFileFormat = new TagUniversalFileFormat();
      tagUniversalFileFormat.parse(FilenameUtils.concat(filePathUploadDir, data.getDataFileName()));
      tagUniversalFileFormat.convert(ncFileToCreate);

    } else {
      // Custom file type, so we need to convert it here.
      // INSERT CUSTOM FILE CONVERSION CODE HERE.
      List<List<String>> parseFileData = fileManager
          .parseByDelimiter(FilenameUtils.concat(filePathUploadDir, data.getDataFileName()),
              Arrays.asList(data.getHeaderLineNumbers().split(",")),
              resourceManager.getDelimiterSymbol(data.getDelimiter()));
      for (List<String> stringList : parseFileData) {
        for (String s : stringList) {
          logger.info(s);
        }
      }
    }

    // Persists the netCDF file information (used for constructing download link).
    data.setNetcdfFile(netcdfFileName);

    // Create the template file for the user to download along with the netCDF file.
    fileManager.writeDataObject(filePathDownloadDir, data);
    data.setTemplateFileName("rosetta.template");

    // Zip it!
    String dataFileName = data.getDataFileName();
    fileManager.compress(filePathDownloadDir, dataFileName);
    String zipFileName = FilenameUtils.removeExtension(dataFileName);
    data.setZip(FilenameUtils.getName(zipFileName));

    // Update persisted data.
    updatePersistedData(data);

    return data;
  }

  /**
   * Creates a unique id for the file name from the clients IP address and the date.
   *
   * @param request The HttpServletRequest used to get the IP address.
   * @return The unique id.
   */
  private String createUniqueDataId(HttpServletRequest request) {
    String id = String.valueOf(new Date().hashCode());
    id = StringUtils.replaceChars(id, "-", "");
    String ipAddress = getIpAddress(request);
    if (ipAddress != null) {
      id = ipAddress + id;
    } else {
      id = String.valueOf(new Random().nextInt() + id);
    }
    return id.replaceAll(":", "");
  }

  /**
   * Deletes the persisted data object information.
   *
   * @param id The id of the Data object to delete.
   */
  @Override
  public void deletePersistedData(String id) {
    dataDao.deletePersistedData(id);
  }

  /**
   * Retrieves the name of the directory used for storing files for downloading.
   *
   * @return The name of the directory used for storing files for downloading.
   */
  @Override
  public String getDownloadDir() {
    return propertiesDao.lookupDownloadDirectory();
  }

  /**
   * Attempts to get the client IP address from the request.
   *
   * @param request The HttpServletRequest.
   * @return The client's IP address.
   */
  private String getIpAddress(HttpServletRequest request) {
    String ipAddress = null;
    if (request.getRemoteAddr() != null) {
      ipAddress = request.getRemoteAddr();
      ipAddress = StringUtils.deleteWhitespace(ipAddress);
      ipAddress = StringUtils.trimToNull(ipAddress);
      ipAddress = StringUtils.lowerCase(ipAddress);
      ipAddress = StringUtils.replaceChars(ipAddress, ".", "");
    }
    return ipAddress;
  }

  /**
   * Pulls the general metadata from a data known file and populates the provided GeneralMetadata
   * object. If the data file type is a custom file (not a known type) then an empty, non-populated
   * GeneralMetadata object is returned.
   *
   * @param filePath The path to the data file which may contain the metadata we need.
   * @param fileType The data file type.
   * @param metadata The GeneralMetadata object to populate.
   * @return The GeneralMetadata object to populated with the general metadata.
   * @throws RosettaDataException If unable to populate the GeneralMetadata object.
   */
  @Override
  public GeneralMetadata getMetadataFromKnownFile(String filePath, String fileType,
      GeneralMetadata metadata) throws RosettaDataException {
    return metadataManager.getMetadataFromKnownFile(filePath, fileType, metadata);
  }

  /**
   * Retrieves the persisted metadata associated with the given id & type. Creates and returns
   * string version of the metadata used by client side.
   *
   * @param id The id of the metadata.
   * @param type The metadata type.
   * @return The string version of the metadata used by client side.
   */
  @Override
  public String getMetadataStringForClient(String id, String type) {
    return metadataManager.getMetadataStringForClient(id, type);
  }


  /**
   * Retrieves the name of the directory used for storing uploaded files.
   *
   * @return The name of the directory used for storing uploaded files.
   */
  @Override
  public String getUploadDir() {
    return propertiesDao.lookupUploadDirectory();
  }

  /**
   * Looks up and retrieves a Data object using the given id.
   *
   * @param id The id of the Data object.
   * @return The Data object corresponding to the given id.
   */
  @Override
  public Data lookupPersistedDataById(String id) {
    return dataDao.lookupById(id);
  }

  /**
   * Retrieves the data file from disk and parses it by line, converting it into a JSON string.
   *
   * @param id The unique id associated with the file (a subdir in the uploads directory).
   * @param dataFileName The file to parse.
   * @return A JSON String of the file data parsed by line.
   * @throws RosettaFileException For any file I/O or JSON conversions problems.
   */
  @Override
  public String parseDataFileByLine(String id, String dataFileName) throws RosettaFileException {
    String filePath = FilenameUtils.concat(FilenameUtils.concat(getUploadDir(), id), dataFileName);
    return fileManager.parseByLine(filePath);
  }

  /**
   * Persists the information in the given data object.
   *
   * @param data The Data object to persist.
   */
  @Override
  public void persistData(Data data, HttpServletRequest request) {
    data.setId(createUniqueDataId(request)); // Create a unique ID for this object.

    // Get the community associated with the selected platform.
    if (data.getPlatform() != null) {
      Platform platform = resourceManager.getPlatform(data.getPlatform().replaceAll("_", " "));
      data.setCommunity(platform.getCommunity());
    }
    dataDao.persistData(data);
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
  public void processCfType(String id, CfTypeData cfTypeData, HttpServletRequest request) throws RosettaDataException {

    // If the ID is present, then there is a cookie.  Combine new with previous persisted data.
    if (id != null) {

      // Get the persisted CF type data corresponding to this ID.
      CfTypeData persistedData = cfTypeDataManager.lookupPersistedCfTypeDataById(id);

      // Update platform value.
      persistedData.setPlatform(persistedData.getPlatform());

      // Update community if needed.
      if (cfTypeData.getPlatform() != null) {
        persistedData.setCommunity(resourceManager.getCommunityFromPlatform(cfTypeData.getPlatform()));
      } else {
        persistedData.setCommunity(null);
      }

      // Update CF type.
      persistedData.setCfType(cfTypeData.getCfType());

      // Update persisted CF type data.
      cfTypeDataManager.updatePersistedCfTypeData(persistedData);

    } else {
      // No ID yet.  First time persisting CF type data.
      cfTypeData.setId(createUniqueDataId(request)); // Create a unique ID for this object.
      String metadataProfile = assessMetadataProfile(cfTypeData); // Set metadata profile.
      cfTypeDataManager.persistCfTypeData(cfTypeData);
    }
  }

  /**
   * Processes the data submitted by the user containing custom data file information.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param data The Data object submitted by the user containing the custom data file information.
   */
  @Override
  public void processCustomFileTypeAttributes(String id, Data data) {

    // Get the persisted data.
    Data persistedData = lookupPersistedDataById(id);

    // Handle boolean values of the Data object for header lines.
    if (data.getNoHeaderLines()) {
      // set no header lines.
      persistedData.setNoHeaderLines(true);
      // Remove any previously persisted headerlines.
      persistedData.setHeaderLineNumbers(null);
    } else {
      // Set header lines.
      persistedData.setNoHeaderLines(false);
      persistedData.setHeaderLineNumbers(data.getHeaderLineNumbers());
    }
    // Set delimiter.
    persistedData.setDelimiter(data.getDelimiter());

    // Update persisted data.
    updatePersistedData(persistedData);
  }

  /**
   * Processes the data submitted by the user containing uploaded file information. Writes the
   * uploaded files to disk. Updates the persisted data corresponding to the provided unique ID with
   * the uploaded file information. TODO: refactor to allow more than one data file for batch
   * processing.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param data The Data object submitted by the user containing the uploaded file information.
   * @throws RosettaFileException If unable to write file(s) to disk or a file conversion exception
   * occurred.
   */
  @Override
  public void processFileUpload(String id, Data data) throws RosettaFileException {

    // Get the persisted data corresponding to this ID.
    Data persistedData = lookupPersistedDataById(id);

    // If a data file has been uploaded.
    if (!data.getDataFile().isEmpty()) {
      // Set the data file type.
      persistedData.setDataFileType(data.getDataFileType());

      // Get the data file name as a lot of the following code keys off of it.
      String dataFileName = data.getDataFileName();

      // Write data file to disk.
      dataFileName = fileManager
          .writeUploadedFileToDisk(getUploadDir(), persistedData.getId(), dataFileName,
              data.getDataFile());

      // May need to uncompress the uploaded data file(s) and handle them.
      String dataFileNameExtension = FilenameUtils.getExtension(dataFileName);

      // Set the data file name.
      persistedData.setDataFileName(dataFileName);

      // Is it a Zip file?
      if (dataFileNameExtension.equals("zip")) {
        uncompress(persistedData, dataFileName);
      }

    } else {
      //
      persistedData.setDataFileType(data.getDataFileType());
    }

    // If a positional file has been uploaded.
    if (!data.getPositionalFile().isEmpty()) {
      String positionalFileName = data.getPositionalFileName();
      // Write file to disk.
      positionalFileName = fileManager
          .writeUploadedFileToDisk(getUploadDir(), persistedData.getId(), positionalFileName,
              data.getPositionalFile());
      // Set the positional file name.
      persistedData.setPositionalFileName(positionalFileName);
    } else {
      // no file and no file name, user is 'undoing' the upload.
      if (data.getPositionalFileName().equals("")) {
        persistedData.setPositionalFileName(null);
      }
    }

    // If a template file has been uploaded.
    if (!data.getTemplateFile().isEmpty()) {
      String templateFileName = data.getTemplateFileName();
      // Write file to disk.
      templateFileName = fileManager
          .writeUploadedFileToDisk(getUploadDir(), persistedData.getId(), templateFileName,
              data.getTemplateFile());
      // Set the template file name.
      persistedData.setTemplateFileName(templateFileName);
    } else {
      // no file and no file name, user is 'undoing' the upload.
      if (data.getTemplateFileName().equals("")) {
        persistedData.setTemplateFileName(null);
      }

    }
    // Update persisted data.
    updatePersistedData(persistedData);
  }

  /**
   * Processes the data submitted by the user containing general metadata information.  Since this
   * is the final step of collecting data in the wizard, the uploaded data file is converted to
   * netCDF format in preparation for user download.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param metadata The Metadata object submitted by the user containing the general metadata
   * information.
   * @throws RosettaDataException If unable to populate the metadata object.
   */
  @Override
  public void processGeneralMetadata(String id, GeneralMetadata metadata)
      throws RosettaDataException {

    // Persist the global metadata.
    metadataManager.persistMetadata(metadataManager.parseGeneralMetadata(metadata, id));
  }

  /**
   * Determines the next step in the wizard based the user specified data file type. This method is
   * called when there is a divergence of possible routes through the wizard.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @return The next step to redirect the user to in the wizard.
   */
  @Override
  public String processNextStep(String id) {

    // The placeholder for what we are going to return.
    String nextStep;

    // Get the persisted data.
    Data persistedData = lookupPersistedDataById(id);

    // The next step depends on what the user specified for the data file type.
    if (persistedData.getDataFileType().equals("Custom_File_Type")) {
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

    // Get the persisted data.
    Data persistedData = lookupPersistedDataById(id);

    // The previous step (if the user chooses to go there) depends
    // on what the user specified for the data file type.
    if (persistedData.getDataFileType().equals("Custom_File_Type")) {
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
   * @param data The Data object submitted by the user containing variable metadata information.
   */
  @Override
  public void processVariableMetadata(String id, Data data) {
    // Persist the variable metadata.
    metadataManager
        .persistMetadata(metadataManager.parseVariableMetadata(data.getVariableMetadata(), id));
  }

  /**
   * Sets the data access object (DAO) for the Data object which will acquire and persist the data
   * passed to it via the methods of this DataManager.
   *
   * @param dataDao The service DAO representing a Data object.
   */
  public void setDataDao(DataDao dataDao) {
    this.dataDao = dataDao;
  }

  /**
   * Sets the data access object (DAO) for the RosettaProperties object which will acquire and
   * persist the data passed to it via the methods of this DataManager.
   *
   * @param propertiesDao The service DAO representing a Data object.
   */
  public void setPropertiesDao(PropertiesDao propertiesDao) {
    this.propertiesDao = propertiesDao;
  }

  /**
   * Uncompresses a compressed file, looks at the inventory of that file and updates the persisted
   * data information based on the file contents.
   *
   * @param data The Data object containing the relevant data needed for the uncompression.
   * @param dataFileName The data file name to uncompress.
   * @throws RosettaFileException If unable to uncompress data file.
   */
  private void uncompress(Data data, String dataFileName) throws RosettaFileException {
    // Unzip.
    fileManager.uncompress(getUploadDir(), data.getId(), dataFileName);
    // Get the zip file contents inventory
    List<String> inventory = fileManager
        .getInventoryData(FilenameUtils.concat(getUploadDir(), data.getId()));
    // Looks at the inventory contents and assign to the data object accordingly.
    for (String entry : inventory) {
      if (entry.contains("rosetta.template"))
      // Template file.
      {
        data.setTemplateFileName(entry);
      } else
      // Data file.
      {
        data.setDataFileName(entry);
      }
    }
  }

  /**
   * Updated the persisted information corresponding to the given data object.
   *
   * @param data The data object to update.
   */
  @Override
  public void updatePersistedData(Data data) {
    dataDao.updatePersistedData(data);
  }


  //-------------------------//

  public CfTypeData lookupPersistedCfTypeDataById(String id) {
    return cfTypeDataManager.lookupPersistedCfTypeDataById(id);
  }


  private String assessMetadataProfile(CfTypeData cfTypeData) throws RosettaDataException {
    // Assign metadata profile to value specified in CfTypeData object.
    String metadataProfile = cfTypeData.getMetadataProfile();

    // If metadata profile value isn't null we can return (below).
    if(metadataProfile == null) {

      // Use the provided community/platform to figure out metadata profile.
      String userSelectedCommuntity = cfTypeData.getCommunity();

      if(userSelectedCommuntity != null) {

        for (MetadataProfile metadataProfileResource: resourceManager.getMetadataProfiles()) {
          List<Community> communities = metadataProfileResource.getCommunities();
          metadataProfile = "";
          if(communities.contains(userSelectedCommuntity)) {
            metadataProfile = metadataProfile + metadataProfileResource.getName() + ",";
            metadataProfile = metadataProfile.substring(0, metadataProfile.length() - 1);
          }
        }

      } else {
        // This shouldn't happen!
        // Either the platform/community or the the CF type/metadata profile must exist.
        throw new RosettaDataException("Neither metadata profile or community values present: "
            + cfTypeData.toString());
      }
    }
    return metadataProfile;
  }



}
