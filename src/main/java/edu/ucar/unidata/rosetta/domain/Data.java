package edu.ucar.unidata.rosetta.domain;

/**
 *
 */
public class Data {

    private int id;
    private String cfType;
    private String platform;
    private String fileName;
    private int headerLineNumbers;
    private String delimiter;


    /*
     * Returns the unique id associated with this object.
     *
     * @return  The unique id.
     */
    public int getId() {
        return id;
    }

    /*
     * Sets the unique id associated with this object.
     *
     * @param id  The unique id.
     */
    public void setId(int id) {
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getHeaderLineNumbers() {
        return headerLineNumbers;
    }

    public void setHeaderLineNumbers(int headerLineNumbers) {
        this.headerLineNumbers = headerLineNumbers;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String toString() {
        if (cfType != null) {
            return cfType;
        } else {
            return platform;
        }
    }
}
