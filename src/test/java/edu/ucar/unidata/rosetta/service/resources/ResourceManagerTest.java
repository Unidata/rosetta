package edu.ucar.unidata.rosetta.service.resources;

import edu.ucar.unidata.rosetta.domain.resources.RosettaResource;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ResourceManagerTest {

    private ResourceManager resourceManager;
    private HttpServletRequest request;
    private File file;


    @Before
    public void setup() throws Exception {
        // The ResourceManager to test.
        resourceManager = mock(ResourceManager.class);
        request = mock(HttpServletRequest.class);
        file = mock(File.class);

        // loadResources() returns a Map<String, Object> where Object is a List.
        when(resourceManager.loadResources()).thenReturn(new ArrayList<RosettaResource>());

    }

    @Test
    public void MockCreationTest() throws Exception {
        assertNotNull(resourceManager);
        assertNotNull(request);
        assertNotNull(file);
    }

    @Test
    public void loadResourcesTest() throws Exception {
        assertTrue(resourceManager.loadResources() instanceof List);
    }

}