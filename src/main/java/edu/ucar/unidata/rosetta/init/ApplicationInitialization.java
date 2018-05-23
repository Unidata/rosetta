package edu.ucar.unidata.rosetta.init;

import edu.ucar.unidata.rosetta.service.EmbeddedDerbyDbInitManager;

import java.io.*;

import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import org.springframework.dao.NonTransientDataAccessResourceException;

/**
 * Done at application initialization.
 * If first time application is run, it create the database and populates it with
 * the data loaded from the application.properties information.
 *
 * @author oxelson@ucar.edu
 */
public class ApplicationInitialization implements ServletContextListener {

    // Handles creation & shutdown of the rosetta database.
   // @Resource(name = "dbInitManager")
   // private DbInitManager dbInitManager;

    protected static Logger logger = Logger.getLogger(ApplicationInitialization.class);

    private static final String DEFAULT_ROSETTA_HOME = System.getProperty("catalina.base") + "/rosetta";
    private static final String CONFIG_FILE = "application.properties";
    private static final String DEFAULT_DOWNLOAD_DIR = "downloads";
    private static final String DEFAULT_UPLOAD_DIR = "uploads";
    private static final int DEFAULT_MAX_UPLOAD_SIZE = 52430000;

    /**
     * Receives notification that the web application initialization process is starting.
     *
     * Information from the application.properties file and JAVA_OPTS is read and used to create
     * the following resources needed by rosetta if they do not already exit:
     *
     *      - ROSETTA_HOME
     *      - uploads & downloads directory
     *      - rosetta database
     *
     * @param servletContextEvent  Event notifications regarding changes to the ServletContext lifecycle.
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent)  {

        logger.info("Application context initialization...");

        // If a application.properties file exists, get the contents.
        Properties props = getProperties();
        String servletContainerHome = props.getProperty("servletContainer.home");

        // rosettaHome not specified in config file. Use default.
        String rosettaHome = props.getProperty("rosetta.home");

        if (rosettaHome != null) {
            if (servletContainerHome != null) {
                rosettaHome = rosettaHome.replaceAll("\\$\\{servletContainer.home\\}", servletContainerHome);
            }
            props.setProperty("rosetta.home", rosettaHome);
        } else {
            // check for java property set at startup
            rosettaHome = System.getProperty("rosetta.home", DEFAULT_ROSETTA_HOME);
            props.setProperty("rosetta.home", rosettaHome);
        }

        File rosettaHomeLocation = new File(rosettaHome);
        try {
            // If ROSETTA_HOME doesn't exist, create it.
            if (!rosettaHomeLocation.exists()) {
                logger.info("Creating ROSETTA_HOME directory at " + rosettaHome);

                if (!rosettaHomeLocation.mkdirs()) {
                    logger.error("Unable to create ROSETTA_HOME directory.");
                    contextDestroyed(servletContextEvent); // Can't proceed without ROSETTA_HOME, so die.
                }
            }

            // If file max upload size is not specified, use the default.
            if (props.getProperty("rosetta.maxUpload") == null)
                props.setProperty("rosetta.maxUpload", String.valueOf(DEFAULT_MAX_UPLOAD_SIZE));

            // Create directory where downloads are stashed.
            File downloadDir = createDownloadsDirectory(rosettaHome);
            props.setProperty("rosetta.downloadDir", String.valueOf(downloadDir));

            // Create directory where uploads are stashed.
            File uploadDir = createUploadDirectory(rosettaHome);
            props.setProperty("rosetta.uploadDir", String.valueOf(uploadDir));

            EmbeddedDerbyDbInitManager dbInitManager = new EmbeddedDerbyDbInitManager();
            // Create the database and populate with the relevant prop values if it doesn't already exist.
            dbInitManager.createDatabase(props);

        } catch (SecurityException | IOException | SQLException | NonTransientDataAccessResourceException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logger.error(errors);
            // Can't continue if important directories do not exist or if we have database issues, so die.
            contextDestroyed(servletContextEvent);
        }
    }

    /**
     * Receives notification that the ServletContext is about to be shut down.
     *
     * @param servletContextEvent  Event notifications regarding changes to the ServletContext lifecycle.
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("Application context destruction...");

        // If a application.properties file exists, get the contents.
        Properties props = getProperties();

        // rosettaHome not specified in config file. Use default.
        String rosettaHome = props.getProperty("rosetta.home");
        if (rosettaHome == null) {
            // check for java property set at startup
            rosettaHome = System.getProperty("rosetta.home", DEFAULT_ROSETTA_HOME);
            props.setProperty("rosetta.home", rosettaHome);
        }

        try {
            EmbeddedDerbyDbInitManager dbInitManager = new EmbeddedDerbyDbInitManager();
            dbInitManager.shutdownDatabase(props);
        } catch (SQLException  e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logger.error(errors);
        }
        logger.error("Goodbye.");
    }


    /**
     * Create directory where rosetta stashes files uploaded by users. The name of this
     * directory can be customized by specified the name in the application.properties file
     * (see documentation for more information). If no custom name is provided, the
     * default directory name is used.
     *
     * @param rosettaHome   Rosetta home directory.
     * @return  The uploads directory.
     * @throws IOException  If unable to create the uploads directory.
     */
    public File createUploadDirectory(String rosettaHome) throws IOException{
        File uploadDir = new File(FilenameUtils.concat(rosettaHome, DEFAULT_UPLOAD_DIR));

        // If this directory doesn't exist, create it.
        if (!uploadDir.exists()) {
            logger.info("Creating uploads directory at " + uploadDir.getPath());
            if (!uploadDir.mkdirs()) {
                throw new IOException("Unable to create uploads directory.");
            }
        }
        return uploadDir;
    }


    /**
     * Create directory where rosetta stashes files that will be downloaded by users.
     * The name of this directory can be customized by specified the name in the
     * application.properties file (see documentation for more information). If no custom
     * name is provided, the default directory name is used.
     *
     * @param rosettaHome      Rosetta home directory.
     * @return  The downloads directory.
     * @throws IOException     If unable to create the downloads directory.
     */
    public File createDownloadsDirectory(String rosettaHome) throws IOException{
        File downloadDir = new File(FilenameUtils.concat(rosettaHome, DEFAULT_DOWNLOAD_DIR));

        // If this directory doesn't exist, create it.
        if (!downloadDir.exists()) {
            logger.info("Creating downloads directory at " + downloadDir.getPath());
            if (!downloadDir.mkdirs()) {
                throw new IOException("Unable to create downloads directory.");
            }
        }
        return downloadDir;
    }


    /**
     * Loads and returns all the properties listed in the application.properties configuration file.
     *
     * @return  All of the properties listed the application.properties configuration file.
     */
    public Properties getProperties() {
        Properties props = new Properties();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String rosettaHome = System.getProperty("rosetta.home");
            File configFile;
            if (rosettaHome != null) {
                configFile = new File(rosettaHome + '/' + CONFIG_FILE);
            } else {
                configFile = new File(classLoader.getResource(CONFIG_FILE).getFile());
            }
            System.out.println();

            logger.info("Reading " + configFile + " configuration file...");
            FileInputStream configFileIS = new FileInputStream(configFile);
            props.load(configFileIS); // load the properties file
        } catch (IOException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logger.error("Unable to load properties from configuration file: " + errors);
        }
        return props;
    }
}
