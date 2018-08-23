/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.domain.wizard.MetadataProfileCmd;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.service.wizard.DataManager;
import edu.ucar.unidata.rosetta.service.wizard.MetadataManager;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucar.unidata.rosetta.service.wizard.UploadedFileManager;
import edu.ucar.unidata.rosetta.service.wizard.WizardManager;
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

/**
 * Controller for collecting variable metadata.
 *
 * @author oxelson@ucar.edu
 */
@Controller
public class VariableMetadataController implements HandlerExceptionResolver {

  private static final Logger logger = Logger.getLogger(VariableMetadataController.class);

  private final ServletContext servletContext;

  @Resource(name = "dataManager")
  private DataManager dataManager;

  @Resource(name = "metadataManager")
  private MetadataManager metadataManager;

  @Resource(name = "resourceManager")
  private ResourceManager resourceManager;

  @Resource(name = "uploadedFileManager")
  private UploadedFileManager uploadedFileManager;

  @Resource(name = "wizardManager")
  private WizardManager wizardManager;

  @Autowired
  public VariableMetadataController(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  /**
   * Accepts a GET request for access to variable metadata collection step of the wizard.
   *
   * @param model The Model object to be populated.
   * @return View and the Model for the wizard to process.
   * @throws IllegalStateException If cookie is null.
   * @throws RosettaFileException For any file I/O or JSON conversions problems while parsing data.
   */
  @RequestMapping(value = "/variableMetadata", method = RequestMethod.GET)
  public ModelAndView displayVariableMetadataForm(Model model, HttpServletRequest request)
      throws RosettaFileException {

    // Have we visited this page before during this session?
    Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

    if (rosettaCookie == null) {
      // Something has gone wrong.  We shouldn't be at this step without having persisted data.
      throw new IllegalStateException(
          "No persisted data available for file upload step.  Check the database & the cookie.");
    }

    // Create the form backing object.
    MetadataProfileCmd metadataProfileCmd;
    // Have we been here before?  Do we have any data persisted?
    MetadataProfileCmd persisted = metadataManager.lookupMetadataById(rosettaCookie.getValue(), "variable");
    if (!persisted.getMetadataProfiles().isEmpty()) {
      metadataProfileCmd = persisted;
    } else {
      // No persisted data.
      metadataProfileCmd = new MetadataProfileCmd();
    }

    WizardData wizardData = wizardManager.lookupPersistedWizardDataById(rosettaCookie.getValue());

    // Populate with any existing variable metadata.
   // data.setVariableMetadata(dataManager.getMetadataStringForClient(data.getId(), "variable"));

    // Add form-backing object.
    model.addAttribute("data", metadataProfileCmd);
    // Add command object to Model.
    model.addAttribute("command", "variableMetadata");
    // Add current step to the Model.
    model.addAttribute("currentStep", "variableMetadata");
    model.addAttribute("variableData", metadataManager.getMetadataProfiles(rosettaCookie.getValue(), "variable"));
    // Add whether we need to show the custom file attributes step in the wizard menu (boolean value).
    model.addAttribute("customFileAttributesStep",  wizardManager.customFileAttributesStep(rosettaCookie.getValue()));
    // Add parsed file data in JSON string format (to show in the SlickGrid).
    model.addAttribute("parsedData",
        dataManager.parseDataFileByLine(wizardData.getId(), uploadedFileManager.getDataFile(rosettaCookie.getValue()).getFileName()));
    // Add delimiter to do additional client-side parsing for SlickGrid.
    model.addAttribute("delimiterSymbol", resourceManager.getDelimiterSymbol(wizardData.getDelimiter()));
    // Add header line numbers value to do additional client-side parsing for SlickGrid.
    model.addAttribute("headerLineNumbers", wizardData.getHeaderLineNumbers());

    // The currentStep variable will determine which jsp frag to load in the wizard.
    return new ModelAndView("wizard");
  }

  /**
   * Accepts a POST request from variable metadata collection step of the wizard. Processes the
   * submitted data and persists it to the database.  Redirects user to next step or previous step
   * depending on submitted form button (Next or Previous).
   *
   * @param data The form-backing object.
   * @param result The BindingResult for error handling.
   * @param model The Model object to be populated by file upload data in the next step.
   * @param request HttpServletRequest needed to get the cookie.
   * @return Redirect to next step.
   * @throws IllegalStateException If cookie is null.
   */
  @RequestMapping(value = "/variableMetadata", method = RequestMethod.POST)
  public ModelAndView processVariableMetadata(Data data, BindingResult result, Model model,
      HttpServletRequest request) {

    // Take user back to custom file attribute collection step (and don't save any data to this step).
    if (data.getSubmit().equals("Previous")) {
      return new ModelAndView(new RedirectView("/customFileTypeAttributes", true));
    }

    // Get the cookie so we can get the persisted data.
    Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
    if (rosettaCookie == null)
    // Something has gone wrong.  We shouldn't be at this step without having persisted data.
    {
      throw new IllegalStateException(
          "No persisted data available for file upload step.  Check the database & the cookie.");
    }

    // Persist the variable metadata information.
    dataManager.processVariableMetadata(rosettaCookie.getValue(), data);

    // Send user to next step to collect general metadata.
    return new ModelAndView(new RedirectView("/generalMetadata", true));
  }

  /**
   * This method gracefully handles any uncaught exception that are fatal in nature and unresolvable
   * by the user.
   *
   * @param request The current HttpServletRequest request.
   * @param response The current HttpServletRequest response.
   * @param handler The executed handler, or null if none chosen at the time of the exception.
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
