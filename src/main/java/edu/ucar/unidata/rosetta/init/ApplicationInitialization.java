package edu.ucar.unidata.rosetta.init;

import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.init.resources.*;
import edu.ucar.unidata.rosetta.service.ServerInfoBean;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.stream.Stream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.springframework.dao.NonTransientDataAccessResourceException;

/**
 * Done at application initialization. If first time application is run, it create the database and
 * populates it with the data loaded from the application.properties information.
 *
 * @author oxelson@ucar.edu
 */
public class ApplicationInitialization implements ServletContextListener {

  private static final Logger logger = Logger.getLogger(ApplicationInitialization.class);

  private static final String ROSETTA_HOME = System
      .getProperty("rosetta.content.root.path");  // set in $JAVA_OPTS
  private static final String CONFIG_FILE = "application.properties";
  private static final String DOWNLOAD_DIR = "downloads";
  private static final String UPLOAD_DIR = "uploads";
  private static final int DEFAULT_MAX_UPLOAD_SIZE = 52430000;

  /**
   * Compares the configuration information in the user's application.properties file in
   * ROSETTA_HOME with the default configuration settings of this version of Rosetta to see if there
   * are any new configs to be added.  Missing configuration properties are noted in the logs and
   * handed over to another method to be written out to the user's application.properties file.
   * TODO: In future, notify admin of these differences via interface and let him/her sort it out.
   *
   * @param props The configuration properties as per the application.properties file in
   * ROSETTA_HOME.
   * @return Updated properties including any new default properties added in more recent versions
   * of Rosetta.
   * @throws IOException If unable to compare the configuration properties to the default
   * properties.
   */
  private Properties comparePropertiesBetweenVersions(Properties props)
      throws IOException, SQLException {

    try {
      // Get the default properties.
      Properties defaultProps = getDefaultProperties();

      logger.info("Comparing default configuration settings of this version of Rosetta (" +
          ServerInfoBean.getVersion() + ") with prior configurations in application.properties");

      Properties missingProperties = new Properties();
      Enumeration propertyNames = (Enumeration) defaultProps.propertyNames();
      while (propertyNames.hasMoreElements()) {
        String key = (String) propertyNames.nextElement();
        // If the application.properties file in ROSETTA_HOME doesn't contain the default prop.
        if (!props.containsKey(key)) {
          String value = defaultProps.getProperty(key);
          logger.info("Default property " + key + " missing from configuration information.");
          missingProperties.setProperty(key, value); // Add to missing properties.
          props.setProperty(key, value); // Add to user's collection of properties.
        }
      }
      // Write any missing default properties to the user's application.properties file in ROSETTA_HOME.
      if (!missingProperties.isEmpty()) {
        writeNewPropertiesToConfigFile(missingProperties);
      }
    } catch (IOException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors);
    }
    return props;
  }

  /**
   * Receives notification that the web application initialization process is starting.
   *
   * Information from the application.properties file and JAVA_OPTS is read and used to create the
   * following resources needed by rosetta if they do not already exit:
   *
   * - ROSETTA_HOME - uploads & downloads directory - rosetta database
   *
   * @param servletContextEvent Event notifications regarding changes to the ServletContext
   * lifecycle.
   */
  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {

    logger.info("Application context initialization...");

    // ROSETTA_HOME not specified in $JAVA_OPTS
    if (ROSETTA_HOME == null) {
      logger.error("ROSETTA_HOME not specified in $JAVA_OPTS.  Cannot proceed.");
      contextDestroyed(servletContextEvent); // Can't proceed without ROSETTA_HOME, so die.
    }

    try {
      // Create ROSETTA_HOME if it doesn't already exists.
      createRosettaHomeDirectory();

      // If a application.properties file exists, get the contents.
      Properties props = getPropertiesFromConfigFile();

      // Set ROSETTA_HOME property.
      props.setProperty("rosetta.home", System.getProperty("rosetta.home", ROSETTA_HOME));

      // If file max upload size is not specified, use the default.
      if (props.getProperty("rosetta.maxUpload") == null) {
        props.setProperty("rosetta.maxUpload", String.valueOf(DEFAULT_MAX_UPLOAD_SIZE));
      }

      // Create directory where downloads are stashed.
      props.setProperty("rosetta.downloadDir", createDownloadsDirectory());

      // Create directory where uploads are stashed.
      props.setProperty("rosetta.uploadDir", createUploadDirectory());

      // Compare properties between
      Properties missingProperties = comparePropertiesBetweenVersions(props);

      // Initialize the database init manager.
      DbInitManager dbInitManager = new EmbeddedDerbyDbInitManager();
      // Create the database and populate with the relevant prop values if it doesn't already exist.
      dbInitManager.createDatabase(props);

    } catch (SecurityException | IOException | SQLException | NonTransientDataAccessResourceException | RosettaDataException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors);
      // Can't continue if important directories and properties do not exist or if we have database issues, so die.
      contextDestroyed(servletContextEvent);
    }
  }

  /**
   * Receives notification that the ServletContext is about to be shut down.
   *
   * @param servletContextEvent Event notifications regarding changes to the ServletContext
   * lifecycle.
   */
  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    logger.info("Application context destruction...");

    try {
      // If a application.properties file exists, get the contents.
      Properties props = getPropertiesFromConfigFile();

      // Set rosettaHome.
      String rosettaHome = System.getProperty("rosetta.home", ROSETTA_HOME);
      props.setProperty("rosetta.home", rosettaHome);

      EmbeddedDerbyDbInitManager dbInitManager = new EmbeddedDerbyDbInitManager();
      dbInitManager.shutdownDatabase(props);
    } catch (IOException | SQLException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors);
    }
    logger.error("Goodbye.");
  }

  /**
   * Makes a copy of the default configuration file in ROSETTA_HOME.
   *
   * @param destination The file to be created in ROSETTA_HOME.
   * @throws IOException If unable to make a copy of the configuration file in ROSETTA_HOME.
   */
  private void copyDefaultConfigFile(File destination) throws IOException {
    logger.info("Copying default configuration application.properties file...");
    // Get the default configuration file.
    ClassLoader classLoader = getClass().getClassLoader();
    File sourceFile = new File(classLoader.getResource(CONFIG_FILE).getFile());

    // Make a copy of the configuration file in ROSETTA_HOME.
    FileUtils.copyFile(sourceFile, destination);
  }

  /**
   * Create directory where rosetta stashes files that will be downloaded by users.
   *
   * @return The full path to the downloads directory.
   * @throws IOException If unable to create the downloads directory.
   */
  private String createDownloadsDirectory() throws IOException {
    File downloadDir = new File(FilenameUtils.concat(ROSETTA_HOME, DOWNLOAD_DIR));

    // If this directory doesn't exist, create it.
    if (!downloadDir.exists()) {
      logger.info("Creating downloads directory at " + downloadDir.getPath());
      if (!downloadDir.mkdirs()) {
        throw new IOException("Unable to create downloads directory.");
      }
    }
    return String.valueOf(downloadDir);
  }

  /**
   * Create the very important ROSETTA_HOME directory, in which data is stored.
   *
   * @throws IOException If unable to create the downloads directory.
   */
  private void createRosettaHomeDirectory() throws IOException {
    File rosettaHome = new File(ROSETTA_HOME);

    // If ROSETTA_HOME doesn't exist, create it.
    if (!rosettaHome.exists()) {
      logger.info("Creating ROSETTA_HOME directory at " + ROSETTA_HOME);

      if (!rosettaHome.mkdirs()) {
        throw new IOException("Unable to create ROSETTA_HOME directory.");
      }
    }
  }

  /**
   * Create directory where rosetta stashes files uploaded by users.
   *
   * @return The full path to the uploads directory.
   * @throws IOException If unable to create the uploads directory.
   */
  private String createUploadDirectory() throws IOException {
    File uploadDir = new File(FilenameUtils.concat(ROSETTA_HOME, UPLOAD_DIR));

    // If this directory doesn't exist, create it.
    if (!uploadDir.exists()) {
      logger.info("Creating uploads directory at " + uploadDir.getPath());
      if (!uploadDir.mkdirs()) {
        throw new IOException("Unable to create uploads directory.");
      }
    }
    return String.valueOf(uploadDir);
  }

  /**
   * Loads and returns all the properties listed in the DEFAULT application.properties configuration
   * file. These property values come bundled with the Rosetta application and are found under
   * WEB-INF/classes.
   *
   * @return All of the properties listed the DEFAULT application.properties configuration file.
   * @throws IOException If unable to load properties from configuration file.
   */
  private Properties getDefaultProperties() throws IOException {

    Properties props = new Properties();
    // Get the default configuration file.
    ClassLoader classLoader = getClass().getClassLoader();

    // Load configuration file properties.
    logger.info("Reading default application.properties configuration file...");

    // Load the default configuration data, filtering out the unwanted lines.
    try (Stream<String> stream = Files
        .lines(Paths.get(classLoader.getResource(CONFIG_FILE).getFile()))) {
      stream.filter(StringUtils::isNotBlank)  // Filter out blank lines.
          .filter(line -> !StringUtils.startsWith(line, "#")) // Filter out comment lines
          .filter(line -> !StringUtils.startsWith(line, "jdbc")) // Filter out database configs
          .map(line -> line.split("=")) // Tokenize the line.
          .forEach(tokenizedLineData -> props.setProperty(tokenizedLineData[0],
              tokenizedLineData[1])); // Add tokenized line data to props.
    }
    return props;
  }

  /**
   * Loads and returns all the properties listed in the application.properties configuration file.
   *
   * @return All of the properties listed the application.properties configuration file.
   * @throws IOException If unable to load properties from configuration file.
   */
  private Properties getPropertiesFromConfigFile() throws IOException {
    Properties props = new Properties();
    File configFile = new File(FilenameUtils.concat(ROSETTA_HOME, CONFIG_FILE));

    // Config file doesn't exist.  Create default file in ROSETTA_HOME.
    if (!configFile.exists()) {
      copyDefaultConfigFile(configFile);
    }

    // Load configuration file properties.
    logger.info("Reading " + configFile + " configuration file...");
    try (FileInputStream configFileIS = new FileInputStream(configFile)) {
      props.load(configFileIS);
    }

    return props;
  }

  /**
   * Writes the provided default properties to the application.properties configuration file in
   * ROSETTA_HOME to ensure user's copy is the most recent and contains any new additions added in
   * more recent versions of Rosetta.
   *
   * @param propsToAdd The default properties to add to the configuration file.
   * @throws IOException If unable to write default properties to configuration file.
   */
  private void writeNewPropertiesToConfigFile(Properties propsToAdd) throws IOException {
    File configFile = new File(FilenameUtils.concat(ROSETTA_HOME, CONFIG_FILE));

    try (BufferedWriter bufferedWriter = new BufferedWriter(
        new FileWriter(configFile.getAbsoluteFile(), true))) {
      Enumeration propertyNames = (Enumeration) propsToAdd.propertyNames();
      Date now = new Date(System.currentTimeMillis());
      bufferedWriter.write("\n\n# ADDING DEFAULT PROPERTIES VALUES " + now.toString());

      while (propertyNames.hasMoreElements()) {
        String key = (String) propertyNames.nextElement();
        String value = propsToAdd.getProperty(key);
        logger.info("Adding default property " + key + "=" + value + " to configuration file.");
        bufferedWriter.write("\n" + key + "=" + value);
      }
    }
  }
}
