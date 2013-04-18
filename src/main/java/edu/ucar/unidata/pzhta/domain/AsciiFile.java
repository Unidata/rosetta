package edu.ucar.unidata.pzhta.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Object representing an AsciiFile.  
 *
 * An arbitrary entity representing an ASCII file uploaded to the 
 * local file system by a user. Various attributes of the AsciiFile 
 * object are populated from the user input data collected via 
 * asynchronous AJAX (POST) requests from the client-side. (AKA
 * SPRING Magic!)
 * 
 * @see UploadedFile
 */
public class AsciiFile {

    private String cfType = null;
    private String uniqueId = null;
    private String fileName = null;
    private String delimiters = null;
    private List<String> delimiterList = new ArrayList<String>();
    private String otherDelimiter = null;
    private String headerLineNumbers = null;
    private List<String> headerLineList = new ArrayList<String>();
    private String platformMetadata = null;
    private Map<String, String> platformMetadataMap = new HashMap<String, String>();
    private String generalMetadata = null;
    private Map<String, String> generalMetadataMap = new HashMap<String, String>();
    private String variableNames = null;
    private Map<String, String> variableNameMap = new HashMap<String, String>();
    private String variableMetadata = null;
    private Map<String, HashMap> variableMetadataMap = new HashMap<String, HashMap>();
    private String jsonStrSessionStorage = null;

    /**
     * Returns the CF Type the user selected. 
     * 
     * @return  The CF Type. 
     */
    public String getCfType() {
        return cfType;
    }

    /**
     * Sets the CF Type the user selected. 
     * 
     * @param cfType  The CF Type. 
     */
    public void setCfType(String cfType) {
        this.cfType = cfType;
    }

    /**
     * Returns the "stringified" json object that holds all of the
     * sessionStorage information.
     *
     * @return  The sessionStorage "stringified" JSON object
     */
    public String getJsonStrSessionStorage() {
        return jsonStrSessionStorage;
    }

    /**
     * Sets the "stringified" json object that holds all of the
     * sessionStorage information.
     *
     * @param jsonStrSessionStorage
     */

    public void setJsonStrSessionStorage(String jsonStrSessionStorage) {
        this.jsonStrSessionStorage = jsonStrSessionStorage;
    }

    /**
     * Returns the local (unique, alphanumeric) directory name
     * where the uploaded ASCII file resides on disk.
     * 
     * @return  The AsciiFile uniqueId (directory name). 
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Sets the local directory name where the ASCII file resides on disk. 
     * The local file directory is a unique alphanumeric file name composed 
     * of the uploader's IP address and java.util.Date of when the file was
     * uploaded.  The uniqueId is created by the controller while writing 
     * the file to the local file system.    
     * 
     * @param uniqueId  The AsciiFile uniqueId (directory name).
     */
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /**
     * Returns the name of the uploaded ASCII file.
     * 
     * @return  The AsciiFile fileName. 
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set the name of the ASCII file. 
     * 
     * @param fileName  The AsciiFile fileName. 
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the delimiters specified by the user in String format.
     * 
     * @return  The delimiters in String format. 
     */
    public String getDelimiters() {
        return delimiters;
    }

    /**
     * Set the delimiters specified by the user in String Format. 
     * 
     * @param delimiters  The delimiters in String format. 
     */
    public void setDelimiters(String delimiters) {
        this.delimiters = delimiters;
        setDelimiterList(delimiters); 
    }

    /**
     * Returns a List containing delimiters. 
     * 
     * @return  The delimiters in a List. 
     */
    public List<String> getDelimiterList() {
        return delimiterList;
    }

    /**
     * Creates a List containing the delimiters as specified by the user.
     * 
     * @param delimiters  The String of delimiters. 
     */
    public void setDelimiterList(String delimiters) {
        List <String> unconvertedDelimiterList = Arrays.asList(delimiters.split(","));
        this.delimiterList = convertDelimiters(unconvertedDelimiterList);
    }

    /**
     * Returns the other delimiter specified by the user.
     * 
     * @return  The other delimiter. 
     */
    public String getOtherDelimiter() {
        return otherDelimiter;
    }

    /**
     * Set the other delimiter specified by the user. 
     * 
     * @param otherDelimiter  The other delimiter. 
     */
    public void setOtherDelimiter(String otherDelimiter) {
        this.otherDelimiter = otherDelimiter;
        if (!otherDelimiter.equals("null")) {
            if (!otherDelimiter.equals("")) {
                this.delimiterList.add(otherDelimiter);
            }
        }
    }

    /**
     * Returns the header line numbers in String format. 
     * 
     * @return  The header line numbers in String Formatt. 
     */
    public String getHeaderLineNumbers() {
        return headerLineNumbers;
    }

    /**
     * Sets the header line numbers as specified by the user in String format.
     * 
     * @param headerLineNumbers  The String of header line numbers. 
     */
    public void setHeaderLineNumbers(String headerLineNumbers) {
        this.headerLineNumbers = headerLineNumbers;
        setHeaderLineList(headerLineNumbers);
    }

    /**
     * Returns a List containing the header line numbers. 
     * 
     * @return  The header line numbers in a list. 
     */
    public List<String> getHeaderLineList() {
        return headerLineList;
    }

    /**
     * Creates a List containing the header line numbers as specified by the user.
     * 
     * @param headerLineNumbers  The String of header line numbers. 
     */
    public void setHeaderLineList(String headerLineNumbers) {
        this.headerLineList = Arrays.asList(headerLineNumbers.split(","));
    }

    /**
     * Returns the platform metadata in String format. 
     * 
     * @return  The platform metadata in String Formatt. 
     */
    public String getPlatformMetadata() {
        return platformMetadata;
    }

    /**
     * Sets the platform metadata as specified by the user in String format.
     * 
     * @param platformMetadata  The String of platform metadata. 
     */
    public void setPlatformMetadata(String platformMetadata) {
        this.platformMetadata = platformMetadata;
        setPlatformMetadataMap();
    }

    /**
     * Returns a Map containing the platform metadata. 
     * 
     * @return  The platform metadata in a map. 
     */
    public Map<String, String> getPlatformMetadataMap() {
        return platformMetadataMap;
    }

    /**
     * Creates a Map containing the platform metadata as specified by the user.
     */
    public void setPlatformMetadataMap() {
        List <String> pairs = Arrays.asList(platformMetadata.split(","));
        Iterator<String> pairsIterator = pairs.iterator();
        while (pairsIterator.hasNext()) {  
            String pairString = pairsIterator.next();
            String[] items =  pairString.split(":");
            this.platformMetadataMap.put(items[0], items[1]);
        }
    }
 
    /**
     * Returns the general metadata in String format. 
     * 
     * @return  The general metadata in String Format. 
     */
    public String getGeneralMetadata() {
        return generalMetadata;
    }

    /**
     * Sets the general metadata as specified by the user in String format.
     * 
     * @param generalMetadata  The String of general metadata. 
     */
    public void setGeneralMetadata(String generalMetadata) {
        this.generalMetadata = generalMetadata;
        setGeneralMetadataMap(generalMetadata);
    }

    /**
     * Returns a Map containing the general metadata. 
     * 
     * @return  The general metadata in a map. 
     */
    public Map<String, String> getGeneralMetadataMap() {
        return generalMetadataMap;
    }

    /**
     * Creates a Map containing the general metadata as specified by the user.
     * 
     * @param generalMetadata  The String of general metadata. 
     */
    public void setGeneralMetadataMap(String generalMetadata) {
        List <String> pairs = Arrays.asList(generalMetadata.split(","));
        Iterator<String> pairsIterator = pairs.iterator();
        while (pairsIterator.hasNext()) {  
            String pairString = pairsIterator.next();
            String[] items =  pairString.split(":");
            this.generalMetadataMap.put(items[0], items[1]);
        } 
    }

    /**
     * Returns the variable names in String format. 
     * 
     * @return  The variable names in String Formatt. 
     */
    public String getVariableNames() {
        return variableNames;
    }

    /**
     * Sets the variable names as specified by the user in String format.
     * 
     * @param variableNames  The String of variable names. 
     */
    public void setVariableNames(String variableNames) {
        this.variableNames = variableNames;
        setVariableNameMap();
    }

    /**
     * Returns a Map containing the variable names. 
     * 
     * @return  The variable names in a map. 
     */
    public Map<String, String> getVariableNameMap() {
        return variableNameMap;
    }

    /**
     * Creates a Map containing the variable units as specified by the user.
     */
    public void setVariableNameMap() {
        List <String> pairs = Arrays.asList(variableNames.split(","));
        Iterator<String> pairsIterator = pairs.iterator();
        while (pairsIterator.hasNext()) {  
            String pairString = pairsIterator.next();
            String[] items =  pairString.split(":");
            this.variableNameMap.put(items[0], items[1]);
        } 
    }

    /**
     * Returns the variable metadata in String format. 
     * 
     * @return  The variable metadata in String Formatt. 
     */
    public String getVariableMetadata() {
        return variableMetadata;
    }

    /**
     * Sets the variable metadata as specified by the user in String format.
     * 
     * @param variableMetadata  The String of variable metadata. 
     */
    public void setVariableMetadata(String variableMetadata) {
        this.variableMetadata = variableMetadata;
        setVariableMetadataMap();
    }

    /**
     * Returns a Map containing the variable metadata. 
     * 
     * @return  The variable metadata in a map. 
     */
    public Map<String, HashMap> getVariableMetadataMap() {
        return variableMetadataMap;
    }

    /**
     * Creates a Map containing the variable metadata as specified by the user.
     */
    public void setVariableMetadataMap() {
        List <String> pairs = Arrays.asList(variableMetadata.split(","));
        Iterator<String> pairsIterator = pairs.iterator();
        while (pairsIterator.hasNext()) {  
            HashMap <String, String> metadataMapping = new HashMap<String, String> (); 
            String pairString = pairsIterator.next();
            String[] items =  pairString.split("=");
            if (!items[1].equals("Do Not Use")) {
                List <String> values =  Arrays.asList(items[1].split("\\+"));
                Iterator<String> valuesIterator = values.iterator();
                while (valuesIterator.hasNext()) {
                    String data = valuesIterator.next();
                    String[] metadata = data.split(":");
                    metadataMapping.put(metadata[0], metadata[1]);
                }
            }
            this.variableMetadataMap.put(items[0], metadataMapping);
        } 
    }

    /**
     * Creates a List containing the delimiter strings.
     * 
     * @param delimiterList  The list of delimiter strings.
     */
    public List <String> convertDelimiters(List <String> delimiterList) {
        HashMap <String, String> delimiterMapping = new HashMap<String, String> (); 
        delimiterMapping.put("Tab", "\t");
        delimiterMapping.put("Comma", ",");
        delimiterMapping.put("Space", " ");
        delimiterMapping.put("Semicolon", ";");
        delimiterMapping.put("Double Quote", "\"");
        delimiterMapping.put("Single Quote", "\'");
 
        Iterator<String> iterator = delimiterList.iterator();
        List <String> convertedDelimiterList = new ArrayList<String>();
	    while (iterator.hasNext()) {
            String delimiter = iterator.next();
            if (!delimiter.equals("Other")) {
                convertedDelimiterList.add(delimiterMapping.get(delimiter)); 
            }
	    } 
        return convertedDelimiterList;
    }
}
