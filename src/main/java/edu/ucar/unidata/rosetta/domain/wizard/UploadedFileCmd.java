/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain.wizard;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Form-backing object for the wizard to collect a collection of uploaded files.
 * Used for dynamic form binding in Spring to collect multiple uploaded file objects.
 * The term 'Cmd' in the name refers to the command object used in form data binding.
 */
public class UploadedFileCmd extends WizardData {

    private String dataFileType;
    private List<UploadedFile> uploadedFiles = new ArrayList<>(3);

    public UploadedFileCmd() {
        // Data file.
        UploadedFile dataFile = new UploadedFile();
        dataFile.setFileType(UploadedFile.FileType.DATA);
        dataFile.setDescription("The file containing the ASCII data you wish to convert.");
        dataFile.setRequired(true);
        addToUploadedFiles(dataFile);

        // Positional file.
        UploadedFile positionalFile = new UploadedFile();
        positionalFile.setFileType(UploadedFile.FileType.POSITIONAL);
        positionalFile.setDescription("An optional file containing positional data "
                + "corresponding to the data contained in the data file.");
        positionalFile.setRequired(false);
        addToUploadedFiles(positionalFile);

        // Template file.
        UploadedFile templateFile = new UploadedFile();
        templateFile.setFileType(UploadedFile.FileType.TEMPLATE);
        templateFile.setDescription("A Rosetta template file used for converting the data file.");
        templateFile.setRequired(false);
        addToUploadedFiles(templateFile);
    }

    public UploadedFileCmd(List<UploadedFile> uploadedFiles) {
        this();
        for (UploadedFile uploadedFile : uploadedFiles) {
            replaceUploadedFile(uploadedFile);
        }
    }

    /**
     * Adds an uploaded file to the list.
     *
     * @param uploadedFile The uploaded file to add.
     */
    public void addToUploadedFiles(UploadedFile uploadedFile) {
        this.uploadedFiles.add(uploadedFile);
    }

    /**
     * Returns the data file type.
     * (Corresponds to fileType resource).
     *
     * @return The data file type.
     */
    public String getDataFileType() {
        return dataFileType;
    }

    /**
     * Sets the data file type.
     * (Corresponds to fileType resource).
     *
     * @param dataFileType The data file type.
     */
    public void setDataFileType(String dataFileType) {
        this.dataFileType = dataFileType;
    }

    /**
     * Returns a list of uploaded files.
     *
     * @return The uploaded files.
     */
    public List<UploadedFile> getUploadedFiles() {
        return uploadedFiles;
    }

    /**
     * Sets a list of uploaded files.
     *
     * @param uploadedFiles The uploaded files.
     */
    public void setUploadedFiles(List<UploadedFile> uploadedFiles) {
        //this.uploadedFiles.clear();
        this.uploadedFiles = uploadedFiles;
    }

    /**
     * Updates one of the existing files in the uploaded files.
     *
     * @param replacementFile The file to replace.
     */
    public void replaceUploadedFile(UploadedFile replacementFile) {
        for (UploadedFile uploadedFile : this.uploadedFiles) {
            if (replacementFile.getFileType().equals(uploadedFile.getFileType())) {
                int index = uploadedFiles.indexOf(uploadedFile);
                this.uploadedFiles.set(index, replacementFile);
            }
        }
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
