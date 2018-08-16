package edu.ucar.unidata.rosetta.config;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.init.resources.ResourceLoader;
import edu.ucar.unidata.rosetta.repository.wizard.WizardDataDao;
import edu.ucar.unidata.rosetta.repository.wizard.JdbcWizardDataDao;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.user.UserManager;
import edu.ucar.unidata.rosetta.service.wizard.*;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mock object dependencies used for controller tests (found via component scan in applicationContext-test.xml)
 */
@Configuration
public class TestContext {

    @Bean
    public WizardManager wizardManager() {
        return Mockito.mock(WizardManager.class);
    }

    @Bean
    public ResourceManager resourceManager() {
        return Mockito.mock(ResourceManager.class);
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
    public ResourceLoader resourceLoader() {
        return Mockito.mock(ResourceLoader.class);
    }
    @Bean
    public UploadedFileManager uploadedFileManager() {
        return Mockito.mock(UploadedFileManager.class);
    }

    @Bean
    public UserManager userManager() {
        return Mockito.mock(UserManager.class);
    }

}
