package edu.ucar.unidata.rosetta.controller.user;

import edu.ucar.unidata.rosetta.domain.user.User;
import edu.ucar.unidata.rosetta.exceptions.RosettaUserException;
import edu.ucar.unidata.rosetta.service.user.UserManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.log4j.Logger;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller to view a user or a group of users.
 *
 * @author oxelson@ucar.edu
 */
@Controller
public class ViewUserController implements HandlerExceptionResolver {

    protected static Logger logger = Logger.getLogger(ViewUserController.class);

    @Resource(name="userManager")
    private UserManager userManager;

    /**
     * Accepts a GET request for a List of all users.
     *
     * The view is the user view.  The model contains a List of User objects which will
     * be loaded and displayed in the view via jspf. The view can handle an empty list
     * of Users if no User objects have been persisted in the database yet.
     *
     * Only Users with the role of 'ROLE_ADMIN' can view the list of users.
     *
     * @param model  The Model used by the View.
     * @return  The path for the ViewResolver.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value="/user", method=RequestMethod.GET)
    public String listUsers(Model model) {
        logger.debug("Get list all users view.");
        List<User> users = userManager.getUsers();
        model.addAttribute("action", "listUsers");
        model.addAttribute("users", users);
        return "user";
    }

    /**
     * This method gracefully handles any uncaught exception that are fatal in nature and unresolvable by the user.
     *
     * @param request   The current HttpServletRequest request.
     * @param response  The current HttpServletRequest response.
     * @param handler   The executed handler, or null if none chosen at the time of the exception.
     * @param exception The exception that got thrown during handler execution.
     * @return The error page containing the appropriate message to the user.
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         java.lang.Object handler,
                                         Exception exception) {

        StringWriter errors = new StringWriter();
        exception.printStackTrace(new PrintWriter(errors));
        String message = "An error has occurred: "
                + exception.getClass().getName() + ":"
                + errors;

        // Log it!
        logger.error(message);
        Map<String, Object> model = new HashMap<>();
        model.put("message", message);
        return new ModelAndView("error", model);
    }

    /**
     * Accepts a GET request to retrieve a specific user.
     *
     * View is the user view.  The model contains the requested User displayed in the view via jspf.
     *
     * Only the User/owner and Users with a role of 'ROLE_ADMIN' are allowed to view the user account.
     *
     * @param userName  The 'userName' as provided by @PathVariable.
     * @param model  The Model used by the View.
     * @return  The View and Model for the ViewResolver.
     * @throws RosettaUserException If unable to locate user.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userName == authentication.name")
    @RequestMapping(value="/user/view/{userName}", method=RequestMethod.GET)
    public ModelAndView viewUserAccount(@PathVariable String userName, Model model) throws RosettaUserException {
        logger.debug("Get specified user information view.");
        User user = userManager.lookupUser(userName);
        model.addAttribute("user", user);
        model.addAttribute("action", "viewUser");
        return new ModelAndView("user");
    }
}
