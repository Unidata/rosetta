package edu.ucar.unidata.rosetta.service;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileParserManagerTest {

    private FileParserManagerImpl fileParserManager;
    private List<String> headerLineList;
    private List<List<String>> fileData;


    @Before
    public void setUp() throws Exception {
        fileParserManager = mock(FileParserManagerImpl.class);

        List<String> headerLines = new ArrayList<>();
        headerLines.add("1");
        headerLines.add("2");
        headerLineList = new ArrayList<>();
        headerLineList.add("Soil temperature at different depths, Ilu, Greenland");
        headerLineList.add("Location: N 69.2390  W 51.0623");

        fileData = new ArrayList<>();
        fileData.add(headerLines);

        when(fileParserManager.parseByLine("/tmp/test.xls")).thenReturn("{\"x\":5,\"y\":6}");
        when(fileParserManager.getHeaderLinesFromFile("/tmp/test.xls", headerLines)).thenReturn(headerLineList);
        when(fileParserManager.parseByDelimiter("/tmp/test.xls", headerLineList, ",")).thenReturn(fileData);
        when(fileParserManager.getBlankLines(new File("/dev/null"))).thenReturn(20);
    }

    @Test
    public void MockCreationTest() throws Exception {
        assertNotNull(fileParserManager);
    }

    @Test
    public void parseByLineTest() throws Exception {
        String jsonString = fileParserManager.parseByLine("/tmp/test.xls");
        assertEquals(jsonString, "{\"x\":5,\"y\":6}");
    }

    @Test
    public void getHeaderLinesFromFileTest() throws Exception {
        List<String> headerLines = new ArrayList<>();
        headerLines.add("1");
        headerLines.add("2");
        List<String> lines = fileParserManager.getHeaderLinesFromFile("/tmp/test.xls", headerLines);
        assertEquals(lines, headerLineList);
    }

    @Test
    public void parseByDelimiterTest() throws Exception {
        List<List<String>> parsedData = fileParserManager.parseByDelimiter("/tmp/test.xls", headerLineList, ",");
        assertEquals(parsedData, fileData);
    }

    @Test
    public void getBlankLinesTest() throws Exception {
        int numberOfBlankLines = fileParserManager.getBlankLines(new File("/dev/null"));
        assertEquals(numberOfBlankLines, 20);
    }
}