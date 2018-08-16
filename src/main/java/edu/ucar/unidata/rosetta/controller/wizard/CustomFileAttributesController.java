/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.wizard.DataManager;
import edu.ucar.unidata.rosetta.service.wizard.WizardManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
 * Controller for collecting custom file type metadata.
 *
 * @author oxelson@ucar.edu
 */
@Controller
public class CustomFileAttributesController implements HandlerExceptionResolver {

    private static final Logger logger = Logger.getLogger(CustomFileAttributesController.class);

    private final ServletContext servletContext;

    @Resource(name = "resourceManager")
    private ResourceManager resourceManager;

    @Resource(name = "wizardManager")
    private WizardManager wizardManager;

    @Autowired
    public CustomFileAttributesController(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Accepts a GET request for access to custom file type attribute collection step of the wizard.
     *
     * @param model The Model object to be populated.
     * @return View and the Model for the wizard to process.
     * @throws IllegalStateException If cookie is null.
     * @throws RosettaFileException  For any file I/O or JSON conversions problems while parsing data.
     */
    @RequestMapping(value = "/customFileTypeAttributes", method = RequestMethod.GET)
    public ModelAndView displayCustomFileTypeAttributesForm(Model model, HttpServletRequest request)
            throws RosettaFileException, IllegalStateException {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        if (rosettaCookie == null) {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException(
                    "No persisted data available for custom file attributes step.  Check the database & the cookie.");
        }

        WizardData wizardData = wizardManager.lookupPersistedWizardDataById(rosettaCookie.getValue());

        // Add command object to Model.
        model.addAttribute("command", "WizardData");
        // Add form-backing object.
        model.addAttribute("data", wizardData);
        // Add current step to the Model.
        model.addAttribute("currentStep", "customFileTypeAttributes");
        // Add delimiters to Model.
        model.addAttribute("delimiters", resourceManager.getDelimiters());
        // Add parsed file data in JSON string format (to sho win the SlickGrid).
        model.addAttribute("parsedData",
                wizardManager.parseDataFileByLine(rosettaCookie.getValue()));

        // The currentStep variable will determine which jsp frag to load in the wizard.
        return new ModelAndView("wizard");

    }

    /**
     * Accepts a POST request from custom file type attribute collection step of the wizard. Processes
     * the submitted data and persists it to the database.  Redirects user to next step or previous
     * step depending on submitted form button (Next or Previous).
     * <p>
     * This step is only accessed/processed when the user uploads a 'custom' data file type (specified
     * during prior step).  Otherwise, if they upload a known data type, they are taken directly to
     * general metadata collection step.
     *
     * @param wizardData The form-backing object.
     * @param submit     The value sent via the submit button.
     * @param result     The BindingResult for error handling.
     * @param model      The Model object to be populated by file upload data in the next step.
     * @param request    HttpServletRequest needed to get the cookie.
     * @return Redirect to next step.
     * @throws IllegalStateException If cookie is null.
     */
    @RequestMapping(value = "/customFileTypeAttributes", method = RequestMethod.POST)
    public ModelAndView processCustomFileTypeAttributes(WizardData wizardData, @RequestParam("submit") String submit, BindingResult result, Model model,
                                                        HttpServletRequest request) {

        // Take user back to file upload step (and don't save any data to this step).
        if (submit != null && submit.equals("Previous")) {
            return new ModelAndView(new RedirectView("/fileUpload", true));
        }

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie == null) {
            // Something has gone wrong.  We shouldn't be at this step without having persisted data.
            throw new IllegalStateException(
                    "No persisted data available for custom file attributes step. Check the database & the cookie.");
        }

        // Persist the custom data file information.
        wizardManager.processCustomFileTypeAttributes(rosettaCookie.getValue(), wizardData);

        // Send user to next step to collect variable metadata.
        return new ModelAndView(new RedirectView("/variableMetadata", true));
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
                                         Object handler,
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
