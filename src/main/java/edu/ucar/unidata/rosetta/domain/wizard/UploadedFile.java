/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain.wizard;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import java.io.Serializable;

/**
 * Form-backing object for the wizard to collect an uploaded file.
 */
public class UploadedFile extends WizardData implements Serializable {

  private String description;
  private CommonsMultipartFile file = null;
  private String fileName;
  private FileType fileType;
  private String id;
  private boolean required;

  public UploadedFile() {}

  public UploadedFile(CommonsMultipartFile file, String fileName, FileType fileType) {
    setFile(file);
    setFileName(fileName);
    setFileType(fileType);
  }

  /**
   * Returns the description describing the uploaded file.
   * (Used in the wizard to provide additional information.)
   *
   * @return The file description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description describing the uploaded file.
   * (Used in the wizard to provide additional information.)
   *
   * @param description The file description.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the uploaded file in CommonsMultipartFile format.
   *
   * @return The CommonsMultipartFile file.
   */
  public CommonsMultipartFile getFile() {
    return file;
  }

  /**
   * Sets the uploaded file as a CommonsMultipartFile file.
   *
   * @param file The CommonsMultipartFile file.
   */
  public void setFile(CommonsMultipartFile file) {
    this.file = file;
  }

  /**
   * Returns the name of the file.
   *
   * @return The name of the file.
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Sets the name of the file.
   *
   * @param fileName The name of the file.
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Returns the file type.
   *
   * @return The file type.
   */
  public FileType getFileType() {
    return fileType;
  }

  /**
   * Sets the file type.
   *
   * @param fileType The file type.
   */
  public void setFileType(FileType fileType) {
    this.fileType = fileType;
  }

  /**
   * Returns the unique id associated with this object.
   *
   * @return The unique id.
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the unique id associated with this object.
   *
   * @param id The unique id.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns whether the file is required to be present during form submission.
   *
   * @return true if required; otherwise false.
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * Sets whether the file is required to be present during form submission.
   *
   * @param required true if required; otherwise false.
   */
  public void setRequired(boolean required) {
    this.required = required;
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

  public enum FileType {
    DATA, POSITIONAL, TEMPLATE
  }
}
