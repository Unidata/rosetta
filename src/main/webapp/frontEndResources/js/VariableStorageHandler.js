/**
 * SlickGrid/custom/VariableStorageHandler.js
 * 
 * Module for handling/storing the variable metadata.
 */
var VariableStorageHandler = (function () {


    /**
     * Generic error message function to display storage-related errors for
     * the variable metadata collection step.
     *
     * @param message   Error message to display.
     */
    function displayErrorMessage(message) {
        $("#dialog #variableNameTypeAssignment").find("label.error").text(message + "  Please contact the Rosetta site administrator.");
    }


    /**
     * Private, utility function (not exported).
     * Finds and returns ALL of the stored additional metadata as an object.
     *
     * @param columnNumber  The columnNumber corresponding to the variable we are interested in.
     * @returns The ALL of the additional metadata corresponding to the variable as a JSON object.
     */
    function getAdditionalVariableData(columnNumber) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);

        // Get & return the additional inner object.
        return variable["additional"];
    }

    /**
     * Returns all the variable data in storage (all columns).
     *
     * @returns All the variable data in storage.
     */
    function getAllVariableData() {
        // Get the stored variable data.
        return getStoredVariableMetadata();
    }

    /**
     * Returns the stored Cf standard info match data for the given variable name.
     *
     * @param variableName  The name of the variable to use.
     */
    function getCfStandardMatches(variableName) {
        return JSON.parse(WebStorage.getStoredData(variableName));
    }


    /**
     * Finds and returns the value for the given key in the stored compliance-level
     * (required, recommended, or additional) inner object of the variable.
     * The difference between this method and the ones below is that this method returns a single
     * metadata value for the requested compliance level, whereas the methods below return ALL
     * metadata keys/values (the entired object) of the requested compliance metadata.
     *
     * @param columnNumber  The columnNumber corresponding to the JSON object we are interested in.
     * @param key               The object key to use to get the stored value data.
     * @param complianceLevel   The compliance level (required, recommended, or additional).
     * @return  The specific metadata value for provided key.
     */
    function getComplianceLevelVariableData(columnNumber, key, complianceLevel) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);

        // Get the compliance level inner object.
        var complianceLevelData = variable[complianceLevel];

        // If restoring incomplete data, return undefined (caller will handle it).
        if (complianceLevelData === undefined) {
            return undefined;
        }
        return complianceLevelData[key];
    }

    /**
     * Private, utility function (not exported).
     * Finds and returns ALL of the stored recommended metadata as an object.
     *
     * @param columnNumber  The columnNumber corresponding to the variable we are interested in.
     * @returns The ALL of the recommended metadata corresponding to the variable as a JSON object.
     */
    function getRecommendedVariableData(columnNumber) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);

        // Get & return the recommended inner object.
        return variable["recommended"];
    }

    /**
     * Private, utility function (not exported).
     * Finds and returns ALL of the stored required metadata as an object.
     *
     * @param columnNumber  The columnNumber corresponding to the variable we are interested in.
     * @returns The ALL of the required metadata corresponding to the variable as a JSON object.
     */
    function getRequiredVariableData(columnNumber) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);

        // Get & return the required inner object.
        return variable["required"];
    }

    /**
     * Private, utility function.
     * Retrieved all of the variable metadata from storage with the key "variableMetadata".
     * Parses the retrieved data into proper JSON objects and returns the result.
     *
     * @return The stored variable as a JSON object.
     */
    function getStoredVariableMetadata() {
        // Get the stored variable data.
        var variableMetadata = WebStorage.getStoredData("variableMetadata");
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
     * Retrieves and returns the JSON object from the provided variableMetadata array at index
     * columnNumber.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested
     *     in.
     * @param variableMetadata  Array of JSON objects
     * @return  The variable as a JSON object.
     */
    function getVariable(columnNumber, variableMetadata) {
        // Get the desired variable object.
        var variable = variableMetadata[columnNumber];

        // Sanity check that we are operating on the correct object.
        sanityCheck(columnNumber, variable);

        return variable;
    }

    /**
     * Finds and returns the value for the given key in the stored variable.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object of interest.
     * @param key           The object key to use to get the stored value data.
     * @return  The stored data corresponding to the give key.
     */
    function getVariableData(columnNumber, key) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);

        return variable[key];
    }

    /**
     * Retrieves and returns an array of columns who have stored metadata.
     * Note, those variables specified "DO_NOT_USE" are ignored and not included.
     *
     * @return candidateColumns  Array of columns who have stored metadata.
     */
    function getVariablesWithMetadata() {
        // Place to stash candidate columns who can provided their metadata.
        var candidateColumns = [];

        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();
        for (var i = 0; i < variableMetadata.length; i++) {
            var variable = variableMetadata[i];
            var variableName = variable.name;
            if (variableName !== undefined  && variableName !== "DO_NOT_USE" ) {
                candidateColumns.push(variableName);
            }
        }
        return candidateColumns;
    }

    /**
     * Initializes storage to hold the correct amount of collected variable metadata:
     *     1) If no data is being restored, adds blank objects for each column; OR
     *     2) If data is being restored, verifies an object exists in storage for all
     *        columns & creates blank objects if needed.
     *
     * @param numberOfColumns   The number of columns in the grid.
     */
    function initialize (numberOfColumns) {
        // There could already be data in web storage if:
        //     1) if we've visited this page before; OR
        //     2) if restoring data from a template.
        var variableMetadata = WebStorage.getStoredData("variableMetadata");
        if (variableMetadata !== null) {
            // Something is already in web storage.
            variableMetadata = JSON.parse(variableMetadata);
            // If we're here, the restored data contains one or more 'DO_NOT_USE' entries.
            for (var i = 0; i < numberOfColumns; i++) {
                if (variableMetadata[i] === undefined) {
                    console.log("no column " + i);
                    variableMetadata.push(
                        {
                            "column": i,
                            "name":"DO_NOT_USE",
                            "required": {},
                            "recommended": {},
                            "additional": {}
                        });
                } else {
                    // Restored data may be incomplete.
                    if (variableMetadata[i].required === undefined) {
                        variableMetadata[i].required = {};
                    }
                    if (variableMetadata[i].recommended === undefined) {
                        variableMetadata[i].recommended = {};
                    }
                    if (variableMetadata[i].additional === undefined) {
                        variableMetadata[i].additional = {};
                    }
                }
            }
            // Add updated objects to storage.
            WebStorage.storeData("variableMetadata", JSON.stringify(variableMetadata));

        } else {
            // Nothing is storage yet.
            variableMetadata = [];

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
            // Add the objects to storage.
            WebStorage.storeData("variableMetadata", JSON.stringify(variableMetadata));
        }

    }

    /**
     * Used by the metadata importer.  Copies the data from the donor column to the recipient
     * column.
     *
     * @param donorColumnNumber The column number of the column to take the data from.
     * @param recipientColumnNumber The column number to copy the data to.
     */
    function populateColumnDataWithAnotherColumn(donorColumnNumber, recipientColumnNumber) {

        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the donor object.
        var donorVariable = getVariable(donorColumnNumber, variableMetadata);

        // Get the recipient object to see if a variable name was already entered.
        var recipientVariable = getVariable(recipientColumnNumber, variableMetadata);
        var variableName = recipientVariable.name;

        // Clone the donor variable.
        recipientVariable = JSON.parse(JSON.stringify(donorVariable));

        // Keep the assigned variable name.
        if (variableName !== undefined) {
            recipientVariable.name = variableName;
        }

        // Change the column number.
        recipientVariable["column"] = parseInt(recipientColumnNumber);

        // Update the stored data with updated variable.
        updateStoredVariableData(variableMetadata, recipientVariable, recipientColumnNumber);
    }

    /**
     * Removes any Cf standard name matche data in web storage associated with the provided
     * variable name.
     *
     * @param variableName  The name of the variable to store.
     */
    function removeCfStandardMatches(variableName) {
        WebStorage.removeFromStorage(variableName);
    }

    /**
     * Removes all but the designated entries from the variable and its inner compliance-level
     * objects.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested
     *     in.
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
     * Resets the stored variable data to the initialized state for the given column,
     *
     * @param columnNumber  The column number corresponding to the variable data to reset.
     */
    function resetVariableData(columnNumber) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Reset the object.
        var variable =  {
            "column": columnNumber,
            "required": {},
            "recommended": {},
            "additional": {}
        };

        // Update the stored data with updated variable.
        updateStoredVariableData(variableMetadata, variable, columnNumber);
    }

    /**
     * Confirms that the columnNumber and the column value in the provided variable match (they
     * should).
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested
     *     in.
     * @param variable      The stored variable.
     */
    function sanityCheck(columnNumber, variable) {
        // Just in case the columnNumber is passes as a string.
        if (typeof columnNumber === "string") {
            columnNumber = parseInt(columnNumber);
        }
        // Sanity check that we are operating on the correct object.
        if (columnNumber !==  parseInt(variable.column)) {
            // These should match; if we are here then something has gone very wrong.
            displayErrorMessage("Unable to access information for variable in column " + columnNumber + ".");
        }
    }

    /**
     * Stores the Cf standard name matches for the given variable name.
     *
     * @param variableName  The name of the variable to store.
     * @param jsonString    The corresponding cf standard matches.
     */
    function storeCfStandardMatches(variableName, jsonString) {
        WebStorage.storeData(variableName, jsonString);
    }

    /**
     * Adds metadata to the stored compliance-level (required, recommended, or additional)
     * inner object of the variable.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested
     *     in.
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
     * Adds a metadata to a stored variable.
     *
     * @param columnNumber  The columnNumber corresponding to the the JSON object we are interested
     *     in.
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
     * Checks to see if the needed input for a particular variable/column of data is complete.
     * This function is called by the testIfComplete() function.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     * @param variableName  The variable name assigned to the column of data by the user.
     * @return true if variable has all it's required data; otherwise false.
     */
    function testVariableCompleteness(key, variableName) {
        if (variableName !== "DO_NOT_USE") {

            // Do we have the metadataType?
            var metadataType = getVariableData(key, "metadataType");

            if (metadataType !== undefined) {

                // If coordinate variable.
                if (metadataType === "coordinate") {
                    // Do we have a metadataTypeStructure?
                    var metadataTypeStructure = getVariableData(key, "metadataTypeStructure");

                    if (metadataTypeStructure !== undefined) {
                        // If metadataTypeStructure is vertical.
                        if (metadataTypeStructure === "vertical") {

                            // Do we have a verticalDirection?
                            var verticalDirection = getVariableData(key, "verticalDirection");

                            if (verticalDirection === undefined) {
                                // verticalDirection is missing.
                                return false;
                            }
                        }

                    } else {
                        // metadataTypeStructure is missing.
                        return false;
                    }
                }

                // Do we have the metadataValueType?
                var metadataValueType = getVariableData(key, "metadataValueType");

                if (metadataValueType !== undefined) {
                    // Do we have the required metadata?

                    // Get the list of required metadata items.
                    var required = WebStorage.getStoredData("_v" + key);
                    if (required === null) {
                        VariableComplianceLevelDataHandler.getRequired(key);
                        required = WebStorage.getStoredData("_v" + key);
                    }
                    required = required.split(/,/g);

                    // Get the stored required data.
                    var storedRequired = getRequiredVariableData(key);

                    // If the required metadata object is empty.
                    if ($.isEmptyObject(storedRequired)) {
                        // ALL required metadata is missing.
                        return false;
                    } else {
                        // Some required metadata has been stored; compare to required list.

                        // Create map from the stored required entries.
                        var requiredStoredMap = new Map();
                        Object.keys(storedRequired).forEach(function(key) {
                            requiredStoredMap.set(key, storedRequired[key]);
                        });

                        // Confirm each of the required metadata entries:
                        //      1) matches what is in the required list; and
                        //      2) has a value associated with it.
                        for (var i = 0; i < required.length; i++) {
                            if (requiredStoredMap.has(required[i])){
                                // Get the value of the stored entry.
                                var storedValue = requiredStoredMap.get(required[i]);
                                if (storedValue === "") {
                                    // No value associated with stored entry.
                                    return false;
                                }
                            } else {
                                // Missing a required metadata item.
                                return false;
                            }
                        }
                    }

                } else {
                    // metadataValueType is missing.
                    return false;
                }

            } else {
                // metadataType is missing.
                return false;
            }
        }
        return true;
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
        WebStorage.storeData("variableMetadata", JSON.stringify(variableMetadata));
    }

    /**
     * Determines if there is variable metadata in storage (not a comprehensive check).
     * @returns true if variable metadata exists, otherwise false.
     */
    function variableDataExists() {
        return WebStorage.getStoredData("variableMetadata") !== null;
    }

    // Expose these functions.
    return {
        getAllVariableData:  getAllVariableData,
        getCfStandardMatches: getCfStandardMatches,
        getComplianceLevelVariableData: getComplianceLevelVariableData,
        getVariableData: getVariableData,
        getVariablesWithMetadata: getVariablesWithMetadata,
        initialize: initialize,
        populateColumnDataWithAnotherColumn: populateColumnDataWithAnotherColumn,
        removeCfStandardMatches: removeCfStandardMatches,
        removeNonMetadataTypeEntriesFromVariableData: removeNonMetadataTypeEntriesFromVariableData,
        resetVariableData: resetVariableData,
        storeCfStandardMatches: storeCfStandardMatches,
        storeComplianceLevelVariableData: storeComplianceLevelVariableData,
        storeVariableData: storeVariableData,
        testVariableCompleteness: testVariableCompleteness,
        variableDataExists: variableDataExists
    };
})();
