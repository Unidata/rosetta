/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.user;

import edu.ucar.unidata.rosetta.domain.user.User;
import edu.ucar.unidata.rosetta.exceptions.RosettaUserException;
import edu.ucar.unidata.rosetta.service.user.UserManager;
import java.util.List;
import javax.annotation.Resource;
import org.apache.log4j.Logger;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller to view a user or a group of users.
 */
@Controller
public class ViewUserController {

    protected static Logger logger = Logger.getLogger(ViewUserController.class);

    @Resource(name = "userManager")
    private UserManager userManager;

    /**
     * Accepts a GET request for a List of all users.
     * <p>
     * The view is the user view.  The model contains a List of User objects which will be loaded and
     * displayed in the view via jspf. The view can handle an empty list of Users if no User objects
     * have been persisted in the database yet.
     * <p>
     * Only Users with the role of 'ROLE_ADMIN' can view the list of users.
     *
     * @param model The Model used by the View.
     * @return The path for the ViewResolver.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String listUsers(Model model) {
        logger.debug("Get list all users view.");
        List<User> users = userManager.getUsers();
        model.addAttribute("action", "listUsers");
        model.addAttribute("users", users);
        return "user";
    }

    /**
     * Accepts a GET request to retrieve a specific user.
     * <p>
     * View is the user view.  The model contains the requested User displayed in the view via jspf.
     * <p>
     * Only the User/owner and Users with a role of 'ROLE_ADMIN' are allowed to view the user
     * account.
     *
     * @param userName The 'userName' as provided by @PathVariable.
     * @param model    The Model used by the View.
     * @return The View and Model for the ViewResolver.
     * @throws RosettaUserException If unable to locate user.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userName == authentication.name")
    @RequestMapping(value = "/user/view/{userName}", method = RequestMethod.GET)
    public ModelAndView viewUserAccount(@PathVariable String userName, Model model)
            throws RosettaUserException {
        logger.debug("Get specified user information view.");
        User user = userManager.lookupUser(userName);
        model.addAttribute("user", user);
        model.addAttribute("action", "viewUser");
        return new ModelAndView("user");
    }
}
