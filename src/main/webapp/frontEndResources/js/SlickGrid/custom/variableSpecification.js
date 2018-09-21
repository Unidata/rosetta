/**
 * SlickGrid/custom/variableSpecification.js
 *
 * This big, ugly beasty contains custom functions that create a SlickGrid instance
 * containing the data from the file the user uploaded.  The user will use the
 * SlickGrid instance to input variable attributes corresponding to the data columns.
 */

/**
 * Returns the sum of all numbers passed to the function.
 * @param {string} target - A string representing the characters you wish to replace.
 * @param {string} replace,emt - A string representing the characters you wish to replace @find
 *     with.
 *
 * @returns {string} A new string where all instances of @find have been replaced with @replace in
 *     the @str
 *
 */
String.prototype.replaceAll = function (target, replacement) {
    return this.split(target).join(replacement);
};

/**
 * This function creates a SlickGrid displaying the parsed file data by by row
 * and delimiter. The user will use the SlickGrid interface and the HeaderButtons
 * Plugin to provide in variable attributes using the JQuery dialog wizard.
 *
 * @param grid  The SlickGrid variable to act upon.
 * @param fileData  The fileData array containing the row data.
 * @param columns  The column definition (array) for the grid header.
 * @param rows  The empty rows array to be populated and passed to the grid.
 * @param LineNumberFormatter  The custom grid formatter for displaying the header line numbers.
 * @param delimiter  The delimiter used to parse the data.
 */
function gridForVariableSpecification(grid, fileData, columns, rows, LineNumberFormatter, delimiter) {

    // load global resources used for this step
    loadCFStandards();
    loadUnitBuilderData();
    loadCFStandardUnits();
    //loadMetadata();

    // SlickGrid options.
    var options = {
        editable: false,
        enableAddRow: false,
        enableColumnReorder: false,
        forceFitColumns: true
    };

    // Instead of using just rows[], we're using the dataView data model so we can use filtering
    var dataView;

    // assign the custom formatter
    columns[0]["formatter"] = LineNumberFormatter;

    // Populate rows[] (for the dataView) with the fileData and
    // format accordingly if the line is a header line or a data
    // line. If a data line, parse the data using the delimiter.
    $(function () {
        // get the header line numbers so we can identify the line type in the fileData array and format accordingly
        var headerLines = sessionStorage.getItem("headerLineNumbers").split(/,/g);

        // denote which is the first or "parent" header line that will be shown when the rest of
        // the header lines are collapsed
        var firstHeaderLine = headerLines[0];
        var colNumber;

        // loop through the fileData line by line
        for (var i = 0; i < fileData.length; i++) {
            var parent = null;
            if (fileData[i] !== "") { // sanity check to make sure the line isn't blank

                // an unglamerous way to keep track of where we are in the loop
                if (i === 0) {
                    bool = 1;
                }

                // create a placeholder object to hold the line data, starting with the line number
                // data for the first column
                var obj = {"line_number": i, "id": i.toString()};

                // test the data against the headerLines array
                if (jQuery.inArray(i.toString(), headerLines) < 0) { // it's not a header line
                    var dataItems;
                    // split the data line using the given delimiter
                    if (delimiter !== " ") {
                        dataItems = fileData[i].split(delimiter);
                    } else {
                        dataItems = fileData[i].split(/\s+/);
                        if (dataItems[0] === "") {
                            dataItems.splice(0, 1);
                        }
                    }

                    // find if this is the first iteration through the data lines in the loop and
                    // finish creating the columns[]
                    if (bool === 1) {
                        colNumber = dataItems.length;
                        // populate columns[]
                        for (var x = 0; x < colNumber; x++) {
                            // create placeholder column object
                            var colObject = {
                                id: x,
                                name: "column " + x,
                                field: x,
                                width: 100,
                                resizable: false,
                                sortable: false
                            };

                            // check to see if variable input has already been entered by user.
                            var variableName = getFromSession("variableName" + x);
                            if (variableName != null) { // data exists
                                // update the column name to be that of the assigned variable name
                                colObject.name = variableName;
                                if (testVariableCompleteness("variableName" + i,
                                        variableName)) {
                                    colObject["header"] = { // "header" option is used with the HeaderButtons Plugin
                                        buttons: [
                                            {
                                                cssClass: "done",
                                                command: "setVariable",
                                                tooltip: variableName
                                            }
                                        ]
                                    }
                                } else {
                                    colObject["header"] = { // "header" option is used with the HeaderButtons Plugin
                                        buttons: [
                                            {
                                                cssClass: "todo",
                                                command: "setVariable",
                                                tooltip: "data column " + x
                                            }
                                        ]
                                    }
                                }
                            } else { // no data stored.
                                // here is where we will do our check to see if any data has been entered prior.
                                colObject["header"] = { // "header" option is used with the HeaderButtons Plugin
                                    buttons: [
                                        {
                                            cssClass: "todo",
                                            command: "setVariable",
                                            tooltip: "data column " + x
                                        }
                                    ]
                                };
                            }
                            columns.push(colObject);
                        }
                        bool = 0;
                    }

                    // add the parsed line data to our placeholder object
                    for (var y = 0; y < dataItems.length; y++) {
                        obj[y] = dataItems[y];
                    }
                } else { // it's a header line
                    // add the un-parsed line data to our placeholder object
                    obj[0] = fileData[i];

                    // the default view of the grid will have the header lines collapsed/hidden
                    parent = firstHeaderLine;
                    if (i === Number(firstHeaderLine)) {
                        obj["_collapsed"] = true;
                    }
                }

                // specify which row (first) is the parent header row (used for filtering)
                obj["parent"] = parent;

                // add the object to rows[]
                rows[i] = obj;
            }
        }

        // initialize the data model, set the data items, and apply the headerLineFilter
        dataView = new Slick.Data.DataView({inlineFilters: true});
        dataView.beginUpdate();
        dataView.setItems(rows);
        dataView.setFilter(headerLineFilter);
        dataView.setFilterArgs(rows);
        dataView.endUpdate();

        // format the header line rows to set them apart from the data lines
        dataView.getItemMetadata = function (i) {
            if (i === Number(firstHeaderLine)) {
                return {
                    "cssClasses": "headerRow",
                    "columns": {
                        1: {
                            "colspan": colNumber
                        }
                    }
                };
            }
        };

        // initialize the session storage for variable info metadata.
        VariableStorageHandler.initialize((columns.length - 1));
       
        // initialize the grid with the data model
        grid = new Slick.Grid("#variableGrid", dataView, columns, options);

        // bind header line toggle events to the grid
        bindGridHeaderLineToggleEvent(grid, dataView, colNumber, headerLines,
            firstHeaderLine);

        // bind generic scroll events to the grid
        bindGridScrollEvent(colNumber, grid);

        // load the HeaderButtons Plugin to custom buttons to column headers
        var headerButtonsPlugin = new Slick.Plugins.HeaderButtons();

        // bind HeaderButtons Plugin events
        bindHeaderButtonsPluginEvent(headerButtonsPlugin, colNumber, grid);

        // register the HeaderButtons Plugin with the grid
        grid.registerPlugin(headerButtonsPlugin);

        // have all the columns been handled?
        testIfComplete(colNumber);

    });
}

/**
 * EVENT FUNCTIONS
 */

/**
 * This function listens and binds column headers button events for the SlickGrid
 * HeaderButtons Plugin. A dialog box is launched when the user selects a header
 * button (column).  This dialog box allows the user to input the variable attributes.
 *
 * @param headerButtonsPlugin  The SlickGrid HeaderButtons Plugin instance.
 * @param colNumber  The total number of columns in the grid.
 * @param grid  The grid to act upon.
 */
function bindHeaderButtonsPluginEvent(headerButtonsPlugin, colNumber, grid) {

    headerButtonsPlugin.onCommand.subscribe(function (e, args) {
        // get the current column id (number) & handle for the SlickGrid HeaderButtons Plugin button
        var id = args.column.id;

        // construct the variable name key
        var variableKey = "variableName" + id;

        // When the specified command event is triggered, launch the jQuery dialog widget.
        if (args.command === "setVariable") {
            $(function () { 
                // Specify jQuery dialog widget options.
                $("#dialog").dialog({
                    closeOnEscape: false,
                    title: "Enter Variable Attributes",
                    width: 500,
                    modal: true,
                    buttons: {
                        "done": function () {
                            //validateVariableData(variableKey, true);
                            // only if we don't have any errors
                            if ($("#dialog").find("label.error").text() === "") {
                                // get the variable name, assign to the column and update the header with the value
                                var variableName = getFromSession(variableKey);
                                grid.updateColumnHeader(id, variableName, "column " + id + ": "
                                    + variableName);

                                // make sure the column is enabled/disabled depending on the user's choice
                                checkIfColumnIsDisabled(colNumber, grid);
                                // have all the columns been handled?
                                testIfComplete(colNumber);

                                $(this).dialog("close");
                            }
                        },
                        "cancel": function () {
                            // remove variable info from variableMetadata value field
                            removeFromSession(variableKey);
                            removeFromSession(variableKey + "Metadata");

                            // ugh!  Kludge to counter the fact the grid header
                            // button resets to previous options if revisiting
                            // dialog.
                            checkIfColumnIsDisabled(colNumber, grid);

                            // have all the columns been handled?
                            testIfComplete(colNumber);
                            $(this).dialog("close");

                        }
                    },
                    open: function () {
                        $(document).on("keypress", (function (e) {
                            if (e.which === 13) {
                                $("button:contains('done')").trigger("click");
                            }
                        }));
                    },
                    close: function () {
                        $(document).off("keypress");
                    }
                });

                // If they've already entered in data for the variable and of they are revisiting the dialog, hide the cancel button.
                if (getFromSession(variableKey)) {
                    $("div.ui-dialog-buttonset button span:contains('cancel')").parents(
                        "button")
                        .addClass("hideMe");
                    $(".ui-dialog-titlebar-close").removeClass("hideMe");
                } else {
                    $(".ui-dialog-titlebar-close").addClass("hideMe");
                }

                // Add content to the dialog widget and bind event handlers
                DialogDomHandler.addContentToDialog(variableKey);
            });
        }
    });
}

/**
 * This function listens and binds variable input events for the dialog box.
 *
 * @param key  The key used to store the data in the variableMetadata value field.
 */
function bindDialogEvents(key) {

    // Assign the variable name or "do_not_use" to column data.
    $("#dialog #variableNameTypeAssignment input[name=\"variableNameType\"]").bind("click", function () {

            // Get rid of any prior error messages.
            $("#dialog #variableNameTypeAssignment").find("label.error").text("");

             // Assign the user's selection to the useColumnData variable.
            var useColumnData = $(this).val();

            // What was selected?
            if (useColumnData === "do_not_use") {
                // User has elected to not use this variable; save info to variableMetadata & disable rest of dialog.

                // Update the stored variable data with the "do_not_use" for a variable name.
                VariableStorageHandler.storeVariableData(key, "name", useColumnData);

                // Hide & remove any existing user entry for variable name input tag.
                $("#dialog #variableNameTypeAssignment input[name=\"variableName\"]").prop("value", "");
                $("label#variableNameAssignment").addClass("hideMe");

                // Hide the import metadata option.
                $("#dialog label.existingMetadataImporter").addClass("hideMe");

                // Disable the rest of the dialog DOM content.
                DialogDomHandler.disableVariableAttributes(key);

            } else { 
                // User has elected to use the column's data.

                // Reveal the input tag to collect the variabe name.
                $("#dialog #variableNameAssignment").removeClass("hideMe");

                // Can we offer them the option of importing data from another column?
                if (testIfMetadataImportPossible()) {
                    // Metadata for another column exists; so show option to import it.
                    $("#dialog label.existingMetadataImporter").removeClass("hideMe");
                }
            }
        });


    // variable name
    //$("#dialog #variableNameTypeAssignment input[name=\"variableName\"]").autocomplete({ source: cfStandards, delay: 0});

    // Variable name assignment.
    $("#dialog #variableNameTypeAssignment input[name=\"variableName\"]").focusout(function () {

        // Get rid of any prior error messages.
        $("#dialog #variableNameTypeAssignment").find("label.error").text("");

        // Assign the variable name (variableName) to the user input.
        var variableName = $(this).val();

        // Update the stored variable data with the variable name.
        VariableStorageHandler.storeVariableData(key, "name", variableName);

        // If the user entered a standard name for the variable, then we can use this value here.
        if (isCFStandardName(variableName)) {
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
        }

        // validate user input
        //validateVariableData(key);

        // If there are no validation errors, we can proceed.
        if ($("#dialog #variableNameTypeAssignment").find("label.error").text() === "") {
            DialogDomHandler.enableDiv("metadataTypeAssignment");
        }
    });

    // existing metadata importer activation: toggle existing metadata importer box
    $("#dialog #variableNameTypeAssignment input[type=\"checkbox\"]").bind("click", function () {
            $("#dialog #variableNameTypeAssignment #existingMetadataImporter").toggleClass("hideMe");
            createExistingMetadataImporter(key);
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
        DialogDomHandler.disableDiv("metadataTypeStructureAssignment");
        // Remove any prior vertical direction entries.
        DialogDomHandler.disableDiv("verticalDirectionAssignment");
        // Clear the prior required, recommended, or additional areas; need to repopulate them with different metadata profile data.
        $("#dialog #requiredMetadataAssignment ul").empty();
        $("#dialog #recommendedMetadataAssignment ul").empty();
        $("#dialog #additionalMetadataAssignment select").empty();

        // validate user input
        //validateVariableData(key);

        // If there are no validation errors, we can proceed.
        if ($("#dialog #metadataTypeAssignment").find("label.error").text() === "") {

            // Enable DOM attribute collection based on the metadata type selected.
            if (metadataType === "coordinate") {
                // Coordinate variable selected.
                DialogDomHandler.enableDiv("metadataTypeStructureAssignment");
            } else {
                // Data variable selected.
                DialogDomHandler.enableDiv("metadataValueTypeAssignment");
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
                DialogDomHandler.enableDiv("verticalDirectionAssignment");
            } else {
                // Vertical not chosen; undo any prior vertical direction selection options
                DialogDomHandler.disableDiv("verticalDirectionAssignment");

                // If it hasn't been enabled aready, show the metadataValueType assignment DOM section.
                DialogDomHandler.enableDiv("metadataValueTypeAssignment");
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
            DialogDomHandler.enableDiv("metadataValueTypeAssignment");
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

            // Populate the required, recommended, & additional metadata areas with metadata profile info.
            ComplianceLevelDataHandler.addComplainceLevelDataToDialog(key);
            // Reveal the metadata sections.
            DialogDomHandler.enableDiv("requiredMetadataAssignment");
            DialogDomHandler.enableDiv("recommendedMetadataAssignment");
            DialogDomHandler.enableDiv("additionalMetadataAssignment");
        }
    });
}


/**
 * This function binds general events associated with the metadata entries added to the dialog DOM.
 *
 * @param key  The key used to store the data in the variableMetadata value field.
 */
function bindGeneralMetadataEvents(key) {

    // required metadata
    $("#dialog #requiredMetadataAssignment input[type=\"text\"]").on("focusout", function () {
        // get rid of any error messages
        $(this).parents("li").find("label.error").text("");

        // concatenation the entered value to any existing Metadata values pulled from the variableMetadata value field
        var generalMetadataValue = $(this).val();
        var metadataString = buildStringForSession(key + "Metadata", $(this).attr("name"), generalMetadataValue);

        // update the data in the variableMetadata value field
        addToSession(key + "Metadata", metadataString);

        // validate user input
        //validateVariableData(key);
    });

    // unit builder activation: toggle unit builder box
    $("#dialog #requiredMetadataAssignment input[type=\"checkbox\"]").bind("click", function () {
        $("#dialog #requiredMetadataAssignment #unitBuilder").toggleClass("hideMe");
        createUnitBuilder(key);
    });

    // recommended metadata
    $("#dialog #recommendedMetadataAssignment input").on("focusout", function () {
        // get rid of any error messages
        $(this).parents("li").find("label.error").text("");

        // concatenation the entered value to any existing Metadata values pulled from the variableMetadata value field
        var metadataString = buildStringForSession(key + "Metadata", $(this).attr("name"), $(this).val());

        // update the data in the variableMetadata value field
        addToSession(key + "Metadata", metadataString);

        // validate user input
        //validateVariableData(key);
    });

    // additional metadata chooser
    $("#dialog #additionalMetadataAssignment img#additionalMetadataChooser").unbind("click").bind("click", function () {

        var additionalMetadataSelected = $("#dialog #additionalMetadataAssignment select[name=\"additionalMetadata\"]").val();

        // get the display name
        var displayName = getMetadataDisplayName(additionalMetadataSelected);

        // see if the user has already provided the value to some of these metadata items.
        var tagValue = getItemEntered(key + "Metadata", additionalMetadataSelected);
        if (tagValue === null) {
            tagValue = "";
        }

        var tag = createAdditionalMetadataTag(additionalMetadataSelected, displayName, tagValue);

        var additionalMetadataInputTags = $("#dialog #additionalMetadataAssignment ul");


        if ($(this).attr("alt") === "Add Metadata") { // Adding an additional metadata item

            // get rid of any global error messages
            $("#additionalMetadataAssignment").find("label[for=\"additionalMetadataAssignment\"].error").text("");

            if ($(additionalMetadataInputTags).length === 0) {
                // no metadata has been added yet, so create bulleted list and add tag.
                $("#dialog #additionalMetadataAssignment").append("<ul>" + tag + "</ul>");
            } else {
                // metadata has already been added and bulleted list exists.

                // trying to add an already existing metadata item: show error message
                if ($(additionalMetadataInputTags).find("li input[name=\"" + additionalMetadataSelected + "\"]").length > 0) {
                    $("#additionalMetadataAssignment").find("label[for=\"additionalMetadataAssignment\"].error")
                            .text("'" + getMetadataDisplayName(additionalMetadataSelected)
                            + "' has already been selected.");
                } else {
                    // append new tag
                    $(additionalMetadataInputTags).append(tag);
                }
            }

        } else { // Removing an additional metadata item
            // get rid of any global error messages
            $("#additionalMetadataAssignment").find("label[for=\"additionalMetadataAssignment\"].error").text("");

            if ($(additionalMetadataInputTags).find("li input[name=\"" + additionalMetadataSelected + "'\"]").length === 0) {
                // trying to add an remove a metadata item that doesn't exist: show error message
                $("#additionalMetadataAssignment").find("label[for=\"additionalMetadataAssignment\"].error")
                        .text("'" + getMetadataDisplayName(additionalMetadataSelected)
                        + "' has NOT been selected and therefore cannot be removed.");
            } else {
                // remove existing metadata item
                $(additionalMetadataInputTags).find("li input[name=\"" + additionalMetadataSelected + "\"]").parents("li").remove();
                var listChildren = $(additionalMetadataInputTags).find("li");
                if ($(listChildren).length <= 0) {
                    $(additionalMetadataInputTags).remove("ul");
                }
                // remove from variableMetadata value field as well
                removeItemFromSessionString(key + "Metadata", additionalMetadataSelected);
            }
        }
        // bind the events for the newly created additional metadata input tags
        bindAdditionalMetadataEvents(key);
    });
}

/**
 * This function binds events associated with the additional metadata entries added to the dialog DOM.
 *
 * @param key  The key used to store the data in the variableMetadata value field.
 */
function bindAdditionalMetadataEvents(key) {
    // additional metadata entries
    $("#dialog #additionalMetadataAssignment ul li input").on("focusout", function () {
        var tagName = $(this).attr("name");
        var tagValue = $(this).val();

        // get rid of any error messages
        $(this).parents("li").find("label[for=\"" + tagName + "\"].error").text("");

        // concatenation the entered value to any existing Metadata values pulled from the variableMetadata value field
        var metadataString = buildStringForSession(key + "Metadata", tagName, tagValue);

        // update the data in the variableMetadata value field
        addToSession(key + "Metadata", metadataString);

        // validate user input
        //validateVariableData(key);
    });
}


/**
 * This function binds events associated with the metadata import feature added to the dialog DOM.
 *
 * @param key  The key used to store the data in the variableMetadata value field.
 */
function bindImportMetadataEvents(key) {
    // metadata import selection
    $("#dialog #existingMetadataImporter select[name=\"existingMetadataChoice\"]").on("change", function () {
        if ($(this).val() !== "") {
            var name = getFromSession($(this).val());
            addToSession(key, name);
            var metadata = getFromSession($(this).val() + "Metadata");
            addToSession(key + "Metadata", metadata);
            populateDataFromStorage(key);
        }
    });
}

/**
 * FUNCTIONS THAT CREATE/ADD HTML TO DOM
 **/



/**
 * Creates the HTML input tag for collecting additional metadata.  This function is called
 * when the user selections a value from the additional metadata chooser, or when
 * pre-populating the dialog form with pre-existing values.
 *
 * @param tagName  The item that goes in the name attribute of the input tag.
 * @param displayName  The display or "pretty" name for the input tag.
 * @param tagValue  The pre-existing value gleaned from the variableMetadata value field (if it exists).
 */
function createAdditionalMetadataTag(tagName, displayName, tagValue) {
    // Create a form tag containing metadata information
    // The additional metadata sectional has a general error tag associated with it for general
    // errors, and we are creating error tags associated with the input tags for displaying
    // specific errors.
    return "   <li>\n" +
        "    <label for=\"" + tagName + "\" class=\"error\"></label> \n" +
        "    <label>\n" +
        "     " + displayName + "\n" +
        "     <input type=\"text\" name=\"" + tagName + "\" value=\"" + tagValue + "\"/> \n" +
        "    </label>\n" +
        "   </li>\n";
}



/**
 * Creates the initial HTML input tags for the existing metadata importer.
 *
 * @param key  The key used to store the data in the variableMetadata value field.
 */
function createExistingMetadataImporter(key) {
    var optionTags = "<option value=\"\">---- select one ----</option>";

    // get variables with metadata
    var variablesWithMetadata = getVariablesWithMetadata();
    for (var i = 0; i < variablesWithMetadata.length; i++) {
        if (testVariableCompleteness(variablesWithMetadata[i], getFromSession(variablesWithMetadata[i]))) {
            optionTags = optionTags +
                "<option value=\"" + variablesWithMetadata[i] + "\">" + getFromSession(variablesWithMetadata[i]) +
                " from column " + variablesWithMetadata[i].replace("variableName", "") +
                "</option>\n";
        }
    }

    // add to the DOM
    $("#dialog #existingMetadataImporter select[name=\"existingMetadataChoice\"]").append(optionTags);

    // bind events
    bindImportMetadataEvents(key);

}



/**
 * Uses the array of compliance level metadata attributes gleaned from server-side metadata profiles.
 * Creates a list of form tags for these items corresponding to the metadataType (coordinate or non-coordinate).
 *
 * @param key  The key used to access the stored variable data.
 * @param complianceLevel  Whether the attribute is required, recommended or additional.
 */

function populateMetadataInputTags(key, complianceLevel) {
    var metadataType = VariableStorageHandler.getVariableData(key, "metadataType");
    var metadataTags = [];
    // The metadataProfileVariableData comes from the server-side.
    for (var i = 0; i < metadataProfileVariableData.length; i++) {
        var metadataItem = metadataProfileVariableData[i];

        // Make the values from the metadata profile look like what is passed as args to this method.
        var type;
        if (metadataItem.metadataType === "CoordinateVariable") {
            type = "coordinate";
        }    
        if (metadataItem.metadataType === "DataVariable") {
            type = "non-coordinate";
        }  
        if (metadataItem.complianceLevel === "optional") {
            // We prefer the term additional to optional.
            metadataItem.complianceLevel = "additional";
        }

        var tag;
        // Grab the metadata entries that correspond to the metadataType (coordinate or non-coordinate) and iable necessity
        // (required, recommended, or additional), create a form tag containing that information.
        if (type === variableType) {
            if (metadataItem.complianceLevel === complianceLevel) {
                tag = createMetadataTagElement(key, metadataItem);
                metadataTags.push(tag);
            }
        }

    }
    // Sort the array.
    metadataTags.sort();
    var metadataTagsAsAString = "";
    for (var x = 0; x < metadataTags.length; x++) {
        metadataTagsAsAString = metadataTagsAsAString + metadataTags[x];
    }
    return metadataTagsAsAString;
}

/**
 * UTILITY FUNCTIONS
 **/

/**
 * This function gets any of the variable data stored in the
 * variableMetadata value field and populates the dialog box with those values.
 *
 * @param key  The key that will be used to store the variable name value in the variableMetadata value field.
 */
function populateDataFromStorage(key) {

    // disable all parts of the dialog content except the first part
    DialogDomHandler.disableVariableAttributes(key);

    // start of with variable name input hidden
    $("#dialog #variableNameAssignment").addClass("hideMe");

    // get the name of the variable supplied by the user
    var variableValue = getFromSession(key);
    if (variableValue) {  // the user has provided something for the variable name or opted not to use the column of data

        if (variableValue !== "Do Not Use") { // variable name provided
            var inputTag = $("#dialog #variableNameTypeAssignment input[name=\"variableNameType\"][value=\"assign\"]");
            $(inputTag).prop("checked", true);
            // add variable name input tag
            $("#dialog #variableNameAssignment").removeClass("hideMe");
            $("#dialog #variableNameAssignment input[name=\"variableName\"]").prop("value", variableValue);
            if (testIfMetadataImportPossible()) {
                // metadata exists for other metadata so show option to import it
                $("#dialog label.existingMetadataImporter").removeClass("hideMe");
            }

            // if they've provided a variable name, then enable the coordinate variable section
            DialogDomHandler.enableDiv("metadataTypeAssignment");

            // from here after, all data collected is stored in the variableMetadata value field as "Metadata"
            // if we have metadata in the variableMetadata value field, grab it and populate the input tags
            var variableMetadataInStorage = getFromSession(key + "Metadata");
            if (variableMetadataInStorage) {

                // coordinate variable
                var coordinateVariableSelected = getItemEntered(key + "Metadata", "_coordinateVariable");
                if (coordinateVariableSelected != null) {
                    // check the appropriate choice and update the metadata options accordingly
                    $("#dialog #metadataTypeAssignment input[name=\"isCoordinateVariable\"][value=\"" + coordinateVariableSelected + "\"]").prop("checked", true);
                    //addMetadataHTMLToDialog(key);

                    var coordinateVariableType = getItemEntered(key + "Metadata", "_coordinateVariableType");
                    if (coordinateVariableType != null) {
                        $("#dialog #metadataTypeStructureAssignment select[name=\"coordVarType\"]").val(coordinateVariableType);
                    }
                    // data type
                    DialogDomHandler.enableDiv("metadataValueTypeAssignment");
                    var dataTypeSelected = getItemEntered(key + "Metadata", "dataType");
                    if (dataTypeSelected != null) {
                        // check the appropriate choice and update the metadata options accordingly
                        $("#dialog #metadataValueTypeAssignment input[name=\"dataType\"][value=\"" + dataTypeSelected + "\"]").prop("checked", true);

                        // metadata
                        DialogDomHandler.enableDiv("requiredMetadataAssignment");
                        DialogDomHandler.enableDiv("recommendedMetadataAssignment");
                        DialogDomHandler.enableDiv("additionalMetadataAssignment");

                        // any required and recommended metadata stored in the variableMetadata value field gets
                        // inserted when the metadata HTML is added to the dialog DOM need to
                        // populate any additional metadata

                        // get the metadata from the variableMetadata value field string, minus the coordinateVariable and dataType entries
                        var metadataProvided = getAllButTheseFromSessionString(key + "Metadata", ["_coordinateVariable", "dataType"]);

                        // get the metadata names (not values) held in the variableMetadata value field
                        var metadataInStorage = getKeysFromSessionData(metadataProvided);

                        for (var i = 0; i < metadataInStorage.length; i++) {
                            if (isAdditionalMetadata(coordinateVariableSelected, metadataInStorage[i])) {

                                var displayName = getMetadataDisplayName(metadataInStorage[i]);

                                // see if the user has already provided the value to some of these metadata items.
                                var tagValue = getItemEntered(key + "Metadata", metadataInStorage[i]);

                                var tag = createAdditionalMetadataTag(metadataInStorage[i], displayName, tagValue);

                                // at the tag HTML to the dialog DOM
                                var additionalMetadataInputTags = $("#dialog #additionalMetadataAssignment ul");

                                if ($(additionalMetadataInputTags).length === 0) {
                                    // no metadata has been added yet, so create bulleted list and add tag.
                                    $("#dialog #additionalMetadataAssignment").append("<ul>" + tag + "</ul>");
                                } else {
                                    // metadata has aleady been added and bulleted list exists.
                                    $(additionalMetadataInputTags).append(tag);
                                }
                            }
                        }
                    }
                }
            }
        } else { // do not use column data selected
            $("#dialog #variableNameTypeAssignment input[name=\"variableNameType\"][value=\"do_not_use\"]").prop("checked", true);
        }
    }
}

/**
 * Tests to see if metadata from another column is available for import.
 * Looks to see if 1) variable metadata exists in the variableMetadata value field; and 2) if
 * all the required metadata for that variable is present and not incomplete.
 */
function testIfMetadataImportPossible() {
    // see if any metadata has been entered for other variables
    var variablesWithMetadata = getVariablesWithMetadata();
    if (variablesWithMetadata.length > 0) {
        for (var i = 0; i < variablesWithMetadata.length; i++) {
            if (testVariableCompleteness(variablesWithMetadata[i], getFromSession(variablesWithMetadata[i]))) {
                return true;
            } else {
                if (i = (variablesWithMetadata.length - 1)) {
                    return false;
                }
            }
        }
    } else {
        return false;
    }

}




/**
 * This function checks all columns of the grid and if the user has decided
 * not to use the data from a column, it disables the column by calling the
 * disableColumn function. Otherwise, makes sure the column of data is enabled.
 *
 * @param colNumber  The total number of data columns in the grid.
 * @param grid  The grid object we are working on.
 */
function checkIfColumnIsDisabled(colNumber, grid) {
    // loop through all of the data columns
    for (var i = 0; i < colNumber; i++) {
        // get the variable name assigned by the user for the column
        var assignedVariableName = getFromSession("variableName" + i);
        // if we have data for this column
        if (assignedVariableName) {
            var headerLines = sessionStorage.getItem("headerLineNumbers").split(/,/g);
            var firstHeaderLine = headerLines[0];
            var item = grid.getDataItem(0);
            if (item) {
                var rowLength = grid.getDataLength();
                if (!item._collapsed) {
                    for (var x = 0; x < rowLength; x++) {
                        if (jQuery.inArray(x.toString(), headerLines) < 0) {
                            if (assignedVariableName === "Do Not Use") {
                                disableColumn(grid.getCellNode(x, (i + 1)));
                            } else {
                                enableColumn(grid.getCellNode(x, (i + 1)));
                            }
                        }
                    }
                } else {
                    for (var y = 0; y < rowLength; y++) {
                        if (y !== firstHeaderLine) {
                            if (assignedVariableName === "Do Not Use") {
                                disableColumn(grid.getCellNode(y, (i + 1)));
                            } else {
                                enableColumn(grid.getCellNode(y, (i + 1)));
                            }
                        }
                    }
                }
            }
            if (testVariableCompleteness("variableName" + i, assignedVariableName)) {
                hackGridButtonElement(i, assignedVariableName);
            }
        }
    }
}

/**
 * This function is kludgy hack to work around to alter the button element
 * (unable to make immediate changes using args.button). It makes sure the
 * column header is checked as complete and the tooltip is the variable name
 * if the variable data can be found in the variableMetadata value field.
 *
 * @param id  The column number.
 * @param variableName  The name of the variable assigned to by the user.
 */
function hackGridButtonElement(id, variableName) {
    var buttonElement = $("div[title=\"data column " + id + "\"].slick-header-button");
    if (buttonElement.length > 0) {
        $(buttonElement).removeClass("todo").addClass("done");
        $(buttonElement).prop("title", "column " + id + ": " + variableName);
    }
}

/**
 * Checks to see if the needed input was supplied by the user by checking the variable
 * string. If the input exists, the next button is activated so the user can proceed.
 *
 * @param colNumber  The total number of SlickGrid columns.
 */
function testIfComplete(colNumber) {
    // loop through all the data columns
    for (var i = 0; i < colNumber; i++) {
        // see if we have values for the column in the variableMetadata value field
        var variableName = getFromSession("variableName" + i);
        // something exists in the variableMetadata value field, see if the metadata exists as well
        if (variableName) {
            if (testVariableCompleteness("variableName" + i, variableName)) {
                if (i === (colNumber - 1)) {
                    // remove disabled status for submit button.
                    $("input[type=submit]#Next").removeAttr("disabled");
                    // remove disabled class for submit button.
                    $("input[type=submit]#Next").removeClass("disabled");
                }

            } else { // not all metadata is present: break
                break;
            }
        } else { // no value stored, not done yet: break
            break;
        }
    }
}

/**
 * Checks to see if the needed input for a particular variable/column of data is complete.
 * Checks to make sure that the coordinate variable, data type and required metadata values
 * are present in the variableMetadata value field.  This function is called by the testIfComplete() function.
 *
 * @param key  The key used to store the data in the variableMetadata value field.
 * @param variableName  The variable name assigned to the column of data by the user.
 */
function testVariableCompleteness(key, variableName) {
    if (variableName !== "Do Not Use") {
        // do we have the coordinateVariable?
        var coordinateVariableInStorage = getItemEntered(key + "Metadata", "_coordinateVariable");
        if (coordinateVariableInStorage != null) {
            // do we have the dataType?
            var dataTypeInStorage = getItemEntered(key + "Metadata", "dataType");
            if (dataTypeInStorage != null) {
                // do we have the required metadata?
                var metadataProvided = getAllButTheseFromSessionString(key + "Metadata", ["_coordinateVariable", "dataType"]);
                if (metadataProvided.length > 0) {
                    // get the metadata names (not values) held in the variableMetadata value field
                    var metadataInStorage = getKeysFromSessionData(metadataProvided);
                    var requiredMetadata = getKnownRequiredMetadataList(coordinateVariableInStorage);
                    for (var i = 0; i < requiredMetadata.length; i++) {
                        if (metadataInStorage.indexOf(requiredMetadata[i]) < 0) {
                            // some required metadata is missing
                            return false;
                        }
                    }
                } else { // ALL metadata is missing
                    return false;
                }
            } else { // dataType is missing
                return false;
            }
        } else {  // coordinateVariable is missing
            return false;
        }
    }
    return true;
}


