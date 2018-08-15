package edu.ucar.unidata.rosetta.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.ucar.unidata.rosetta.domain.RosettaProperties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PropertiesDaoTest {

  private JdbcPropertiesDao propertiesDao;
  private RosettaProperties rosettaProperties;

  @Before
  public void setUp() throws Exception {
    propertiesDao = mock(JdbcPropertiesDao.class);

    rosettaProperties = new RosettaProperties();
    rosettaProperties.setId(19993590);
    when(propertiesDao.lookupUploadDirectory()).thenReturn("/dev/null");
    when(propertiesDao.lookupDownloadDirectory()).thenReturn("/dev/null");
  }

  @Test
  public void mockCreationTest() throws Exception {
    assertNotNull(propertiesDao);
    assertNotNull(rosettaProperties);
  }

  @Test
  public void lookupUploadDirectoryTest() throws Exception {
    String uploadsDir = propertiesDao.lookupUploadDirectory();
    assertEquals(uploadsDir, "/dev/null");
  }

  @Ignore("downloadsDir is set to /dev/null, not /tmp - is this right?")
  @Test
  public void lookupDownloadDirectoryTest() throws Exception {
    String downloadsDir = propertiesDao.lookupDownloadDirectory();
    assertEquals(downloadsDir, "/tmp");
  }
}