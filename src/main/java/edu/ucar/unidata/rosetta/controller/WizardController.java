package edu.ucar.unidata.rosetta.controller;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.service.DataManager;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.service.validators.CFTypeValidator;
import edu.ucar.unidata.rosetta.service.validators.FileValidator;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
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
public class WizardController implements HandlerExceptionResolver {

    protected static Logger logger = Logger.getLogger(WizardController.class);

    @Autowired
    ServletContext servletContext;

    @Resource(name = "dataManager")
    private DataManager dataManager;

    @Resource(name = "resourceManager")
    private ResourceManager resourceManager;


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
        // Persist the data.
        dataManager.persistData(data, request);
        response.addCookie(new Cookie("rosetta", data.getId()));
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

        // Take user back to the CF type selection step (and don't save any data submitted in file upload step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/cfType", true));

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        Data persistedData;
        if (rosettaCookie != null) {
            // Combine user-provided data with persisted data.
            persistedData = dataManager.populateDataObjectWithInput(rosettaCookie.getValue(), data);
        } else {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");
        }

        // Write data file to disk.
        String dataFileName = data.getDataFileName();
        dataManager.writeUploadedFileToDisk(persistedData.getId(), dataFileName, data.getDataFile());

        // If the uploaded file was .xls or .xlsx, convert it to .csv
        String dataFileNameExtension = FilenameUtils.getExtension(dataFileName);
        if (dataFileNameExtension.equals("xls") || dataFileNameExtension.equals("xlsx"))
            dataFileName = dataManager.convertToCSV(persistedData.getId(), dataFileName);
        persistedData.setDataFileName(dataFileName); // Updated the file name to have the .csv extension.

        // Write positional file to disk if it exists.
        if (!data.getPositionalFileName().equals("")) {
            String positionalFileName = data.getPositionalFileName();
            // If the uploaded file was .xls or .xlsx, convert it to .csv
            String positionalFileNameExtension = FilenameUtils.getExtension(positionalFileName);
            if (positionalFileNameExtension.equals("xls") || positionalFileNameExtension.equals("xlsx"))
                positionalFileName = dataManager.convertToCSV(persistedData.getId(), positionalFileName);
            persistedData.setDataFileName(positionalFileName); // Updated the file name to have the .csv extension.
        }

        // Write template file to disk if it exists.
        if (!data.getTemplateFileName().equals("")) {
            String templateFileName = data.getTemplateFileName();
            // If the uploaded file was .xls or .xlsx, convert it to .csv
            String templateFileNameExtension = FilenameUtils.getExtension(templateFileName);
            if (templateFileName.equals("xls") || templateFileNameExtension.equals("xlsx"))
                templateFileName = dataManager.convertToCSV(persistedData.getId(), templateFileName);
            persistedData.setDataFileName(templateFileName); // Updated the file name to have the .csv extension.
        }

        // Persist the data!
        dataManager.updateData(persistedData);
        return new ModelAndView(new RedirectView("/customFileTypeAttributes", true));
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
            // Add a list of all steps to the Model for rendering left nav menu.
            model.addAttribute("steps", resourceManager.loadResources().get("steps"));
            // Add domains data to Model (for file upload  display based on community type).
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
    public ModelAndView processCustomFileTypeAttributes(Data data, BindingResult result, Model model, HttpServletRequest request) throws RosettaDataException {

        logger.info("submitted for custom file type: " + data.toString());
        // Take user back to file upload step (and don't save any data submitted in custom file type attribute collection step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/convertAndDownload", true));

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        Data persistedData;
        if (rosettaCookie != null) {
            // Combine user-provided data with persisted data.
            persistedData = dataManager.populateDataObjectWithInput(rosettaCookie.getValue(), data);
        } else {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");
        }

        // Handle boolean values of the Data object that are not handled in dataManager.populateDataObjectWithInput
        if (data.getNoHeaderLines()) {
            persistedData.setNoHeaderLines(true);
            persistedData.setHeaderLineNumbers(null);
        } else {
            persistedData.setNoHeaderLines(false);
        }

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
    public ModelAndView displayVariableMetadataForm(Model model, HttpServletRequest request) {

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

        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model.
        model.addAttribute("currentStep", "variableMetadata");
        // Add a list of all steps to the Model for rendering left nav menu.
        model.addAttribute("steps", resourceManager.loadResources().get("steps"));
        // Add domains data to Model (for file upload  display based on community type).
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

        // Take user back to custom file type attribute collection step (and don't save any data submitted in variable metadata collection step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/customFileTypeAttributes", true));


        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        Data persistedData;
        if (rosettaCookie != null)
            // Data persisted in the database.
            persistedData = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        logger.info("persisted data: " + data.toString());

        // Persist the new data.
        // dataManager.updateData(data);
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
        //if (rosettaCookie != null)
        // User-provided cfType info already exists.  Populate Data object with info.
        //    data = dataManager.lookupById(rosettaCookie.getValue());
        //else
        data = new Data();



        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model.
        model.addAttribute("currentStep", "generalMetadata");
        // Add a list of all steps to the Model for rendering left nav menu.
        model.addAttribute("steps", resourceManager.loadResources().get("steps"));
        // Add domains data to Model (for file upload  display based on community type).
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
    public ModelAndView processGeneralMetadata(Data data, BindingResult result, Model model, HttpServletRequest request) {

        logger.info("submitted for general metadata: " + data.toString());

        // Take user back to variable metadata collection step (and don't save any data submitted in general metadata collection step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/variableMetadata", true));


        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        Data persistedData;
        if (rosettaCookie != null)
            // Data persisted in the database.
            persistedData = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        logger.info("persisted data: " + data.toString());

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
        return new ModelAndView(new RedirectView("/variableMetadata", true));


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
