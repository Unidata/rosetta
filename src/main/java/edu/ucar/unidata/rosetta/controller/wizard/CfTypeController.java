/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.validators.wizard.CfTypeValidator;
import edu.ucar.unidata.rosetta.service.wizard.WizardManager;
import edu.ucar.unidata.rosetta.util.CookieUtils;
import java.util.Objects;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
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

  private static final Logger logger = LogManager.getLogger(CfTypeController.class);
  private final CfTypeValidator cfTypeValidator;


  @Resource(name = "wizardManager")
  private WizardManager wizardManager;

  @Resource(name = "resourceManager")
  private ResourceManager resourceManager;

  /**
   * Creates this controller class.
   *
   * @param cfTypeValidator The validator used to validate the user input data collected via this controller class.
   */
  @Autowired
  public CfTypeController(CfTypeValidator cfTypeValidator) {
    this.cfTypeValidator = cfTypeValidator;
  }

  /**
   * Accepts a GET request for access to CF type selection step of the wizard.
   *
   * @param model The Model object to be populated.
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
    if (Objects.nonNull(rosettaCookie)) {
      // User-provided CF type data already exists. Populate WizardData object.
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
   * Initialize the WebDataBinder used for populating command and form object arguments.
   *
   * @param binder The WebDataBinder.
   */
  @InitBinder
  public void initBinder(WebDataBinder binder) {
    // Transform an empty string in submitted data into a null.
    StringTrimmerEditor stringTrimmer = new StringTrimmerEditor(true);
    binder.registerCustomEditor(String.class, stringTrimmer);
    binder.setValidator(cfTypeValidator);
  }

  /**
   * Accepts a POST request from CF type selection step of the wizard. Processes the submitted data
   * and persists it to the database. Redirects user to next step or previous step depending on
   * submitted form button (Next or Previous).
   *
   * @param wizardData The form-backing object.
   * @param result The BindingResult for error handling.
   * @param request HttpServletRequest needed to pass to the resourceManager to get client IP.
   * @param response HttpServletResponse needed for setting cookie.
   * @return Redirect to next step.
   * @throws RosettaDataException If unable to process the CF type data.
   * @throws RosettaFileException If unable to create transaction log.
   */
  @RequestMapping(value = "/cfType", method = RequestMethod.POST)
  public ModelAndView processCFType(@Valid WizardData wizardData, BindingResult result, HttpServletRequest request,
      HttpServletResponse response) throws RosettaDataException, RosettaFileException {

    logger.info(wizardData.toString());

    // Check for validation errors.
    if (result.hasErrors()) {
      logger.info("Validation errors detected in create user form data. Returning user to form view.");
      return new ModelAndView(new RedirectView("/cfType", true));
    }

    // Have we visited this page before during this session?
    Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
    if (Objects.nonNull(rosettaCookie)) {
      // We've been here before, combine new with previous persisted CF type data.
      wizardManager.processCfType(rosettaCookie.getValue(), wizardData);
    } else {
      // Haven't been before, so proceed with persisting the CF type data.
      wizardManager.processCfType(wizardData, request);
      // First time posting to this page in this session; create the cookie.
      response.addCookie(CookieUtils.createCookie(wizardData.getId(), request));
    }
    return new ModelAndView(new RedirectView("/fileUpload", true));
  }
}
