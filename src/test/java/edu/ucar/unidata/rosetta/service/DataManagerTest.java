package edu.ucar.unidata.rosetta.service;


import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.repository.DataDao;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.dao.DataRetrievalFailureException;

public class DataManagerTest {

    private DataManagerImpl dataManager;
    private DataDao dataDao;
    private Data data;
    private HttpServletRequest request;

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

        // The behavior we want to see.
        when(dataManager.lookupPersistedDataById("000000345HDV4")).thenReturn(data);
        doThrow(new DataRetrievalFailureException("Unable to find persisted Data object corresponding to id " + data.getId())).when(dataManager).deletePersistedData(data.getId());
        when(dataManager.getUploadDir()).thenReturn("/dev/null");
        when(dataManager.getDownloadDir()).thenReturn("/tmp");
        when(dataManager.convertToCSV("000000345HDV4", "test.xls")).thenReturn("true");
        when(dataManager.parseDataFileByLine("000000345HDV4", "test.xls")).thenReturn("{\"x\":5,\"y\":6}");
        when(dataManager.getDelimiterSymbol("Comma")).thenReturn(",");
        when(dataManager.getCFTypeFromPlatform("eTag")).thenReturn("trajectory");
    }

    @Test
    public void mockCreationTest() throws Exception {
        assertNotNull(dataManager);
        assertNotNull(data);
        assertNotNull(dataDao);
        assertNotNull(request);
    }

    @Test
    public void lookupByIdTest() throws Exception {
        Data persistedData = dataManager.lookupPersistedDataById("000000345HDV4");
        assertEquals(persistedData.getCfType(), "trajectory");
    }

    @Test
    public void persistDataTest() throws Exception {
        dataManager.persistData(data, request);
        Data persistedData = dataManager.lookupPersistedDataById("000000345HDV4");
        assertEquals(persistedData.getCfType(), "trajectory");
    }

    @Test
    public void updateDataTest() throws Exception {
        data.setCfType("profile"); // Update data cfType.
        dataManager.updatePersistedData(data);
        Data persistedData = dataManager.lookupPersistedDataById("000000345HDV4");
        assertEquals(persistedData.getCfType(), "profile");
    }

    @Test(expected = DataRetrievalFailureException.class)
    public void deleteDataTest() throws Exception {
        dataManager.deletePersistedData("000000345HDV4");
        dataManager.lookupPersistedDataById("000000345HDV4");
    }

    @Test
    public void getUploadDirTest() throws Exception {
        String uploadsDir = dataManager.getUploadDir();
        assertEquals(uploadsDir, "/dev/null");
    }

    @Test
    public void getDownloadDirTest() throws Exception {
        String downloadsDir = dataManager.getDownloadDir();
        assertEquals(downloadsDir, "/tmp");
    }

    @Test
    public void convertToCSVTest() throws Exception {
        String success = dataManager.convertToCSV("000000345HDV4", "test.xls");
        assertEquals(success, "true");
    }

    @Test
    public void parseDataFileByLineTest() throws Exception {
        String jsonString = dataManager.parseDataFileByLine("000000345HDV4", "test.xls");
        assertEquals(jsonString, "{\"x\":5,\"y\":6}");
    }

    @Test
    public void getDelimiterSymbolTest() throws Exception {
        String symbol = dataManager.getDelimiterSymbol("Comma");
        assertEquals(symbol, ",");
    }

    @Test
    public void getCFTypeFromPlatformTest() throws Exception {
        String cfType = dataManager.getCFTypeFromPlatform("eTag");
        assertNotEquals(cfType, "profile");
    }
}