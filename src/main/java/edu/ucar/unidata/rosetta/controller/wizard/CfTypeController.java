/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.wizard.WizardManager;
import edu.ucar.unidata.rosetta.util.CookieUtils;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;

/**
 * Controller for collecting CF Type information.
 */
@Controller
public class CfTypeController {

    @Resource(name = "wizardManager")
    private WizardManager wizardManager;

    @Resource(name = "resourceManager")
    private ResourceManager resourceManager;

    /**
     * Accepts a GET request for access to CF type selection step of the wizard.
     *
     * @param model   The Model object to be populated.
     * @param request HttpServletRequest needed to get the cookie.
     * @return View and the Model for the wizard to process.
     */
    @RequestMapping(value = "/cfType", method = RequestMethod.GET)
    public ModelAndView displayCFTypeSelectionForm(Model model, HttpServletRequest request) {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        // Create the form-backing object.
        WizardData wizardData;
        boolean customFileAttributesStep = false;
        if (rosettaCookie != null) {
            // User-provided CF type data already exists.  Populate WizardData object.
            wizardData = wizardManager.lookupPersistedWizardDataById(rosettaCookie.getValue());
            customFileAttributesStep = wizardManager.customFileAttributesStep(rosettaCookie.getValue());
        } else {
            wizardData = new WizardData();
        }

        // Add command object to Model.
        model.addAttribute("command", "WizardData");
        // Add form-backing object.
        model.addAttribute("data", wizardData);
        // Add current step to the Model (used by view to keep track of where we are in the wizard).
        model.addAttribute("currentStep", "cfType");
        // Add whether we need to show the custom file attributes step in the wizard menu.
        model.addAttribute("customFileAttributesStep", customFileAttributesStep);
        // Add communities data to Model (for platform display).
        model.addAttribute("communities", resourceManager.getCommunities());
        // Add CF types data to Model (for direct display).
        model.addAttribute("cfTypes", resourceManager.getCfTypes());
        // Add metadata profile data to Model (for direct display).
        model.addAttribute("metadataProfiles", resourceManager.getMetadataProfiles());

        // The currentStep variable will determine which jsp frag to load in the wizard.
        return new ModelAndView("wizard");
    }

    /**
     * Accepts a POST request from CF type selection step of the wizard. Processes the submitted data
     * and persists it to the database.  Redirects user to next step or previous step depending on
     * submitted form button (Next or Previous).
     *
     * @param wizardData The form-backing object.
     * @param result     The BindingResult for error handling.
     * @param request    HttpServletRequest needed to pass to the resourceManager to get client IP.
     * @param response   HttpServletResponse needed for setting cookie.
     * @return Redirect to next step.
     * @throws RosettaDataException If unable to process the CF type data.
     */
    @RequestMapping(value = "/cfType", method = RequestMethod.POST)
    public ModelAndView processCFType(WizardData wizardData, BindingResult result,
                                      HttpServletRequest request, HttpServletResponse response) throws RosettaDataException {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie != null) {
            // We've been here before, combine new with previous persisted CF type data.
            wizardManager.processCfType(rosettaCookie.getValue(), wizardData, null);
        } else {
            // Haven't been before, so proceed with persisting the CF type data.
            wizardManager.processCfType(null, wizardData, request);
            // First time posting to this page in this session; create the cookie.
            response.addCookie(CookieUtils.createCookie(wizardData.getId(), request));
        }
        return new ModelAndView(new RedirectView("/fileUpload", true));
    }
}
