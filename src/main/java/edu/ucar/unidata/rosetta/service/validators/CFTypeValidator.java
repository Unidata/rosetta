package edu.ucar.unidata.rosetta.service.validators;

import edu.ucar.unidata.rosetta.domain.CFType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


@Component
public class CFTypeValidator implements Validator {
    private static final String[] NAUGHTY_STRINGS = {"<script>", "../", "svg", "javascript", "::", "&quot;", "fromcharCode", "%3", "$#", "alert(", ".js", ".source", "\\", "scriptlet", ".css", "binding:", ".htc", "vbscript", "mocha:", "livescript:", "base64", "\00", "xss:", "%77", "0x", "IS NULL;", "1;", "; --", "1=1"};
    private static final String[] NAUGHTY_CHARS = {"<", ">", "`", "^", "|", "}", "{"};


    public boolean supports(Class clazz) {
        return CFType.class.equals(clazz);
    }

    public void validate(Object obj, Errors errors) {
        CFType cfType = (CFType) obj;
        validateInput(cfType.getSpecifiedCFType(), errors);
        validateInput(cfType.getPlatform(), errors);
    }

    private void validateInput(String input, Errors errors) {
        String badChar = checkForNaughtyChars(input);
        if (badChar != null) {
            errors.reject("Bad value submitted: " + badChar);
        }
        String badString = checkForNaughtyStrings(input);
        if (badString != null) {
            errors.reject("Bad value submitted: " + badString);
        }
    }


    private String checkForNaughtyStrings(String itemToCheck) {
        for (String item : NAUGHTY_STRINGS) {
            if (StringUtils.contains(StringUtils.lowerCase(itemToCheck), item)) {
                return item;
            }
        }
        return null;
    }

    private String checkForNaughtyChars(String itemToCheck) {
        for (String item : NAUGHTY_CHARS) {
            if (StringUtils.contains(itemToCheck, item)) {
                return item;
            }
        }
        return null;
    }
}
