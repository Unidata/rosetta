package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.exceptions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucar.unidata.rosetta.service.wizard.DataManager;
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
 * Controller for handling file uploads.
 *
 * @author oxelson@ucar.edu
 */
@Controller
public class FileUploadController implements HandlerExceptionResolver {

  private static final Logger logger = Logger.getLogger(FileUploadController.class);

  private final ServletContext servletContext;

  @Resource(name = "dataManager")
  private DataManager dataManager;

  @Autowired
  public FileUploadController(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  /**
   * Accepts a GET request for access to file upload step of the wizard.
   *
   * @param model The Model object to be populated.
   * @param request HttpServletRequest needed to get the cookie.
   * @return View and the Model for the wizard to process.
   * @throws IllegalStateException If cookie is null.
   */
  @RequestMapping(value = "/fileUpload", method = RequestMethod.GET)
  public ModelAndView displayFileUploadForm(Model model, HttpServletRequest request) {

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
    // Add current step to the Model.
    model.addAttribute("currentStep", "fileUpload");
    // Add community data to Model (for file upload display based on community type).
    model.addAttribute("communities", dataManager.getCommunities());
    // Add file type data to Model (for file type selection if cfType was directly specified).
    model.addAttribute("fileTypes", dataManager.getFileTypes());

    // The currentStep variable will determine which jsp frag to load in the wizard.
    return new ModelAndView("wizard");
  }

  /**
   * Accepts a POST request from file upload step of the wizard. Processes the submitted data and
   * persists it to the database.  Redirects user to next step or previous step depending on
   * submitted form button (Next or Previous).
   *
   * @param data The form-backing object.
   * @param result The BindingResult for error handling.
   * @param model The Model object to be populated by file upload data in the next step.
   * @param request HttpServletRequest needed to get the cookie.
   * @return Redirect to next step.
   * @throws IllegalStateException If cookie is null.
   * @throws RosettaFileException If unable to write file(s) to disk or a file conversion exception
   * occurred.
   */
  @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
  public ModelAndView processFileUpload(Data data, BindingResult result, Model model,
      HttpServletRequest request) throws RosettaFileException {

    // Take user back to the CF type selection step (and don't save any data to this step).
    if (data.getSubmit().equals("Previous")) {
      return new ModelAndView(new RedirectView("/cfType", true));
    }

    // Get the cookie so we can get the persisted data.
    Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
    if (rosettaCookie == null)
    // Something has gone wrong.  We shouldn't be at this step without having persisted data.
    {
      throw new IllegalStateException(
          "No persisted data available for file upload step.  Check the database & the cookie.");
    }

    // Persist the file upload data.
    dataManager.processFileUpload(rosettaCookie.getValue(), data);

    // Depending on what the user entered for the data file, we may need to
    // add an extra step to collect data associated with that custom file type.
    String nextStep = dataManager.processNextStep(rosettaCookie.getValue());

    // Send user to the next view.  (See dataManager.processFileUpload).
    return new ModelAndView(new RedirectView(nextStep, true));
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
