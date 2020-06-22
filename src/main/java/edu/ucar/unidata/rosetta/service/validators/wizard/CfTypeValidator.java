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
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator class that corresponds to the data collected via the CfTypeController. ID is not set yet. The user must
 * provide either the platform or CF type value. Required and non-required user input data that is not empty is
 * evaluated for naughty strings and chars (see CommonValidator), and passing that is confirmed to be legitimate by
 * comparing with the persisted approved resources. Populates the Errors object if validation fails.
 */
@Component("CfTypeValidator")
public class CfTypeValidator extends CommonValidator implements Validator {

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
    logger.info(data.toString());
    String cfType = data.getCfType();
    String platform = data.getPlatform();
    String metadataProfile = data.getMetadataProfile();

    // Confirm user provided required data.
    validateRequiredData(cfType, platform, errors);

    // No errors so far, validate the actual data.
    if (!errors.hasErrors()) {
      if (!validateNotEmpty(cfType)) { // CF type provided.

        // Check for dubious input.
        findDubiousInput(cfType, errors);
        if (!validateNotEmpty(metadataProfile)) { // metadataProfile value can be null
          findDubiousInput(metadataProfile, errors);
        }

        // CF type data contains nothing naughty, confirm user-provided data is a legit CF type.
        if (!errors.hasGlobalErrors()) {
          validateCfType(cfType, errors);
        }

        // Metadata profile data contains nothing naughty, confirm user-provided data is a legit metadata profile.
        if (!errors.hasGlobalErrors() && !validateNotEmpty(metadataProfile)) {
          validateMetadataProfile(metadataProfile, errors);
        }

      } else { // Platform provided.
        // Check for dubious input.
        findDubiousInput(platform, errors);

        // Platform contains nothing naughty, confirm user-provided data is a legit platform.
        if (!errors.hasGlobalErrors()) {
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
    if (!blessedCfTypeNames.contains(StringUtils.replaceChars(cfType, "_", " "))) {
      logError("Invalid CF type value submitted: " + cfType);
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
    logger.info(metadataProfile);
    // Get the list of persisted & blessed metadata profile names.
    List<String> blessedMetadataProfileNames = resourceManager.getMetadataProfileNames();
    for (String profile : userProvidedMetadataProfiles) {
      // Check if the user-provided metadata profile is one of the approved profiles.
      if (!blessedMetadataProfileNames.contains(StringUtils.replaceChars(profile, "_", " "))) {
        logError("Invalid metadata profile value submitted: " + profile);
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
    if (!blessedPlatformNames.contains(StringUtils.replaceChars(platform, "_", " "))) {
      logError("Invalid platform value submitted by: " + platform);
      errors.reject("platform.invalid");
    }
  }

  /**
   * Checks to make sure either the platform or CF type was provided by the user. (Both cannot be empty). The platform
   * is associated with a CF type and will be used to determine the CF type value at a later stage in the program.
   * <p>
   * Populates the Errors object if validation fails.
   *
   * @param cfType   The user-provided CF type.
   * @param platform The user-provided platform.
   * @param errors   Object to hold validation errors.
   */
  private void validateRequiredData(String cfType, String platform, Errors errors) {
    if (validateNotEmpty(cfType) && validateNotEmpty(platform)) {
      logError("Required data CF type or platform not provided");
      errors.reject("cfType.missingData");
    }
  }
}
