package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.Data;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import edu.ucar.unidata.rosetta.service.exceptions.RosettaDataException;
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
     * Combines non-null user-provided entries with persisted entries in a single Data object.
     * A new Data is created from the persisted data and is populated with the non-null values
     * of the user-provided information using reflection.
     *
     * @param id    The id of the persisted data associated with the provided Data object.
     * @param data  The Data object containing the user-provided data.
     * @return  A data object that contains the new user-provided data and the persisted data.
     * @throws RosettaDataException  If unable to populate the Data object by reflection.
     */
    public Data populateDataObjectWithInput(String id, Data data) throws RosettaDataException;

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

}
