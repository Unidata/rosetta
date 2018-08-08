package edu.ucar.unidata.rosetta.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.repository.wizard.JdbcDataDao;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataRetrievalFailureException;

public class DataDaoTest {

  private JdbcDataDao dataDao;
  private Data data;

  @Test(expected = DataRetrievalFailureException.class)
  public void deletePersistedDataTest() throws Exception {
    dataDao.deletePersistedData("000000345HDV4");
    dataDao.lookupById("000000345HDV4");
  }

  @Test
  public void lookupByIdTest() throws Exception {
    Data persistedData = dataDao.lookupById("000000345HDV4");
    assertEquals(persistedData.getCfType(), "trajectory");
  }

  @Test
  public void mockCreationTest() throws Exception {
    assertNotNull(data);
    assertNotNull(dataDao);
  }

  @Test
  public void persistDataTest() throws Exception {
    dataDao.persistData(data);
    Data persistedData = dataDao.lookupById("000000345HDV4");
    assertEquals(persistedData.getCfType(), "trajectory");
  }

  @Before
  public void setUp() throws Exception {
    dataDao = mock(JdbcDataDao.class);

    data = new Data();
    data.setId("000000345HDV4");
    data.setCfType("trajectory");
    data.setSubmit("Next");

    when(dataDao.lookupById("000000345HDV4")).thenReturn(data);
    doThrow(new DataRetrievalFailureException(
        "Unable to find persisted Data object corresponding to id " + data.getId())).when(dataDao)
        .deletePersistedData(data.getId());
  }

  @Test
  public void updatePersistedDataTest() throws Exception {
    data.setCfType("profile"); // Update data cfType.
    dataDao.updatePersistedData(data);
    Data persistedData = dataDao.lookupById("000000345HDV4");
    assertEquals(persistedData.getCfType(), "profile");
  }
}