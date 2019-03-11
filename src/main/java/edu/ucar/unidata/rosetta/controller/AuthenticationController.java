/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller to handle user authentication.
 */
@Controller
public class AuthenticationController {

    protected static Logger logger = Logger.getLogger(AuthenticationController.class);

    /**
     * Accepts a GET request for the login page. View is the login page.
     *
     * @param error The authentication error.
     * @param model The Model used by the View.
     * @return The 'login' path for the ViewResolver.
     */
    /*
    @RequestMapping(value = "/login/{error}", method = RequestMethod.GET)
    public String getLoginPage(@PathVariable String error, Model model) {
        logger.debug("Get login view.");
        if (error != null) {
            logger.debug("Authentication errors detected. Returning user to login view.");
            model.addAttribute("error", error);
        }
        return "login";
    }
    */

    /**
     * Accepts a GET request for the login page. View is the login page.
     *
     * @param model The Model used by the View.
     * @return The 'login' path for the ViewResolver.
     */
    /*
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage(Model model) {
        logger.debug("Get login view.");
        return "login";
    }
    */

    /**
     * Accepts a GET request for the denied page. This is shown whenever a regular user tries to
     * access an admin/user-specific only page. View is a denied page.
     *
     * @return The 'denied' path for the ViewResolver.
     */
    /*
    @RequestMapping(value = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        logger.debug("Access denied.");
        return "denied";
    }
    */
}
