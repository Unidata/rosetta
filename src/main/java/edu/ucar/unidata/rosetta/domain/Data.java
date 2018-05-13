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
    private CommonsMultipartFile dataFile = null;
    private String dataFileName;
    private String dataFileType;
    private CommonsMultipartFile positionalFile = null;
    private String positionalFileName;
    private CommonsMultipartFile templateFile = null;
    private String templateFileName;
    private String headerLineNumbers;
    private String delimiter;


    /**
     * Returns the unique id associated with this object.
     *
     * @return  The unique id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique id associated with this object.
     *
     * @param id  The unique id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the cfType.
     *
     * @return  The cfType.
     */
    public String getCfType() {
        return cfType;
    }

    /**
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

    /**
     * Returns the platform.
     *
     * @return  The platform.
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Sets the platform.
     *
     * @param platform  The platform.
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * Returns the uploaded data file in CommonsMultipartFile format.
     *
     * @return  The CommonsMultipartFile data file.
     */
    public CommonsMultipartFile getDataFile() {
        return dataFile;
    }

    /**
     * Sets the uploaded data file as a CommonsMultipartFile file.
     *
     * @param dataFile  The CommonsMultipartFile data file.
     */
    public void setDataFile(CommonsMultipartFile dataFile) {
        setDataFileName(dataFile.getOriginalFilename());
        this.dataFile = dataFile;
    }

    public String getDataFileName() {
        return dataFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }


    public String getDataFileType() {
        return dataFileType;
    }

    public void setDataFileType(String dataFileType) {
        this.dataFileType = dataFileType;
    }

    /**
     * Returns the uploaded positional file in CommonsMultipartFile format.
     *
     * @return  The CommonsMultipartFile positional file.
     */
    public CommonsMultipartFile getPositionalFile() {
        return positionalFile;
    }

    /**
     * Sets the uploaded positional file as a CommonsMultipartFile file.
     *
     * @param positionalFile  The CommonsMultipartFile positional file.
     */
    public void setPositionalFile(CommonsMultipartFile positionalFile) {
        setPositionalFileName(positionalFile.getOriginalFilename());
        this.positionalFile = positionalFile;
    }

    public String getPositionalFileName() {
        return positionalFileName;
    }

    public void setPositionalFileName(String positionalFileName) {
        this.positionalFileName = positionalFileName;
    }

    /**
     * Returns the uploaded template file in CommonsMultipartFile format.
     *
     * @return  The CommonsMultipartFile template file.
     */
    public CommonsMultipartFile getTemplateFile() {
        return templateFile;
    }

    /**
     * Sets the uploaded tmeplate file as a CommonsMultipartFile file.
     *
     * @param templateFile  The CommonsMultipartFile template file.
     */
    public void setTemplateFile(CommonsMultipartFile templateFile) {
        setTemplateFileName(templateFile.getOriginalFilename());
        this.templateFile = templateFile;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public void setTemplateFileName(String templateFileName) {
        this.templateFileName = templateFileName;
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
