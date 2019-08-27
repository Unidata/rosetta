/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import edu.ucar.unidata.rosetta.repository.PropertiesDao;

/**
 * Utils class with methods for accessing rosetta properties.
 */
public class PropertyUtils {

  private static final Logger logger = Logger.getLogger(PropertyUtils.class);

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
   * Retrieves the name of the directory used for storing user files.
   *
   * @return The name of the directory used for storing user files.
   */
  public static String getUserFilesDir() {
    return propertiesDao.lookupUserFilesDirectory();
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
   * Returns the host name for the server running rosetta.
   *
   * @return the host name.
   */
  public static String getHostName() {
    InetAddress ip;
    String hostname = null;
    try {
      ip = InetAddress.getLocalHost();
      hostname = ip.getHostName();
    } catch (UnknownHostException e) {
      logger.error(e);
    }
    return hostname;
  }

  /**
   * Sets the data access object (DAO) for the RosettaProperties object.
   *
   * @param propertiesDao The service DAO representing a Property object.
   */
  public void setPropertiesDao(PropertiesDao propertiesDao) {
    this.propertiesDao = propertiesDao;
  }
}
