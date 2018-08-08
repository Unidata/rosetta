/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller to handle user authentication.
 *
 * @author oxelson@ucar.edu
 */
@Controller
public class AuthenticationController implements HandlerExceptionResolver {

  protected static Logger logger = Logger.getLogger(AuthenticationController.class);

  /**
   * Accepts a GET request for the login page. View is the login page.
   *
   * @param error The authentication error.
   * @param model The Model used by the View.
   * @return The 'login' path for the ViewResolver.
   */
  @RequestMapping(value = "/login/{error}", method = RequestMethod.GET)
  public String getLoginPage(@PathVariable String error, Model model) {
    logger.debug("Get login view.");
    if (error != null) {
      logger.debug("Authentication errors detected. Returning user to login view.");
      model.addAttribute("error", error);
    }
    return "login";
  }

  /**
   * Accepts a GET request for the login page. View is the login page.
   *
   * @param model The Model used by the View.
   * @return The 'login' path for the ViewResolver.
   */
  @RequestMapping(value = "/login", method = RequestMethod.GET)
  public String getLoginPage(Model model) {
    logger.debug("Get login view.");
    return "login";
  }

  /**
   * Accepts a GET request for the denied page. This is shown whenever a regular user tries to
   * access an admin/user-specific only page. View is a denied page.
   *
   * @return The 'denied' path for the ViewResolver.
   */
  @RequestMapping(value = "/denied", method = RequestMethod.GET)
  public String getDeniedPage() {
    logger.debug("Access denied.");
    return "denied";
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
