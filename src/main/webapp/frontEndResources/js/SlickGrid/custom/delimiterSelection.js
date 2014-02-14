/**
 * SlickGrid/custom/delimiterSelection.js
 * 
 * Custom functions that create a SlickGrid instance containing the data 
 * from the file the user uploaded.  The user will use the SlickGrid 
 * instance to verify the delimiters for the file data.
 */



/** 
 * This function creates a SlickGrid displaying the parsed file data by row
 * and delimiter. We try and detect what we think the data delimiter(s) might be 
 * and show the result to the user.  The user can override/influence the delimiter 
 * selection by specifying the delimiter used (non-grid event handled by form input).  
 * Any user input is detected and the grid is redrawn based on that information.
 *
 * @param grid      The SlickGrid variable to act upon.
 * @param fileData  The fileData array containing the row data.
 * @param columns   The column definition (array) for the gris header.
 * @param rows      The empty rows array to be populated and passed to the grid.
 */
function gridForDelimiterSelection(grid, fileData, columns, rows) {
// do stuff
}
