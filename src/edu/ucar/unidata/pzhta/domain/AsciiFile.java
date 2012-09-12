package edu.ucar.unidata.pzhta.domain;

import org.grlea.log.SimpleLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;

/**
 * Object representing an AsciiFile.  
 *
 * An arbitrary entity representing an ASCII file uploaded to the 
 * local file system by a user. Various attributes of the AsciiFile 
 * object are populated from the user input data collected via 
 * asynchronous AJAX (POST) requests from the client-side.
 * 
 * @see UploadedFile
 */
public class AsciiFile {


    private String cfType = null;
    private String uniqueId = null;
    private String fileName = null;
    private String otherDelimiter = null;
    private String headerLineNumbers = null;
    private List <String> headerLineNumberList = new ArrayList <String> ();
    private String delimiters = null;
    private List <String> delimiterList = new ArrayList <String> ();
    private String variableNames = null;
    private HashMap <String, String> variableNameMap = new HashMap <String, String> ();
    private String variableUnits = null;
    private HashMap <String, String> variableUnitMap = new HashMap <String, String> ();
    private String variableMetadata = null;
    private HashMap <String, HashMap> variableMetadataMap = new HashMap <String, HashMap> ();

    /* global metadata */
    private String title = null;
    private String institution = null;
    private String processor = null;
    private String version = null;
    private String source = null;
    private String description = null;
    private String comment = null;
    private String history = null;
    private String references = null;

    private static final SimpleLogger log = new SimpleLogger(AsciiFile.class);


    /*
     * Returns the CF Type the user selected. 
     * 
     * @return  The CF Type. 
     */
    public String getCfType() {
        return cfType;
    }

    /*
     * Sets the CF Type the user selected. 
     * 
     * @param cfType  The CF Type. 
     */
    public void setCfType(String cfType) {
        this.cfType = cfType;
    }

    /*
     * Returns the local (unique, alphanumeric) directory name
     * where the uploaded ASCII file resides on disk.
     * 
     * @return  The AsciiFile uniqueId (directory name). 
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /*
     * Sets the local directory name where the ASCII file resides on disk. 
     * The local file directory is a unique alphanumeric file name composed 
     * of the uploader's IP address and java.util.Date of when the file was
     * uploaded.  The uniqueId is created by the controller while writing 
     * the file to the local file system.    
     * 
     * @param uniqueId  The AsciiFile uniqueId (directory name). 
     * @see   FileUploadController 
     */
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /*
     * Returns the name of the uploaded ASCII file.
     * 
     * @return  The AsciiFile fileName. 
     */
    public String getFileName() {
        return fileName;
    }

    /*
     * Set the name of the ASCII file. 
     * 
     * @param fileName  The AsciiFile fileName. 
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /*
     * Returns the other delimiter specified by the user.
     * 
     * @return  The other delimiter. 
     */
    public String getOtherDelimiter() {
        return otherDelimiter;
    }

    /*
     * Set the other delimiter specified by the user. 
     * 
     * @param otherDelimiter  The other delimiter. 
     */
    public void setOtherDelimiter(String otherDelimiter) {
        this.otherDelimiter = otherDelimiter;
        if (!otherDelimiter.equals("null")) {
            this.delimiterList.add(otherDelimiter);
        }
    }

    /*
     * Returns the delimiters specified by the user in String format.
     * 
     * @return  The delimiters in String format. 
     */
    public String getDelimiters() {
        return delimiters;
    }

    /*
     * Set the delimiters specified by the user in String Format. 
     * 
     * @param delimiters  The delimiters in String format. 
     */
    public void setDelimiters(String delimiters) {
        this.delimiters = delimiters;
        setDelimiterList(delimiters); 
    }



    /*
     * Returns the header line numbers in String format. 
     * 
     * @return  The header line numbers in String Formatt. 
     */
    public String getHeaderLineNumbers() {
        return headerLineNumbers;
    }

    /*
     * Sets the header line numbers as specified by the user in String format.
     * 
     * @param headerLineNumbers  The String of header line numbers. 
     */
    public void setHeaderLineNumbers(String headerLineNumbers) {
        this.headerLineNumbers = headerLineNumbers;
        setHeaderLineNumberList(headerLineNumbers);
    }


    /*
     * Returns a List containing delimiters. 
     * 
     * @return  The delimiters in a List. 
     */
    public List <String> getDelimiterList() {
        return delimiterList;
    }

    /*
     * Creates a List containing the delimiters as specified by the user.
     * 
     * @param delimiters  The String of delimiters. 
     */
    public void setDelimiterList(String delimiters) {
        List <String> unconvertedDelimiterList = Arrays.asList(delimiters.split(","));
        this.delimiterList = convertDelimiters(unconvertedDelimiterList);
    }


    /*
     * Returns a List containing the header line numbers. 
     * 
     * @return  The header line numbers in a list. 
     */
    public List <String> getHeaderLineNumberList() {
        return headerLineNumberList;
    }

    /*
     * Creates a List containing the header line numbers as specified by the user.
     * 
     * @param headerLineNumbers  The String of header line numbers. 
     */
    public void setHeaderLineNumberList(String headerLineNumbers) {
        this.headerLineNumberList = Arrays.asList(headerLineNumbers.split(","));
    }

    
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



    /*
     * Returns the global metadata value of title.
     * 
     * @return  The title (global metadata). 
     */
    public String getTitle() {
        return title;
    }

    /*
     * Set the global metadata value of title.
     * 
     * @param title  The title (global metadata). 
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /*
     * Returns the global metadata value of institution.
     * 
     * @return  The institution (global metadata). 
     */
    public String getInstitution() {
        return institution;
    }

    /*
     * Set the global metadata value of institution.
     * 
     * @param institution  The institution (global metadata). 
     */
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    /*
     * Returns the global metadata value of processor.
     * 
     * @return  The processor (global metadata). 
     */
    public String getProcessor() {
        return processor;
    }

    /*
     * Set the global metadata value of processor.
     * 
     * @param processor  The processor (global metadata). 
     */
    public void setProcessor(String processor) {
        this.processor = processor;
    }

    /*
     * Returns the global metadata value of version.
     * 
     * @return  The version (global metadata). 
     */
    public String getVersion() {
        return version;
    }

    /*
     * Set the global metadata value of version.
     * 
     * @param version  The version (global metadata). 
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /*
     * Returns the global metadata value of source.
     * 
     * @return  The source (global metadata). 
     */
    public String getSource() {
        return source;
    }

    /*
     * Set the global metadata value of source.
     * 
     * @param source  The source (global metadata). 
     */
    public void setSource(String source) {
        this.source = source;
    }


    /*
     * Returns the global metadata value of description.
     * 
     * @return  The description (global metadata). 
     */
    public String getDescription() {
        return description;
    }

    /*
     * Set the global metadata value of description.
     * 
     * @param description  The description (global metadata). 
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /*
     * Returns the global metadata value of comment.
     * 
     * @return  The comment (global metadata). 
     */
    public String getComment() {
        return comment;
    }

    /*
     * Set the global metadata value of comment.
     * 
     * @param comment  The comment (global metadata). 
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /*
     * Returns the global metadata value of history.
     * 
     * @return  The history (global metadata). 
     */
    public String getHistory() {
        return history;
    }

    /*
     * Set the global metadata value of history.
     * 
     * @param history  The history (global metadata). 
     */
    public void setHistory(String history) {
        this.history = history;
    }


    /*
     * Returns the global metadata value of references.
     * 
     * @return  The references (global metadata). 
     */
    public String getReferences() {
        return references;
    }

    /*
     * Set the global metadata value of references.
     * 
     * @param references  The references (global metadata). 
     */
    public void setReferences(String references) {
        this.references = references;
    }

    /*
     * Returns the variable names in String format. 
     * 
     * @return  The variable names in String Formatt. 
     */
    public String getVariableNames() {
        return variableNames;
    }

    /*
     * Sets the variable names as specified by the user in String format.
     * 
     * @param variableNames  The String of variable names. 
     */
    public void setVariableNames(String variableNames) {
        this.variableNames = variableNames;
        setVariableNameMap(variableNames);        
    }

    /*
     * Returns a Map containing the variable names. 
     * 
     * @return  The variable names in a map. 
     */
    public HashMap <String, String> getVariableNameMap() {
        return variableNameMap;
    }

    /*
     * Creates a Map containing the variable units as specified by the user.
     * 
     * @param variableNames  The String of variable names. 
     */
    public void setVariableNameMap(String variableNames) {
        List <String> pairs = Arrays.asList(variableNames.split(","));
        Iterator<String> pairsIterator = pairs.iterator();
        while (pairsIterator.hasNext()) {  
            String pairString = pairsIterator.next();
            String[] items =  pairString.split(":");
            this.variableNameMap.put(items[0], items[1]);
        } 
    }


    /*
     * Returns the variable units in String format. 
     * 
     * @return  The variable units in String Formatt. 
     */
    public String getVariableUnits() {
        return variableUnits;
    }

    /*
     * Sets the variable units as specified by the user in String format.
     * 
     * @param variableUnits  The String of variable units. 
     */
    public void setVariableUnits(String variableUnits) {
        this.variableUnits = variableUnits;
        setVariableUnitMap(variableUnits);
    }

    /*
     * Returns a Map containing the variable units. 
     * 
     * @return  The variable units in a map. 
     */
    public HashMap <String, String> getVariableUnitMap() {
        return variableUnitMap;
    }

    /*
     * Creates a Map containing the variable units as specified by the user.
     * 
     * @param variableUnits  The String of variable units. 
     */
    public void setVariableUnitMap(String variableUnits) {
        List <String> pairs = Arrays.asList(variableUnits.split(","));
        Iterator<String> pairsIterator = pairs.iterator();
        while (pairsIterator.hasNext()) {  
            String pairString = pairsIterator.next();
            String[] items =  pairString.split(":");
            this.variableUnitMap.put(items[0].replaceAll("Unit", ""), items[1]);
        } 
    }


    /*
     * Returns the variable metadata in String format. 
     * 
     * @return  The variable metadata in String Formatt. 
     */
    public String getVariableMetadata() {
        return variableMetadata;
    }

    /*
     * Sets the variable metadata as specified by the user in String format.
     * 
     * @param variableMetadata  The String of variable metadata. 
     */
    public void setVariableMetadata(String variableMetadata) {
        this.variableMetadata = variableMetadata;
        setVariableMetadataMap(variableMetadata);
    }

    /*
     * Returns a Map containing the variable metadata. 
     * 
     * @return  The variable metadata in a map. 
     */
    public HashMap <String, HashMap> getVariableMetadataMap() {
        return variableMetadataMap;
    }

    /*
     * Creates a Map containing the variable metadata as specified by the user.
     * 
     * @param variableMetadata  The String of variable metadata. 
     */
    public void setVariableMetadataMap(String variableMetadata) {
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

  
}
