package edu.ucar.unidata.rosetta.controller;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.service.DataManager;
import edu.ucar.unidata.rosetta.service.MetadataManager;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.exceptions.RosettaDataException;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
public class WizardController implements HandlerExceptionResolver {

    protected static Logger logger = Logger.getLogger(WizardController.class);

    @Autowired
    ServletContext servletContext;

    @Resource(name = "dataManager")
    private DataManager dataManager;

    @Resource(name = "metadataManager")
    private MetadataManager metadataManager;

    @Resource(name = "resourceManager")
    private ResourceManager resourceManager;


    // Validators
    /*
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
    */

    /**
     * STEP 1: display CF type form.
     * Accepts a GET request for access to CF type selection step of the wizard.
     *
     * @param model  The Model object to be populated.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return  View and the Model for the wizard to process.
     */
    @RequestMapping(value = "/cfType", method = RequestMethod.GET)
    public ModelAndView displayCFTypeSelectionForm(Model model, HttpServletRequest request) {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        // Create a Data form-backing object.
        Data data;
        if (rosettaCookie != null) {
            // User-provided cfType info already exists.  Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
            logger.info("pulled from db to pop cf type: " + data.toString());
        } else {
            data = new Data();
        }

        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model (used by view to keep track of where we are in the wizard).
        model.addAttribute("currentStep", "cfType");
        // Add domains data to Model (for platform display).
        model.addAttribute("domains", resourceManager.loadResources().get("domains"));
        // Add platforms data to Model (for platform selection).
        model.addAttribute("platforms", resourceManager.loadResources().get("platforms"));
        // Add data object to Model.
        return new ModelAndView("wizard");
    }

    /**
     * STEP 1: process CF type form data.
     * Accepts a POST request from CF type selection step of the wizard. Processes the
     * submitted data and persists it to the database.  Redirects user to next step or
     * previous step depending on submitted form button (Next or Previous).
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated.
     * @param request   HttpServletRequest needed to pass to the dataManager to get client IP.
     * @param response  HttpServletResponse needed for setting cookie.
     * @return          Redirect to next step.
     */
    @RequestMapping(value = "/cfType", method = RequestMethod.POST)
    public ModelAndView processCFType(Data data, BindingResult result, Model model, HttpServletRequest request, HttpServletResponse response) {

        logger.info("submitted for cfType: " + data.toString());

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie != null) { // We've been here before, combine new with previous persisted data.
            // Get the persisted data.
            Data persistedData = dataManager.lookupById(rosettaCookie.getValue());
            logger.info("data pulled from db: " + persistedData.toString());

            // Update platform if needed.
            persistedData.setPlatform(data.getPlatform()); // If updating a previous value.
            // Update community if needed.
            persistedData.setCommunity(resourceManager.getCommunity(data.getPlatform()));
            // Update CF type.
            persistedData.setCfType(data.getCfType());
            // Update persisted the data!
            dataManager.updateData(persistedData);

            logger.info("persisted data: " + data.toString());


        } else { // Haven't been before, so proceed with creation of entry in the db and cookie.
            // Persist the data.
            dataManager.persistData(data, request);
            logger.info("persisted data: " + data.toString());
            // First time posting to this page in this session.
            response.addCookie(new Cookie("rosetta", data.getId()));
        }
        return new ModelAndView(new RedirectView("/fileUpload", true));
    }


    /**
     * STEP 2: display file upload form.
     * Accepts a GET request for access to file upload step of the wizard.
     *
     * @param model  The Model object to be populated.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return  View and the Model for the wizard to process.
     */
    @RequestMapping(value = "/fileUpload", method = RequestMethod.GET)
    public ModelAndView displayFileUploadForm(Model model, HttpServletRequest request) {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        // Create a Data form-backing object.
        Data data;
        if (rosettaCookie != null) {
            // Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
            logger.info("pulled from db to pop file upload: " + data.toString());
        } else {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");
        }
        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model.
        model.addAttribute("currentStep", "fileUpload");
        // Add domains data to Model (for file upload  display based on community type).
        model.addAttribute("domains", resourceManager.loadResources().get("domains"));
        // Add file type data to Model (for file type selection if cfType was directly specified).
        model.addAttribute("fileTypes", resourceManager.loadResources().get("fileTypes"));
        return new ModelAndView("wizard");
    }


    /**
     * STEP 2: process file upload form data.
     * Accepts a POST request from file upload step of the wizard. Processes the
     * submitted data and persists it to the database.  Redirects user to next step
     * or previous step depending on submitted form button (Next or Previous).
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     */
    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    public ModelAndView processFileUpload(Data data, BindingResult result, Model model, HttpServletRequest request) throws IOException, RosettaDataException{

        logger.info("submitted for file upload: " + data.toString());

        // Take user back to the CF type selection step (and don't save any data to this step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/cfType", true));

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        Data persistedData;
        if (rosettaCookie != null) {
            // Get the persisted data.
            persistedData = dataManager.lookupById(rosettaCookie.getValue());
            logger.info("data pulled from db: " + persistedData.toString());
        } else {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");
        }

        // If a data file has been uploaded.
        if (!data.getDataFile().isEmpty()) {
            // Set the data file type.
            persistedData.setDataFileType(data.getDataFileType());
            // Write data file to disk.
            String dataFileName = data.getDataFileName();
            dataManager.writeUploadedFileToDisk(persistedData.getId(), dataFileName, data.getDataFile());
            // If the uploaded file was .xls or .xlsx, convert it to .csv
            String dataFileNameExtension = FilenameUtils.getExtension(dataFileName);
            if (dataFileNameExtension.equals("xls") || dataFileNameExtension.equals("xlsx"))
                dataFileName = dataManager.convertToCSV(persistedData.getId(), dataFileName);
            // Set the data file name.
            persistedData.setDataFileName(dataFileName);
        } else {
            logger.info("setting data file type");
            persistedData.setDataFileType(data.getDataFileType());
        }

        // If a positional file has been uploaded.
        if (!data.getPositionalFile().isEmpty()) {
            String positionalFileName = data.getPositionalFileName();
            // Write file to disk.
            dataManager.writeUploadedFileToDisk(persistedData.getId(), positionalFileName, data.getPositionalFile());
            // If the uploaded file was .xls or .xlsx, convert it to .csv
            String positionalFileNameExtension = FilenameUtils.getExtension(positionalFileName);
            if (positionalFileNameExtension.equals("xls") || positionalFileNameExtension.equals("xlsx"))
                positionalFileName = dataManager.convertToCSV(persistedData.getId(), positionalFileName);
            // Set the positional file name.
            persistedData.setPositionalFileName(positionalFileName);
        } else {
            // no file and no file name, user is 'undoing' the upload.
            if (data.getPositionalFileName().equals("")) {
                persistedData.setPositionalFileName(null);
            }
        }

        // If a template file has been uploaded.
        if (!data.getTemplateFile().isEmpty()) {
            String templateFileName = data.getTemplateFileName();
            // Write file to disk.
            dataManager.writeUploadedFileToDisk(persistedData.getId(), templateFileName, data.getTemplateFile());
            // If the uploaded file was .xls or .xlsx, convert it to .csv
            String templateFileNameExtension = FilenameUtils.getExtension(templateFileName);
            if (templateFileName.equals("xls") || templateFileNameExtension.equals("xlsx"))
                templateFileName = dataManager.convertToCSV(persistedData.getId(), templateFileName);
            // Set the template file name.
            persistedData.setTemplateFileName(templateFileName);
        } else {
            // no file and no file name, user is 'undoing' the upload.
            if (data.getTemplateFileName().equals("")) {
                persistedData.setTemplateFileName(null);
            }
        }
        // Persist the data!
        dataManager.updateData(persistedData);

        /*
         Depending on what the user entered for the data file, we may need to
         add an extra step to collect data associated with that custom file type.
         */
        if(persistedData.getDataFileType().equals("Custom_File_Type")) {
            // Custom file type selected; send user to view to collect data about the custom file type.
            return new ModelAndView(new RedirectView("/customFileTypeAttributes", true));
        } else {
            // Known file type selected; send user to general metadata step.
            return new ModelAndView(new RedirectView("/generalMetadata", true));
        }
    }


    /**
     * STEP 3: display custom file attribute form.
     * Accepts a GET request for access to custom file type attribute collection step of the wizard.
     *
     * @param model  The Model object to be populated.
     * @return  View and the Model for the wizard to process.
     */
    @RequestMapping(value = "/customFileTypeAttributes", method = RequestMethod.GET)
    public ModelAndView displayCustomFileTypeAttributesForm(Model model, HttpServletRequest request) throws IOException {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        // Create a Data form-backing object.
        Data data;
        if (rosettaCookie != null) {
            // User-provided fileType info already exists.  Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
            logger.info("pulled from db to pop custom file attr: " + data.toString());
        } else {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");
        }
        // If the custom data file was specified by the user, we need to add an additional step in the wizard.
        if (data.getDataFileType().equals("Custom_File_Type")) {
            // Add data object to Model.
            model.addAttribute("data", data);
            // Add current step to the Model.
            model.addAttribute("currentStep", "customFileTypeAttributes");
            // Add parsed file data in JSON string format (to sho win the SlickGrid).
            model.addAttribute("parsedData", dataManager.parseDataFileByLine(data.getId(),data.getDataFileName()));
            return new ModelAndView("wizard");
        } else {
            // Using a known data file type, so go directly to variable metadata collection step.
            return new ModelAndView(new RedirectView("/variableMetadata", true));
        }
    }

    /**
     * STEP 3: process custom file attribute form data.
     * Accepts a POST request from custom file type attribute collection step of the wizard.
     * Processes the submitted data and persists it to the database.  Redirects user to next
     * step or previous step depending on submitted form button (Next or Previous).
     *
     * STEP 3 is only accessed/processed when the user uploads a 'custom' data file type (specified
     * during prior step).  Otherwise, if they upload a known data type, they are taken directly to
     * STEP 4.
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     */
    @RequestMapping(value = "/customFileTypeAttributes", method = RequestMethod.POST)
    public ModelAndView processCustomFileTypeAttributes(Data data, BindingResult result, Model model, HttpServletRequest request) {

        logger.info("submitted for custom file type: " + data.toString());

        // Take user back to file upload step (and don't save any data to this step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/fileUpload", true));

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        Data persistedData;
        if (rosettaCookie != null) {
            // Get the persisted data.
            persistedData = dataManager.lookupById(rosettaCookie.getValue());
            logger.info("data pulled from db: " + persistedData.toString());
        } else {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");
        }

        // Handle boolean values of the Data object for header lines.
        if (data.getNoHeaderLines()) {
            // set no header lines.
            persistedData.setNoHeaderLines(true);
            // Remove any previously persisted headerlines.
            persistedData.setHeaderLineNumbers(null);
        } else {
            // Set header lines.
            persistedData.setNoHeaderLines(false);
            persistedData.setHeaderLineNumbers(data.getHeaderLineNumbers());
        }
        // Set delimiter.
        persistedData.setDelimiter(data.getDelimiter());

        // Persist the data!
        dataManager.updateData(persistedData);
        return new ModelAndView(new RedirectView("/variableMetadata", true));
    }

    /**
     * STEP 4: display form.
     * Accepts a GET request for access to variable metadata collection step of the wizard.
     *
     * @param model  The Model object to be populated.
     * @return  View and the Model for the wizard to process.
     */
    @RequestMapping(value = "/variableMetadata", method = RequestMethod.GET)
    public ModelAndView displayVariableMetadataForm(Model model, HttpServletRequest request) throws IOException {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        // Create a Data form-backing object.
        Data data;
        if (rosettaCookie != null) {
            // Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
            logger.info("pulled from db to pop variable metadata: " + data.toString());
        } else {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");
        }

        // Populate with any existing variable metadata.
        data.setVariableMetadata(metadataManager.getMetadataStringForClient(data.getId(), "variable"));




        //TO DO: PARSE STANDARD DATA TYPE FILES (AND SUPPLEMENTAL FILES) AND SEND TO CLIENT BASED ON FILE TYPE
        //data.setVariableMetadata("variableName0<>year<=>variableName0Metadata<>_coordinateVariable:coordinate,_coordinateVariableType:dateOnly,dataType:Integer,long_name:ddd,units:fff,standard_name:aaa<=>variableName1<>foo<=>variableName1Metadata<>_coordinateVariable:non-coordinate,dataType:Float,source:sss,missing_value:ddd,long_name:fff,units:ggg,valid_min:ggg<=>variableName2<>bar<=>variableName2Metadata<>_coordinateVariable:coordinate,dataType:Text,long_name:sss,units:ddd,standard_name:fff,time_leap_month:yes<=>variableName3<>Do Not Use<=>variableName4<>Do Not Use<=>variableName5<>Do Not Use<=>variableName6<>Do Not Use<=>variableName7<>Do Not Use<=>variableName8<>Do Not Use<=>variableName9<>Do Not Use");

        data.setVariableMetadata("variableName0<>Timestamp<=>variableName0Metadata<>_coordinateVariable:coordinate,_coordinateVariableType:fullDateTime,dataType:Text,long_name:Full timestamp,units:yyyy-MM-ddThh\\:mm\\:ss.sTZD,units:yyyy-MM-ddThh\\:mm\\:ss.sTZDMM/dd/yyyy HH\\:mm\\:ss<=>variableName1<>dBar<=>variableName1Metadata<>_coordinateVariable:non-coordinate,dataType:Integer,dataType:Float,source:foo,missing_value:bar,long_name:baz,units:who knows<=>variableName2<>Light_at_depth<=>variableName2Metadata<>_coordinateVariable:non-coordinate,dataType:Integer,source:light at depth,missing_value:foo,long_name:bar,units:baz<=>variableName3<>Internal Temp<=>variableName3Metadata<>_coordinateVariable:non-coordinate,dataType:Float,source:temp,missing_value:foo,long_name:bar,units:degree_Celsius,source:internal temp<=>variableName4<>External Temp<=>variableName4Metadata<>_coordinateVariable:non-coordinate,dataType:Float,source:temp,missing_value:foo,long_name:bar,units:degree_Celsius,source:internal temp,source:external temp");
        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model.
        model.addAttribute("currentStep", "variableMetadata");
        // Add parsed file data in JSON string format (to show in the SlickGrid).
        model.addAttribute("parsedData", dataManager.parseDataFileByLine(data.getId(),data.getDataFileName()));
        // Add delimiter to do additional client-side parsing for SlickGrid.
        model.addAttribute("delimiterSymbol", dataManager.getDelimiterSymbol(data.getDelimiter()));
        return new ModelAndView("wizard");
    }


    /**
     * STEP 4: process form data.
     * Accepts a POST request from variable metadata collection step of the wizard. Processes
     * the submitted data and persists it to the database.  Redirects user to next step or
     * previous step depending on submitted form button (Next or Previous).
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     */
    @RequestMapping(value = "/variableMetadata", method = RequestMethod.POST)
    public ModelAndView processVariableMetadata(Data data, BindingResult result, Model model, HttpServletRequest request) {

        logger.info("submitted for variable metadata: " + data.toString());

        // Take user back to custom file attribute collection step (and don't save any data to this step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/customFileTypeAttributes", true));

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        Data persistedData;
        if (rosettaCookie != null) {
            // Get the persisted data.
            persistedData = dataManager.lookupById(rosettaCookie.getValue());
            logger.info("data pulled from db: " + persistedData.toString());
        } else {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");
        }

        // Persist the variable metadata.
        metadataManager.persistMetadata(metadataManager.parseVariableMetadata(data.getVariableMetadata(), persistedData.getId()));

        return new ModelAndView(new RedirectView("/generalMetadata", true));
    }

    /**
     * STEP 5: display form.
     * Accepts a GET request for access to general metadata collection step of the wizard.
     *
     * @param model  The Model object to be populated.
     * @return  View and the Model for the wizard to process.
     */
    @RequestMapping(value = "/generalMetadata", method = RequestMethod.GET)
    public ModelAndView displayGeneralMetadataForm(Model model, HttpServletRequest request) {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        // Create a Data form-backing object.
        Data data;
        if (rosettaCookie != null) {
            // Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
            logger.info("pulled from db to pop global metadata: " + data.toString());
        } else {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");
        }

        // Add data object to Model.
        model.addAttribute("data", data);

            // OIIP stuff.
            GeneralMetadata metadata = new GeneralMetadata();
            metadata.setSpecies_capture("sddssa");
            metadata.setSpeciesTSN_capture("sddssa");
            metadata.setLength_type_capture("sddssa");
            metadata.setLength_method_capture("sddssa");
            metadata.setCondition_capture("sddssa");
            metadata.setLength_recapture("sddssa");
            metadata.setLength_unit_recapture("sddssa");
            metadata.setLength_type_recapture("sddssa");
            metadata.setLength_method_recapture("sddssa");
            metadata.setAttachment_method("sddssa");
            metadata.setLon_release("sddssa");
            metadata.setLat_release("sddssa");
            metadata.setPerson_tagger_capture("sddssa");
            metadata.setDatetime_release("sddssa");
            metadata.setDevice_type("sddssa");
            metadata.setManufacturer("sddssa");
            metadata.setModel("sddssa");
            metadata.setSerial_number("sddssa");
            metadata.setDevice_name("sddssa");
            metadata.setPerson_owner("sddssa");
            metadata.setOwner_contact("sddssa");
            metadata.setFirmware("sddssa");
            metadata.setEnd_details("sddssa");
            metadata.setDatetime_end("sddssa");
            metadata.setLon_end("sddssa");
            metadata.setLat_end("sddssa");
            metadata.setEnd_type("sddssa");
            metadata.setProgramming_software("sddssa");
            metadata.setProgramming_report("sddssa");
            metadata.setFound_problem("sddssa");
            metadata.setPerson_qc("sddssa");
            metadata.setWaypoints_source("sddssa");

            model.addAttribute("generalMetadata", metadata);


        // Add current step to the Model.
        model.addAttribute("currentStep", "generalMetadata");
        return new ModelAndView("wizard");
    }


    /**
     * STEP 5: process form data
     * Accepts a POST request from general metadata collection step of the wizard. Processes
     * the submitted data and persists it to the database.  Redirects user to next step or
     * previous step depending on submitted form button (Next or Previous).
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     */
    @RequestMapping(value = "/generalMetadata", method = RequestMethod.POST)
    public ModelAndView processGeneralMetadata(Data data, GeneralMetadata metadata, BindingResult result, Model model, HttpServletRequest request) throws RosettaDataException{

        logger.info("submitted for general metadata: " + data.toString().toLowerCase() );

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        Data persistedData;
        if (rosettaCookie != null) {
            // Get the persisted data.
            persistedData = dataManager.lookupById(rosettaCookie.getValue());
            logger.info("data pulled from db: " + persistedData.toString());
        } else {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");
        }

        // The previous step depends on what the user specified for the data file type.
        if (data.getSubmit().equals("Previous")) {
            if(persistedData.getDataFileType().equals("Custom_File_Type"))
                // Take user back to variable metadata collection step (and don't save any data to this step).
                return new ModelAndView(new RedirectView("/variableMetadata", true));
            else
                // Take user back to file upload step (and don't save any data to this step).
                return new ModelAndView(new RedirectView("/fileUpload", true));
        }

        // Persist the global metadata.
        metadataManager.persistMetadata(metadataManager.parseGeneralMetadata(metadata, persistedData.getId()));

        // Persist the new data.
        // dataManager.updateData(data);
        return new ModelAndView(new RedirectView("/convertAndDownload", true));
    }

    /**
     * STEP 6: display form.
     * Accepts a GET request for access to convert and download step of the wizard.
     *
     * @param model  The Model object to be populated.
     * @return  View and the Model for the wizard to process.
     */
    @RequestMapping(value = "/convertAndDownload", method = RequestMethod.GET)
    public ModelAndView displayConvertedFileDownloadPage(Model model, HttpServletRequest request) {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        // DELETE COOKIE
        // Create a Data form-backing object.
        Data data;
        //if (rosettaCookie != null)
        // User-provided cfType info already exists.  Populate Data object with info.
        //    data = dataManager.lookupById(rosettaCookie.getValue());
        //else
        data = new Data();

        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model.
        model.addAttribute("currentStep", "convertAndDownload");
        // Add a list of all steps to the Model for rendering left nav menu.
        model.addAttribute("steps", resourceManager.loadResources().get("steps"));
        // Add domains data to Model (for file upload  display based on community type).
        return new ModelAndView("wizard");
    }


    /**
     * STEP 6: process form data.
     * Accepts a POST request from convert and download step of the wizard. If the user clicked
     * the previous button, they are redirected back to general metadata collection step.
     *
     * @param data      The form-backing object.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     */
    @RequestMapping(value = "/convertAndDownload", method = RequestMethod.POST)
    public ModelAndView processReturnToPriorPageRequest(Data data, Model model, HttpServletRequest request) {
        return new ModelAndView(new RedirectView("/generalMetadata", true));
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
