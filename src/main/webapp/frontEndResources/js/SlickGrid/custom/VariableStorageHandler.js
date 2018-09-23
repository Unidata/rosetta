/**
 * SlickGrid/custom/VariableStorageHandler.js
 * 
 * Module for handling storing the variable metadata.
 */
var VariableStorageHandler = (function () {

    function initialize (numberOfColumns) {
        var variableMetadata = [];
        // Create empty variable metadata objects for each column in the grid.
        for (var i = 0; i < numberOfColumns; i++) {
            variableMetadata.push(
            { 
                "column": i,
                "required": {},
                "recommended": {},
                "additional": {}
            }); 
        }
        // Add the objects to the session.
        storeData("variableMetadata", JSON.stringify(variableMetadata));
    }

    /**
     * Adds an entry in the stored variable.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested in.
     * @param key           The key of the attribute to store.
     * @param value         The value of the attribute to store.
     */
    function storeVariableData(columnNumber, key, value) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);

        // Assign the provided object key to the provided value.
        variable[key] = value;

        // Update the stored data with updated variable.
        updateStoredVariableData(variableMetadata, variable, columnNumber);
    }

    /**
     * Finds and removes the value for the given key in the stored variable.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested in.
     * @param key               The object key to use to remove the stored value data.
     */
    function removeVariableData(columnNumber, key) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);

        // delete the entry using the provided object key.
        delete variable[key];

        // Update the stored data with updated variable.
        updateStoredVariableData(variableMetadata, variable, columnNumber);
    }

    /**
     * Finds and returns the value for the given key in the stored variable.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested in.
     * @param key           The object key to use to get the stored value data.
     */
    function getVariableData(columnNumber, key) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);
        return variable[key];
    }

    /**
     * Removes all but the designated entries from the variable and its inner compliance-level objects.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested in.
     */
    function removeNonMetadataTypeEntriesFromVariableData(columnNumber) {
        var keep = ["column", "name", "required", "recommended", "additional", "metadataType", "metadataValueType"];

        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);

        Object.keys(variable).forEach(function(key) {
            if (!keep.includes(key)) {
                // Not one of the chosen, so delete.
                delete variable[key];
            } else {
                if (key === "required" || key === "recommended" || key === "additional") {
                    // Remove attributes of nested compliance level objects.
                    var innerKeep = ["standard_name", "units", "long_name"];
                    var complianceLevelData = variable[key];
                    Object.keys(complianceLevelData).forEach(function(innerKey) {
                        // Remove all of the nested required attributes except the few specified in the innerKeep array.
                        if (!innerKeep.includes(innerKey)) {
                            delete complianceLevelData[innerKey];   
                        }
                    });
                    // Update the compliance level inner object in the variable.
                    variable[key] = complianceLevelData;
                }
            }
        });

        // Update the stored data with updated variable.
        updateStoredVariableData(variableMetadata, variable, columnNumber);

    }

    /**
     * Adds an entry in the stored compliance-level (required, recommended, or additional)
     * inner object of the variable.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested in.
     * @param key           The key of the attribute to store.
     * @param value         The value of the attribute to store.
     * @param complianceLevel   The compliance level (required, recommended, or additional).
     */
    function storeComplianceLevelVariableData(columnNumber, key, value, complianceLevel) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);

        // Get the compliance level inner object.
        var complianceLevelData = variable[complianceLevel];

        // Assign the provided object key to the provided value.
        complianceLevelData[key] = value;

        // Update the compliance level inner object in the variable.
        variable[complianceLevel] = complianceLevelData;

        // Update the stored data with updated variable.
        updateStoredVariableData(variableMetadata, variable, columnNumber);
    }

    /**
     * Finds and removes the value for the given key in the stored compliance-level
     * (required, recommended, or additional) inner object of the variable.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested in.
     * @param key               The object key to use to remove the stored value data.
     * @param complianceLevel   The compliance level (required, recommended, or additional).
     */
    function removeComplianceLevelVariableData(columnNumber, key, complianceLevel) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);

        // Get the compliance level inner object.
        var complianceLevelData = variable[complianceLevel];

        // delete the entry using the provided object key.
        delete complianceLevelData[key];

        // Update the compliance level inner object in the variable.
        variable[complianceLevel] = complianceLevelData;

        // Update the stored data with updated variable.
        updateStoredVariableData(variableMetadata, variable, columnNumber);
    }

    /**
     * Finds and returns the value for the given key in the stored compliance-level
     * (required, recommended, or additional) inner object of the variable.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested in.
     * @param key               The object key to use to get the stored value data.
     * @param complianceLevel   The compliance level (required, recommended, or additional).
     */
    function getComplianceLevelVariableData(columnNumber, key, complianceLevel) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);

        // Get the compliance level inner object.
        var complianceLevelData = variable[complianceLevel];

        return complianceLevelData[key];
    }

    /**
     * Retrieves and returns an array of columns who have stored metadata.
     * Note, those variables specified "Do Not Use" are ignored and not included.
     *
     * @return candidateColumns  Array of columns who have stored metadata.
     */
    function getVariablesWithMetadata() {
        // Place to stash canidate columns who can provided their metadata.
        var candidateColumns = [];

        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();
        for (var i = 0; i < variableMetadata.length; i++) {
            var variable = variableMetadata[i];
            var variableName = variable[name];
            if (variableName !== undefined  && variableName !== "do_not_use" ) {
                candidateColumns.push(variableName);
            }
        }
        return candidateColumns;
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

    /**
     * Confirms that the columnNumber and the column value in the provided variable match (they should).
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested in.
     * @param variable      The stored variable
     */
    function sanityCheck(columnNumber, variable) {
        // Sanity check that we are operating on the correct object.
        if (columnNumber !== variable.column) {
            // These should match; if we are here then something has gone very wrong.
            displayErrorMessage("Unable to access information for variable " + columnNumber + ".");
        }
    }

    /**
     * Private, utility function.
     * Updated the stored variable metadata array with the provided variable.
     *
     * @param variableMetadata  The variable metadata array that holds all the variables in storage.
     * @param variable          The variable to update in the array.
     * @param columnNumber      The index of the variable to update.
     */
    function updateStoredVariableData(variableMetadata, variable, columnNumber) {
        // Update the variableMetadataArray with the new object.
        variableMetadata[columnNumber] = variable;

        // Stringify and stored data.
        storeData("variableMetadata", JSON.stringify(variableMetadata));
    }

    /**
     * Private, utility function.
     * Retrieved all of the variable metadata from storage with the key "variableMetadata".
     * Parses the retrieved data into proper JSON objects and returns the result.
     */
    function getStoredVariableMetadata() {
        // Get the stored variable data.
        var variableMetadata = getStoredData("variableMetadata");

        if (!variableMetadata) {
            // This shouldn't happen; if we are here then something has gone very wrong (this info should be in the stored).
             displayErrorMessage("Unable to access stored variable matadata.");
        } else {
            // Un-stringify.
            return JSON.parse(variableMetadata);
        }
    }

    /**
     * Private, utility function.
     * Retrieves and returns the JSON object from the provided variableMetadata array at index columnNumber.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested in.
     * @param variableMetadata  Array of JSON objects
     */
    function getVariable(columnNumber, variableMetadata) {
        // Get the index number for accessing the object in the array from the columnNumber variable.
        columnNumber = parseInt(columnNumber.replace("variableName", ""));

        // Get the desired object.
        var variable = variableMetadata[columnNumber];

        // Sanity check that we are operating on the correct object.
        sanityCheck(columnNumber, variable);

        return variable;
    }

    function displayErrorMessage(message) {
        $("#dialog #variableNameTypeAssignment").find("label.error").text(message + "  Please contact the Rosetta site administrator.");
    }

    // Expose these functions.
    return {
        initialize: initialize,
        storeVariableData: storeVariableData,
        storeComplianceLevelVariableData: storeComplianceLevelVariableData,
        removeVariableData: removeVariableData,
        removeComplianceLevelVariableData: removeComplianceLevelVariableData,
        removeNonMetadataTypeEntriesFromVariableData: removeNonMetadataTypeEntriesFromVariableData,
        getVariableData: getVariableData,
        getComplianceLevelVariableData: getComplianceLevelVariableData,
        getVariablesWithMetadata: getVariablesWithMetadata,
        testVariableCompleteness: testVariableCompleteness
    };
})();
