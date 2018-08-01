package edu.ucar.unidata.rosetta.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import edu.ucar.unidata.rosetta.repository.PropertiesDao;

/**
 * Utils class with methods for accessing rosetta properties.
 *
 * @author oxelson@ucar.edu
 */
public class PropertyUtils {

    private static PropertiesDao propertiesDao;

    /**
     * Creates a unique id for the file name from the clients IP address and the date.
     *
     * @param request The HttpServletRequest used to get the IP address.
     * @return The unique id.
     */
    public static String createUniqueDataId(HttpServletRequest request) {
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
     * Retrieves the name of the directory used for storing files for downloading.
     *
     * @return The name of the directory used for storing files for downloading.
     */
    public static String getDownloadDir() {
        return propertiesDao.lookupDownloadDirectory();
    }

    /**
     * Attempts to get the client IP address from the request.
     *
     * @param request The HttpServletRequest.
     * @return The client's IP address.
     */
    private static String getIpAddress(HttpServletRequest request) {
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
     * Retrieves the name of the directory used for storing uploaded files.
     *
     * @return The name of the directory used for storing uploaded files.
     */
    public static String getUploadDir() {
        return propertiesDao.lookupUploadDirectory();
    }

    /**
     * Sets the data access object (DAO) for the RosettaProperties object.
     *
     * @param propertiesDao The service DAO representing a Data object.
     */
    public void setPropertiesDao(PropertiesDao propertiesDao) {
        this.propertiesDao = propertiesDao;
    }
}
