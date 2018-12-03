/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.util.CookieUtils;
import edu.ucar.unidata.rosetta.util.PropertyUtils;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.ucar.unidata.rosetta.service.wizard.WizardManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;

/**
 * Main controller for Rosetta application.
 */
@Controller
public class ConvertAndDownloadController {

    @Resource(name = "wizardManager")
    private WizardManager wizardManager;

    /**
     * Accepts a GET request for access to convert and download step of the wizard.
     * Displays the converted data file and rosetta template for download.
     *
     * @param model The Model object to be populated.
     * @param redirectAttrs  A specialization of the model to pass along message if redirected back to starting step.
     * @param request The HttpServletRequest used to retrieve the cookie.
     * @return View and the Model for the wizard to process.
     * @throws RosettaFileException If unable to create the template file from the Data object.
     * @throws RosettaDataException If unable to parse data file with delimiter.
     */
    @RequestMapping(value = "/convertAndDownload", method = RequestMethod.GET)
    public ModelAndView displayConvertedFileDownloadPage(Model model, RedirectAttributes redirectAttrs, HttpServletRequest request)
            throws RosettaFileException, RosettaDataException {

        // Have we visited this page before during this session?
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");

        if (rosettaCookie == null) {
            // No cookie.  Take user back to first step.
            redirectAttrs.addFlashAttribute("message", "session expired");
            return new ModelAndView(new RedirectView("/cfType", true));
        }

        // Convert the uploaded file to netCDF & create a template for future conversions.
        String netcdfFile = wizardManager.convertToNetcdf(rosettaCookie.getValue());
        String template =  wizardManager.getTemplateFile(rosettaCookie.getValue());
        // Add data object to Model.
        String userFilesDir = PropertyUtils.getUserFilesDir();

        model.addAttribute("netCDF", netcdfFile.replace(userFilesDir, ""));
        model.addAttribute("template", template.replace(userFilesDir, ""));

        // Add current step to the Model.
        model.addAttribute("currentStep", "convertAndDownload");

        // The currentStep variable will determine which jsp frag to load in the wizard.
        return new ModelAndView("wizard");
    }

    /**
     * Accepts a POST request from convert and download step of the wizard. The only purpose of this
     * method is to capture if the user clicked the 'finished' button, in which case the user's cookie
     * is invalidated (if the client side hasn't already done so). The use is then redirected back to
     * to the starting step of the wizard.
     *
     * @param request The HttpServletRequest used to retrieve the cookie.
     * @param response The HttpServletResponse used to invalidate the cookie.
     * @return Redirect to previous step.
     */
    @RequestMapping(value = "/convertAndDownload", method = RequestMethod.POST)
    public ModelAndView processConvertAndDownload(HttpServletRequest request, HttpServletResponse response) {

        // Invalidate the cookie.
        Cookie rosettaCookie = WebUtils.getCookie(request, "rosetta");
        if (rosettaCookie != null) {
            CookieUtils.invalidateCookie(rosettaCookie, response);
        }

        // Take user back to first step.
        return new ModelAndView(new RedirectView("/cfType", true));
    }
}
