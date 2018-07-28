package edu.ucar.unidata.rosetta.repository.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.ucar.unidata.rosetta.domain.Metadata;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataRetrievalFailureException;

public class MetadataDaoTest {

  private JdbcMetadataDao metadataDao;
  private Metadata metadata1;
  private Metadata metadata2;

  @Before
  public void setUp() throws Exception {
    metadataDao = mock(JdbcMetadataDao.class);

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

    when(metadataDao.lookupMetadata("000000345HDV4"))
        .thenReturn(Arrays.asList(metadata1, metadata2));
    when(metadataDao.lookupMetadata("000000345HDV4", "general"))
        .thenReturn(Arrays.asList(metadata1));
    doThrow(new DataRetrievalFailureException(
        "Unable to delete metadata entries corresponding to id " + metadata1.getId()))
        .when(metadataDao).deletePersistedMetadata(metadata1.getId());
    doThrow(new DataRetrievalFailureException(
        "Unable to delete metadata entries corresponding to id " + metadata1.getId() + " and type "
            + metadata1.getType())).when(metadataDao)
        .deletePersistedMetadata(metadata1.getId(), metadata1.getType());
  }

  @Test
  public void mockCreationTest() throws Exception {
    assertNotNull(metadata1);
    assertNotNull(metadata2);
    assertNotNull(metadataDao);
  }

  @Test
  public void lookupMetadataByIdTest() throws Exception {
    List<Metadata> persistedMetadata = metadataDao.lookupMetadata("000000345HDV4");
    assertTrue(persistedMetadata.size() == 2);
    for (Metadata metadata : persistedMetadata) {
      assertEquals(metadata.getMetadataKey(), "temp");
    }
  }

  @Test
  public void lookupMetadataByIdAndTypeTest() throws Exception {
    List<Metadata> persistedMetadata = metadataDao.lookupMetadata("000000345HDV4", "general");
    assertTrue(persistedMetadata.size() == 1);
    Metadata metadata = persistedMetadata.get(0);
    assertEquals(metadata.getMetadataValue(), "degrees C");
  }

  @Test
  public void persistMetadataObjectListTest() throws Exception {
    metadataDao.persistMetadata(Arrays.asList(metadata1, metadata2));
    List<Metadata> persistedMetadata = metadataDao.lookupMetadata("000000345HDV4");
    assertTrue(persistedMetadata.size() == 2);
    for (Metadata metadata : persistedMetadata) {
      assertEquals(metadata.getMetadataKey(), "temp");
    }
  }

  @Test
  public void persistMetadataObjectTest() throws Exception {
    metadataDao.persistMetadata(metadata1);
    List<Metadata> persistedMetadata = metadataDao.lookupMetadata("000000345HDV4", "general");
    Metadata metadata = persistedMetadata.get(0);
    assertEquals(metadata.getMetadataValue(), "degrees C");
  }

  @Test
  public void updatePersistedMetadataObjectListTest() throws Exception {
    metadata1.setMetadataKey("internal_temperature"); // Update
    metadata2.setMetadataKey("external_temperature"); // Update
    metadataDao.updatePersistedMetadata(Arrays.asList(metadata1, metadata2));
    List<Metadata> persistedMetadata = metadataDao.lookupMetadata("000000345HDV4");
    // They should not be equal.
    assertTrue(!persistedMetadata.get(0).getMetadataKey()
        .equals(persistedMetadata.get(1).getMetadataKey()));
  }

  @Test
  public void updatePersistedMetadataObjectTest() throws Exception {
    metadata1.setMetadataValue("degrees F"); // Update
    metadataDao.updatePersistedMetadata(metadata1);
    List<Metadata> persistedMetadata = metadataDao.lookupMetadata("000000345HDV4", "general");
    assertEquals(persistedMetadata.get(0).getMetadataValue(), "degrees F");
  }

  @Test(expected = DataRetrievalFailureException.class)
  public void deletePersistedMetadataByIdTest() throws Exception {
    metadataDao.deletePersistedMetadata("000000345HDV4");
    metadataDao.lookupMetadata("000000345HDV4");
  }

  @Test(expected = DataRetrievalFailureException.class)
  public void deletePersistedMetadataByIdAndTypeTest() throws Exception {
    metadataDao.deletePersistedMetadata("000000345HDV4", "general");
    metadataDao.lookupMetadata("000000345HDV4");
  }
}