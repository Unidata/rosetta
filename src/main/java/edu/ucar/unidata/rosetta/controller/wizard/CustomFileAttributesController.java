/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.wizard.WizardManager;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;

/**
 * Controller for collecting custom file type metadata.
 */
@Controller
public class CustomFileAttributesController {

    @Resource(name = "resourceManager")
    private ResourceManager resourceManager;

    @Resource(name = "wizardManager")
    private WizardManager wizardManager;

    /**
     * Accepts a GET request for access to custom file type attribute collection step of the wizard.
     *
     * @param model The Model object to be populated.
     * @param redirectAttrs  A specialization of the model to pass along message if redirected back to starting step.
     * @param request The HttpServletRequest used to retrieve the cookie.
     * @return View and the Model for the wizard to process.
     * @throws RosettaFileException  For any file I/O or JSON conversions problems while parsing data.
     */
    @RequestMapping(value = "/customFileTypeAttributes", method = RequestMethod.GET)
    public ModelAndView displayCustomFileTypeAttributesForm(Model model, RedirectAttributes redirectAttrs, HttpServletRequest request)
            throws RosettaFileException {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        if (rosettaCookie == null) {
            // No cookie.  Take user back to first step.
            redirectAttrs.addFlashAttribute("message", "session expired");
            return new ModelAndView(new RedirectView("/cfType", true));
        }

        // Get the persisted wizard data.
        WizardData wizardData = wizardManager.lookupPersistedWizardDataById(rosettaCookie.getValue());

        // Add command object to Model.
        model.addAttribute("command", "WizardData");
        // Add form-backing object.
        model.addAttribute("data", wizardData);
        // Add current step to the Model.
        model.addAttribute("currentStep", "customFileTypeAttributes");
        // Add whether we need to show the custom file attributes step in the wizard menu.
        model.addAttribute("customFileAttributesStep",  wizardManager.customFileAttributesStep(rosettaCookie.getValue()));
        // Add delimiters to Model.
        model.addAttribute("delimiters", resourceManager.getDelimiters());
        // Add parsed file data in JSON string format (to show in the SlickGrid).
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
     * global metadata collection step.
     *
     * @param wizardData The form-backing object.
     * @param submit     The value sent via the submit button.
     * @param result     The BindingResult for error handling.
     * @param model      The Model object to be populated by file upload data in the next step.
     * @param redirectAttrs  A specialization of the model to pass along message if redirected back to starting step.
     * @param request The HttpServletRequest used to retrieve the cookie.
     * @return Redirect to next step.
     */
    @RequestMapping(value = "/customFileTypeAttributes", method = RequestMethod.POST)
    public ModelAndView processCustomFileTypeAttributes(WizardData wizardData, @RequestParam("submit") String submit, BindingResult result, Model model,
                                                        RedirectAttributes redirectAttrs, HttpServletRequest request) {

        // Take user back to file upload step (and don't save any data to this step).
        if (submit != null && submit.equals("Previous")) {
            return new ModelAndView(new RedirectView("/fileUpload", true));
        }

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie == null) {
            // No cookie.  Take user back to first step.
            redirectAttrs.addFlashAttribute("message", "session expired");
            return new ModelAndView(new RedirectView("/cfType", true));
        }

        // Persist the custom data file information.
        wizardManager.processCustomFileTypeAttributes(rosettaCookie.getValue(), wizardData);

        // Send user to next step to collect variable metadata.
        return new ModelAndView(new RedirectView("/variableMetadata", true));
    }
}
