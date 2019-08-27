package edu.ucar.unidata.rosetta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import java.util.Properties;

/**
 * Application context info not specified in resources/applicationContext-test.xml
 */
@Configuration
@ImportResource("classpath:applicationContext-test.xml")
public class WebAppContext {

  @Bean
  public SimpleMappingExceptionResolver exceptionResolver() {
    SimpleMappingExceptionResolver exceptionResolver = new SimpleMappingExceptionResolver();
    Properties exceptionMappings = new Properties();
    exceptionMappings.put("org.springframework.dao.DataRetrievalFailureException", "fatalError");

    exceptionResolver.setExceptionMappings(exceptionMappings);
    exceptionResolver.setDefaultErrorView("error");
    return exceptionResolver;
  }

  @Bean
  public ViewResolver jspViewResolver() {
    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();

    viewResolver.setViewClass(JstlView.class);
    viewResolver.setPrefix("/WEB-INF/views/");
    viewResolver.setSuffix(".jsp");
    return viewResolver;
  }
}


