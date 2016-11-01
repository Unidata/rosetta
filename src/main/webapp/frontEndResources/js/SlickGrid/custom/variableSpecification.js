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

// string to replace : in units...used for sessionStorage.
var colonReplacement = "DotDot";

/**
 * This function creates a SlickGrid displaying the parsed file data by by row
 * and delimiter. The user will use the SlickGrid interface and the HeaderButtons
 * Plugin to provide in variable attributes using the JQuery dialog wizard.
 *
 * @param grid  The SlickGrid variable to act upon.
 * @param fileData  The fileData array containing the row data.
 * @param columns  The column definition (array) for the gris header.
 * @param rows  The empty rows array to be populated and passed to the grid.
 * @param LineNumberFormatter  The line number formatter for toggling the header lines.
 * @param step  The current step number in the jWizard interface.
 */
function gridForVariableSpecification(grid, fileData, columns, rows, LineNumberFormatter, step) {

    // load global resources used for this step
    loadCFStandards();
    loadUnitBuilderData();
    loadCFStandardUnits();
    loadMetadata();

    // The SlickGrid options for this particular grid.
    var options = {
        editable: false,
        enableAddRow: false,
        enableColumnReorder: false,
        forceFitColumns: true,
        enableCellNavigation: true
    };

    // Instead of using just rows[], we're using the dataView data model so we can use filtering
    var dataView;

    // assign the custom formatter
    columns[0]["formatter"] = LineNumberFormatter;

    // Populate rows[] (for the dataView) with the fileData and
    // format accordingly if the line is a header line or a data
    // line. If a data line, parse the data using the delimiter. 
    $(function () {
        // get the header line numbers from the session so we can identify the line type in the
        // fileData array and format accordingly
        var headerLines = getFromSession("headerLineNumbers").split(/,/g);

        // denote which is the first or "parent" header line that will be shown when the rest of
        // the header lines are collapsed
        var firstHeaderLine = headerLines[0];

        // grab the "common" delimiter from the fileData
        var delimiter = fileData.shift();
        var colNumber;

        // loop through the fileData line by line
        for (var i = 0; i < fileData.length; i++) {
            var parent = null;
            if (fileData[i] != "") { // sanity check to make sure the line isn't blank  

                // an unglamerous way to keep track of where we are in the loop              
                if (i == 0) {
                    bool = 1;
                }

                // create a placeholder object to hold the line data, starting with the line number
                // data for the first column
                var obj = {"line_number": i, "id": i};

                // test the data against the headerLines array                        
                if (jQuery.inArray(i.toString(), headerLines) < 0) { // it's not a header line

                    // split the data line using the given delimiter
                    if (delimiter != " ") {
                        var dataItems = fileData[i].split(delimiter);
                    } else {
                        var dataItems = fileData[i].split(/\s+/);
                        if (dataItems[0] == "") {
                            dataItems.splice(0, 1);
                        }
                    }

                    // find if this is the first iteration through the data lines in the loop and
                    // finish creating the columns[]
                    if (bool == 1) {
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

                            // check session to see if variable input has already been entered by
                            // user
                            var variableName = getFromSession("variableName" + x);
                            if (variableName != null) { // data already in session
                                // update the column name to be that of the assigned variable name
                                colObject.name = variableName;
                                if (testVariableCompleteness("variableName" + i, variableName)) {
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
                            } else { // no data in session
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
                            columns.push(colObject);
                        }
                        bool = 0;
                    }

                    // add the parsed line data to our placeholder object 
                    for (var x = 0; x < dataItems.length; x++) {
                        obj[x] = dataItems[x];
                    }

                } else { // it's a header line    

                    // add the un-parsed line data to our placeholder object
                    //
                    obj[0] = fileData[i];

                    // the default view of the grid will have the header lines collapsed/hidden
                    parent = firstHeaderLine;
                    if (i == firstHeaderLine) {
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
            if (i == firstHeaderLine) {
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

        // initialize the grid with the data model
        grid = new Slick.Grid("#variableGrid", dataView, columns, options);

        // bind header line toggle events to the grid 
        bindGridHeaderLineToggleEvent(grid, dataView, colNumber, headerLines, firstHeaderLine);

        // bind generic scroll events to the grid 
        bindGridScrollEvent(colNumber, grid);

        // load the HeaderButtons Plugin to custom buttons to column headers
        var headerButtonsPlugin = new Slick.Plugins.HeaderButtons();

        // bind HeaderButtons Plugin events 
        bindHeaderButtonsPluginEvent(headerButtonsPlugin, colNumber, grid, step);

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
 * @param step  The current step number in the jWizard interface.
 */
function bindHeaderButtonsPluginEvent(headerButtonsPlugin, colNumber, grid, step) {

    headerButtonsPlugin.onCommand.subscribe(function (e, args) {
        // get the current column id (number) & handle for the SlickGrid HeaderButtons Plugin button
        var id = args.column.id;

        // construct the variable name key for the session
        var sessionKey = "variableName" + id;

        // when the specified command event is triggered, launch the jQuery dialog widget
        if (args.command == "setVariable") {
            $(function () { // specify jQuery dialog widget options
                $("#dialog").dialog({
                                        closeOnEscape: false,
                                        title: "Enter Variable Attributes",
                                        width: 500,
                                        modal: true,
                                        buttons: {
                                            "done": function () {
                                                validateVariableData(sessionKey, true);
                                                // only if we don't have any errors
                                                if ($("#dialog").find("label.error").text()
                                                    === "") {

                                                    // get the variable name and assign to the
                                                    // column and update the header with the value
                                                    //
                                                    var variableName = getFromSession(sessionKey);
                                                    grid.updateColumnHeader(id,
                                                                            "column " + id + ": "
                                                                            + variableName,
                                                                            "column " + id + ": "
                                                                            + variableName);

                                                    // make sure the column is enabled/disabled
                                                    // depending on the user's choice
                                                    checkIfColumnIsDisabled(colNumber, grid);

                                                    // have all the columns been handled?
                                                    testIfComplete(colNumber);

                                                    $(this).dialog("close");
                                                }
                                            },
                                            "cancel": function () {
                                                // remove variable info from session
                                                removeFromSession(sessionKey);
                                                removeFromSession(sessionKey + "Metadata");

                                                // ugh!  Kludge to counter the fact the grid header
                                                // button resets to previous options if revisiting
                                                // dialog.
                                                checkIfColumnIsDisabled(colNumber, grid);

                                                // have all the columns been handled?
                                                testIfComplete(colNumber);
                                                $(this).dialog("close");

                                            }
                                        }
                                    });

                // if they've already entered in data for the variable and of they are revisiting
                // the dialog, hide the cancel button
                if (getFromSession(sessionKey)) {
                    $("div.ui-dialog-buttonset button span:contains('cancel')").parents("button")
                        .addClass("hideMe");
                    $(".ui-dialog-titlebar-close").removeClass("hideMe");
                } else {
                    $(".ui-dialog-titlebar-close").addClass("hideMe");
                }

                // Add content to the dialog widget and bind event handlers
                addContentToDialog(sessionKey);
            });
        }
    });
}

/**
 * This function listens and binds variable input events for the dialog box.
 *
 * @param sessionKey  The key used to store the data in the session.
 */
function bindDialogEvents(sessionKey) {

    // assign variable name or do not to use column data
    $("#dialog #variableNameTypeAssignment input[name=\"variableNameType\"]")
        .bind("click", function () {

            $("#dialog #variableNameTypeAssignment").find("label.error").text(""); // get rid of
                                                                                   // any error
                                                                                   // messages

            // do not use this variable: save that info to session and close dialog
            if ($(this).attr("value") == "do_not_use") {
                variableName = "Do Not Use";

                // update the data in the session
                addToSession(sessionKey, variableName);
                removeFromSession(sessionKey + "Metadata");

                // remove any existing entry for variable name input
                $("#dialog #variableNameTypeAssignment input[name=\"variableName\"]")
                    .attr("value", "");
                $("label#variableNameAssignment").addClass("hideMe");

                // hide the import metadata option
                // (can appear of variable data was inputed before & then user chooses to not use
                // column data)
                $("#dialog label.existingMetadataImporter").addClass("hideMe");

                // disable all parts of the dialog content except the first part
                disableVariableAttributes();

                // enter in variable name
            } else {
                $("#dialog #variableNameAssignment").removeClass("hideMe");
                if (testIfMetadataImportPossible()) {
                    // metadata exists for other metadata so show option to import it
                    $("#dialog label.existingMetadataImporter").removeClass("hideMe");
                }
            }
        });

    // variable name
    $("#dialog #variableNameTypeAssignment input[name=\"variableName\"]").autocomplete({
                                                                                           source: cfStandards,
                                                                                           delay: 0
                                                                                       });

    // variable name assignment
    $("#dialog #variableNameTypeAssignment input[name=\"variableName\"]").focusout(function () {

        $("#dialog #variableNameTypeAssignment").find("label.error").text(""); // get rid of any
                                                                               // error messages

        // update the data in the session
        addToSession(sessionKey, $(this).attr("value"));

        // if the user selected a standard name for the variable, then we can use this value here. 
        if (isCFStandardName($(this).attr("value"))) {
            // standard_name
            var metadataString = buildStringForSession(sessionKey + "Metadata", "standard_name",
                                                       $(this).attr("value"));
            addToSession(sessionKey + "Metadata", metadataString);
            // units
            var units = cfStandardUnits[$(this).attr("value")];
            if (units != "") {
                if (units != undefined) {
                    metadataString = buildStringForSession(sessionKey + "Metadata", "units", units);
                    addToSession(sessionKey + "Metadata", metadataString);
                }
            }
        }
        addMetadataHTMLToDialog(sessionKey);

        // validate user input    
        validateVariableData(sessionKey);

        // if there are no validation errors, we can proceed
        if ($("#dialog #variableNameTypeAssignment").find("label.error").text() === "") {
            enableVariableAttributes("coordinateVariableAssignment");
        }
    });

    // existing metadata importer activation: toggle existing metadata importer box
    $("#dialog #variableNameTypeAssignment input[type=\"checkbox\"]").bind("click", function () {
        $("#dialog #variableNameTypeAssignment #existingMetadataImporter").toggleClass("hideMe");
        createExistingMetadataImporter(sessionKey);
    });

    // coordinate variable
    $("#dialog #coordinateVariableAssignment input[name=\"isCoordinateVariable\"]")
        .bind("click", function () {

            $("#dialog #coordinateVariableAssignment").find("label.error").text(""); // get rid of
                                                                                     // any error
                                                                                     // messages

            // concatenation the entered value to any existing Metadata values pulled from the
            // session
            var metadataString = buildStringForSession(sessionKey + "Metadata",
                                                       "_coordinateVariable",
                                                       $(this).attr("value"));

            // update the data in the session
            addToSession(sessionKey + "Metadata", metadataString);
            removeAllButTheseFromSessionString(sessionKey + "Metadata",
                                               ["standard_name", "units", "_coordinateVariable",
                                                "dataType", "long_name"]);

            // update the metadata choices based on the user input
            addMetadataHTMLToDialog(sessionKey, $(this).attr("value"));

            // validate user input
            validateVariableData(sessionKey);
            // if there are no validation errors, we can proceed
            if ($("#dialog #coordinateVariableAssignment").find("label.error").text() === "") {
                enableVariableAttributes("dataTypeAssignment");
            }
        });

    $("#dialog #coordinateVarTypeAssignment select[name=\"coordVarType\"]")
        .bind("change", function () {

            $("#dialog #coordinateVarTypeAssignment").find("label.error").text(""); // get rid of
                                                                                    // any error
                                                                                    // messages

            // concatenation the entered value to any existing Metadata values pulled from the
            // session
            var metadataString = buildStringForSession(sessionKey + "Metadata",
                                                       "_coordinateVariableType",
                                                       $(this).attr("value"));
            removeItemFromSessionString(sessionKey + "Metadata", "_coordinateVariableType");
            addToSession(sessionKey + "Metadata", metadataString);

            // validate user input

            var coordVarTypeError = lookForBlankSelection($(this).attr("value"),
                                                          "Coordinate Variable Type");
            if (coordVarTypeError != null) {
                $("#dialog #coordinateVarTypeAssignment").find("label.error")
                    .text("Please specify the coordinate variable type.");
            }

            // if there are no validation errors, we can proceed
            if ($("#dialog #coordinateVarTypeAssignment").find("label.error").text() === "") {
                enableVariableAttributes("dataTypeAssignment");
            }
        });

    // data type
    $("#dialog #dataTypeAssignment input[name=\"dataType\"]").bind("click", function () {

        $("#dialog #dataTypeAssignment").find("label.error").text(""); // get rid of any error
                                                                       // messages

        // concatenation the entered value to any existing Metadata values pulled from the session
        var metadataString = buildStringForSession(sessionKey + "Metadata", "dataType",
                                                   $(this).attr("value"));

        // update the data in the session
        addToSession(sessionKey + "Metadata", metadataString);

        // validate user input 
        validateVariableData(sessionKey);

        // if there are no validation errors, we can proceed
        if ($("#dialog #dataTypeAssignment").find("label.error").text() === "") {
            enableVariableAttributes("requiredMetadataAssignment");
            enableVariableAttributes("recommendedMetadataAssignment");
            enableVariableAttributes("additionalMetadataAssignment");
        }
    });
}

/**
 * This function binds general events associated with the metadata entries added to the dialog DOM.
 *
 * @param sessionKey  The key used to store the data in the session.
 */
function bindGeneralMetadataEvents(sessionKey) {

    // required metadata
    $("#dialog #requiredMetadataAssignment input[type=\"text\"]").on("focusout", function () {
        $(this).parents("li").find("label.error").text(""); // get rid of any error messages

        // concatenation the entered value to any existing Metadata values pulled from the session
        var generalMetadataValue = $(this).attr("value");
        if ($(this).attr("name") == "units") {
            generalMetadataValue = generalMetadataValue.replaceAll(colonReplacement, ":");
            generalMetadataValue = generalMetadataValue.replaceAll(":", colonReplacement);
        }
        var metadataString = buildStringForSession(sessionKey + "Metadata", $(this).attr("name"),
                                                   generalMetadataValue);

        // update the data in the session
        addToSession(sessionKey + "Metadata", metadataString);

        // validate user input 
        validateVariableData(sessionKey);
    });

    // unit builder activation: toggle unit builder box
    $("#dialog #requiredMetadataAssignment input[type=\"checkbox\"]").bind("click", function () {
        $("#dialog #requiredMetadataAssignment #unitBuilder").toggleClass("hideMe");
        createUnitBuilder(sessionKey);
    });

    // recommended metadata
    $("#dialog #recommendedMetadataAssignment input").on("focusout", function () {
        $(this).parents("li").find("label.error").text(""); // get rid of any error messages

        // concatenation the entered value to any existing Metadata values pulled from the session
        var metadataString = buildStringForSession(sessionKey + "Metadata", $(this).attr("name"),
                                                   $(this).attr("value"));

        // update the data in the session
        addToSession(sessionKey + "Metadata", metadataString);

        // validate user input 
        validateVariableData(sessionKey);
    });

    // additional metadata chooser
    $("#dialog #additionalMetadataAssignment img#additionalMetadataChooser").unbind("click")
        .bind("click", function () {
            var additionalMetadataSelected = $(
                "#dialog #additionalMetadataAssignment select[name=\"additionalMetadata\"]").val();

            // get the display name
            var displayName = getMetadataDisplayName(additionalMetadataSelected);

            // see if the user has already provided the value to some of these metadata items.
            var tagValue = getItemEntered(sessionKey + "Metadata", additionalMetadataSelected);
            if (tagValue == null) {
                tagValue = "";
            }

            var tag = createAdditionalMetadataTag(additionalMetadataSelected, displayName,
                                                  tagValue);

            var additionalMetadataInputTags = $("#dialog #additionalMetadataAssignment ul");

            // Adding an additional metadata item
            if ($(this).attr("alt") == "Add Metadata") {

                $("#additionalMetadataAssignment")
                    .find("label[for=\"additionalMetadataAssignment\"].error").text(""); // get rid
                                                                                         // of any
                                                                                         // global
                                                                                         // error
                                                                                         // messages

                if ($(additionalMetadataInputTags).length == 0) {
                    // no metadata has been added yet, so create bulleted list and add tag.
                    $("#dialog #additionalMetadataAssignment").append("<ul>" + tag + "</ul>");
                } else {
                    // metadata has aleady been added and bulleted list exists.

                    // trying to add an already existing metadata item: show error message
                    if ($(additionalMetadataInputTags)
                            .find("li input[name=\"" + additionalMetadataSelected + "\"]").length
                        > 0) {
                        $("#additionalMetadataAssignment")
                            .find("label[for=\"additionalMetadataAssignment\"].error").text(
                            "'" + getMetadataDisplayName(additionalMetadataSelected)
                            + "' has already been selected.");
                    } else {
                        // append new tag
                        $(additionalMetadataInputTags).append(tag);
                    }
                }

                // Removing an additional metadata item
            } else {
                $("#additionalMetadataAssignment")
                    .find("label[for=\"additionalMetadataAssignment\"].error").text(""); // get rid
                                                                                         // of any
                                                                                         // global
                                                                                         // error
                                                                                         // messages

                if ($(additionalMetadataInputTags)
                        .find("li input[name=\"" + additionalMetadataSelected + "'\"]").length
                    == 0) {
                    // trying to add an remove a metadata item that doesn't exist: show error
                    // message
                    $("#additionalMetadataAssignment")
                        .find("label[for=\"additionalMetadataAssignment\"].error").text(
                        "'" + getMetadataDisplayName(additionalMetadataSelected)
                        + "' has NOT been selected and therefore cannot be removed.");
                } else {
                    // remove existing metadata item
                    $(additionalMetadataInputTags)
                        .find("li input[name=\"" + additionalMetadataSelected + "\"]").parents("li")
                        .remove();
                    var listChildren = $(additionalMetadataInputTags).find("li");
                    if ($(listChildren).length <= 0) {
                        $(additionalMetadataInputTags).remove("ul");
                    }
                    // remove from session as well
                    removeItemFromSessionString(sessionKey + "Metadata",
                                                additionalMetadataSelected);
                }
            }
            // bind the events for the newly created additional metadata input tags
            bindAdditionalMetadataEvents(sessionKey);
        });
}

/**
 * This function binds events associated with the additional metadata entries added to the dialog
 * DOM.
 *
 * @param sessionKey  The key used to store the data in the session.
 */
function bindAdditionalMetadataEvents(sessionKey) {
    // additional metadata entries
    $("#dialog #additionalMetadataAssignment ul li input").on("focusout", function () {
        var tagname = $(this).attr("name");
        var tagValue = $(this).attr("value");

        $(this).parents("li").find("label[for=\"" + tagname + "\"].error").text(""); // get rid of
                                                                                     // any error
                                                                                     // messages

        // concatenation the entered value to any existing Metadata values pulled from the session
        var metadataString = buildStringForSession(sessionKey + "Metadata", tagname, tagValue);

        // update the data in the session
        addToSession(sessionKey + "Metadata", metadataString);

        // validate user input 
        validateVariableData(sessionKey);
    });
}

/**
 * This function binds events associated with the unit builder added to the dialog DOM.
 *
 * @param sessionKey  The key used to store the data in the session.
 */
function bindUnitBuildEvents(sessionKey) {
    // unit builder data type selection
    $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitBuilderDataType\"]")
        .on("change", function () {
            if ($(this).attr("value") != "") {
                addUnitBuilderOptionsToDom($(this).attr("value"));
            }
        });

    // unit builder chooser 
    $("#dialog #unitBuilder img#unitBuilderChooser").bind("click", function () {

        var unitString;

        // get the user selected values of the unit chooser
        var unitSelected = $("#dialog #unitBuilder select[name=\"unitSelected\"]").val();
        // for time related units
        unitSelected = unitSelected.replaceAll(":", colonReplacement);

        var prefixSelected = $("#dialog #unitBuilder select[name=\"unitPrefix\"]").val();
        if (prefixSelected != null) {
            unitString = prefixSelected + unitSelected;
        } else {
            unitString = unitSelected;
        }

        // get what is in the units from session
        var unitsInSession = getItemEntered(sessionKey + "Metadata", "units");
        if (unitsInSession == null) {
            unitsInSession = "";
        }

        // Adding to units 
        if ($(this).attr("alt") == "Add To Units") {

            $(this).parents("li").find("label[for=\"units\"].error").text(""); // get rid of any
                                                                               // errors

            unitsInSession = unitsInSession + unitString;

            // concatenation the entered value to any existing Metadata values pulled from the
            // session
            var metadataString = buildStringForSession(sessionKey + "Metadata", "units",
                                                       unitsInSession);

            // update the data in the session
            addToSession(sessionKey + "Metadata", metadataString);

            // update units display in dialog to show new value
            $("#dialog #requiredMetadataAssignment input[name=\"units\"]")
                .attr("value", unitsInSession.replaceAll(colonReplacement, ":"));

            // Removing from units
        } else {
            var index = unitsInSession.lastIndexOf(unitString);
            if (index >= 0) {
                // lame
                var pre = unitsInSession.substring(0, index);
                var post = unitsInSession.substring(index + unitString.length);

                // concatenation the entered value to any existing Metadata values pulled from the
                // session
                var metadataString = buildStringForSession(sessionKey + "Metadata", "units",
                                                           pre + post);

                // update the data in the session
                addToSession(sessionKey + "Metadata", metadataString);

                // update units display in dialog to show new value
                $("#dialog #requiredMetadataAssignment input[name=\"units\"]")
                    .attr("value", pre + post);

            } else {
                $("#dialog #requiredMetadataAssignment #unitBuilder").find("label.error").text(
                    "'" + unitString
                    + "' has NOT been detected in the current units and therefore cannot be removed.");
            }
        }

    });

}

/**
 * This function binds events associated with the metadata import feature added to the dialog DOM.
 *
 * @param sessionKey  The key used to store the data in the session.
 */
function bindImportMetdataEvents(sessionKey) {
    // metadata import selection
    $("#dialog #existingMetadataImporter select[name=\"existingMetadataChoice\"]")
        .on("change", function () {
            if ($(this).attr("value") != "") {
                var name = getFromSession($(this).attr("value"));
                addToSession(sessionKey, name);
                var metadata = getFromSession($(this).attr("value") + "Metadata");
                addToSession(sessionKey + "Metadata", metadata);
                populateDataFromSession(sessionKey);
            }
        });
}

/**
 * FUNCTIONS THAT CREATE/ADD HTML TO DOM
 **/

/**
 * This function adds HTML input tags with which the user will provide the
 * data associated with the column. This HTML is added to the dialog DOM
 * and the event handlers for the inserted HTML are bound.
 *
 * @param sessionKey  The key used to store the data in the session.
 */
function addContentToDialog(sessionKey) {
    $("#dialog").empty(); // start off with a dialog box free of content

    var dialogContent = "<div id=\"variableNameTypeAssignment\">\n" +
                        " <h3>What would you like to do with this column of data?</h3>\n" +
                        " <label class=\"error\"></label>" +
                        " <ul class=\"half\">\n" +
                        "  <li>\n" +
                        "   <label>\n" +
                        "    <input type=\"radio\" name=\"variableNameType\" value=\"assign\"/> Assign a variable name\n"
                        +
                        "   </label>\n" +
                        "   <label id=\"variableNameAssignment\">\n" +
                        "    <input type=\"text\" name=\"variableName\" value=\"\"/>\n" +
                        "   </label>\n" +
                        "   <label class=\"existingMetadataImporter hideMe\">\n" +
                        "     use metadata from another column?  \n" +
                        "    <input type=\"checkbox\" name=\"existingMetadataImporter\" value=\"true\"/> \n"
                        +
                        "   </label>\n" +
                        "   <div id=\"existingMetadataImporter\" class=\"hideMe\">\n" +
                        "    <label for=\"existingMetadataChoice\" class=\"whole\">\n" +
                        "     Import metadata from: \n" +
                        "     <select name=\"existingMetadataChoice\">\n" +
                        "     </select>\n" +
                        "    </label>\n" +
                        "   </div>\n" +
                        "  </li>\n" +
                        "  <li>\n" +
                        "   <label>\n" +
                        "    <input type=\"radio\" name=\"variableNameType\" value=\"do_not_use\"/> Do not use this column of data\n"
                        +
                        "   </label>\n" +
                        "  </li>\n" +
                        " </ul>\n" +
                        "</div>\n" +
                        "<div id=\"variableAttributes\">\n" +
                        " <div id=\"coordinateVariableAssignment\">\n" +
                        "  <h3>Is this variable a coordinate variable? (examples: latitude, longitude, time)</h3>\n"
                        +
                        "  <ul class=\"third\">\n" +
                        "   <li>\n" +
                        "    <label>\n" +
                        "     <input type=\"radio\" name=\"isCoordinateVariable\" value=\"coordinate\"/> Yes\n"
                        +
                        "    </label>\n" +
                        "   </li>\n" +
                        "   <li>\n" +
                        "    <label>\n" +
                        "     <input type=\"radio\" name=\"isCoordinateVariable\" value=\"non-coordinate\"/> No\n"
                        +
                        "    </label>\n" +
                        "   </li>\n" +
                        "  </ul>\n" +
                        "  <label class=\"error\"></label>" +
                        " </div>\n" +
                        " <div id=\"coordinateVarTypeAssignment\">\n" +
                        "  <h3>What type of coordinate variable?</h3>\n" +
                        "  <label for=\"coordinateVarTypeAssignment\" class=\"error\"></label>\n" +
                        "  <select name=\"coordVarType\">\n" +
                        "    <option value=\"\">---- select one ----</option>\n" +
                        "    <option value=\"lat\">latitude</option>\n" +
                        "    <option value=\"lon\">longitude</option>\n" +
                        "    <option value=\"alt\">altitude</option>\n" +
                        "    <option value=\"relTime\">Relative time (i.e. days since 1970-01-01)</option>\n"
                        +
                        "    <option value=\"fullDateTime\">Full date and time string</option>\n" +
                        "    <option value=\"dateOnly\">Date only (year, month, and/or day)</option>\n"
                        +
                        "    <option value=\"timeOnly\">Time only (hour, minute, second, and/or millisecond)</option>\n"
                        +
                        "  </select>\n" +
                        " </div>\n" +
                        " <div id=\"dataTypeAssignment\">\n" +
                        "  <h3>Specify variable data type:</h3>\n" +
                        "  <ul class=\"third\">\n" +
                        "   <li>\n" +
                        "    <label>\n" +
                        "     <input type=\"radio\" name=\"dataType\" value=\"Integer\"/> Integer\n"
                        +
                        "    </label>\n" +
                        "   </li>\n" +
                        "   <li>\n" +
                        "    <label>\n" +
                        "     <input type=\"radio\" name=\"dataType\" value=\"Float\"/> Float (decimal)\n"
                        +
                        "    </label>\n" +
                        "   </li>\n" +
                        "   <li>\n" +
                        "    <label>\n" +
                        "     <input type=\"radio\" name=\"dataType\" value=\"Text\"/> Text\n" +
                        "    </label>\n" +
                        "   </li>\n" +
                        "  </ul>\n" +
                        "  <label class=\"error\"></label>" +
                        " </div>\n" +
                        " <div id=\"requiredMetadataAssignment\">\n" +
                        "  <h3>Required Metadata:</h3>\n" +
                        "  <ul>\n" +
                        "  </ul>\n" +
                        " </div>\n" +
                        " <div id=\"recommendedMetadataAssignment\">\n" +
                        "  <h3>Recommended Metadata:</h3>\n" +
                        "  <ul>\n" +
                        "  </ul>\n" +
                        " </div>\n" +
                        " <div id=\"additionalMetadataAssignment\">\n" +
                        "  <h3>Additional Metadata:</h3>\n" +
                        "  <label for=\"additionalMetadataAssignment\" class=\"error\"></label> \n"
                        +
                        "  <img src=\"resources/img/add.png\" id=\"additionalMetadataChooser\" alt=\"Add Metadata\" /> \n"
                        +
                        "  <img src=\"resources/img/remove.png\" id=\"additionalMetadataChooser\" alt=\"Remove Metadata\" /> \n"
                        +
                        "  <select name=\"additionalMetadata\">\n" +
                        "  </select>\n" +
                        " </div>\n" +
                        "</div>\n";

    $("#dialog").append(dialogContent);
    addMetadataHTMLToDialog(sessionKey); // this is dynamically generated and hence separate from
                                         // the above

    populateDataFromSession(sessionKey);
    bindDialogEvents(sessionKey);
}

/**
 * Create the empty metadata input tags from the eference metadata file and add to DOM.
 *
 * @param sessionKey  The key used to store the data in the session.
 * @param variableType  The variable "type" (coordinate variable or not). Optional.
 */
function addMetadataHTMLToDialog(sessionKey, variableType) {
    // There are a number of times when we need to add the metadata items to the dialog that is NOT
    // a result of a coordinate variable seclection event.  Hence, we need to check the session to
    // see if the user has already specified that information and assign the variableType based on
    // that info.
    if (variableType == undefined) {
        var coordinateVariableSelected = getItemEntered(sessionKey + "Metadata",
                                                        "_coordinateVariable");
        if (coordinateVariableSelected != null) {
            variableType = coordinateVariableSelected;
            if (variableType === "coordinate") {
                enableDiv("coordinateVarTypeAssignment");
            } else {
                disableDiv("coordinateVarTypeAssignment");
                ;
            }
        } else {
            variableType = "coordinate";
            disableDiv("coordinateVarTypeAssignment");
        }
    } else {
        if (variableType === "coordinate") {
            enableDiv("coordinateVarTypeAssignment");
        } else {
            disableDiv("coordinateVarTypeAssignment");
        }
    }
    $("#dialog #requiredMetadataAssignment ul").empty();
    var requiredMetadata = populateMetadataInputTags(sessionKey, variableType, "required");
    $("#dialog #requiredMetadataAssignment ul").append(requiredMetadata);

    $("#dialog #recommendedMetadataAssignment ul").empty();
    var recommendedMetadata = populateMetadataInputTags(sessionKey, variableType, "recommended");
    $("#dialog #recommendedMetadataAssignment ul").append(recommendedMetadata);

    $("#dialog #additionalMetadataAssignment select").empty();
    var additionalMetadata = populateMetadataInputTags(sessionKey, variableType, "additional");
    $("#dialog #additionalMetadataAssignment select").append(additionalMetadata);

    // remove any additional metadata items added to the DOM since the additional metadata choices
    // gets redrawn
    $("#dialog #additionalMetadataAssignment ul").remove("ul");

    bindGeneralMetadataEvents(sessionKey);
}

/**
 * Creates the HTML input tag for collecting additional metadata.  This function is called
 * when the user selections a value from the additional metadata chooser, or when
 * pre-populating the dialog form with pre-existing values.
 *
 * @param tagName  The item that goes in the name attribuet of the input tag.
 * @param displayName  The display or "pretty" name for the input tag.
 * @param tagValue  The pre-existing value gleaned from the session (if it exists).
 */
function createAdditionalMetadataTag(tagName, displayName, tagValue) {
    // Create a form tag containing metadata information
    // The addititional metadata sectional has a general error tag associated with it for general
    // errors, and we are creating error tags associated with the input tags for displaying
    // specific errors.
    var tag = "   <li>\n" +
              "    <label for=\"" + tagName + "\" class=\"error\"></label> \n" +
              "    <label>\n" +
              "     " + displayName + "\n" +
              "     <input type=\"text\" name=\"" + tagName + "\" value=\"" + tagValue + "\"/> \n" +
              "    </label>\n" +
              "   </li>\n";
    return tag;
}

/**
 * Creates the initial HTML input tags for the unit builder.
 *
 * @param sessionKey  The key used to store the data in the session.
 */
function createUnitBuilder(sessionKey) {
    var optionTags = "<option value=\"\">---- select one ----</option>";
    // loop through unitBuilderData 
    for (var i = 0; i < unitBuilderData.length; i++) {
        var unitItem = unitBuilderData[i];
        if (unitItem.entry != "prefix") {
            optionTags =
                optionTags + "<option value=\"" + unitItem.entry + "\">" + unitItem.entry
                + "</option>\n";
        }
    }

    // add option tags to selection menu and show to user.
    $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitBuilderDataType\"]")
        .append(optionTags);
    $("#dialog #requiredMetadataAssignment #unitBuilder label[for=\"unitBuilderDataType\"]")
        .removeClass("hideMe");

    var selectionTags = "     <label for=\"unitPrefix\" class=\"hideMe\">\n" +
                        "      unit prefix:\n" +
                        "      <select name=\"unitPrefix\">\n" +
                        "      </select>\n" +
                        "     </label>\n" +
                        "     <label for=\"unitSelected\" class=\"hideMe\">\n" +
                        "      unit:\n" +
                        "      <select name=\"unitSelected\">\n" +
                        "      </select>\n" +
                        "     </label>\n" +
                        "     <img src=\"resources/img/add.png\" id=\"unitBuilderChooser\" alt=\"Add To Units\" class=\"hideMe\" /> \n"
                        +
                        "     <img src=\"resources/img/remove.png\" id=\"unitBuilderChooser\" alt=\"Remove From Units\" class=\"hideMe\"/> \n"
                        +
                        "     <label class=\"error\"></label>";

    // add the additional selection menu tags but hide until activated
    $("#dialog #requiredMetadataAssignment #unitBuilder").append(selectionTags);

    // bind events
    bindUnitBuildEvents(sessionKey);
}

/**
 * Creates the initial HTML input tags for the existing metadata importer.
 *
 * @param sessionKey  The key used to store the data in the session.
 */
function createExistingMetadataImporter(sessionKey) {
    var optionTags = "<option value=\"\">---- select one ----</option>";

    // get variables with metadata           
    var variablesWithMetadata = getVariablesWithMetadata();
    for (var i = 0; i < variablesWithMetadata.length; i++) {
        if (testVariableCompleteness(variablesWithMetadata[i],
                                     getFromSession(variablesWithMetadata[i]))) {
            optionTags =
                optionTags + "<option value=\"" + variablesWithMetadata[i] + "\">" + getFromSession(
                    variablesWithMetadata[i]) + " from column " + variablesWithMetadata[i].replace(
                    "variableName", "") + "</option>\n";
        }
    }

    // add to the DOM
    $("#dialog #existingMetadataImporter select[name=\"existingMetadataChoice\"]")
        .append(optionTags);

    // bind events
    bindImportMetdataEvents(sessionKey);

}

/**
 * Creates the initial HTML input tags for the unit builder.
 *
 * @param dataTypeSelected  The type of data we are building units for.
 */
function addUnitBuilderOptionsToDom(dataTypeSelected) {

    var unitTags = "";
    var prefixTags = "";

    var showPrefixList = true;

    // loop through known unitBuilderData 
    for (var i = 0; i < unitBuilderData.length; i++) {
        var unitItem = unitBuilderData[i];
        if (unitItem.entry == dataTypeSelected) {
            var units = unitItem.unit;
            for (var x = 0; x < units.length; x++) {
                unitTags =
                    unitTags + "<option value=\"" + units[x] + "\">" + units[x] + "</option>\n";
            }
            if (unitItem.use_prefix != "") {
                showPrefixList = false;
            }
        }

        if (unitItem.entry == "prefix") {
            var prefixes = unitItem.unit;
            for (var x = 0; x < prefixes.length; x++) {
                prefixTags =
                    prefixTags + "<option value=\"" + prefixes[x] + "\">" + prefixes[x]
                    + "</option>\n";
            }
        }

    }

    // prefixes
    $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitPrefix\"] option")
        .remove();
    if (showPrefixList) {
        $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitPrefix\"]")
            .append(prefixTags);
        $("#dialog #requiredMetadataAssignment #unitBuilder label[for=\"unitPrefix\"]")
            .removeClass("hideMe");
    } else {
        $("#dialog #requiredMetadataAssignment #unitBuilder label[for=\"unitPrefix\"]")
            .addClass("hideMe");
    }

    // units
    $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitSelected\"] option")
        .remove();
    $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitSelected\"]")
        .append(unitTags);
    $("#dialog #requiredMetadataAssignment #unitBuilder label[for=\"unitSelected\"]")
        .removeClass("hideMe");

    // add/remove buttons 
    $("#dialog #requiredMetadataAssignment #unitBuilder").find("img").each(function () {
        $(this).removeClass("hideMe");
    });
}

/**
 * Creates a text input tag containing the metadata information.
 *
 * @param sessionKey  The key used to store the data in the session.
 * @param tagName  The name attribute of the input tag, which happens to be the id of the metadata
 *     item as it appears in the reference XML file.
 * @param variableValue  The name value assigned to the variable by the user.
 * @param displayName  The user-friendly name of the metadata item as it appears in the reference
 *     XML file.
 * @param metadataNecessity  The importance of the metadata entry (required, recommended or
 *     additonal)
 * @param helpTip The text for for the help tip hover text.
 */
function createTagElement(sessionKey, tagName, variableValue, displayName, metadataNecessity,
                          helpTip) {

    // first thing we do is get any metadata items already stored in the session and assign them to
    // tagValue
    var tagValue = getItemEntered(sessionKey + "Metadata", tagName);
    if (tagValue == null) { // nothing entered yet
        tagValue = "";
        // if the user selected a standard name for the variable we can use this value.
        if (isCFStandardName(variableValue)) {
            if (tagName == "standard_name") {
                tagValue = variableValue;
            }
            // we can also use the defacto units
            if (tagName == "units") {
                tagValue = cfStandardUnits[variableValue];
            }
        }
    }

    // get the display (pretty) name if it exists
    if (displayName == undefined) {
        displayName = tagName;
    }

    // if we are building the units tag, add in the unit builder
    var unitBuilderSelector = "";
    var unitBuilder = "";
    if (tagName == "units") {
        unitBuilderSelector = "    <label class=\"unitBuilder\">\n" +
                              "     show unit builder \n" +
                              "     <input type=\"checkbox\" name=\"unitBuilder\" value=\"true\"/> \n"
                              +
                              "    </label>\n";

        var optionTags = "";
        unitBuilder = "    <div id=\"unitBuilder\" class=\"hideMe\">\n" +
                      "     <label for=\"unitBuilderDataType\" class=\"hideMe whole\">\n" +
                      "      What type of data are we building units for?\n" +
                      "      <select name=\"unitBuilderDataType\">\n" +
                      "      </select>\n" +
                      "     </label>\n" +
                      "    </div>\n";
    }

    // create help tip element, if defined
    var helpTipElement = "";
    if (helpTip != "") {
        helpTipElement = "<img src=\"resources/img/help.png\" alt=\"" + helpTip + "\" />";
    }

    // create the tag!
    var tag;

    if (metadataNecessity == "additional") {
        tag = "<option value=\"" + tagName + "\">" + displayName + "</option>\n";
    } else {
        // if the user has specified the coordinate var type and data type, then enable input tags
        // (otherwise start out disabled)
        var isDisabled = "disabled";
        if (getItemEntered(sessionKey + "Metadata", "_coordinateVariable") != null) {
            if (getItemEntered(sessionKey + "Metadata", "dataType") != null) {
                isDisabled = "";
            }
        }

        tag = "   <li>\n" +
              "    <label for=\"" + tagName + "\" class=\"error\"></label>" +
              "    <label>\n" +
              "     " + displayName + "\n" +
              "     " + helpTipElement + "\n" +
              "     <input type=\"text\" name=\"" + tagName + "\" value=\"" + tagValue + "\" "
              + isDisabled + "/> \n" +
              "    </label>\n" +
              unitBuilderSelector +
              unitBuilder +
              "   </li>\n";
    }
    return tag;
}

/**
 * Uses the array of know metadata items gleaned from the metadata.xml file.
 * Creates a list of form tags for the required metadata items corresponding to the
 * variable type (coordinate or non-coordinate).
 *
 * @param sessionKey  The key used to store the data in the session.
 * @param variableType  The variable "type" (coordinate variable or not).
 * @param metadataNecessity  The importance of the metadata entry (required, recommended or
 *     additional)
 */
function populateMetadataInputTags(sessionKey, variableType, metadataNecessity) {
    var variableValue = getFromSession(sessionKey);
    var metadataTags = "";

    // loop through known metadata 
    for (var i = 0; i < metadata.length; i++) {
        var metadataItem = metadata[i];
        var tag;

        // Grab the metadata entries that correspond to the variable type (coordinate or
        // non-coordinate),  grab the metadata entries that correspond to the variable necessity
        // (required, recommended, or additional), create a form tag containing that information.
        if (metadataItem.type == variableType) {
            if (metadataItem.necessity == metadataNecessity) {
                tag =
                    createTagElement(sessionKey, metadataItem.entry, variableValue,
                                     metadataItem.displayName, metadataItem.necessity,
                                     metadataItem.helptip);
                metadataTags = metadataTags + tag;
            }
        }

        // Grab the metadata entries that correspond to the both coordinate or non-coordinate
        // variables and create a form tag containing that information.
        if (metadataItem.type == "both") {
            if (metadataItem.necessity == metadataNecessity) {
                tag =
                    createTagElement(sessionKey, metadataItem.entry, variableValue,
                                     metadataItem.displayName, metadataItem.necessity,
                                     metadataItem.helptip);
                metadataTags = metadataTags + tag;
            }
        }
    }
    return metadataTags;
}

/**
 * UTILITY FUNCTIONS
 **/

/**
 * This function gets any of the variable data stored in the
 * session and populates the dialog box with those values.
 *
 * @param sessionKey  The key that will be used to store the variable name value in the session.
 */
function populateDataFromSession(sessionKey) {

    // disable all parts of the dialog content except the first part
    disableVariableAttributes();

    // start of with variable name input hidden
    $("#dialog #variableNameAssignment").addClass("hideMe");

    // get the name of the variable supplied by the user 
    var variableValue = getFromSession(sessionKey);
    if (variableValue) {  // the user has provided something for the variable name or opted not to use the column of data

        if (variableValue != "Do Not Use") { // variable name provided
            var inputTag = $(
                "#dialog #variableNameTypeAssignment input[name=\"variableNameType\"][value=\"assign\"]");
            $(inputTag).attr("checked", true);
            // add variable name input tag 
            $("#dialog #variableNameAssignment").removeClass("hideMe");
            $("#dialog #variableNameAssignment input[name=\"variableName\"]")
                .attr("value", variableValue);
            if (testIfMetadataImportPossible()) {
                // metadata exists for other metadata so show option to import it
                $("#dialog label.existingMetadataImporter").removeClass("hideMe");
            }

            // if they've provided a variable name, then enable the coordinate variable section 
            enableVariableAttributes("coordinateVariableAssignment");

            // from here after, all data collected is stored in the session as "Metadata"
            // if we have metadata in the session, grab it and populate the input tags
            var variableMetadataInSession = getFromSession(sessionKey + "Metadata");
            if (variableMetadataInSession) {

                // coordinate variable
                var coordinateVariableSelected = getItemEntered(sessionKey + "Metadata",
                                                                "_coordinateVariable");
                if (coordinateVariableSelected != null) {
                    // check the appropriate choice and update the metadata options accordingly
                    $("#dialog #coordinateVariableAssignment input[name=\"isCoordinateVariable\"][value=\""
                      + coordinateVariableSelected + "\"]").attr("checked", true);
                    addMetadataHTMLToDialog(sessionKey);

                    var coordinateVariableType = getItemEntered(sessionKey + "Metadata",
                                                                "_coordinateVariableType");
                    if (coordinateVariableType != null) {
                        $("#dialog #coordinateVarTypeAssignment select[name=\"coordVarType\"]")
                            .val(coordinateVariableType);
                    }
                    // data type
                    enableVariableAttributes("dataTypeAssignment");
                    var dataTypeSelected = getItemEntered(sessionKey + "Metadata", "dataType");
                    if (dataTypeSelected != null) {
                        // check the appropriate choice and update the metadata options accordingly
                        $("#dialog #dataTypeAssignment input[name=\"dataType\"][value=\""
                          + dataTypeSelected + "\"]").attr("checked", true);

                        // metadata
                        enableVariableAttributes("requiredMetadataAssignment");
                        enableVariableAttributes("recommendedMetadataAssignment");
                        enableVariableAttributes("additionalMetadataAssignment");

                        // any required and recommended metadata stored in the session gets
                        // inserted when the metadata HTML is added to the dialog DOM need to
                        // populate any additional metadata

                        // get the metadata from the session string, minus the coordinateVariable
                        // and dataType entries
                        var metadataProvided = getAllButTheseFromSessionString(
                            sessionKey + "Metadata", ["_coordinateVariable", "dataType"]);

                        // get the metadata names (not values) held in the session
                        var metadataInSession = getKeysFromSessionData(metadataProvided);

                        for (var i = 0; i < metadataInSession.length; i++) {
                            if (isAdditionalMetadata(coordinateVariableSelected,
                                                     metadataInSession[i])) {

                                var displayName = getMetadataDisplayName(metadataInSession[i]);

                                // see if the user has already provided the value to some of these
                                // metadata items.
                                var tagValue = getItemEntered(sessionKey + "Metadata",
                                                              metadataInSession[i]);

                                var tag = createAdditionalMetadataTag(metadataInSession[i],
                                                                      displayName, tagValue);

                                // at the tag HTML to the dialog DOM
                                var additionalMetadataInputTags = $(
                                    "#dialog #additionalMetadataAssignment ul");

                                if ($(additionalMetadataInputTags).length == 0) {
                                    // no metadata has been added yet, so create bulleted list and
                                    // add tag.
                                    $("#dialog #additionalMetadataAssignment")
                                        .append("<ul>" + tag + "</ul>");
                                } else {
                                    // metadata has aleady been added and bulleted list exists.
                                    //
                                    $(additionalMetadataInputTags).append(tag);
                                }
                            }
                        }
                    }
                }
            }
        } else { // do not use column data selected
            $("#dialog #variableNameTypeAssignment input[name=\"variableNameType\"][value=\"do_not_use\"]")
                .attr("checked", true);

        }
    }
}

/**
 * Tests to see if metadata from another column is available for import.
 * Looks to see if 1) variable metadata exists in the session; and 2) if
 * all the required metadata for that variable is present and not incomplete.
 */
function testIfMetadataImportPossible() {
    // see if any metadata has been entered for other variables           
    var variablesWithMetadata = getVariablesWithMetadata();
    if (variablesWithMetadata.length > 0) {
        for (var i = 0; i < variablesWithMetadata.length; i++) {
            if (testVariableCompleteness(variablesWithMetadata[i],
                                         getFromSession(variablesWithMetadata[i]))) {
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
 * Disables the input tags and de-emphasizes the text of the variable
 * attribute assignment section of the dialog form.
 */
function disableVariableAttributes() {
    // disable all parts of the dialog content except the first part
    $("#dialog #variableAttributes").find("div").each(function () {
        $(this).addClass("inactive");
        $(this).find("input").each(function () {
            $(this).attr("disabled", true);
            if ($(this).attr("type") == "text") {
                $(this).attr("value", "");
            } else { // radio or checkbox
                $(this).attr("checked", false);
            }
        });
    });
}

/**
 * Enables the input tags and emphasizes the text of the variable
 * attribute assignment section of the dialog form.
 *
 * @param dialogDomSection  The section of the dialog dom to enable.
 */
function enableVariableAttributes(dialogDomSection) {
    // enable this section of the dialog content
    $("#dialog #" + dialogDomSection).removeClass("inactive");
    $("#dialog #" + dialogDomSection).find("input").each(function () {
        $(this).attr("disabled", false);
    });
}

function disableDiv(dialogDomSection) {
    // enable this section of the dialog content
    $("#dialog #" + dialogDomSection).addClass("inactive");
    $("#dialog #" + dialogDomSection).find("input").each(function () {
        $(this).attr("disabled", true);
    });
}

function enableDiv(dialogDomSection) {
    // enable this section of the dialog content
    $("#dialog #" + dialogDomSection).removeClass("inactive");
    $("#dialog #" + dialogDomSection).find("input").each(function () {
        $(this).attr("disabled", false);
    });
}

/**
 * Marks a column of data as disabled by changing the css info.
 *
 * @param node  The grid's cell node data.
 */
function disableColumn(node) {
    $(node).addClass("columnDisabled");
}

/**
 * Marks a column of data as enabled by changing the css info.
 *
 * @param node  The grid's cell node data.
 */
function enableColumn(node) {
    $(node).removeClass("columnDisabled");
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
                            if (assignedVariableName == "Do Not Use") {
                                disableColumn(grid.getCellNode(x, (i + 1)));
                            } else {
                                enableColumn(grid.getCellNode(x, (i + 1)));
                            }
                        }
                    }
                } else {
                    for (var x = 0; x < rowLength; x++) {
                        if (x != firstHeaderLine) {
                            if (assignedVariableName == "Do Not Use") {
                                disableColumn(grid.getCellNode(x, (i + 1)));

                            } else {
                                enableColumn(grid.getCellNode(x, (i + 1)));
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
 * if the variable data can be found in the session.
 *
 * @param id  The column number.
 * @param variableName  The name of the variable assigned to by the user.
 */
function hackGridButtonElement(id, variableName) {
    var buttonElement = $("div[title=\"data column " + id + "\"].slick-header-button");
    if (buttonElement.length > 0) {
        $(buttonElement).removeClass("todo").addClass("done");
        $(buttonElement).attr("title", "column " + id + ": " + variableName);
    }
}

/**
 * Checks to see if the needed input was supplied by the user by checking the session.
 * If the input exists, the next button is activated so the user can proceed.
 *
 * @param colNumber  The total number of SlickGrid columns.
 */
function testIfComplete(colNumber) {
    // loop through all the data columns
    for (var i = 0; i < colNumber; i++) {
        // see if we have values for the column in the session
        var variableName = getFromSession("variableName" + i);
        // something exists in the session, see if the metadata exists as well
        if (variableName) {
            if (testVariableCompleteness("variableName" + i, variableName)) {
                if (i == (colNumber - 1)) {
                    $("#faux").remove();
                    $(".jw-button-next").removeClass("hideMe");
                } else {
                    continue;
                }
                // not all metadata is present: break
            } else {
                break;
            }
            // no value in the session, not done yet: break
        } else {
            break;
        }
    }
}

/**
 * Checks to see if the needed input for a particular variable/column of data is complete.
 * Checks to make sure that the coordinate variable, data type and required metadata values
 * are present in the session.  This function is called by the testIfComplete() function.
 *
 * @param sessionKey  The key used to store the data in the session.
 * @param variableType  The variable name assigned to the column of data by the user.
 */
function testVariableCompleteness(sessionKey, variableName) {
    if (variableName != "Do Not Use") {
        // do we have the coordinateVariable?
        var coordinateVariableInSession = getItemEntered(sessionKey + "Metadata",
                                                         "_coordinateVariable");
        if (coordinateVariableInSession != null) {
            // do we have the dataType?
            var dataTypeInSession = getItemEntered(sessionKey + "Metadata", "dataType");
            if (dataTypeInSession != null) {
                // do we have the required metadata?
                var metadataProvided = getAllButTheseFromSessionString(sessionKey + "Metadata",
                                                                       ["_coordinateVariable",
                                                                        "dataType"]);
                if (metadataProvided.length > 0) {
                    // get the metadata names (not values) held in the session
                    var metadataInSession = getKeysFromSessionData(metadataProvided);
                    var requiredMetadata = getKnownRequiredMetadataList(
                        coordinateVariableInSession);
                    for (var i = 0; i < requiredMetadata.length; i++) {
                        if (metadataInSession.indexOf(requiredMetadata[i]) < 0) {
                            // some required metadata is missing
                            return false;
                        }
                    }
                    // ALL metadata is missing
                } else {
                    return false;
                }
                // dataType is missing
            } else {
                return false;
            }
            // coordinateVariable is missing
        } else {
            return false;
        }
    }
    return true;
}
