package edu.ucar.unidata.rosetta.domain;

import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.ResourceManagerImpl;

import java.util.*;

public class Publisher extends AsciiFile {
    private String pubName;
    private String userName;
    private String auth;
    private Map<String, String> publisherInfoMap;

    private ResourceManager rs = new ResourceManagerImpl();

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
        setPublisherInfoMap();
    }

    /**
     * Returns the publisher username.
     *
     * @return  The username for publishing login.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the publisher username.
     *
     * @param userName  The username for publishing login.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the publisher auth.
     *
     * @return  The auth for publishing login.
     */
    public String getAuth() {
        return auth;
    }

    /**
     * Sets the publisher auth.
     *
     * @param auth  The username for publishing login.
     */
    public void setAuth(String auth) {
        this.auth = auth;
    }

    /**
     * Returns a Map containing the variable metadata.
     *
     * @return  The variable metadata in a map.
     */
    public Map<String, String> getPublisherInfoMap() {
        return publisherInfoMap;
    }

    /**
     * Creates a Map containing the variable metadata as specified by the user.
     */
    public void setPublisherInfoMap() {
        publisherInfoMap = new HashMap<String, String>();
        Map resources = rs.loadResources();
        List publishers = (List) resources.get("publishers");
        for(Object pub : publishers) {
            HashMap<String, String> pubMap = (HashMap<String, String>) pub;
            String pubName = pubMap.get("pubName");
            if (pubName.equals(this.getPubName())) {
                publisherInfoMap = pubMap;
            }
        }
    }

}