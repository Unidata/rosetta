/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.user;

import edu.ucar.unidata.rosetta.domain.user.User;
import edu.ucar.unidata.rosetta.exceptions.RosettaUserException;
import edu.ucar.unidata.rosetta.service.user.UserManager;
import edu.ucar.unidata.rosetta.service.validators.user.EditUserValidator;
import java.util.Collection;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller to edit/modify a User.
 */
@Ignore("enable once user managment is working")
@Controller
public class EditUserController {

    private static final Logger logger = Logger.getLogger(EditUserController.class);
    private final EditUserValidator editUserValidator;
    @Resource(name = "userManager")
    private UserManager userManager;

    /**
     * Creates this controller class.
     *
     * @param editUserValidator The validator used to validate User objects associated with this
     *                          controller class.
     */
    @Autowired
    public EditUserController(EditUserValidator editUserValidator) {
        this.editUserValidator = editUserValidator;
    }


    /**
     * Accepts a GET request to edit an existing user.
     * <p>
     * The view is the user view.  The model contains the User object to edit and the information
     * which will be loaded and displayed in the view via jspf.
     * <p>
     * Only the User/owner and Users with a role of 'ROLE_ADMIN' are allowed to edit a User account.
     *
     * @param userName The 'userName' as provided by @PathVariable.
     * @param model    The Model used by the View.
     * @return The path for the ViewResolver.
     * @throws RosettaUserException If unable to locate user.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userName == authentication.name")
    @RequestMapping(value = "/user/edit/{userName}", method = RequestMethod.GET)
    public String editUser(@PathVariable String userName, Model model) throws RosettaUserException {
        logger.debug("Get edit user form.");
        // Get the user to edit.
        User user = userManager.lookupUser(userName);
        model.addAttribute("action", "editUser");
        model.addAttribute("user", user);
        return "user";
    }

    /**
     * Initialize the WebDataBinder used for populating command and form object arguments.
     *
     * @param binder The WebDataBinder.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmer = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmer);
        binder.setValidator(editUserValidator);
    }

    /**
     * Accepts a POST request to edit an existing user.
     * <p>
     * View is the user view.  The model contains: 1) the updated User object (if successful)
     * displayed in the view via jspf; or 2) the web form to edit the User if there are validation
     * errors with the user input.
     * <p>
     * Only the User/owner and Users with a role of 'ROLE_ADMIN' are allowed to edit the User
     * account.
     *
     * @param user           The User to edit.
     * @param authentication The Authentication object to check roles with.
     * @param result         The BindingResult for error handling.
     * @param model          The Model used by the View.
     * @return The redirect to the needed View.
     * @throws RosettaUserException If unable to locate user.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userName == authentication.name")
    @RequestMapping(value = "/user/edit/{userName}", method = RequestMethod.POST)
    public ModelAndView processUserModification(@PathVariable String userName, @Valid User user,
                                                BindingResult result, Authentication authentication, Model model)
            throws RosettaUserException {
        logger.debug("Processing submitted edit user form data.");

        // Check for validation errors.
        if (result.hasErrors()) {
            logger
                    .debug("Validation errors detected in edit user form data. Returning user to form view.");
            model.addAttribute("action", "editUser");
            model.addAttribute("user", user);
            return new ModelAndView("user");
        } else {
            logger.debug(
                    "No validation errors detected in edit user form data. Proceeding with edit user process.");
            // Lookup the persisted User object and update the information with the provided user input.
            User u = userManager.lookupUser(user.getUserName());
            String name = user.getFullName();
            u.setFullName(name);
            u.setEmailAddress(user.getEmailAddress());
            // Get the user's role and permissions.
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                u.setAccessLevel(user.getAccessLevel());
                u.setAccountStatus(user.getAccountStatus());
            }
            // Persist the updated User object.
            u = userManager.updateUser(u);
            // Update the session.
            Authentication auth = new UsernamePasswordAuthenticationToken(u, u.getPassword(),
                    u.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            return new ModelAndView(new RedirectView("/user/view/" + userName, true));
        }
    }
}
