/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.validators.wizard;

import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.service.validators.CommonValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates a Data object containing user input.
 *
 * @author oxelson@ucar.edu
 */
@Component
public class CFTypeValidator extends CommonValidator implements Validator {

  @Override
  public boolean supports(Class clazz) {
    return Data.class.equals(clazz);
  }

  @Override
  public void validate(Object obj, Errors errors) {
    Data data = (Data) obj;
    String cfType = data.getCfType();
    String platform = data.getPlatform();
    validateInput(cfType, errors);
    validateInput(platform, errors);
    validateNotEmpty(cfType, platform, errors);
  }

  /**
   * Checks to make sure either the platform or cfType was selected by the user. (Both cannot be
   * empty).  The platform is associated with a cfType and will be converted into the proper cfType
   * value at a later stage in the program.
   *
   * @param cfType The cfType selected by the user.
   * @param platform The platform selected by the user.
   * @param errors Validation errors.
   */
  private void validateNotEmpty(String cfType, String platform, Errors errors) {
    if (cfType == null && platform == null) {
      errors.reject(null, "You must select either a platform or specify a CF type.");
    }
  }
}
