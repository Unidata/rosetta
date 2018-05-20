package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * A POJO to hold the uploaded file-related data collected from the user in the
 * corresponding step of the wizard (acts as a form-backing object in that step).
 *
 * @author oxelson@ucar.edu
 */
public class UploadedFileData {

    private String id;
    private String dataFileType;
    private CommonsMultipartFile dataFile = null;
    private String dataFileName;
    private CommonsMultipartFile positionalFile = null;
    private String positionalFileName;
    private CommonsMultipartFile templateFile = null;
    private String templateFileName;
    private String submit;

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
     * Returns the data file type.
     *
     * @return  The data file type.
     */
    public String getDataFileType() {
        return dataFileType;
    }

    /**
     * Sets the data file type.
     *
     * @param dataFileType  The data file type.
     */
    public void setDataFileType(String dataFileType) {
        this.dataFileType = dataFileType;
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

    /**
     * Returns the name of the data file.
     *
     * @return  The name of the data file.
     */
    public String getDataFileName() {
        return dataFileName;
    }

    /**
     * Sets the name of the data file.
     *
     * @param dataFileName  The name of the data file.
     */
    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
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

    /**
     * Returns the name of the positional file.
     *
     * @return  The name of the positional file.
     */
    public String getPositionalFileName() {
        return positionalFileName;
    }

    /**
     * Sets the name of the positional file.
     *
     * @param positionalFileName The name of the positional file.
     */
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
     * Sets the uploaded template file as a CommonsMultipartFile file.
     *
     * @param templateFile  The CommonsMultipartFile template file.
     */
    public void setTemplateFile(CommonsMultipartFile templateFile) {
        setTemplateFileName(templateFile.getOriginalFilename());
        this.templateFile = templateFile;
    }

    /**
     * Returns the name of the template file.
     *
     * @return  The name of the template file.
     */
    public String getTemplateFileName() {
        return templateFileName;
    }

    /**
     * Sets the name of the template file.
     *
     * @param templateFileName The name of the template file.
     */
    public void setTemplateFileName(String templateFileName) {
        this.templateFileName = templateFileName;
    }

    /**
     * Returns the submit type.
     *
     * @return  The submit type.
     */
    public String getSubmit() {
        return submit;
    }

    /**
     * Sets the submit type.
     *
     * @param submit    The submit type.
     */
    public void setSubmit(String submit) {
        this.submit = submit;
    }

    /**
     * String representation of this Data object.
     *
     * @return  The string representation.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
