package edu.ucar.unidata.rosetta.domain;

import java.util.*;

public class Publisher extends AsciiFile {
    private String pubName;
    private String pubUrl;
    private String incomingDest;

    /**
     * Returns the publisher name.
     *
     * @return  The publisher name.
     */
    public String getPubName() {
        return pubName;
    }

    /**
     * Sets the publisher name.
     *
     * @param pubName  The publisher name.
     */
    public void setPubName(String pubName) {
        this.pubName = pubName;
    }

    /**
     * Returns the publisher url.
     *
     * @return  The publisher url.
     */
    public String getPubUrl() {
        return pubUrl;
    }

    /**
     * Sets the publisher url.
     *
     * @param pubUrl the publisher url.
     */
    public void setPubUrl(String pubUrl) {
        this.pubUrl = pubUrl;
    }

    /**
     * Returns the publish location.
     *
     * @return  The location to publish to.
     */
    public String getIncomingDest() {
        return incomingDest;
    }

    /**
     * Sets the publish location.
     *
     * @param incomingDest  The location to publish to.
     */
    public void setIncomingDest(String incomingDest) {
        this.incomingDest = incomingDest;
    }
}