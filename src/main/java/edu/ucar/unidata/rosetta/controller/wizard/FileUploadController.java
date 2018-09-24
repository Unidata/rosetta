/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.wizard.UploadedFileManager;
import edu.ucar.unidata.rosetta.service.wizard.WizardManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling file uploads.
 *
 * @author oxelson@ucar.edu
 */
@Controller
public class FileUploadController implements HandlerExceptionResolver {

    private static final Logger logger = Logger.getLogger(FileUploadController.class);

    private final ServletContext servletContext;

    @Resource(name = "resourceManager")
    private ResourceManager resourceManager;

    @Resource(name = "uploadedFileManager")
    private UploadedFileManager uploadedFileManager;

    @Resource(name = "wizardManager")
    private WizardManager wizardManager;

    @Autowired
    public FileUploadController(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Accepts a GET request for access to file upload step of the wizard.
     *
     * @param model   The Model object to be populated.
     * @param request HttpServletRequest needed to get the cookie.
     * @return View and the Model for the wizard to process.
     * @throws IllegalStateException If cookie is null.
     */
    @RequestMapping(value = "/fileUpload", method = RequestMethod.GET)
    public ModelAndView displayFileUploadForm(Model model, HttpServletRequest request) {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        if (rosettaCookie == null) {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException(
                    "No persisted data available for file upload step.  Check the database & the cookie.");
        }

        // Look up CF type data entered in prior wizard step & add to model. (Needed for display logic in wizard step).
        WizardData wizardData = wizardManager.lookupPersistedWizardDataById(rosettaCookie.getValue());
        model.addAttribute("community", wizardData.getCommunity());
        model.addAttribute("cfType", wizardData.getCfType());

        // Create the form backing object.
        UploadedFileCmd uploadedFileCmd;
        // Have we been here before?  Do we have any data persisted?
        UploadedFileCmd persisted = uploadedFileManager.lookupPersistedDataById(rosettaCookie.getValue());
        if (!persisted.getUploadedFiles().isEmpty()) {
            uploadedFileCmd = persisted;
        } else {
            // No persisted data.
            uploadedFileCmd = new UploadedFileCmd();
        }
        // Add command object to Model.
        model.addAttribute("command", "uploadedFileCmd");
        // Add form-backing object.
        model.addAttribute("uploadedFileCmd", uploadedFileCmd);
        // Add current step to the Model.
        model.addAttribute("currentStep", "fileUpload");
        // Add whether we need to show the custom file attributes step in the wizard menu.
        model.addAttribute("customFileAttributesStep",  wizardManager.customFileAttributesStep(rosettaCookie.getValue()));
        // Add community data to Model (for file upload display based on community type).
        model.addAttribute("communities", resourceManager.getCommunities());
        // Add file type data to Model (for file type selection if CF type was directly specified).
        model.addAttribute("fileTypes", resourceManager.getFileTypes());

        // The currentStep variable will determine which jsp frag to load in the wizard.
        return new ModelAndView("wizard");
    }

    /**
     * Accepts a POST request from file upload step of the wizard. Processes the submitted data and
     * persists it to the database.  Redirects user to next step or previous step depending on
     * submitted form button (Next or Previous).
     *
     * @param uploadedFileCmd The form-backing object.
     * @param submit          The value sent via the submit button.
     * @param result          The BindingResult for error handling.
     * @param model           The Model object to be populated by file upload data in the next step.
     * @param request         HttpServletRequest needed to get the cookie.
     * @return Redirect to next step.
     * @throws IllegalStateException If cookie is null.
     * @throws RosettaFileException  If unable to write file(s) to disk or a file conversion exception
     *                               occurred.
     */
    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    public ModelAndView processFileUpload(@ModelAttribute("uploadedFileCmd") UploadedFileCmd uploadedFileCmd,
                                          @RequestParam("submit") String submit, BindingResult result, Model model, HttpServletRequest request) throws RosettaFileException {

        // Take user back to the CF type selection step (and don't save any data to this step).
        if (submit != null && submit.equals("Previous")) {
            return new ModelAndView(new RedirectView("/cfType", true));
        }

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie == null) {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException(
                    "No persisted data available for file upload step.  Check the database & the cookie.");
        }

        // Persist the file upload data.
        uploadedFileManager.processFileUpload(rosettaCookie.getValue(), uploadedFileCmd);

        // Depending on what the user entered for the data file, we may need to
        // add an extra step to collect data associated with that custom file type.
        String nextStep = wizardManager.processNextStep(rosettaCookie.getValue());

        // Send user to the next view.
        return new ModelAndView(new RedirectView(nextStep, true));
    }

    /**
     * This method gracefully handles any uncaught exception that are fatal in nature and unresolvable
     * by the user.
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
