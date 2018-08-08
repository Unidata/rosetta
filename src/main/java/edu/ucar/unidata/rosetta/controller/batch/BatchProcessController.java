/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.batch;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucar.unidata.rosetta.domain.batch.BatchProcessZip;
import edu.ucar.unidata.rosetta.service.batch.BatchFileManagerImpl;
import edu.ucar.unidata.rosetta.util.PropertyUtils;

/**
 * Main controller for the Rosetta batch processing feature.
 */
@Controller
public class BatchProcessController implements HandlerExceptionResolver {

    protected static Logger logger = Logger.getLogger(BatchProcessController.class);

    private BatchFileManagerImpl batchFileManager = new BatchFileManagerImpl();


    @Autowired
    ServletContext servletContext;


    /**
     * Accepts a POST request for an uploaded zip file to be batch processed. Calls out to batch
     * rocessing code Returns a zip file of the converted datasets.
     *
     * @param batchZipFile The batchZipFile form backing object containing the file.
     * @param request      The HttpServletRequest with which to glean the client IP address.
     * @return A String of the local file name for the ASCII file (or null for an error).
     */
    @RequestMapping(value = "/batchProcess", method = RequestMethod.POST, produces = "application/zip")
    @ResponseBody
    public Resource batchProcess(BatchProcessZip batchZipFile, HttpServletRequest request, HttpServletResponse response) throws IOException {
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

    /**
     * This method gracefully handles any uncaught exception that are fatal in nature and
     * unresolvable by the user.
     *
     * @param request   The current HttpServletRequest request.
     * @param response  The current HttpServletRequest response.
     * @param handler   The executed handler, or null if none chosen at the time of the exception.
     * @param exception The exception that got thrown during handler execution.
     * @return The error page containing the appropriate message to the user.
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         java.lang.Object handler,
                                         Exception exception) {
        String message;
        if (exception instanceof MaxUploadSizeExceededException) {
            // this value is declared in the /WEB-INF/rosetta-servlet.xml file
            // (we can move it elsewhere for convenience)
            message = "File size should be less than "
                    + ((MaxUploadSizeExceededException) exception)
                    .getMaxUploadSize() + " byte.";
        } else {
            StringWriter errors = new StringWriter();
            exception.printStackTrace(new PrintWriter(errors));
            message = "An error has occurred: "
                    + exception.getClass().getName() + ":"
                    + errors;
        }
        // Log it!
        logger.error(message);
        Map<String, Object> model = new HashMap<>();
        model.put("message", message);
        return new ModelAndView("error", model);
    }
}
