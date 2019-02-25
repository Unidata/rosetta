/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.validators.user;

import edu.ucar.unidata.rosetta.domain.user.User;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator class for the form-backing object for resetting a User's password.
 *
 * @author oxelson@ucar.edu
 */
@Component
public class PasswordValidator extends CommonUserValidator implements Validator {

  /**
   * Checks to see if Object class can be validated.
   *
   * @param clazz The Object class to validate
   * @return true if class can be validated
   */
  @Override
  public boolean supports(Class clazz) {
    return User.class.equals(clazz);
  }

  /**
   * Validates the user input contained in the User object.
   *
   * @param obj The target object to validate.
   * @param errors Object in which to store any validation errors.
   */
  @Override
  public void validate(Object obj, Errors errors) {
    User user = (User) obj;
    validatePassword("password", user.getPassword(), errors);
    validatePassword("confirmPassword", user.getConfirmPassword(), errors);
    comparePasswords(user.getPassword(), user.getConfirmPassword(), errors);
  }
}
