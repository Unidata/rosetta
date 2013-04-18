package edu.ucar.unidata.pzhta.controller;

import edu.ucar.unidata.converters.xlsToCsv;
import edu.ucar.unidata.pzhta.Pzhta;
import edu.ucar.unidata.pzhta.domain.AsciiFile;
import edu.ucar.unidata.pzhta.domain.UploadedFile;
import edu.ucar.unidata.pzhta.service.FileParserManager;
import edu.ucar.unidata.pzhta.service.FileValidator;
import edu.ucar.unidata.pzhta.service.NcmlFileManager;
import edu.ucar.unidata.pzhta.service.ResourceManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Main controller for pzhta application.
 */
@Controller
public class TemplateController implements HandlerExceptionResolver {

    protected static Logger logger = Logger.getLogger(TemplateController.class);
    @Resource(name="resourceManager")
    private ResourceManager resourceManager;
    @Resource(name="fileParserManager")
    private FileParserManager fileParserManager;
    @Resource(name="ncmlFileManager")
    private NcmlFileManager ncmlFileManager;
    @Resource(name="fileValidator")
    private FileValidator fileValidator;
   
    /**
     * Accepts a GET request for template creation, fetches resource information
     * from file system and inject that data into the Model to be used in the View.
     * Returns the view that walks the user through the steps of template creation. 
     * 
     * @param model  The Model object to be populated by Resources. 
     * @return  The 'create' ModelAndView containing the Resource-populated Model.
     * 
     */
    @RequestMapping(value="/create", method=RequestMethod.GET)
    public ModelAndView createTemplate(Model model) {              
        model.addAllAttributes(resourceManager.loadResources());   
        return new ModelAndView("create");        
    }

    /**
     * Accepts a POST request for an uploaded file, stores that file to disk and,
     * returns the local (unique, alphanumeric) file name of the uploaded ASCII file.
     * 
     * @param file  The UploadedFile form backing object containing the file.
     * @param request  The HttpServletRequest with which to glean the client IP address.
     * @return  A String of the local file name for the ASCII file (or null for an error). 
     */
    @RequestMapping(value="/upload", method=RequestMethod.POST)
    @ResponseBody
    public String processUpload(UploadedFile file, HttpServletRequest request) {
        FileOutputStream outputStream = null;
        String uniqueId = createUniqueId(request);
        String filePath = System.getProperty("java.io.tmpdir") + "/" + uniqueId;
        try {
            File localFileDir = new File(filePath);
            if (!localFileDir.exists()) localFileDir.mkdir();
            outputStream = new FileOutputStream(new File(filePath + "/" + file.getFileName()));
            outputStream.write(file.getFile().getFileItem().get());
            outputStream.flush();
            outputStream.close();
            if ((file.getFileName().contains(".xls")) || (file.getFileName().contains(".xlsx"))) {
                String xlsFilePath =  filePath + "/" + file.getFileName();
                xlsToCsv.convert(xlsFilePath, null);
                String csvFilePath = null;
                if (xlsFilePath.contains(".xlsx")) {
                    csvFilePath = xlsFilePath.replace(".xlsx",".csv");
                } else if (xlsFilePath.contains(".xls")) {
                    csvFilePath = xlsFilePath.replace(".xls",".csv");
                }
                file.setFileName(csvFilePath);
            }
        } catch (Exception e) {
            logger.error("A file upload error has occurred: " + e.getMessage());
            return null;
        }
        return uniqueId;                 
    }

    /**
     * Accepts a POST request Parse the uploaded file.  The methods gets called at various
     * times and the file data is parsed according the data it contains.
     * 
     * @param file  The AsciiFile form backing object.
     * @param result  The BindingResult object for errors. 
     * @return  A String of the local file name for the ASCII file. 
     */
    @RequestMapping(value="/parse", method=RequestMethod.POST)
    @ResponseBody
    public String parseFile(AsciiFile file, BindingResult result) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String filePath = tmpDir + "/" + file.getUniqueId() + "/" + file.getFileName();

        // SCENARIO 1: no header lines yet
        if (file.getHeaderLineList().isEmpty()) {
            return fileParserManager.parseByLine(filePath);
        } else {
            // SCENARIO 2: header lines and delimiters available 
            if (!file.getDelimiterList().isEmpty()) {                                          
                List <String> delimiterList = file.getDelimiterList(); 
                String selectedDelimiter = delimiterList.get(0); 
                /**
                // Time for some validation
                fileValidator.validateList(delimiterList, result);
                fileValidator.validateList(file.getHeaderLineList(), result);
                fileValidator.validateDelimiterCount(filePath, file, result);

                if (result.hasErrors()) {
                    List<ObjectError> validationErrors = result.getAllErrors();
                    Iterator<ObjectError> iterator = validationErrors.iterator();
	                while (iterator.hasNext()) {
                        logger.error("Validation Error: " + iterator.next().toString());
	                }
                    return null;

                } else {
                **/
                    String normalizedFileData = fileParserManager.normalizeDelimiters(filePath,selectedDelimiter, delimiterList, file.getHeaderLineList());
                    if (file.getVariableNameMap().isEmpty()) {     
                        return selectedDelimiter + "\n" + normalizedFileData;
                    } else {
                        // SCENARIO 3: we have variable data!
                        List<List<String>> parseFileData = fileParserManager.getParsedFileData();
                        // Create the NCML file using the file data
                        String catalinaBase = System.getProperty("catalina.base");
                        String downloadDir = catalinaBase + "/webapps/pzhta/download";
                        String ncmlFile = null;
                        try {
                            ncmlFile = ncmlFileManager.createNcmlFile(file, parseFileData, downloadDir);
                        } catch (IOException e) {
                            System.err.println("Caught IOException: " + e.getMessage());
                            return ncmlFile;
                        } 
                        // Create the netCDF file
                        Pzhta ncWriter = new Pzhta();
                        String fileOut = downloadDir + "/" + FilenameUtils.removeExtension(file.getFileName()) + ".nc";
                       logger.warn(fileOut);
                        if (ncWriter.convert(ncmlFile, fileOut, parseFileData)) {
                            return fileOut.replaceAll(downloadDir + "/", "") + "\n" + ncmlFile.replaceAll(downloadDir + "/", "");
                        } else {
                            logger.error("netCDF file not created.");
                            return null;
                        }  
                    }                    
               // } 
            } else {
                logger.error("File header lines are present, but no delimiters have been specified.");
                return null;
            }        
        }
    }



    /**
     * Attempts to get the client IP address from the request.
     * 
     * @param request  The HttpServletRequest.
     * @return  The client's IP address.
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
     * Creates a unique id for the file name from the clients IP address and the date.
     * 
     * @param request  The HttpServletRequest.
     * @return  The unique file name id.
     */
    private String createUniqueId(HttpServletRequest request) {
        String id = new Integer(new Date().hashCode()).toString();
        String ipAddress = getIpAddress(request);
        if (ipAddress != null) {
            id = ipAddress + id;
        } else {
            id = new Integer(new Random().nextInt()).toString() + id;
        }
        return id;
    }

   /**
    * This method gracefully handles any uncaught exception that are fatal 
    * in nature and unresolvable by the user.
    * 
    * @param arg0   The current HttpServletRequest request.
    * @param arg1  The current HttpServletRequest response.
    * @param arg2  The executed handler, or null if none chosen at the time of the exception.
    * @param exception  The  exception that got thrown during handler execution.
    * @return  The error page containing the appropriate message to the user. 
    */
    @Override
    public ModelAndView resolveException(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception exception) {
        String message = "";
        if (exception instanceof MaxUploadSizeExceededException){ 
            // this value is declared in the /WEB-INF/pzhta-servlet.xml file (we can move it elsewhere for convenience)
            message = "File size should be less then "+ ((MaxUploadSizeExceededException)exception).getMaxUploadSize()+" byte.";
        } else{
            message = "An error has occurred: " + exception.getClass().getName() + ":" + exception.getMessage();
        }     
        // log it
        logger.error(message);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("message", message);
        ModelAndView modelAndView = new ModelAndView("fatalError", model);
        return modelAndView;
    }
} 
