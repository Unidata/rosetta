/**
 * SlickGrid/custom/headerLineSelection.js
 *
 * Custom functions that create a SlickGrid instance containing the data
 * from the file the user uploaded.  The user will use the SlickGrid
 * instance to indicate which lines in the file are header lines.
 */

/**
 * This function listens and binds events to the SlickGrid.
 * User input is collected and stored in the session.
 *
 * @param grid  The grid to act upon.
 * @param step  The current step number in the jWizard interface.
 */
function bindGridHeaderLineSelectionEvent(grid, step) {
    grid.onSelectedRowsChanged.subscribe(function () {
        // Uncheck the 'no header lines available in this file' checkbox if it's been previously checked.
        $("#step" + step + " #noHeaderLines").attr('checked', false);

        // Stash the user input in the session.
        console.log("headerLineNumbers", grid.getSelectedRows().sort(function (a, b) {
            return a - b
        }));

        // Hide the checkbox in the grid column header (for aesthetics reasons).
        $(".slick-column-name :checkbox").addClass("hideMe");  // click of checkboxes in other part of the Grid will show this.

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

        // Make sure checkbox is actually checked.
        $("#step" + step + " #noHeaderLines").attr('checked', true);
    });

}
