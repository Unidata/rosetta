/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.validators.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.service.validators.CommonValidator;
import org.apache.commons.lang3.StringUtils;
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
   * @param clazz  The Object class to validate
   * @return true if class can be validated
   */
  public boolean supports(Class clazz) {
    return WizardData.class.equals(clazz);
  }

  /**
   * Validates the user input contained in the WizardData object.
   *
   * @param target  The target object to validate.
   * @param errors  Object in which to store any validation errors.
   */
  @Override
  public void validate(Object target, Errors errors) {
    WizardData wizardData = (WizardData) target;
    validateCfType(wizardData.getCfType(), errors);
//    validateCommunity(wizardData.getCommunity(), errors);
//    validateDataFileType(wizardData.getDataFileType(), errors);
//    validateDelimiter(wizardData.getDelimiter(), errors);
//    validateHeaderLineNumbers(wizardData.getHeaderLineNumbers(), errors);
//    validateId(wizardData.getId(), errors);
//    validateMetadataProfile(wizardData.getMetadataProfile(), errors);
//    validateNoHeaderLines(wizardData.hasNoHeaderLines(), errors);
//    validatePlatform(wizardData.getPlatform(), errors);
//    validateVariableMetadata(wizardData.getVariableMetadata(), errors);
//    validateGlobalMetadata(wizardData.getGlobalMetadata(), errors);
  }

  private void validateCfType(String cfType, Errors errors) {
    // Check for dubious input.
    validateInput("cfType", cfType, errors);
    if (errors.getFieldErrorCount("cfType") > 0 ) {
      validateNotEmpty("cfType", cfType, errors);
    }
  }


}
