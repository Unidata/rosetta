package edu.ucar.unidata.rosetta.service.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class FileManagerTest {

  private FileManagerImpl fileManager;
  private List<String> headerLineList;
  private List<List<String>> fileData;

  @Before
  public void setUp() throws Exception {
    fileManager = mock(FileManagerImpl.class);

    List<String> headerLines = new ArrayList<>();
    headerLines.add("1");
    headerLines.add("2");
    headerLineList = new ArrayList<>();
    headerLineList.add("Soil temperature at different depths, Ilu, Greenland");
    headerLineList.add("Location: N 69.2390  W 51.0623");

    fileData = new ArrayList<>();
    fileData.add(headerLines);

    List<String> inventory = new ArrayList<>();
    inventory.add("rosetta.template");
    inventory.add("datafile.txt");

    when(fileManager.parseByLine("/tmp/test.xls")).thenReturn("{\"x\":5,\"y\":6}");
    when(fileManager.getHeaderLinesFromFile("/tmp/test.xls", headerLines))
        .thenReturn(headerLineList);
    when(fileManager.parseByDelimiter("/tmp/test.xls", headerLineList, ",")).thenReturn(fileData);
    when(fileManager.getBlankLines(new File("/dev/null"))).thenReturn(20);
    when(fileManager.getInventoryData("/dev/null")).thenReturn(inventory);
  }

  @Test
  public void getBlankLinesTest() throws Exception {
    int numberOfBlankLines = fileManager.getBlankLines(new File("/dev/null"));
    assertEquals(numberOfBlankLines, 20);
  }

  @Test
  public void getHeaderLinesFromFileTest() throws Exception {
    List<String> headerLines = new ArrayList<>();
    headerLines.add("1");
    headerLines.add("2");
    List<String> lines = fileManager.getHeaderLinesFromFile("/tmp/test.xls", headerLines);
    assertEquals(lines, headerLineList);
  }

  @Test
  public void getInventoryDataTest() throws Exception {
    List<String> inventory = fileManager.getInventoryData("/dev/null");
    assertTrue(inventory.contains("rosetta.template"));
    assertTrue(inventory.contains("datafile.txt"));
  }

  @Test
  public void MockCreationTest() throws Exception {
    assertNotNull(fileManager);
  }

  @Test
  public void parseByDelimiterTest() throws Exception {
    List<List<String>> parsedData = fileManager
        .parseByDelimiter("/tmp/test.xls", headerLineList, ",");
    assertEquals(parsedData, fileData);
  }

  @Test
  public void parseByDelimiterUsingStreamTest() throws Exception {
    List<List<String>> parsedData = fileManager
        .parseByDelimiter("/tmp/test.xls", headerLineList, ",");
    assertEquals(parsedData, fileData);
  }

  @Test
  public void parseByLineTest() throws Exception {
    String jsonString = fileManager.parseByLine("/tmp/test.xls");
    assertEquals(jsonString, "{\"x\":5,\"y\":6}");
  }


}