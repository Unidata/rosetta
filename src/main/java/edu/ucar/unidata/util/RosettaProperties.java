package edu.ucar.unidata.util;


import java.io.*;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

public class RosettaProperties {

    private static String defaultConfigFileName = "defaultRosettaConfig.properties";
    private static String ROSETTA_HOME_ENVAR = "ROSETTA_HOME";
    private static String SPRING_RESOURCES = "WEB-INF/classes/";

    public static void create() {
        String defaultRosettaConfigPath = getDefaultConfigFileLoc();
        Properties prop = new Properties();
        String defaultDownloadDir = FilenameUtils.concat(getDefaultRosettaHome(), "downloads");

        File defaultRosettaConfigLoc = new File(defaultRosettaConfigPath.replace(defaultConfigFileName,""));
        if (!defaultRosettaConfigLoc.exists()){
            defaultRosettaConfigLoc.mkdirs();
        }

        try {
            //set the properties value
            prop.setProperty("downloadDir", defaultDownloadDir);
            prop.setProperty("downloadDirRelToWebap", "true");
            //save properties to project root folder
            prop.store(new FileOutputStream(defaultRosettaConfigPath), null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String getDefaultRosettaHome() {
        String catalinaBase = System.getProperty("catalina.base");
        String rosettaHomeDir = FilenameUtils.concat(catalinaBase, "webapps/rosetta/");
        File f = new File(rosettaHomeDir);
        if (!f.exists()) {
            rosettaHomeDir = FilenameUtils.concat(catalinaBase, "webapps/ROOT/");
        }
        return rosettaHomeDir;
    }

    private static String getDefaultConfigFileLoc() {
        String configFileLoc = FilenameUtils.concat(getDefaultRosettaHome(), SPRING_RESOURCES);
        configFileLoc = FilenameUtils.concat(configFileLoc, defaultConfigFileName);
        return configFileLoc;
    }

    private static String getDefaultDownloadDir() {
        String downloadDir = getRosettaProps().getProperty("downloadDir");
        String defaultDownloadDir = FilenameUtils.concat(getDefaultRosettaHome(), downloadDir);
        return defaultDownloadDir;
    }

    private static String getConfigFileLoc() {
        String configFileLoc = System.getenv(ROSETTA_HOME_ENVAR);

        if (configFileLoc == null) {
            configFileLoc = getDefaultConfigFileLoc();
        } else {
            configFileLoc = FilenameUtils.concat(configFileLoc, defaultConfigFileName);
        }
        return configFileLoc;
    }

    public static String getDownloadDir(){
        Properties props = getRosettaProps();
        String downloadDir = props.getProperty("downloadDir");
        String isDownloadDirInWebapp = props.getProperty("downloadDirRelToWebap", "false");
        if (isDownloadDirInWebapp.equals("true")) {
            downloadDir = FilenameUtils.concat(getDefaultRosettaHome(), downloadDir);
        }
        return downloadDir;
    }

    public static Properties getRosettaProps() {
        String configFileLoc = getConfigFileLoc();

        Properties prop = new Properties();
        try {
            //load a properties file
            prop.load(new FileInputStream(configFileLoc));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return prop;
        }
    }
    public static void main( String[] args ) {
        System.out.println(getConfigFileLoc());
        create();
        Properties props = getRosettaProps();
        System.out.println(props.getProperty("downloadDir"));
    }

}