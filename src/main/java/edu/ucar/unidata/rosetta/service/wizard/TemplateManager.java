/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */
package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;

/**
 * Service for handling/building Template objects and template files (rosetta.template).
 */
public interface TemplateManager {

  /**
   * Retrieves persisted data to create a Template object which is used to
   * write to a template file and a transaction log.
   *
   * @param id The unique transaction ID associated with this template.
   * @return A Template object.
   * @throws RosettaFileException If unable to create the template file.
   */
  Template createTemplate(String id) throws RosettaFileException;

}
