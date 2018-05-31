package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.FileType;
import edu.ucar.unidata.rosetta.domain.resources.Platform;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

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
     * Processes the data submitted by the user containing uploaded file information.
     * Writes the uploaded files to disk. Updates the persisted data corresponding
     * to the provided unique ID with the uploaded file information.
     *
     * @param id    The unique ID corresponding to already persisted data.
     * @param data  The Data object submitted by the user containing the uploaded file information.
     * @return  The url redirect view used to send the user to the next step in the controller.
     * @throws IOException  If unable to write file(s) to disk.
     */
    public String processFileUpload(String id, Data data) throws IOException;

}
