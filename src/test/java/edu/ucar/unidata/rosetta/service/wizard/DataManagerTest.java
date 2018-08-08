package edu.ucar.unidata.rosetta.service.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.domain.resources.CfType;
import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.Delimiter;
import edu.ucar.unidata.rosetta.domain.resources.FileType;
import edu.ucar.unidata.rosetta.domain.resources.Platform;
import edu.ucar.unidata.rosetta.repository.wizard.DataDao;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataRetrievalFailureException;

public class DataManagerTest {

  private DataManagerImpl dataManager;
  private DataDao dataDao;
  private Community community1;
  private Community community2;
  private CfType cfType1;
  private CfType cfType2;
  private Platform platform1;
  private Platform platform2;
  private FileType fileType1;
  private FileType fileType2;
  private Delimiter tabDelimiter;
  private Delimiter commaDelimiter;
  private GeneralMetadata generalMetadata;
  private Data data;
  private HttpServletRequest request;

  /* no longer compiles

  @Before
  public void setup() throws Exception {
    dataManager = mock(DataManagerImpl.class);
    dataDao = mock(DataDao.class);
    dataManager.setDataDao(dataDao);
    request = mock(HttpServletRequest.class);

    data = new Data();
    data.setId("000000345HDV4");
    data.setCfType("trajectory");
    data.setSubmit("Next");

    generalMetadata = new GeneralMetadata();
    generalMetadata.setInstitution("University of Denmark");

    community1 = new Community();
    community1.setId(123);

    community2 = new Community();
    community2.setId(456);

    platform1 = new Platform();
    platform1.setName("Moored Buoy");

    platform2 = new Platform();
    platform2.setName("glider");

    fileType1 = new FileType();
    fileType1.setName("GeoCSV");

    fileType2 = new FileType();
    fileType2.setName("EOL Composite Sounding File");

    tabDelimiter = new Delimiter();
    tabDelimiter.setName("Tab");
    tabDelimiter.setCharacterSymbol("\t");

    commaDelimiter = new Delimiter();
    commaDelimiter.setName("Comma");
    commaDelimiter.setCharacterSymbol(",");

    // The behavior we want to see.
    cfType1 = new CfType();
    cfType1.setId(123);
    cfType1.setName("Trajectory");

    cfType2 = new CfType();
    cfType2.setId(345);
    cfType2.setName("Profile");

    List<Map<String, Object>> test = new ArrayList<>();

    when(dataManager.getCfTypes()).thenReturn(Arrays.asList(cfType1, cfType2));
    when(dataManager.lookupPersistedDataById("000000345HDV4")).thenReturn(data);
    doThrow(new DataRetrievalFailureException(
        "Unable to find persisted Data object corresponding to id " + data.getId()))
        .when(dataManager).deletePersistedData(data.getId());
    when(dataManager.getUploadDir()).thenReturn("/dev/null");
    when(dataManager.getDownloadDir()).thenReturn("/tmp");
    when(dataManager.parseDataFileByLine("000000345HDV4", "test.xls"))
        .thenReturn("{\"x\":5,\"y\":6}");
    when(dataManager.getDelimiters()).thenReturn(Arrays.asList(tabDelimiter, commaDelimiter));
    when(dataManager.getDelimiterSymbol("Comma")).thenReturn(",");
    when(dataManager.getCFTypeFromPlatform("eTag")).thenReturn("trajectory");
    when(dataManager.convertToNetCDF(data)).thenReturn(data);
    when(dataManager.getCommunities()).thenReturn(Arrays.asList(community1, community2));
    when(dataManager.getCommunityFromPlatform("Single Station")).thenReturn("Atmospheric Sciences");
    when(dataManager.getPlatforms()).thenReturn(Arrays.asList(platform1, platform2));
    when(dataManager.getFileTypes()).thenReturn(Arrays.asList(fileType1, fileType2));
    when(dataManager.getMetadataFromKnownFile("/foo/bar/baz", "eTuff", generalMetadata))
        .thenReturn(generalMetadata);
    when(dataManager.getMetadataStringForClient("12345", "eTuff")).thenReturn("oiip");
    when(dataManager.processNextStep("12345")).thenReturn("/generalMetadata");
    when(dataManager.processPreviousStep("12345")).thenReturn("/fileUpload");
  }

  @Test
  public void convertToNetCDFTest() throws Exception {
    Data persistedData = dataManager.convertToNetCDF(data);
    assertEquals(data, persistedData);
  }

  @Test(expected = DataRetrievalFailureException.class)
  public void deletePersistedDataTest() throws Exception {
    dataManager.deletePersistedData("000000345HDV4");
    dataManager.lookupPersistedDataById("000000345HDV4");
  }

  @Test
  public void getCFTypeFromPlatformTest() throws Exception {
    String cfType = dataManager.getCFTypeFromPlatform("eTag");
    assertNotEquals(cfType, "profile");
  }

  @Test
  public void getCFTypeTest() throws Exception {
    List<CfType> cfTypes = dataManager.getCfTypes();
    assertTrue(cfTypes.size() == 2);
  }

  @Test
  public void getCommunitiesTest() throws Exception {
    List<Community> communities = dataManager.getCommunities();
    assertTrue(communities.size() == 2);
  }

  @Test
  public void getCommunityFromPlatform() throws Exception {
    String community = dataManager.getCommunityFromPlatform("Single Station");
    assertEquals(community, "Atmospheric Sciences");
  }

  @Test
  public void getDelimitersTest() throws Exception {
    List<Delimiter> delimiters = dataManager.getDelimiters();
    assertTrue(delimiters.size() == 2);
  }

  @Test
  public void getDelimiterSymbolTest() throws Exception {
    String symbol = dataManager.getDelimiterSymbol("Comma");
    assertEquals(symbol, ",");
  }

  @Test
  public void getDownloadDirTest() throws Exception {
    String downloadsDir = dataManager.getDownloadDir();
    assertEquals(downloadsDir, "/tmp");
  }

  @Test
  public void getFileTypesTest() throws Exception {
    List<FileType> fileTypes = dataManager.getFileTypes();
    assertTrue(fileTypes.size() == 2);
  }

  @Test
  public void getMetadataFromKnownFileTest() throws Exception {
    GeneralMetadata metadata = dataManager
        .getMetadataFromKnownFile("/foo/bar/baz", "eTuff", generalMetadata);
    assertEquals(metadata.getInstitution(), "University of Denmark");
  }

  @Test
  public void getMetadataStringForClientTest() throws Exception {
    String metadata = dataManager.getMetadataStringForClient("12345", "eTuff");
    assertEquals(metadata, "oiip");
  }

  @Test
  public void getPlatformsTest() throws Exception {
    List<Platform> platforms = dataManager.getPlatforms();
    assertTrue(platforms.size() == 2);
  }

  @Test
  public void getUploadDirTest() throws Exception {
    String uploadsDir = dataManager.getUploadDir();
    assertEquals(uploadsDir, "/dev/null");
  }

  @Test
  public void lookupPersistedDataByIdTest() throws Exception {
    Data persistedData = dataManager.lookupPersistedDataById("000000345HDV4");
    assertEquals(persistedData.getCfType(), "trajectory");
  }

  @Test
  public void mockCreationTest() throws Exception {
    assertNotNull(dataManager);
    assertNotNull(data);
    assertNotNull(dataDao);
    assertNotNull(request);
  }

  @Test
  public void parseDataFileByLineTest() throws Exception {
    String jsonString = dataManager.parseDataFileByLine("000000345HDV4", "test.xls");
    assertEquals(jsonString, "{\"x\":5,\"y\":6}");
  }


  @Test
  public void persistDataTest() throws Exception {
    dataManager.persistData(data, request);
    Data persistedData = dataManager.lookupPersistedDataById("000000345HDV4");
    assertEquals(persistedData.getCfType(), "trajectory");
  }

  @Test
  public void processNextStepTest() throws Exception {
    String nextStep = dataManager.processNextStep("12345");
    assertEquals(nextStep, "/generalMetadata");
  }

  @Test
  public void processPreviousStepTest() throws Exception {
    String previousStep = dataManager.processPreviousStep("12345");
    assertEquals(previousStep, "/fileUpload");
  }

  @Test
  public void updateDataTest() throws Exception {
    data.setCfType("profile"); // Update data cfType.
    dataManager.updatePersistedData(data);
    Data persistedData = dataManager.lookupPersistedDataById("000000345HDV4");
    assertEquals(persistedData.getCfType(), "profile");
  }
  */
}