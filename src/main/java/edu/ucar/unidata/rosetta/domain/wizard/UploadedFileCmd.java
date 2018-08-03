package edu.ucar.unidata.rosetta.domain.wizard;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Form-backing object for the wizard to collect a collection of uploaded files.
 *
 * @author oxelson@ucar.edu
 */
public class UploadedFileCmd extends WizardData {

  private String dataFileType;
  private List<UploadedFile> uploadedFiles = new ArrayList<>();

  public UploadedFileCmd() {}

  public UploadedFileCmd(List<UploadedFile> uploadedFiles) {
    setUploadedFiles(uploadedFiles);
  }

  /**
   * Returns the data file type.
   * (Corresponds to fileType resource).
   *
   * @return  The data file type.
   */
  public String getDataFileType() {
    return dataFileType;
  }

  /**
   * Returns a list of uploaded files.
   *
   * @return  The uploaded files.
   */
  public List<UploadedFile> getUploadedFiles() {
    return uploadedFiles;
  }

  /**
   * Sets the data file type.
   * (Corresponds to fileType resource).
   *
   * @param dataFileType  The data file type.
   */
  public void setDataFileType(String dataFileType) {
    this.dataFileType = dataFileType;
  }

  /**
   * Sets a list of uploaded files.
   *
   * @param uploadedFiles The uploaded files.
   */
  public void setUploadedFiles(List<UploadedFile> uploadedFiles) {
    this.uploadedFiles.clear();
    this.uploadedFiles = uploadedFiles;
  }

  /**
   * String representation of this object.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
