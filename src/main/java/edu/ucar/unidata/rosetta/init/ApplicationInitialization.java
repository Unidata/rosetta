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

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import org.springframework.dao.NonTransientDataAccessResourceException;

/**
 * Done at application initialization.
 * If first time application is run, it create the database and populates it with
 * the data loaded from the rosetta.properties information.
 */
public class ApplicationInitialization implements ServletContextListener {

    // Handles creation of the rosetta database.
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
     * Information from the rosetta.properties file and JAVA_OPTS is read and used to create
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

        // If a rosetta.properties file exists, get the contents.
        Properties props = RosettaProperties.getProperties(FilenameUtils.concat(ROSETTA_HOME, CONFIG_FILE));

        File rosettaHomeLocation = new File(ROSETTA_HOME);
        try {

            // If ROSETTA_HOME doesn't exist, create it.
            if (!rosettaHomeLocation.exists()) {
                logger.info("Creating ROSETTA_HOME directory...");

                if (!rosettaHomeLocation.mkdirs()) {
                    logger.error("Unable to create ROSETTA_HOME directory.");
                    contextDestroyed(servletContextEvent); // Can't proceed without ROSETTA_HOME, so die.
                }

                // Add ROSETTA_HOME to props.
                props.setProperty("rosettaHome", ROSETTA_HOME);

                // If file max upload size is not specified, use the default.
                if (props.getProperty("maxUpload") == null)
                    props.setProperty("maxUpload", String.valueOf(DEFAULT_MAX_UPLOAD_SIZE));

                // Create directory where downloads are stashed.
                createDownloadsDirectory(props.getProperty("downloadDir"));

                // Create directory where uploads are stashed.
                createUploadDirectory(props.getProperty("uploadDir"));

                // Create the database and populate with the relevant prop values.
                dbInitManager.createDatabase(props);


            } else {  // ROSETTA_HOME already exists.

                // Create directory where downloads are stashed if it doesn't already exist.
                createDownloadsDirectory(props.getProperty("downloadDir"));

                // Create directory where uploads are stashed if it doesn't already exist.
                createUploadDirectory(props.getProperty("uploadDir"));

                // Create the database and populate with the relevant prop values if it doesn't already exist.
                dbInitManager.createDatabase(props);

            }
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

        // If a rosetta.properties file exists, get the contents.
        Properties props = RosettaProperties.getProperties(FilenameUtils.concat(ROSETTA_HOME, CONFIG_FILE));
        // Add ROSETTA_HOME to props.
        props.setProperty("rosettaHome", ROSETTA_HOME);

        try {
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
     * directory can be customized by specified the name in the rosetta.properties file
     * (see documentation for more information). If no custom name is provided, the
     * default directory name is used.
     *
     * @param uploadDirProp The user-specified uploads directory name.
     * @throws IOException  If unable to create the uploads directory.
     */
    public void createUploadDirectory(String uploadDirProp) throws IOException{
        File uploadDir;
        if (uploadDirProp != null)
            uploadDir = new File(FilenameUtils.concat(ROSETTA_HOME, uploadDirProp));
        else
            uploadDir = new File(FilenameUtils.concat(ROSETTA_HOME, DEFAULT_UPLOAD_DIR));

        // If this directory doesn't exist, create it.
        if (!uploadDir.exists()) {
            logger.info("Creating uploads directory...");
            if (!uploadDir.mkdirs()) {
                throw new IOException("Unable to create uploads directory.");
            }
        }
    }


    /**
     * Create directory where rosetta stashes files that will be downloaded by users.
     * The name of this directory can be customized by specified the name in the
     * rosetta.properties file (see documentation for more information). If no custom
     * name is provided, the default directory name is used.
     *
     * @param downloadDirProp  The user-specified downloads directory name.
     * @throws IOException     If unable to create the downloads directory.
     */
    public void createDownloadsDirectory(String downloadDirProp) throws IOException{
        File downloadDir;
        if (downloadDirProp != null)
            downloadDir = new File(FilenameUtils.concat(ROSETTA_HOME, downloadDirProp));
        else
            downloadDir = new File(FilenameUtils.concat(ROSETTA_HOME, DEFAULT_DOWNLOAD_DIR));

        // If this directory doesn't exist, create it.
        if (!downloadDir.exists()) {
            logger.info("Creating downloads directory...");
            if (!downloadDir.mkdirs()) {
                throw new IOException("Unable to create downloads directory.");
            }
        }
    }



}
