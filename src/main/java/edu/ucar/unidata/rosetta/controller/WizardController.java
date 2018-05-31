package edu.ucar.unidata.rosetta.controller;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.service.*;

import java.io.File;
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

import ucar.ma2.InvalidRangeException;

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

    @Resource(name = "convertManager")
    private ConvertManager convertManager;

    @Resource(name = "metadataManager")
    private MetadataManager metadataManager;

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
    public ModelAndView displayCFTypeSelectionForm(Model model, HttpServletRequest request){


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
        model.addAttribute("currentStep", "cfType");
        // Add communities data to Model (for platform display).
        model.addAttribute("domains", dataManager.getCommunitiesForView());
        // Add platforms data to Model (for platform selection).
        model.addAttribute("platforms", dataManager.getPlatformsForView());
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
        if (rosettaCookie != null)
            // Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model.
        model.addAttribute("currentStep", "fileUpload");
        // Add community data to Model (for file upload display based on community type).
        model.addAttribute("domains", dataManager.getCommunitiesForView());
        // Add file type data to Model (for file type selection if cfType was directly specified).
        model.addAttribute("fileTypes", dataManager.getFileTypesForView());
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


        // Take user back to the CF type selection step (and don't save any data to this step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/cfType", true));

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie == null)
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // Persist the file upload data.
        String nextStep = dataManager.processFileUpload(rosettaCookie.getValue(), data);

        // Send user to the next view.  (See dataManager.processFileUpload).
        return new ModelAndView(new RedirectView(nextStep, true));
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
        if (rosettaCookie != null)
            // Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

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

        // Take user back to file upload step (and don't save any data to this step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/fileUpload", true));

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        Data persistedData;
        if (rosettaCookie != null)
            // Get the persisted data.
            persistedData = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

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
        if (rosettaCookie != null)
            // Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // Populate with any existing variable metadata.
        data.setVariableMetadata(metadataManager.getMetadataStringForClient(data.getId(), "variable"));

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

        // Take user back to custom file attribute collection step (and don't save any data to this step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/customFileTypeAttributes", true));

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        Data persistedData;
        if (rosettaCookie != null)
            // Get the persisted data.
            persistedData = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

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
    public ModelAndView displayGeneralMetadataForm(Model model, HttpServletRequest request) throws RosettaDataException {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        // Create a Data form-backing object.
        Data data;
        if (rosettaCookie != null)
            // Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        // Add data object to Model.
        model.addAttribute("data", data);

        GeneralMetadata metadata = new GeneralMetadata();

        // If not a custom file type, ot probably already includes metadata, so get it.
        if (!data.getDataFileType().equals("Custom_File_Type"))
            metadata = metadataManager.getMetadataFromKnownFile(FilenameUtils.concat(FilenameUtils.concat(dataManager.getUploadDir(), data.getId()), data.getDataFileName()), data.getDataFileType(), metadata);


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
    public ModelAndView processGeneralMetadata(Data data, GeneralMetadata metadata, BindingResult result, Model model, HttpServletRequest request) throws RosettaDataException, IOException, InvalidRangeException {

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        Data persistedData;
        if (rosettaCookie != null)
            // Get the persisted data.
            persistedData = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

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

        //Convert
        String netcdfFile = convertManager.convertToNetCDF(persistedData);

        // Update persisted data.
        persistedData.setNetcdfFile(netcdfFile);
        dataManager.updateData(persistedData);
        // Add current step to the Model.
        model.addAttribute("currentStep", "generalMetadata");
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
        if (rosettaCookie != null)
            // Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

        String[] pathParts;
        if (File.separator.equals("\\" )) {
            pathParts = data.getNetcdfFile().split("\\\\");
        } else {
            pathParts = data.getNetcdfFile().split(File.separator);
        }
        String netcdfFileName = pathParts[pathParts.length - 2] + File.separator + pathParts[pathParts.length -1];

        data.setNetcdfFile(netcdfFileName);
        dataManager.updateData(data);

        // Add data object to Model.
        model.addAttribute("data", data);
        // Add current step to the Model.
        model.addAttribute("fileName", pathParts[pathParts.length -1]);
        model.addAttribute("currentStep", "downloadAndConvert");
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
