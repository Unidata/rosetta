/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service;

import java.io.IOException;
import java.util.Properties;
import javax.servlet.ServletContext;
import org.springframework.web.context.ServletContextAware;

public class ServerInfoBean implements ServletContextAware {

  private static ServerInfo serverInfo;
  private ServletContext servletContext = null;

  public ServerInfoBean() {
  }

  public static String getVersion() {
    return serverInfo.getVersion();
  }

  public static String getBuildDate() {
    return serverInfo.getBuildDate();
  }

  public void init() {
    serverInfo = new ServerInfo(this.servletContext);
  }

  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  /**
   * Bean containing Server Information, including info from the MANIFEST file.
   */
  private static class ServerInfo {

    private String version = "local development";
    private String buildDate = "development";
    private ServletContext servletContext = null;

    private ServerInfo(ServletContext servletContext) {
      setServletContext(servletContext);
      this.init();
    }

    private void init() {
      String name = "/META-INF/MANIFEST.MF";

      Properties props = new Properties();
      try {
        props.load(servletContext.getResourceAsStream(name));
        this.version = (String) props.get("Implementation-Version");
        this.buildDate = (String) props.get("Built-On");
        System.out
            .println("Initilizing Rosetta version " + this.version + " built on " + this.buildDate);
      } catch (IOException e) {
        // not running from war file, so cannot get from /META-INF
        e.printStackTrace();
      }
    }

    private void setServletContext(ServletContext servletContext) {
      this.servletContext = servletContext;
    }

    public String getVersion() {
      return this.version;
    }

    public String getBuildDate() {
      return this.buildDate;
    }

  }
}