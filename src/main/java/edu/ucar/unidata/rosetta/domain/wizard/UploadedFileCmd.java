package edu.ucar.unidata.rosetta.domain.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile.FileType;

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
  private List<UploadedFile> uploadedFiles;

  /**
   * Initializing this object with three uploaded files.
   */
  public UploadedFileCmd() {

    uploadedFiles = new ArrayList<>(3);

    // Instantiate the three types of files.
    UploadedFile dataFile = new UploadedFile();
    dataFile.setFileType(FileType.DATA);
    dataFile.setDescription("The file containing the ASCII data you wish to convert.");
    dataFile.setRequired(true);
    uploadedFiles.add(dataFile);

    UploadedFile positionalFile = new UploadedFile();
    positionalFile.setFileType(FileType.POSITIONAL);
    positionalFile.setDescription("An optional file containing positional data "
            + "corresponding to the data contained in the data file.");
    uploadedFiles.add(positionalFile);

    UploadedFile templateFile = new UploadedFile();
    templateFile.setFileType(FileType.TEMPLATE);
    templateFile.setDescription("A Rosetta template file used for converting the data file.");
    uploadedFiles.add(templateFile);
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
