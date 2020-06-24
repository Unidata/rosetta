/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.validators.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.service.validators.CommonValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component("wizardDataValidator")
public class WizardDataValidator extends CommonValidator implements Validator {

  private static final Logger logger = LogManager.getLogger(WizardDataValidator.class);

  /**
   * Checks to see if Object class can be validated.
   *
   * @param clazz The Object class to validate
   * @return true if class can be validated
   */
  public boolean supports(Class clazz) {
    return WizardData.class.equals(clazz);
  }

  /**
   * Validates the user input contained in the WizardData object.
   *
   * @param obj The object to validate.
   * @param errors Object in which to store any validation errors.
   */
  @Override
  public void validate(Object obj, Errors errors) {
    WizardData wizardData = (WizardData) obj;
    validateCfType(wizardData.getCfType(), errors); // required
    // validateCommunity(wizardData.getCommunity(), errors); // optional
    // validateDataFileType(wizardData.getDataFileType(), errors);
    // validateDelimiter(wizardData.getDelimiter(), errors);
    // validateHeaderLineNumbers(wizardData.getHeaderLineNumbers(), errors);
    // validateId(wizardData.getId(), errors); // required
    // validateMetadataProfile(wizardData.getMetadataProfile(), errors); // required
    // validateNoHeaderLines(wizardData.hasNoHeaderLines(), errors);
    // validatePlatform(wizardData.getPlatform(), errors); // optional
    // validateVariableMetadata(wizardData.getVariableMetadata(), errors);
    // validateGlobalMetadata(wizardData.getGlobalMetadata(), errors);
  }

  /**
   * Validates the CF type data the user selected/inputted.
   * CF type data is required and must be present.
   *
   * @param cfType The CF type data to validate.
   * @param errors Object in which to store any validation errors.
   */
  private void validateCfType(String cfType, Errors errors) {
    // Cannot by empty or null.
    // validateNotEmpty("cfType", cfType, errors);
    if (errors.getFieldErrorCount("cfType") > 0) {
      // Check for dubious input.
      findDubiousInput(cfType, errors);
    }

  }


}
