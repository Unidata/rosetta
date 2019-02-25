/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.batch;

import edu.ucar.unidata.rosetta.domain.batch.BatchProcessZip;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.batch.BatchFileManager;
import edu.ucar.unidata.rosetta.service.batch.BatchFileManagerImpl;
import edu.ucar.unidata.rosetta.util.PropertyUtils;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Main controller for the Rosetta batch processing feature.
 */
@Controller
public class BatchProcessController {

    protected static Logger logger = Logger.getLogger(BatchProcessController.class);

    @javax.annotation.Resource(name = "batchFileManager")
    private BatchFileManager batchFileManager;

    /**
     * Accepts a POST request for an uploaded zip file to be batch processed. Calls out to batch
     * processing code Returns a zip file of the converted datasets.
     *
     * @param batchZipFile The batchZipFile form backing object containing the file.
     * @param request      The HttpServletRequest with which to glean the client IP address.
     * @return A String of the local file name for the ASCII file (or null for an error).
     * @throws IOException If unable to access template file.
     * @throws RosettaDataException  If unable to parse data file with given delimiter.
     */
    @RequestMapping(value = "/batchProcess", method = RequestMethod.POST, produces = "application/zip")
    @ResponseBody
    public Resource batchProcess(BatchProcessZip batchZipFile, HttpServletRequest request, HttpServletResponse response) throws IOException, RosettaDataException {
        String id = PropertyUtils.createUniqueDataId(request);
        batchZipFile.setId(id);
        String processedZipFile;

        processedZipFile = batchFileManager.batchProcess(batchZipFile);

        File zipFile = new File(processedZipFile);
        String APPLICATION_ZIP = "application/zip";
        response.setContentType(APPLICATION_ZIP);
        response.setHeader("Content-Disposition", "inline; filename=" + zipFile.getName());
        response.setHeader("Content-Length", String.valueOf(zipFile.length()));
        return new FileSystemResource(zipFile.getAbsolutePath());
    }
}
