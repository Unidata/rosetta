/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFile.FileType;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.repository.wizard.UploadedFileDao;
import edu.ucar.unidata.rosetta.util.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * Implements uploaded file manager functionality.
 */
public class UploadedFileManagerImpl implements UploadedFileManager {

    private UploadedFileDao uploadedFileDao;

    @Resource(name = "fileManager")
    private FileManager fileManager;

    /**
     * Returns the data file corresponding to the given ID as an UploadedFile object.
     *
     * @param id The ID corresponding to the stored file.
     * @return  The data file as an UploadedFile object.
     */
    public UploadedFile getDataFile(String id) {
        UploadedFile dataFile = null;
        UploadedFileCmd uploadedFileCmd = lookupPersistedDataById(id);
        for (UploadedFile uploadedFile : uploadedFileCmd.getUploadedFiles()) {
            if (uploadedFile.getFileType() == FileType.DATA) {
                dataFile = uploadedFile;
                break;
            }
        }
        return dataFile;
    }

    /**
     * Returns the positional file corresponding to the given ID as an UploadedFile object.
     *
     * @param id The ID corresponding to the stored file.
     * @return  The positional file as an UploadedFile object.
     */
    public UploadedFile getPositionalFile(String id) {
        UploadedFile dataFile = null;
        UploadedFileCmd uploadedFileCmd = lookupPersistedDataById(id);
        for (UploadedFile uploadedFile : uploadedFileCmd.getUploadedFiles()) {
            if (uploadedFile.getFileType() == FileType.POSITIONAL) {
                dataFile = uploadedFile;
                break;
            }
        }
        return dataFile;
    }

    /**
     * Returns the template file corresponding to the given ID as an UploadedFile object.
     *
     * @param id The ID corresponding to the stored file.
     * @return  The template file as an UploadedFile object.
     */
    public UploadedFile getTemplateFile(String id) {
        UploadedFile dataFile = null;
        UploadedFileCmd uploadedFileCmd = lookupPersistedDataById(id);
        for (UploadedFile uploadedFile : uploadedFileCmd.getUploadedFiles()) {
            if (uploadedFile.getFileType() == FileType.TEMPLATE) {
                dataFile = uploadedFile;
                break;
            }
        }
        return dataFile;
    }

    /**
     * Looks up and retrieves a uploaded file data using the given id.
     *
     * @param id The ID corresponding to the data to retrieve.
     * @return The persisted uploaded file data.
     */
    @Override
    public UploadedFileCmd lookupPersistedDataById(String id) {
        return uploadedFileDao.lookupById(id);
    }

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
    @Override
    public void processFileUpload(String id, UploadedFileCmd uploadedFileCmd) throws RosettaFileException {

        // Get the persisted data corresponding to this ID.
        UploadedFileCmd persistedData = lookupPersistedDataById(id);

        // Save uploaded files to disk.
        List<UploadedFile> uploadedFiles = uploadedFileCmd.getUploadedFiles();
        for (UploadedFile uploadedFile : uploadedFiles) {
            // One bother with saving files that actually exist.
            if (StringUtils.trimToNull(uploadedFile.getFileName()) != null) {

                // Save file only if name is NOT the same as the one that is persisted.
                // (If the user visits the prior step in the wizard and doesn't update
                // the uploaded file data, we don't need to process that resubmitted data.)
                for (UploadedFile persistedFile : persistedData.getUploadedFiles()) {
                    // Get the matching file type.
                    if (uploadedFile.getFileType().equals(persistedFile.getFileType())) {
                        if (!uploadedFile.getFileName().equals(persistedFile.getFileName())) {
                            int index = uploadedFiles.indexOf(uploadedFile);
                            // Write data file to disk.
                            String fileName = fileManager.writeUploadedFileToDisk(PropertyUtils.getUploadDir(),
                                    id, uploadedFile.getFileName(), uploadedFile.getFile());
                            // Update file name with CSV version.
                            uploadedFile.setFileName(fileName);
                            // Update the uploaded files list.
                            uploadedFiles.set(index, uploadedFile);
                        }
                    }
                }
            }
        }

        // Do we have persisted data to update?
        if (!persistedData.getUploadedFiles().isEmpty() && persistedData.getDataFileType() != null) {
            // Persisted uploaded file data exists.
            // Update persisted data.
            uploadedFileDao.updatePersistedData(id, uploadedFileCmd);

        } else {
            // No persisted uploaded file data.  Add it.
            uploadedFileDao.persistData(id, uploadedFileCmd);
        }
    }

    /**
     * Sets the data access object (DAO) for the UploadedFile object.
     *
     * @param uploadedFileDao The service DAO representing a UploadedFile object.
     */
    public void setUploadedFileDao(UploadedFileDao uploadedFileDao) {
        this.uploadedFileDao = uploadedFileDao;
    }

}
