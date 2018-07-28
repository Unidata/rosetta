package edu.ucar.unidata.rosetta.service.resources;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import edu.ucar.unidata.rosetta.init.resources.EmbeddedDerbyDbInitManager;
import org.junit.Before;
import org.junit.Test;


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
}