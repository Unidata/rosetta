package edu.ucar.unidata.rosetta.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.service.DataManager;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.validators.CFTypeValidator;
import edu.ucar.unidata.rosetta.service.validators.FileValidator;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;


/**
 * Main controller for Rosetta application.
 */
@Controller
public class TemplateController implements HandlerExceptionResolver {

    protected static Logger logger = Logger.getLogger(TemplateController.class);

    @Autowired
    ServletContext servletContext;

    @Resource(name = "dataManager")
    private DataManager dataManager;
    /*
    @Resource(name = "jsonManager")
    private JsonManager jsonManager;
    */
    @Resource(name = "resourceManager")
    private ResourceManager resourceManager;
    /*
    @Resource(name = "fileParserManager")
    private FileParserManager fileParserManager;
    @Resource(name = "netcdfFileManager")
    private NetcdfFileManager netcdfFileManager;

    */
    // Validators
    @Autowired
    private CFTypeValidator cfTypeValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmer = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmer);
        binder.setValidator(cfTypeValidator);
    }


    @Resource(name = "fileValidator")
    private FileValidator fileValidator;


    /**
     * Accepts a GET request for access to step 1 of the wizard (shows a web form to collect CF type data).
     *
     * @param model  The Model object to be populated by CF type data.
     * @return  View and the Model for the wizard to process.
     */
    @RequestMapping(value = "/step1", method = RequestMethod.GET)
    public ModelAndView specifyCFType(Model model, HttpServletRequest request) {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        // Create a Data form-backing object.
        Data data;
        if (rosettaCookie != null)
            // User-provided cfType info already exists.  Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
        else
            data = new Data();

        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model (used by view to keep track of where we are in the wizard).
        model.addAttribute("currentStep", "1");
        // Add a list of all steps to the Model for rendering left nav menu.
        model.addAttribute("steps", resourceManager.loadResources().get("steps"));
        // Add domains data to Model (for platform display).
        model.addAttribute("domains", resourceManager.loadResources().get("domains"));
        // Add platforms data to Model (for platform selection).
        model.addAttribute("platforms", resourceManager.loadResources().get("platforms"));
        // Add data object to Model.
        return new ModelAndView("wizard");
    }

    /**
     * Accepts a POST request from step 1 of the wizard. Collects the CF type data entered by the user
     * and validates it.  If it passes validation,
     *
     * @param data      The form-backing object containing the CF type data.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to pass to the dataManager to get client IP.
     * @param response  HttpServletResponse needed for setting cookie.
     * @return          Redirect to next step.
     */
    @RequestMapping(value = "/step1", method = RequestMethod.POST)
    public ModelAndView specifyCFType(Data data, BindingResult result, Model model, HttpServletRequest request, HttpServletResponse response) {
    //    public ModelAndView specifyCFType(@Valid Data data, BindingResult result, Model model, HttpServletRequest request, HttpServletResponse response) {

        if (result.hasErrors()) {   // validation errors
            logger.error("Validation errors detected in CF type form data.");
            model.addAttribute("error", result.getGlobalError().getDefaultMessage());
            // Add current step to the Model (used by view to keep track of where we are in the wizard).
            model.addAttribute("currentStep", "1");
            // Add a list of all steps to the Model for rendering left nav menu.
            model.addAttribute("steps", resourceManager.loadResources().get("steps"));
            // Add domains data to Model (for platform display).
            model.addAttribute("domains", resourceManager.loadResources().get("domains"));
            // Add platforms data to Model (for platform selection).
            model.addAttribute("platforms", resourceManager.loadResources().get("platforms"));
            return new ModelAndView("wizard");
        } else {
            // Persist the data.
            dataManager.persistData(data, request);
            response.addCookie(new Cookie("rosetta", data.getId()));
            return new ModelAndView(new RedirectView("/step2", true));
        }
    }


    /**
     * Accepts a GET request for access to step 2 of the wizard.
     *
     * @param model  The Model object to be populated by CF type data.
     * @return  View and the Model for the wizard to process.
     */
    @RequestMapping(value = "/step2", method = RequestMethod.GET)
    public ModelAndView fileUpload(Model model, HttpServletRequest request) {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        // Create a Data form-backing object.
        Data data;
        if (rosettaCookie != null)
            // User-provided cfType info already exists.  Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the cookie.");

        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model.
        model.addAttribute("currentStep", "2");
        // Add a list of all steps to the Model for rendering left nav menu.
        model.addAttribute("steps", resourceManager.loadResources().get("steps"));
        // Add domains data to Model (for file upload  display based on community type).
        model.addAttribute("domains", resourceManager.loadResources().get("domains"));
        // Add file type data to Model (for file type selection if cfType was directly specified).
        model.addAttribute("fileTypes", resourceManager.loadResources().get("fileTypes"));
        return new ModelAndView("wizard");
    }


    /**
     * Accepts a POST request for an uploaded file, stores that file to disk
     * and, returns the local (unique, alphanumeric) file name of the uploaded
     * ASCII file.
     *
     * @param file    The UploadedFile form backing object containing the file.
     * @param request The HttpServletRequest with which to glean the client IP address.
     * @return A String of the local file name for the ASCII file (or null for an error).
     */
    /*
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String processUpload(Data data, HttpServletRequest request) {
        // FileOutputStream outputStream = null;
        String uniqueId = createUniqueId(request);
        String filePath = FilenameUtils.concat(getUploadDir(), uniqueId);
        try {
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
                return null;
            }

            if ((file.getFileName().contains(".xls")) || (file.getFileName().contains(".xlsx"))) {
                String xlsFilePath = FilenameUtils.concat(filePath, file.getFileName());
                boolean conversionSuccessful = XlsToCsv.convert(xlsFilePath, null);
                String csvFilePath = null;
                if (conversionSuccessful) {
                    if (xlsFilePath.contains(".xlsx")) {
                        csvFilePath = xlsFilePath.replace(".xlsx", ".csv");
                    } else if (xlsFilePath.contains(".xls")) {
                        csvFilePath = xlsFilePath.replace(".xls", ".csv");
                    }
                }
                file.setFileName(csvFilePath);
            }
        } catch (Exception e) {
            logger.error("A file upload error has occurred: " + e.getMessage());
            return null;
        }
        return uniqueId;
    }

*/


    /**
     * Accepts a GET AJAX request for specific platform data corresponding to a community.
     * Called during STEP 0 in the wizard.
     *
     * @param community  The community selected by the user.
     * @param model  The Model object to be populated by platform data.
     * @return  The platforms in JSON string format.
     */
    /*
    @RequestMapping(value = "/getPlatforms/{community}", method = RequestMethod.GET)
    @ResponseBody
    public List getPlatforms(@PathVariable String community, Model model) {
        // Get list of community resources.
        List communities = (List) resourceManager.loadResources().get("communitys");
        // Get list of platform resources.
        List allPlatforms = (List) resourceManager.loadResources().get("platforms");

        // Map to act as placeholder for data that will be returned to the client.
        List<Map> matchingPlatforms = new ArrayList<Map>();


        for (Object comm : communities) {

            Map c = (Map)comm;
            String communityName = (String) c.get("name");

            if (community.equals(communityName.replaceAll("\\p{Z}", ""))) {

                // The number of platforms a community has will determine the resulting type: 
                if (String.class.isInstance(c.get("platform"))) {
                    // If there is only one platform for the community, then it will be a String
                    String platformName = (String) c.get("platform");

                    for (Object plat : allPlatforms) {
                        Map p = (Map)plat;
                        String pName = (String) p.get("name");
                        if (pName.equals(platformName)) {
                            matchingPlatforms.add(p);
                        }
                            
                    }
                } else {
                    // More than one platform will be a List.
                    List platforms = (List) c.get("platform");

                    for (Object platform : platforms) {
                        String platformName = (String) platform;
                        for (Object plat : allPlatforms) {
                            Map p = (Map)plat;
                            String pName = (String) p.get("name");
                            if (pName.equals(platformName)) {
                                matchingPlatforms.add(p);
                            }
                            
                        }
                    }
                }
            }
        }
        return matchingPlatforms;
    }


*/




    /**
     * Accepts a GET request for template creation, fetches resource information
     * from file system and inject that data into the Model to be used in the
     * View. Returns the view that walks the user through the steps of template
     * creation.
     *
     * @param model The Model object to be populated by Resources.
     * @return The 'create' ModelAndView containing the Resource-populated Model.
     */
    /*
    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public ModelAndView createTemplate(Model model) {
        model.addAllAttributes(resourceManager.loadResources());
        model.addAttribute("maxUploadSize", RosettaProperties.getMaxUploadSize(servletContext));
        return new ModelAndView("create");
    }
    */

    /**
     * Accepts a GET request for template creation, fetches resource information
     * from file system and inject that data into the Model to be used in the
     * View. Returns the view that walks the user through the steps of template
     * creation.
     *
     * @param model The Model object to be populated by Resources.
     * @return The 'create' ModelAndView containing the Resource-populated Model.
     */
    /*
    @RequestMapping(value = "/createRegex", method = RequestMethod.GET)
    public ModelAndView createRegexTemplate(Model model) {
        model.addAllAttributes(resourceManager.loadResources());
        model.addAttribute("maxUploadSize", RosettaProperties.getMaxUploadSize(servletContext));
        return new ModelAndView("createRegex");
    }
    */

    /**
     * Accepts a GET request for autoconversion of a known file, fetches resource information
     * from file system and inject that data into the Model to be used in the
     * View. Returns the view that walks the user through the steps of template
     * creation.
     *
     * @param model The Model object to be populated by Resources.
     * @return The 'create' ModelAndView containing the Resource-populated Model.
     */
    /*
    @RequestMapping(value = "/autoConvert", method = RequestMethod.GET)
    public ModelAndView autoConvertKnownFile(Model model) {
        model.addAllAttributes(resourceManager.loadResources());
        model.addAttribute("maxUploadSize", RosettaProperties.getMaxUploadSize(servletContext));
        return new ModelAndView("autoConvert");
    }
    */

    /**
     * Accepts a GET request for workflow to augment metadata of a known file,
     * fetches resource information from file system and injects that data into
     * the Model to be used in the View
     * Returns the view that walks the user through the steps of template
     * creation.
     *
     * @param model The Model object to be populated by Resources.
     * @return The 'create' ModelAndView containing the Resource-populated Model.
     */
    @RequestMapping(value = "/augmentMetadata", method = RequestMethod.GET)
    public ModelAndView augmentMetadata(Model model) {
        model.addAllAttributes(resourceManager.loadResources());
        return new ModelAndView("augmentMetadata");
    }

    /**
     * Accepts a GET request for template creation, fetches resource information
     * from file system and inject that data into the Model to be used in the
     * View. Returns the view that walks the user through the steps of template
     * creation.
     *
     * @param model The Model object to be populated by Resources.
     * @return The 'create' ModelAndView containing the Resource-populated Model.
     */
    @RequestMapping(value = "/createTrajectory", method = RequestMethod.GET)
    public ModelAndView createTrajectoryTemplate(Model model) {
        model.addAllAttributes(resourceManager.loadResources());
        return new ModelAndView("createTrajectory");
    }

    /**
     * Accepts a GET request for template restoration, fetches resource
     * information from file system and inject that data into the Model to be
     * used in the View. Returns the view that walks the user through the steps
     * of template creation.
     *
     * @param model The Model object to be populated by Resources.
     * @return The 'create' ModelAndView containing the Resource-populated Model.
     */
    /*
    @RequestMapping(value = "/restore", method = RequestMethod.GET)
    public ModelAndView restoreTemplate(Model model) {
        BasicConfigurator.configure();
        model.addAllAttributes(resourceManager.loadResources());
        model.addAttribute("maxUploadSize", RosettaProperties.getMaxUploadSize(servletContext));
        return new ModelAndView("restore");
    }
    */




    /**
     * Accepts a GET request to see if an already uploaded file contains blank lines.
     *
     * @param fileName The UploadedFile form backing object containing the file.
     * @param uniqueId The UploadedFile form backing object containing the file.     *
     * @return The number of blank lines in the file.
     */
    /*
    @RequestMapping(value = "/getBlankLines", method = RequestMethod.GET)
    @ResponseBody
    public String getBlankLines(@RequestParam("fileName") String fileName, @RequestParam("uniqueId") String uniqueId) {
        int blankLineCount = 0;
        String filePath = FilenameUtils.concat(getUploadDir(), uniqueId);
        File uploadedFile = new File(FilenameUtils.concat(filePath, fileName));
        blankLineCount = fileParserManager.getBlankLines(uploadedFile);
        return String.valueOf(blankLineCount);
    }
*/

    /**
     * Accepts a POST request for an uploaded file, stores that file to disk, auto converts the
     * file, and, returns a zip file of the converted dataset ASCII file.
     *
     * @param file    The UploadedFile form backing object containing the file.
     * @param request The HttpServletRequest with which to glean the client IP address.
     * @return A String of the local file name for the ASCII file (or null for an error).
     */
    /*
    @RequestMapping(value = "/autoConvertKnownFile", method = RequestMethod.POST)
    @ResponseBody
    public String autoConvertKnownFile(UploadedAutoconvertFile file, HttpServletRequest request) {
        List<String> convertedFiles = new ArrayList<>();
        String uniqueId = file.getUniqueId();
        String convertTo = file.getConvertTo();
        String convertFrom = file.getConvertFrom();

        String returnFile = null;

        String filePath = FilenameUtils.concat(getUploadDir(), uniqueId);
        String fileName = file.getFileName();
        String fullFileName = FilenameUtils.concat(filePath, fileName);

        String downloadDir = FilenameUtils.concat(getDownloadDir(), uniqueId);
        File downloadFileDir = new File(downloadDir);
        if (!downloadFileDir.exists()) {
            downloadFileDir.mkdir();
        }

        try {
            if (convertFrom != null) {
                if (convertFrom.equals("esc")) {
                    EolSoundingComp escConvertor = new EolSoundingComp();
                    convertedFiles = escConvertor.convert(fullFileName);
                    if (!convertedFiles.isEmpty()) {
                        logger.info("Success");
                    }
                } else if (convertFrom.equals("tuff")) {
                    TagUniversalFileFormat tuff = new TagUniversalFileFormat();
                    tuff.parse(fullFileName);
                    String fullFileNameExt = FilenameUtils.getExtension(fullFileName);
                    String ncfile = fileName.replace(fullFileNameExt, "nc");
                    ncfile = FilenameUtils.concat(downloadDir, ncfile);
                    returnFile = tuff.convert(ncfile);
                    //returnFile = ncfile;
                }
            }
        } catch (Exception e) {
            logger.error("A file conversion error has occurred: " + e.getMessage());
            return null;
        }

        if (convertedFiles.size() >= 1) {
            String zipFileName = FilenameUtils.concat(downloadDir, "converted_files.zip");
            ZipFileUtil zippedConvertedFiles = new ZipFileUtil(zipFileName);
            zippedConvertedFiles.addAllToZip(convertedFiles);
            returnFile = zipFileName;
        }

        returnFile = returnFile.replaceAll(downloadDir + "/", "");

        return returnFile;
    }

*/
    /**
     * Accepts a POST request Parse the uploaded file. The methods gets called
     * at various times and the file data is parsed according the data it
     * contains.
     *
     * @param file   The AsciiFile form backing object.
     * @param result The BindingResult object for errors.
     * @return A String of the local file name for the ASCII file.
     */
    /*
    @RequestMapping(value = "/parse", method = RequestMethod.POST)
    @ResponseBody
    public String parseFile(AsciiFile file, BindingResult result) {

        String filePath = FilenameUtils.concat(getUploadDir(), file.getUniqueId());

        filePath = FilenameUtils.concat(filePath, file.getFileName());

        // SCENARIO 1: no header lines yet (return the file contents to display in grid)
        if (file.getHeaderLineList().isEmpty()) {
            return fileParserManager.parseByLine(filePath);
        } else {
            // SCENARIO 2: header lines and delimiters available
            if (!file.getDelimiterList().isEmpty()) {
                List<String> delimiterList = file.getDelimiterList();
                String selectedDelimiter = delimiterList.get(0);

 */
                /**
                 * // Time for some validation
                 * fileValidator.validateList(delimiterList, result);
                 * fileValidator.validateList(file.getHeaderLineList(), result);
                 * fileValidator.validateDelimiterCount(filePath, file, result);
                 *
                 * if (result.hasErrors()) { List<ObjectError> validationErrors
                 * = result.getAllErrors(); Iterator<ObjectError> iterator =
                 * validationErrors.iterator(); while (iterator.hasNext()) {
                 * logger.error("Validation Error: " +
                 * iterator.next().toString()); } return null;
                 *
                 * } else {
                 **/

                /*
                String normalizedFileData = fileParserManager.normalizeDelimiters(filePath, selectedDelimiter, delimiterList, file.getHeaderLineList());
                if (file.getVariableNameMap().isEmpty()) {
                    return StringEscapeUtils.escapeHtml4(selectedDelimiter + "\n" + normalizedFileData);
                } else {
                    // SCENARIO 3: we have variable data!

                    // pull out sessionStorage data
                    HashMap<String, HashMap<String, String>> mm = file.getVariableMetadataMap();
                    HashMap<String, String> coords = new HashMap<String, String>();

                    // fix where session storage where necessairy
                    // FIXME: hardcoded stuff that should not be in here?
                    if (file.getCfType().equals("trajectory")) {
                        // only the time variable should be a coordinate variable, so
                        // clean up session storage to reflect that.

                        for (String mmKey : mm.keySet()) {
                            if (mm.get(mmKey).containsKey("_coordinateVariableType")) {
                                HashMap<String, String> tmp = mm.get(mmKey);
                                String cvt = tmp.get("_coordinateVariableType").toString();
                                boolean chk1 = cvt.equals("relTime");
                                boolean chk2 = cvt.equals("fullDateTime");
                                boolean chk3 = cvt.equals("dateOnly");
                                boolean chk4 = cvt.equals("timeOnly");
                                if (!(chk1 | chk2 | chk3 | chk4)) {
                                    coords.put(cvt, file.getVariableNameMap().get(mmKey.replace("Metadata", "")));
                                    tmp.remove("_coordinateVariableType");
                                    tmp.put("_coordinateVariable", "non-coordinate");
                                    mm.put(mmKey, tmp);
                                }
                            }
                            file.setOtherInfo(coords);
                        }
                    }
                    List<List<String>> parseFileData = fileParserManager
                            .getParsedFileData();
                    List<String> header = fileParserManager.getHeader();

                    // Create the NCML file using the file data
                    String downloadDir = FilenameUtils.concat(getDownloadDir(),
                            file.getUniqueId());

                    // create JSON file that holds sessionStorage information
                    // Should be using file.getJsonStrSessionStorage()
                    // use jsonUtil.strToJson(file.getJsonStrSessionStorage()) to remove
                    // connection specific metadata, like uniqueid,
                    Boolean isQuickSave = false;
                    String userFile = file.getFileName();
                    String jsonFileName = getTemplateFileName(userFile,
                            isQuickSave);

                    String jsonOut = FilenameUtils.concat(downloadDir,
                            FilenameUtils.getFullPath(file.getFileName()));
                    jsonOut = FilenameUtils.concat(jsonOut, jsonFileName);
                    JsonUtil jsonUtil = new JsonUtil(jsonOut);
                    //JSONObject jsonObj = jsonUtil.ssHashMapToJson(mm);
                    JSONObject jsonObj = jsonUtil.strToJson(file.getJsonStrSessionStorage());
                    jsonObj.remove("uniqueId");
                    jsonObj.remove("fileName");
                    //jsonObj.remove("varCoords");
                    jsonUtil.writeJsonToFile(jsonObj);

                    // zip JSON and NcML files
                    // String zipFileName = FilenameUtils.concat(downloadDir,
                    // "rosetta.zip");
                    // ZipFileUtil transactionZip = new
                    // ZipFileUtil(zipFileName);
                    // String[] sourceFiles = {jsonOut};
                    // transactionZip.addAllToZip(sourceFiles);
                    // String netcdfFile = null;

                    String netcdfFile = null;
                    try {
                        NetcdfFileManager dsgWriter;
                        for (NetcdfFileManager potentialDsgWriter : netcdfFileManager.asciiToDsg()) {
                            if (potentialDsgWriter.isMine(file.getCfType())) {
                                netcdfFile = potentialDsgWriter.createNetcdfFile(file,
                                        parseFileData, header, downloadDir);
                                break;
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Caught IOException: "
                                + e.getMessage());
                        return netcdfFile;
                    } catch (IllegalArgumentException e) {
                        //FIXME: possible security issue with revealing too much?
                        return e.getMessage();
                    }

                    String fileOut = FilenameUtils.concat(downloadDir,
                            FilenameUtils.removeExtension(file.getFileName())
                                    + ".nc");

                    String escapedDownloadDir = StringEscapeUtils.escapeJava(downloadDir + File.separator);
                    return fileOut.replaceAll(escapedDownloadDir, "") + "\n"
                            + jsonOut.replaceAll(escapedDownloadDir, "");
                }
                // }
            } else {
                logger.error("File header lines are present, but no delimiters have been specified.");
                return null;
            }
        }
    }
*/

    /**
     * Accepts a POST request Get global metadata from the uploaded file.
     *
     * @param file    The AsciiFile form backing object.
     * @param request The BindingResult object for errors.
     * @return A String of the local file name for the ASCII file.
     */
    /*
    @RequestMapping(value = "/getMetadataKnownFile", method = RequestMethod.POST)
    @ResponseBody
    public String getMetadataKnownFile(UploadedAutoconvertFile file, HttpServletRequest request) {
        String uniqueId = file.getUniqueId();
        String convertFrom = file.getConvertFrom();

        String metadataStr = "";

        String filePath = FilenameUtils.concat(getUploadDir(), uniqueId);
        String fileName = file.getFileName();
        String fullFileName = FilenameUtils.concat(filePath, fileName);

        String downloadDir = FilenameUtils.concat(getDownloadDir(), uniqueId);
        File downloadFileDir = new File(downloadDir);
        if (!downloadFileDir.exists()) {
            downloadFileDir.mkdir();
        }
        HashMap<String, String> globalMetadata = new HashMap<String, String>();
        // get metadata
        if (convertFrom != null) {
            // tag base archive flat file
            if (convertFrom.equals("tuff")) {
                TagUniversalFileFormat tuffConverter = new TagUniversalFileFormat();
                tuffConverter.parse(fullFileName);
                globalMetadata = tuffConverter.getGlobalMetadata();
            }

        }
        StringBuilder sb = new StringBuilder();
        for (String key : globalMetadata.keySet()) {
            sb.append(key + ":" + globalMetadata.get(key).trim().replace("\"", "") + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        metadataStr = sb.toString();
        // need to return something like this:
        // "title:title here,description:des here,institution:inst here,dataAuthor:data aut here,version:version here,dataSource:source here"
        return metadataStr;
    }
*/
    /**
     * Accepts a POST request to restore session from an NcML file,
     *
     * @param file The UploadedFile form backing object containing the file.
     * @return A String of the local file name for the ASCII file (or null for an error).
     */
    /*
    @RequestMapping(value = "/restoreFromZip", method = RequestMethod.POST)
    @ResponseBody
    public String processZip(UploadedFile file) {
        String jsonStrSessionStorage = null;
        String filePath = FilenameUtils.concat(getUploadDir(),
                file.getUniqueId());
        filePath = FilenameUtils.concat(filePath, file.getFileName());
        if (filePath.endsWith(".zip")) {
            ZipFileUtil restoreZip = new ZipFileUtil(filePath);
            jsonStrSessionStorage = restoreZip
                    .readFileFromZip("rosettaSessionStorage.json");
        } else if (filePath.endsWith(".template")) {
            InputStream inStream = null;
            jsonStrSessionStorage = "";
            String line;
            try {
                inStream = new FileInputStream(filePath);
                BufferedReader buffReader = new BufferedReader(
                        new InputStreamReader(inStream));
                while ((line = buffReader.readLine()) != null) {
                    jsonStrSessionStorage = jsonStrSessionStorage + line;
                }
                buffReader.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonStrSessionStorage;
    }
*/
    /**
     * Accepts a POST request to to initiate a quick save (download a temp save
     * template)
     *
     * @return A String of the local file name for the ASCII file (or null for an error).
     */
    /*
    @RequestMapping(value = "/QuickSave", method = RequestMethod.POST)
    @ResponseBody
    public String quickSave(String jsonStrSessionStorage) {
        Boolean isQuickSave = true;
        Map<String, String> infoForDownload = new HashMap<>();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObj = null;
        try {
            jsonObj = (JSONObject) jsonParser.parse(jsonStrSessionStorage);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String userFileName = jsonObj.get("fileName").toString();
        String jsonFileName = getTemplateFileName(userFileName, isQuickSave);

        // map to be returned to front end to construct download request
        infoForDownload.put("uniqueId", jsonObj.get("uniqueId").toString());
        infoForDownload.put("fileName", jsonFileName);

        // crete JSON file that holds sessionStorage information
        String downloadDir = FilenameUtils.concat(getDownloadDir(), jsonObj
                .get("uniqueId").toString());

        String jsonOut = FilenameUtils.concat(downloadDir, jsonFileName);

        jsonObj.remove("uniqueId");
        jsonObj.remove("fileName");

        JsonUtil jsonUtil = new JsonUtil(jsonOut);
        jsonUtil.writeJsonToFile(jsonObj);
        // return enough info to download the template file
        String returnString = JSONObject.toJSONString(infoForDownload);
        return returnString;
    }
    */

/*
    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    @ResponseBody
    public String publish(PublisherInfo publisherObj) {
        BasicConfigurator.configure();

        String userId = publisherObj.getUserName();
        String passwd = publisherObj.getAuth();
        HashMap<String, String> pubMap = (HashMap<String, String>) publisherObj
                .getPublisherInfoMap();
        String server = pubMap.get("pubUrl");
        String parent = pubMap.get("pubDest");

        String entryName = publisherObj.getGeneralMetadataMap().get("title");
        String entryDescription = publisherObj.getGeneralMetadataMap().get(
                "description");

        String downloadDir = FilenameUtils.concat(getDownloadDir(),
                publisherObj.getUniqueId());
        String ncFileName = FilenameUtils.removeExtension(publisherObj
                .getFileName()) + ".nc";
        String filePath = FilenameUtils.concat(downloadDir, ncFileName);
        String msg = "";
        boolean success;
        try {
            if (server.toLowerCase().contains("cadis")) {
                AcadisGatewayProjectReader projectReader = new AcadisGatewayProjectReader(
                        parent);
                parent = projectReader.getDatasetShortName();
                AcadisGateway pub = new AcadisGateway(server, userId, passwd,
                        parent, filePath);
                success = pub.publish();
                if (success) {
                    msg = pub.getGatewayProjectUrl();
                } else {
                    System.err.println("Failed to publish to ACADIS Gateway");
                }
            }
        } catch (Exception e) {
            msg = e.getMessage();
        }

        return msg;
    }
*/

    /**
     * Accepts a GET request to download a file from the download directory.
     *
     * @param uniqueID the sessions unique ID.
     * @param fileName the name of the file the user wants to download from the download directory
     * @return IOStream of the file requested.
     */
    /*
    @RequestMapping(value = "/fileDownload/{uniqueID}/{file:.*}", method = RequestMethod.GET)
    public void fileDownload(@PathVariable(value = "uniqueID") String uniqueID,
                             @PathVariable(value = "file") String fileName,
                             HttpServletResponse response) {
        String relFileLoc = uniqueID + File.separator + fileName;
        String fullFilePath = FilenameUtils
                .concat(getDownloadDir(), relFileLoc);
        File requestedFile = new File(fullFilePath);
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(requestedFile);
            String ext = FilenameUtils.getExtension(fileName);
            String contentType = "text/plain";
            if (ext.equals("template")) {
                contentType = "application/rosetta";
            } else if (ext.equals("nc")) {
                contentType = "application/x-netcdf";
            } else if (ext.equals("zip")) {
                contentType = "application/zip";
            }
            response.setHeader("Content-Type", contentType);
            // copy it to response's OutputStream
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }
*/
    /**
     * Attempts to get the client IP address from the request.
     *
     * @param request The HttpServletRequest.
     * @return The client's IP address.
     */
    /*
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
*/
    /**
     * Creates a unique id for the file name from the clients IP address and the
     * date.
     *
     * @param request The HttpServletRequest.
     * @return The unique file name id.
     */
    /*
    private String createUniqueId(HttpServletRequest request) {
        String id = String.valueOf(new Date().hashCode());
        String ipAddress = getIpAddress(request);
        if (ipAddress != null) {
            id = ipAddress + id;
        } else {
            id = String.valueOf(new Random().nextInt()) + id;
        }
        return id.replaceAll(":", "_");
    }

    private String getTemplateFileName(String userFileName, Boolean isQuickSave) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd_HHmmss")
                .format(new Date());
        String jsonFileName = FilenameUtils.removeExtension(userFileName);
        jsonFileName = jsonFileName + "-Rosetta_" + currentDate;
        jsonFileName = jsonFileName + ".template";
        if (isQuickSave) {
            jsonFileName = "QuickSave_" + jsonFileName;
        }
        return jsonFileName;
    }
    */

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
            StringWriter errors = new StringWriter();
            exception.printStackTrace(new PrintWriter(errors));
            message = "An error has occurred: "
                    + exception.getClass().getName() + ":"
                    + errors;
        }
        // log it
        logger.error(message);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("message", message);
        ModelAndView modelAndView = new ModelAndView("fatalError", model);
        return modelAndView;
    }

}
