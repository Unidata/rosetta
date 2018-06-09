package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.domain.resources.CfType;
import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.FileType;
import edu.ucar.unidata.rosetta.domain.resources.Platform;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ucar.ma2.InvalidRangeException;

/**
 * Service for handling collected data information.
 *
 * @author oxelson@ucar.edu
 */
public interface DataManager {

    /**
     * Converts the uploaded data file(s) to netCDF and writes a template for to aid in future conversions.
     *
     * @param data  The Data object representing the uploaded data file to convert.
     * @return The updated data object containing the converted file information.
     * @throws InvalidRangeException If unable to convert the data file to netCDF.
     * @throws RosettaFileException If unable to create the template file from the Data object.
     */
    public Data convertToNetCDF(Data data) throws InvalidRangeException, RosettaFileException;

    /**
     * Deletes the persisted data object information.
     *
     * @param id    The id of the Data object to delete.
     */
    public void deletePersistedData(String id);

    /**
     * Retrieves the CF Types associated with the given platform.
     *
     * @param platform  The platform.
     * @return  The CF Types associated with the given platform.
     */
    public String getCFTypeFromPlatform(String platform);

    /**
     * Retrieves a list of all the persisted CfType objects.
     *
     * @return  A list of CfType objects.
     */
    public List<CfType> getCFTypes();

    /**
     * Retrieves a list of all the persisted communities.
     *
     * @return  A list of Community objects.
     */
    public List<Community> getCommunities();

    /**
     * Retrieves the community associated with the given platform.
     *
     * @param platform  The platform.
     * @return  The community associated with the given platform.
     */
    public String getCommunityFromPlatform(String platform);

    /**
     * Returns the symbol corresponding to the given delimiter string.
     *
     * @param delimiter The delimiter string.
     * @return  The symbol corresponding to the given string.
     */
    public String getDelimiterSymbol(String delimiter);

    /**
     * Retrieves the name of the directory used for storing files for downloading.
     *
     * @return  The name of the directory used for storing files for downloading.
     */
    public String getDownloadDir();

    /**
     * Retrieves a list of all the persisted FileType objects.
     *
     * @return  A list of FileType objects.
     */
    public List<FileType> getFileTypes();

    /**
     * Pulls the general metadata from a data known file and populates the provided
     * GeneralMetadata object. If the data file type is a custom file (not a known type)
     * then an empty, non-populated GeneralMetadata object is returned.
     *
     * @param filePath  The path to the data file which may contain the metadata we need.
     * @param fileType  The data file type.
     * @param metadata  The GeneralMetadata object to populate.
     * @return  The GeneralMetadata object to populated with the general metadata.
     * @throws RosettaDataException If unable to populate the GeneralMetadata object.
     */
    public GeneralMetadata getMetadataFromKnownFile(String filePath, String fileType, GeneralMetadata metadata) throws RosettaDataException;

    /**
     * Retrieves the persisted metadata associated with the given id & type.
     * Creates and returns string version of the metadata used by client side.
     *
     * @param id    The id of the metadata.
     * @param type  The metadata type.
     * @return  The string version of the metadata used by client side.
     */
    public String getMetadataStringForClient(String id, String type);

    /**
     * Retrieves a list of all the persisted Platform objects.
     *
     * @return  A list of Platform objects.
     */
    public List<Platform> getPlatforms();

    /**
     * Retrieves the name of the directory used for storing uploaded files.
     *
     * @return  The name of the directory used for storing uploaded files.
     */
    public String getUploadDir();

    /**
     * Looks up and retrieves a Data object using the given id.
     *
     * @param id    The id of the Data object.
     * @return      The Data object corresponding to the given id.
     */
    public Data lookupPersistedDataById(String id);

    /**
     * Retrieves the data file from disk and parses it by line, converting it into a JSON string.
     *
     * @param id  The unique id associated with the file (a subdir in the uploads directory).
     * @param dataFileName  The file to parse.
     * @return  A JSON String of the file data parsed by line.
     * @throws RosettaFileException  For any file I/O or JSON conversions problems.
     */
    public String parseDataFileByLine(String id, String dataFileName) throws RosettaFileException;

    /**
     * Persists the information in the given data object.
     *
     * @param data  The Data object to persist.
     */
    public void persistData(Data data, HttpServletRequest request);

    /**
     * Processes the data submitted by the user containing CF type information.
     * If an ID already exists, the persisted data corresponding to that ID is
     * collected and updated with the newly submitted data.  If no ID exists
     * (is null), the data is persisted for the first time.
     *
     * @param id      The unique ID corresponding to already persisted data (may be null).
     * @param data    The Data object submitted by the user containing the CF type information.
     * @param request The HttpServletRequest used to get the IP address to make unique IDs for new data.
     */
    public void processCfType(String id, Data data, HttpServletRequest request);

    /**
     * Processes the data submitted by the user containing custom data file information.
     *
     * @param id    The unique ID corresponding to already persisted data.
     * @param data  The Data object submitted by the user containing the custom data file information.
     */
    public void processCustomFileTypeAttributes(String id, Data data);

    /**
     * Processes the data submitted by the user containing uploaded file information.
     * Writes the uploaded files to disk. Updates the persisted data corresponding
     * to the provided unique ID with the uploaded file information.
     *
     * @param id    The unique ID corresponding to already persisted data.
     * @param data  The Data object submitted by the user containing the uploaded file information.
     * @throws RosettaFileException If unable to write file(s) to disk or a file conversion exception occurred.
     */
    public void processFileUpload(String id, Data data) throws RosettaFileException;

    /**
     * Processes the data submitted by the user containing general metadata information.  Since this
     * is the final step of collecting data in the wizard, the uploaded data file is converted to
     * netCDF format in preparation for user download.
     *
     * @param id    The unique ID corresponding to already persisted data.
     * @param metadata  The Metadata object submitted by the user containing the general metadata information.
     * @throws RosettaDataException  If unable to populate the metadata object.
     */
    public void processGeneralMetadata(String id, GeneralMetadata metadata) throws RosettaDataException;

    /**
     * Determines the next step in the wizard based the user specified data file type.
     * This method is called when there is a divergence of possible routes through the wizard.
     *
     * @param id  The unique ID corresponding to already persisted data.
     * @return  The next step to redirect the user to in the wizard.
     */
    public String processNextStep(String id);

    /**
     * Determines the previous step in the wizard based the user specified data file type.
     * This method is called when there is a divergence of possible routes through the wizard.
     *
     * @param id  The unique ID corresponding to already persisted data.
     * @return  The previous step to redirect the user to in the wizard.
     */
    public String processPreviousStep(String id);

    /**
     * Processes the data submitted by the user containing variable metadata information.
     *
     * @param id    The unique ID corresponding to already persisted data.
     * @param data  The Data object submitted by the user containing variable metadata information.
     */
    public void processVariableMetadata(String id, Data data);

    /**
     * Updates the persisted information corresponding to the given data object.
     *
     * @param data  The data object to update.
     */
    public void updatePersistedData(Data data);
}
