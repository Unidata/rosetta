/**
 * SlickGrid/custom/headerLineSelection.js
 *
 * Custom functions that create a SlickGrid instance containing the data
 * from the file the user uploaded.  The user will use the SlickGrid
 * instance to indicate which lines in the file are header lines.
 */

/**
 * This function creates a SlickGrid displaying the parsed file data by row.
 * The user will use the SlickGrid interface to indicate which rows of the
 * file are header lines (this information used in subsequent steps).
 *
 * @param grid  The SlickGrid variable to act upon.
 * @param fileData  The fileData array containing the row data.
 * @param columns  The column definition (array) for the gris header.
 * @param rows  The empty rows array to be populated and passed to the grid.
 * @param step  The current step number in the jWizard interface.
 */
function gridForHeaderRowSelection(grid, fileData, columns, rows, step) {

    // The SlickGrid options for this particular grid.
    var options = {
        editable: false,
        enableAddRow: false,
        enableColumnReorder: false,
        forceFitColumns: true,
    };

    // Add another column for displaying the non-parsed line data.
    columns.push(
        {
            id: "line_data",
            name: "Line Data",
            field: "line_data",
            width: 1000,
            resizable: false
        }
    );

    // Populate rows[] with the fileData and create the grid.
    // Implement the SlickGrid checkboxSelector plugin. Bind 
    // any events associated with the checkboxSelector plugin. 
    $(function () {
        // loop through the fileData and populate rows[]     
        for (var i = 0; i < fileData.length; i++) {
            if (fileData[i] != "") {
                rows[i] = {
                    line_number: i,
                    line_data: fileData[i]
                };
            }
        }

        // initialize the checkboxSelector plugin and add to columns[]
        var checkboxSelector = new Slick.CheckboxSelectColumn({
            cssClass: "slick-cell-checkboxsel"
        });
        columns.unshift(checkboxSelector.getColumnDefinition());

        // initialize the grid and set the selection model
        grid = new Slick.Grid("#headerLineGrid", rows, columns, options);
        var rowModel = new Slick.RowSelectionModel({selectActiveRow: false});
        grid.setSelectionModel(rowModel);

        // add the checkboxSelector to the grid
        grid.registerPlugin(checkboxSelector);

        // if user has landed on this step before and specified what is checked, add that to the
        // grid
        if (sessionStorage.headerLineNumbers) {
            if (sessionStorage.headerLineNumbers == "none") { // user has specified that there are no header lines in the file
                $("#step" + step + " #noHeaderLines").attr('checked', true);
            } else {  // contains actual header line number data
                var lines = sessionStorage.getItem("headerLineNumbers").split(/,/g);
                grid.setSelectedRows(lines.sort(function (a, b) {
                    return a - b
                }));
            }
        }

        // hide the checkbox in the grid column header on initial grid load (for aesthetics reasons)
        $(".slick-column-name :checkbox").addClass("hideMe");

        // bind header line selection events to the grid 
        bindGridHeaderLineSelectionEvent(grid, step);

        // Uncheck the 'no header lines available in this file' checkbox if it's been previously
        // checked.
        bindNoHeaderLinesAvailableSelectionEvent(grid, step);
    });
}

/**
 * This function listens and binds events to the SlickGrid.
 * User input is collected and stored in the session.
 *
 * @param grid  The grid to act upon.
 * @param step  The current step number in the jWizard interface.
 */
function bindGridHeaderLineSelectionEvent(grid, step) {
    grid.onSelectedRowsChanged.subscribe(function () {
        // Uncheck the 'no header lines available in this file' checkbox if it's been previously
        // checked.
        $("#step" + step + " #noHeaderLines").attr('checked', false);

        // Stash the user input in the session.
        addToSession("headerLineNumbers", grid.getSelectedRows().sort(function (a, b) {
            return a - b
        }));

        // Activate the jWizard next button so the user can proceed.
        $("#faux").remove();
        $(".jw-button-next").removeClass("hideMe");

        // Hide the checkbox in the grid column header (for aesthetics reasons).
        $(".slick-column-name :checkbox").addClass("hideMe");  // needed for step 3

    });
}

/**
 * This function listens and binds events to the input box whose
 * name and ID are  'noHeaderLines'.
 * User input is collected and stored in the session.
 *
 * @param grid  The grid to act upon.
 * @param step  The current step number in the jWizard interface.
 */
function bindNoHeaderLinesAvailableSelectionEvent(grid, step) {
    // if user specifies that the file contains no header data
    var inputName = "#step" + step + " #noHeaderLines";
    $(inputName).bind("click", function () {
        // Uncheck any selected values in the grid if they exist.
        grid.setSelectedRows([]);

        // Stash the user input in the session.
        addToSession("headerLineNumbers", "none");

        // Make sure checkbox is actually checked.
        $("#step" + step + " #noHeaderLines").attr('checked', true);

        // Activate the jWizard next button so the user can proceed.
        $("#faux").remove();
        $(".jw-button-next").removeClass("hideMe");

    });

}
