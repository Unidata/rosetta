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
    private String delimiters = null;
    private String done = null;
    private List <String> headerLineNumberList = new ArrayList <String> ();
    private List <String> delimiterList = new ArrayList <String> ();


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

        public String getDone() {
        return done;
    }


    public void setDone(String done) {
        this.done = done;
    }


}
