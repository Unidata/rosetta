/**
 * validationFunctions.css
 * 
 * Custom functions handling client-side validation (validation also performed on backend).
 */



/** 
 * This function tests/loops through the metadata array and validates the 
 * user input data (String) stored in the session against the array values.  
 *
 * @param metadataArray  The array of metadata objects we are going to evaluate.
 * @param metadataType  The type of metadata to validate (platform, etc.). 
 * @param currentStep  The current step in the jWizard.
 */
function validateMetadataEntries(metadataArray, metadataType, currentStep) {
    var boolean = true;
    for (var i = 0; i < metadataArray.length; i++) { 
        var obj = metadataArray[i];
        var errorLabel = $(".jw-step:eq(" + currentStep + ")").find("label[for=\"" + obj["tagName"] + "\"]");
        var userEnteredValue = getItemEntered(metadataType, obj["tagName"]);

        if (userEnteredValue == null) {
            if (obj["isRequired"]) {
                $(errorLabel).text("You need to provide input for " + obj["displayName"]);
                $(errorLabel).attr("style", "");
                boolean = false;
            }
        } else {
            var errorMessage = lookForBadChars(userEnteredValue, obj["displayName"]);
            if (errorMessage != null) { 
                $(errorLabel).text(errorMessage);
                $(errorLabel).attr("style", "");
                boolean = false;
            } else {
                if (obj["units"]) {
                    var units = $(".jw-step:eq(" + currentStep + ")").find("select[name=\"" + obj["tagName"] + "Units\"]").attr("value");
                    var metadataString = buildStringForSession("platformMetadata", obj["tagName"] + "Units", units);
                    addToSession(metadataType, metadataString);  
                }
            }
        }
    }
    return boolean;
}

/** 
 * This function verifies the needed data exists in the session.
 *
 * @param currentStep  The current step in the jWizard.
 * @param dataToExamine  The data to look for in the session.
 * @param errorMessage  The corresponding error message if the data is missing.
 */
function validateItemExistsInSession(currentStep, dataToExamine, errorMessage) {
    var boolean = true;
    var errorLabel = $(".jw-step:eq(" + currentStep + ")").find("label.error");
    if (!getFromSession(dataToExamine)) {
        $(errorLabel).text(errorMessage);
        boolean = false;
    }
    return boolean;
}

/** 
 * This function validates the file size and type uploaded by the user.
 *
 * @param file  The file uploaded by the user.
 * @param currentStep  The current step in the jWizard.
 */
function validateUploadedFile(file, currentStep) {
    var boolean = true;
    var errorLabel = $(".jw-step:eq(" + currentStep + ")").find("label.error");
    // RegEx patters for valid files (extensions, "type" from input variable `file`)
    var excelPattern = /^\.(xls|xlsx)$/i;
    var ncmlPattern = /^\.ncml$/i;
    var zipPattern = /^\.zip$/i;
    var filePattern = /(text)/i;

    // get file extension
    var fileExt = file.name.match(/\.[a-zA-Z]{3,4}$/);

    // test valid regex patterns
    var isExcel = excelPattern.test(fileExt[0]);
    var isNcml = ncmlPattern.test(fileExt[0]);
    var isZip = zipPattern.test(fileExt[0]);
    var isFile = filePattern.test(file.type);

    //if ((file.size / 1024) > 1024) {
    if ((file.size / 1024) > 2150) {
        $(errorLabel).text("Error! File size should be less then 1MB");
        $("#upload").addClass("hideMe"); 
        boolean = false;
    //} else if (($("#file")[0].files[0].size / 1024) <= 0) {
    } else if (($("#file")[0].files[0].size / 1024) <= 0) {
        $(errorLabel).text("Error! You are attempting to upload an empty file");
        $("#upload").addClass("hideMe"); 
        boolean = false;
    } else {
        // handle special cases first, then as last check see if it of type "file:
        if (isExcel) {
          $(".jw-step:eq(" + currentStep + ")").find("#notice").empty().append("Notice: Any date formatted cells in your spreadsheet will be reformatted in 'seconds since 1970-01-01'!");
        } else if ((!isFile) && (!isNcml) && (!isZip)) {
            $(errorLabel).text("Error! Incorrect file type selected for upload");
            $("#upload").addClass("hideMe");
            boolean = false;
        }
    }
    return boolean;
}

/**
 * This function validates the file size and type uploaded by the user.
 *     Specific to template files
 * @param file  The file uploaded by the user.
 * @param currentStep  The current step in the jWizard.
 */
function validateUploadedTemplateFile(file, currentStep) {
    var boolean = true;
    var errorLabel = $(".jw-step:eq(" + currentStep + ")").find("label.error");
    // RegEx patters for valid files (extensions, "type" from input variable `file`)
    var zipPattern = /^\.zip$/i;

    // get file extension
    var fileExt = file.name.match(/\.[a-zA-Z]{3,4}$/);

    // test valid regex patterns
    var isZip = zipPattern.test(fileExt[0]);

    //if ((file.size / 1024) > 1024) {
    if ((file.size / 1024) > 2150) {
        $(errorLabel).text("Error! File size should be less then 1MB");
        $("#uploadTemplate").addClass("hideMe");
        boolean = false;
    } else if (($("#templateFile")[0].files[0].size / 1024) <= 0) {
        $(errorLabel).text("Error! You are attempting to upload an empty file");
        $("#uploadTemplate").addClass("hideMe");
        boolean = false;
    } else {
        // handle special cases first, then as last check see if it of type "file:
        if (!isZip) {
            $(errorLabel).text("Error! Incorrect file type selected for upload");
            $("#uploadTemplate").addClass("hideMe");
            boolean = false;
        }
    }
    return boolean;
}

/** 
 * This function verifies a value for other delimiter exists if users has "Other" among the delimiters listed in the session.
 *
 * @param currentStep  The current step in the jWizard.
 */
function validateOtherDelimiter(currentStep) {
    var boolean = true;
    var errorLabel = $(".jw-step:eq(" + currentStep + ")").find("label.error");

    if (getFromSession("delimiters").search("Other") >= 0 ) {
        if (!getFromSession("otherDelimiter")) {
            $(errorLabel).text("You Specified 'Other' as a delimiter.  Please input that delimiter to continue.");
            boolean = false;
        }
    }
    return boolean;
}

/** 
 * This function tests user input data (String) against a know set of acceptable 
 * characters.  If any bad characters are detected in the input data, the function 
 * returns a message and the calling code handles the error display accordingly.
 *
 * @param userInput  The user input data to test.
 * @param tagName  The name value of the input tag that collected the data.  
 */
function lookForBadChars(userInput, tagName) {
    if (/[^a-zA-Z0-9_\s\-\.]/g.test(userInput.trim())) { // garbage characters entered
        return "Please enter a legitimate value for " + tagName + " (allowed: a-zA-Z0-9 _-.):";
    } else {
        return null;
    }
}

/** 
 * This function tests user input data (String) to see if the user provided
 * only whitespace characters or if the entry was submitted with a blank value.
 * If the entry is perceived as being blank, the function returns a message 
 * and the calling code handles the error display accordingly.
 *
 * @param userInput  The user input data to test.
 * @param tagName  The name value of the input tag that collected the data. 
 */
function lookForBlankEntries(userInput, tagName) {  
    if (userInput.trim() === "") {  // only whitespace entered
        return "Please enter a legitimate value for " + getMetadataDisplayName(tagName) + ":";
    } else {
        return null;
    }
}

/**
 * Similar to lookForBlankEnteries, but for a blank
 * option for a select element
 *
 * @param userInput  The user input data to test.
 * @param tagName  The name value of the input tag that collected the data.
 */
function lookForBlankSelection(userInput, tagName) {
    if (userInput === "") {  // empty selection
        return "Please select a value for " + getMetadataDisplayName(tagName);
    } else {
        return null;
    }
}

/** 
 * This function checks to see that all the required metadata values for the specified
 * variable type (coordinate versus non-coordinate) were provided by the user and stored in
 * the session.  If something is missing, an error for that metadata item is added to the DOM.
 *
 * @param variableType  The variable "type" (coordinate variable or not).
 * @param metadataInSession  The metadata values entered by the user and stored in the session
 */
function checkRequiredMetadataCompletion(variableType, metadataInSession) {
    var requiredMetadata = getKnownRequiredMetadataList(variableType);
    for (var i = 0; i < requiredMetadata.length; i++) {
        if (metadataInSession.indexOf(requiredMetadata[i]) < 0) {
            $("#dialog #requiredMetadataAssignment label[for=\"" + requiredMetadata[i] + "\"].error").text("This metadata item is a required.  Please provide a value: ");
        } 
    }  
}

/** 
 * This function looks at the data stored in the session and does a validation. 
 *
 * @param sessionKey  The key used to store the data in the session.
 * @param finalCheck  If the final validation being performed (when done button is clicked). Optional.
 */
function validateVariableData(sessionKey, finalCheck) {
    // get the name of the variable supplied by the user 
    var variableValue = getFromSession(sessionKey);  

    if (variableValue) {  // the user has provided something for the variable name or opted not to use the column of data

        if (variableValue != "Do Not Use") {
            var errorMessage = lookForBlankEntries(variableValue, "Variable Name");
            if (errorMessage != null) { 
                $("#dialog #variableNameTypeAssignment").find("label.error").text(errorMessage);
                removeFromSession(sessionKey); 
            } else {
                errorMessage = lookForBadChars(variableValue, "Variable Name");
                if (errorMessage != null) { 
                    $("#dialog #variableNameTypeAssignment").find("label.error").text(errorMessage);
                    removeFromSession(sessionKey); 
                } else {

                    // now validate the metadata if it exists in the session
                    var variableMetadataInSession = getFromSession(sessionKey + "Metadata");

                    if (variableMetadataInSession) { // if we have metadata in the session, grab it and populate the input tags
                        // coordinate variable
                        var coordinateVariableSelected = getItemEntered(sessionKey + "Metadata", "_coordinateVariable");
                        if (coordinateVariableSelected != null) {
                            var coordinateVariableType = getItemEntered(sessionKey + "Metadata", "_coordinateVariableType");
                            if (((coordinateVariableType != "") & (coordinateVariableType != null)) | ((coordinateVariableType == null) & (coordinateVariableSelected != "coordinate"))) {
                                // data type
                                var dataTypeSelected = getItemEntered(sessionKey + "Metadata", "dataType");
                                if (dataTypeSelected != null) {

                                    // metadata
                                    // get the metadata from the session string, minus the coordinateVariable and dataType entries
                                    var metadataProvided = getAllButTheseFromSessionString(sessionKey + "Metadata", ["_coordinateVariable", "dataType"]);
                                    if (metadataProvided.length > 0) {
                                        // make sure chars are correct and no blank entries
                                        for (var i = 0; i < metadataProvided.length; i++) {
                                            var metadataKeyValuePair = metadataProvided[i].split(/:/);

                                            // all entries have to pass this test
                                            errorMessage = lookForBadChars(metadataKeyValuePair[1], getMetadataDisplayName(metadataKeyValuePair[0]));
                                            if (errorMessage != null) {
                                                $("#dialog #variableAttributes label[for=\"" + metadataKeyValuePair[0] + "\"].error").text(errorMessage);
                                                removeItemFromSessionString(sessionKey + "Metadata", metadataKeyValuePair[0]);
                                            } else {
                                                errorMessage = lookForBlankEntries(metadataKeyValuePair[1], metadataKeyValuePair[0]);
                                                if (errorMessage != null) {
                                                    // only required metadata must pass this
                                                    if (isRequiredMetadata(coordinateVariableSelected, metadataKeyValuePair[0])) {
                                                        $("#dialog #variableAttributes label[for=\"" + metadataKeyValuePair[0] + "\"].error").text(errorMessage);
                                                    }
                                                    removeItemFromSessionString(sessionKey + "Metadata", metadataKeyValuePair[0]);
                                                } else {
                                                    // has all the required metadata been provided?
                                                    if (finalCheck) { // has the "done" button been clicked?
                                                        checkRequiredMetadataCompletion(coordinateVariableSelected, getKeysFromSessionData(metadataProvided));
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        if (finalCheck) { // has the "done" button been clicked?
                                            $("#dialog #requiredMetadataAssignment").find("label.error").text("Please provide an entry for this field: ");
                                        }
                                    }

                                } else {
                                    if (finalCheck) { // has the "done" button been clicked?
                                        $("#dialog #dataTypeAssignment").find("label.error").text("Please select the data type for this variable.");
                                    }
                                }
                            } else { // no coordinate variable type specified
                                if (finalCheck) { // has the "done" button been clicked?
                                    $("#dialog #coordinateVarTypeAssignment").find("label.error").text("Please specify the coordinate variable type.");
                                }
                            }

                        } else { // no coordinate variable specified
                            if (finalCheck) { // has the "done" button been clicked?
                                $("#dialog #coordinateVariableAssignment").find("label.error").text("Please specify if this a coordinate variable or not.");
                            }
                        }

                    }  else {  // no variable metadata in session yet
                        if (finalCheck) { // has the "done" button been clicked?
                            // give the error because the coordinateVariable is the first thing we collect
                            $("#dialog #coordinateVariableAssignment").find("label.error").text("Please specify if this a coordinate variable or not.");
                            removeFromSession(sessionKey + "Metadata"); 
                        }
                    }
                }   
            } 
        }          
    } else { // no input given: error
        if (finalCheck) { // has the "done" button been clicked?
            $("#dialog #variableNameTypeAssignment").find("label.error").text("You must assign a variable name or select not to use this column of data.");
            removeFromSession(sessionKey); 
        }
    }
}
