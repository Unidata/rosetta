/**
 * DialogDOMHandler.js
 *
 * General DOM manipulation functions for dialog used in variable metadata collection step.
 */
var DialogDomHandler = (function () {

    var cfStandards = [];
    var cfStandardUnits = {};

    /**
     * This function adds HTML input tags with which the user will provide the
     * data associated with the column. This HTML is added to the dialog DOM
     * and the event handlers for the inserted HTML are bound.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     */
    function addContentToDialog(key) {
        $("#dialog").empty(); // start off with a dialog box free of content

        var dialogContent = 
            "<div id=\"variableNameTypeAssignment\">\n" +
            " <h3>What would you like to do with this column of data?</h3>\n" +
            " <label class=\"error\"></label>" +
            " <ul class=\"half\">\n" +
            "  <li>\n" +
            "   <label>\n" +
            "    <input type=\"radio\" name=\"variableNameType\" value=\"assign\"/> Assign a variable name\n" +
            "   </label>\n" +
            "   <label id=\"variableNameAssignment\">\n" +
            "    <input type=\"text\" name=\"variableName\" value=\"\"/>\n" +
            "   </label>\n" +
            "   <label class=\"metadataImporter hideMe\">\n" +
            "     Use metadata from another column?  \n" +
            "    <input type=\"checkbox\" name=\"metadataImporter\" value=\"true\"/> \n" +
            "   </label>\n" +
            "   <div id=\"metadataImporter\" class=\"hideMe\">\n" +
            "    <label for=\"metadataChoice\" class=\"whole\">\n" +
            "     Import metadata from: \n" +
            "     <select name=\"metadataChoice\">\n" +
            "     </select>\n" +
            "    </label>\n" +
            "   </div>\n" +
            "  </li>\n" +
            "  <li>\n" +
            "   <label>\n" +
            "    <input type=\"radio\" name=\"variableNameType\" value=\"DO_NOT_USE\"/> Do not use this column of data\n" +
            "   </label>\n" +
            "  </li>\n" +
            " </ul>\n" +
            "</div>\n" +
            "<div id=\"variableAttributes\">\n" +
            " <div id=\"metadataTypeAssignment\">\n" +
            "  <h3>Is this variable a coordinate variable? <small>(examples: latitude, longitude, time)</small></h3>\n" +
            "  <ul class=\"third\">\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"metadataType\" value=\"coordinate\"/> Yes\n" +
            "    </label>\n" +
            "   </li>\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"metadataType\" value=\"non-coordinate\"/> No\n" +
            "    </label>\n" +
            "   </li>\n" +
            "  </ul>\n" +
            "  <label for=\"metadataTypeAssignment\" class=\"error\"></label>" +
            " </div> <!--#metadataTypeAssignment -->\n" +
            " <div id=\"metadataTypeStructureAssignment\">\n" +
            "  <h3>What type of coordinate variable?</h3>\n" +
            "  <label for=\"metadataTypeStructureAssignment\" class=\"error\"></label>\n" +
            "  <select name=\"metadataTypeStructure\" autocomplete=\"nope\">\n" +
            "    <option value=\"\">---- select one ----</option>\n" +
            "    <option value=\"latitude\">latitude</option>\n" +
            "    <option value=\"longitude\">longitude</option>\n" +
            "    <option value=\"vertical\">vertical</option>\n" +
            "    <option value=\"relativeTime\">Relative time (e.g. hours since 1970-01-01)</option>\n" +
            "    <option value=\"fullDateTime\">Full date and time string</option>\n" +
            "    <option value=\"dateOnly\">Date only (year, month, and/or day)</option>\n" +
            "    <option value=\"timeOnly\">Time only (hour, minute, second, and/or millisecond)</option>\n" +
            "  </select>\n" +
            " </div> <!--#metadataTypeStructureAssignment -->\n" +
            " <div id=\"verticalDirectionAssignment\">\n" +
            "  <h3>Which direction do the vertical values increase?</h3>\n" +
            "  <ul class=\"third\">\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"verticalDirection\" value=\"up\"/> Up\n" +
            "    </label>\n" +
            "   </li>\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"verticalDirection\" value=\"down\"/> Down\n" +
            "    </label>\n" +
            "   </li>\n" +
            "  </ul>\n" +
            "  <label class=\"error\"></label>" +
            " </div> <!-- #verticalDirectionAssignment -->\n" +
            " <div id=\"metadataValueTypeAssignment\">\n" +
            "  <h3>Specify variable data type:</h3>\n" +
            "  <ul class=\"third\">\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"metadataValueType\" value=\"Integer\"/> Integer\n" +
            "    </label>\n" +
            "   </li>\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"metadataValueType\" value=\"Float\"/> Float (decimal)\n" +
            "    </label>\n" +
            "   </li>\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"metadataValueType\" value=\"Text\"/> Text\n" +
            "    </label>\n" +
            "   </li>\n" +
            "  </ul>\n" +
            "  <label class=\"error\"></label>" +
            " </div> <!-- #metadataValueTypeAssignment -->\n" +
            " <div id=\"requiredMetadataAssignment\">\n" +
            "  <h3>Required Metadata:</h3>\n" +
            "  <ul>\n" +
            "  </ul>\n" +
            " </div><!-- #requiredMetadataAssignment -->\n" +
            " <div id=\"recommendedMetadataAssignment\">\n" +
            "  <h3>Recommended Metadata:</h3>\n" +
            "  <ul>\n" +
            "  </ul>\n" +
            " </div><!-- recommendedMetadataAssignment -->\n" +
            " <div id=\"additionalMetadataAssignment\">\n" +
            "  <h3>Additional Metadata:</h3>\n" +
            "  <ul>\n" +
            "  </ul>\n" +  
            " </div><!-- additionalMetadataAssignment -->\n" +
            "</div><!-- variableAttributes -->\n";

        $("#dialog").append(dialogContent);

        // Start off with the name input box hidden.
        disableDiv("variableNameAssignment");

        // Start off with only the first part of the dialog visible (everything in the variableAttributes div is hidden).
        disableVariableAttributes();
        // Use any stored data to auto-populate the dialog web form elements.
        populateVariableDataFromStorage(key);
        // Bind dialog events.
        bindDialogEvents(key);
    }

    /**
     * Private, utility function (not exported).
     * This function listens and binds variable input events for the dialog box.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     */
    function bindDialogEvents(key) {

        // Assign the variable name or "DO_NOT_USE" to column data.
        $("#dialog #variableNameTypeAssignment input[name=\"variableNameType\"]").bind("click", function () {

            // Get rid of any prior error messages.
            $("#dialog #variableNameTypeAssignment").find("label.error").text("");

            // Assign the user's selection to the useColumnData variable.
            var useColumnData = $(this).val();

            // What was selected?
            if (useColumnData === "DO_NOT_USE") {
                // User has elected to not use this variable; save info to variableMetadata & disable rest of dialog.

                // Disable the rest of the dialog DOM content.
                disableVariableAttributes(key);

                // Update the stored variable data with the "DO_NOT_USE" for a variable name.
                VariableStorageHandler.storeVariableData(key, "name", useColumnData);

                // Hide & remove any existing user entry for variable name input tag.
                $("#dialog #variableNameTypeAssignment input[name=\"variableName\"]").prop("value", "");
                $("label#variableNameAssignment").addClass("hideMe");

                // Hide the import metadata option.
                $("#dialog label.metadataImporter").addClass("hideMe");
                disableDiv("metadataImporter");

            } else {
                // User has elected to use the column's data.
                // Reveal the input tag to collect the variable name.
                enableDiv("variableNameAssignment");
                // Can we offer them the option of importing data from another column?
                if (MetadataImporter.isImportPossible()) {
                    // Metadata for another column exists; so show option to import it.
                    $("#dialog label.metadataImporter").removeClass("inactive").removeClass("hideMe");
                }
            }
        });


        // variable name
        //$("#dialog #variableNameTypeAssignment input[name=\"variableName\"]").autocomplete({ source: cfStandards, delay: 1});

        // Variable name assignment.
        $("#dialog #variableNameTypeAssignment input[name=\"variableName\"]").focusout(function () {

            // Get rid of any prior error messages.
            $("#dialog #variableNameTypeAssignment").find("label.error").text("");

            // Assign the variable name (variableName) to the user input.
            var variableName = $(this).val();

            // Update the stored variable data with the variable name.
            VariableStorageHandler.storeVariableData(key, "name", variableName);

            // Get possible CF standard name matches for the variable.
            var cfStandardInfo = getCFStandardInfo(variableName);

            // If we have potential matching Cf standard names:
            if (cfStandardInfo.length > 0) {

                // If the matching cfStandardInfo array contains only one item, use that item for the standard_name & units.
                if (cfStandardInfo.length === 1) {
                    // Update the stored variable data with the standard name.
                    VariableStorageHandler.storeComplianceLevelVariableData(key, "standard_name", cfStandardInfo[0], "required");
                    // Automatically add any corrsponding units.
                    var units = cfStandardUnits[cfStandardInfo[0]];
                    if (units !== "") {
                        if (units !== undefined) {
                            // Update the stored variable data with the units.
                            VariableStorageHandler.storeComplianceLevelVariableData(key, "units", units, "required");
                        }
                    }

                    // Remove any prior match data from web storage.
                    VariableStorageHandler.removeCfStandardMatches(variableName);
                    
                } else {  // Multiple options for the CF standard name exist.

                    // If the matching cfStandardInfo array contains exactly the variableName (user inputted the standard name)
                    // use that item for the standard_name & units.
                    if (cfStandardInfo.includes(variableName)) {
                        // Update the stored variable data with the standard name.
                        VariableStorageHandler.storeComplianceLevelVariableData(key, "standard_name", variableName, "required");
                        // Automatically add any corrsponding units.
                        var units = cfStandardUnits[variableName];
                        if (units !== "") {
                            if (units !== undefined) {
                                // Update the stored variable data with the units.
                                VariableStorageHandler.storeComplianceLevelVariableData(key, "units", units, "required");
                            }
                        }

                        // Remove any prior match data from web storage.
                        VariableStorageHandler.removeCfStandardMatches(variableName);

                    } else {
                        // Store the possible values for user selection later.
                        var standardNameMatches = {};
                        for (var i = 0; i < cfStandardInfo.length; i++) {
                            var standardName = cfStandardInfo[i];
                            standardNameMatches[standardName] = cfStandardUnits[cfStandardInfo[i]];
                        }
                        VariableStorageHandler.storeCfStandardMatches(variableName, JSON.stringify(standardNameMatches));
                    }
                }
            } else {
                // No matching CF standard information.  Remove any such prior entry from web storage.
                VariableStorageHandler.removeCfStandardMatches(variableName);
            }
            // validate user input
            //validateVariableData(key);

            // If there are no validation errors, we can proceed.
            if ($("#dialog #variableNameTypeAssignment").find("label.error").text() === "") {
                enableDiv("metadataTypeAssignment");
            }
        });

        // existing metadata importer activation: toggle existing metadata importer box
        $("#dialog #variableNameTypeAssignment input[type=\"checkbox\"]").bind("click", function () {
            $("#dialog #variableNameTypeAssignment #metadataImporter").toggleClass("hideMe");
            MetadataImporter.create(key);
        });


        // Coordinate or data variable selection (metadataType).
        $("#dialog #metadataTypeAssignment input[name=\"metadataType\"]").bind("click", function () {

            // Get rid of any prior error messages.
            $("#dialog #metadataTypeAssignment").find("label.error").text("");

            // Assign the metadata type (metadataType) to the user input.
            var metadataType = $(this).val();

            // Update the stored variable data with the chosen metadata type (coordinate or data variable).
            VariableStorageHandler.storeVariableData(key, "metadataType", metadataType);

            // Remove any prior collected entries from variable that does not pertain to the metadata type.
            // (This is needed if the user goes back to change the metadata type selection... we don't want to keep mismatched metadata.)
            VariableStorageHandler.removeNonMetadataTypeEntriesFromVariableData(key);

            // Remove any prior metadata type structure entries.
            disableDiv("metadataTypeStructureAssignment");
            // Remove any prior vertical direction entries.
            disableDiv("verticalDirectionAssignment");
            // Remove any prior metadata type value entries.
            disableDiv("metadataValueTypeAssignment");
            // Remove the prior required, recommended, or additional areas; need to repopulate them with different metadata profile data.
            disableDiv("requiredMetadataAssignment");
            $("#dialog #requiredMetadataAssignment ul").empty();
            disableDiv("recommendedMetadataAssignment");
            $("#dialog #recommendedMetadataAssignment ul").empty();
            disableDiv("additionalMetadataAssignment");
            $("#dialog #additionalMetadataAssignment select").empty();

            // validate user input
            //validateVariableData(key);

            // If there are no validation errors, we can proceed.
            if ($("#dialog #metadataTypeAssignment").find("label.error").text() === "") {

                // Enable DOM attribute collection based on the metadata type selected.
                if (metadataType === "coordinate") {
                    // Coordinate variable selected.
                    enableDiv("metadataTypeStructureAssignment");
                } else {
                    // Data variable selected.
                    enableDiv("metadataValueTypeAssignment");
                }
            }
        });

        // Coordinate variable "flavor" (metadataTypeStructure).
        $("#dialog #metadataTypeStructureAssignment select[name=\"metadataTypeStructure\"]").bind("change", function () {

            // Get rid of any prior error messages.
            $("#dialog #metadataTypeStructureAssignment").find("label.error").text("");

            // Assign the metadata type structure (metadataTypeStructure) to the user input.
            var metadataTypeStructure = $(this).val();

            // Update the stored variable data with the chosen metadata type structure.
            VariableStorageHandler.storeVariableData(key, "metadataTypeStructure", metadataTypeStructure);

            // Validate user input.
            var coordVarTypeError = lookForBlankSelection($(this).val(), "Coordinate Variable Type");
            if (coordVarTypeError != null) {
                $("#dialog #metadataTypeStructureAssignment").find("label.error").text("Please specify the coordinate variable type.");
            }

            // If there are no validation errors, we can proceed.
            if ($("#dialog #metadataTypeStructureAssignment").find("label.error").text() === "") {

                // Determine if 'vertical' was selected for the metadata type structure.
                if (metadataTypeStructure === "vertical") {
                    // Vertical selected; enable DOM section that determines direction of vertical.
                    enableDiv("verticalDirectionAssignment");
                } else {
                    // Vertical not chosen; undo any prior vertical direction selection options
                    disableDiv("verticalDirectionAssignment");

                    // If it hasn't been enabled aready, show the metadataValueType assignment DOM section.
                    enableDiv("metadataValueTypeAssignment");
                }
            }
        });

        // Vertical direction selection (only used if metadataTypeStructure=vertical was selected).
        $("#dialog #verticalDirectionAssignment input[name=\"verticalDirection\"]").bind("click", function () {

            // Get rid of any prior error messages.
            $("#dialog #verticalDirection").find("label.error").text("");

            // Assign the vertical direction (verticalDirection) to the user input.
            var verticalDirection = $(this).val();

            // Update the stored variable data with the chosen vertical direction.
            VariableStorageHandler.storeVariableData(key, "verticalDirection", verticalDirection);

            // validate user input
            //validateVariableData(key);

            // if there are no validation errors, we can proceed
            if ($("#dialog #metadataValueTypeAssignment").find("label.error").text() === "") {
                // If it hasn't been enabled aready, show the metadataValueType assignment DOM section.
                enableDiv("metadataValueTypeAssignment");
            }
        });


        // Metadata type value (metadataValueType).
        $("#dialog #metadataValueTypeAssignment input[name=\"metadataValueType\"]").bind("click", function () {

            // Get rid of any prior error messages.
            $("#dialog #metadataValueTypeAssignment").find("label.error").text("");

            // Assign the metadata type value (metadataValueType) to the user input.
            var metadataValueType = $(this).val();

            // Update the stored variable data with the chosen metadata type value.
            VariableStorageHandler.storeVariableData(key, "metadataValueType", metadataValueType);

            // validate user input
            //validateVariableData(key);

            // If there are no validation errors, we can proceed
            if ($("#dialog #metadataValueTypeAssignment").find("label.error").text() === "") {

                // Populate the required, recommended, & additional metadata areas with metadata profile info & reveal the metadata sections.
                VariableComplianceLevelDataHandler.addComplianceLevelDataToDialog(key);
            }
        });
    }


    /**
     * Private, utility function (not exported).
     * Disables the input tags and de-emphasizes the text of provided section in the dialog form.
     *
     * @param dialogDomSection The section of the dialog DOM to disable.
     */
    function disableDiv(dialogDomSection) {
        // Disable this section of the dialog content.
        $("#dialog #" + dialogDomSection).addClass("inactive");
        var inputTags = $("#dialog #" + dialogDomSection).find("input");
        if (inputTags.length > 0) {
            $(inputTags).each(function () {
                $(this).prop("disabled", true);
                if ($(this).attr("type") === "text") {
                    $(this).prop("value", "");
                } else { // radio or checkbox
                    $(this).prop("checked", false);
                }
            });
        }
        var selectTags = $("#dialog #" + dialogDomSection).find("select");
        $(selectTags).val("");

    }

    /**
     * Private, utility function (not exported).
     * Disables ALL of the input tags, de-emphasizes the text in the #variableAttributes
     * section of the dialog form, and removes any corresponding  stored data.
     *
     * @param key
     */
    function disableVariableAttributes(key) {
        // Disable all parts of the dialog content except the first part.
        $("#dialog #variableAttributes").find("div").each(function () {
            var divTag = $(this);
            $(divTag).addClass("inactive");
            $(divTag).find("input").each(function () {
                var inputTag = $(this);
                $(inputTag).prop("disabled", true);
                if ($(inputTag).attr("type") === "text") {
                    $(inputTag).prop("value", "");
                } else { // radio or checkbox
                    $(inputTag).prop("checked", false);
                }
                var divTagId = $(divTag).attr("id").replace("MetadataAssignment", "");
                // Remove any stored data associated with these sections.
                if (key !== undefined) {
                    VariableStorageHandler.resetVariableData(key);
                }
            });
        });
    }

    /**
     * Enables the input tags and emphasizes the text of provided section in the dialog form.
     *
     * @param dialogDomSection  The section of the dialog DOM to enable.
     */
    function enableDiv(dialogDomSection) {
        // Enable this section of the dialog content
        $("#dialog #" + dialogDomSection).removeClass("inactive");
        $("#dialog #" + dialogDomSection).removeClass("hideMe");
        $("#dialog #" + dialogDomSection).find("input").each(function () {
            $(this).prop("disabled", false);
        });
    }

    /**
     * Uses the array of cfStandard names gleaned from the cf-standard-name-table.xml file.
     * Loop through the array and looks for any entries that contain the provided variableName.
     * Adds any such matches to an array for returning to the caller.
     *
     * @param variableName  The name of the variable we are testing to see if it is a standard_name.
     * @return an array containing matching Cf Standard names. (Can be empty if no matches found).
     */
    function getCFStandardInfo(variableName) {
        if (variableName === "*") {
            return cfStandards;
        }
        var cfStandardMatches = [];

        // Split the provided variable name.
        var variableNamePieces = variableName.replace(/_/g, " ").split(" ");
        for (var j = 0; j < variableNamePieces.length; j++) {
            for (var i = 0; i < cfStandards.length; i++) {
                var cfStandardNamePieces = cfStandards[i].split("_");
                if (cfStandardNamePieces.includes(variableNamePieces[j])) {
                    if (!cfStandardMatches.includes(cfStandards[i])) { // No duplicates.
                        cfStandardMatches.push(cfStandards[i]);
                    }
                }
            }
        }
        return cfStandardMatches;
    }

    /**
     * Populates the cfStandards array with data from the cf-standard-name-table.xml file.
     */
    function loadCFStandards() {
        $.get("resources/cf-standard-name-table.xml",
              function (data) {
                  var s = [];
                  $(data).find("entry").each(function () {
                      s.push($(this).attr("id"));
                  });
                  cfStandards = s;
              },
              "xml");
    }

    /**
     * Populates the cfStandardsUnits object with data from the cf-standard-name-table.xml file.
     */
    function loadCFStandardUnits() {
        $.get("resources/cf-standard-name-table.xml",
              function (data) {
                  var u = {};
                  $(data).find("entry").each(function () {
                      u[$(this).attr("id")] = $(this).find("canonical_units").text();
                  });
                  cfStandardUnits = u;
              },
              "xml");
    }

    /**
     * This function gets any of the variable data stored in the variableMetadata value field and populates the dialog box with those values.
     *
     * @param key  The key that will be used to store the variable name value in the variableMetadata value field.
     */
    function populateVariableDataFromStorage(key) {
        // Get the variable name from storage.
        var variableName = VariableStorageHandler.getVariableData(key, "name");

        if (variableName) {
            // The user has provided something for the variable name or opted not to use the column of data.

            if (variableName !== "DO_NOT_USE") {
                // Variable name provided.

                // Check the 'Assign a variable name' radio button
                var inputTag = $("#dialog #variableNameTypeAssignment input[name=\"variableNameType\"][value=\"assign\"]");
                $(inputTag).prop("checked", true);


                // Add variable name to input tag.
                $("#dialog #variableNameAssignment").removeClass("inactive").removeClass("hideMe");
                $("#dialog #variableNameAssignment input").removeAttr("disabled");
                $("#dialog #variableNameAssignment input[name=\"variableName\"]").val(variableName);

                if (MetadataImporter.isImportPossible()) {
                    // Metadata exists for other metadata so show option to import it.
                    $("#dialog label.metadataImporter").removeClass("inactive").removeClass("hideMe");
                }

                // If they've provided a variable name, then enable the coordinate variable section
                enableDiv("metadataTypeAssignment");

                // Get the metadataType from storage.
                var metadataType = VariableStorageHandler.getVariableData(key, "metadataType");

                // Check the metadata type radio button using the metadataType information from storage.
                $("#dialog #metadataTypeAssignment input").each(function () {
                    var inputValue = $(this).val();
                    if (inputValue === metadataType) {
                        $(this).prop("checked", true);
                    }
                });

                // If the metadataType was coordinate, enable & populate the metadataTypeStructureAssignment section.
                if (metadataType === "coordinate") {

                    // Get the metadataTypeStructure from storage.
                    var metadataTypeStructure =  VariableStorageHandler.getVariableData(key, "metadataTypeStructure");

                    // Enable.
                    enableDiv("metadataTypeStructureAssignment");

                    $("#dialog #metadataTypeStructureAssignment select[name=\"metadataTypeStructure\"]").val(metadataTypeStructure);

                    // If metadataTypeStructure is vertical, enable & populate the vertical direction section.
                    if (metadataTypeStructure === "vertical") {

                        // Get the verticalDirection from storage.
                        var verticalDirection = VariableStorageHandler.getVariableData(key, "verticalDirection");

                        // Enable.
                        enableDiv("verticalDirectionAssignment");

                        // Check the metadata type radio button using the metadataType information from storage.
                        $("#dialog #verticalDirectionAssignment input").each(function () {
                            var inputValue = $(this).val();
                            if (inputValue === verticalDirection) {
                                $(this).prop("checked", true);
                            }
                        });

                    }

                }

                // Get the metadataValueType from storage.
                var metadataValueType = VariableStorageHandler.getVariableData(key, "metadataValueType");

                // Enable metadata value type section.
                enableDiv("metadataValueTypeAssignment");

                // Check the metadata value type radio button using the metadataType information from storage.
                $("#dialog #metadataValueTypeAssignment input").each(function () {
                    var inputValue = $(this).val();
                    if (inputValue === metadataValueType) {
                        $(this).prop("checked", true);
                    }
                });

                // Enable and populate compliance-level data.
                enableDiv("requiredMetadataAssignment");
                enableDiv("recommendedMetadataAssignment");
                enableDiv("additionalMetadataAssignment");
                VariableComplianceLevelDataHandler.addComplianceLevelDataToDialog(key);

            } else {
                // Do not use column data selected; check the 'do not use this column' radio button.
                $("#dialog #variableNameTypeAssignment input[name=\"variableNameType\"][value=\"DO_NOT_USE\"]").prop("checked", true);
            }
        }
    }

    // Expose these functions.
    return {
        addContentToDialog: addContentToDialog,
        enableDiv: enableDiv,
        getCFStandardInfo: getCFStandardInfo,
        loadCFStandards: loadCFStandards,
        loadCFStandardUnits: loadCFStandardUnits,
        populateVariableDataFromStorage: populateVariableDataFromStorage
    };
})();
