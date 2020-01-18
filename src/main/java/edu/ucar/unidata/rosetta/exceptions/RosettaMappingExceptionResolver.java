/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

public class RosettaMappingExceptionResolver extends SimpleMappingExceptionResolver {

  private static final Logger logger = LogManager.getLogger();

  public RosettaMappingExceptionResolver() {
    super();
  }

  @Override
  public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
      Exception exception) {
    logger.error(getStackTrace(exception));
    return super.resolveException(request, response, handler, exception);
  }


  public String getStackTrace(Throwable t) {
    StringWriter stringWritter = new StringWriter();
    PrintWriter printWritter = new PrintWriter(stringWritter, true);
    t.printStackTrace(printWritter);
    printWritter.flush();
    stringWritter.flush();
    return stringWritter.toString();
  }


}
