package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.CfType;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CfTypeDaoTest {

    private JdbcCfTypeDao cfTypeDao;
    private CfType cfType1;
    private CfType cfType2;

    @Before
    public void setUp() throws Exception {
        cfTypeDao = mock(JdbcCfTypeDao.class);

        cfType1 = new CfType();
        cfType1.setId(123);
        cfType1.setName("Trajectory");

        cfType2 = new CfType();
        cfType2.setId(345);
        cfType2.setName("Profile");

        when(cfTypeDao.getCfTypes()).thenReturn(Arrays.asList(cfType1, cfType2));
        when(cfTypeDao.lookupCfTypeById(123)).thenReturn(cfType1);
        when(cfTypeDao.lookupCfTypeByName("Profile")).thenReturn(cfType2);
    }
    
    @Test
    public void getCfTypesTest() throws Exception {
        List<CfType> cfTypes = cfTypeDao.getCfTypes();
        assertTrue(cfTypes.size() == 2);
    }

    @Test
    public void lookupCfTypeByIdTest() throws Exception {
        CfType cfType = cfTypeDao.lookupCfTypeById(123);
        assertEquals(cfType.getName(), "Trajectory");
    }

    @Test
    public void lookupCfTypeByNameTest() throws Exception{
        CfType cfType = cfTypeDao.lookupCfTypeByName("Profile");
        assertEquals(cfType.getId(), 345);
    }

    @Test
    public void mockCreationTest() throws Exception {
        assertNotNull(cfType1);
        assertNotNull(cfType2);
        assertNotNull(cfTypeDao);
    }

}