package edu.ucar.unidata.rosetta.util;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;

import org.apache.log4j.Logger;

/**
 * Utils class to access data in the .properties files.
 */
public class RosettaProperties {

    protected static Logger logger = Logger.getLogger(RosettaProperties.class);
    
    private static String defaultConfigFileName = "defaultRosettaConfig.properties";
    private static String configFileName = "rosettaConfig.properties";
    private static String ROSETTA_HOME = System.getProperty("rosetta.content.root.path", "../content");  // set in $JAVA_OPTS

    /**
     * Gets the designated directory where users can download files from the web application.
     *
     * @return The downloads directory.
     */
    public static String getDownloadDir() {
        Properties props = getRosettaProps();
        String downloadDirProp = props.getProperty("downloadDir");
        File downloadDir = new File(FilenameUtils.concat(getDefaultRosettaHome(), downloadDirProp));
        if (!downloadDir.exists()) {
			logger.info("Creating downloads directory.");
            downloadDir.mkdirs();
        }
        return downloadDir.getAbsolutePath();
    }

    /**
     * Gets the designated directory where users upload data files they want to convert.
     *
     * @return The uploads directory.
     */
    public static String getUploadDir() {
        Properties props = getRosettaProps();
        String uploadDirProp = props.getProperty("uploadDir");
        File uploadDir = new File(FilenameUtils.concat(getDefaultRosettaHome(), uploadDirProp));
        if (!uploadDir.exists()) {
			logger.info("Creating uploads directory.");
            uploadDir.mkdirs();
        }
        return uploadDir.getAbsolutePath();
    }

    /**
     * Gets the maximum upload file size specified in the .properties file.
     *
     * @return The max upload size of a file.
     */
    public static String getMaxUploadSize(ServletContext servletContext) {
        Properties props = new Properties();
		String maxUploadSize = "";
        try {
            props.load(servletContext.getResourceAsStream("/WEB-INF/classes/defaultRosettaConfig.properties"));
            maxUploadSize = props.getProperty("maxUploadSize");
        } catch (IOException e) {
            logger.error("Unable to load .properties file: " + e);
    	}
		return maxUploadSize;
    }

    /**
     * Gets the default ROSETTA_HOME location.
     *
     * @return The default ROSETTA_HOME location.
     */
    private static String getDefaultRosettaHome() {
        return ROSETTA_HOME;
    }

    /**
     * Gets the configuration file for rosetta web application.  
     *
     * @return The configuration file.
     */
    private static String getConfigFile() {
        String configFileLoc = ROSETTA_HOME;
        String config = FilenameUtils.concat(configFileLoc, configFileName);
        File configFile = new File(config);
        if (!configFile.exists()) {
            getDefaultConfigFile();
        }
        return config;
    }

    /**
     * Gets the default configuration file for rosetta web application.  
     *
     * @return The default configuration file.
     */
    private static String getDefaultConfigFile() {
        String config = FilenameUtils.concat(getDefaultRosettaHome(), defaultConfigFileName);
        File configFile = new File(config);
        if (!configFile.exists()) {
            createDefaultConfigFile();
        }
        return config;
    }


    /**
     * Gets the properites listed in the configuration file.
     *
     * @return Properties from the configuration file.
     */
    private static Properties getRosettaProps() {
        String configFile = getConfigFile();

        Properties prop = new Properties();
        try (FileInputStream configFileIS = new FileInputStream(configFile)) {
            // load a properties file
            prop.load(configFileIS);
        } catch (IOException e) {
            logger.error("Unable to load properties from configuration file: " + e);
        }
        return prop;
    }

    /**
     * Creates the configuration files in the default ROSETTA_HOME directory.
     * Will also create ROSETTA_HOME if it hasn't already been created.
     */
    private static void createDefaultConfigFile() {
        String defaultDownloadDir = FilenameUtils.concat(getDefaultRosettaHome(), "downloads");
        String defaultUploadDir = FilenameUtils.concat(getDefaultRosettaHome(), "uploads");

        Properties prop = new Properties();
        // set the properties value
        prop.setProperty("downloadDir", defaultDownloadDir);
        prop.setProperty("uploadDir", defaultUploadDir);

        List<String> configNames = Arrays.asList(defaultConfigFileName, configFileName);

        File defaultRosettaConfigLoc = new File(getDefaultRosettaHome());

        if (!defaultRosettaConfigLoc.exists()) {
            logger.info("Creating default ROSETTA_HOME directory.");
            defaultRosettaConfigLoc.mkdirs();
        }

        for (String configName : configNames) {
            String rosettaConfigFile = FilenameUtils.concat(getDefaultRosettaHome(), configName);
            try (FileOutputStream configFileOS = new FileOutputStream(rosettaConfigFile)) {
                logger.info("Creating configuration file: " + rosettaConfigFile);
                // save properties to project root folder
                prop.store(configFileOS, null);
            } catch (IOException e) {
                logger.error("Unable to store items from configuration file in properties table: " + e);
            }
        }
    }

}