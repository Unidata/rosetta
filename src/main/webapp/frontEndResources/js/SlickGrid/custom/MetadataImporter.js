/**
 * SlickGrid/custom/MetadataImporter.js
 *
 * Module for the creation of & event handling for the metadata importer.
 */
var MetadataImporter = (function () {

    /**
     * This function binds events associated with the metadata import feature added to the dialog DOM.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     */
    function bindImportMetadataEvents(key) {
        // Metadata import selection.
        $("#dialog #metadataImporter select[name=\"metadataChoice\"]").on("change", function () {
            if ($(this).val() !== "") {
                var name = getFromSession($(this).val());
                addToSession(key, name);
                var metadata = getFromSession($(this).val() + "Metadata");
                addToSession(key + "Metadata", metadata);
                populateDataFromStorage(key);
            }
        });
    }

    /**
     * Creates the initial HTML input tags for the metadata importer which will import
     * another column's metadat into the selected column.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     */
    function create(key) {
        var optionTags = "<option value=\"\">---- select one ----</option>";

        // get variables with metadata
        var variablesWithMetadata = VariableStorageHandler.getVariablesWithMetadata();
        for (var i = 0; i < variablesWithMetadata.length; i++) {
            if (VariableStorageHandler.testVariableCompleteness(variablesWithMetadata[i], getFromSession(variablesWithMetadata[i]))) {
                optionTags = optionTags +
                    "<option value=\"" + variablesWithMetadata[i] + "\">" + getFromSession(variablesWithMetadata[i]) +
                    " from column " + variablesWithMetadata[i].replace("variableName", "") +
                    "</option>\n";
            }
        }

        // add to the DOM
        $("#dialog #metadataImporter select[name=\"metadataChoice\"]").append(optionTags);

        // bind events
        bindImportMetadataEvents(key);

    }

    /**
     * Tests to see if metadata from another column is available for import.
     * Looks to see if:
     *      1) there is stored variable metadata for another column; and 
     *      2) if all the required metadata for that variable is present and not incomplete.
     */
    function isImportPossible() {
        // see if any metadata has been entered for other variables
        var variablesWithMetadata = VariableStorageHandler.getVariablesWithMetadata();
        if (variablesWithMetadata.length > 0) {
            for (var i = 0; i < variablesWithMetadata.length; i++) {
                if (VariableStorageHandler.testVariableCompleteness(variablesWithMetadata[i], getFromSession(variablesWithMetadata[i]))) {
                    return true;
                } else {
                    if (i = (variablesWithMetadata.length - 1)) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }

    }


    // Expose these functions.
    return {
        create: create,
        isImportPossible: isImportPossible
    };
    

})();
