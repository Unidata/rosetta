package edu.ucar.unidata.rosetta.service.resources;

import edu.ucar.unidata.rosetta.service.resources.EmbeddedDerbyDbInitManager;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertNotNull;

import static org.mockito.Mockito.mock;


public class DbInitManagerTest {

    private EmbeddedDerbyDbInitManager dbInitManager;


    @Before
    public void setUp() throws Exception {
        dbInitManager = mock(EmbeddedDerbyDbInitManager.class);

    }

    @Test
    public void MockCreationTest() throws Exception {
        assertNotNull(dbInitManager);
    }

    // The methods of the EmbeddedDerbyDbInitManager class involve I/O, so difficult to test.

}