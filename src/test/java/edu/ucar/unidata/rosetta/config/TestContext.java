/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */
package edu.ucar.unidata.rosetta.config;

import edu.ucar.unidata.rosetta.init.resources.ResourceLoader;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.batch.BatchFileManager;
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
  public BatchFileManager batchFileManager() {
    return Mockito.mock(BatchFileManager.class);
  }

  @Bean
  public WizardManager wizardManager() {
    return Mockito.mock(WizardManager.class);
  }

  @Bean
  public ResourceManager resourceManager() {
    return Mockito.mock(ResourceManager.class);
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
}
