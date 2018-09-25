/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;

/**
 * Service for handling/building Template objects.
 */
public interface TemplateManager {

    public Template createTemplate(String id) throws RosettaFileException;

}
