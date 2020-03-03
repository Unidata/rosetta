/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import javax.servlet.http.HttpServletRequest;

/**
 * Service for handling data collected from the wizard.
 */
public interface WizardManager {

  String getTemplateFile(String id);

  /**
   * Processes all of the provided data and creates a netCDF file.
   *
   * @param id The unique ID corresponding to this transaction.
   * @return The location of the created netCDF file.
   * @throws RosettaFileException If unable to create the template file.
   * @throws RosettaDataException If unable to parse data file with delimiter.
   */
  public String convertToNetcdf(String id) throws RosettaFileException, RosettaDataException;

  /**
   * Determines whether the custom file attributes step needs to be visited in the wizard.
   *
   * @param id The ID corresponding to the persisted data needed to make this determination.
   * @return true if custom file attributes step needs to be visited; otherwise false;
   */
  public boolean customFileAttributesStep(String id);

  /**
   * Looks up and retrieves persisted wizard data using the given ID.
   *
   * @param id The ID corresponding to the data to retrieve.
   * @return The persisted wizard data.
   */
  public WizardData lookupPersistedWizardDataById(String id);

  /**
   * Retrieves the data file from disk and parses it by line, converting it into a JSON string. Used in the wizard for
   * header line selection.
   *
   * @param id The unique id associated with the file (a sub directory in the uploads directory).
   * @return A JSON string of the file data parsed by line.
   * @throws RosettaFileException For any file I/O or JSON conversions problems.
   */
  public String parseDataFileByLine(String id) throws RosettaFileException;

  /**
   * Processes the data collected from the wizard for the CF type step.
   *
   * @param id The unique ID corresponding to already persisted data (may be null).
   * @param wizardData The WizardData object containing user-submitted CF type information.
   * @param request HttpServletRequest used to make unique IDs for new data.
   * @throws RosettaDataException If unable to lookup the metadata profile.
   */
  public void processCfType(String id, WizardData wizardData, HttpServletRequest request)
      throws RosettaDataException, RosettaFileException;

  /**
   * Processes the data submitted by the user containing custom data file attributes.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param wizardData The WizardData containing custom file type attribute data.
   */
  public void processCustomFileTypeAttributes(String id, WizardData wizardData);

  /**
   * Processes the data submitted by the user containing global metadata information. Since this
   * is the final step of collecting data in the wizard, the uploaded data file is converted to
   * netCDF format in preparation for user download.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param wizardData The WizardData containing the global metadata.
   * @throws RosettaDataException If unable to populate the metadata object.
   */
  public void processGlobalMetadata(String id, WizardData wizardData) throws RosettaDataException;

  /**
   * Determines the next step in the wizard based the user specified data file type. This method is called when there
   * is a divergence of possible routes through the wizard.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @return The next step to redirect the user to in the wizard.
   */
  public String processNextStep(String id);

  /**
   * Determines the previous step in the wizard based the user specified data file type. This method is called when
   * there is a divergence of possible routes through the wizard.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @return The previous step to redirect the user to in the wizard.
   */
  public String processPreviousStep(String id);

  /**
   * Processes the data submitted by the user containing variable metadata information.
   *
   * @param id The unique ID corresponding to already persisted data.
   * @param wizardData The WizardData containing variable metadata information.
   */
  public void processVariableMetadata(String id, WizardData wizardData);

  /**
   * Updates persisted wizard data with the information in the provided WizardData object.
   *
   * @param wizardData The updated wizard data.
   */
  public void updatePersistedWizardData(WizardData wizardData);
}
