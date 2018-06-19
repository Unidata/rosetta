package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Delimiter;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DelimiterDaoTest {

    private JdbcDelimiterDao delimiterDao;
    private Delimiter tabDelimiter;
    private Delimiter commaDelimiter;

    @Before
    public void setUp() throws Exception {
        delimiterDao = mock(JdbcDelimiterDao.class);

        tabDelimiter = new Delimiter();
        tabDelimiter.setName("Tab");
        tabDelimiter.setCharacterSymbol("\t");

        commaDelimiter = new Delimiter();
        commaDelimiter.setName("Comma");
        commaDelimiter.setCharacterSymbol(",");

        when(delimiterDao.getDelimiters()).thenReturn(Arrays.asList(tabDelimiter, commaDelimiter));
        when(delimiterDao.lookupDelimiterByName("Tab")).thenReturn(tabDelimiter);
    }

    @Test
    public void getDelimitersTest() throws Exception {
        List<Delimiter> delimiters = delimiterDao.getDelimiters();
        assertTrue(delimiters.size() == 2);
    }

    @Test
    public void lookupDelimiterByNameTest() throws Exception {
        Delimiter delimiter  = delimiterDao.lookupDelimiterByName("Tab");
        assertEquals(delimiter.getCharacterSymbol(), "\t");
    }

    @Test
    public void mockCreationTestTest() throws Exception {
        assertNotNull(tabDelimiter);
        assertNotNull(commaDelimiter);
        assertNotNull(delimiterDao);
    }


}