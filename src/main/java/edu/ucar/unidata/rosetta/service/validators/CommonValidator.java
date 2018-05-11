package edu.ucar.unidata.rosetta.service.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;

/**
 * Common validation methods all validators will use.
 */
public abstract class CommonValidator {

    protected static final String[] NAUGHTY_STRINGS = {"<script>", "../", "svg", "javascript", "::", "&quot;", "fromcharCode", "%3", "$#", "alert(", ".js", ".source", "\\", "scriptlet", ".css", "binding:", ".htc", "vbscript", "mocha:", "livescript:", "base64", "\00", "xss:", "%77", "0x", "IS NULL;", "1;", "; --", "1=1"};
    protected static final String[] NAUGHTY_CHARS = {"<", ">", "`", "^", "|", "}", "{"};

    protected void validateInput(String input, Errors errors) {
        String badChar = checkForNaughtyChars(input);
        if (badChar != null) {
            errors.reject(null,"Bad value submitted: " + badChar);
        }
        String badString = checkForNaughtyStrings(input);
        if (badString != null) {
            errors.reject(null, "Bad value submitted: " + badString);
        }
    }

    protected String checkForNaughtyStrings(String itemToCheck) {
        for (String item : NAUGHTY_STRINGS) {
            if (StringUtils.contains(StringUtils.lowerCase(itemToCheck), item)) {
                return item;
            }
        }
        return null;
    }

    protected String checkForNaughtyChars(String itemToCheck) {
        for (String item : NAUGHTY_CHARS) {
            if (StringUtils.contains(itemToCheck, item)) {
                return item;
            }
        }
        return null;
    }
}
