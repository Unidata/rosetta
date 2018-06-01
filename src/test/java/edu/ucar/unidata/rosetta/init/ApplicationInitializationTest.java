package edu.ucar.unidata.rosetta.init;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContextEvent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

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