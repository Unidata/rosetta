package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author oxelson@ucar.edu
 */
public class RosettaProperties {

    private String rosettaHome;
    private String uploadDir;
    private String downloadDir;
    private int maxUpload;

    public String getRosettaHome() {
        return rosettaHome;
    }

    public void setRosettaHome(String rosettaHome) {
        this.rosettaHome = rosettaHome;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public void setDownloadDir(String downloadDir) {
        this.downloadDir = downloadDir;
    }

    public int getMaxUpload() {
        return maxUpload;
    }

    public void setMaxUpload(int maxUpload) {
        this.maxUpload = maxUpload;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

