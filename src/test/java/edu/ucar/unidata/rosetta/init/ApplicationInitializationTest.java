package edu.ucar.unidata.rosetta.init;

import static org.mockito.Mockito.mock;

import javax.servlet.ServletContextEvent;
import org.junit.Before;

public class ApplicationInitializationTest {

  ApplicationInitialization applicationInitialization;
  ServletContextEvent servletContextEvent;

  @Before
  public void setUp() throws Exception {
    applicationInitialization = mock(ApplicationInitialization.class);
    servletContextEvent = mock(ServletContextEvent.class);

  }
// methods of this class are I/O intensive.  Not quite sure yet how to test this class.
}