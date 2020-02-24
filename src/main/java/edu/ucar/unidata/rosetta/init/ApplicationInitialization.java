/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.init;

import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.init.resources.DbInitManager;
import edu.ucar.unidata.rosetta.init.resources.EmbeddedDerbyDbInitManager;
import edu.ucar.unidata.rosetta.service.ServerInfoBean;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.NonTransientDataAccessResourceException;

/**
 * Done at application initialization. If first time application is run, it creates the database and populates it with
 * the data loaded from the configuration information.
 */
public class ApplicationInitialization implements ServletContextListener {

  private static final Logger logger = LogManager.getLogger(ApplicationInitialization.class);

  private static final String ROSETTA_HOME = System.getProperty("rosetta.content.root.path"); // set in $JAVA_OPTS
  private static final String CONFIG_FILE = "application.properties";
  private static final String USER_FILES_DIR = "user_files";
  private static final int DEFAULT_MAX_UPLOAD_SIZE = 52430000;

  /**
   * Compares the configuration information in ROSETTA_HOME with the default configuration settings bundled in rosetta
   * WAR to detect any new configurations. New configuration properties detected in the default config file are noted in
   * the logs and handed over to another method to be written out to the configuration file in ROSETTA_HOME.
   * <p>
   * TODO: In future, notify admin of these differences via interface and let him/her sort it out.
   *
   * @param rosettaHomeProps The properties from the ROSETTA_HOME configuration file.
   */
  private void comparePropertiesBetweenVersions(Properties rosettaHomeProps) {
    try {
      // Get the default properties.
      Properties defaultProps = getDefaultProperties();

      logger.info("Comparing default configuration settings of this version of Rosetta (" + ServerInfoBean.getVersion()
          + ") with prior configurations in found in the ROSETTA_HOME configuration file.");

      Properties newDefaultProperties = new Properties();
      Enumeration propertyNames = defaultProps.propertyNames();
      while (propertyNames.hasMoreElements()) {
        String key = (String) propertyNames.nextElement();
        // If the configuration file in ROSETTA_HOME doesn't contain the default prop.
        if (!rosettaHomeProps.containsKey(key)) {
          String value = defaultProps.getProperty(key);
          logger.info("New default property " + key + " missing from ROSETTA_HOME configuration information.");
          newDefaultProperties.setProperty(key, value); // Add to missing properties.
          rosettaHomeProps.setProperty(key, value); // Add to user's collection of properties.
        }
      }
      // Write any missing default properties to the user's configuration file in ROSETTA_HOME.
      if (!newDefaultProperties.isEmpty()) {
        writeNewPropertiesToConfigFile(newDefaultProperties);
      }
    } catch (IOException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors);
    }
  }

  /**
   * Receives notification that the web application initialization process is starting.
   * <p>
   * Information from the configuration file(s) and JAVA_OPTS is read and used to create the following resources needed
   * by rosetta if they do not already exit:
   * <p>
   * - ROSETTA_HOME <br>
   * - user_files directory <br>
   * - rosetta database
   *
   * @param servletContextEvent Event notifications regarding changes to the ServletContext lifecycle.
   */
  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {

    logger.info("Application context initialization...");

    // ROSETTA_HOME not specified in $JAVA_OPTS.
    if (ROSETTA_HOME == null) {
      logger.error("ROSETTA_HOME not specified in $JAVA_OPTS.  Cannot proceed.");
      contextDestroyed(servletContextEvent); // Can't proceed without ROSETTA_HOME, so die.
    }

    try {
      // Create ROSETTA_HOME if it doesn't already exists.
      createRosettaHomeDirectory();

      // If a configuration file exists in ROSETTA_HOME, get the contents.
      Properties props = getRosettaHomeProperties();

      // Set/add ROSETTA_HOME to properties.
      props.setProperty("rosetta.home", System.getProperty("rosetta.home", ROSETTA_HOME));

      // Now look for properties that influence the rosetta application.
      logger.info("Reading max upload: " + props.getProperty("rosetta.maxUpload"));
      // If file max upload size is not specified, use the default.
      if (props.getProperty("rosetta.maxUpload") == null) {
        props.setProperty("rosetta.maxUpload", String.valueOf(DEFAULT_MAX_UPLOAD_SIZE));
      }

      // Create directory where user files are stashed and set/add it to properties.
      props.setProperty("rosetta.userFilesDir", createUserFilesDirectory());

      // Initialize the database init manager.
      DbInitManager dbInitManager = new EmbeddedDerbyDbInitManager();
      // Create the database and populate with the relevant prop values if it doesn't already exist.
      dbInitManager.createDatabase(props);

    } catch (SecurityException | IOException | SQLException | NonTransientDataAccessResourceException
        | RosettaDataException e) {
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
   * @param servletContextEvent Event notifications regarding changes to the ServletContext lifecycle.
   */
  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    logger.info("Application context destruction...");

    try {
      // If a configuration file exists in ROSETTA_HOME, get the contents.
      Properties props = getRosettaHomeProperties();

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
   * Copies the default configuration file (bundled with WAR file) into ROSETTA_HOME.
   *
   * @param destination The full path of the config file to be created in ROSETTA_HOME.
   * @throws IOException If unable to make a copy of the configuration file in ROSETTA_HOME.
   */
  private void copyDefaultConfigFile(File destination) throws IOException {
    logger.info("Copying default configuration configuration file to ROSETTA_HOME...");
    // Get the default configuration file.
    ClassLoader classLoader = getClass().getClassLoader();
    File sourceFile = new File(Objects.requireNonNull(classLoader.getResource(CONFIG_FILE)).getFile());
    // Make a copy of the configuration file in ROSETTA_HOME.
    FileUtils.copyFile(sourceFile, destination);
  }

  /**
   * Create directory where rosetta stashes files uploaded/downloaded by users.
   *
   * @return The full path to the user_files directory.
   * @throws IOException If unable to create the user_files directory.
   */
  private String createUserFilesDirectory() throws IOException {
    File userFilesDir = new File(FilenameUtils.concat(ROSETTA_HOME, USER_FILES_DIR));

    // If this directory doesn't exist, create it.
    if (!userFilesDir.exists()) {
      logger.info("Creating user_files directory at " + userFilesDir.getPath());
      if (!userFilesDir.mkdirs()) {
        throw new IOException("Unable to create user_files directory.");
      }
    }
    return String.valueOf(userFilesDir);
  }

  /**
   * Create the very important ROSETTA_HOME directory, in which data is stored.
   *
   * @throws IOException If unable to create the ROSETTA_HOME directory.
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
   * Loads and returns all the properties listed in the default configuration file (the default configuration file is
   * bundled in the rosetta WAR and is found under WEB-INF/classes).
   *
   * @return All of the properties listed the default configuration configuration file.
   * @throws IOException If unable to load properties from configuration file.
   */
  private Properties getDefaultProperties() throws IOException {
    // Get the default configuration file.
    ClassLoader classLoader = getClass().getClassLoader();
    // Load the default configuration data, filtering out the unwanted lines.
    File defaultConfigFile = new File(Objects.requireNonNull(classLoader.getResource(CONFIG_FILE)).getFile());
    // Load default configuration file properties.
    logger.info("Reading " + defaultConfigFile + " configuration file...");
    return loadProperties(defaultConfigFile);
  }

  /**
   * Loads and returns all the properties listed in the ROSETTA_HOME configuration file. If a configuration file
   * doesn't exist in ROSETTA_HOME, the default configuration file (file bundled in the rosetta WAR) is copied into
   * ROSETTA_HOME to use.
   *
   * @return All of the properties listed the configuration file.
   * @throws IOException If unable to load properties from configuration file.
   */
  private Properties getRosettaHomeProperties() throws IOException {
    boolean isCopied = false;
    // Get the properties file from ROSETTA_HOME.
    File rosettaHomeConfigFile = new File(FilenameUtils.concat(ROSETTA_HOME, CONFIG_FILE));

    // Config file doesn't exist. Create default file in ROSETTA_HOME.
    if (!rosettaHomeConfigFile.exists()) {
      copyDefaultConfigFile(rosettaHomeConfigFile);
      isCopied = true;
    }

    // Load configuration ROSETTA_HOME file properties.
    logger.info("Reading " + rosettaHomeConfigFile + " configuration file...");
    Properties props = loadProperties(rosettaHomeConfigFile);

    // If the DEFAULT config file was not copied over to ROSETTA_HOME.
    if (!isCopied) {
      // Compare default configuration file and ROSETTA_HOME configuration file.
      comparePropertiesBetweenVersions(props);
    }
    return props;
  }

  /**
   * Loads and returns all the properties in the given configuration file
   *
   * @return All of the properties listed the given configuration file.
   * @throws IOException If unable to load properties from configuration file.
   */
  private Properties loadProperties(File configFile) throws IOException {
    Properties props = new Properties();
    try (FileInputStream configFileIS = new FileInputStream(configFile)) {
      props.load(configFileIS);
    }
    return props;
  }

  /**
   * Writes the provided default properties to the configuration file in ROSETTA_HOME to ensure user's copy is the most
   * recent and contains any new additions added in more recent versions of Rosetta.
   *
   * @param propsToAdd The new default properties to add to the configuration file.
   * @throws IOException If unable to write default properties to configuration file.
   */
  private void writeNewPropertiesToConfigFile(Properties propsToAdd) throws IOException {
    File configFile = new File(FilenameUtils.concat(ROSETTA_HOME, CONFIG_FILE));
    try (Writer writer =
        new OutputStreamWriter(new FileOutputStream(configFile.getAbsoluteFile()), StandardCharsets.UTF_8)) {
      PrintWriter printWriter = new PrintWriter(writer);
      Enumeration propertyNames = propsToAdd.propertyNames();
      Date now = new Date(System.currentTimeMillis());
      printWriter.write("\n\n# ADDING DEFAULT PROPERTIES VALUES " + now.toString());

      while (propertyNames.hasMoreElements()) {
        String key = (String) propertyNames.nextElement();
        String value = propsToAdd.getProperty(key);
        logger.info("Adding default property " + key + "=" + value + " to configuration file.");
        printWriter.write("\n" + key + "=" + value);
      }
    }
  }
}
