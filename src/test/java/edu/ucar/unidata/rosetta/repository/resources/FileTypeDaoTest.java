package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.FileType;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileTypeDaoTest {

    private JdbcFileTypeDao fileTypeDao;
    private FileType fileType1;
    private FileType fileType2;

    @Test
    public void getFileTypes() throws Exception {
        List<FileType> fileTypes = fileTypeDao.getFileTypes();
        assertTrue(fileTypes.size() == 2);
    }

    @Test
    public void lookupFileTypeById() throws Exception {
        FileType fileType = fileTypeDao.lookupFileTypeById(123);
        assertEquals(fileType.getName(), "GeoCSV");
    }

    @Test
    public void lookupFileTypeByName() throws Exception {
        FileType fileType = fileTypeDao.lookupFileTypeByName("eTuff");
        assertEquals(fileType.getId(), 345);
    }

    @Test
    public void mockCreationTest() throws Exception {
        assertNotNull(fileType1);
        assertNotNull(fileType2);
        assertNotNull(fileTypeDao);
    }

    @Before
    public void setUp() throws Exception {
        fileTypeDao = mock(JdbcFileTypeDao.class);

        fileType1 = new FileType();
        fileType1.setId(123);
        fileType1.setName("GeoCSV");

        fileType2 = new FileType();
        fileType2.setId(345);
        fileType2.setName("eTuff");

        when(fileTypeDao.getFileTypes()).thenReturn(Arrays.asList(fileType1, fileType2));
        when(fileTypeDao.lookupFileTypeById(123)).thenReturn(fileType1);
        when(fileTypeDao.lookupFileTypeByName("eTuff")).thenReturn(fileType2);
    }

}