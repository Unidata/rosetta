package edu.ucar.unidata.rosetta.service.validators.user;

import edu.ucar.unidata.rosetta.service.user.UserManager;
import edu.ucar.unidata.rosetta.service.validators.CommonValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.springframework.validation.Errors;

/**
 * Common validation methods for User objects.
 *
 * @author oxelson@ucar.edu
 */
public class CommonUserValidator extends CommonValidator {

    protected static Logger logger = Logger.getLogger(CommonUserValidator.class);

    protected static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    protected static final String USER_NAME_PATTERN = "^[a-zA-Z0-9_-]{2,}$";

    private Pattern pattern;
    private Matcher matcher;

    @Resource(name="userManager")
    private UserManager userManager;

    /**
     * Validates password and confirmation passwords to make sure they are the same.
     *
     * @param password  The password specified by the user.
     * @param confirmPassword  The confirmation of the password.
     * @param errors  Object in which to store any validation errors.
     */
    protected void comparePasswords(String password, String confirmPassword, Errors errors) {
        logger.debug("Comparing user password and confirm password entries.");

        if (!StringUtils.equals(password, confirmPassword))
            errors.rejectValue("confirmPassword", "password.match");
    }

    /**
     * Validates the admin input for the user's access level.
     *
     * @param input  The user input to validate.
     * @param errors  Object in which to store any validation errors.
     */
    protected void validateAccessLevel(int input, Errors errors) {
        logger.debug("Validating user access level.");

        // Malformed.
        if ((input > 2) || (input < 1))
            errors.rejectValue("accessLevel", "accessLevel.options");
    }

    /**
     * Validates the admin input for the user's account status.
     *
     * @param input  The user input to validate.
     * @param errors  Object in which to store any validation errors.
     */
    protected void validateAccountStatus(int input, Errors errors) {
        logger.debug("Validating user account status.");

        // Malformed.
        if (input > 1)
            errors.rejectValue("accountStatus", "accountStatus.options");
    }

    /**
     * Validates the user input for the email address.
     *
     * @param input  The user input to validate.
     * @param errors  Object in which to store any validation errors.
     */
    protected void validateEmailAddress(String input, Errors errors) {
        logger.debug("Validating user email address.");

        // Check for dubious input.
        validateInput(input, errors);

        // Is blank.
        if (StringUtils.isBlank(input))
            errors.rejectValue("emailAddress", "emailAddress.required");

        // Malformed.
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(input);
        if (!matcher.matches())
            errors.rejectValue("emailAddress", "emailAddress.wellFormed");
    }


    /**
     * Validates the user input for the full name.
     *
     * @param input  The user input to validate.
     * @param errors  Object in which to store any validation errors.
     */
    protected void validateFullName(String input, Errors errors) {
        logger.debug("Validating user full name.");

        // Check for dubious input.
        validateInput(input, errors);

        // Is blank.
        if (StringUtils.isBlank(input))
            errors.rejectValue("fullName", "fullName.required");

        // Is of the incorrect length.
        if ((StringUtils.length(input) < 2) || (StringUtils.length(input) > 75))
            errors.rejectValue("fullName", "fullName.length");
    }

    /**
     * Validates the user input for the password.
     *
     * @param formField  The form field corresponding to the user input.
     * @param input  The user input to validate.
     * @param errors  Object in which to store any validation errors.
     */
    protected void validatePassword(String formField, String input, Errors errors) {
        logger.debug("Validating user password.");

        // Check for dubious input.
        validateInput(input, errors);

        // Is blank.
        if (StringUtils.isBlank(input))
            errors.rejectValue(formField, "password.required");

        // Is of the incorrect length.
        if ((StringUtils.length(input) < 8) || (StringUtils.length(input) > 25))
            errors.rejectValue(formField, "password.length");
    }

    /**
     * Validates the user input for the user name.
     *
     * @param input  The user input to validate.
     * @param errors  Object in which to store any validation errors.
     */
    protected void validateUserName(String input, Errors errors) {
        logger.debug("Validating user name.");

        // Check for dubious input.
        validateInput(input, errors);

        // Is blank.
        if (StringUtils.isBlank(input))
            errors.rejectValue("userName", "userName.required");

        // Is of the incorrect length.
        if ((StringUtils.length(input) < 2) || (StringUtils.length(input) > 50))
            errors.rejectValue("userName", "userName.length");

        // Malformed.
        pattern = Pattern.compile(USER_NAME_PATTERN);
        matcher = pattern.matcher(input);
        if (!matcher.matches())
            errors.rejectValue("userName", "userName.chars");
    }

    /**
     * Validates the user name is not already in use.
     *
     * @param input  The user input to validate.
     * @param errors  Object in which to store any validation errors.
     */
    protected void validateUserNameNotInUse(String input, Errors errors) {
        logger.debug("Confirming user name is not already in use.");

        // Already exists.
        if (userManager.userExists("userName", input))
            errors.rejectValue("userName", "userName.alreadyInUse");
    }
}
