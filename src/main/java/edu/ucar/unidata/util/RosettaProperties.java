package edu.ucar.unidata.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class RosettaProperties {

    private static String defaultConfigFileName = "rosettaConfig.properties";
    public static String ROSETTA_HOME_ENVAR = "ROSETTA_HOME";

    public static void create() {
        String defaultRosettaConfigPath = getDefaultConfigFileLoc();
        Properties prop = new Properties();
        // downloadDir in catalina.base context, not file system!
        String defaultDownloadDir = "/webapps/rosetta/download";
        try {
            //set the properties value
            prop.setProperty("downloadDir", defaultDownloadDir);

            //save properties to project root folder
            prop.store(new FileOutputStream(defaultRosettaConfigPath), null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String getDefaultRosettaHome() {
        String homeDir = System.getProperty("user.home");
        String unidataDir = "/.unidata/rosetta/";
        return homeDir + unidataDir;
    }

    private static String getDefaultConfigFileLoc() {
        String configFileLoc = getDefaultRosettaHome() + defaultConfigFileName;
        return configFileLoc;
    }

    private static String getConfigFileLoc() {
        String configFileLoc = System.getenv(ROSETTA_HOME_ENVAR);

        if (configFileLoc == null) {
            configFileLoc = getDefaultConfigFileLoc();
        } else {
            File file1 = new File(configFileLoc);
            File file2 = new File(file1, defaultConfigFileName);
            configFileLoc =  file2.getPath();
        }
        return configFileLoc;
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