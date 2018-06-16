package edu.ucar.unidata.rosetta.service.user.validators;

import edu.ucar.unidata.rosetta.domain.user.User;
import edu.ucar.unidata.rosetta.service.user.UserManager;

import org.apache.log4j.Logger;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Resource;

/**
 * Validator class for the form-backing object for a User creation/registration.
 *
 * @author oxelson@ucar.edu
 */
@Component
public class CreateUserValidator extends CommonUserValidator implements Validator {

    private static final Logger logger = Logger.getLogger(CreateUserValidator.class);

    @Resource(name="userManager")
    private UserManager userManager;

    /**
     * Checks to see if Object class can be validated.
     *
     * @param clazz  The Object class to validate
     * @return true if class can be validated
     */
    @Override
    public boolean supports(Class clazz) {
        return User.class.equals(clazz);
    }

    /**
     * Validates the user input contained in the User object.
     *
     * @param obj  The target object to validate.
     * @param errors  Object in which to store any validation errors.
     */
    @Override
    public void validate(Object obj, Errors errors) {
        User user = (User) obj;
        validateUserName(user.getUserName(), errors);
        validateUserNameNotInUse(user.getUserName(), errors);
        validateFullName(user.getFullName(), errors);
        validatePassword("password",  user.getPassword(), errors);
        validatePassword("confirmPassword", user.getConfirmPassword(), errors);
        comparePasswords(user.getPassword(), user.getConfirmPassword(), errors);
        validateAccessLevel(user.getAccessLevel(), errors);
        validateAccountStatus(user.getAccountStatus(), errors);
        validateEmailAddress(user.getEmailAddress(), errors);
        validateEmailAddressNotInUse(user.getEmailAddress(), errors);
    }

    /**
     * Validates the email address is not already in use.
     *
     * @param input  The user input to validate.
     * @param errors  Object in which to store any validation errors.
     */
    private void validateEmailAddressNotInUse(String input, Errors errors) {
        logger.debug("Confirming email address is not already in use.");

        // Already exists.
        if (userManager.userExists("emailAddress", input))
            errors.rejectValue("emailAddress", "emailAddress.alreadyInUse");
    }
}
