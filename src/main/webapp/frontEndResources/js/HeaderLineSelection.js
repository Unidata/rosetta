/**
 * HeaderLineSelection.js
 *
 * Custom functions that create a SlickGrid instance containing the data
 * from the file the user uploaded.  The user will use the SlickGrid
 * instance to indicate which lines in the file are header lines.
 */
var HeaderLineSelection = (function () {

    /**
     * This function listens and binds events to the SlickGrid.
     *
     * @param grid  The grid to act upon.
     */
    function bindGridHeaderLineSelectionEvent(grid) {
        grid.onSelectedRowsChanged.subscribe(function () {
            // Uncheck the 'no header lines available in this file' checkbox if it's been
            // previously checked.
            $("#customFileTypeAttributes #noHeaderLines").prop("checked", false);
            $("#customFileTypeAttributes #noHeaderLines").prop("value", false);

            // Stash the user input in storage.
            addHeaderLinesToFormData(grid.getSelectedRows().sort(function (a, b) {
                return a - b
            }));

            // Hide the checkbox in the grid column header (for aesthetics reasons).
            $(".slick-column-name :checkbox").addClass("hideMe");  // click of checkboxes in other
                                                                   // part of the Grid will show
                                                                   // this.

            // Remove hideMe class for delimiter selection section.
            $("#delimiters").removeClass("hideMe");
            bindDelimiterSelection();

            // Activate navigation buttons between wizard steps if warranted.
            manageCustomFileButtonNav();

        });
    }

    /**
     * Private, utility function (not exported).
     * Adds the user selected header line numbers to the data for value to pass to the server-side.
     *
     * @param headerLines  The header line numbers.
     */
    function addHeaderLinesToFormData(headerLines) {
        $(":input#headerLineNumbers").val(headerLines);
    }

    /**
     * Private, utility function (not exported).
     * This function listens and binds events to delimiter input tags.
     */
    function bindDelimiterSelection() {
        $("input#delimiter").change(function () {
            manageCustomFileButtonNav();
        });
    }


    /**
     * This function listens and binds events to the input box whose name and ID are 'noHeaderLines'.
     *
     * @param grid  The grid to act upon.
     */
    function bindNoHeaderLinesAvailableSelectionEvent(grid) {
        // if user specifies that the file contains no header data
        var inputName = "#customFileTypeAttributes #noHeaderLines";
        $(inputName).bind("click", function () {

            // Uncheck any selected values in the grid if they exist.
            grid.setSelectedRows([]);
            // Remove any selected headewr line values from hidden form field.
            $("#customFileTypeAttributes input#headerLineNumbers").val("");

            if ($(this).val() === 'true') {
                // Uncheck the checkbox.
                $(this).prop('checked', false);
                $(this).prop('value', false);
            } else {
                // Make sure checkbox is checked.
                $(this).prop('checked', true);
                $(this).prop('value', true);
            }

            // remove hideMe class for delimiter selection section.
            $("#delimiters").removeClass("hideMe");
            bindDelimiterSelection();

            // Activate navigation buttons between wizard steps if warranted.
            manageCustomFileButtonNav();

        });
    }

    /**
     * Private, utility function (not exported).
     * Manages the button navigation between wizard steps based on the custom file attribute state.
     */
    function manageCustomFileButtonNav() {
        if ($("#customFileTypeAttributes #noHeaderLines").val() === 'true' || $(
            "input#headerLineNumbers").val()) {
            // Show next button if there is also delimiter data.
            if ($("input#delimiter").is(':checked')) {
                // Remove disabled status for submit button.
                $("input[type=submit]#Next").removeAttr("disabled");
                // Remove disabled class for submit button.
                $("input[type=submit]#Next").removeClass("disabled");
            } else {
                // Delimiter unselected for some reason.
                // Add disabled status for submit button.
                $("input[type=submit]#Next").attr("disabled", true);
                // Add disabled class for submit button.
                $("input[type=submit]#Next").addClass("disabled");
            }
        } else {
            // Header line info absent.
            // Add disabled status for submit button.
            $("input[type=submit]#Next").attr("disabled", true);
            // Add disabled class for submit button.
            $("input[type=submit]#Next").addClass("disabled");
        }

    }

    // Expose these functions.
    return {
        bindGridHeaderLineSelectionEvent: bindGridHeaderLineSelectionEvent,
        bindNoHeaderLinesAvailableSelectionEvent: bindNoHeaderLinesAvailableSelectionEvent
    };

})();