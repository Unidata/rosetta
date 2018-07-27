package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.domain.Data;

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
 * Controller for collecting CF Type information.
 *
 * @author oxelson@ucar.edu
 */
@Controller
public class CfTypeController implements HandlerExceptionResolver {

  private static final Logger logger = Logger.getLogger(CfTypeController.class);

  private final ServletContext servletContext;

  @Resource(name = "dataManager")
  private DataManager dataManager;

  @Autowired
  public CfTypeController(ServletContext servletContext) {
    this.servletContext = servletContext;
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
    // Create a Data form-backing object.
    Data data;
    if (rosettaCookie != null)
      // User-provided cfType info already exists.  Populate Data object with info.
    {
      data = dataManager.lookupPersistedDataById(rosettaCookie.getValue());
    } else {
      data = new Data();
    }

    // Add data object to Model.
    model.addAttribute("data", data);
    // Add current step to the Model (used by view to keep track of where we are in the wizard).
    model.addAttribute("currentStep", "cfType");
    // Add communities data to Model (for platform display).
    model.addAttribute("communities", dataManager.getCommunities());
    // Add CF types data to Model (for direct display).
    model.addAttribute("cfTypes", dataManager.getCfTypes());

    // The currentStep variable will determine which jsp frag to load in the wizard.
    return new ModelAndView("wizard");
  }

  /**
   * Accepts a POST request from CF type selection step of the wizard. Processes the submitted data
   * and persists it to the database.  Redirects user to next step or previous step depending on
   * submitted form button (Next or Previous).
   *
   * @param data The form-backing object.
   * @param result The BindingResult for error handling.
   * @param model The Model object to be populated.
   * @param request HttpServletRequest needed to pass to the dataManager to get client IP.
   * @param response HttpServletResponse needed for setting cookie.
   * @return Redirect to next step.
   */
  @RequestMapping(value = "/cfType", method = RequestMethod.POST)
  public ModelAndView processCFType(Data data, BindingResult result, Model model,
      HttpServletRequest request, HttpServletResponse response) {

    // Have we visited this page before during this session?
    Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
    if (rosettaCookie != null) {
      // We've been here before, combine new with previous persisted data.
      dataManager.processCfType(rosettaCookie.getValue(), data, null);
    } else {
      // Haven't been before, so proceed with persist the data.
      dataManager.processCfType(null, data, request);
      // First time posting to this page in this session.
      response.addCookie(new Cookie("rosetta", data.getId()));
    }
    return new ModelAndView(new RedirectView("/fileUpload", true));
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
