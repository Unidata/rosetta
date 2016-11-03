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
        if ((stepData.type == "previous") || (stepData.type == "next")) {
            if (getItemEntered("generalMetadata", "title") != null) {
                if (getItemEntered("generalMetadata", "institution") != null) {
                    if (getItemEntered("generalMetadata", "description") != null) {
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }
            }
        }
        var stepElement = "#step" + stepData.nextStepIndex + " input";
        var inputElements = $(stepElement);
        for (var i = 0; i < inputElements.length; i++) {
            var name = $(inputElements[i]).attr("name");
            var itemInSession = getItemEntered("generalMetadata", name);
            if (itemInSession != null) {
                $("input[name=\"" + name + "\"]").val(itemInSession);
            } else {
                $("input[name=\"" + name + "\"]").val("");
            }
        }
    } else if (stepType == "stepFunctions") {
        var stepElement = "#step" + stepData + " input";
        var stepCheck = ".jw-step:eq(" + stepData + ")";
        $(stepElement).on("focusout", function () {
            if ($(this).attr("value") != "") {
                // add to the session
                var metadataString = buildStringForSession("generalMetadata", $(this).attr("name"),
                                                           $(this).attr("value"));
                addToSession("generalMetadata", metadataString);
            } else {
                // entered a blank value so get rid of it in the session
                removeItemFromSessionString("generalMetadata", $(this).attr("name"));
            }

            // see if we can expose the next button
            if (getItemEntered("generalMetadata", "title") != null) {
                if (getItemEntered("generalMetadata", "institution") != null) {
                    if (getItemEntered("generalMetadata", "description") != null) {
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }
            }
        });
    }
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
        if ((stepData.type == "previous") || (stepData.type == "next")) {
            if (getItemEntered("platformMetadata", "platformName") != null) {
                if (getItemEntered("platformMetadata", "latitude") != null) {
                    if (getItemEntered("platformMetadata", "longitude") != null) {
                        if (getItemEntered("platformMetadata", "altitude") != null) {
                            $("#faux").remove();
                            $(".jw-button-next").removeClass("hideMe");
                        }
                    }
                }
            }
        }

        // populate input elements from sessionStorage
        var stepElement = "#step" + stepData.nextStepIndex + " input";
        var inputElements = $(stepElement);
        for (var i = 0; i < inputElements.length; i++) {
            var name = $(inputElements[i]).attr("name");
            var itemInSession = getItemEntered("platformMetadata", name);
            if (itemInSession != null) {
                $("input[name=\"" + name + "\"]").val(itemInSession);
            } else {
                $("input[name=\"" + name + "\"]").val("");
            }
        }

        // populate select elements from sessionStorage
        var stepElementSelect = "#step" + stepData.nextStepIndex + " select";
        var inputElementsSelect = $(stepElementSelect);
        for (var i = 0; i < inputElementsSelect.length; i++) {
            var name = $(inputElementsSelect[i]).attr("name");
            var itemInSession = getItemEntered("platformMetadata", name);
            if (itemInSession != null) {
                $("select[name=\"" + name + "\"]").val(itemInSession);
            } else {
                var metadataString = buildStringForSession("platformMetadata", name,
                                                           $("select[name=\"" + name + "\"]")
                                                               .val());
                addToSession("platformMetadata", metadataString);
            }
        }

    } else if (stepType == "stepFunctions") {
        var stepElement = "#step" + stepData + " input";
        var moveAlongInput = false;
        var moveAlongSelect = false;
        $(stepElement).on("focusout", function () {
            if ($(this).attr("value") != "") {
                // add to the session
                var metadataString = buildStringForSession("platformMetadata", $(this).attr("name"),
                                                           $(this).attr("value"));
                addToSession("platformMetadata", metadataString);
            } else {
                // entered a blank value so get rid of it in the session
                removeItemFromSessionString("platformMetadata", $(this).attr("name"));
            }

            // see if we can expose the next button
            if (getItemEntered("platformMetadata", "platformName") != null) {
                if (getItemEntered("platformMetadata", "latitude") != null) {
                    if (getItemEntered("platformMetadata", "longitude") != null) {
                        if (getItemEntered("platformMetadata", "altitude") != null) {
                            moveAlongInput = true;
                            if (moveAlongSelect) {
                                $("#faux").remove();
                                $(".jw-button-next").removeClass("hideMe");
                            }
                        }
                    }
                }
            }
        });

        var stepElementSelect = "#step" + stepData + " select";
        // grab initial values for tje select elements
        $(stepElementSelect).each(function () {
            if ($(this).attr("value") != "") {
                // add to the session
                var metadataString = buildStringForSession("platformMetadata", $(this).attr("name"),
                                                           $(this).attr("value"));
                addToSession("platformMetadata", metadataString);
            }
            // see if we can expose the next button
            if (getItemEntered("platformMetadata", "altitudeUnits") != null) {
                if (getItemEntered("platformMetadata", "latitudeUnits") != null) {
                    if (getItemEntered("platformMetadata", "longitudeUnits") != null) {
                        moveAlongSelect = true
                        if (moveAlongInput) {
                            $("#faux").remove();
                            $(".jw-button-next").removeClass("hideMe");
                        }
                    }
                }
            }
        });

        $(stepElementSelect).change(function () {
            if ($(this).attr("value") != "") {
                // add to the session
                var metadataString = buildStringForSession("platformMetadata", $(this).attr("name"),
                                                           $(this).attr("value"));
                addToSession("platformMetadata", metadataString);
            }

            // see if we can expose the next button
            if (getItemEntered("platformMetadata", "altitudeUnits") != null) {
                if (getItemEntered("platformMetadata", "latitudeUnits") != null) {
                    if (getItemEntered("platformMetadata", "longitudeUnits") != null) {
                        moveAlongSelect = true
                        if (moveAlongInput) {
                            $("#faux").remove();
                            $(".jw-button-next").removeClass("hideMe");
                        }
                    }
                }
            }
        });
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