package edu.ucar.unidata.rosetta.config;

import edu.ucar.unidata.rosetta.service.ConvertManager;
import edu.ucar.unidata.rosetta.service.DataManager;
import edu.ucar.unidata.rosetta.service.FileManager;
import edu.ucar.unidata.rosetta.service.MetadataManager;
import edu.ucar.unidata.rosetta.service.resources.ResourceManager;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mock object dependancies used for controller tests (found via component scan in applicationContext-test.xml)
 */
@Configuration
public class TestContext {

    @Bean
    public ConvertManager convertManager() {
        return Mockito.mock(ConvertManager.class);
    }

    @Bean
    public DataManager dataManager() {
        return Mockito.mock(DataManager.class);
    }

    @Bean
    public FileManager fileParserManager() {
        return Mockito.mock(FileManager.class);
    }

    @Bean
    public MetadataManager metadataManager() {
        return Mockito.mock(MetadataManager.class);
    }

    @Bean
    public ResourceManager resourceManager() {
        return Mockito.mock(ResourceManager.class);
    }


}
