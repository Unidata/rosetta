package edu.ucar.unidata.rosetta.domain.wizard;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.Serializable;

/**
 * A POJO to represent a file uploaded by the user in
 * the Rosetta application (acts as a form-backing-object).
 *
 * @author oxelson@ucar.edu
 */
public class UploadedFile implements Serializable {

    private String id;
    private CommonsMultipartFile file;
    private String fileName;
    private String type;

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
     * Returns the uploaded file in CommonsMultipartFile format.
     *
     * @return  The CommonsMultipartFile file.
     */
    public CommonsMultipartFile getFile() {
        return file;
    }

    /**
     * Sets the uploaded ffile as a CommonsMultipartFile file.
     *
     * @param file  The CommonsMultipartFile ffile.
     */
    public void setFile(CommonsMultipartFile file) {
        setFileName(file.getOriginalFilename());
        this.file = file;
    }

    /**
     * Returns the name of the file.
     *
     * @return  The name of the file.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the name of the file.
     *
     * @param fileName  The name of the file.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the file type.
     *
     * @return  The file type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the tfile type.
     *
     * @param type  The file type.
     */
    public void setType(String type) {
        this.type = type;
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
