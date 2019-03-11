/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain.batch;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.Serializable;

/**
 * A POJO to hold the zip file uploaded by the user which contains:
 *   At least one data file
 *   At least one main template file (rosetta.template)
 *   Optionally one .position file per data file (same name, different extension)
 *   Optionally one .template file per data file (same name, different extension)
 *   Optionally one .metadata file per data file (same name, different extension)
 *
 * @author sarms@ucar.edu
 */
public class BatchProcessZip implements Serializable {

    private CommonsMultipartFile batchZipFile = null;
    private String batchZipName;
    private String id;

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
     * Returns the uploaded data file in CommonsMultipartFile format.
     *
     * @return The CommonsMultipartFile data file.
     */
    public CommonsMultipartFile getBatchZipFile() {
        return batchZipFile;
    }

    /**
     * Sets the uploaded data file as a CommonsMultipartFile file.
     *
     * @param batchZipFile The CommonsMultipartFile data file.
     */
    public void setBatchZipFile(CommonsMultipartFile batchZipFile) {
        setBatchZipName(batchZipFile.getOriginalFilename());
        this.batchZipFile = batchZipFile;
    }

    /**
     * Returns the name of the data file.
     *
     * @return The name of the data file.
     */
    public String getBatchZipName() {
        return batchZipName;
    }

    /**
     * Sets the name of the data file.
     *
     * @param batchZipName The name of the data file.
     */
    private void setBatchZipName(String batchZipName) {
        this.batchZipName = batchZipName;
    }

}