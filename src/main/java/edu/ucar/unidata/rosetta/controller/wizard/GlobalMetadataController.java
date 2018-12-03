/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.service.wizard.MetadataManager;
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
 * Controller for collecting general metadata.
 */
@Controller
public class GlobalMetadataController {

    @Resource(name = "metadataManager")
    private MetadataManager metadataManager;

    @Resource(name = "wizardManager")
    private WizardManager wizardManager;

    /**
     * Accepts a GET request for access to general metadata collection step of the wizard.
     *
     * @param model The Model object to be populated.
     * @param redirectAttrs  A specialization of the model to pass along message if redirected back to starting step.
     * @param request The HttpServletRequest used to retrieve the cookie.
     * @return View and the Model for the wizard to process.
     */
    @RequestMapping(value = "/generalMetadata", method = RequestMethod.GET)
    public ModelAndView displayGeneralMetadataForm(Model model, RedirectAttributes redirectAttrs, HttpServletRequest request) {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        if (rosettaCookie == null) {
            // No cookie.  Take user back to first step.
            redirectAttrs.addFlashAttribute("message", "session expired");
            return new ModelAndView(new RedirectView("/cfType", true));
        }


        WizardData wizardData = wizardManager.lookupPersistedWizardDataById(rosettaCookie.getValue());

        // Add command object to Model.
        model.addAttribute("command", "WizardData");
        // Add form-backing object.
        model.addAttribute("data", wizardData);
        // Add current step to the Model.
        model.addAttribute("currentStep", "generalMetadata");
        // Add whether we need to show the custom file attributes step in the wizard menu.
        model.addAttribute("customFileAttributesStep", wizardManager.customFileAttributesStep(rosettaCookie.getValue()));

        // Add relevant metadata profile information for variable metadata collection.
        model.addAttribute("metadataProfileGeneralData",
                metadataManager.getMetadataProfiles(rosettaCookie.getValue(), "general"));

        // The currentStep variable will determine which jsp frag to load in the wizard.
        return new ModelAndView("wizard");
    }

    /**
     * Accepts a POST request from general metadata collection step of the wizard. Processes the
     * submitted data and persists it to the database.  Redirects user to next step or previous step
     * depending on submitted form button (Next or Previous).
     *
     * @param wizardData The form-backing object.
     * @param submit     The value sent via the submit button.
     * @param result     The BindingResult for error handling.
     * @param redirectAttrs  A specialization of the model to pass along message if redirected back to starting step.
     * @param request The HttpServletRequest used to retrieve the cookie.
     * @return Redirect to next step.
     * @throws RosettaDataException  If unable to populate the metadata object.
     */
    @RequestMapping(value = "/generalMetadata", method = RequestMethod.POST)
    public ModelAndView processGeneralMetadata(WizardData wizardData, @RequestParam("submit") String submit,
                                               BindingResult result, RedirectAttributes redirectAttrs, HttpServletRequest request) throws RosettaDataException {

        // Get the cookie so we can get the persisted data.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie == null) {
            // No cookie.  Take user back to first step.
            redirectAttrs.addFlashAttribute("message", "session expired");
            return new ModelAndView(new RedirectView("/cfType", true));
        }

        // The previous step depends on what the user specified for the data file type.
        if (submit != null && submit.equals("Previous")) {
            String previousStep = wizardManager.processPreviousStep(rosettaCookie.getValue());
            // Take user back to previous step & don't save data submitted to this step. (See dataManager.processPreviousStep).
            return new ModelAndView(new RedirectView(previousStep, true));
        }

        // Persist the general metadata information.
        wizardManager.processGeneralMetadata(rosettaCookie.getValue(), wizardData);

        // Send user to next step to download the converted file(s).
        return new ModelAndView(new RedirectView("/convertAndDownload", true));
    }
}
