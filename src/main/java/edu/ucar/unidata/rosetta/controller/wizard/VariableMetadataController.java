/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.wizard;


import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.wizard.MetadataManager;
import edu.ucar.unidata.rosetta.service.wizard.UploadedFileManager;
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
 * Controller for collecting variable metadata.
 */
@Controller
public class VariableMetadataController {

  @Resource(name = "metadataManager")
  private MetadataManager metadataManager;

  @Resource(name = "resourceManager")
  private ResourceManager resourceManager;

  @Resource(name = "uploadedFileManager")
  private UploadedFileManager uploadedFileManager;

  @Resource(name = "wizardManager")
  private WizardManager wizardManager;

  /**
   * Accepts a GET request for access to variable metadata collection step of the wizard.
   *
   * @param model The Model object to be populated.
   * @param redirectAttrs A specialization of the model to pass along message if redirected back to starting step.
   * @param request The HttpServletRequest used to retrieve the cookie.
   * @return View and the Model for the wizard to process.
   * @throws RosettaFileException For any file I/O or JSON conversions problems while parsing data.
   */
  @RequestMapping(value = "/variableMetadata", method = RequestMethod.GET)
  public ModelAndView displayVariableMetadataForm(Model model, RedirectAttributes redirectAttrs,
      HttpServletRequest request) throws RosettaFileException {

    // Have we visited this page before during this session?
    Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

    if (rosettaCookie == null) {
      // No cookie. Take user back to first step.
      redirectAttrs.addFlashAttribute("message", "session expired");
      return new ModelAndView(new RedirectView("/cfType", true));
    }


    WizardData wizardData = wizardManager.lookupPersistedWizardDataById(rosettaCookie.getValue());

    // Add command object to Model.
    model.addAttribute("command", "WizardData");
    // Add form-backing object.
    model.addAttribute("data", wizardData);
    // Add current step to the Model.
    model.addAttribute("currentStep", "variableMetadata");
    // Add whether we need to show the custom file attributes step in the wizard menu (boolean value).
    model.addAttribute("customFileAttributesStep", wizardManager.customFileAttributesStep(rosettaCookie.getValue()));

    // Add relevant metadata profile information for variable metadata collection.
    model.addAttribute("metadataProfileVariableData",
        metadataManager.getMetadataProfiles(rosettaCookie.getValue(), "variable"));

    // Add parsed file data in JSON string format (to show in the SlickGrid).
    model.addAttribute("parsedData", uploadedFileManager.parseDataFileByLine(wizardData.getId(),
        uploadedFileManager.getDataFile(rosettaCookie.getValue()).getFileName()));
    // Add delimiter to do additional client-side parsing for SlickGrid.
    model.addAttribute("delimiterSymbol", resourceManager.getDelimiterSymbol(wizardData.getDelimiter()));
    // Add header line numbers value to do additional client-side parsing for SlickGrid.
    model.addAttribute("headerLineNumbers", wizardData.getHeaderLineNumbers());

    return new ModelAndView("wizard");
  }

  /**
   * Accepts a POST request from variable metadata collection step of the wizard. Processes the
   * submitted data and persists it to the database. Redirects user to next step or previous step
   * depending on submitted form button (Next or Previous).
   *
   * @param wizardData The form-backing object.
   * @param submit The value sent via the submit button.
   * @param result The BindingResult for error handling.
   * @param redirectAttrs A specialization of the model to pass along message if redirected back to starting step.
   * @param request The HttpServletRequest used to retrieve the cookie.
   * @return Redirect to next step.
   * @throws IllegalStateException If cookie is null.
   */
  @RequestMapping(value = "/variableMetadata", method = RequestMethod.POST)
  public ModelAndView processVariableMetadata(WizardData wizardData, @RequestParam("submit") String submit,
      BindingResult result, RedirectAttributes redirectAttrs, HttpServletRequest request) {

    // Take user back to file upload step (and don't save any data to this step).
    if (submit != null && submit.equals("Previous")) {
      return new ModelAndView(new RedirectView("/customFileTypeAttributes", true));
    }

    // Get the cookie so we can get the persisted data.
    Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
    if (rosettaCookie == null) {
      // No cookie. Take user back to first step.
      redirectAttrs.addFlashAttribute("message", "session expired");
      return new ModelAndView(new RedirectView("/cfType", true));
    }

    // Persist the custom data file information.
    wizardManager.processVariableMetadata(rosettaCookie.getValue(), wizardData);

    // Send user to next step to collect global metadata.
    return new ModelAndView(new RedirectView("/globalMetadata", true));
  }
}
