package edu.ucar.unidata.rosetta.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Utils class to access data in the .properties files.
 */
public class RosettaProperties {

    private static Logger logger = Logger.getLogger(RosettaProperties.class);

    /**
     * Loads and returns all the properties listed in the provided configuration file.
     *
     * @param configFile  The configuration file to use.
     * @return  All of the properties listed the configuration file.
     */

    public static Properties getProperties(String configFile) {
        Properties props = new Properties();
        try {
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