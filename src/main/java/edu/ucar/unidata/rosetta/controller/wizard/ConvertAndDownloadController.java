package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.service.wizard.DataManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;
import ucar.ma2.InvalidRangeException;

/**
 * Main controller for Rosetta application.
 */
@Controller
public class ConvertAndDownloadController implements HandlerExceptionResolver {

  private static final Logger logger = Logger.getLogger(ConvertAndDownloadController.class);

  private final ServletContext servletContext;

  @Resource(name = "dataManager")
  private DataManager dataManager;

  @Autowired
  public ConvertAndDownloadController(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  /**
   * Accepts a GET request for access to convert and download step of the wizard.
   *
   * @param model The Model object to be populated.
   * @return View and the Model for the wizard to process.
   * @throws InvalidRangeException If unable to convert the data file to netCDF.
   * @throws RosettaFileException If unable to create the template file from the Data object.
   */
  @RequestMapping(value = "/convertAndDownload", method = RequestMethod.GET)
  public ModelAndView displayConvertedFileDownloadPage(Model model, HttpServletRequest request)
      throws InvalidRangeException, RosettaFileException {

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

    // Convert the uploaded file to netCDF & create a template for future conversions.
    data = dataManager.convertToNetCDF(data);

    // Add data object to Model.
    model.addAttribute("data", data);

    // Add current step to the Model.
    model.addAttribute("currentStep", "convertAndDownload");

    // The currentStep variable will determine which jsp frag to load in the wizard.
    return new ModelAndView("wizard");
  }

  /**
   * Accepts a POST request from convert and download step of the wizard. The only purpose of this
   * method is to capture if the user clicked the previous button, in which case they are redirected
   * back to general metadata collection step.
   *
   * @return Redirect to previous step.
   */
  @RequestMapping(value = "/convertAndDownload", method = RequestMethod.POST)
  public ModelAndView processConvertAndDownload() {
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
