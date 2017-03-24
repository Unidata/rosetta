/**
 * stepFunctions.js
 *
 * Functions that correspond to a particular step in the jWizard.
 */

/**
 * Used in the 'Select Observation Platform' step.  Called during template creation.
 *
 * @param stepType  A key denoting where we are in the Wizard process.  Options are:
 *     stepValidation:  Validate input for current step.
 *     repopulateStep:  Repopulate user input (if exists) if user lands on the step using a next or
 *     previous step stepFunctions:   Initialization.  Do things that need to be done interactively
 *     during the step.
 *
 * @param stepData  Optional data needed in this function.
 */
function selectPlatform(stepType, stepData) {
    if (stepType == "stepValidation") {
        // If we land on this page and user has already enter something
        // (e.g., clicked previous or used the menu to navigate)
        // Don't hide the 'Next' button
        error =
            validateItemExistsInSession(stepData.currentStepIndex, "cfType",
                                        "You must select a platform to continue.");
        if (!error) {
            return false;
        } else {
            return error;
        }
    } else if (stepType == "repopulateStep") {
        // If we land on this page and user has already enter something
        // (e.g., clicked previous or used the menu to navigate)
        // Don't hide the 'Next' button
        if ((stepData.type == "previous") || (stepData.type == "next")) {
            if (getFromSession("cfType")) {
                $("#faux").remove();
                $(".jw-button-next").removeClass("hideMe");
                $('input:radio[name="cfType"][value="' + getFromSession("cfType") + '"]')
                    .attr('checked', 'checked');
            }
        }
    } else if (stepType == "stepFunctions") {  // Initialization.  
        var inputName = "#step" + stepData + " input";
        $(inputName).bind("click", function () {    // Bind any click events and capture user input.
            addToSession("cfType", $(this).val());
            // Show 'Next' button after user makes a selection
            $("#faux").remove()
            $(".jw-button-next").removeAttr("disabled").removeClass("disabled")
                .removeClass("hideMe");
        });
    }
}

function selectKnownType(stepType, stepData) {
    // stepTypes:
    //    stepValidation - validate input for current step
    //    repopulateStep - repopulate user input (if exists) if user lands
    //                     on the step using a next or previous step
    //    stepFunctions - things that need to be done interactively during the step
    //
    //    stepData (optional) - any data you need to pass into the function
    if (stepType == "stepValidation") {
        // nothing to validate
    } else if (stepType == "repopulateStep") {
        // If we land on this page and user has already enter something
        // (e.g., clicked previous or used the menu to navigate)
        // Don't hide the 'Next' button
        if ((stepData.type == "previous") || (stepData.type == "next")) {
            if (getFromSession("convertFrom") != null) {
                $("#faux").remove();
                $(".jw-button-next").removeClass("hideMe");
                $("select#convertFrom").val(getFromSession("convertFrom"));
            } else {
                //console.log("No Convert From in Session Storage")
            }
        }
    } else if (stepType == "stepFunctions") {
        if (getFromSession("convertFrom") == null) {
            //console.log("convertFrom is null")
            $("#faux").remove();
            $(".jw-button-next").removeClass("hideMe");
            //console.log("add to session")
            addToSession("convertFrom", $("select#convertFrom").val());
        }
        $("select#convertFrom").change(function () {
            //console.log("select changed");
            addToSession("convertFrom", $(this).val());
            // Show 'Next' button after user makes a selection
            $("#faux").remove();
            $(".jw-button-next").removeAttr("disabled").removeClass("disabled")
                .removeClass("hideMe");
        });
    }
}

/**
 * Used to upload a file.  Called during template creation, auto convert, and restoration.
 *
 * @param stepType  A key denoting where we are in the Wizard process.  Options are:
 *     stepValidation:  Validate input for current step.
 *     repopulateStep:  Repopulate user input (if exists) if user lands on the step using a next or
 *     previous step stepFunctions:   Initialization.  Do things that need to be done interactively
 *     during the step.
 *
 * @param stepData  Optional data needed in this function.
 */
function uploadDataFile(stepType, stepData) {
    var fileId = "#file";
    var progressId = "#progress";
    var uploadId = "#upload";
    var clearId = "#clearFileUpload";
    if (stepType == "stepValidation") {
        error =
            validateItemExistsInSession(stepData.currentStepIndex, "uniqueId",
                                        "You need to upload a file to continue.");
        if (!error) {
            return false;
        } else {
            return error;
        }
    } else if (stepType == "repopulateStep") {
        // Initially hide the upload button (will appear when user opens file chooser)
        $(uploadId).addClass("hideMe");
        if ((stepData.type == "previous") || (stepData.type == "next")) {
            var uid = getFromSession("uniqueId")
            if (getFromSession("uniqueId")) {
                $(fileId).addClass("hideMe");
                $("#quickSaveButton").removeClass("hideMe");
                progressBarCallback(progressId, clearId);
                $("#faux").remove();
                $(".jw-button-next").removeClass("hideMe");
            }
        }
    } else if (stepType == "stepFunctions") {
        $(fileId).bind("change", function () {
            // Validate file being uploaded
            var error = validateUploadedFile($("#file")[0].files[0], 1);
            if (!error) {
                return false;
            } else {
                // Show upload button after user launches file chooser (if upload successful)
                $(".jw-step:eq(1)").find("label.error").text("");
                $("#upload").removeClass("hideMe");
            }
        });

        $(uploadId).bind("click", function () {
            // Upload file and add to session
            var up = instantiateUploader($(progressId), $(".jw-step:eq(1)").find("label.error"),
                                         $(uploadId), $(fileId), $(clearId));
            up.send();
            addToSession("fileName", cleanFilePath($(fileId).val()));
            // show 'Next' button after user uploads file
            $(fileId).addClass("hideMe");
            $(".jw-button-next").removeAttr("disabled").removeClass("disabled");
            $("#quickSaveButton").removeClass("hideMe")
        });

        $(clearId).bind("click", function () {
            //removed uploaded file from session and recreate the upload form.
            removeAllButTheseFromSession(["platformMetadata", "cfType"]);
            $(fileId).removeClass("hideMe");
            $(clearId).addClass("hideMe");
            $("#quickSaveButton").addClass("hideMe")
            // clear progress bar
            $(progressId).attr("style", "").addClass("progress");
            $(progressId).html("0%");
            // clear any notices about file types
            $("#notice").empty();
            // hide the 'Next' button
            $(".jw-button-next").addClass("hideMe").after(stepData);
        });
    }
}

function uploadRosettaTemplate(stepType, stepData) {
    var fileId = "#templateFile";
    var progressId = "#templateProgress";
    var uploadId = "#uploadTemplate";
    var clearId = "#clearTemplateFileUpload";
    if (stepType == "stepValidation") {
        error =
            validateItemExistsInSession(stepData.currentStepIndex, "uniqueId",
                                        "You need to upload a file to continue.");
        if (!error) {
            return false;
        } else {
            restoreSession("stepFunctions");
            return error;
        }
    } else if (stepType == "repopulateStep") {
        $(fileId).bind("change", function () {
            // Validate file being uploaded
            var error = validateUploadedTemplateFile($(fileId)[0].files[0], 0);
            if (!error) {
                return false;
            } else {
                // Show upload button after user launches file chooser (if upload successful)
                $(".jw-step:eq(0)").find("label.error").text("");
                $(uploadId).removeClass("hideMe");
            }
            restoreSession("stepFunctions");
        });

        // Initially hide the upload button (will appear when user opens file chooser)
        $(uploadId).addClass("hideMe");
        if ((stepData.type == "previous") || (stepData.type == "next")) {
            if (getFromSession("uniqueId")) {
                $(fileId).addClass("hideMe");
                progressBarCallback(progressId, clearId);
                $("#faux").remove();
                $(".jw-button-next").removeClass("hideMe");
            }
        }
    } else if (stepType == "stepFunctions") {
        $(fileId).bind("change", function () {
            // Validate file being uploaded
            var error = validateUploadedTemplateFile($(fileId)[0].files[0], 0);
            if (!error) {
                return false;
            } else {
                // Show upload button after user launches file chooser (if upload successful)
                $(".jw-step:eq(0)").find("label.error").text("");
                $(uploadId).removeClass("hideMe");
            }
        });

        $(uploadId).bind("click", function () {
            // Upload file and add to session
            var up = instantiateUploader($(progressId), $(".jw-step:eq(0)").find("label.error"),
                                         $(uploadId), $(fileId), $(clearId));
            up.send();
            addToSession("fileName", cleanFilePath($(fileId).val()));
            // show 'Next' button after user uploads file
            $(fileId).addClass("hideMe");
            $(".jw-button-next").removeAttr("disabled").removeClass("disabled");
        });

        $(clearId).bind("click", function () {
            //removed uploaded file from session and recreate the upload form.
            removeFromSession("uniqueId");
            removeFromSession("fileName");
            $(fileId).removeClass("hideMe");
            $(clearId).addClass("hideMe");
            // clear progress bar
            $(progressId).attr("style", "").addClass("progress");
            $(progressId).html("0%");
            // clear any notices about file types
            $("#notice").empty();
            // hide the 'Next' button
            $("#faux").remove();
            $(".jw-button-next").addClass("hideMe").after(faux);
        });
    }
}

function specifyHeaderLines(stepType, stepData) {
    if (stepType == "stepValidation") {
        if (stepData.type == "next") {
            error =
                validateItemExistsInSession(stepData.currentStepIndex, "headerLineNumbers",
                                            "You need to specify which lines are header lines to continue.");
            if (!error) {
                return false;
            } else {
                return error;
            }
            return false
        }
    } else if (stepType == "repopulateStep") {
        $.post("parse",
               {uniqueId: getFromSession("uniqueId"), fileName: getFromSession("fileName")},
               function (data) {
                   drawGrid(data, "2")
               },
               "text");

        // If we land on this page and user has already enter something
        // (e.g., clicked previous or used the menu to navigate)
        // Don't hide the 'Next' button
        if ((stepData.type == "previous") || (stepData.type == "next")) {
            if (getFromSession("headerLineNumbers")) {
                $("#faux").remove();
                $(".jw-button-next").removeClass("hideMe");
            }
        }
    } else if (stepType == "stepFunctions") {
        /**
         * STEP 2 handled in SlickGrid/custom/headerLineSelection.js
         */
        return true;
    }
}

function specifyDelimiters(stepType, stepData) {
    if (stepType == "stepValidation") {
        if (stepData.type == "next") {
            error =
                validateItemExistsInSession(stepData.currentStepIndex, "delimiters",
                                            "You need to specify at least one delimiter to continue.");
            if (!error) {
                return false;
            }
            error = validateOtherDelimiter(stepData.currentStepIndex);
            if (!error) {
                return false;
            }
            return error;
        }

    } else if (stepType == "repopulateStep") {
        if ((stepData.type == "previous") || (stepData.type == "next")) {
            if (getFromSession("delimiters")) {
                $("#step3 #delimiter").each(function () {
                    if (getFromSession("delimiters").search($(this).val()) >= 0) {
                        if ($(this).val() == "Other") {
                            if (!getFromSession("otherDelimiter")) {
                                removeItemFromSessionString("delimiters", "Other")
                                return true;
                            }
                        }
                        $(this).attr("checked", true);
                    } else {
                        $(this).attr("checked", false);
                    }
                });
                if (getFromSession("otherDelimiter")) {
                    $("#otherDelimiter").val(getFromSession("otherDelimiter"));
                    $("#otherDelimiter").removeClass("hideMe");
                }
                $("#faux").remove();
                $(".jw-button-next").removeClass("hideMe");
            }
            if (getFromSession("decimalSeparator")) {
                $("#step3 #decimalSeparator").each(function () {
                    if (getFromSession("decimalSeparator").search($(this).val()) >= 0) {
                        $(this).attr("checked", true);
                    }
                });
            }
        }
    } else if (stepType == "stepFunctions") {
        var stepElement = "#step" + stepData + " input:checkbox";
        var stepCheck = ".jw-step:eq(" + stepData + ")";
        $(stepElement).bind("click", function () {
            $(stepCheck).find("label.error").text("");
            // create array from selected values
            var checkedDelimiters = $("input:checkbox").serializeArray();
            var delimiterArray = [];
            $.each(checkedDelimiters, function (index, field) {
                delimiterArray[index] = field.value;
            });

            // add to session
            addToSession("delimiters", delimiterArray);
            if (delimiterArray.length <= 0) {
                removeFromSession("delimiters");
            } else {
                // Show 'Next' button after user makes a selection
                $("#faux").remove();
                $(".jw-button-next").removeClass("hideMe");
            }

            // if Other is selected
            if ($(this).val() == "Other") {
                if (jQuery.inArray("Other", delimiterArray) < 0) {
                    // toggled off
                    $("#otherDelimiter").addClass("hideMe");
                    $("#otherDelimiter").val("");
                    removeFromSession("otherDelimiter");
                } else {
                    // toggled on
                    $("#otherDelimiter").removeClass("hideMe");
                }
            }
        });

        $("#step3 #decimalSeparator").bind("click", function () {
            addToSession("decimalSeparator",
                         $('input[name=decimalSeparator]:checked', '#step3').val());
        });

        $("#otherDelimiter").on("focusin", function () {
            $(stepCheck).find("label.error").text("");
        });

        $("#otherDelimiter").on("focusout", function () {
            addToSession("otherDelimiter", $(this).val());
            if (getFromSession("delimiters")) {
                // Show 'Next' button after user makes a selection
                $("#faux").remove();
                $(".jw-button-next").removeClass("hideMe");
            }
        });
    }
}

function specifyVariableMetadata(stepType, stepData) {
    if (stepType == "stepValidation") {
        // handled elsewhere
    } else if (stepType == "repopulateStep") {
        $.post("parse", {
                   uniqueId: getFromSession("uniqueId"),
                   fileName: getFromSession("fileName"),
                   otherDelimiter: getFromSession("otherDelimiter"),
                   headerLineNumbers: getFromSession("headerLineNumbers"),
                   delimiters: getFromSession("delimiters")
               },
               function (data) {
                   drawGrid(data, "4")
               },
               "text");
    } else if (stepType == "stepFunctions") {
        /**
         * Handled in SlickGrid/custom/variableSpecification.js
         */
    }
}

function specifyGeneralMetadata(stepType, stepData) {
    if (stepType == "stepValidation") {
        if (stepData.type == "next") {
            error =
                validateMetadataEntries(getFromSession("generalMetadata"), "generalMetadata",
                                        stepData.currentStepIndex);
            if (!error) {
                return false;
            } else {
                return error;
            }
        }
    } else if (stepType == "repopulateStep") {
        if ( stepData.type && ((stepData.type == "previous") || (stepData.type == "next"))) {
            checkAndExposeNext("generalMetadata");
        } else {
            //We only want to actually "repopulate" on the first run, which is called from 
            //the stepFunctions - why we have this here, and what repopulate really means
            //should be reworked imo.
            if (nCustomAttributes == 0){
                //Add custom attributes if none have been added
                //and there are some in the sessionstorage
                //The way sessionstorage and customattributes works atm, it is possible to reload
                //the page or quicksave while having a non-consistent custom attribute
                // numbering saved in sessiong storage
                // this will first be fixed here by re-indexing the custom attributes:
                var customAttributes = getFromSession("customAttributes");
                if (customAttributes){
                    customAttributes = getValuesFromSessionString(customAttributes);
                    removeFromSession("customAttributes");
                    for (var i = 0; i<customAttributes.length;i++){
                        var id = "customAttribute" + nCustomAttributes;
                        var cAS = buildStringForSession("customAttributes", id, customAttributes[i]);
                        addToSession("customAttributes", cAS);
                        addCustomGeneralAttribute();
                    }
                }
            }
            // populate text input elements from sessionStorage
            var stepElement = "#step" + stepData.nextStepIndex + " input[type=\"text\"]";
            var inputElements = $(stepElement);
            for (var i = 0; i < inputElements.length; i++) {
                var name = $(inputElements[i]).attr("name");
                if (name){
                    var itemInSession = getItemEntered("generalMetadata", name);
                    if (itemInSession != null) {
                        $("input[name=\"" + name + "\"]").val(itemInSession);
                    } else {
                        $("input[name=\"" + name + "\"]").val("");
                    }
                } else {
                    //if no name, its a custom attribute
                    var id = $(inputElements[i]).attr("id");
                    if (id.substr(id.length - 4) == "Name"){
                        baseID = id.substr(0,id.length - 4);
                        name = getItemEntered("customAttributes", baseID);
                        $("#"+id).val(name);
                    } else {
                        baseID = id.substr(0,id.length - 5);
                        name = getItemEntered("customAttributes", baseID);
                        $("#"+id).val(getItemEntered("generalMetadata", name));
                    }
                }
            }
            // populate pattern checkboxes from sessionStorage
            var stepElementCb = "#step" + stepData.nextStepIndex + " input[type=\"checkbox\"]";
            var inputElements = $(stepElementCb);
            for (var i = 0; i < inputElements.length; i++) {
                var name = $(inputElements[i]).attr("name");
                if (name){
                    var itemInSession = getItemEntered("parseHeaderForMetadata", name);
                    if (itemInSession == "true") {
                        $("input[name=\"" + name + "\"][type=\"checkbox\"]").prop('checked', true);
                    } else {
                        $("input[name=\"" + name + "\"][type=\"checkbox\"]").prop('checked', false);
                    }
                } else {
                    var id = $(inputElements[i]).attr("id");
                    var baseID = id.substr(0,id.length - 2);
                    name = getItemEntered("customAttributes", baseID);
                    var itemInSession = getItemEntered("parseHeaderForMetadata", name);
                    if (itemInSession == "true") {
                        $("#"+id).prop('checked', true);
                    } else {
                        $("#"+id).prop('checked', false);
                    }
                }
            }
        }
    } else if (stepType == "stepFunctions") {
        var customAttDiv = $("#containerForCustomAttributes");
        customAttDiv.after("<button type='button' onclick='addCustomGeneralAttribute()'>"
                + "Add custom attribute</button>");
        var stepElement = "#step" + stepData + " input[type='text']";
        var stepCheck = ".jw-step:eq(" + stepData + ")";
        $(stepElement).on("focusout", function () {
            var value = $(this).attr("value");
            var name = $(this).attr("name");
            if (value != "") {
                var isPattern = getItemEntered("parseHeaderForMetadata", name);
                var validPattern = validatePattern(value);
                if (isPattern && ( !validPattern || value.indexOf("(") <0 )) {
                    //Pattern is not valid
                    removeItemFromSessionString("generalMetadata", name);
                    var errorMessage = "Pattern is not valid";
                    if (validPattern) {
                        errorMessage = "Pattern must contain at least one capturing group. It does not even contain a '('";
                    }
                    $(this).closest("li").find("label.error").text(errorMessage);
                } else {
                    $(this).closest("li").find("label.error").text("");
                    // add to the session
                    var metadataString = buildStringForSession("generalMetadata", name,
                                                               value);
                    addToSession("generalMetadata", metadataString);
                }
            } else {
                // entered a blank value so get rid of it in the session
                removeItemFromSessionString("generalMetadata", name);
            }

            // see if we can expose the next button
            checkAndExposeNext("generalMetadata");
        });

        // When pattern/regex checkbox is toggled
        var stepElementCheckbox = "#step" + stepData + " input[type='checkbox']";
        $(stepElementCheckbox).on("change", function(){
            var name = $(this).attr("name");
            var li = $(this).closest("li");
            var value = li.find("input[type='text']").attr("value");
            if ($(this).is(':checked')){
                //add to session
                var sessionString = buildStringForSession("parseHeaderForMetadata",
                                                          name,
                                                          "true");
                addToSession("parseHeaderForMetadata", sessionString);
                //validate the text
                if (!validatePattern(value)){
                    removeItemFromSessionString("generalMetadata", name);
                    li.find("label.error").text("Pattern is not valid");
                } else if (value.indexOf("(") <0){
                    removeItemFromSessionString("generalMetadata", name);
                    li.find("label.error").text("Pattern must contain at least one capturing group. It does not even contain a '('");
                }
            } else {
                //remove from session
                li.find("label.error").text("");
                removeItemFromSessionString("parseHeaderForMetadata", name);
                // Add text to session, it would not be saved if it was an invalid pattern
                if (value != "") {
                    var metadataString = buildStringForSession("generalMetadata", name,
                            value);
                    addToSession("generalMetadata", metadataString);
                }
            }
            // see if we can expose the next button
            checkAndExposeNext("generalMetadata");
        });
        specifyGeneralMetadata("repopulateStep",{"nextStepIndex":stepData});
    }
}
var nCustomAttributes = 0;
function addCustomGeneralAttribute() {
    var id = "customAttribute" + nCustomAttributes;
    var label1 = "Custom Attribute #"+nCustomAttributes+"<br>"
                    + "Name<img src='resources/img/help.png'alt="
                    + "'Must begin with a letter, allowed characters are:"
                    + " letters(a-z,A-Z), digits, and underscores.'"+
                    " />:"
                    + "<input type='text' id='"+id+"Name' value=''/>"
                    + "<br>"
                    + "<input type='checkbox' id="+id+"CB> is a regex"
                    + "<br>"
                    + "Value:" + "<input type='text' id='"+id+"Value' value=''/>";
    label1 = wrap(label1, "label")
    var label2 = "<label for='" + id + "' class='error'></label>";
    var liElement = wrap(label1 + label2, "li", id);
    $('#containerForCustomAttributes').append(liElement);
    $("#"+id+"Name").keyup(function (event) {
        // We dont't want to remove the errormessage when
        // alt,ctrl,shift,caps,windows,cmd or arrows are released
        // These are buttons that should not change the input,
        // and users might use them. 
        if ([16,17,18,20,91,92,93,37,38,39,40].indexOf(event.keyCode) < 0){
            var errorText = "";
            if (this.value.match(/[^0-9a-zA-Z_]/)) {
                this.value = this.value.replace(/[^0-9a-zA-Z_]/g, '');
                errorText = "\nAllowed characters for the name are: letters(a-z,A-Z),"
                    + " digits, and underscores. ";
            }
            while(this.value.length > 0 && !this.value[0].match(/[a-zA-Z]/)){
                this.value = this.value.substring(1);
                errorText = "\nThe name must begin with a letter(a-z,A-Z).";
            }
            $("#"+id).find("label.error").text(validateCustomAttribute(id)+errorText);
        }
    });
    $("#"+id+"Name").on("focusout", function () {
        var newName = this.value;
        $("#"+id).find("label.error").text();
        var previousName = getItemEntered("customAttributes", id);
        if (previousName) {
            removeItemFromSessionString("customAttributes", id);
            removeItemFromSessionString("generalMetadata", previousName);
            removeItemFromSessionString("parseHeaderForMetadata", previousName);
        }
        var errorMessage = validateCustomAttribute(id);
        if (errorMessage){
            $("#"+id).find("label.error").text(errorMessage);
        } else {
            var customAttributesString = buildStringForSession("customAttributes", id, newName);
            addToSession("customAttributes", customAttributesString);
            var currentValue = $("#"+id+"Value")[0].value;
            var metadataString = buildStringForSession("generalMetadata", newName, currentValue);
            addToSession("generalMetadata", metadataString);
            var isPattern = $("#"+id+"CB").is(':checked');
            if (isPattern){
                var metadataString = buildStringForSession("parseHeaderForMetadata", newName, "true");
                addToSession("parseHeaderForMetadata", metadataString);
            }
        }
        checkAndExposeNext("generalMetadata");
    });
    $("#"+id+"Value").on("focusout", function () {
        var errorMessage = validateCustomAttribute(id);
        $("#"+id).find("label.error").text(errorMessage);
        if (!errorMessage) {
            var name = getItemEntered("customAttributes", id);
            if (name) {
                removeItemFromSessionString("generalMetadata", name);
            } else {
                name = $("#"+id+"Name").val();
            }
            var metadataString = buildStringForSession("generalMetadata", name, this.value);
            addToSession("generalMetadata", metadataString);
        }
        checkAndExposeNext("generalMetadata");
    });
    $("#"+id+"CB").on("change", function(){
        var name = getItemEntered("customAttributes", id);
        if (name){
            if ($(this).is(':checked')){
                //add to session
                var sessionString = buildStringForSession("parseHeaderForMetadata",
                                                          name,
                                                          "true");
                addToSession("parseHeaderForMetadata", sessionString);
            } else {
                //remove from session
                removeItemFromSessionString("parseHeaderForMetadata", name);
            }
        }
        var errorMessage = validateCustomAttribute(id);
        if (errorMessage) {
            removeItemFromSessionString("generalMetadata", name);
        }
        $("#"+id).find("label.error").text(errorMessage);
        // see if we can expose the next button
        checkAndExposeNext("generalMetadata");
    });
    nCustomAttributes++;
    // When adding a new custom attr - always disable the next
    checkAndExposeNext(false);
}

function validateCustomAttribute(baseID){
    var errorMessage = "";
    errorMessage += validateName(baseID);
    errorMessage += validateValue(baseID);
    return errorMessage;
}

function validateValue(baseID){
    var value = $("#"+baseID+"Value").val();
    if (!value) {
        return "\nValue can not be empty.";
    } else {
        var name = getItemEntered("customAttributes", baseID);
        var pattern = getItemEntered("parseHeaderForMetadata", name)
        if (pattern){
            if (!validatePattern(value)){
                return "Pattern is not valid";
            } else if (value.indexOf("(") <0){
                return "Pattern must contain at least one capturing group. It does not even contain a '('";
            }
        }
    }
    return "";
}

function validateName(baseID) {
    //TODO: Should also check for things that are reserved/generated
    //like Conventions cf_role or created_date etc?
    //Perhaps you want to add to e.g. the Conventions attribute also?
    var name = $("#"+baseID+"Name").val();
    if (!name) {
        return "Name can not be empty.";
    } else {
        var existsInGeneralMetadata = false;
        for ( var i in generalMetadata) {
            if (generalMetadata[i].tagName == name) {
                return "Name already in use by: "
                        + generalMetadata[i].displayName
                        + ".";
            }
        }
    }
    var existsInCustAtt = searchForValue("customAttributes", name, baseID);
    if (existsInCustAtt) {
        return "Name already in use by: " + existsInCustAtt;
    }
    return "";
}

function wrap(el, wrapType, id) {
    var idStr = "";
    if (id){
        idStr = " id='"+id+"'";
    }
    return "<" + wrapType + idStr+">" + el + "</" + wrapType + ">";
}

function specifyPlatformMetadata(stepType, stepData) {
    if (stepType == "stepValidation") {
        if (stepData.type == "next") {
            // platformMetadata array loaded via jstl
            error =
                validateMetadataEntries(getFromSession("platformMetadata"), "platformMetadata",
                                        stepData.currentStepIndex);
            if (!error) {
                return false;
            } else {
                return error;
            }
        }
    } else if (stepType == "repopulateStep") {
        //TODO: check the units!
        var newPlatform = findPlatformType();
        if (newPlatform != platform){
            platform = newPlatform;
            var metadataList = getPlatformMetadataList();
            /* 
             * TODO: The selectors are reset when HTML is (re)built.
             * E.g. degrees_south becomes degrees_north.
             * Previous session storage info is lost.
             */
            $("#platformMetadataDiv").html(createMetadataList(metadataList));
            // We need to run this before stepfunctions to not overwrite these
            // At least with how it works now. Should probably we reworked.
            repopulatePlatformMetadata(stepData.nextStepIndex)
            // TODO: Should move the code in here ?
            // I don't see the need for a stepFunctions when the HTML is generated in place.
            specifyPlatformMetadata("stepFunctions", 5)
        } else {
            repopulatePlatformMetadata(stepData.nextStepIndex)
        }
        //What other types could there be, and why would we not check then?
        if ((stepData.type == "previous") || (stepData.type == "next")) {
            // see if we can expose the next button
            checkAndExposeNext("platformMetadata");
        }
    } else if (stepType == "stepFunctions") {
        var stepElement = "#step" + stepData + " input[type='text']";
        $(stepElement).on("focusout", function () {
            var value = $(this).attr("value");
            var name = $(this).attr("name");
            if (value != "") {
                var isPattern = getItemEntered("parseHeaderForMetadata", name);
                var validPattern = validatePattern(value);
                if (isPattern && ( !validPattern || value.indexOf("(") <0 )) {
                    //Pattern is not valid
                    removeItemFromSessionString("platformMetadata", name);
                    var errorMessage = "Pattern is not valid";
                    if (validPattern) {
                        errorMessage = "Pattern must contain at least one capturing group. It does not even contain a '('";
                    }
                    $(this).closest("li").find("label.error").text(errorMessage);
                } else {
                    $(this).closest("li").find("label.error").text("");
                    // add to the session
                    var metadataString = buildStringForSession("platformMetadata", name,
                                                               value);
                    addToSession("platformMetadata", metadataString);
                }
            } else {
                // entered a blank value so get rid of it in the session
                removeItemFromSessionString("platformMetadata", name);
            }

            // see if we can expose the next button
            checkAndExposeNext("platformMetadata");
        });
        // When pattern/regex checkbox is toggled
        var stepElementCheckbox = "#step" + stepData + " input[type='checkbox']";
        $(stepElementCheckbox).on("change", function(){
            if ($(this).is(':checked')){
                //add to session
                var sessionString = buildStringForSession("parseHeaderForMetadata",
                                                          $(this).attr("name"),
                                                          "true");
                addToSession("parseHeaderForMetadata", sessionString);
            } else {
                //remove from session
                $(this).closest("li").find("label.error").text("");
                removeItemFromSessionString("parseHeaderForMetadata", $(this).attr("name"));
            }
            // see if we can expose the next button
            checkAndExposeNext("platformMetadata");
        });

        var stepElementSelect = "#step" + stepData + " select";
        // grab initial values for the select elements
        $(stepElementSelect).each(function () {
            if ($(this).attr("value") != "") {
                // add to the session
                var metadataString = buildStringForSession("platformMetadata", $(this).attr("name"),
                                                           $(this).attr("value"));
                addToSession("platformMetadata", metadataString);
            }
            // see if we can expose the next button
            checkAndExposeNext("platformMetadata");
        });

        $(stepElementSelect).change(function () {
            if ($(this).attr("value") != "") {
                // add to the session
                var metadataString = buildStringForSession("platformMetadata", $(this).attr("name"),
                                                           $(this).attr("value"));
                addToSession("platformMetadata", metadataString);
            }

            // see if we can expose the next button
            checkAndExposeNext("platformMetadata");
        });
    }
}

function createMetadataList(metadata){
    var list = "";
    for (var i in metadata){
        var element = "";
        // Add required-mark
        if (metadata[i].isRequired)
            element += "*";
        element += metadata[i].displayName;
        // Add help-text
        if (metadata[i].description){
            element += "<img src=\"resources/img/help.png\" alt=\"";
            element += metadata[i].description;
            element += "\"/>";
        }
        element += "<br>";
        // Add pattern checkbox
        element += "<input type='checkbox' name='" + metadata[i].tagName + "'>";
        element += " is a regex";
        element += "<br>";
        // Add the input field
        element += "<input type=\"text\" name=\"";
        element += metadata[i].tagName;
        element += "\" />";
        // Add the units selector
        if (metadata[i].units){
            if (metadata[i].units in units){
                element += "<select name=\"";
                element += metadata[i].tagName;
                element += "Units\">"
                element += units[metadata[i].units];
                element += "</select>";
            }
        } else {
            element += "<input type=\"text\" name=\"";
            element += metadata[i].tagName;
            element += "Units\" value=\"\" hidden />";
        }
        element = packElement(element, "label");
        var errorLabel = "<label class=\"error\"></label>"
        list += packElement(element+errorLabel, "li");
    }
    list = packElement(list, "ul");
    return list;
}

function packElement(content, element){
    return "<"+element+">"+content+"</"+element+">";
}

function checkAndExposeNext(metadata){
    var expose = true;
    var metadataList = [];
    switch(metadata) {
        case "platformMetadata":
            metadataList = getPlatformMetadataList();
            break;
        case "generalMetadata":
            metadataList = generalMetadata;
            for (var i = 0; i < nCustomAttributes; i++){
                var id = "customAttribute" + i;
                var errorMessage = validateCustomAttribute(id);
                $("#"+id).find("label.error").text(errorMessage);
                if (errorMessage){
                    expose = false;
                    metadataList = [];
                    break;
                }
            }
            break;
        case false:
            expose = false;
            break;
        default:
            return false;
    }
    for (var i in metadataList){
        var value = getItemEntered(metadata, metadataList[i].tagName);
        if (metadataList[i].isRequired){
            if (value == null){
                expose = false;
                break;
            }
        }
        // if its a search pattern, validate it
        if (getItemEntered("parseHeaderForMetadata",
                           metadataList[i].tagName)){
            if (!value || !validatePattern(value) || value.indexOf("(") <0 ){
                expose = false;
                break;
            }
        }
    }
    if (expose){
        $("#faux").addClass("hideMe");
        $(".jw-button-next").removeClass("hideMe");
    } else {
        $("#faux").removeClass("hideMe");
        $(".jw-button-next").addClass("hideMe");
    }
}

function validatePattern(pattern) {
    var isValid = true;
    try {
        new RegExp(pattern);
    } catch(e) {
        isValid = false;
    }
    return isValid;
}

function repopulatePlatformMetadata(step) {
    // populate text input elements from sessionStorage
    var stepElementText = "#step" + step + " input[type=\"text\"]";
    var inputElements = $(stepElementText);
    for (var i = 0; i < inputElements.length; i++) {
        var name = $(inputElements[i]).attr("name");
        var itemInSession = getItemEntered("platformMetadata", name);
        if (itemInSession != null) {
            $("input[name=\"" + name + "\"][type=\"text\"]").val(itemInSession);
        } else {
            $("input[name=\"" + name + "\"][type=\"text\"]").val("");
        }
    }

    // populate select elements from sessionStorage
    var stepElementSelect = "#step" + step + " select";
    var inputElementsSelect = $(stepElementSelect);
    for (var i = 0; i < inputElementsSelect.length; i++) {
        var name = $(inputElementsSelect[i]).attr("name");
        var itemInSession = getItemEntered("platformMetadata", name);
        if (itemInSession != null) {
            $("select[name=\"" + name + "\"]").val(itemInSession);
        } else {
            var metadataString = buildStringForSession("platformMetadata", name, $("select[name=\"" + name + "\"]").val());
            addToSession("platformMetadata", metadataString);
        }
    }
    
    // populate pattern checkboxes from sessionStorage
    var stepElementCb = "#step" + step + " input[type=\"checkbox\"]";
    var inputElements = $(stepElementCb);
    for (var i = 0; i < inputElements.length; i++) {
        var name = $(inputElements[i]).attr("name");
        var itemInSession = getItemEntered("parseHeaderForMetadata", name);
        if (itemInSession == "true") {
            $("input[name=\"" + name + "\"][type=\"checkbox\"]").prop('checked', true);
        } else {
            $("input[name=\"" + name + "\"][type=\"checkbox\"]").prop('checked', false);
        }
    }
}

function convertAndDownload(stepType, stepData) {
    if (stepType == "stepValidation") {
        // validate data entered from step
    } else if (stepType == "repopulateStep") {
        convertAndDownload("stepFunctions", stepData);
    } else if (stepType == "stepFunctions") {
        $.post("parse", stepData,
               function (data) {
                   var urls = data.split(/\r\n|\r|\n/g);
                   var download = $("ul#download");
                   //console.warn("here 1");
                   var templatePattern = /^\.template$/i;
                   var ncPattern = /^\.nc$/i;
                   //console.warn("here 2");
                   $(download).empty();
                   for (var i = 0; i < urls.length; i++) {
                       var fileExt = urls[i].match(/\.[a-zA-Z]{3,4}$/);
                       if (templatePattern.test(fileExt)) {
                           var linkName = "Rosetta transaction receipt"
                       } else if (ncPattern.test(fileExt)) {
                           var linkName = "netCDF Data File"
                       } else {
                           var linkName = urls[i];
                       }

                       var link = "<li><a href=\"" + "fileDownload/" + getFromSession("uniqueId")
                                  + "/" + urls[i] + "\" " + "download=\"" + urls[i] + "\" >"
                                  + linkName + "</a></li>";
                       //console.warn(link);
                       $(download).append(link);
                   }
               },
               "text");
        $(".jw-button-next").removeClass("hideMe")
        $(".jw-button-finish").addClass("hideMe");
        $("#faux").remove();
    }
}

function autoconvertAndDownload(stepType, stepData) {
    if (stepType == "stepValidation") {
        // validate data entered from step
    } else if (stepType == "repopulateStep") {
        autoconvertAndDownload("stepFunctions", stepData);
    } else if (stepType == "stepFunctions") {
        $.post("autoConvertKnownFile", stepData,
               function (data) {
                   var download = $("ul#download");
                   //console.warn("here 1");
                   var templatePattern = /^\.template$/i;
                   var ncPattern = /^\.nc$/i;
                   //console.warn("here 2");
                   $(download).empty();

                   var linkName = "Rosetta Autoconverted file"
                   var link = "<li><a href=\"" + "fileDownload/" + getFromSession("uniqueId") + "/"
                              + data + "\" " + "download=\"" + data + "\" >" + linkName
                              + "</a></li>";
                   //console.warn(link);
                   $(download).append(link);
               },
               "text");
        $(".jw-button-next").removeClass("hideMe")
        $(".jw-button-finish").addClass("hideMe");
        $("#faux").remove();
    }
}

function publish(stepType, stepData) {
    if (stepType == "stepValidation") {
        // validate data entered from step
    } else if (stepType == "repopulateStep") {
        // if we land on this page from a previous or next step
        // page, then repopulate from storage
    } else if (stepType == "stepFunctions") {
        $("#publish").bind("click", function () {

            var pubName = publisherName.value;
            var data = {
                "pubName": pubName,
                "userName": $(userName).val(),
                "auth": $(auth).val(),
                "pubDest": $(pubDest).val(),
                "uniqueId": getFromSession("uniqueId"),
                "fileName": getFromSession("fileName"),
                "generalMetadata": getFromSession("generalMetadata")
            };

            $.post("publish", data,
                   function (returnData) {
                       var pubMessage = $("ul#pubMessage");
                       $(pubMessage).empty();
                       if (returnData.indexOf("Incorrect") !== -1) {
                           pubMessage.append(
                               "<br><label class=\"error\">" + returnData + "</label>");
                       } else {
                           $("#publish").remove()
                           if (/ramadda/.test(pubName.toLowerCase())) {
                               var linkName = "View published data!";
                               pubMessage.append("<br><li><a href=\""
                                                 + "http://motherlode.ucar.edu/repository/entry/show?entryid="
                                                 + returnData + "\">" + linkName + "</a></li>");
                           } else if (/cadis/.test(pubName.toLowerCase())) {
                               var linkName = "Download link to published data!";
                               pubMessage.append(
                                   "<br><li><a href=\"" + returnData + "\">" + linkName
                                   + "</a></li>");
                           }
                       }
                   },
                   "text");
        });
    }
}

function restoreSession(stepType, stepData) {
    if (stepType == "stepValidation") {
        // validate data entered from step
    } else if (stepType == "repopulateStep") {
        restoreSession("stepFunctions", null);
    } else if (stepType == "stepFunctions") {
        var sessionData = getAllDataInSession();
        $.post("restoreFromZip",
               sessionData,
               function (data) {
                   //console.warn(data);
                   var restoredSessionStorage = JSON.parse(data);
                   for (var item in restoredSessionStorage) {
                       addToSession(item, restoredSessionStorage[item]);
                   }
                   // remove session storage things related to file upload
                   // as they are no longer needed after the repopulation of
                   // the session storage from the transaction receipt.
                   removeFromSession("uniqueId");
                   removeFromSession("fileName");
               },
               "text");

        $("#faux").remove();
        $(".jw-button-next").addClass("hideMe");
        $(".jw-button-finish").addClass("hideMe");

    }
}

function stepTemplate(stepType, stepData) {
    if (stepType == "stepValidation") {
        // validate data entered from step
    } else if (stepType == "repopulateStep") {
        // if we land on this page from a previous or next step
        // page, then repopulate from storage
    } else if (stepType == "stepFunctions") {
        // Do stuff needed to be done on page interactively
        return true;
    }
}