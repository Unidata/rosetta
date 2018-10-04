/**
 * SlickGrid/custom/CommonSlickGridEvents.js
 *
 * Custom SlickGrid functions shared by multiple custom javascript files.
 */
var CommonSlickGridEvents = (function () {

    /**
     * A custom filter used by the SlickGrid dataView to determine how the lines are to be formatted.
     *
     * @param item  The line of data (as an object).
     * @param rows  The populated rows array.
     * @return  true if not a header line or a header line isn't collapsed; otherwise false.
     */
    function headerLineFilter(item, rows) {
        if (item.parent != null) {  // If it's a header line

            // Weed out the non-parent header lines if the parent is collapsed (since they are not
            // showing, we don't need to format them).
            var parent = rows[item.parent];
            if (parent._collapsed) {
                if (item.parent != item.id) {
                    return false;
                }
            }
            // The parent line and the non-parent header lines will be formatted if the parent line
            // isn't collapsed.
            parent = rows[parent.parent];
        }
        return true;
    }

    /**
     * This function listens and binds row events to the SlickGrid.
     * It is responsible for toggling the header lines (expanding
     * or collapsing) based on if the user clicks the toggle icon
     * in the parent header line row.
     *
     * @param grid  The grid to act upon.
     * @param dataView  The data model containing the parsed file data.
     * @param colNumber  The total number of columns in the grid.
     * @param headerLines  The header line number in array format.
     * @param firstHeaderLine  The parent header line.
     */
    function bindGridHeaderLineToggleEvent(grid, dataView, colNumber, headerLines, firstHeaderLine) {
        grid.onClick.subscribe(function (e, args) {
            if ($(e.target).hasClass("toggle")) {
                // Get the event target (parent header line) and determine its state (expanded or
                // collapsed).
                var item = dataView.getItem(args.row);
                if (!item._collapsed) { // currently expanded
                    dataView.getItemMetadata = function (i) {
                        if (i === firstHeaderLine) {
                            // If it is the parent header line, format the row accordingly.
                            return {
                                "cssClasses": "headerRow",
                                "columns": {
                                    1: {
                                        "colspan": colNumber
                                    }
                                }
                            };
                        } else { // not the parent header line: do nothing
                            return null;
                        }
                    };
                    // Toggle the header lines to be collapsed.
                    item._collapsed = true;
                } else { // currently collapsed
                    dataView.getItemMetadata = function (i) {
                        if (jQuery.inArray(i.toString(), headerLines) >= 0) { // format the header line rows
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
                    // Toggle the header lines to be expanded.
                    item._collapsed = false;
                }
                // Update the dataView.
                dataView.updateItem(item.id, item);
                checkIfColumnIsDisabled(colNumber, grid);
                e.stopImmediatePropagation();
            }
        });

        /**
         * Update grid row count
         */
        dataView.onRowCountChanged.subscribe(function (e, args) {
            grid.updateRowCount();
            grid.render();
        });

        /**
         * handle row change
         */
        dataView.onRowsChanged.subscribe(function (e, args) {
            grid.invalidateRows(args.rows);
            grid.render();
        });

    }

    /**
     * This function listens and binds scroll events to the SlickGrid.
     *
     * @param colNumber  The total number of columns in the grid.
     * @param grid  The grid to act upon.
     */
    function bindGridScrollEvent(colNumber, grid) {
        grid.onScroll.subscribe(function (e, args) {
            checkIfColumnIsDisabled(colNumber, grid);
        });
    }

    // Expose these functions.
    return {
        headerLineFilter: headerLineFilter,
        bindGridHeaderLineToggleEvent: bindGridHeaderLineToggleEvent,
        bindGridScrollEvent: bindGridScrollEvent
    };

})();
