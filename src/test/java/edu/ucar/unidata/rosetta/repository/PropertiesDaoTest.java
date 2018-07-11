package edu.ucar.unidata.rosetta.repository;

import edu.ucar.unidata.rosetta.domain.RosettaProperties;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataRetrievalFailureException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertiesDaoTest {

    private JdbcPropertiesDao propertiesDao;
    private RosettaProperties rosettaProperties;

    @Before
    public void setUp() throws Exception {
        propertiesDao = mock(JdbcPropertiesDao.class);

        rosettaProperties = new RosettaProperties() ;
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

    @Test
    public void lookupDownloadDirectoryTest() throws Exception {
        String downloadsDir = propertiesDao.lookupDownloadDirectory();
        assertEquals(downloadsDir, "/tmp");
    }
}