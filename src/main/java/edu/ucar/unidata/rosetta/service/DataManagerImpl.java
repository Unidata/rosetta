package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.converters.XlsToCsv;
import edu.ucar.unidata.rosetta.repository.DataDao;
import edu.ucar.unidata.rosetta.repository.PropertiesDao;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.activation.UnsupportedDataTypeException;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * @author oxelson@ucar.edu
 */
public class DataManagerImpl implements DataManager {

    protected static final Logger logger = Logger.getLogger(DataManagerImpl.class);

    private DataDao dataDao;
    private PropertiesDao propertiesDao;

    @Resource(name = "resourceManager")
    private ResourceManager resourceManager;
    @Resource(name = "fileParserManager")
    private FileParserManager fileParserManager;

    /**
     * Sets the data access object (DAO) for the Data object which will acquire and persist
     * the data passed to it via the methods of this DataManager.
     *
     * @param dataDao  The service DAO representing a Data object.
     */
    public void setDataDao(DataDao dataDao) {
        this.dataDao = dataDao;
    }

    /**
     * Sets the data access object (DAO) for the RosettaProperties object which will acquire
     * and persist the data passed to it via the methods of this DataManager.
     *
     * @param propertiesDao  The service DAO representing a Data object.
     */
    public void setPropertiesDao(PropertiesDao propertiesDao) {
        this.propertiesDao = propertiesDao;
    }

    /**
     * Looks up and retrieves a Data object using the given id.
     *
     * @param id    The id of the Data object.
     * @return      The Data object corresponding to the given id.
     */
    @Override
    public Data lookupById(String id) {
        return dataDao.lookupById(id);
    }

    /**
     * Persists the information in the given data object.
     *
     * @param data  The Data object to persist.
     */
    @Override
    public void persistData(Data data, HttpServletRequest request) {
        data.setId(createUniqueDataId(request)); // Create a unique ID for this object.
        if (data.getPlatform() != null) {
            String community = resourceManager.getCommunity(data.getPlatform());
            data.setCommunity(community);
        }
        dataDao.persistData(data);
    }

    /**
     * Updated the persisted information corresponding to the given data object.
     *
     * @param data  The data object to update.
     */
    @Override
    public void updateData(Data data) {
        dataDao.updatePersistedData(data);
    }

    /**
     * Deletes the persisted data object information.
     *
     * @param id    The id of the Data object to delete.
     */
    @Override
    public void deleteData(String id) {
        dataDao.deletePersistedData(id);
    }

    /**
     * Retrieves the name of the directory used for storing uploaded files.
     *
     * @return  The name of the directory used for storing uploaded files.
     */
    @Override
    public String getUploadDir() {
        return propertiesDao.lookupUploadDirectory();
    }

    /**
     * Retrieves the name of the directory used for storing files for downloading.
     *
     * @return  The name of the directory used for storing files for downloading.
     */
    public String getDownloadDir() {
        return propertiesDao.lookupDownloadDirectory();
    }

    /**
     * Converts .xls and .xlsx files to .csv files.
     *
     * @param id        The unique id associated with the file (a subdir in the uploads directory).
     * @param fileName  The name of the .xls or .xlsx file to convert.
     * @return          The name of the converted .csv file.
     * @throws IOException  If unable to convert to .csv file.
     */
    @Override
    public String convertToCSV(String id, String fileName) throws IOException {
        String extension = FilenameUtils.getExtension(fileName);
        if (!(extension.equals("xls") || extension.equals("xlsx")))
            throw new UnsupportedDataTypeException("Attempting to convert a non .xls type file to .csv format.");

        String xlsFilePath = FilenameUtils.concat(FilenameUtils.concat(getUploadDir(), id), fileName);
        // Change the file on disk.
        boolean conversionSuccessful = XlsToCsv.convert(xlsFilePath, null);
        String csvFileName = null;
        if (conversionSuccessful)
            csvFileName = FilenameUtils.removeExtension(fileName) + ".csv";
        else
            throw new IOException("Attempting to convert a non .xls type file to .csv format.");

        return csvFileName;
    }

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
    @Override
    public void writeUploadedFileToDisk(String id, String fileName, CommonsMultipartFile file) throws SecurityException, IOException {

        String filePath = FilenameUtils.concat(getUploadDir(), id);
        File localFileDir = new File(filePath);

        if (!localFileDir.exists())
            if (!localFileDir.mkdirs())
                throw new IOException("Unable to create " + id + " subdirectory in uploads directory.");

        logger.info("Writing uploaded file " + fileName + " to disk");
        File uploadedFile = new File(FilenameUtils.concat(filePath, fileName));

        FileOutputStream outputStream = new FileOutputStream(uploadedFile);
        outputStream.write(file.getFileItem().get());
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Retrieves the data file from disk and parses it by line, converting it into a JSON string.
     *
     * @param id  The unique id associated with the file (a subdir in the uploads directory).
     * @param dataFileName  The file to parse.
     * @return  A JSON String of the file data parsed by line.
     * @throws IOException  For any file I/O or JSON conversions problems.
     */
    public String parseDataFileByLine(String id, String dataFileName) throws IOException {
        String filePath = FilenameUtils.concat( FilenameUtils.concat(getUploadDir(), id), dataFileName);
        return fileParserManager.parseByLine(filePath);
    }

    /**
     * Returns the symbol corresponding to the given delimiter string.
     *
     * @param delimiter The delimiter string.
     * @return  The symbol corresponding to the given string.
     */
    public String getDelimiterSymbol(String delimiter) {
        Map<String, String> delimiters = new HashMap<>();
        delimiters.put("Tab", "\t");
        delimiters.put("Comma", ",");
        delimiters.put("Whitespace", " ");
        delimiters.put("Colon", ":");
        delimiters.put("Semicolon", ";");
        delimiters.put("Single Quote", "'");
        delimiters.put("Double Quote", "\"");

        return delimiters.get(delimiter);
    }

    public String getCFTypeFromPlatform(String platform) {
        String cfType = null;
        List<Map> platforms = (List<Map>) resourceManager.loadResources().get("platforms");
        for(Map p : platforms) {
            if(p.get("name").equals(platform)) {
                cfType = (String) p.get("type");
                break;
            }
        }
        return cfType;
    }

    /**
     * Creates a unique id for the file name from the clients IP address and the date.
     *
     * @param request   The HttpServletRequest used to get the IP address.
     * @return          The unique id.
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
     * Attempts to get the client IP address from the request.
     *
     * @param request   The HttpServletRequest.
     * @return          The client's IP address.
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
}
