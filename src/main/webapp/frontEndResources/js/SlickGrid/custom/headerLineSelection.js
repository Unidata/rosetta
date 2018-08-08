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
 */
function bindGridHeaderLineSelectionEvent(grid) {
  grid.onSelectedRowsChanged.subscribe(function () {
    // Uncheck the 'no header lines available in this file' checkbox if it's been previously checked.
    $("#customFileTypeAttributes #noHeaderLines").prop("checked", false);
    $("#customFileTypeAttributes #noHeaderLines").prop("value", false);

    // Stash the user input in the session.
    addHeaderLinesToFormData(grid.getSelectedRows().sort(function (a, b) {
      return a - b
    }));

    // Hide the checkbox in the grid column header (for aesthetics reasons).
    $(".slick-column-name :checkbox").addClass("hideMe");  // click of checkboxes in other part of the Grid will show this.

    // remove hideMe class for delimiter selection section.
    $("#delimiters").removeClass("hideMe");

    manageCustomFileButtonNav();

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

    manageCustomFileButtonNav();

  });
}

/**
 * Manages the button navigation between steps based on the custom file attribute state.
 */
function manageCustomFileButtonNav() {
  if ($("#customFileTypeAttributes #noHeaderLines").val() === 'true' || $(
          "input#headerLineNumbers").val()) {
    // show next button if there is also delimimter data.
    if ($("input#delimiter").is(':checked')) {
      // remove disabled status for submit button.
      $("input[type=submit]#Next").removeAttr("disabled");
      // remove disabled class for submit button.
      $("input[type=submit]#Next").removeClass("disabled");
    } else {
      // delimiter unselected for some reason.
      // add disabled status for submit button.
      $("input[type=submit]#Next").attr("disabled", true);
      // add disabled class for submit button.
      $("input[type=submit]#Next").addClass("disabled");
    }
  } else {
    // header line info absent.
    // add disabled status for submit button.
    $("input[type=submit]#Next").attr("disabled", true);
    // add disabled class for submit button.
    $("input[type=submit]#Next").addClass("disabled");
  }

}



