package edu.ucar.unidata.rosetta.controller;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.service.DataManager;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.validators.CFTypeValidator;
import edu.ucar.unidata.rosetta.service.validators.FileValidator;


import java.io.IOException;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;


/**
 * Main controller for Rosetta application.
 */
@Controller
public class WizardController{

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
            data = dataManager.lookupById(rosettaCookie.getValue());
        else
            data = new Data();

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
     * Accepts a POST request from CF type selection step of the wizard. Collects the CF type data
     * entered by the user and validates it.  If it passes validation, the data is written to the
     * database and the user is redirected to file upload step.  Otherwise, the user is taken back
     * to Cf type selection step to correct the invalid data.
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

        if (result.hasErrors()) {   // validation errors
            logger.error("Validation errors detected in CF type form data.");
            model.addAttribute("error", result.getGlobalError().getDefaultMessage());
            // Add current step to the Model (used by view to keep track of where we are in the wizard).
            model.addAttribute("currentStep", "cfType");
            // Add domains data to Model (for platform display).
            model.addAttribute("domains", resourceManager.loadResources().get("domains"));
            // Add platforms data to Model (for platform selection).
            model.addAttribute("platforms", resourceManager.loadResources().get("platforms"));
            return new ModelAndView("wizard");
        } else {
            // Persist the data.
            dataManager.persistData(data, request);
            response.addCookie(new Cookie("rosetta", data.getId()));
            return new ModelAndView(new RedirectView("/fileUpload", true));
        }
    }


    /**
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
        // Add domains data to Model (for file upload  display based on community type).
        model.addAttribute("domains", resourceManager.loadResources().get("domains"));
        // Add file type data to Model (for file type selection if cfType was directly specified).
        model.addAttribute("fileTypes", resourceManager.loadResources().get("fileTypes"));
        return new ModelAndView("wizard");
    }


    /**
     * Accepts a POST request from file upload step of the wizard. Collects the dataFileType and uploaded 
     * files entered by the user and validates them.  If they passes validation, the files are written to 
     * the file system, the dataFileType is written to the database and the user is redirected to the next
     * step. Otherwise, the user is taken back to file upload step to correct the invalid data.
     *
     * If the user clicked the previous button, they are redirected back to CF type selection step.
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     */
    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    public ModelAndView processFileUpload(Data data, BindingResult result, Model model, HttpServletRequest request) throws IOException {

        // Take user back to Step 1 (and don't save any data submitted in file upload step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/cfType", true));

        if (result.hasErrors()) {   // validation errors
            logger.error("Validation errors detected in file upload form data.");
            model.addAttribute("error", result.getGlobalError().getDefaultMessage());
            // Add current step to the Model (used by view to keep track of where we are in the wizard).
            model.addAttribute("currentStep", "1");
            // Add a list of all steps to the Model for rendering left nav menu.
            model.addAttribute("steps", resourceManager.loadResources().get("steps"));
            // Add domains data to Model (for platform display).
            model.addAttribute("domains", resourceManager.loadResources().get("domains"));
            // Add file type data to Model (for file type selection if cfType was directly specified).
            model.addAttribute("fileTypes", resourceManager.loadResources().get("fileTypes"));
            return new ModelAndView("wizard");

        } else {

            // Get the cookie.
            Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
            // Create a Data form-backing object.
            Data persistedData;
            if (rosettaCookie != null)
                // Data persisted in the database.
                persistedData = dataManager.lookupById(rosettaCookie.getValue());
            else
                // Something has gone wrong.  We shouldn't be at this step without having persisted data.
                throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

            // Add the user-provided data from file upload step to the persistedData object.
            persistedData.setDataFileType(data.getDataFileType());
            String dataFileName = data.getDataFileName();

            // Write data file to disk.
            dataManager.writeUploadedFileToDisk(persistedData.getId(), dataFileName, data.getDataFile());
            String dataFileNameExtension = FilenameUtils.getExtension(dataFileName);
            if (dataFileNameExtension.equals("xls") || dataFileNameExtension.equals("xlsx"))
                dataFileName = dataManager.convertToCSV(persistedData.getId(), dataFileName);
            persistedData.setDataFileName(dataFileName);

            // Write positional file to disk if it exists.
            if (!data.getPositionalFileName().equals("")) {
                String positionalFileName = data.getPositionalFileName();
                String positionalFileNameExtension = FilenameUtils.getExtension(positionalFileName);
                if (positionalFileNameExtension.equals("xls") || positionalFileNameExtension.equals("xlsx"))
                    positionalFileName = dataManager.convertToCSV(persistedData.getId(), positionalFileName);
                persistedData.setDataFileName(positionalFileName);
            }

            // Write template file to disk if it exists.
            if (!data.getTemplateFileName().equals("")) {
                String templateFileName = data.getTemplateFileName();
                String templateFileNameExtension = FilenameUtils.getExtension(templateFileName);
                if (templateFileName.equals("xls") || templateFileNameExtension.equals("xlsx"))
                    templateFileName = dataManager.convertToCSV(persistedData.getId(), templateFileName);
                persistedData.setDataFileName(templateFileName);
            }

            // Persist the new data.
            dataManager.updateData(persistedData);
            return new ModelAndView(new RedirectView("/customFileTypeAttributes", true));
        }
    }


    /**
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
            // User-provided fileType info already exists.  Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

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
            // Using a known data file type, so go directly to custom file type attribue collection step.
            return new ModelAndView(new RedirectView("/variableMetadata", true));
        }
    }

    /**
     * Accepts a POST request from custom file type attribute collection step of the wizard. Collects 
     * the custom file type attributes entered by the user and validates them.  If they passes validation, 
     * the data is written to the database and the user is redirected to the next step. Otherwise, the user 
     * is taken back to file upload step to correct the invalid data.
     *
     * If the user clicked the previous button, they are redirected back to file upload step.
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     */
    @RequestMapping(value = "/customFileTypeAttributes", method = RequestMethod.POST)
    public ModelAndView processCustomFileTypeAttributes(Data data, BindingResult result, Model model, HttpServletRequest request) {

        // Take user back to file upload step (and don't save any data submitted in custom file type attribute collection step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/convertAndDownload", true));

        // Persist the new data.
        // dataManager.updateData(data);
        return new ModelAndView(new RedirectView("/variableMetadata", true));
    }

    /**
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
        if (rosettaCookie != null)
            // User-provided fileType info already exists.  Populate Data object with info.
            data = dataManager.lookupById(rosettaCookie.getValue());
        else
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException("No persisted data available for file upload step.  Check the database & the cookie.");

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
     * Accepts a POST request from variable metadata collection step of the wizard. Collects 
     * the variable metadata entered by the user and validates it.  If it passes validation,
     * the data is written to the database and the user is redirected to the next step. Otherwise,
     * the user is taken back to the variable metadata collection step to correct the invalid data.
     *
     * If the user clicked the previous button, they are redirected back to either the custom file
     * type attribute collection step or the file upload step (depending on if they specified a custom
     * file type).
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     */
    @RequestMapping(value = "/variableMetadata", method = RequestMethod.POST)
    public ModelAndView processVariableMetadata(Data data, BindingResult result, Model model, HttpServletRequest request) {

        // Take user back to custom file type attribute collection step (and don't save any data submitted in variable metadata collection step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/customFileTypeAttributes", true));

        // Persist the new data.
        // dataManager.updateData(data);
        return new ModelAndView(new RedirectView("/generalMetadata", true));


    }

    /**
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
     * Accepts a POST request from general metadata collection step of the wizard. Collects
     * the general metadata entered by the user and validates it.  If it passes validation,
     * the data is written to the database and the user is redirected to the next step. Otherwise,
     * the user is taken back to the general metadata collection step to correct the invalid data.
     *
     * If the user clicked the previous button, they are redirected back to variable metadata
     * collection step.
     *
     * @param data      The form-backing object.
     * @param result    The BindingResult for error handling.
     * @param model     The Model object to be populated by file upload data in the next step.
     * @param request   HttpServletRequest needed to get the cookie.
     * @return          Redirect to next step.
     */
    @RequestMapping(value = "/generalMetadata", method = RequestMethod.POST)
    public ModelAndView processGeneralMetadata(Data data, BindingResult result, Model model, HttpServletRequest request) {

        // Take user back to variable metadata collection step (and don't save any data submitted in general metadata collection step).
        if (data.getSubmit().equals("Previous"))
            return new ModelAndView(new RedirectView("/variableMetadata", true));

        // Persist the new data.
        // dataManager.updateData(data);
        return new ModelAndView(new RedirectView("/convertAndDownload", true));


    }

    /**
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
}
