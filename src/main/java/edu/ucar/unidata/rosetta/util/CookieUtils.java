package edu.ucar.unidata.rosetta.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtils {

    private static String APP_NAME = "rosetta";
    private static int EXPIRATION = 1800; // 30 minutes.

    public static Cookie createCookie(String id, HttpServletRequest request) {
        Cookie cookie = new Cookie(APP_NAME, id);
        cookie.setMaxAge(EXPIRATION);
        cookie.setDomain(request.getServerName());
        return cookie;
    }

}
