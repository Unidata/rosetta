package edu.ucar.unidata.rosetta.config;

import edu.ucar.unidata.rosetta.init.resources.ResourceLoader;
import edu.ucar.unidata.rosetta.service.user.UserManager;
import edu.ucar.unidata.rosetta.service.wizard.DataManager;
import edu.ucar.unidata.rosetta.service.wizard.FileManager;
import edu.ucar.unidata.rosetta.service.wizard.MetadataManager;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mock object dependencies used for controller tests (found via component scan in
 * applicationContext-test.xml)
 */
@Configuration
public class TestContext {

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
  public ResourceLoader resourceManager() {
    return Mockito.mock(ResourceLoader.class);
  }

  @Bean
  public UserManager userManager() {
    return Mockito.mock(UserManager.class);
  }
}
