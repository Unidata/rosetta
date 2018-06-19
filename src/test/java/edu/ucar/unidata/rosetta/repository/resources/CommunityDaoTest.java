package edu.ucar.unidata.rosetta.repository.resources;

import edu.ucar.unidata.rosetta.domain.resources.Community;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommunityDaoTest {

    private JdbcCommunityDao communityDao;
    private Community community;
    @Before
    public void setUp() throws Exception {
        communityDao = mock(JdbcCommunityDao.class);

        List<String> fileTypes = new ArrayList<>();
        fileTypes.add("EOL Composite Sounding File");
        fileTypes.add("GeoCSV");

        community = new Community();
        community.setName("Atmospheric Sciences");
        community.setId(123);
        community.setFileType(fileTypes);

        when(communityDao.lookupCommunitiesByFileType("GeoCSV")).thenReturn(Arrays.asList(community));
        when(communityDao.lookupCommunityByName("Atmospheric Sciences")).thenReturn(community);
        when(communityDao.getCommunities()).thenReturn(Arrays.asList(community));
    }

    @Test
    public void getCommunities() throws Exception {
        List<Community> communities = communityDao.getCommunities();
        assertTrue(communities.size() == 1);
    }

    @Test
    public void lookupCommunitiesByFileType() throws Exception {
        List<Community> communities = communityDao.lookupCommunitiesByFileType("GeoCSV");
        assertTrue(communities.size() == 1);
    }

    @Test
    public void lookupCommunityByName() throws Exception {
        Community community = communityDao.lookupCommunityByName("Atmospheric Sciences");
        assertTrue(community.getId() == 123);
    }

    @Test
    public void mockCreationTest() throws Exception {
        assertNotNull(community);
        assertNotNull(communityDao);
    }


}