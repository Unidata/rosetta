package edu.ucar.unidata.rosetta.service.validators.user;

import edu.ucar.unidata.rosetta.domain.user.User;
import edu.ucar.unidata.rosetta.service.user.UserManager;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator class for the form-backing object for modifying a User (sans password).
 *
 * @author oxelson@ucar.edu
 */
@Component
public class EditUserValidator extends CommonUserValidator implements Validator {

    protected static Logger logger = Logger.getLogger(CreateUserValidator.class);

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
        validateFullName(user.getFullName(), errors);
        validateAccessLevel(user.getAccessLevel(), errors);
        validateAccountStatus(user.getAccountStatus(), errors);
        validateEmailAddress(user.getEmailAddress(), errors);
        validateEmailAddressBelongsToThisUser(user.getUserName(), user.getEmailAddress(), errors);
    }

    /**
     * Validates the email address belongs to this persisted user.
     *
     * @param userName  The user name of the user.
     * @param emailAddress The email address of the user.
     * @param errors  Object in which to store any validation errors.
     */
    private void validateEmailAddressBelongsToThisUser(String userName, String emailAddress, Errors errors) {
        logger.debug("Confirming email address is not already in use.");

        // Check to make sure it is the same user.
        if (!userManager.sameUser(userName, emailAddress))
            errors.rejectValue("emailAddress", "emailAddress.alreadyInUse");
    }
}
