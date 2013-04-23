package edu.ucar.unidata.pzhta.service;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

/**
 * Bean containing Server Information, including info from the MANIFEST file.
 */
public class ServerInfo implements ServletContextAware {
    private static String version = null;
    private static String buildDate = null;
    private static ServerInfo instance = null;
    private static ServletContext servletContext = null;

    private ServerInfo() {}

    public static String getVersion(){
        if(instance == null){
            try {
                instance = new ServerInfo();
                String name = "/META-INF/MANIFEST.MF";
                Properties props = new Properties();
                props.load(servletContext.getResourceAsStream(name));
                instance.version = (String) props.get("Implementation-Version");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            instance = null;
            getVersion();
        }
        return instance.version;
    }

    public static String getBuildDate(){
        if(instance == null){
            try {
                instance = new ServerInfo();
                String name = "/META-INF/MANIFEST.MF";
                Properties props = new Properties();
                props.load(servletContext.getResourceAsStream(name));
                instance.buildDate = (String) props.get("Built-On");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            instance = null;
            getBuildDate();
        }
        return instance.buildDate;
    }


    public void setServletContext(ServletContext servletContext){
        this.servletContext = servletContext;
    }
}