/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;

/**
 * Service for handling uploaded file data collected from the wizard.
 */
public interface UploadedFileManager {


    /**
     * Returns the data file corresponding to the given ID as an UploadedFile object.
     *
     * @param id The ID corresponding to the stored file.
     * @return  The data file as an UploadedFile object.
     */
    public UploadedFile getDataFile(String id);

    /**
     * Returns the positional file corresponding to the given ID as an UploadedFile object.
     *
     * @param id The ID corresponding to the stored file.
     * @return  The positional file as an UploadedFile object.
     */
    public UploadedFile getPositionalFile(String id);

    /**
     * Returns the template file corresponding to the given ID as an UploadedFile object.
     *
     * @param id The ID corresponding to the stored file.
     * @return  The template file as an UploadedFile object.
     */
    public UploadedFile getTemplateFile(String id);

    /**
     * Looks up and retrieves a uploaded file data using the given id.
     *
     * @param id The ID corresponding to the data to retrieve.
     * @return The persisted uploaded file data.
     */
    public UploadedFileCmd lookupPersistedDataById(String id);

    /**
     * Retrieves the data file from disk and parses it by line, converting it into a JSON string.
     *
     * @param id The unique id associated with the file (a subdir in the uploads directory).
     * @param dataFileName The file to parse.
     * @return A JSON String of the file data parsed by line.
     * @throws RosettaFileException For any file I/O or JSON conversions problems.
     */
    public String parseDataFileByLine(String id, String dataFileName) throws RosettaFileException;

    /**
     * Processes the data submitted by the user containing uploaded file information. Writes the
     * uploaded files to disk. Updates the persisted data corresponding to the provided unique ID
     * with the uploaded file information.
     *
     * @param id              The unique ID corresponding to already persisted data.
     * @param uploadedFileCmd The user-submitted data containing the uploaded file information.
     * @throws RosettaFileException If unable to write file(s) to disk or a file conversion exception
     *                              occurred.
     */
    public void processFileUpload(String id, UploadedFileCmd uploadedFileCmd) throws RosettaFileException;
}
