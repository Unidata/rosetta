package edu.ucar.unidata.rosetta.repository.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.ucar.unidata.rosetta.domain.resources.Platform;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class PlatformDaoTest {
  /* does not compile
  private JdbcPlatformDao platformDao;
  private Platform platform1;
  private Platform platform2;

  @Before
  public void setUp() throws Exception {
    platformDao = mock(JdbcPlatformDao.class);

    platform1 = new Platform();
    platform1.setName("Moored Buoy");
    platform1.setCfType("profile");
    platform1.setCommunity("Physical Ocean Sciences");

    platform2 = new Platform();
    platform2.setName("Wind Profiler");
    platform2.setCfType("profile");
    platform2.setCommunity("Atmospheric Sciences");

    when(platformDao.getPlatforms()).thenReturn(Arrays.asList(platform1, platform2));
    when(platformDao.lookupPlatformByName("Wind Profiler")).thenReturn(platform2);
    when(platformDao.lookupPlatformsByCfType("profile"))
        .thenReturn(Arrays.asList(platform1, platform2));
    when(platformDao.lookupPlatformsByCommunity("Physical Ocean Sciences"))
        .thenReturn(Arrays.asList(platform1));

  }

  @Test
  public void getPlatforms() {
    List<Platform> platforms = platformDao.getPlatforms();
    assertTrue(platforms.size() == 2);
  }

  @Test
  public void lookupPlatformByName() {
    Platform platform = platformDao.lookupPlatformByName("Wind Profiler");
    assertEquals(platform.getCommunity(), "Atmospheric Sciences");
  }

  @Test
  public void lookupPlatformsByCfType() {
    List<Platform> platforms = platformDao.lookupPlatformsByCfType("profile");
    assertTrue(platforms.size() == 2);
  }

  @Test
  public void lookupPlatformsByCommunity() {
    List<Platform> platforms = platformDao.lookupPlatformsByCommunity("Physical Ocean Sciences");
    assertTrue(platforms.size() == 1);
    assertEquals(platforms.get(0).getName(), "Moored Buoy");
  }

  @Test
  public void mockCreationTest() throws Exception {
    assertNotNull(platform1);
    assertNotNull(platform2);
    assertNotNull(platformDao);
  }

  */

}