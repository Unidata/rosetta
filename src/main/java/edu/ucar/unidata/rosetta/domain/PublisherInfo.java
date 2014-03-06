package edu.ucar.unidata.rosetta.domain;

import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.ResourceManagerImpl;

import java.util.*;

public class PublisherInfo {
    private String pubName;
    private String userName;
    private String auth;
    private String pubDest;
    private String uniqueId;
    private String fileName;
    private String generalMetadata;

    private Map<String, String> generalMetadataMap = new HashMap<String, String>();
    private Map<String, String> publisherInfoMap;

    private ResourceManager rs = new ResourceManagerImpl();

    public String getGeneralMetadata() {
        return generalMetadata;
    }

    public void setGeneralMetadata(String generalMetadata) {
        this.generalMetadata = generalMetadata;
        setGeneralMetadataMap(generalMetadata);
    }

    public Map<String, String> getGeneralMetadataMap() {
        return generalMetadataMap;
    }

    public void setGeneralMetadataMap(String generalMetadata) {
        List <String> pairs = Arrays.asList(generalMetadata.split(","));
        Iterator<String> pairsIterator = pairs.iterator();
        while (pairsIterator.hasNext()) {
            String pairString = pairsIterator.next();
            String[] items =  pairString.split(":");
            this.generalMetadataMap.put(items[0], items[1]);
        }
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPubDest() {
        return pubDest;
    }

    public void setPubDest(String pubDest) {
        this.pubDest = pubDest;
    }


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
        if (pubDest != null) {
            publisherInfoMap.put("pubDest", pubDest);
        }
    }

}