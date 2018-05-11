package edu.ucar.unidata.rosetta.service.validators;

import edu.ucar.unidata.rosetta.domain.Data;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates a Data object containing user input.
 */
@Component
public class CFTypeValidator extends CommonValidator implements Validator {

    public boolean supports(Class clazz) {
        return Data.class.equals(clazz);
    }

    public void validate(Object obj, Errors errors) {
        Data cfType = (Data) obj;
        String specifiedCfType = cfType.getCfType();
        String platform = cfType.getPlatform();
        validateInput(specifiedCfType, errors);
        validateInput(platform, errors);
        validateNotEmpty(specifiedCfType, platform, errors);
    }

    /**
     * Checks to make sure either the platform or cfType was selected by the user.
     * (Both cannot be empty).  The platform is associated with a cfType and will
     * be converted into the proper cfType value at a later stage in the program.
     *
     * @param specifiedCfType   The cfType selected by the user.
     * @param platform          The platform selected by the user.
     * @param errors            Validation errors.
     */
    private void validateNotEmpty(String specifiedCfType, String platform, Errors errors) {
        if (specifiedCfType == null && platform == null) {
            errors.reject(null, "You must select either a platform or specify a CF type.");
        }
    }
}
