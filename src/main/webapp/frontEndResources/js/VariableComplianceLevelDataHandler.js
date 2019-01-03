/**
 * VariableComplianceLevelDataHandler.js
 *
 * Module to handle compliance level data (required, recommended, and additional) for variable metadata.
 */
var VariableComplianceLevelDataHandler = (function () {

    var metadataProfileVariableData = [];

    /**
     * Pushes a metadata profile object onto the metadataProfileVariableData array.
     *
     * @param profile   The metadata profile object to add to the array.
     */
    function addToMetadataProfileVariableData(profile) {
        metadataProfileVariableData.push(profile);
    }

    /**
     * Private, utility function (not exported).
     * Uses the array of compliance level metadata attributes gleaned from server-side metadata profiles.
     * Creates a list of form tags for these items corresponding to the metadataType (coordinate or non-coordinate).
     *
     * @param key  The key used to access the stored variable data.
     * @param complianceLevel  Whether the attribute is required, recommended or additional.
     */
    function populateComplianceLevelInputTags(key, complianceLevel) {        

        var metadataTypeStructure = VariableStorageHandler.getVariableData(key, "metadataTypeStructure");

        // We will stash our created tags here.
        var metadataTags = [];
        // We will stash our required attribute names here.
        var required = [];
        var used = [];
        for (var i = 0; i < metadataProfileVariableData.length; i++) {
            var metadataProfile = metadataProfileVariableData[i];
            var typeStructure = metadataTypeStructure;
            var metadataTypeStructureName = metadataProfile.metadataTypeStructureName;

            if (metadataProfile.complianceLevel === "optional") {
                metadataProfile.complianceLevel = "additional";
            }
            if (metadataProfile.complianceLevel === complianceLevel) {
                if (metadataProfile.metadataType === "CoordinateVariable") {
                   metadataProfile.metadataType = "coordinate";
                } 
                if (metadataProfile.metadataType === "DataVariable") {
                    metadataProfile.metadataType = "non-coordinate";
                }

                if (typeStructure !== undefined) {
                    if (typeStructure === "latitude") {
                        typeStructure = "lat";
                    } else if (typeStructure === "longitude") {
                        typeStructure = "lon";
                    } else if (typeStructure === "vertical") {
                        typeStructure = "depth";
                    } else {
                        typeStructure = "time";
                    }

                    if (typeStructure !== metadataTypeStructureName) {
                        continue;
                    }
                }

                if (!used.includes(metadataProfile.attributeName)) {
                    used.push(metadataProfile.attributeName);
                    var tag = createComplianceLevelTagElement(key, metadataProfile);
                    metadataTags.push(tag);
                    if (complianceLevel === "required" && metadataProfile.complianceLevel === "required") {
                        // Push attribute name onto required array.
                        required.push(metadataProfile.attributeName);
                    }
                }
            }
            
        }
        
        // If compliance level is required, add list to storage.
        if (complianceLevel === "required") {
            WebStorage.storeData("_v" + key, required);
        }

        // Sort the array.
        metadataTags.sort();
        var metadataTagsAsAString = "";
        for (var x = 0; x < metadataTags.length; x++) {
            metadataTagsAsAString = metadataTagsAsAString + metadataTags[x];
        }
        return metadataTagsAsAString;
    }

    /**
     * Determines the required metadata needed for the column of the given key.
     * Adds the required metadata for that column to storage.
     *
     * @param key  The key used to access the stored variable data.
     */
    function getRequired(key) {
        var used = [];
        var required = [];
        for (var i = 0; i < metadataProfileVariableData.length; i++) {
            var metadataProfile = metadataProfileVariableData[i];
            if (!used.includes(metadataProfile.attributeName)) {
                used.push(metadataProfile.attributeName);
                if (metadataProfile.complianceLevel === "required") {
                    // Push attribute name onto required array.
                    required.push(metadataProfile.attributeName);
                }
            }
        }
        WebStorage.storeData("_v" + key, required);
    }


    /**
     * Creates and returns the compliance-level data tag for the dialog for the given column
     * and metadata item.
     *
     * @param key  The key used to access the stored variable data.
     * @param metadataItem  The metadata attribute item taken from the metadata profile.
     * @return  The compliance level tag.
     */
    function createComplianceLevelTagElement(key, metadataItem) {
        // Pull out the relevant data items for the metadataItem object and assign to variables.
        var tagName = metadataItem.attributeName;
        var displayName = metadataItem.attributeName.replace(/_/g, " ");
        if (metadataItem.displayName) {
            displayName = metadataItem.displayName;
        } 
        var helpTip = "";
        if (metadataItem.description) {
            helpTip = metadataItem.description;
        } 
        if (metadataItem.exampleValues) {
            helpTip = helpTip + "  Examples: " + metadataItem.exampleValues;
        } 
        var complianceLevel = "additional"; // Default
        if (metadataItem.complianceLevel) {
            complianceLevel = metadataItem.complianceLevel;
        }

        // Assign any matching stored compliance level data to the tagValue.
        var tagValue = VariableStorageHandler.getComplianceLevelVariableData(key, tagName, complianceLevel);
        if (tagValue === undefined) {
            tagValue="";
        }
    
        // If we are building the units tag, add in the unit builder.
        var unitBuilderSelector = "";
        var unitBuilder = "";
        if (tagName === "units") {
            unitBuilderSelector = UnitBuilder.createShowUnitBuilderSelector();
            unitBuilder = UnitBuilder.createUnitBuilderTypeSelector();
        }

        // Create help tip element, if defined.
        var helpTipElement = "";
        if (helpTip !== "") {
            helpTipElement = "<img src=\"resources/img/help.png\" alt=\"" + helpTip + "\" />";
        }

        // Create the tag!
        var tag = 
            "   <li>\n" +
            "    <label for=\"" + tagName + "\" class=\"error\"></label>" +
            "    <label>\n" +
            "     " + displayName + "\n" +
            "     " + helpTipElement + "\n" +
            "     <input id=\"" + tagName + "\" type=\"text\" name=\"" + tagName + "\" value=\"" + tagValue + "\" /> \n" +
            "    </label>\n" + unitBuilderSelector + unitBuilder +
            "   </li>\n";

        return tag;
    }   

    /**
     * Create the compliance-level input tags from the metadata profile data and add to DOM.
     *
     * @param key  The key used to access the stored variable data.
     */
    function addComplianceLevelDataToDialog(key) {

        // Add required attribute tags to DOM and bind events.
        $("#dialog #requiredMetadataAssignment ul").empty();
        var requiredMetadata = populateComplianceLevelInputTags(key, "required");
        if (requiredMetadata !== "") {
            $("#dialog #requiredMetadataAssignment").removeClass("hideMe");
            DialogDomHandler.enableDiv("requiredMetadataAssignment");
            $("#dialog #requiredMetadataAssignment ul").append(requiredMetadata);
            bindRequiredMetadataEvents(key);
        } else {
            $("#dialog #requiredMetadataAssignment").addClass("hideMe");
        }

        // Add recommended attribute tags to DOM and bind events.
        $("#dialog #recommendedMetadataAssignment ul").empty();
        var recommendedMetadata = populateComplianceLevelInputTags(key, "recommended");
        if (recommendedMetadata !== "") {
            $("#dialog #recommendedMetadataAssignment").removeClass("hideMe");
            DialogDomHandler.enableDiv("recommendedMetadataAssignment");
            $("#dialog #recommendedMetadataAssignment ul").append(recommendedMetadata);
            bindRecommendedMetadataEvents(key);
        } else {
            $("#dialog #recommendedMetadataAssignment").addClass("hideMe");
        }
        
        // Add additional attribute tags to DOM and bind events.
        $("#dialog #additionalMetadataAssignment ul").empty();
        var additionalMetadata = populateComplianceLevelInputTags(key, "additional");
        if (additionalMetadata !== "") {
            $("#dialog #additionalMetadataAssignment").removeClass("hideMe");
            DialogDomHandler.enableDiv("additionalMetadataAssignment");
            $("#dialog #additionalMetadataAssignment ul").append(additionalMetadata);
            bindAdditionalMetadataEvents(key);
        } else {
            $("#dialog #additionalMetadataAssignment").addClass("hideMe");
        }

        // Bind unit builder events.
        UnitBuilder.bindUnitBuilderEvent(key);
    }

    /**
     * Private, utility function (not exported).
     * This function binds events associated with required compliance-level metadata entries added to the dialog DOM.
     *
     * @param key  The key used to access the stored variable data.
     */
    function bindRequiredMetadataEvents(key) {
        $("#dialog #requiredMetadataAssignment input[type=\"text\"]").on("focusout", function () {

            // Get rid of any prior error messages.
            $(this).parents("li").find("label.error").text("");

            // Assign the attributeName & attributeValue to the user input.
            var attributeName = $(this).attr("name");
            var attributeValue = $(this).val();

            // Update the stored variable data with the required attribute.
            VariableStorageHandler.storeComplianceLevelVariableData(key, attributeName, attributeValue, "required");
    
            // validate user input
            //validateVariableData(key);
        });
    }

    /**
     * Private, utility function (not exported).
     * This function binds events associated with recommended compliance-level metadata entries added to the dialog DOM.
     *
     * @param key  The key used to access the stored variable data.
     */
    function bindRecommendedMetadataEvents(key) {
        $("#dialog #recommendedMetadataAssignment input[type=\"text\"]").on("focusout", function () {
             // Get rid of any prior error messages.
            $(this).parents("li").find("label.error").text("");

            // Assign the attributeName & attributeValue to the user input.
            var attributeName = $(this).attr("name");
            var attributeValue = $(this).val();

            // Update the stored variable data with the recommended attribute.
            VariableStorageHandler.storeComplianceLevelVariableData(key, attributeName, attributeValue, "recommended");
    
            // validate user input
            //validateVariableData(key);
        });
    }

    /**
     * * Private, utility function (not exported).
     * This function binds events associated with additional compliance-level metadata entries added to the dialog DOM.
     *
     * @param key  The key used to access the stored variable data.
     */
    function bindAdditionalMetadataEvents(key) {
        $("#dialog #additionalMetadataAssignment input[type=\"text\"]").on("focusout", function () {

             // Get rid of any prior error messages.
            $(this).parents("li").find("label.error").text("");

            // Assign the attributeName & attributeValue to the user input.
            var attributeName = $(this).attr("name");
            var attributeValue = $(this).val();

            // Update the stored variable data with the recommended attribute.
            VariableStorageHandler.storeComplianceLevelVariableData(key, attributeName, attributeValue, "additional");
    
            // validate user input
            //validateVariableData(key);
        });
    }

    // Expose these functions.
    return {
        addToMetadataProfileVariableData:  addToMetadataProfileVariableData,
        addComplianceLevelDataToDialog: addComplianceLevelDataToDialog,
        getRequired: getRequired
    };
})();


