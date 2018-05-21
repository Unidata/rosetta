package edu.ucar.unidata.rosetta.controller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucar.unidata.rosetta.converters.TagUniversalFileFormat;
import edu.ucar.unidata.rosetta.domain.UploadedFile;
import edu.ucar.unidata.rosetta.util.RosettaProperties;
import edu.ucar.unidata.rosetta.util.ZipFileUtil;
import ucar.ma2.InvalidRangeException;

import static java.util.stream.Collectors.toList;

/**
 * Main controller for Rosetta application.
 */
@Controller
public class AutoConvertController implements HandlerExceptionResolver {

    protected static Logger logger = Logger.getLogger(AutoConvertController.class);
    @Autowired
    ServletContext servletContext;

    String APPLICATION_ZIP = "application/zip";

    private String getDownloadDir() {
        String downloadDir = "";
        downloadDir = RosettaProperties.getDownloadDir();
        return downloadDir;
    }

    private String getUploadDir() {
        String uploadDir = "";
        uploadDir = RosettaProperties.getUploadDir();
        return uploadDir;
    }

    private ArrayList<String> cleanupInventory(ArrayList<String> inventory) {
        // first pass based on typical .gitignore for OS generated files
        Stream<String> cleanInventory = inventory.stream()
                //.map(inventoryFile -> inventoryFile.toUpperCase())
                .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, ".Spotlight-V100"))
                .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, ".Trashes"))
                .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, "ehthumbs.db"))
                .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, "Thumbs.db"))
                .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, "/__MACOSX/"))
                .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, ".DS_STORE"));

        return new ArrayList<String>(cleanInventory.collect(toList()));
    }

    /**
     * Accepts a POST request for an uploaded file, stores that file to disk, auto converts the
     * file, and, returns a zip file of the converted dataset ASCII file.
     *
     * @param file    The UploadedFile form backing object containing the file.
     * @param request The HttpServletRequest with which to glean the client IP address.
     * @return A String of the local file name for the ASCII file (or null for an error).
     */
    @RequestMapping(value = "/batchProcess", method = RequestMethod.POST, produces = "application/zip")
    @ResponseBody
    public Resource batchProcess(UploadedFile file, HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<String> convertedFiles = new ArrayList<>();
        String uniqueId = createUniqueId(request);



        String filePath = FilenameUtils.concat(getUploadDir(), uniqueId);
        String extractToDir = FilenameUtils.concat(filePath, "extracted");
        File localFileDir = new File(filePath);
        if (!localFileDir.exists()) {
            localFileDir.mkdirs();
        }

        File uploadedFile = new File(FilenameUtils.concat(filePath, file.getFileName()));
        try (FileOutputStream outputStream = new FileOutputStream(uploadedFile)) {
            outputStream.write(file.getFile().getFileItem().get());
            outputStream.flush();
            outputStream.close();
        } catch (Exception exc) {
            logger.error("error while saving uploaded file to disk.");
        }

        File toDirectory = new File(extractToDir);
        if (!localFileDir.exists()) {
            localFileDir.mkdirs();
        }
        ArrayList<String> inventory = new ArrayList<>();
        if (uploadedFile.getName().endsWith(".zip")) {
           // inventory = ZipFileUtil.unzipAndInventory(uploadedFile, toDirectory);
        }

        // clean up inventory - some auto-generated OS files can really play havoc
        // if they are not accounted for. For example: "__MACOSX/"

        inventory = cleanupInventory(inventory);

        String template = "";
        for (String inventoryFile : inventory) {
            if (inventoryFile.endsWith("template")) {
                template = inventoryFile;
            }
        }

        // load template
        JSONParser parser = new JSONParser();
        String convertFrom = "";
        try {
            Object obj = parser.parse(new FileReader(template));


            JSONObject jsonObject = (JSONObject) obj;
            convertFrom = (String) jsonObject.get("convertFrom");
            System.out.println(convertFrom);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // process data files based on convertTo type
        if (convertFrom.equals("tuff")) {
            for (String inventoryFile : inventory) {
                if (!inventoryFile.endsWith("template") && !inventoryFile.endsWith("metadata")) {
                    TagUniversalFileFormat tuff = new TagUniversalFileFormat();
                    tuff.parse(inventoryFile);
                    String fullFileNameExt = FilenameUtils.getExtension(inventoryFile);
                    String ncfile = inventoryFile.replace(fullFileNameExt, "nc");
                    ncfile = FilenameUtils.concat(filePath, ncfile);
                    try {
                        String convertedFile = tuff.convert(ncfile);
                        convertedFiles.add(convertedFile);
                    } catch (InvalidRangeException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // return zip file containing one or more converted files in body of the response

        String zipFileName = FilenameUtils.concat(filePath, "converted_files.zip");
        File zipFile = new File(zipFileName);
        ZipFileUtil zippedConvertedFiles = new ZipFileUtil(zipFileName);
       // zippedConvertedFiles.addAllToZip("converted_files", convertedFiles);

        response.setContentType(APPLICATION_ZIP);
        response.setHeader("Content-Disposition", "inline; filename=" + zipFile.getName());
        response.setHeader("Content-Length", String.valueOf(zipFile.length()));
        return new FileSystemResource(zipFile.getAbsolutePath());

    }

    /**
     * Creates a unique id for the file name from the clients IP address and the
     * date.
     *
     * @param request The HttpServletRequest.
     * @return The unique file name id.
     */
    private String createUniqueId(HttpServletRequest request) {
        String id = Integer.valueOf(new Date().hashCode()).toString();
        String ipAddress = getIpAddress(request);
        if (ipAddress != null) {
            id = ipAddress + id;
        } else {
            id = Integer.valueOf(new Random().nextInt()).toString() + id;
        }
        return id.replaceAll(":", "_");
    }

    /**
     * Attempts to get the client IP address from the request.
     *
     * @param request The HttpServletRequest.
     * @return The client's IP address.
     */
    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = null;
        if (request.getRemoteAddr() != null) {
            ipAddress = request.getRemoteAddr();
            ipAddress = StringUtils.deleteWhitespace(ipAddress);
            ipAddress = StringUtils.trimToNull(ipAddress);
            ipAddress = StringUtils.lowerCase(ipAddress);
            ipAddress = StringUtils.replaceChars(ipAddress, ".", "");
        }
        return ipAddress;
    }

    /**
     * This method gracefully handles any uncaught exception that are fatal in
     * nature and unresolvable by the user.
     *
     * @param arg0      The current HttpServletRequest request.
     * @param arg1      The current HttpServletRequest response.
     * @param arg2      The executed handler, or null if none chosen at the time of the exception.
     * @param exception The exception that got thrown during handler execution.
     * @return The error page containing the appropriate message to the user.
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest arg0,
                                         HttpServletResponse arg1, Object arg2, Exception exception) {
        String message = "";
        if (exception instanceof MaxUploadSizeExceededException) {
            // this value is declared in the /WEB-INF/rosetta-servlet.xml file
            // (we can move it elsewhere for convenience)
            message = "File size should be less than "
                    + ((MaxUploadSizeExceededException) exception)
                    .getMaxUploadSize() + " byte.";
        } else {
            message = "An error has occurred: "
                    + exception.getClass().getName() + ":"
                    + exception.getMessage();
        }
        // log it
        logger.error(message);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("message", message);
        ModelAndView modelAndView = new ModelAndView("fatalError", model);
        return modelAndView;
    }

}
