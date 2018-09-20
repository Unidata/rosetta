/**
 * SlickGrid/custom/variableMetadata.js
 * 
 * Module for handling variableMetadata
 */
var variableMetadata = (function () {

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

    function getVariableData(columnNumber, key) {
        // Get the stored variable data.
        var variableMetadata = getStoredVariableMetadata();

        // Get the desired object.
        var variable = getVariable(columnNumber, variableMetadata);
        return variable[key];
    }

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


    function getVariable(columnNumber, variableMetadata) {
        // Get the index number for accessing the object in the array from the columnNumber variable.
        columnNumber = parseInt(columnNumber.replace("variableName", ""));

        // Get the desired object.
        var variable = variableMetadata[columnNumber];

        // Sanity check that we are operating on the correct object.
        sanityCheck(columnNumber, variable);

        return variable;
    }

    function sanityCheck(columnNumber, variable) {
        // Sanity check that we are operating on the correct object.
        if (columnNumber !== variable.column) {
            // These should match; if we are here then something has gone very wrong.
            displayErrorMessage("Unable to access information for variable " + columnNumber + ".");
        }
    }

    function updateStoredVariableData(variableMetadata, variable, columnNumber) {
        // Update the variableMetadataArray with the new object.
        variableMetadata[columnNumber] = variable;

        // Stringify and stored data.
        storeData("variableMetadata", JSON.stringify(variableMetadata));
    }

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
        getVariableData: getVariableData
    };
})();
