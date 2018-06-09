package edu.ucar.unidata.rosetta.controller;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.exceptions.*;
import edu.ucar.unidata.rosetta.service.*;

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

import ucar.ma2.InvalidRangeException;

/**
 * Main controller for Rosetta application.
 */
@Controller
public class WizardController implements HandlerExceptionResolver {

    private static final Logger logger = Logger.getLogger(WizardController.class);

    private final ServletContext servletContext;

    @Resource(name = "dataManager")
    private DataManager dataManager;

    @Autowired
    public WizardController(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

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
        if (rosettaCookie != null)
            // User-provided cfType info already exists.  Populate Data object with info.
            data = dataManager.lookupPersistedDataById(rosettaCookie.getValue());
        else
            data = new Data();

        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model (used by view to keep track of where we are in the wizard).
        model.addAttribute("currentStep", "cfType");
        // Add communities data to Model (for platform display).
        model.addAttribute("domains", dataManager.getCommunitiesForView());
        // Add platforms data to Model (for platform selection).
        model.addAttribute("platforms", dataManager.getPlatformsForView());

        model.addAttribute("communities", dataManager.getCommunities());

        // The currentStep variable will determine which jsp frag to load in the wizard.
        return new ModelAndView("wizard");
    }

    /**
     * Accepts a GET request for access to convert and download step of the wizard.
     *
     * @param model  The Model object to be populated.
     * @return  View and the Model for the wizard to process.
     * @throws InvalidRangeException If unable to convert the data file to netCDF.
     * @throws RosettaFileException If unable to create the template file from the Data object.
     */
    @RequestMapping(value = "/convertAndDownload", method = RequestMethod.GET)
    public ModelAndView displayConvertedFileDownloadPage(Model model, HttpServletRequest request) throws InvalidRangeException, RosettaFileException{

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        if (rosettaCookie == null)
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // Create a Data form-backing object.
        Data data = dataManager.lookupPersistedDataById(rosettaCookie.getValue());

        // Convert the uploaded file to netCDF & create a template for future conversions.
        data = dataManager.convertToNetCDF(data);

        // Add data object to Model.
        model.addAttribute("data", data);

        // Add current step to the Model.
        model.addAttribute("currentStep", "convertAndDownload");

        // The currentStep variable will determine which jsp frag to load in the wizard.
        return new ModelAndView("wizard");
    }

    /**
     * Accepts a GET request for access to custom file type attribute collection step of the wizard.
     *
     * @param model  The Model object to be populated.
     * @return  View and the Model for the wizard to process.
     * @throws IllegalStateException  If cookie is null.
     * @throws RosettaFileException  For any file I/O or JSON conversions problems while parsing data.
     */
    @RequestMapping(value = "/customFileTypeAttributes", method = RequestMethod.GET)
    public ModelAndView displayCustomFileTypeAttributesForm(Model model, HttpServletRequest request) throws RosettaFileException, IllegalStateException {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        if (rosettaCookie == null)
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // Create a Data form-backing object.
        Data data = dataManager.lookupPersistedDataById(rosettaCookie.getValue());


        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model.
        model.addAttribute("currentStep", "customFileTypeAttributes");
        // Add parsed file data in JSON string format (to sho win the SlickGrid).
        model.addAttribute("parsedData", dataManager.parseDataFileByLine(data.getId(),data.getDataFileName()));

        // The currentStep variable will determine which jsp frag to load in the wizard.
        return new ModelAndView("wizard");

    }

    /**
     * Accepts a GET request for access to file upload step of the wizard.
     *
     * @param model  The Model object to be populated.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return  View and the Model for the wizard to process.
     * @throws IllegalStateException  If cookie is null.
     */
    @RequestMapping(value = "/fileUpload", method = RequestMethod.GET)
    public ModelAndView displayFileUploadForm(Model model, HttpServletRequest request) {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        if (rosettaCookie == null)
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // Create a Data form-backing object.
        Data data = dataManager.lookupPersistedDataById(rosettaCookie.getValue());

        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model.
        model.addAttribute("currentStep", "fileUpload");
        // Add community data to Model (for file upload display based on community type).
        model.addAttribute("domains", dataManager.getCommunitiesForView());
        // Add file type data to Model (for file type selection if cfType was directly specified).
        model.addAttribute("fileTypes", dataManager.getFileTypesForView());

        // The currentStep variable will determine which jsp frag to load in the wizard.
        return new ModelAndView("wizard");
    }

    /**
     * Accepts a GET request for access to general metadata collection step of the wizard.
     *
     * @param model  The Model object to be populated.
     * @return  View and the Model for the wizard to process.
     * @throws RosettaDataException If unable to populate the GeneralMetadata object.
     * @throws IllegalStateException  If cookie is null.
     */
    @RequestMapping(value = "/generalMetadata", method = RequestMethod.GET)
    public ModelAndView displayGeneralMetadataForm(Model model, HttpServletRequest request) throws RosettaDataException {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        if (rosettaCookie == null)
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // Create a Data form-backing object.
        Data data = dataManager.lookupPersistedDataById(rosettaCookie.getValue());

        // Add data object to Model.
        model.addAttribute("data", data);

        GeneralMetadata metadata = new GeneralMetadata();

        // Mine the data file for any included metadata.
        metadata = dataManager.getMetadataFromKnownFile(FilenameUtils.concat(FilenameUtils.concat(dataManager.getUploadDir(), data.getId()), data.getDataFileName()), data.getDataFileType(), metadata);

        model.addAttribute("generalMetadata", metadata);

        // Add current step to the Model.
        model.addAttribute("currentStep", "generalMetadata");

        // The currentStep variable will determine which jsp frag to load in the wizard.
        return new ModelAndView("wizard");
    }

    /**
     * Accepts a GET request for access to variable metadata collection step of the wizard.
     *
     * @param model  The Model object to be populated.
     * @return  View and the Model for the wizard to process.
     * @throws IllegalStateException  If cookie is null.
     * @throws RosettaFileException  For any file I/O or JSON conversions problems while parsing data.
     */
    @RequestMapping(value = "/variableMetadata", method = RequestMethod.GET)
    public ModelAndView displayVariableMetadataForm(Model model, HttpServletRequest request) throws RosettaFileException {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        if (rosettaCookie == null)
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // Create a Data form-backing object.
        Data data = dataManager.lookupPersistedDataById(rosettaCookie.getValue());

        // Populate with any existing variable metadata.
        data.setVariableMetadata(dataManager.getMetadataStringForClient(data.getId(), "variable"));

        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model.
        model.addAttribute("currentStep", "variableMetadata");
        // Add parsed file data in JSON string format (to show in the SlickGrid).
        model.addAttribute("parsedData", dataManager.parseDataFileByLine(data.getId(),data.getDataFileName()));
        // Add delimiter to do additional client-side parsing for SlickGrid.
        model.addAttribute("delimiterSymbol", dataManager.getDelimiterSymbol(data.getDelimiter()));

        // The currentStep variable will determine which jsp frag to load in the wizard.
        return new ModelAndView("wizard");
    }

    /**
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

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie != null) {
            // We've been here before, combine new with previous persisted data.
            dataManager.processCfType(rosettaCookie.getValue(), data, null);
        } else {
            // Haven't been before, so proceed with persist the data.
            dataManager.processCfType(null, data, request);
            // First time posting to this page in this session.
            response.addCookie(new Cookie("rosetta", data.getId()));
        }
        return new ModelAndView(new RedirectView("/fileUpload", true));
    }

    /**
     * Accepts a POST request from convert and download step of the wizard. The only purpose of
     * this method is to capture if the user clicked the previous button, in which case they
     * are redirected back to general metadata collection step.
     *
     * @return  Redirect to previous step.
     */
    @RequestMapping(value = "/convertAndDownload", method = RequestMethod.POST)
    public ModelAndView processConvertAndDownload() {
        return new ModelAndView(new RedirectView("/generalMetadata", true));
    }

    /**
     * Accepts a POST request from custom file type attribute collection step of the wizard.
     * Processes the submitted data and persists it to the database.  Redirects user to next
     * step or previous step depending on submitted form button (Next or Previous).
     *
     * This step is only accessed/processed when the user uploads a 'custom' data file type (specified
     * during prior step).  Otherwise, if they upload a known data type, they are taken directly to
     * general metadata collection step.
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     * @throws IllegalStateException  If cookie is null.
     */
    @RequestMapping(value = "/customFileTypeAttributes", method = RequestMethod.POST)
    public ModelAndView processCustomFileTypeAttributes(Data data, BindingResult result, Model model, HttpServletRequest request) {

        // Take user back to file upload step (and don't save any data to this step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/fileUpload", true));

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie == null)
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // Persist the custom data file information.
        dataManager.processCustomFileTypeAttributes(rosettaCookie.getValue(), data);

        // Send user to next step to collect variable metadata.
        return new ModelAndView(new RedirectView("/variableMetadata", true));
    }

    /**
     * Accepts a POST request from file upload step of the wizard. Processes the
     * submitted data and persists it to the database.  Redirects user to next step
     * or previous step depending on submitted form button (Next or Previous).
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     * @throws IllegalStateException  If cookie is null.
     * @throws RosettaFileException If unable to write file(s) to disk or a file conversion exception occurred.
     */
    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    public ModelAndView processFileUpload(Data data, BindingResult result, Model model, HttpServletRequest request) throws RosettaFileException {

        // Take user back to the CF type selection step (and don't save any data to this step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/cfType", true));

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie == null)
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // Persist the file upload data.
        dataManager.processFileUpload(rosettaCookie.getValue(), data);

        // Depending on what the user entered for the data file, we may need to
        // add an extra step to collect data associated with that custom file type.
        String nextStep = dataManager.processNextStep(rosettaCookie.getValue());

        // Send user to the next view.  (See dataManager.processFileUpload).
        return new ModelAndView(new RedirectView(nextStep, true));
    }

    /**
     * Accepts a POST request from general metadata collection step of the wizard. Processes
     * the submitted data and persists it to the database.  Redirects user to next step or
     * previous step depending on submitted form button (Next or Previous).
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     * @throws IllegalStateException  If cookie is null.
     * @throws RosettaDataException  If unable to populate the metadata object.
     */
    @RequestMapping(value = "/generalMetadata", method = RequestMethod.POST)
    public ModelAndView processGeneralMetadata(Data data, GeneralMetadata metadata, BindingResult result, Model model, HttpServletRequest request) throws RosettaDataException {

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie == null)
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // The previous step depends on what the user specified for the data file type.
        if (data.getSubmit().equals("Previous")) {
            String previousStep = dataManager.processPreviousStep(rosettaCookie.getValue());
            // Take user back to previous step & don't save data submitted to this step. (See dataManager.processPreviousStep).
            return new ModelAndView(new RedirectView(previousStep, true));
        }

        // Persist the general metadata information.
        dataManager.processGeneralMetadata(rosettaCookie.getValue(), metadata);

        // Send user to next step to download the converted file(s).
        return new ModelAndView(new RedirectView("/convertAndDownload", true));
    }

    /**
     * Accepts a POST request from variable metadata collection step of the wizard. Processes
     * the submitted data and persists it to the database.  Redirects user to next step or
     * previous step depending on submitted form button (Next or Previous).
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     * @throws IllegalStateException  If cookie is null.
     */
    @RequestMapping(value = "/variableMetadata", method = RequestMethod.POST)
    public ModelAndView processVariableMetadata(Data data, BindingResult result, Model model, HttpServletRequest request) {

        // Take user back to custom file attribute collection step (and don't save any data to this step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/customFileTypeAttributes", true));

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie == null)
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // Persist the variable metadata information.
        dataManager.processVariableMetadata(rosettaCookie.getValue(), data);

        // Send user to next step to collect general metadata.
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
        // log it
        logger.error(message);
        Map<String, Object> model = new HashMap<>();
        model.put("message", message);
        return new ModelAndView("fatalError", model);
    }

}
