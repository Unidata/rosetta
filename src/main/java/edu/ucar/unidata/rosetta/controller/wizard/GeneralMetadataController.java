/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.service.wizard.DataManager;
import edu.ucar.unidata.rosetta.util.PropertyUtils;
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

/**
 * Controller for collecting general metadata.
 *
 * @author oxelson@ucar.edu
 */
@Controller
public class GeneralMetadataController implements HandlerExceptionResolver {

  private static final Logger logger = Logger.getLogger(GeneralMetadataController.class);

  private final ServletContext servletContext;

  @Resource(name = "dataManager")
  private DataManager dataManager;

  @Autowired
  public GeneralMetadataController(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  /**
   * Accepts a GET request for access to general metadata collection step of the wizard.
   *
   * @param model The Model object to be populated.
   * @return View and the Model for the wizard to process.
   * @throws RosettaDataException If unable to populate the GeneralMetadata object.
   * @throws IllegalStateException If cookie is null.
   */
  @RequestMapping(value = "/generalMetadata", method = RequestMethod.GET)
  public ModelAndView displayGeneralMetadataForm(Model model, HttpServletRequest request)
      throws RosettaDataException {

    // Have we visited this page before during this session?
    Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

    if (rosettaCookie == null)
    // Something has gone wrong.  We shouldn't be at this step without having persisted data.
    {
      throw new IllegalStateException(
          "No persisted data available for file upload step.  Check the database & the cookie.");
    }

    // Create a Data form-backing object.
    Data data = dataManager.lookupPersistedDataById(rosettaCookie.getValue());

    // Add data object to Model.
    model.addAttribute("data", data);

    GeneralMetadata metadata = new GeneralMetadata();

    // Mine the data file for any included metadata.
    metadata = dataManager.getMetadataFromKnownFile(FilenameUtils
        .concat(FilenameUtils.concat(PropertyUtils.getUploadDir(), data.getId()),
            data.getDataFileName()), data.getDataFileType(), metadata);

    model.addAttribute("generalMetadata", metadata);

    // Add current step to the Model.
    model.addAttribute("currentStep", "generalMetadata");

    // The currentStep variable will determine which jsp frag to load in the wizard.
    return new ModelAndView("wizard");
  }

  /**
   * Accepts a POST request from general metadata collection step of the wizard. Processes the
   * submitted data and persists it to the database.  Redirects user to next step or previous step
   * depending on submitted form button (Next or Previous).
   *
   * @param data The form-backing object.
   * @param result The BindingResult for error handling.
   * @param model The Model object to be populated by file upload data in the next step.
   * @param request HttpServletRequest needed to get the cookie.
   * @return Redirect to next step.
   * @throws IllegalStateException If cookie is null.
   * @throws RosettaDataException If unable to populate the metadata object.
   */
  @RequestMapping(value = "/generalMetadata", method = RequestMethod.POST)
  public ModelAndView processGeneralMetadata(Data data, GeneralMetadata metadata,
      BindingResult result, Model model, HttpServletRequest request) throws RosettaDataException {

    // Get the cookie so we can get the persisted data.
    Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
    if (rosettaCookie == null)
    // Something has gone wrong.  We shouldn't be at this step without having persisted data.
    {
      throw new IllegalStateException(
          "No persisted data available for file upload step.  Check the database & the cookie.");
    }

    // The previous step depends on what the user specified for the data file type.
    if (data.getSubmit().equals("Previous")) {
      String previousStep = dataManager.processPreviousStep(rosettaCookie.getValue());
      // Take user back to previous step & don't save data submitted to this step. (See dataManager.processPreviousStep).
      return new ModelAndView(new RedirectView(previousStep, true));
    }

    // Persist the general metadata information.
    dataManager.processGeneralMetadata(rosettaCookie.getValue(), metadata);

    // Send user to next step to download the converted file(s).
    return new ModelAndView(new RedirectView("/convertAndDownload", true));
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
