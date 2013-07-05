function selectPlatform(stepType, stepData) {
    // stepTypes:
    //    stepValidation - validate input for current step
    //    repopulateStep - repopulate user input (if exists) if user lands
    //                     on the step using a next or previous step
    //    stepFunctions - things that need to be done interactively during the step
    //
    //    stepData (optional) - any data you need to pass into the function
    if (stepType == "stepValidation") {
        // If we land on this page and user has already enter something
        // (e.g., clicked previous or used the menu to navigate)
        // Don't hide the 'Next' button
        error = validateItemExistsInSession(stepData.currentStepIndex, "cfType", "You must select a platform to continue.");
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
            }
        }
    } else if (stepType == "stepFunctions") {
        var inputName = "#step" + stepData + " input";
        $(inputName).bind("click", function() {
            addToSession("cfType", $(this).val());
            // Show 'Next' button after user makes a selection
            $(".jw-button-next").removeAttr("disabled").removeClass("disabled");
        });
    }
}

function uploadDataFile(stepType, stepData) {
    if (stepType == "stepValidation") {
        error = validateItemExistsInSession(stepData.currentStepIndex, "uniqueId", "You need to upload a file to continue.");
        if (!error) {
            return false;
        } else {
            return error;
        }
    } else if (stepType == "repopulateStep") {
        // Initially hide the upload button (will appear when user opens file chooser)
        $("#upload").addClass("hideMe");
        if ((stepData.type == "previous") || (stepData.type == "next")) {
            if (getFromSession("uniqueId")) {
                $("#file").addClass("hideMe");
                progressBarCallback();
                $("#faux").remove();
                $(".jw-button-next").removeClass("hideMe");
            }
        }
    } else if (stepType == "stepFunctions") {
        $("#file").bind("change", function() {
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

        $("#upload").bind("click", function() {
            // Upload file and add to session
            var up = instantiateUploader($("#progress"), $(".jw-step:eq(1)").find("label.error"), $("#upload"));
            up.send();
            addToSession("fileName", cleanFilePath($("#file").val()));
            // show 'Next' button after user uploads file
            $("#file").addClass("hideMe");
        });

        $("#clearFileUpload").bind("click", function() {
            //removed uploaded file from session and recreate the upload form.
            removeFromSession("uniqueId");
            removeFromSession("fileName");
            $("#file").removeClass("hideMe");
            $("#clearFileUpload").addClass("hideMe");
            // clear progress bar
            $("#progress").attr("style","").addClass("progress");
            $("#progress").html("0%");
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
            error = validateItemExistsInSession(stepData.currentStepIndex, "headerLineNumbers", "You need to specify which lines are header lines to continue.");
            if (!error) {
                return false;
            } else {
                return error;
            }
        }
    } else if (stepType == "repopulateStep") {
        $.post("parse", { uniqueId: getFromSession("uniqueId"), fileName: getFromSession("fileName") },
            function(data) {
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
            error = validateItemExistsInSession(stepData.currentStepIndex, "delimiters", "You need to specify at least one delimiter to continue.");
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
                $("#step3 #delimiter").each(function(){
                    if (getFromSession("delimiters").search($(this).val()) >= 0 ) {
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
        }
    } else if (stepType == "stepFunctions") {
        var stepElement = "#step" + stepData + " input:checkbox";
        var stepCheck = ".jw-step:eq(" + stepData + ")";
        $(stepElement).bind("click", function() {
            $(stepCheck).find("label.error").text("");
            // create array from selected values
            var checkedDelimiters = $("input:checkbox").serializeArray();
            var delimiterArray = [];
            $.each(checkedDelimiters, function(index, field){
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

        $("#otherDelimiter").on("focusin", function() {
            $(stepCheck).find("label.error").text("");
        });

        $("#otherDelimiter").on("focusout", function() {
            addToSession("otherDelimiter", $(this).val());
            if (getFromSession("delimiters")) {
                // Show 'Next' button after user makes a selection
                $("#faux").remove();
                $(".jw-button-next").removeClass("hideMe");
            }
        });
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