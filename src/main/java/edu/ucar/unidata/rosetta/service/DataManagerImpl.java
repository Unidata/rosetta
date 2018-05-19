package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.converters.XlsToCsv;
import edu.ucar.unidata.rosetta.repository.DataDao;
import edu.ucar.unidata.rosetta.repository.PropertiesDao;
import edu.ucar.unidata.rosetta.service.exceptions.RosettaDataException;

import java.beans.Statement;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
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
        data.setId(createUniqueDataId(request)); // Create a unqiue ID for this object.
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
     * Combines non-null user-provided entries with persisted entries in a single Data object.
     * A new Data is created from the persisted data and is populated with the non-null values
     * of the user-provided information using reflection.
     *
     * @param id    The id of the persisted data associated with the provided Data object.
     * @param data  The Data object containing the user-provided data.
     * @return  A data object that contains the new user-provided data and the persisted data.
     * @throws RosettaDataException  If unable to populate the Data object by reflection.
     */
    public Data populateDataObjectWithInput(String id, Data data) throws RosettaDataException {
        // Data persisted in the database.
        Data persistedData = lookupById(id);

        try {
            for (Method method : data.getClass().getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())
                        && method.getParameterTypes().length == 0
                        && method.getReturnType() != void.class
                        && (method.getName().startsWith("get"))
                        ) {
                    Object value = method.invoke(data);
                    if (value != null) {
                        if (value instanceof String) {
                            if ("".equals((String) value)) {
                                continue;
                            } else {
                                Statement statement = new Statement(persistedData, method.getName().replaceFirst("get", "set"), new Object[]{value});
                                statement.execute();
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {
            /*
            NOTE: code in the try block actually throws a bunch of different exceptions, including
            java.lang.Exception itself.  Hence, the use catch of the generic Exception class.
             */
            throw new RosettaDataException("Unable to populate data object by reflection: " + e);
        }
        return persistedData;
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
     * Creates a unique id for the file name from the clients IP address and the date.
     *
     * @param request   The HttpServletRequest used to get the IP address.
     * @return          The unique id.
    */
    private String createUniqueDataId(HttpServletRequest request) {
        String id = String.valueOf(new Date().hashCode());
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
