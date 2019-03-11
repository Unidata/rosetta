/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Service class used to handle a successful user authentication.
 *
 * @author oxelson@ucar.edu
 */
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  /**
   * Called when a user has been successfully authenticated.
   *
   * @param httpServletRequest The request which caused the successful authentication.
   * @param httpServletResponse The response.
   * @param authentication Authentication object which was created during the authentication
   * process.
   * @throws IOException If unable to send the redirect in the response.
   * @throws ServletException If errors occur during the servlet request/response cycle.
   */
  @Override
  public void onAuthenticationSuccess(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      Authentication authentication) throws IOException, ServletException {

    // Set our response to OK status.
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);

    // Since we have created our custom success handler, its up to us
    // to decide where we will redirect the user after successfully login.
    httpServletResponse.sendRedirect("cfType");
  }
}

