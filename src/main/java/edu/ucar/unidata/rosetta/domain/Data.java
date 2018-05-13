package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 *
 */
public class Data {

    private String id;
    private String cfType;
    private String community;
    private String platform;
    private CommonsMultipartFile file = null;
    private String fileName;
    private String headerLineNumbers;
    private String delimiter;


    /*
     * Returns the unique id associated with this object.
     *
     * @return  The unique id.
     */
    public String getId() {
        return id;
    }

    /*
     * Sets the unique id associated with this object.
     *
     * @param id  The unique id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /*
     * Returns the cfType.
     *
     * @return  The cfType.
     */
    public String getCfType() {
        return cfType;
    }

    /*
     * Sets the cfType.
     *
     * @param cfType  The cfType.
     */
    public void setCfType(String cfType) {
        this.cfType = cfType;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    /*
     * Returns the platform.
     *
     * @return  The platform.
     */
    public String getPlatform() {
        return platform;
    }

    /*
     * Sets the platform.
     *
     * @param platform  The platform.
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /*
     * Returns the uploaded file in CommonsMultipartFile format.
     *
     * @return  The CommonsMultipartFile file.
     */
    public CommonsMultipartFile getFile() {
        return file;
    }

    /*
     * Sets the uploaded file as a CommonsMultipartFile file.
     * The file is uploaded via an asynchronous AJAX call.
     *
     * @param file  The CommonsMultipartFile file.
     */
    public void setFile(CommonsMultipartFile file) {
        setFileName(file.getOriginalFilename());
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getHeaderLineNumbers() {
        return headerLineNumbers;
    }

    public void setHeaderLineNumbers(String headerLineNumbers) {
        this.headerLineNumbers = headerLineNumbers;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
