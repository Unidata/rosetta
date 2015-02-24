package edu.ucar.unidata.rosetta.util;


import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

public class RosettaProperties {

    private static String defaultConfigFileName = "defaultRosettaConfig.properties";
    private static String configFileName = "rosettaConfig.properties";
    private static String ROSETTA_HOME = System.getProperty("rosetta.content.root.path", "../content");

    public static String getDownloadDir(){
        Properties props = getRosettaProps();
        String downloadDirProp = props.getProperty("downloadDir");
        File downloadDir = new File(FilenameUtils.concat(getDefaultRosettaHome(), downloadDirProp));
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        
        return downloadDir.getAbsolutePath();
    }

    public static String getUploadDir(){
        Properties props = getRosettaProps();
        String uploadDirProp = props.getProperty("uploadDir");
        File uploadDir = new File(FilenameUtils.concat(getDefaultRosettaHome(), uploadDirProp));
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }       
        return uploadDir.getAbsolutePath();
    }
    
    // only private methods below here
    
    private static String getDefaultRosettaHome() {
        return ROSETTA_HOME;
    }

    private static String getConfigFile() {
        String configFileLoc = ROSETTA_HOME;
        String config = FilenameUtils.concat(configFileLoc, configFileName);
        File configFile = new File(config);
        if (!configFile.exists()) {
            getDefaultConfigFile();
        }
        return config;
    }
    
    private static String getDefaultConfigFile() {
        String config = FilenameUtils.concat(getDefaultRosettaHome(), defaultConfigFileName);
        File configFile = new File(config);
        if (!configFile.exists()) {
           System.out.println("creating defautl config");
           createDefaultConfigFile(); 
        }
        return config;
    }

    //private static String getDefaultDownloadDir() {
    //    String downloadDir = getRosettaProps().getProperty("downloadDir");
    //    String defaultDownloadDir = FilenameUtils.concat(getDefaultRosettaHome(), downloadDir);
    //    return defaultDownloadDir;
    //}
    
    private static Properties getRosettaProps() {
        String configFile = getConfigFile();

        Properties prop = new Properties();
        try {
            //load a properties file
            prop.load(new FileInputStream(configFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;

    }
    
    private static void createDefaultConfigFile() {
        String defaultDownloadDir = FilenameUtils.concat(getDefaultRosettaHome(), "downloads");
        String defaultUploadDir = FilenameUtils.concat(getDefaultRosettaHome(), "uploads");
        
        Properties prop = new Properties();
        //set the properties value
        prop.setProperty("downloadDir", defaultDownloadDir);
        prop.setProperty("uploadDir", defaultUploadDir);
        
        List<String> configNames = Arrays.asList(defaultConfigFileName, configFileName);

        File defaultRosettaConfigLoc = new File(getDefaultRosettaHome());

        if (!defaultRosettaConfigLoc.exists()){
            defaultRosettaConfigLoc.mkdirs();
        }
        
        for (String configName : configNames) {
            
            String defaultRosettaConfigFile = FilenameUtils.concat(getDefaultRosettaHome(), configName);
    
            try {
                 //save properties to project root folder
                prop.store(new FileOutputStream(defaultRosettaConfigFile), null);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
 

}