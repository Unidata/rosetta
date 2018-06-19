package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.domain.Metadata;
import edu.ucar.unidata.rosetta.repository.wizard.MetadataDao;

import edu.ucar.unidata.rosetta.service.wizard.MetadataManagerImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetadataManagerTest {

    private MetadataManagerImpl metadataManager;
    private MetadataDao metadataDao;
    private Metadata metadata1;
    private Metadata metadata2;
    private GeneralMetadata generalMetadata;

    @Test(expected = DataRetrievalFailureException.class)
    public void deleteMetadataByIdTest() throws Exception {
        metadataManager.deletePersistedMetadata("000000345HDV4");
        metadataManager.lookupPersistedMetadata("000000345HDV4");
    }

    @Test(expected = DataRetrievalFailureException.class)
    public void deleteMetadataByIdAndTypeTest() throws Exception {
        metadataManager.deletePersistedMetadata("000000345HDV4", "general");
        metadataManager.lookupPersistedMetadata("000000345HDV4");
    }

    @Test
    public void getMetadataStringForClientTest() throws Exception {
        String metadata = metadataManager.getMetadataStringForClient("000000345HDV4", "general");
        assertEquals(metadata, "variableName0<>year");
    }

    @Test
    public void getStringFromParsedVariableMetadataTest() throws Exception {
        String metadata = metadataManager.getStringFromParsedVariableMetadata(Arrays.asList(metadata1, metadata2));
        assertEquals(metadata, "variableName0<>year");
    }


    @Test
    public void lookupMetadataByIdTest() throws Exception {
        List<Metadata> persistedMetadata = metadataManager.lookupPersistedMetadata("000000345HDV4");
        assertTrue(persistedMetadata.size() == 2);
        for (Metadata metadata : persistedMetadata) {
            assertEquals(metadata.getMetadataKey(), "temp");
        }
    }

    @Test
    public void lookupMetadataByIdAndTypeTest() throws Exception {
        List<Metadata> persistedMetadata = metadataManager.lookupPersistedMetadata("000000345HDV4", "general");
        assertTrue(persistedMetadata.size() == 1);
        Metadata metadata = persistedMetadata.get(0);
        assertEquals(metadata.getMetadataValue(), "degrees C");
    }

    @Test
    public void mockCreationTest() throws Exception {
        assertNotNull(metadataManager);
        assertNotNull(metadata1);
        assertNotNull(metadataDao);
        assertNotNull(generalMetadata);
    }

    @Test
    public void parseGeneralMetadataTest() throws Exception {
        List<Metadata> parsedMetadata =  metadataManager.parseGeneralMetadata(generalMetadata, "000000345HDV4");
        assertTrue(parsedMetadata.size() == 2);
    }

    @Test
    public void parseVariableMetadataTest() throws Exception {
        List<Metadata> parsedMetadata = metadataManager.parseVariableMetadata("variableName0<>year", "000000345HDV4");
        assertTrue(parsedMetadata.size() == 2);
        for (Metadata metadata : parsedMetadata) {
            assertEquals(metadata.getMetadataKey(), "temp");
        }
    }

    @Test
    public void persistMetadataObjectListTest() throws Exception {
        metadataManager.persistMetadata(Arrays.asList(metadata1, metadata2));
        List<Metadata> persistedMetadata = metadataManager.lookupPersistedMetadata("000000345HDV4");
        assertTrue(persistedMetadata.size() == 2);
        for (Metadata metadata : persistedMetadata) {
            assertEquals(metadata.getMetadataKey(), "temp");
        }
    }

    @Test
    public void persistMetadataObjectTest() throws Exception {
        metadataManager.persistMetadata(metadata1);
        List<Metadata> persistedMetadata = metadataManager.lookupPersistedMetadata("000000345HDV4", "general");
        Metadata metadata = persistedMetadata.get(0);
        assertEquals(metadata.getMetadataValue(), "degrees C");
    }

    @Before
    public void setUp() throws Exception {
        metadataManager = mock(MetadataManagerImpl.class);
        metadataDao = mock(MetadataDao.class);
        metadataManager.setMetadataDao(metadataDao);

        metadata1 = new Metadata();
        metadata1.setId("000000345HDV4");
        metadata1.setType("general");
        metadata1.setMetadataKey("temp");
        metadata1.setMetadataValue("degrees C");

        metadata2 = new Metadata();
        metadata2.setId("000000345HDV4");
        metadata2.setType("variable");
        metadata2.setMetadataKey("temp");
        metadata2.setMetadataValue("degrees F");
        generalMetadata = new GeneralMetadata();

        when(metadataManager.lookupPersistedMetadata("000000345HDV4")).thenReturn(Arrays.asList(metadata1, metadata2));
        when(metadataManager.lookupPersistedMetadata("000000345HDV4", "general")).thenReturn(Arrays.asList(metadata1));
        doThrow(new DataRetrievalFailureException("Unable to delete metadata entries corresponding to id " + metadata1.getId())).when(metadataManager).deletePersistedMetadata(metadata1.getId());
        doThrow(new DataRetrievalFailureException("Unable to delete metadata entries corresponding to id " + metadata1.getId() + " and type " + metadata1.getType())).when(metadataManager).deletePersistedMetadata(metadata1.getId(), metadata1.getType());
        when(metadataManager.parseVariableMetadata("variableName0<>year", "000000345HDV4")).thenReturn(Arrays.asList(metadata1, metadata2));
        when(metadataManager.getStringFromParsedVariableMetadata(Arrays.asList(metadata1, metadata2))).thenReturn("variableName0<>year");
        when(metadataManager.getMetadataStringForClient("000000345HDV4", "general")).thenReturn("variableName0<>year");
        when(metadataManager.parseGeneralMetadata(generalMetadata, "000000345HDV4")).thenReturn(Arrays.asList(metadata1, metadata2));
    }


    @Test
    public void updateMetadataObjectListTest() throws Exception {
        metadata1.setMetadataKey("internal_temperature"); // Update
        metadata2.setMetadataKey("external_temperature"); // Update
        metadataManager.updatePersistedMetadata(Arrays.asList(metadata1, metadata2));
        List<Metadata> persistedMetadata = metadataManager.lookupPersistedMetadata("000000345HDV4");
        // They should not be equal.
        assertTrue(!persistedMetadata.get(0).getMetadataKey().equals(persistedMetadata.get(1).getMetadataKey()));
    }

    @Test
    public void updateMetadataObjectTest() throws Exception {
        metadata1.setMetadataValue("degrees F"); // Update
        metadataManager.updatePersistedMetadata(metadata1);
        List<Metadata> persistedMetadata = metadataManager.lookupPersistedMetadata("000000345HDV4", "general");
        assertEquals(persistedMetadata.get(0).getMetadataValue(), "degrees F");
    }
}