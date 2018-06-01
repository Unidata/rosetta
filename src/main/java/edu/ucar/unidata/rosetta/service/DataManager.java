package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.FileType;
import edu.ucar.unidata.rosetta.domain.resources.Platform;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import ucar.ma2.InvalidRangeException;

/**
 * @author oxelson@ucar.edu
 */
public interface DataManager {

    /**
     * Looks up and retrieves a Data object using the given id.
     *
     * @param id    The id of the Data object.
     * @return      The Data object corresponding to the given id.
     */
    public Data lookupById(String id);

    /**
     * Persists the information in the given data object.
     *
     * @param data  The Data object to persist.
     */
    public void persistData(Data data, HttpServletRequest request);

    /**
     * Updates the persisted information corresponding to the given data object.
     *
     * @param data  The data object to update.
     */
    public void updateData(Data data);

    /**
     * Deletes the persisted data object information.
     *
     * @param id    The id of the Data object to delete.
     */
    public void deleteData(String id);

    /**
     * Retrieves the name of the directory used for storing uploaded files.
     *
     * @return  The name of the directory used for storing uploaded files.
     */
    public String getUploadDir();

    /**
     * Retrieves the name of the directory used for storing files for downloading.
     *
     * @return  The name of the directory used for storing files for downloading.
     */
    public String getDownloadDir();

    /**
     * Converts .xls and .xlsx files to .csv files.
     *
     * @param id        The unique id associated with the file (a subdir in the uploads directory).
     * @param fileName  The name of the .xls or .xlsx file to convert.
     * @return          The name of the converted .csv file.
     * @throws IOException  If unable to convert to .csv file.
     */
    public String convertToCSV(String id, String fileName) throws IOException;

    /**
     * Creates a subdirectory in the designated uploads directory using the (unique) id
     * and writes the given file to the uploads subdirectory.
     *
     * @param id        The unique id associated with the file (a subdir in the uploads directory).
     * @param fileName  The name of the file to save to disk.
     * @param file      The CommonsMultipartFile to save to disk.
     * @throws SecurityException  If unable to write file to disk because of a JVM security manager violation.
     * @throws IOException  If unable to write file to disk.
     */
    public void writeUploadedFileToDisk(String id, String fileName, CommonsMultipartFile file) throws SecurityException, IOException;

    /**
     * Retrieves the data file from disk and parses it by line, converting it into a JSON string.
     *
     * @param id  The unique id associated with the file (a subdir in the uploads directory).
     * @param dataFileName  The file to parse.
     * @return  A JSON String of the file data parsed by line.
     * @throws IOException  For any file I/O or JSON conversions problems.
     */
    public String parseDataFileByLine(String id, String dataFileName) throws IOException;

    /**
     * Returns the symbol corresponding to the given delimiter string.
     *
     * @param delimiter The delimiter string.
     * @return  The symbol corresponding to the given string.
     */
    public String getDelimiterSymbol(String delimiter);

    public String getCFTypeFromPlatform(String platform);

    public String getCommunityFromPlatform(String platform);

    public List<Platform> getPlatforms();

    public List<Map<String, Object>> getPlatformsForView();

    public List<Community> getCommunities();

    public List<Map<String, Object>> getCommunitiesForView();

    public List<FileType> getFileTypes();

    public List<Map<String, Object>> getFileTypesForView();

    public GeneralMetadata getMetadataFromKnownFile(String filePath, String fileType, GeneralMetadata metadata) throws RosettaDataException;

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
     * @throws IOException  If unable to write file(s) to disk.
     */
    public void processFileUpload(String id, Data data) throws IOException;

    /**
     * Processes the data submitted by the user containing general metadata information.  Since this
     * is the final step of collecting data in the wizard, the uploaded data file is converted to
     * netCDF format in preparation for user download.
     *
     * @param id    The unique ID corresponding to already persisted data.
     * @param metadata  The Metadata object submitted by the user containing the general metadata information.
     * @throws InvalidRangeException // If encounters an invalid range while converting file to netCDF.
     * @throws IOException  // If unable to convert file to netCDF format.
     * @throws RosettaDataException  If unable to populate the metadata object.
     */
    public void processGeneralMetadata(String id, GeneralMetadata metadata) throws InvalidRangeException, IOException, RosettaDataException;

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


}
