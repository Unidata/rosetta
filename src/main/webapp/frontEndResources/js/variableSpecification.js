/**
 * SlickGrid/custom/variableSpecification.js
 *
 * This big, ugly beasty contains custom functions that create a SlickGrid instance
 * containing the data from the file the user uploaded.  The user will use the
 * SlickGrid instance to input variable attributes corresponding to the data columns.
 */

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
function gridForVariableSpecification(grid, fileData, columns, rows, LineNumberFormatter,
                                      delimiter) {

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
        // Get the header line numbers so we can identify the line type in the fileData array and
        // format accordingly.
        var headerLines = WebStorage.getStoredData("headerLineNumbers").split(/,/g);

        // denote which is the first or "parent" header line that will be shown when the rest of
        // the header lines are collapsed
        var firstHeaderLine = headerLines[0];
        var colNumber;

        // loop through the fileData line by line
        for (var i = 0; i < fileData.length; i++) {
            var parent = null;
            // an unglamorous way to keep track of where we are in the loop
            if (i === 0) {
                bool = 1;
            }

            // create a placeholder object to hold the line data, starting with the line number
            // data for the first column
            var obj = {"line_number": i, "id": i.toString()};

            // test the data against the headerLines array
            if (jQuery.inArray(i.toString(), headerLines) < 0) { // it's not a header line
                var dataItems;
                // Split the data line using the given delimiter.
                if (delimiter === '\\s+') {
                    // Whitespace.
                    dataItems = fileData[i].split(/\s+/);
                    if (dataItems[0] === "") {
                        dataItems.splice(0, 1);
                    }
                } else if (delimiter === '\"') {
                    // Double quotes are handled differently in the slick grid,
                    dataItems = fileData[i].split(/&quot;/);
                } else {
                    // Everything else.
                    dataItems = fileData[i].split(delimiter);
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
                        var variableName = WebStorage.getStoredData(x);
                        if (variableName != null) { // data exists
                            // update the column name to be that of the assigned variable name
                            colObject.name = variableName;
                            if (VariableStorageHandler.testVariableCompleteness(i, variableName)) {
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
                            // here is where we will do our check to see if any data has been
                            // entered prior.
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

        // initialize the data model, set the data items, and apply the headerLineFilter
        dataView = new Slick.Data.DataView({inlineFilters: true});
        dataView.beginUpdate();
        dataView.setItems(rows);
        dataView.setFilter(CommonSlickGridEvents.headerLineFilter);
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

        // Initialize storage to hold the correct amount of collected variable metadata.
        // (If no data is being restored, adds blank objects for each column. if data is
        // being restored, verifies an object exists in storage for all columns & creates
        //  blank objects if needed.)
        VariableStorageHandler.initialize((columns.length - 1));

        // Initialize the grid with the data model
        grid = new Slick.Grid("#variableGrid", dataView, columns, options);

        // Get the variable name, assign to the column and update the header with the value.
        for (var n = 0; n < colNumber; n++) {
            var variableName = VariableStorageHandler.getVariableData(n, "name");
            grid.updateColumnHeader(n, variableName, "column " + n + ": " + variableName);
        }

        // bind header line toggle events to the grid
        CommonSlickGridEvents.bindGridHeaderLineToggleEvent(grid, dataView, colNumber, headerLines,
                                                            firstHeaderLine);

        // Bind generic scroll events to the grid.
        CommonSlickGridEvents.bindGridScrollEvent(colNumber, grid);

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
        var variableKey = id;

        // When the specified command event is triggered, launch the jQuery dialog widget.
        if (args.command === "setVariable") {
            $(function () {
                // Specify jQuery dialog widget options.
                $("#dialog")
                    .dialog({
                        closeOnEscape: false,
                        title: "Enter Variable Attributes",
                        width: 900,
                        modal: true,
                        buttons: {
                            "done": function () {
                                //validateVariableData(variableKey, true);
                                // only if we don't have any errors
                                if ($("#dialog").find("label.error").text() === "") {

                                    // Get the variable name, assign to the column
                                    // and update the header with the value
                                    var variableName = VariableStorageHandler.getVariableData(variableKey, "name");
                                    grid.updateColumnHeader(id, variableName, "column " + id + ": " + variableName);

                                    // Make sure the column is enabled/disabled
                                    // depending on the user's choice.
                                    checkIfColumnIsDisabled(colNumber, grid);

                                    // Have all the columns been handled?
                                    testIfComplete(colNumber);

                                    $(this).dialog("close");
                                }
                            },
                            "cancel": function () {

                                // remove variable info from variableMetadata value field
                                VariableStorageHandler.resetVariableData(variableKey);

                                // Ugh!  Kludge to counter the fact the grid header button resets to
                                // previous options if revisiting dialog.
                                checkIfColumnIsDisabled(colNumber, grid);

                                // Have all the columns been handled?
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

                // Add content to the dialog widget and bind event handlers.
                DialogDomHandler.addContentToDialog(variableKey);
            });
        }
    });
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
    // Loop through all of the data columns
    for (var i = 0; i < colNumber; i++) {
        // Get the variable name assigned by the user for the column
        var assignedVariableName = VariableStorageHandler.getVariableData(i, "name");
        // If we have data for this column
        if (assignedVariableName) {
            var headerLines = WebStorage.getStoredData("headerLineNumbers").split(/,/g);
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
            if (VariableStorageHandler.testVariableCompleteness(i, assignedVariableName)) {
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
    // Loop through all the data columns.
    for (var i = 0; i < colNumber; i++) {
        // Get the variable name assigned by the user for the column
        var assignedVariableName = VariableStorageHandler.getVariableData(i, "name");

        // If column has an assigned name.
        if (assignedVariableName) {
            // If variable has all the required metadata populated OR we are opting not to use the
            // variable.
            if (VariableStorageHandler.testVariableCompleteness(i, assignedVariableName)
                || assignedVariableName === "DO_NOT_USE") {

                // If we've reached the last column.
                if (i === (colNumber - 1)) {
                    populateCmd();
                    // Remove disabled status for submit button.
                    $("input[type=submit]#Next").removeAttr("disabled");
                    // Remove disabled class for submit button.
                    $("input[type=submit]#Next").removeClass("disabled");
                }

            } else { // not all metadata is present: break
                // Add disabled status for submit button.
                $("input[type=submit]#Next").attr("disabled");
                // Add disabled class for submit button.
                $("input[type=submit]#Next").addClass("disabled");
                break;
            }
        } else { // no value stored, not done yet: break
            // Add disabled status for submit button.
            $("input[type=submit]#Next").attr("disabled");
            // Add disabled class for submit button.
            $("input[type=submit]#Next").addClass("disabled");
            break;
        }
    }
}

/**
 * Populates the Spring command object (form-backing object) with the data in web storage.
 */
function populateCmd() {
    // Get the object from storage.
    var storedVariableData = JSON.stringify(VariableStorageHandler.getAllVariableData());
    $("input#variableMetadata").val(storedVariableData);
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


