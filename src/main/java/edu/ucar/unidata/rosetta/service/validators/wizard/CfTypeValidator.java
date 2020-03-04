/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.validators.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.validators.CommonValidator;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator class that corresponds to the data collected via the CfTypeController. ID is not set yet.
 * The user must provide either the platform or CF type value.  Required and non-required user input data that is
 * not empty is evaluated for naughty strings and chars (see CommonValidator), and passing that is confirmed to be
 * legitimate by comparing with the persisted approved resources. Populates the Errors object if validation fails.
 */
@Component("CfTypeValidator")
public class CfTypeValidator extends CommonValidator implements Validator {

  private static final Logger logger = LogManager.getLogger(WizardDataValidator.class);

  @Resource(name = "resourceManager")
  private ResourceManager resourceManager;

  /**
   * Checks to see if Object class can be validated.
   *
   * @param clazz The Object class to validate
   * @return true if class can be validated
   */
  @Override
  public boolean supports(@NotNull Class clazz) {
    return WizardData.class.isAssignableFrom(clazz);
  }

  /**
   * Validates the CF type data the user selected/inputted.
   *
   * @param obj    The object to validate.
   * @param errors Object in which to store any validation errors.
   */
  @Override
  public void validate(@NotNull Object obj, @NotNull Errors errors) {
    WizardData data = (WizardData) obj;
    String cfType = data.getCfType();
    String platform = data.getPlatform();
    String metadataProfile = data.getMetadataProfile();

    // Confirm user provided required data.
    validateRequiredData(cfType, platform, errors);

    // No errors so far, validate the actual data.
    if (!errors.hasErrors()) {

      if (!StringUtils.isBlank(cfType)) { // CF type provided.
        // Check for dubious input.
        validateInput("cfType", cfType, errors);
        validateInput("metadataProfile", metadataProfile, errors);

        // CF type data contains nothing naughty, confirm user-provided data is a legit CF type.
        if (errors.getFieldErrorCount("cfType") > 0) {
          validateCfType(cfType, errors);
        }

        // Metadata profile data contains nothing naughty, confirm user-provided data is a legit metadata profile.
        if (errors.getFieldErrorCount("metadataProfile") > 0) {
          validateMetadataProfile(metadataProfile, errors);
        }
      } else {  // Platform provided.
        // Check for dubious input.
        validateInput("platform", platform, errors);

        // Platform contains nothing naughty, confirm user-provided data is a legit platform.
        if (errors.getFieldErrorCount("platform") > 0) {
          validatePlatform(platform, errors);
        }
      }
    }
  }


  /**
   * Verifies the Cf type provided by the user is one of the blessed/approved CF types. Populates the Errors object if
   * validation fails.
   *
   * @param cfType The user-provided CF type.
   * @param errors Object to hold validation errors.
   */
  private void validateCfType(String cfType, Errors errors) {
    // Get the list of persisted & blessed CF types names.
    List<String> blessedCfTypeNames = resourceManager.getCfTypeNames();
    if (!blessedCfTypeNames.contains(cfType)) {
      logger.info("  VALIDATION ERROR  Invalid CF type value submitted: " + cfType);
      errors.reject("cfType.invalid");
    }
  }

  /**
   * Verifies the metadata profile provided by the user is one of the blessed/approved metadata profiles. This data can
   * be a string of comma-separated if more than on metadata profile was provided by the user. Populates the Errors
   * object if validation fails.
   *
   * @param metadataProfile The user-provided metadata profile.
   * @param errors          Object to hold validation errors.
   */
  private void validateMetadataProfile(String metadataProfile, Errors errors) {
    String[] userProvidedMetadataProfiles = StringUtils.split(metadataProfile, ",");
    // Get the list of persisted & blessed metadata profile names.
    List<String> blessedMetadataProfileNames = resourceManager.getMetadataProfileNames();
    for (String name : userProvidedMetadataProfiles) {
      // Check if the user-provided metadata profile name is one of the approved profile names.
      if (!blessedMetadataProfileNames.contains(name)) {
        logger.info("  VALIDATION ERROR  Invalid metadata profile value submitted: " + name);
        errors.reject("metadataProfile.invalid");
        return;
      }
    }
  }

  /**
   * Verifies the platform provided by the user is one of the blessed/approved platforms. Populates the Errors object if
   * validation fails.
   *
   * @param platform The user-provided platform.
   * @param errors   Object to hold validation errors.
   */
  private void validatePlatform(String platform, Errors errors) {
    // Get the list of persisted & blessed platform names.
    List<String> blessedPlatformNames = resourceManager.getPlatformNames();
    if (!blessedPlatformNames.contains(platform)) {
      logger.info("  VALIDATION ERROR  Invalid platform value submitted: " + platform);
      errors.reject("platform.invalid");
    }
  }

  /**
   * Checks to make sure either the platform or CF type was provided by the user. (Both cannot be empty). The platform
   * is associated with a CF type and will be used to determine the CF type value at a later stage in the program.
   * Populates the Errors object if validation fails.
   *
   * @param cfType   The user-provided CF type.
   * @param platform The user-provided  platform.
   * @param errors   Object to hold validation errors.
   */
  private void validateRequiredData(String cfType, String platform, Errors errors) {
    if (StringUtils.isBlank(cfType) && StringUtils.isBlank(platform)) {
      logger.info("  VALIDATION ERROR   required data CF type or platform not provided.");
      errors.reject("cfType.missingData");
    }
  }
}
