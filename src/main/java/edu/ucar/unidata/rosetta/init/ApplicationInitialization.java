package edu.ucar.unidata.rosetta.init;

import edu.ucar.unidata.rosetta.init.repository.DatabaseInitializationManager;
import edu.ucar.unidata.rosetta.util.RosettaProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;


import java.nio.charset.StandardCharsets;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


import org.springframework.dao.NonTransientDataAccessResourceException;

/**
 * Done at application initialization.
 * If first time application is run, it create the database and populates it with
 * the data loaded from the rosetta.properties information.
 */
public class ApplicationInitialization implements ServletContextListener {


    @Resource(name = "dbInitManager")
    private DatabaseInitializationManager dbInitManager;

    protected static Logger logger = Logger.getLogger(ApplicationInitialization.class);

    private static final String ROSETTA_HOME = System.getProperty("rosetta.content.root.path", "../content");  // set in $JAVA_OPTS
    private static final String CONFIG_FILE = "rosetta.properties";
    private static final String DEFAULT_DOWNLOAD_DIR = "downloads";
    private static final String DEFAULT_UPLOAD_DIR = "uploads";
    private static final int DEFAULT_MAX_UPLOAD_SIZE = 52430000;

    /**
     * Receives notification that the web application initialization process is starting.
     *
     * Find the application home ROSETTA_HOME and make sure it exists.  If not, create it.
     * Find out what database was selected for use and create the database if it doesn't exist.
     *
     * @param servletContextEvent  The event class.
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent)  {

        ServletContext servletContext = servletContextEvent.getServletContext();
        logger.info("Application context initialization...");

        File rosettaHomeLocation = new File(ROSETTA_HOME);
        try {

            // If ROSETTA_HOME doesn't exist, create it.
            if (!rosettaHomeLocation.exists()) {
                logger.info("Creating ROSETTA_HOME directory...");

                if (!rosettaHomeLocation.mkdirs()) {
                    logger.error("Unable to create ROSETTA_HOME directory.");
                    contextDestroyed(servletContextEvent); // Can't proceed without ROSETTA_HOME, so die.
                }

                // If a rosetta.properties file exists, get the contents.
                Properties props = RosettaProperties.getProperties(FilenameUtils.concat(ROSETTA_HOME, CONFIG_FILE));
                props.setProperty("rosettaHome", ROSETTA_HOME); // Add ROSETTA_HOME.
                String downloadDirProp = props.getProperty("downloadDir");
                String uploadDirProp = props.getProperty("uploadDir");
                String maxUploadProp = props.getProperty("maxUpload");
                if (maxUploadProp == null)
                    props.setProperty("maxUpload", String.valueOf(DEFAULT_MAX_UPLOAD_SIZE));
                String databaseProp = props.getProperty("database");


                // Create directory where downloads are stashed.
                File downloadDir;
                if (downloadDirProp != null)
                    downloadDir = new File(FilenameUtils.concat(ROSETTA_HOME, downloadDirProp));
                else
                    downloadDir = new File(FilenameUtils.concat(ROSETTA_HOME, DEFAULT_DOWNLOAD_DIR));

                if (!downloadDir.exists()) {
                    logger.info("Creating downloads directory...");
                    if (!downloadDir.mkdirs()) {
                        logger.error("Unable to create downloads directory.");
                        contextDestroyed(servletContextEvent); // Shouldn't proceed without downloads directory, so die.
                    }
                }

                // Create directory where uploads are stashed.
                File uploadDir;
                if (uploadDirProp != null)
                    uploadDir = new File(FilenameUtils.concat(ROSETTA_HOME, uploadDirProp));
                else
                    uploadDir = new File(FilenameUtils.concat(ROSETTA_HOME, DEFAULT_UPLOAD_DIR));

                if (!uploadDir.exists()) {
                    logger.info("Creating uploads directory...");
                    if (!uploadDir.mkdirs()) {
                        logger.error("Unable to create uploads directory.");
                        contextDestroyed(servletContextEvent); // Shouldn't proceed without uploads directory, so die.
                    }
                }

                // Create the database.
                try {
                    dbInitManager.createDatabase(props);
                } catch (SQLException | NonTransientDataAccessResourceException e) {
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
                    logger.error(errors);
                    contextDestroyed(servletContextEvent); // Can't continue if have database issues, so die.
                }

            } else {
                // ROSETTA_HOME exists.
            }
        } catch (SecurityException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logger.error(errors);
            contextDestroyed(servletContextEvent); // Important directories needed by rosetta do not exist, so die.
        }



        // if home doesn't exist, create the whole thing
            // create home
            // if config file exists
                // create upload and download dirs
                // create db and populate it
            // if config file doesn't exist, use defaults
            // create db and populate
        // if home does exist
            // if upload & download dirs dont exist
                // create
            // if


    }

    /**
     * Shutdown the database if it hasn't already been shutdown.
     *
     * @param servletContextEvent  The event class.
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("Application context destruction...");
        if (databaseSelected.equals("derby")) {
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                try {
                    DriverManager.deregisterDriver(driver);
                    logger.info("De-registering jdbc driver.");
                } catch (SQLException e) {
                    logger.error("Error de-registering driver: " + e.getMessage());
                }
            }
        }
        logger.error("Goodbye.");
    }
}
