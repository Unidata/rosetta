/**
 * SlickGrid/custom/headerLineSelection.js
 *
 * Custom functions that create a SlickGrid instance containing the data
 * from the file the user uploaded.  The user will use the SlickGrid
 * instance to indicate which lines in the file are header lines.
 */

/**
 * This function listens and binds events to the SlickGrid.
 *
 * @param grid  The grid to act upon.
 * @param step  The current step number in the wizard.
 */
function bindGridHeaderLineSelectionEvent(grid, step) {
    grid.onSelectedRowsChanged.subscribe(function () {
        // Uncheck the 'no header lines available in this file' checkbox if it's been previously checked.
        $("#step" + step + " #noHeaderLines").prop("checked", false);

        // Stash the user input in the session.
        addHeaderLinesToFormData(grid.getSelectedRows().sort(function (a, b) {
            return a - b
        }));

        // Hide the checkbox in the grid column header (for aesthetics reasons).
        $(".slick-column-name :checkbox").addClass("hideMe");  // click of checkboxes in other part of the Grid will show this.

        // remove hideMe class for delimiter selection section.
        $("#delimiters").removeClass("hideMe");

        if ($("input#noHeaderLines").is(':checked') || $("input#headerLineNumbers").val()) {
            // show next button if there is also delimimter data.
            if ($("input#delimiter").is(':checked')) {
               // remove disabled status for submit button.
                $("input[type=submit]#next").removeAttr("disabled");
                // remove disabled class for submit button.
                $("input[type=submit]#next").removeClass("disabled");
            }
        } else {
            // header line info absent.
            // add disabled status for submit button.
            $("input[type=submit]#next").attr("disabled", true);
            // add disabled class for submit button.
            $("input[type=submit]#next").addClass("disabled");
        }

    });
}

/**
 * Adds the user selected header line numbers to the data for value to pass to the server-side.
 *
 * @param headerLines  The header line numbers.
 */
function addHeaderLinesToFormData(headerLines) {
    $(":input#headerLineNumbers").val(headerLines);
}

/**
 * This function listens and binds events to the input box whose name and ID are'noHeaderLines'.
 *
 * @param grid  The grid to act upon.
 * @param step  The current step number in the wizard.
 */
function bindNoHeaderLinesAvailableSelectionEvent(grid, step) {
    // if user specifies that the file contains no header data
    var inputName = "#step" + step + " #noHeaderLines";
    $(inputName).bind("click", function () {

        // Uncheck any selected values in the grid if they exist.
        grid.setSelectedRows([]);

        // Make sure checkbox is actually checked.
        $("#step" + step + " #noHeaderLines").prop('checked', true);

        // remove hideMe class for delimiter selection section.
        $("#delimiters").removeClass("hideMe");

        if ($("input#noHeaderLines").is(':checked') || $("input#headerLineNumbers").val()) {
            // show next button if there is also delimimter data.
            if ($("input#delimiter").is(':checked')) {
               // remove disabled status for submit button.
                $("input[type=submit]#next").removeAttr("disabled");
                // remove disabled class for submit button.
                $("input[type=submit]#next").removeClass("disabled");
            }
        } else {
            // header line info absent.
            // add disabled status for submit button.
            $("input[type=submit]#next").attr("disabled", true);
            // add disabled class for submit button.
            $("input[type=submit]#next").addClass("disabled");
        }
    });

}
