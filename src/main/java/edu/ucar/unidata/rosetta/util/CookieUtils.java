/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utilities for handling wizard-related cookies.
 */
public class CookieUtils {

  private static String APP_NAME = "rosetta";
  private static int EXPIRATION = 1800; // 30 minutes.

  /**
   * Creates a cookie for a wizard session.
   *
   * @param id The unique ID associated with the session.
   * @param request The request object used for setting the domain name.
   * @return The cookie.
   */
  public static Cookie createCookie(String id, HttpServletRequest request) {
    Cookie cookie = new Cookie(APP_NAME, id);
    cookie.setMaxAge(EXPIRATION);
    cookie.setDomain(request.getServerName());
    return cookie;
  }

  /**
   * Invalidates the provided cookie.
   *
   * @param cookie The cookie to invalidate.
   * @param response The response in which to add the invalidated cookie.
   */
  public static void invalidateCookie(Cookie cookie, HttpServletResponse response) {
    cookie.setValue("");
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }
}
