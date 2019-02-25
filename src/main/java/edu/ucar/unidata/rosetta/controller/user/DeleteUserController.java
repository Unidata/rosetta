/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.user;

import edu.ucar.unidata.rosetta.domain.user.User;
import edu.ucar.unidata.rosetta.exceptions.RosettaUserException;
import edu.ucar.unidata.rosetta.service.user.UserManager;
import javax.annotation.Resource;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller to handle user removal.
 */
@Ignore("enable once user managment is working")
@Controller
public class DeleteUserController {

    private static final Logger logger = Logger.getLogger(DeleteUserController.class);

    @Resource(name = "userManager")
    private UserManager userManager;

    /**
     * Accepts a GET request to delete a specific user.
     * <p>
     * View is the user view.  The model contains the User object to display in the view via jspf.
     * <p>
     * Only Users with a role of 'ROLE_ADMIN' are allowed to delete the User account.
     *
     * @param userName The 'userName' as provided by @PathVariable.
     * @param model    The Model used by the View.
     * @return The path for the ViewResolver.
     * @throws RosettaUserException If unable to locate user to delete.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/user/delete/{userName}", method = RequestMethod.GET)
    public String deleteUser(@PathVariable String userName, Model model) throws RosettaUserException {
        logger.debug("Get delete user confirmation form.");
        // Get the persisted user to delete.
        User user = userManager.lookupUser(userName);
        model.addAttribute("user", user);
        model.addAttribute("action", "deleteUser");
        return "user";
    }

    /**
     * Accepts a POST request to delete an existing user.
     * <p>
     * View is the user view.  The model contains a List of remaining User objects (if successful)
     * displayed in the view via jspf.
     * <p>
     * Only Users with a role of 'ROLE_ADMIN' are allowed to delete users.
     *
     * @param user   The User to delete.
     * @param result The BindingResult for error handling.
     * @param model  The Model used by the View.
     * @return The redirect to the needed View.
     * @throws RosettaUserException If unable to delete the user.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/user/delete", method = RequestMethod.POST)
    public ModelAndView processUserDeletion(User user, BindingResult result, Model model)
            throws RosettaUserException {
        logger.debug("Processing delete user request.");
        // Delete the user.
        userManager.deleteUser(user.getEmailAddress());
        // Get the remaining available users and redirect to the list of users view.
        List<User> users = userManager.getUsers();
        model.addAttribute("action", "listUsers");
        model.addAttribute("users", users);
        return new ModelAndView(new RedirectView("/user", true));
    }
}
